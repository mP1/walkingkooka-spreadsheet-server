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
import walkingkooka.locale.LocaleContext;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContextDelegator;
import walkingkooka.plugin.ProviderContexts;
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
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterInfo;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterInfoSet;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.reference.AnchoredSpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoaders;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.spreadsheet.server.delta.SpreadsheetDeltaHttpMappings;
import walkingkooka.spreadsheet.server.delta.SpreadsheetExpressionReferenceSimilarities;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterHateosResourceMappings;
import walkingkooka.spreadsheet.server.function.ExpressionFunctionHateosResourceMappings;
import walkingkooka.spreadsheet.server.importer.SpreadsheetImporterHateosResourceMappings;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserHateosResourceMappings;
import walkingkooka.spreadsheet.server.validation.ValidationHateosResourceMappings;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormName;
import walkingkooka.validation.provider.ValidatorInfo;
import walkingkooka.validation.provider.ValidatorInfoSet;
import walkingkooka.validation.provider.ValidatorName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link SpreadsheetMetadataHateosResourceHandlerContext} that creates a new {@link SpreadsheetStoreRepository} for unknown {@link SpreadsheetId}.
 * There is no way to delete existing spreadsheets.
 */
final class BasicSpreadsheetMetadataHateosResourceHandlerContext implements SpreadsheetMetadataHateosResourceHandlerContext,
    HateosResourceHandlerContextDelegator,
    ProviderContextDelegator {

    /**
     * Creates a new empty {@link BasicSpreadsheetMetadataHateosResourceHandlerContext}
     */
    static BasicSpreadsheetMetadataHateosResourceHandlerContext with(final AbsoluteUrl serverUrl,
                                                                     final Indentation indentation,
                                                                     final LineEnding lineEnding,
                                                                     final LocaleContext localeContext,
                                                                     final SpreadsheetProvider systemSpreadsheetProvider,
                                                                     final ProviderContext providerContext,
                                                                     final SpreadsheetMetadataStore metadataStore,
                                                                     final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                     final HateosResourceHandlerContext hateosResourceHandlerContext) {
        Objects.requireNonNull(serverUrl, "serverUrl");
        Objects.requireNonNull(indentation, "indentation");
        Objects.requireNonNull(lineEnding, "lineEnding");
        Objects.requireNonNull(localeContext, "localeContext");
        Objects.requireNonNull(systemSpreadsheetProvider, "systemSpreadsheetProvider");
        Objects.requireNonNull(providerContext, "providerContext");
        Objects.requireNonNull(metadataStore, "metadataStore");
        Objects.requireNonNull(spreadsheetIdToSpreadsheetProvider, "spreadsheetIdToSpreadsheetProvider");
        Objects.requireNonNull(spreadsheetIdToRepository, "spreadsheetIdToRepository");
        Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext");

        return new BasicSpreadsheetMetadataHateosResourceHandlerContext(
            serverUrl,
            indentation,
            lineEnding,
            localeContext,
            systemSpreadsheetProvider,
            providerContext,
            metadataStore,
            spreadsheetIdToSpreadsheetProvider,
            spreadsheetIdToRepository,
            hateosResourceHandlerContext
        );
    }

    private BasicSpreadsheetMetadataHateosResourceHandlerContext(final AbsoluteUrl serverUrl,
                                                                 final Indentation indentation,
                                                                 final LineEnding lineEnding,
                                                                 final LocaleContext localeContext,
                                                                 final SpreadsheetProvider systemSpreadsheetProvider,
                                                                 final ProviderContext providerContext,
                                                                 final SpreadsheetMetadataStore metadataStore,
                                                                 final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                 final HateosResourceHandlerContext hateosResourceHandlerContext) {
        super();

        this.serverUrl = serverUrl;

        this.indentation = indentation;
        this.lineEnding = lineEnding;

        this.localeContext = localeContext;
        this.systemSpreadsheetProvider = systemSpreadsheetProvider;
        this.providerContext = providerContext;

        this.metadataStore = metadataStore;

        this.spreadsheetIdToSpreadsheetProvider = spreadsheetIdToSpreadsheetProvider;
        this.spreadsheetIdToRepository = spreadsheetIdToRepository;

        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
    }

    // SpreadsheetMetadataHateosResourceHandlerContext..................................................................

    private SpreadsheetMetadata load(final SpreadsheetId id) {
        return this.spreadsheetIdToRepository.apply(id)
            .metadatas()
            .loadOrFail(id);
    }

    @Override
    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");

        final SpreadsheetStoreRepository repo = this.storeRepository(
            metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)
        );

        SpreadsheetMetadata saved = metadata;
        {
            final SpreadsheetMetadataPropertyName<SpreadsheetViewport> propertyName = SpreadsheetMetadataPropertyName.VIEWPORT;

            final Optional<SpreadsheetViewport> maybeViewport = metadata.get(propertyName);
            if (maybeViewport.isPresent()) {
                // if a selection is present and is a label that does not exist clear it.

                final SpreadsheetViewport viewport = maybeViewport.get();
                final Optional<AnchoredSpreadsheetSelection> maybeAnchored = viewport.anchoredSelection();
                if (maybeAnchored.isPresent()) {
                    final AnchoredSpreadsheetSelection anchored = maybeAnchored.get();
                    final SpreadsheetSelection selection = anchored.selection();
                    if (selection.isLabelName()) {

                        final SpreadsheetLabelStore labelStore = repo.labels();
                        if (false == labelStore.load(selection.toLabelName()).isPresent()) {
                            saved = saved.set(
                                propertyName,
                                viewport.setAnchoredSelection(SpreadsheetViewport.NO_ANCHORED_SELECTION)
                            );
                        }
                    }
                }
            }
        }

        // metadata must have id
        return repo.metadatas()
            .save(saved);
    }

    @Override
    public SpreadsheetMetadataStore metadataStore() {
        return this.metadataStore;
    }

    private final SpreadsheetMetadataStore metadataStore;

    // ConverterProvider................................................................................................

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

        final SpreadsheetMetadata metadata = this.load(id);

        final ProviderContext providerContext = this.providerContext;

        final SpreadsheetEngineContext context = SpreadsheetEngineContexts.basic(
            this.serverUrl,
            metadata,
            repository,
            SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS,
            this.localeContext,
            metadata.spreadsheetProvider(spreadsheetProvider),
            ProviderContexts.basic(
                providerContext,
                metadata.environmentContext(
                    providerContext
                ),
                providerContext.pluginStore()
            )
        );

        return this.cellColumnProvidersRowViewportRouter(
            id,
            100, // defaultMax
            engine,
            context,
            this.systemSpreadsheetProvider
        );
    }

    private final LocaleContext localeContext;

    private SpreadsheetMetadata stamp(final SpreadsheetMetadata metadata) {
        return metadata.set(
            SpreadsheetMetadataPropertyName.AUDIT_INFO,
            this.providerContext.refreshModifiedAuditInfo(
                metadata.getOrFail(SpreadsheetMetadataPropertyName.AUDIT_INFO)
            )
        );
    }

    private final ProviderContext providerContext;

    private Router<HttpRequestAttribute<?>, HttpHandler> cellColumnProvidersRowViewportRouter(final SpreadsheetId id,
                                                                                              final int defaultMax,
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
        final SpreadsheetMetadata metadata = context.spreadsheetMetadata();

        final SpreadsheetEngineHateosResourceHandlerContext handlerContext = SpreadsheetEngineHateosResourceHandlerContexts.basic(
            HateosResourceHandlerContexts.basic(
                JsonNodeMarshallUnmarshallContexts.basic(
                    metadata.jsonNodeMarshallContext(),
                    metadata.jsonNodeUnmarshallContext()
                )
            ),
            context,
            metadata.spreadsheetFormatterContext(
                SpreadsheetMetadata.NO_CELL,
                (final Optional<Object> v) -> context.spreadsheetExpressionEvaluationContext(
                    SpreadsheetMetadata.NO_CELL,
                    SpreadsheetExpressionReferenceLoaders.fake()
                ).addLocalVariable(
                    SpreadsheetExpressionEvaluationContext.FORMAT_VALUE,
                    v
                ),
                context, // SpreadsheetLabelNameResolver
                context, // ConverterProvider
                context, // SpreadsheetFormatterProvider
                context, // LocaleContext
                context // ProviderContext
            ),
            systemSpreadsheetProvider
        ).setPreProcessor(
            SpreadsheetMetadataHateosResourceHandlerContexts.spreadsheetDeltaJsonCellLabelResolver(
                context.storeRepository()
                    .labels()
            )
        );

        final HateosResourceMappings<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetCell, SpreadsheetEngineHateosResourceHandlerContext> cell = SpreadsheetDeltaHttpMappings.cell(
            engine,
            defaultMax,
            this.indentation,
            this.lineEnding,
            handlerContext
        );

        final HateosResourceMappings<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetEngineHateosResourceHandlerContext> cellReference = SpreadsheetDeltaHttpMappings.cellReference(context);

        final HateosResourceMappings<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn, SpreadsheetEngineHateosResourceHandlerContext> column = SpreadsheetDeltaHttpMappings.column(
            engine
        );

        final HateosResourceMappings<FormName, SpreadsheetDelta, SpreadsheetDelta, Form<SpreadsheetExpressionReference>, SpreadsheetEngineHateosResourceHandlerContext> form = SpreadsheetDeltaHttpMappings.form(
            defaultMax,
            engine
        );

        final HateosResourceMappings<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet, SpreadsheetFormatterInfo, SpreadsheetEngineHateosResourceHandlerContext> formatter = SpreadsheetFormatterHateosResourceMappings.formatter(context);

        final HateosResourceMappings<ExpressionFunctionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet, ExpressionFunctionInfo, SpreadsheetEngineHateosResourceHandlerContext> expressionFunction = ExpressionFunctionHateosResourceMappings.function();

        final HateosResourceMappings<SpreadsheetImporterName, SpreadsheetImporterInfo, SpreadsheetImporterInfoSet, SpreadsheetImporterInfo, SpreadsheetEngineHateosResourceHandlerContext> importer = SpreadsheetImporterHateosResourceMappings.importer();

        final HateosResourceMappings<SpreadsheetLabelName, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetLabelMapping, SpreadsheetEngineHateosResourceHandlerContext> label = SpreadsheetDeltaHttpMappings.label(
            defaultMax,
            engine
        );

        final HateosResourceMappings<SpreadsheetId,
            SpreadsheetMetadata,
            SpreadsheetMetadata,
            SpreadsheetMetadata,
            SpreadsheetEngineHateosResourceHandlerContext> metadataMappings = MetadataHateosResourceMappings.metadata();

        final HateosResourceMappings<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet, SpreadsheetParserInfo, SpreadsheetEngineHateosResourceHandlerContext> parser = SpreadsheetParserHateosResourceMappings.parser();

        final HateosResourceMappings<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow, SpreadsheetEngineHateosResourceHandlerContext> row = SpreadsheetDeltaHttpMappings.row(
            engine
        );

        final HateosResourceMappings<ValidatorName, ValidatorInfo, ValidatorInfoSet, ValidatorInfo, SpreadsheetEngineHateosResourceHandlerContext> validator = ValidationHateosResourceMappings.validator();

        return HateosResourceMappings.router(
            deltaUrlPath,
            Sets.of(
                cell,
                cellReference,
                column,
                form,
                formatter, // formatter
                expressionFunction, // function
                importer,
                label,
                metadataMappings,
                parser, // /parser
                row,
                validator
            ),
            this.indentation,
            this.lineEnding,
            handlerContext
        );
    }

    private final AbsoluteUrl serverUrl;
    private final Indentation indentation;
    private final LineEnding lineEnding;

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
    public SpreadsheetMetadataHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        final HateosResourceHandlerContext before = this.hateosResourceHandlerContext;
        final HateosResourceHandlerContext after = before.setPreProcessor(processor);

        return before.equals(after) ?
            this :
            new BasicSpreadsheetMetadataHateosResourceHandlerContext(
                this.serverUrl,
                this.indentation,
                this.lineEnding,
                this.localeContext,
                this.systemSpreadsheetProvider,
                this.providerContext,
                this.metadataStore,
                this.spreadsheetIdToSpreadsheetProvider,
                this.spreadsheetIdToRepository,
                after
            );
    }

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    // ProviderContextDelegator.........................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.serverUrl.toString();
    }
}
