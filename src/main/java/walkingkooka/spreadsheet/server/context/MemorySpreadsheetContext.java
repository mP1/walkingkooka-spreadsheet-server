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

package walkingkooka.spreadsheet.server.context;

import walkingkooka.ToStringBuilder;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.http.json.JsonHttpRequestHttpResponseBiConsumers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetCellBox;
import walkingkooka.spreadsheet.SpreadsheetCoordinates;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumn;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRow;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.server.engine.hateos.SpreadsheetEngineHateosHandlers;
import walkingkooka.spreadsheet.server.engine.hateos.SpreadsheetEngineHateosResourceMappings;
import walkingkooka.spreadsheet.server.engine.hateos.SpreadsheetExpressionReferenceSimilarities;
import walkingkooka.spreadsheet.server.format.Formatters;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatRequest;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatResponse;
import walkingkooka.spreadsheet.server.label.hateos.SpreadsheetLabelHateosHandlers;
import walkingkooka.spreadsheet.server.label.hateos.SpreadsheetLabelHateosResourceMappings;
import walkingkooka.spreadsheet.server.parse.Parsers;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseRequest;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseResponse;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@link SpreadsheetContext} that creates a new {@link SpreadsheetStoreRepository} for unknown {@link SpreadsheetId}.
 * There is no way to delete existing spreadsheets.
 */
final class MemorySpreadsheetContext implements SpreadsheetContext {

    /**
     * Creates a new empty {@link MemorySpreadsheetContext}
     */
    static MemorySpreadsheetContext with(final AbsoluteUrl base,
                                         final HateosContentType contentType,
                                         final Function<BigDecimal, Fraction> fractioner,
                                         final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                         final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> spreadsheetIdFunctions,
                                         final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(fractioner, "fractioner");
        Objects.requireNonNull(createMetadata, "createMetadata");
        Objects.requireNonNull(spreadsheetIdFunctions, "spreadsheetIdFunctions");
        Objects.requireNonNull(spreadsheetIdToRepository, "spreadsheetIdToRepository");

        return new MemorySpreadsheetContext(base,
                contentType,
                fractioner,
                createMetadata,
                spreadsheetIdFunctions,
                spreadsheetIdToRepository);
    }

    private MemorySpreadsheetContext(final AbsoluteUrl base,
                                     final HateosContentType contentType,
                                     final Function<BigDecimal, Fraction> fractioner,
                                     final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                     final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> spreadsheetIdFunctions,
                                     final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository) {
        super();

        this.base = base;
        this.contentType = contentType;
        this.fractioner = fractioner;
        this.createMetadata = createMetadata;
        this.spreadsheetIdFunctions = spreadsheetIdFunctions;
        this.spreadsheetIdToRepository = spreadsheetIdToRepository;
    }

