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
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.json.JsonHttpRequestHttpResponseBiConsumers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpRequestHttpResponseBiConsumers;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumn;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRow;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.server.engine.http.SpreadsheetEngineHateosResourceMappings;
import walkingkooka.spreadsheet.server.engine.http.SpreadsheetEngineHttps;
import walkingkooka.spreadsheet.server.engine.http.SpreadsheetExpressionReferenceSimilarities;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatRequest;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatResponse;
import walkingkooka.spreadsheet.server.format.SpreadsheetServerFormatters;
import walkingkooka.spreadsheet.server.label.http.SpreadsheetLabelHateosHandlers;
import walkingkooka.spreadsheet.server.label.http.SpreadsheetLabelHateosResourceMappings;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseRequest;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseResponse;
import walkingkooka.spreadsheet.server.parse.SpreadsheetServerParsers;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.json.marshall.util.MarshallUtils;

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
final class BasicSpreadsheetContext implements SpreadsheetContext {

    /**
     * Creates a new empty {@link BasicSpreadsheetContext}
     */
    static BasicSpreadsheetContext with(final AbsoluteUrl base,
                                        final HateosContentType contentType,
                                        final Function<BigDecimal, Fraction> fractioner,
                                        final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                        final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> spreadsheetIdFunctions,
                                        final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                        final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(fractioner, "fractioner");
        Objects.requireNonNull(createMetadata, "createMetadata");
        Objects.requireNonNull(spreadsheetIdFunctions, "spreadsheetIdFunctions");
        Objects.requireNonNull(spreadsheetIdToRepository, "spreadsheetIdToRepository");
        Objects.requireNonNull(spreadsheetMetadataStamper, "spreadsheetMetadataStamper");

        return new BasicSpreadsheetContext(
                base,
                contentType,
                fractioner,
                createMetadata,
                spreadsheetIdFunctions,
                spreadsheetIdToRepository,
                spreadsheetMetadataStamper);
    }

    private BasicSpreadsheetContext(final AbsoluteUrl base,
                                    final HateosContentType contentType,
                                    final Function<BigDecimal, Fraction> fractioner,
                                    final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                    final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> spreadsheetIdFunctions,
                                    final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                    final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        super();

        this.base = base;
        this.contentType = contentType;
        this.fractioner = fractioner;
        this.createMetadata = createMetadata;
        this.spreadsheetIdFunctions = spreadsheetIdFunctions;
        this.spreadsheetIdToRepository = spreadsheetIdToRepository;
        this.spreadsheetMetadataStamper = spreadsheetMetadataStamper;
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
    public Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> httpRouter(final SpreadsheetId id) {
        SpreadsheetContext.checkId(id);

        Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> hateosRouter = this.idToHateosRouter.get(id);
        if (null == hateosRouter) {
            hateosRouter = this.createHttpRouter(id);

            this.idToHateosRouter.put(id, hateosRouter);
        }
        return hateosRouter;
    }

    private final Map<SpreadsheetId, Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>>> idToHateosRouter = Maps.sorted();

    /**
     * Factory that creates a {@link Router} for the given {@link SpreadsheetId spreadsheet}.
     */
    private Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> createHttpRouter(final SpreadsheetId id) {
        final SpreadsheetStoreRepository repository = this.storeRepository(id);

        final SpreadsheetMetadata metadata = this.load(id);

        final SpreadsheetEngine engine = SpreadsheetEngines.stamper(
                SpreadsheetEngines.basic(metadata),
                this.spreadsheetMetadataStamper
        );

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
                        this.cellColumnRowViewportRouter(
                                id,
                                repository,
                                engine,
                                context
                        )
                );
    }

    private final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper;

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

