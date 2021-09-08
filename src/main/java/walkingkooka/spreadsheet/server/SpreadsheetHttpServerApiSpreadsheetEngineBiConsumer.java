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
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A handler that routes all spreadsheet API calls.
 */
final class SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer implements BiConsumer<HttpRequest, HttpResponse> {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer} handler.
     */
    static SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer with(final AbsoluteUrl base,
                                                                    final Function<BigDecimal, Fraction> fractioner,
                                                                    final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions,
                                                                    final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                                                    final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer(
                base,
                fractioner,
                idToFunctions,
                idToStoreRepository,
                spreadsheetMetadataStamper
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer(final AbsoluteUrl base,
                                                                final Function<BigDecimal, Fraction> fractioner,
                                                                final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions,
                                                                final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                                                final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        super();

        this.baseUrl = base;
        this.fractioner = fractioner;
        this.idToFunctions = idToFunctions;
        this.idToStoreRepository = idToStoreRepository;

        int spreadsheetIdPathComponent = 0;

        for (final UrlPathName name : base.path()) {
            spreadsheetIdPathComponent++;
        }

        this.spreadsheetIdPathComponent = spreadsheetIdPathComponent;

        this.spreadsheetMetadataStamper = spreadsheetMetadataStamper;
    }

    // Router...........................................................................................................

    @Override
    public void accept(final HttpRequest request,
                       final HttpResponse response) {
        SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerRequest.with(request, response, this)
                .handle();
    }

    // EngineRouter.....................................................................................................

    /**
     * Creates a {@link Router} for engine apis with base url=<code>/api/spreadsheet/$spreadsheetId$/</code> for the given spreadsheet.
     */
    Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router(final SpreadsheetId id) {
        final SpreadsheetStoreRepository repository = this.idToStoreRepository.apply(id);
        final SpreadsheetMetadata metadata = repository.metadatas()
                .loadOrFail(id);

        final SpreadsheetEngine engine = this.engine(metadata);

        final SpreadsheetEngineContext context = this.engineContext(
                id,
                metadata,
                engine,
                repository
        );

        final AbsoluteUrl baseUrl = this.baseUrl;
        final UrlPath spreadsheetIdPath = baseUrl.path().
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

    // engine................................................................................................................

    private Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> cellColumnRowViewportRouter(final SpreadsheetId id,
                                                                                                               final SpreadsheetStoreRepository repository,
                                                                                                               final SpreadsheetEngine engine,
                                                                                                               final SpreadsheetEngineContext context) {
        final HateosResourceMapping<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetCell> cell = cell(engine, context);

        final HateosResourceMapping<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> cellReference = cellReference(engine, context);

        final HateosResourceMapping<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetColumn> column = column(engine, context);

        final HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping> label = label(repository.labels());

        final HateosResourceMapping<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange, HateosResource<SpreadsheetViewport>> range = range(engine, context);

        final HateosResourceMapping<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow> row = row(engine, context);

        final AbsoluteUrl base = this.baseUrl;

        final SpreadsheetMetadata metadata = context.metadata();

        return HateosResourceMapping.router(base.setPath(base.path().append(UrlPathName.with(id.toString()))),
                HateosContentType.json(
                        metadata.jsonNodeUnmarshallContext().setPreProcessor(SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerPreProcessorBiFunction.with(repository.labels())),
                        metadata.jsonNodeMarshallContext()
                ),
                Sets.of(
                        cell,
                        cellReference,
                        column,
                        label,
                        range,
                        row
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
                context.storeRepository().labels()::cellReference
        );
    }

    private SpreadsheetEngine engine(final SpreadsheetMetadata metadata) {
        return SpreadsheetEngines.stamper(
                SpreadsheetEngines.basic(metadata),
                this.spreadsheetMetadataStamper
        );
    }

    /**
     * Updates the last-modified timestamp.
     */
    private final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper;

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
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterColumns = SpreadsheetEngineHateosHandlers.insertAfterColumns(engine, context);
        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeColumns = SpreadsheetEngineHateosHandlers.insertBeforeColumns(engine, context);

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

    private static HateosResourceMapping<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange, HateosResource<SpreadsheetViewport>> range(final SpreadsheetEngine engine,
                                                                                                                                                     final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange> handler = SpreadsheetEngineHateosHandlers.range(engine, context);
        return SpreadsheetEngineHateosResourceMappings.range(handler);
    }

    private static HateosResourceMapping<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetRow> row(final SpreadsheetEngine engine,
                                                                                                                          final SpreadsheetEngineContext context) {
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows = SpreadsheetEngineHateosHandlers.deleteRows(engine, context);
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertRows = SpreadsheetEngineHateosHandlers.insertRows(engine, context);
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertAfterRows = SpreadsheetEngineHateosHandlers.insertAfterRows(engine, context);
        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertBeforeRows = SpreadsheetEngineHateosHandlers.insertBeforeRows(engine, context);

        return SpreadsheetEngineHateosResourceMappings.row(
                deleteRows,
                insertRows,
                insertAfterRows,
                insertBeforeRows
        );
    }

    private SpreadsheetEngineContext engineContext(final SpreadsheetId id,
                                                   final SpreadsheetMetadata metadata,
                                                   final SpreadsheetEngine engine,
                                                   final SpreadsheetStoreRepository repository) {
        return SpreadsheetEngineContexts.basic(
                metadata,
                this.idToFunctions.apply(id),
                engine,
                this.fractioner,
                repository);
    }

    private final Function<BigDecimal, Fraction> fractioner;

    private final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions;

    /**
     * A {@link Function} that returns a {@link SpreadsheetStoreRepository} for a given {@link SpreadsheetId}.
     */
    private final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository;

    // shared with SpreadsheetHttpServerApiSpreadsheetEngineBiConsumerRequest
    final int spreadsheetIdPathComponent;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.baseUrl.toString();
    }

    private final AbsoluteUrl baseUrl;
}
