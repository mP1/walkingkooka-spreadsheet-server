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

import walkingkooka.ToStringBuilder;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.format.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetParserName;
import walkingkooka.spreadsheet.format.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.reference.AnchoredSpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.server.comparator.SpreadsheetComparatorsHateosResourceMappings;
import walkingkooka.spreadsheet.server.delta.SpreadsheetDeltaHateosResourceMappings;
import walkingkooka.spreadsheet.server.delta.SpreadsheetExpressionReferenceSimilarities;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterHateosResourceMappings;
import walkingkooka.spreadsheet.server.function.ExpressionFunctionHateosResourceMappings;
import walkingkooka.spreadsheet.server.label.SpreadsheetLabelHateosResourceMappings;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserHateosResourceMappings;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonString;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link SpreadsheetMetadataHateosResourceHandlerContext} that creates a new {@link SpreadsheetStoreRepository} for unknown {@link SpreadsheetId}.
 * There is no way to delete existing spreadsheets.
 */
final class BasicSpreadsheetMetadataHateosResourceHandlerContext implements SpreadsheetMetadataHateosResourceHandlerContext {

    /**
     * Creates a new empty {@link BasicSpreadsheetMetadataHateosResourceHandlerContext}
     */
    static BasicSpreadsheetMetadataHateosResourceHandlerContext with(final AbsoluteUrl base,
                                                                     final Indentation indentation,
                                                                     final LineEnding lineEnding,
                                                                     final Function<BigDecimal, Fraction> fractioner,
                                                                     final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                                     final SpreadsheetMetadataStore metadataStore,
                                                                     final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdToComparatorProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdToFormatterProvider,
                                                                     final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdToParserProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                     final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                                     final JsonNodeMarshallContext marshallContext,
                                                                     final JsonNodeUnmarshallContext unmarshallContext,
                                                                     final Supplier<LocalDateTime> now) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(indentation, "indentation");
        Objects.requireNonNull(lineEnding, "lineEnding");
        Objects.requireNonNull(fractioner, "fractioner");
        Objects.requireNonNull(createMetadata, "createMetadata");
        Objects.requireNonNull(metadataStore, "metadataStore");
        Objects.requireNonNull(spreadsheetIdToComparatorProvider, "spreadsheetIdToComparatorProvider");
        Objects.requireNonNull(spreadsheetIdToFormatterProvider, "spreadsheetIdToFormatterProvider");
        Objects.requireNonNull(spreadsheetIdToExpressionFunctionProvider, "spreadsheetIdToExpressionFunctionProvider");
        Objects.requireNonNull(spreadsheetIdToParserProvider, "spreadsheetIdToParserProvider");
        Objects.requireNonNull(spreadsheetIdToRepository, "spreadsheetIdToRepository");
        Objects.requireNonNull(spreadsheetMetadataStamper, "spreadsheetMetadataStamper");
        Objects.requireNonNull(marshallContext, "marshallContext");
        Objects.requireNonNull(unmarshallContext, "unmarshallContext");
        Objects.requireNonNull(now, "now");