    @Override
    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
        return this.createMetadata.apply(locale);
    }

    private final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata;

    @Override
    public Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>> functions(final SpreadsheetId id) {
        return this.spreadsheetIdFunctions.apply(id);
    }

    private final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> spreadsheetIdFunctions;

    /**
     * Loads the {@link SpreadsheetMetadata} and then executes the given getter to return a particular property.
     */
    private <T> T loadAndGet(final SpreadsheetId id,
                             final Function<SpreadsheetMetadata, T> getter) {
        Objects.requireNonNull(id, "id");

        return getter.apply(this.load(id));
    }

    private SpreadsheetMetadata load(final SpreadsheetId id) {
        return this.spreadsheetIdToRepository.apply(id)
                .metadatas()
                .loadOrFail(id);
    }

    // hateosRouter.....................................................................................................

    /**
     * Lazily creates a {@link Router} using the {@link SpreadsheetId} to a cache.
     */
    @Override
    public Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> hateosRouter(final SpreadsheetId id) {
        SpreadsheetContext.checkId(id);

        Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> hateosRouter = this.idToHateosRouter.get(id);
        if (null == hateosRouter) {
            hateosRouter = this.createHateosHandler(id);

            this.idToHateosRouter.put(id, hateosRouter);
        }
        return hateosRouter;
    }

    private final Map<SpreadsheetId, Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>>> idToHateosRouter = Maps.sorted();

    /**
     * Factory that creates a {@link Router} for the given {@link SpreadsheetId spreadsheet}.
     */
    private Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> createHateosHandler(final SpreadsheetId id) {
        final SpreadsheetStoreRepository repository = this.storeRepository(id);

        final SpreadsheetMetadata metadata = this.load(id);

        final SpreadsheetEngine engine = SpreadsheetEngines.basic(metadata);

        final Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>> functions = this.spreadsheetIdFunctions.apply(id);
        final Function<BigDecimal, Fraction> fractioner = this.fractioner;

        final SpreadsheetEngineContext context = SpreadsheetEngineContexts.basic(
                metadata,
                functions,
                engine,
                fractioner,
                repository
        );

        final AbsoluteUrl base = this.base;
        final UrlPath spreadsheetIdPath = base.path().
                append(UrlPathName.with(id.hateosLinkId()));

        return formatRouter(spreadsheetIdPath, context, metadata)
                .then(parseRouter(spreadsheetIdPath, context, metadata))
                .then(
                        this.cellCellBoxColumnRowViewportRouter(
                                id,
                                repository,
                                engine,
                                context
                        )
                );
    }

    // format...........................................................................................................

    private static Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> formatRouter(final UrlPath spreadsheetId,
                                                                                                       final SpreadsheetEngineContext context,
                                                                                                       final SpreadsheetMetadata metadata) {
        return RouteMappings.<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>>empty()
                .add(formatRouting(spreadsheetId).build(), formatHandler(context, metadata))
                .router();
    }

    private static HttpRequestAttributeRouting formatRouting(final UrlPath spreadsheetId) {
        return formatOrParseRouting(spreadsheetId,
                "format");
    }

    private static BiConsumer<HttpRequest, HttpResponse> formatHandler(final SpreadsheetEngineContext context,
                                                                       final SpreadsheetMetadata metadata) {
        return JsonHttpRequestHttpResponseBiConsumers.postRequestBody(
                Formatters.multiFormatters(context),
                SpreadsheetMultiFormatRequest.class,
                SpreadsheetMultiFormatResponse.class,
                metadata.jsonNodeMarshallContext(),
                metadata.jsonNodeUnmarshallContext()
        );
    }

    // parse............................................................................................................

    private static Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> parseRouter(final UrlPath spreadsheetId,
                                                                                                      final SpreadsheetEngineContext context,
                                                                                                      final SpreadsheetMetadata metadata) {
        return RouteMappings.<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>>empty()
                .add(parseRouting(spreadsheetId).build(), parseHandler(context, metadata))
                .router();
    }

    private static HttpRequestAttributeRouting parseRouting(final UrlPath spreadsheetId) {
        return formatOrParseRouting(spreadsheetId,
                "parse");
    }

    private static BiConsumer<HttpRequest, HttpResponse> parseHandler(final SpreadsheetEngineContext context,
                                                                      final SpreadsheetMetadata metadata) {
        return JsonHttpRequestHttpResponseBiConsumers.postRequestBody(
                Parsers.multiParsers(context),
                SpreadsheetMultiParseRequest.class,
                SpreadsheetMultiParseResponse.class,
                metadata.jsonNodeMarshallContext(),
                metadata.jsonNodeUnmarshallContext()
        );
    }

    private static HttpRequestAttributeRouting formatOrParseRouting(final UrlPath spreadsheetId,
                                                                    final String formatOrParse) {
        return HttpRequestAttributeRouting.empty()
                .path(spreadsheetId.append(UrlPathName.with(formatOrParse)));
    }

    private Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> cellCellBoxColumnRowViewportRouter(final SpreadsheetId id,
                                                                                                                      final SpreadsheetStoreRepository repository,
                                                                                                                      final SpreadsheetEngine engine,
                                                                                                                      final SpreadsheetEngineContext context) {
        final HateosResourceMapping<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetCell> cell = cell(engine, context);

        final HateosResourceMapping<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox, HateosResource<SpreadsheetCoordinates>> cellBox = cellBox(engine, context);

        final HateosResourceMapping<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference = cellReference(engine, context);

        final HateosResourceMapping<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn> column = column(engine, context);

        final HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping> label = label(repository.labels());

        final HateosResourceMapping<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow> row = row(engine, context);

        final HateosResourceMapping<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange, HateosResource<SpreadsheetViewport>> viewport = viewport(engine, context);

        final AbsoluteUrl base = this.base;

        return HateosResourceMapping.router(base.setPath(base.path().append(UrlPathName.with(id.toString()))),
                this.contentType,
                Sets.of(
                        cell,
                        cellBox,
                        cellReference,
                        column,
                        label,
                        row,
                        viewport
                )
        );
    }

    private static HateosResourceMapping<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetCell> cell(final SpreadsheetEngine engine,
                                                                                                                             final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells = SpreadsheetEngineHateosHandlers.fillCells(engine, context);
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate = SpreadsheetEngineHateosHandlers.loadCell(
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate = SpreadsheetEngineHateosHandlers.loadCell(
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute = SpreadsheetEngineHateosHandlers.loadCell(
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary = SpreadsheetEngineHateosHandlers.loadCell(
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell = SpreadsheetEngineHateosHandlers.saveCell(engine, context);
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell = SpreadsheetEngineHateosHandlers.deleteCell(engine, context);

        return SpreadsheetEngineHateosResourceMappings.cell(
                fillCells,
                loadCellClearValueErrorSkipEvaluate,
                loadCellSkipEvaluate,
                loadCellForceRecompute,
                loadCellComputeIfNecessary,
                saveCell,
                deleteCell,
                (r) -> SpreadsheetEngineHateosResourceMappings.reference(r, context.storeRepository().labels())
        );
    }

    private static HateosResourceMapping<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox, HateosResource<SpreadsheetCoordinates>> cellBox(final SpreadsheetEngine engine,
                                                                                                                                                         final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox> handler = SpreadsheetEngineHateosHandlers.cellBox(engine, context);
        return SpreadsheetEngineHateosResourceMappings.cellBox(handler);
    }

    private static HateosResourceMapping<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference(final SpreadsheetEngine engine,
                                                                                                                                                                                                   final SpreadsheetEngineContext context) {
        final HateosHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> findSimilarities = SpreadsheetEngineHateosHandlers.findSimilarities(engine, context);

        return SpreadsheetEngineHateosResourceMappings.cellReference(
                findSimilarities
        );
    }

    private static HateosResourceMapping<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn> column(final SpreadsheetEngine engine,
                                                                                                                                   final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns = SpreadsheetEngineHateosHandlers.deleteColumns(engine, context);
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertColumns = SpreadsheetEngineHateosHandlers.insertColumns(engine, context);

        return SpreadsheetEngineHateosResourceMappings.column(deleteColumns, insertColumns);
    }

    private static HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping> label(final SpreadsheetLabelStore store) {

        final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> delete = SpreadsheetLabelHateosHandlers.delete(store);
        final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> load = SpreadsheetLabelHateosHandlers.load(store);
        final HateosHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> saveOrUpdate = SpreadsheetLabelHateosHandlers.saveOrUpdate(store);

        return SpreadsheetLabelHateosResourceMappings.with(
                delete,
                load,
                saveOrUpdate
        );
    }

    private static HateosResourceMapping<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow> row(final SpreadsheetEngine engine,
                                                                                                                          final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows = SpreadsheetEngineHateosHandlers.deleteRows(engine, context);
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertRows = SpreadsheetEngineHateosHandlers.insertRows(engine, context);

        return SpreadsheetEngineHateosResourceMappings.row(deleteRows, insertRows);
    }

    private static HateosResourceMapping<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange, HateosResource<SpreadsheetViewport>> viewport(final SpreadsheetEngine engine,
                                                                                                                                                final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange> handler = SpreadsheetEngineHateosHandlers.computeRange(engine, context);
        return SpreadsheetEngineHateosResourceMappings.viewport(handler);
    }

    private final AbsoluteUrl base;
    private final HateosContentType contentType;
    private final Function<BigDecimal, Fraction> fractioner;

    @Override
    public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
        return this.spreadsheetIdToRepository.apply(id);
    }

    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .label("base").value(this.base)
                .label("contentType").value(this.contentType)
                .build();
    }
}
