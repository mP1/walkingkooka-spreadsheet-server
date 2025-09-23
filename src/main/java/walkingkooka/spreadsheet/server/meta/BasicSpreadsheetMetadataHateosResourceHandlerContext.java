/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.server.meta;

import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContextDelegator;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetContext;
import walkingkooka.spreadsheet.meta.SpreadsheetContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserName;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.spreadsheet.server.delta.SpreadsheetDeltaHttpMappings;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserHateosResourceMappings;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.terminal.TerminalContexts;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.Printers;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormName;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link SpreadsheetMetadataHateosResourceHandlerContext} that creates a new {@link SpreadsheetStoreRepository} for unknown {@link SpreadsheetId}.
 * There is no way to delete existing spreadsheets.
 */
final class BasicSpreadsheetMetadataHateosResourceHandlerContext implements SpreadsheetMetadataHateosResourceHandlerContext,
    HateosResourceHandlerContextDelegator,
    EnvironmentContextDelegator,
    SpreadsheetContextDelegator {

    /**
     * Creates a new empty {@link BasicSpreadsheetMetadataHateosResourceHandlerContext}
     */
    static BasicSpreadsheetMetadataHateosResourceHandlerContext with(final AbsoluteUrl serverUrl,
                                                                     final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                     final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                                     final SpreadsheetContext spreadsheetContext,
                                                                     final SpreadsheetProvider systemSpreadsheetProvider) {
        Objects.requireNonNull(serverUrl, "serverUrl");
        Objects.requireNonNull(spreadsheetIdToSpreadsheetProvider, "spreadsheetIdToSpreadsheetProvider");
        Objects.requireNonNull(spreadsheetIdToRepository, "spreadsheetIdToRepository");
        Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext");
        Objects.requireNonNull(spreadsheetContext, "spreadsheetContext");
        Objects.requireNonNull(systemSpreadsheetProvider, "systemSpreadsheetProvider");

        return new BasicSpreadsheetMetadataHateosResourceHandlerContext(
            serverUrl,
            spreadsheetIdToSpreadsheetProvider,
            spreadsheetIdToRepository,
            hateosResourceHandlerContext,
            spreadsheetContext,
            systemSpreadsheetProvider
        );
    }

    private BasicSpreadsheetMetadataHateosResourceHandlerContext(final AbsoluteUrl serverUrl,
                                                                 final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                 final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                                 final SpreadsheetContext spreadsheetContext,
                                                                 final SpreadsheetProvider systemSpreadsheetProvider) {
        super();

        this.serverUrl = serverUrl;

        this.spreadsheetIdToSpreadsheetProvider = spreadsheetIdToSpreadsheetProvider;
        this.spreadsheetIdToRepository = spreadsheetIdToRepository;

        this.hateosResourceHandlerContext = hateosResourceHandlerContext;

        this.spreadsheetContext = spreadsheetContext;

        this.systemSpreadsheetProvider = systemSpreadsheetProvider;
    }

    // SpreadsheetMetadataHateosResourceHandlerContext..................................................................

    @Override
    public SpreadsheetProvider spreadsheetProvider(final SpreadsheetId id) {
        return this.spreadsheetIdToSpreadsheetProvider.apply(id);
    }

    private final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider;

    // hateosRouter.....................................................................................................

    /**
     * Lazily creates a {@link Router} using the {@link SpreadsheetId} to a cache.
     */
    @Override
    public Router<HttpRequestAttribute<?>, HttpHandler> httpRouter(final SpreadsheetId id) {
        Objects.requireNonNull(id, "id");

        Router<HttpRequestAttribute<?>, HttpHandler> hateosRouter = this.spreadsheetIdToHateosRouter.get(id);
        if (null == hateosRouter) {
            hateosRouter = this.createHttpRouter(id);

            this.spreadsheetIdToHateosRouter.put(id, hateosRouter);
        }
        return hateosRouter;
    }

    private final Map<SpreadsheetId, Router<HttpRequestAttribute<?>, HttpHandler>> spreadsheetIdToHateosRouter = Maps.sorted();

    /**
     * Factory that creates a {@link Router} for the given {@link SpreadsheetId spreadsheet}.
     */
    private Router<HttpRequestAttribute<?>, HttpHandler> createHttpRouter(final SpreadsheetId id) {
        final SpreadsheetStoreRepository repository = this.storeRepository(id);

        final SpreadsheetEngine engine = SpreadsheetEngines.stamper(
            SpreadsheetEngines.basic(),
            this::stamp
        );

        final SpreadsheetProvider spreadsheetProvider = this.spreadsheetIdToSpreadsheetProvider.apply(id);

        final SpreadsheetContext spreadsheetContext = this.spreadsheetContext;

        final SpreadsheetMetadata metadata = spreadsheetContext.loadMetadataOrFail(id);

        final ProviderContext providerContext = spreadsheetContext.providerContext();

        final SpreadsheetEngineContext spreadsheetEngineContext = SpreadsheetEngineContexts.basic(
            this.serverUrl,
            metadata,
            repository,
            SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS,
            EnvironmentContexts.map(
                EnvironmentContexts.empty(
                    metadata.locale(),
                    providerContext::now, // share ProviderContext#hasNow
                    providerContext.user() // TODO https://github.com/mP1/walkingkooka-spreadsheet-server/issues/1860
                )
            ),
            spreadsheetContext,
            TerminalContexts.printer(
                Printers.sink(LineEnding.NONE)
            ),
            metadata.spreadsheetProvider(spreadsheetProvider)
        );

        return this.mappings(
            id,
            engine,
            spreadsheetEngineContext,
            this.systemSpreadsheetProvider
        );
    }

    private final SpreadsheetContext spreadsheetContext;

    private SpreadsheetMetadata stamp(final SpreadsheetMetadata metadata) {
        return metadata.set(
            SpreadsheetMetadataPropertyName.AUDIT_INFO,
            this.spreadsheetContext.providerContext()
                .refreshModifiedAuditInfo(
                    metadata.getOrFail(SpreadsheetMetadataPropertyName.AUDIT_INFO)
                )
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> mappings(final SpreadsheetId id,
                                                                  final SpreadsheetEngine engine,
                                                                  final SpreadsheetEngineContext context,
                                                                  final SpreadsheetProvider systemSpreadsheetProvider) {
        final AbsoluteUrl serverUrl = this.serverUrl;
        final UrlPath deltaUrlPath = serverUrl.path()
            .append(
                UrlPathName.with(
                    id.toString()
                )
            );

        final SpreadsheetEngineHateosResourceHandlerContext handlerContext = SpreadsheetEngineHateosResourceHandlerContexts.basic(
            engine,
            this.hateosResourceHandlerContext,
            context,
            systemSpreadsheetProvider
        ).setPreProcessor(
            SpreadsheetMetadataHateosResourceHandlerContexts.spreadsheetDeltaJsonCellLabelResolver(
                context.storeRepository()
                    .labels()
            )
        );

        final HateosResourceMappings<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetCell, SpreadsheetEngineHateosResourceHandlerContext> cell = SpreadsheetDeltaHttpMappings.cell();

        final HateosResourceMappings<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn, SpreadsheetEngineHateosResourceHandlerContext> column = SpreadsheetDeltaHttpMappings.column();

        final HateosResourceMappings<FormName, SpreadsheetDelta, SpreadsheetDelta, Form<SpreadsheetExpressionReference>, SpreadsheetEngineHateosResourceHandlerContext> form = SpreadsheetDeltaHttpMappings.form();

        final HateosResourceMappings<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetLabelMapping, SpreadsheetEngineHateosResourceHandlerContext> label = SpreadsheetDeltaHttpMappings.label();

        final HateosResourceMappings<SpreadsheetMetadataPropertyName<?>,
            SpreadsheetMetadataPropertyNameHateosResource,
            SpreadsheetMetadataPropertyNameHateosResource,
            SpreadsheetMetadataPropertyNameHateosResource,
            SpreadsheetEngineHateosResourceHandlerContext> metadata = SpreadsheetMetadataPropertyNameHateosResourceMappings.spreadsheetEngineHateosResourceHandlerContext();

        final HateosResourceMappings<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet, SpreadsheetParserInfo, SpreadsheetEngineHateosResourceHandlerContext> parser = SpreadsheetParserHateosResourceMappings.engine();

        final HateosResourceMappings<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow, SpreadsheetEngineHateosResourceHandlerContext> row = SpreadsheetDeltaHttpMappings.row();

        return HateosResourceMappings.router(
            deltaUrlPath,
            Sets.of(
                cell,
                column,
                form,
                label,
                metadata,
                parser, // /parser
                row
            ),
            handlerContext
        );
    }

    private final AbsoluteUrl serverUrl;
    private final SpreadsheetProvider systemSpreadsheetProvider;

    @Override
    public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
        return this.spreadsheetIdToRepository.apply(id);
    }

    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository;

    @Override
    public MediaType contentType() {
        return SpreadsheetServerMediaTypes.CONTENT_TYPE;
    }

    // HateosResourceHandlerContext ....................................................................................

    @Override
    public Indentation indentation() {
        return this.hateosResourceHandlerContext.indentation();
    }

    @Override
    public LineEnding lineEnding() {
        return this.hateosResourceHandlerContext.lineEnding();
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final HateosResourceHandlerContext before = this.hateosResourceHandlerContext;
        final HateosResourceHandlerContext after = before.setPreProcessor(processor);

        return before.equals(after) ?
            this :
            new BasicSpreadsheetMetadataHateosResourceHandlerContext(
                this.serverUrl,
                this.spreadsheetIdToSpreadsheetProvider,
                this.spreadsheetIdToRepository,
                after,
                this.spreadsheetContext,
                this.systemSpreadsheetProvider
            );
    }

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    // EnvironmentContext...............................................................................................

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale locale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext setLocale(final Locale locale) {
        Objects.requireNonNull(locale, "locale");

        throw new UnsupportedOperationException();
    }

    @Override
    public <T> SpreadsheetMetadataHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                                   final T value) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");

        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        Objects.requireNonNull(name, "name");

        throw new UnsupportedOperationException();
    }

    // https://github.com/mP1/walkingkooka-spreadsheet-server/issues/1928
    // BasicSpreadsheetMetadataHateosResourceHandlerContext requires EnvironmentContext currently uses "global" ProviderContext
    @Override
    public EnvironmentContext environmentContext() {
        return this.spreadsheetContext.providerContext();
    }

    // SpreadsheetContextDelegator......................................................................................

    @Override
    public SpreadsheetContext spreadsheetContext() {
        return this.spreadsheetContext;
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.serverUrl.toString();
    }
}