        return new BasicSpreadsheetMetadataHateosResourceHandlerContext(
                base,
                indentation,
                lineEnding,
                fractioner,
                createMetadata,
                metadataStore,
                spreadsheetIdToComparatorProvider,
                spreadsheetIdToFormatterProvider,
                spreadsheetIdToExpressionFunctionProvider,
                spreadsheetIdToParserProvider,
                spreadsheetIdToRepository,
                spreadsheetMetadataStamper,
                marshallContext,
                unmarshallContext,
                now
        );
    }

    private BasicSpreadsheetMetadataHateosResourceHandlerContext(final AbsoluteUrl base,
                                                                 final Indentation indentation,
                                                                 final LineEnding lineEnding,
                                                                 final Function<BigDecimal, Fraction> fractioner,
                                                                 final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                                 final SpreadsheetMetadataStore metadataStore,
                                                                 final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdToComparatorProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdToFormatterProvider,
                                                                 final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdToParserProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                 final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                                 final JsonNodeMarshallContext marshallContext,
                                                                 final JsonNodeUnmarshallContext unmarshallContext,
                                                                 final Supplier<LocalDateTime> now) {
        super();

        this.base = base;

        this.indentation = indentation;
        this.lineEnding = lineEnding;

        this.fractioner = fractioner;
        this.createMetadata = createMetadata;
        this.metadataStore = metadataStore;

        this.spreadsheetIdToComparatorProvider = spreadsheetIdToComparatorProvider;
        this.spreadsheetIdToFormatterProvider = spreadsheetIdToFormatterProvider;
        this.spreadsheetIdToExpressionFunctionProvider = spreadsheetIdToExpressionFunctionProvider;
        this.spreadsheetIdToParserProvider = spreadsheetIdToParserProvider;
        this.spreadsheetIdToRepository = spreadsheetIdToRepository;
        this.spreadsheetMetadataStamper = spreadsheetMetadataStamper;

        this.marshallContext = marshallContext;
        this.unmarshallContext = unmarshallContext;

        this.now = now;
    }

    // SpreadsheetMetadataHateosResourceHandlerContext..................................................................

    @Override
    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
        return this.createMetadata.apply(locale);
    }

    private final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata;

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

    // SpreadsheetComparatorProvider....................................................................................

    @Override
    public SpreadsheetComparatorProvider comparatorProvider(final SpreadsheetId id) {
        return this.spreadsheetIdToComparatorProvider.apply(id);
    }

    private final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdToComparatorProvider;

    // SpreadsheetFormatterProvider.....................................................................................

    @Override
    public SpreadsheetFormatterProvider formatterProvider(final SpreadsheetId id) {
        return this.spreadsheetIdToFormatterProvider.apply(id);
    }

    final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdToFormatterProvider;

    // ExpressionFunctionProvider.......................................................................................

    @Override
    public ExpressionFunctionProvider expressionFunctionProvider(final SpreadsheetId id) {
        return this.spreadsheetIdToExpressionFunctionProvider.apply(id);
    }

    private final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider;

    // SpreadsheetParserProvider........................................................................................

    @Override
    public SpreadsheetParserProvider parserProvider(final SpreadsheetId id) {
        return this.spreadsheetIdToParserProvider.apply(id);
    }

    final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdToParserProvider;
    
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
                this.spreadsheetMetadataStamper
        );

        final SpreadsheetComparatorProvider spreadsheetComparatorProvider = this.spreadsheetIdToComparatorProvider.apply(id);
        final SpreadsheetFormatterProvider spreadsheetFormatterProvider = this.spreadsheetIdToFormatterProvider.apply(id);
        final ExpressionFunctionProvider expressionFunctionProvider = this.spreadsheetIdToExpressionFunctionProvider.apply(id);
        final SpreadsheetParserProvider spreadsheetParserProvider = this.spreadsheetIdToParserProvider.apply(id);

        final Function<BigDecimal, Fraction> fractioner = this.fractioner;
        final SpreadsheetMetadata metadata = this.load(id);

        final SpreadsheetEngineContext context = SpreadsheetEngineContexts.basic(
                metadata,
                metadata.spreadsheetComparatorProvider(spreadsheetComparatorProvider),
                metadata.spreadsheetFormatterProvider(spreadsheetFormatterProvider),
                metadata.expressionFunctionProvider(expressionFunctionProvider),
                metadata.spreadsheetParserProvider(spreadsheetParserProvider),
                engine,
                fractioner,
                repository,
                this.base,
                this.now
        );

        return this.cellColumnProvidersRowViewportRouter(
                                id,
                                100, // defaultMax
                                engine,
                                context
        );
    }

    private final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper;
    private final Supplier<LocalDateTime> now;

    private Router<HttpRequestAttribute<?>, HttpHandler> cellColumnProvidersRowViewportRouter(final SpreadsheetId id,
                                                                                              final int defaultMax,
                                                                                              final SpreadsheetEngine engine,
                                                                                              final SpreadsheetEngineContext context) {
        final HateosResourceMapping<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetCell, SpreadsheetEngineHateosResourceHandlerContext> cell = SpreadsheetDeltaHateosResourceMappings.cell(
                engine,
                defaultMax
        );

        final HateosResourceMapping<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetEngineHateosResourceHandlerContext> cellReference = SpreadsheetDeltaHateosResourceMappings.cellReference(context);

        final HateosResourceMapping<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn, SpreadsheetEngineHateosResourceHandlerContext> column = SpreadsheetDeltaHateosResourceMappings.column(
                engine
        );

        final HateosResourceMapping<SpreadsheetComparatorName, SpreadsheetComparatorInfo, SpreadsheetComparatorInfoSet, SpreadsheetComparatorInfo, SpreadsheetEngineHateosResourceHandlerContext> comparator = SpreadsheetComparatorsHateosResourceMappings.comparator(context);

        final HateosResourceMapping<SpreadsheetFormatterName, SpreadsheetFormatterInfo, SpreadsheetFormatterInfoSet, SpreadsheetFormatterInfo, SpreadsheetEngineHateosResourceHandlerContext> formatter = SpreadsheetFormatterHateosResourceMappings.formatter(context);

        final HateosResourceMapping<FunctionExpressionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet, ExpressionFunctionInfo, SpreadsheetEngineHateosResourceHandlerContext> expressionFunction = ExpressionFunctionHateosResourceMappings.function(context);

        final HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetEngineHateosResourceHandlerContext> label = SpreadsheetLabelHateosResourceMappings.INSTANCE;

        final HateosResourceMapping<SpreadsheetParserName, SpreadsheetParserInfo, SpreadsheetParserInfoSet, SpreadsheetParserInfo, SpreadsheetEngineHateosResourceHandlerContext> parser = SpreadsheetParserHateosResourceMappings.parser(context);

        final HateosResourceMapping<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow, SpreadsheetEngineHateosResourceHandlerContext> row = SpreadsheetDeltaHateosResourceMappings.row(
                engine
        );

        final AbsoluteUrl base = this.base;
        final SpreadsheetMetadata metadata = context.spreadsheetMetadata();

        return HateosResourceMapping.router(
                base.setPath(
                        base.path()
                                .append(
                                        UrlPathName.with(
                                                id.toString()
                                        )
                                )
                ),
                Sets.of(
                        cell,
                        cellReference,
                        column,
                        comparator, // /comparator
                        formatter, // formatter
                        expressionFunction, // function
                        label,
                        parser, // /parser
                        row
                ),
                this.indentation,
                this.lineEnding,
                SpreadsheetEngineHateosResourceHandlerContexts.basic(
                        metadata.jsonNodeMarshallContext(),
                        metadata.jsonNodeUnmarshallContext()
                                .setPreProcessor(
                                        SpreadsheetMetadataHateosResourceHandlerContexts.spreadsheetDeltaJsonCellLabelResolver(
                                                context.storeRepository()
                                                        .labels()
                                        )
                                ),
                        context
                )
        );
    }

    private final AbsoluteUrl base;
    private final Indentation indentation;
    private final LineEnding lineEnding;
    private final Function<BigDecimal, Fraction> fractioner;

    @Override
    public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
        return this.spreadsheetIdToRepository.apply(id);
    }

    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository;

    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_JSON;
    }

    // JsonNodeMarshallContext..........................................................................................

    @Override
    public JsonNodeMarshallContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor context) {
        return this.marshallContext.setObjectPostProcessor(context);
    }

    @Override
    public JsonNode marshall(final Object value) {
        return this.marshallContext.marshall(value);
    }

    @Override
    public JsonNode marshallEnumSet(final Set<? extends Enum<?>> set) {
        return this.marshallContext.marshallEnumSet(set);
    }

    @Override
    public JsonNode marshallWithType(final Object value) {
        return this.marshallContext.marshallWithType(value);
    }

    @Override
    public JsonNode marshallCollection(final Collection<?> collection) {
        return this.marshallContext.marshallCollection(collection);
    }

    @Override
    public JsonNode marshallMap(final Map<?, ?> map) {
        return this.marshallContext.marshallMap(map);
    }

    @Override
    public JsonNode marshallWithTypeCollection(final Collection<?> collection) {
        return this.marshallContext.marshallWithTypeCollection(collection);
    }

    @Override
    public JsonNode marshallWithTypeMap(final Map<?, ?> map) {
        return this.marshallContext.marshallWithTypeMap(map);
    }

    private final JsonNodeMarshallContext marshallContext;

    // JsonNodeUnmarshallContext........................................................................................

    @Override
    public ExpressionNumberKind expressionNumberKind() {
        return this.unmarshallContext.expressionNumberKind();
    }

    @Override
    public MathContext mathContext() {
        return this.unmarshallContext.mathContext();
    }

    @Override
    public JsonNodeUnmarshallContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        return this.unmarshallContext.setPreProcessor(processor);
    }

    @Override
    public <T> T unmarshall(final JsonNode json,
                            final Class<T> type) {
        return this.unmarshallContext.unmarshall(
                json,
                type
        );
    }

    @Override
    public <T extends Enum<T>> Set<T> unmarshallEnumSet(final JsonNode json,
                                                        final Class<T> type,
                                                        final Function<String, T> nameToEnum) {
        return this.unmarshallContext.unmarshallEnumSet(
                json,
                type,
                nameToEnum
        );
    }

    @Override
    public <T> List<T> unmarshallList(final JsonNode json,
                                      final Class<T> type) {
        return this.unmarshallContext.unmarshallList(
                json,
                type
        );
    }

    @Override
    public <T> Set<T> unmarshallSet(final JsonNode json,
                                    final Class<T> type) {
        return this.unmarshallContext.unmarshallSet(
                json,
                type
        );
    }

    @Override
    public <K, V> Map<K, V> unmarshallMap(final JsonNode json,
                                          final Class<K> keyType,
                                          final Class<V> valueType) {
        return this.unmarshallContext.unmarshallMap(
                json,
                keyType,
                valueType
        );
    }

    @Override
    public <T> T unmarshallWithType(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithType(json);
    }

    @Override
    public <T> List<T> unmarshallWithTypeList(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithTypeList(json);
    }

    @Override
    public <T> Set<T> unmarshallWithTypeSet(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithTypeSet(json);
    }

    @Override
    public <K, V> Map<K, V> unmarshallWithTypeMap(final JsonNode json) {
        return this.unmarshallContext.unmarshallWithTypeMap(json);
    }

    @Override
    public Optional<Class<?>> registeredType(final JsonString string) {
        return this.unmarshallContext.registeredType(string);
    }

    @Override
    public Optional<JsonString> typeName(final Class<?> type) {
        return this.unmarshallContext.typeName(type);
    }

    private final JsonNodeUnmarshallContext unmarshallContext;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .label("base").value(this.base)
                .build();
    }
}
