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

package walkingkooka.spreadsheet.server;

import walkingkooka.collect.map.Maps;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.spreadsheet.server.meta.SpreadsheetIdRouter;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.terminal.server.TerminalServerContext;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A basic fully functional {@link SpreadsheetServerContext}
 */
final class BasicSpreadsheetServerContext implements SpreadsheetServerContext,
    SpreadsheetEnvironmentContextDelegator,
    LocaleContextDelegator,
    HateosResourceHandlerContextDelegator,
    SpreadsheetProviderDelegator {

    static BasicSpreadsheetServerContext with(final SpreadsheetEngine spreadsheetEngine,
                                              final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToSpreadsheetStoreRepository,
                                              final SpreadsheetProvider spreadsheetProvider,
                                              final Function<SpreadsheetContext, SpreadsheetEngineContext> spreadsheetEngineContextFactory,
                                              final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext,
                                              final LocaleContext localeContext,
                                              final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                              final HateosResourceHandlerContext hateosResourceHandlerContext,
                                              final ProviderContext providerContext,
                                              final TerminalServerContext terminalServerContext) {
        return new BasicSpreadsheetServerContext(
            Objects.requireNonNull(spreadsheetEngine, "spreadsheetEngine"),
            Objects.requireNonNull(spreadsheetIdToSpreadsheetStoreRepository, "spreadsheetIdToSpreadsheetStoreRepository"),
            Objects.requireNonNull(spreadsheetProvider, "spreadsheetProvider"),
            Objects.requireNonNull(spreadsheetEngineContextFactory, "spreadsheetEngineContextFactory"),
            Objects.requireNonNull(spreadsheetEnvironmentContext, "spreadsheetEnvironmentContext"),
            Objects.requireNonNull(localeContext, "localeContext"),
            Objects.requireNonNull(spreadsheetMetadataContext, "spreadsheetMetadataContext"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
            Objects.requireNonNull(providerContext, "providerContext"),
            Objects.requireNonNull(terminalServerContext, "terminalServerContext")
        );
    }
    
    private BasicSpreadsheetServerContext(final SpreadsheetEngine spreadsheetEngine,
                                          final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToSpreadsheetStoreRepository,
                                          final SpreadsheetProvider spreadsheetProvider,
                                          final Function<SpreadsheetContext, SpreadsheetEngineContext> spreadsheetEngineContextFactory,
                                          final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext,
                                          final LocaleContext localeContext,
                                          final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                          final HateosResourceHandlerContext hateosResourceHandlerContext,
                                          final ProviderContext providerContext,
                                          final TerminalServerContext terminalServerContext) {
        this.spreadsheetEngine = spreadsheetEngine;
        this.spreadsheetIdToSpreadsheetStoreRepository = spreadsheetIdToSpreadsheetStoreRepository;
        this.spreadsheetProvider = spreadsheetProvider;

        this.spreadsheetEngineContextFactory = spreadsheetEngineContextFactory;

        this.spreadsheetEnvironmentContext = spreadsheetEnvironmentContext;
        this.localeContext = LocaleContexts.readOnly(localeContext);
        this.spreadsheetMetadataContext = spreadsheetMetadataContext;
        this.hateosResourceHandlerContext = hateosResourceHandlerContext;
        this.providerContext = providerContext;
        this.terminalServerContext = terminalServerContext;
    }

    // SpreadsheetServerContext.........................................................................................

    @Override
    public SpreadsheetContext createEmptySpreadsheet(final Optional<Locale> locale) {
        final EmailAddress user = this.userOrFail();

        final SpreadsheetMetadata metadata = this.spreadsheetMetadataContext.createMetadata(
            user,
            locale
        );

        final SpreadsheetId spreadsheetId = metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID);

        final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext = this.spreadsheetEnvironmentContext.cloneEnvironment();
        spreadsheetEnvironmentContext.setSpreadsheetId(spreadsheetId);

        final ProviderContext providerContext = this.providerContext.cloneEnvironment();
        providerContext.setUser(
            Optional.of(user)
        );

        final SpreadsheetContext context = SpreadsheetContexts.fixedSpreadsheetId(
            this.spreadsheetEngine,
            this.spreadsheetIdToSpreadsheetStoreRepository.apply(spreadsheetId),
            this.spreadsheetEngineContextFactory,
            (SpreadsheetEngineContext c) -> SpreadsheetIdRouter.create(
                c,
                this.hateosResourceHandlerContext
            ),
            metadata.spreadsheetEnvironmentContext(spreadsheetEnvironmentContext),
            this.localeContext,
            this.spreadsheetProvider,
            ProviderContexts.readOnly(providerContext)
        );

        this.spreadsheetIdToSpreadsheetContext.put(
            spreadsheetId,
            context
        );

        return context;
    }

    @Override
    public SpreadsheetContext createSpreadsheetContext() {
        return SpreadsheetContexts.mutableSpreadsheetId(
            this.spreadsheetEngine,
            this.spreadsheetIdToSpreadsheetStoreRepository::apply,
            this.spreadsheetMetadataContext,
            this.spreadsheetEngineContextFactory,
            this.spreadsheetEnvironmentContext.cloneEnvironment(),
            this.localeContext,
            this.spreadsheetProvider,
            ProviderContexts.readOnly(this.providerContext)
        );
    }

    private final SpreadsheetEngine spreadsheetEngine;

    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToSpreadsheetStoreRepository;

    private final Function<SpreadsheetContext, SpreadsheetEngineContext> spreadsheetEngineContextFactory;

    /**
     * The default or starting {@link SpreadsheetEnvironmentContext} for each new spreadsheet.
     */
    private final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext;

    private final TerminalServerContext terminalServerContext;

    @Override
    public Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(
            this.spreadsheetIdToSpreadsheetContext.get(id)
        );
    }

    private final Map<SpreadsheetId, SpreadsheetContext> spreadsheetIdToSpreadsheetContext = Maps.concurrent();
    
    // EnvironmentContextDelegator......................................................................................

    @Override
    public SpreadsheetEnvironmentContext cloneEnvironment() {
        return this.spreadsheetEnvironmentContext()
            .cloneEnvironment();
    }

    @Override
    public SpreadsheetServerContext setEnvironmentContext(final EnvironmentContext environmentContext) {
        final ProviderContext before = this.providerContext;
        final ProviderContext after = before.setEnvironmentContext(environmentContext);

        return before == after ?
            this :
            new BasicSpreadsheetServerContext(
                this.spreadsheetEngine,
                this.spreadsheetIdToSpreadsheetStoreRepository,
                this.spreadsheetProvider,
                this.spreadsheetEngineContextFactory,
                this.spreadsheetEnvironmentContext,
                this.localeContext,
                this.spreadsheetMetadataContext,
                this.hateosResourceHandlerContext,
                after,
                this.terminalServerContext
            );
    }

    @Override
    public Indentation indentation() {
        return this.spreadsheetEnvironmentContext()
            .indentation();
    }

    @Override
    public LineEnding lineEnding() {
        return this.spreadsheetEnvironmentContext()
            .lineEnding();
    }

    @Override
    public Locale locale() {
        return this.spreadsheetEnvironmentContext()
            .locale();
    }

    @Override
    public void setLocale(final Locale locale) {
        this.spreadsheetEnvironmentContext()
            .setLocale(locale);
    }

    @Override
    public SpreadsheetEnvironmentContext spreadsheetEnvironmentContext() {
        return this.spreadsheetEnvironmentContext;
    }

    // LocaleContextDelegator...........................................................................................

    @Override
    public LocaleContext localeContext() {
        return this.localeContext;
    }

    private final LocaleContext localeContext;

    // SpreadsheetMetadataContext.......................................................................................

    @Override
    public SpreadsheetMetadata createMetadata(final EmailAddress user,
                                              final Optional<Locale> locale) {
        return this.spreadsheetMetadataContext.createMetadata(
            user,
            locale
        );
    }

    @Override
    public Optional<SpreadsheetMetadata> loadMetadata(final SpreadsheetId spreadsheetId) {
        return this.spreadsheetMetadataContext.loadMetadata(spreadsheetId);
    }

    @Override
    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
        return this.spreadsheetMetadataContext.saveMetadata(metadata);
    }

    @Override
    public void deleteMetadata(final SpreadsheetId spreadsheetId) {
        this.spreadsheetMetadataContext.deleteMetadata(spreadsheetId);

        this.spreadsheetIdToSpreadsheetContext.remove(spreadsheetId);
    }

    @Override
    public List<SpreadsheetMetadata> findMetadataBySpreadsheetName(final String name,
                                                                   final int offset,
                                                                   final int count) {
        return this.spreadsheetMetadataContext.findMetadataBySpreadsheetName(
            name,
            offset,
            count
        );
    }

    private final SpreadsheetMetadataContext spreadsheetMetadataContext;

    // HateosResourceHandlerContextDelegator............................................................................

    @Override
    public HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.hateosResourceHandlerContext;
    }

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    @Override
    public SpreadsheetServerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public SpreadsheetServerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosResourceHandlerContext(
            this.hateosResourceHandlerContext.setPreProcessor(processor)
        );
    }

    private SpreadsheetServerContext setHateosResourceHandlerContext(final HateosResourceHandlerContext context) {
        return this.hateosResourceHandlerContext.equals(context) ?
            this :
            new BasicSpreadsheetServerContext(
                this.spreadsheetEngine,
                this.spreadsheetIdToSpreadsheetStoreRepository,
                this.spreadsheetProvider,
                this.spreadsheetEngineContextFactory,
                this.spreadsheetEnvironmentContext,
                this.localeContext,
                this.spreadsheetMetadataContext,
                Objects.requireNonNull(context, "hateosResourceHandlerContext"),
                this.providerContext,
                this.terminalServerContext
            );
    }

    // SpreadsheetProviderDelegator.....................................................................................

    @Override
    public SpreadsheetProvider spreadsheetProvider() {
        return this.spreadsheetProvider;
    }

    private final SpreadsheetProvider spreadsheetProvider;

    // ProviderContext..................................................................................................

    @Override
    public ProviderContext providerContext() {
        return this.providerContext;
    }

    private final ProviderContext providerContext;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.spreadsheetEnvironmentContext + " " + this.localeContext + " " + this.spreadsheetProvider;
    }
}