        return HttpRequestHttpResponseBiConsumers.methodNotAllowed(
                HttpMethod.POST,
                JsonHttpRequestHttpResponseBiConsumers.json(
                        MarshallUtils.mapper(
                                SpreadsheetMultiFormatRequest.class,
                                metadata.jsonNodeUnmarshallContext(),
                                metadata.jsonNodeMarshallContext(),
                                SpreadsheetServerFormatters.multiFormatters(context)
                        ),
                        BasicSpreadsheetContext::formatHandlerPostHandler
                )
        );
    }

    private static HttpEntity formatHandlerPostHandler(final HttpEntity entity) {
        return entity.addHeader(
                JsonHttpRequestHttpResponseBiConsumers.X_CONTENT_TYPE_NAME, SpreadsheetMultiFormatResponse.class.getSimpleName()
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
        return HttpRequestHttpResponseBiConsumers.methodNotAllowed(
                HttpMethod.POST,
                JsonHttpRequestHttpResponseBiConsumers.json(
                        MarshallUtils.mapper(
                                SpreadsheetMultiParseRequest.class,
                                metadata.jsonNodeUnmarshallContext(),
                                metadata.jsonNodeMarshallContext(),
                                SpreadsheetServerParsers.multiParsers(context)
                        ),
                        BasicSpreadsheetContext::parseHandlerPostHandler
                )
        );
    }

    private static HttpEntity parseHandlerPostHandler(final HttpEntity entity) {
        return entity.addHeader(
                JsonHttpRequestHttpResponseBiConsumers.X_CONTENT_TYPE_NAME, SpreadsheetMultiParseResponse.class.getSimpleName()
        );
    }

    private static HttpRequestAttributeRouting formatOrParseRouting(final UrlPath spreadsheetId,
                                                                    final String formatOrParse) {
        return HttpRequestAttributeRouting.empty()
                .path(spreadsheetId.append(UrlPathName.with(formatOrParse)));
    }

    private Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> cellColumnRowViewportRouter(final SpreadsheetId id,
                                                                                                               final SpreadsheetStoreRepository repository,
                                                                                                               final SpreadsheetEngine engine,
                                                                                                               final SpreadsheetEngineContext context) {
        final HateosResourceMapping<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetCell> cell = cell(engine, context);

        final HateosResourceMapping<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference = cellReference(engine, context);

        final HateosResourceMapping<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn> column = column(engine, context);

        final HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping> label = label(repository.labels());

        final HateosResourceMapping<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow> row = row(engine, context);

        final HateosResourceMapping<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange, HateosResource<SpreadsheetViewport>> viewport = range(engine, context);

        final AbsoluteUrl base = this.base;

        return HateosResourceMapping.router(base.setPath(base.path().append(UrlPathName.with(id.toString()))),
                this.contentType,
                Sets.of(
                        cell,
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
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells = SpreadsheetEngineHttps.fillCells(engine, context);
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate = SpreadsheetEngineHttps.loadCell(
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate = SpreadsheetEngineHttps.loadCell(
                SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute = SpreadsheetEngineHttps.loadCell(
                SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary = SpreadsheetEngineHttps.loadCell(
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                engine,
                context
        );
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell = SpreadsheetEngineHttps.saveCell(engine, context);
        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell = SpreadsheetEngineHttps.deleteCell(engine, context);

        return SpreadsheetEngineHateosResourceMappings.cell(
                fillCells,
                loadCellClearValueErrorSkipEvaluate,
                loadCellSkipEvaluate,
                loadCellForceRecompute,
                loadCellComputeIfNecessary,
                saveCell,
                deleteCell,
                context.storeRepository().labels()::cellReferenceOrFail
        );
    }

    private static HateosResourceMapping<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference(final SpreadsheetEngine engine,
                                                                                                                                                                                                   final SpreadsheetEngineContext context) {
        final HateosHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> findSimilarities = SpreadsheetEngineHttps.findSimilarities(engine, context);

        return SpreadsheetEngineHateosResourceMappings.cellReference(
                findSimilarities
        );
    }

    private static HateosResourceMapping<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn> column(final SpreadsheetEngine engine,
                                                                                                                                   final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns = SpreadsheetEngineHttps.deleteColumns(engine, context);
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertColumns = SpreadsheetEngineHttps.insertColumns(engine, context);
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterColumns = SpreadsheetEngineHttps.insertAfterColumns(engine, context);
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeColumns = SpreadsheetEngineHttps.insertBeforeColumns(engine, context);

        return SpreadsheetEngineHateosResourceMappings.column(
                deleteColumns,
                insertColumns,
                insertAfterColumns,
                insertBeforeColumns
        );
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
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows = SpreadsheetEngineHttps.deleteRows(engine, context);
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertRows = SpreadsheetEngineHttps.insertRows(engine, context);
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterRows = SpreadsheetEngineHttps.insertAfterRows(engine, context);
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeRows = SpreadsheetEngineHttps.insertBeforeRows(engine, context);

        return SpreadsheetEngineHateosResourceMappings.row(
                deleteRows,
                insertRows,
                insertAfterRows,
                insertBeforeRows
        );
    }

    private static HateosResourceMapping<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange, HateosResource<SpreadsheetViewport>> range(final SpreadsheetEngine engine,
                                                                                                                                                     final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange> handler = SpreadsheetEngineHttps.range(engine, context);
        return SpreadsheetEngineHateosResourceMappings.range(handler);
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