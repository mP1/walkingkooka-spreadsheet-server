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

import walkingkooka.Cast;
import walkingkooka.ToStringBuilder;
import walkingkooka.collect.map.Maps;
import walkingkooka.convert.BinaryNumberConverterFunction;
import walkingkooka.currency.CurrencyCode;
import walkingkooka.currency.CurrencyLocaleContext;
import walkingkooka.currency.CurrencyLocaleContextDelegator;
import walkingkooka.currency.CurrencyLocaleContexts;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.locale.LocaleLanguageTag;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.MediaTypeDetector;
import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.net.http.server.hateos.HateosHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContexts;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContext;
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
import walkingkooka.store.StoreWatcher;
import walkingkooka.terminal.server.TerminalServerContext;
import walkingkooka.text.CaseKind;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Currency;
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
    CurrencyLocaleContextDelegator,
    SpreadsheetEnvironmentContextDelegator,
    HateosHandlerContextDelegator,
    SpreadsheetProviderDelegator,
    TreePrintable {

    static BasicSpreadsheetServerContext with(final MediaTypeDetector mediaTypeDetector,
                                              final BinaryNumberConverterFunction<SpreadsheetConverterContext> multiplier,
                                              final SpreadsheetEngine spreadsheetEngine,
                                              final Function<SpreadsheetId, Optional<SpreadsheetStoreRepository>> spreadsheetIdToSpreadsheetStoreRepository,
                                              final SpreadsheetProvider spreadsheetProvider,
                                              final CurrencyLocaleContext currencyLocaleContext,
                                              final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext,
                                              final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                              final HateosHandlerContext hateosHandlerContext,
                                              final ProviderContext providerContext,
                                              final TerminalServerContext terminalServerContext) {
        return new BasicSpreadsheetServerContext(
            Objects.requireNonNull(mediaTypeDetector, "mediaTypeDetector"),
            Objects.requireNonNull(multiplier, "multiplier"),
            Objects.requireNonNull(spreadsheetEngine, "spreadsheetEngine"),
            Objects.requireNonNull(spreadsheetIdToSpreadsheetStoreRepository, "spreadsheetIdToSpreadsheetStoreRepository"),
            Objects.requireNonNull(spreadsheetProvider, "spreadsheetProvider"),
            Objects.requireNonNull(currencyLocaleContext, "currencyLocaleContext"),
            Objects.requireNonNull(spreadsheetEnvironmentContext, "spreadsheetEnvironmentContext"),
            Objects.requireNonNull(spreadsheetMetadataContext, "spreadsheetMetadataContext"),
            Objects.requireNonNull(hateosHandlerContext, "hateosHandlerContext"),
            Objects.requireNonNull(providerContext, "providerContext"),
            Objects.requireNonNull(terminalServerContext, "terminalServerContext")
        );
    }

    private BasicSpreadsheetServerContext(final MediaTypeDetector mediaTypeDetector,
                                          final BinaryNumberConverterFunction<SpreadsheetConverterContext> multiplier,
                                          final SpreadsheetEngine spreadsheetEngine,
                                          final Function<SpreadsheetId, Optional<SpreadsheetStoreRepository>> spreadsheetIdToSpreadsheetStoreRepository,
                                          final SpreadsheetProvider spreadsheetProvider,
                                          final CurrencyLocaleContext currencyLocaleContext,
                                          final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext,
                                          final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                          final HateosHandlerContext hateosHandlerContext,
                                          final ProviderContext providerContext,
                                          final TerminalServerContext terminalServerContext) {
        super();

        this.mediaTypeDetector = mediaTypeDetector;

        this.multiplier = multiplier;

        this.spreadsheetEngine = spreadsheetEngine;
        this.spreadsheetIdToSpreadsheetStoreRepository = spreadsheetIdToSpreadsheetStoreRepository;
        this.spreadsheetProvider = spreadsheetProvider;

        this.currencyLocaleContext = CurrencyLocaleContexts.readOnly(currencyLocaleContext);
        this.spreadsheetEnvironmentContext = spreadsheetEnvironmentContext;
        this.spreadsheetMetadataContext = spreadsheetMetadataContext;
        this.hateosHandlerContext = hateosHandlerContext;
        this.providerContext = providerContext;
        this.terminalServerContext = terminalServerContext;
    }

    // SpreadsheetServerContext.........................................................................................

    @Override
    public SpreadsheetContext createEmptySpreadsheet(final Optional<Locale> locale) {
        Objects.requireNonNull(locale, "locale");

        return this.fixedSpreadsheetContext(
            this.spreadsheetMetadataContext.createMetadata(
                this.userOrFail(),
                locale
            )
        );
    }

    @Override
    public SpreadsheetContext createSpreadsheetContext() {
        return SpreadsheetContexts.mutableSpreadsheetId(
            this.mediaTypeDetector,
            this.multiplier,
            this.spreadsheetEngine,
            this, // SpreadsheetContextSupplier
            this.spreadsheetMetadataContext,
            this.currencyLocaleContext,
            this.spreadsheetEnvironmentContext.cloneEnvironment(),
            this.spreadsheetProvider,
            ProviderContexts.readOnly(this.providerContext)
        );
    }

    private final MediaTypeDetector mediaTypeDetector;

    private final BinaryNumberConverterFunction<SpreadsheetConverterContext> multiplier;

    private final SpreadsheetEngine spreadsheetEngine;

    private final Function<SpreadsheetId, Optional<SpreadsheetStoreRepository>> spreadsheetIdToSpreadsheetStoreRepository;

    /**
     * The default or starting {@link SpreadsheetEnvironmentContext} for each new spreadsheet.
     */
    private final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext;

    private final TerminalServerContext terminalServerContext;

    @Override
    public synchronized Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
        Objects.requireNonNull(id, "id");

        SpreadsheetContext context = this.spreadsheetIdToSpreadsheetContext.get(id);

        if (null == context) {
            final SpreadsheetMetadata metadata = this.loadMetadata(id)
                .orElse(null);
            if (null != metadata) {
                context = this.fixedSpreadsheetContext(metadata);
            }
        }

        return Optional.ofNullable(context);
    }

    /**
     * Uses the provided {@link SpreadsheetId} to get or lazily create a {@link SpreadsheetContext}.
     */
    private SpreadsheetContext fixedSpreadsheetContext(final SpreadsheetMetadata metadata) {
        final SpreadsheetId spreadsheetId = metadata.getOrFail(SpreadsheetMetadataPropertyName.SPREADSHEET_ID);

        final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext = this.spreadsheetEnvironmentContext.cloneEnvironment();
        spreadsheetEnvironmentContext.setSpreadsheetId(
            Optional.of(spreadsheetId)
        );

        final ProviderContext providerContext = this.providerContext.cloneEnvironment();
        providerContext.setUser(
            Optional.of(
                this.userOrFail()
            )
        );

        final SpreadsheetContext context = SpreadsheetContexts.fixedSpreadsheetId(
            this.mediaTypeDetector,
            this.spreadsheetMetadataContext, // SpreadsheetMetadataCreator
            this.multiplier,
            this.spreadsheetEngine,
            this.spreadsheetIdToSpreadsheetStoreRepository.apply(spreadsheetId)
                .orElseThrow(spreadsheetId::missingSpreadsheetException),
            (SpreadsheetEngineContext c) -> Cast.to(
                SpreadsheetIdRouter.create(
                    c,
                    this.hateosHandlerContext
                )
            ),
            this.currencyLocaleContext,
            metadata.spreadsheetEnvironmentContext(spreadsheetEnvironmentContext),
            this.spreadsheetProvider,
            ProviderContexts.readOnly(providerContext)
        );

        this.spreadsheetIdToSpreadsheetContext.put(
            spreadsheetId,
            context
        );

        return context;
    }

    private final Map<SpreadsheetId, SpreadsheetContext> spreadsheetIdToSpreadsheetContext = Maps.concurrent();

    // CurrencyLocaleContextDelegator...................................................................................

    @Override
    public Optional<Currency> currencyForCurrencyCode(final CurrencyCode currencyCode) {
        return this.currencyLocaleContext.currencyForCurrencyCode(currencyCode);
    }

    @Override
    public Optional<Locale> localeForLanguageTag(final LocaleLanguageTag languageTag) {
        return this.currencyLocaleContext.localeForLanguageTag(languageTag);
    }

    @Override
    public CurrencyLocaleContext currencyLocaleContext() {
        return this.currencyLocaleContext;
    }

    private final CurrencyLocaleContext currencyLocaleContext;

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
                this.mediaTypeDetector,
                this.multiplier,
                this.spreadsheetEngine,
                this.spreadsheetIdToSpreadsheetStoreRepository,
                this.spreadsheetProvider,
                this.currencyLocaleContext,
                this.spreadsheetEnvironmentContext,
                this.spreadsheetMetadataContext,
                this.hateosHandlerContext,
                after,
                this.terminalServerContext
            );
    }

    @Override
    public Currency currency() {
        return this.spreadsheetEnvironmentContext()
            .currency();
    }

    @Override
    public void setCurrency(final Currency currency) {
        this.spreadsheetEnvironmentContext()
            .setCurrency(currency);
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

    @Override
    public Runnable addMetadataWatcher(final StoreWatcher<SpreadsheetMetadata> watcher) {
        return this.spreadsheetMetadataContext.addMetadataWatcher(watcher);
    }

    @Override
    public Runnable addMetadataWatcherOnce(final StoreWatcher<SpreadsheetMetadata> watcher) {
        return this.spreadsheetMetadataContext.addMetadataWatcherOnce(watcher);
    }

    private final SpreadsheetMetadataContext spreadsheetMetadataContext;

    // HateosHandlerContextDelegator............................................................................

    @Override
    public HateosHandlerContext hateosHandlerContext() {
        return this.hateosHandlerContext;
    }

    private final HateosHandlerContext hateosHandlerContext;

    @Override
    public SpreadsheetServerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setObjectPostProcessor(processor)
        );
    }

    @Override
    public SpreadsheetServerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.setHateosHandlerContext(
            this.hateosHandlerContext.setPreProcessor(processor)
        );
    }

    private SpreadsheetServerContext setHateosHandlerContext(final HateosHandlerContext context) {
        return this.hateosHandlerContext.equals(context) ?
            this :
            new BasicSpreadsheetServerContext(
                this.mediaTypeDetector,
                this.multiplier,
                this.spreadsheetEngine,
                this.spreadsheetIdToSpreadsheetStoreRepository,
                this.spreadsheetProvider,
                this.currencyLocaleContext,
                this.spreadsheetEnvironmentContext,
                this.spreadsheetMetadataContext,
                Objects.requireNonNull(context, "hateosHandlerContext"),
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
        return ToStringBuilder.empty()
            .label("mediaTypeDetector")
            .value(this.mediaTypeDetector)
            .label("multiplier")
            .value(this.multiplier)
            .label("spreadsheetEngine")
            .value(this.spreadsheetEngine)
            .label("spreadsheetIdToSpreadsheetContext")
            .value(this.spreadsheetIdToSpreadsheetContext)
            .label("currencyLocaleContext")
            .value(this.currencyLocaleContext)
            .label("spreadsheetEnvironmentContext")
            .value(this.spreadsheetEnvironmentContext)
            .label("spreadsheetMetadataContext")
            .value(this.spreadsheetMetadataContext)
            .label("hateosHandlerContext")
            .value(this.hateosHandlerContext)
            .label("spreadsheetProvider")
            .value(this.spreadsheetProvider)
            .label("providerContext")
            .value(this.providerContext)
            .label("terminalServerContext")
            .value(this.terminalServerContext)
            .build();
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.getClass().getSimpleName());
        printer.indent();
        {
            this.printTreeWithLabel(
                printer,
                MediaTypeDetector.class.getSimpleName(),
                this.mediaTypeDetector
            );

            this.printTreeWithLabel(
                printer,
                CurrencyLocaleContext.class.getSimpleName(),
                this.currencyLocaleContext
            );

            this.printTreeWithLabel(
                printer,
                SpreadsheetEnvironmentContext.class.getSimpleName(),
                this.spreadsheetEnvironmentContext
            );

            this.printTreeWithLabel(
                printer,
                SpreadsheetMetadataContext.class.getSimpleName(),
                this.spreadsheetMetadataContext
            );

            this.printTreeWithLabel(
                printer,
                HateosHandlerContext.class.getSimpleName(),
                this.hateosHandlerContext
            );

            this.printTreeWithLabel(
                printer,
                SpreadsheetProvider.class.getSimpleName(),
                this.spreadsheetProvider
            );

            this.printTreeWithLabel(
                printer,
                ProviderContext.class.getSimpleName(),
                this.providerContext
            );

            this.printTreeWithLabel(
                printer,
                TerminalServerContext.class.getSimpleName(),
                this.terminalServerContext
            );
        }
        printer.outdent();
    }

    private void printTreeWithLabel(final IndentingPrinter printer,
                                    final String label,
                                    final Object print) {
        printer.println(
            CaseKind.PASCAL.change(
                label,
                CaseKind.CAMEL
            )
        );
        printer.indent();
        {
            TreePrintable.printTreeOrToString(
                print,
                printer
            );
        }
        printer.outdent();
    }
}
