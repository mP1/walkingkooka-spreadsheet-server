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
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCellBox;
import walkingkooka.spreadsheet.SpreadsheetCoordinates;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.conditionalformat.SpreadsheetConditionalFormattingRule;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStore;
import walkingkooka.spreadsheet.server.engine.hateos.SpreadsheetEngineHateosHandlers;
import walkingkooka.spreadsheet.server.format.Formatters;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatRequest;
import walkingkooka.spreadsheet.server.format.SpreadsheetMultiFormatResponse;
import walkingkooka.spreadsheet.server.parse.Parsers;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseRequest;
import walkingkooka.spreadsheet.server.parse.SpreadsheetMultiParseResponse;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
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
                                                                    final HateosContentType contentTypeJson,
                                                                    final Function<BigDecimal, Fraction> fractioner,
                                                                    final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions,
                                                                    final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer(base,
                contentTypeJson,
                fractioner,
                idToFunctions,
                idToStoreRepository);
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer(final AbsoluteUrl base,
                                                                final HateosContentType contentTypeJson,
                                                                final Function<BigDecimal, Fraction> fractioner,
                                                                final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions,
                                                                final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository) {
        super();

        this.baseUrl = base;
        this.contentTypeJson = contentTypeJson;
        this.fractioner = fractioner;
        this.idToFunctions = idToFunctions;
        this.idToStoreRepository = idToStoreRepository;

        int spreadsheetIdPathComponent = 0;

        for(final UrlPathName name : base.path()) {
            spreadsheetIdPathComponent++;
        }

        this.spreadsheetIdPathComponent = spreadsheetIdPathComponent;
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
        final SpreadsheetMetadata metadata = repository.metadatas().loadOrFail(id);

        final AbsoluteUrl baseUrl = this.baseUrl;
        final UrlPath spreadsheetIdPath = baseUrl.path().
                append(UrlPathName.with(id.hateosLinkId()));

        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetEngine engine = this.engine(
                id,
                metadata,
                repository,
                labelStore
        );

        final SpreadsheetEngineContext context = this.engineContext(
                id,
                metadata,
                labelStore,
                engine
        );

        // insert handling of /format & /parse
        return formatRouter(spreadsheetIdPath, context, metadata)
                .then(parseRouter(spreadsheetIdPath, context, metadata))
                .then(this.engineHateosRouter(id,
                        repository,
                        metadata,
                        baseUrl.setPath(spreadsheetIdPath))
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

    private Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> engineHateosRouter(final SpreadsheetId id,
                                                                                                      final SpreadsheetStoreRepository repository,
                                                                                                      final SpreadsheetMetadata metadata,
                                                                                                      final AbsoluteUrl spreadsheetId) {
        final SpreadsheetLabelStore labelStore = repository.labels();

        final SpreadsheetEngine engine = this.engine(
                id,
                metadata,
                repository,
                labelStore
        );

        final SpreadsheetEngineContext context = this.engineContext(
                id,
                metadata,
                labelStore,
                engine
        );

        // else default to engine hateos handlers...
        final HateosHandler<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox> cellBox = SpreadsheetEngineHateosHandlers.cellBox(engine, context);

        final HateosHandler<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange> computeRange = SpreadsheetEngineHateosHandlers.computeRange(engine, context);

        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns = SpreadsheetEngineHateosHandlers.deleteColumns(engine, context);

        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows = SpreadsheetEngineHateosHandlers.deleteRows(engine, context);

        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells = SpreadsheetEngineHateosHandlers.fillCells(engine, context);

        final HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertColumns = SpreadsheetEngineHateosHandlers.insertColumns(engine, context);

        final HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertRows = SpreadsheetEngineHateosHandlers.insertRows(engine, context);

        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate = SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                engine,
                context);

        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate = SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                engine,
                context);

        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute = SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE,
                engine,
                context);

        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary = SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                engine,
                context);

        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell = SpreadsheetEngineHateosHandlers.saveCell(engine, context);

        final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell = SpreadsheetEngineHateosHandlers.deleteCell(engine, context);

        return SpreadsheetEngineHateosHandlers.engineRouter(spreadsheetId,
                this.contentTypeJson,
                cellBox,
                computeRange,
                deleteColumns,
                deleteRows,
                fillCells,
                insertColumns,
                insertRows,
                loadCellClearValueErrorSkipEvaluate,
                loadCellSkipEvaluate,
                loadCellForceRecompute,
                loadCellComputeIfNecessary,
                saveCell,
                deleteCell);
    }

    private SpreadsheetEngine engine(final SpreadsheetId id,
                                     final SpreadsheetMetadata metadata,
                                     final SpreadsheetStoreRepository repository,
                                     final SpreadsheetLabelStore labelStore) {
        final SpreadsheetCellStore cellStore = SpreadsheetCellStores.spreadsheetFormulaSpreadsheetMetadataAware(
                repository.cells(),
                metadata
        );
        final SpreadsheetExpressionReferenceStore<SpreadsheetCellReference> cellReferencesStore = repository.cellReferences();
        final SpreadsheetExpressionReferenceStore<SpreadsheetLabelName> labelReferencesStore = repository.labelReferences();
        final SpreadsheetRangeStore<SpreadsheetCellReference> rangeToCellStore = repository.rangeToCells();
        final SpreadsheetRangeStore<SpreadsheetConditionalFormattingRule> rangeToConditionalFormattingRuleStore = repository.rangeToConditionalFormattingRules();

        return SpreadsheetEngines.basic(
                id,
                metadata,
                cellStore,
                cellReferencesStore,
                labelStore,
                labelReferencesStore,
                rangeToCellStore,
                rangeToConditionalFormattingRuleStore
        );
    }

    private SpreadsheetEngineContext engineContext(final SpreadsheetId id,
                                                   final SpreadsheetMetadata metadata,
                                                   final SpreadsheetLabelStore labelStore,
                                                   final SpreadsheetEngine engine) {
        return SpreadsheetEngineContexts.basic(
                metadata.parser(),
                metadata.getOrFail(SpreadsheetMetadataPropertyName.VALUE_SEPARATOR),
                this.idToFunctions.apply(id),
                engine,
                labelStore,
                metadata.converterContext(),
                metadata.numberToColor(),
                metadata.nameToColor(),
                metadata.getOrFail(SpreadsheetMetadataPropertyName.WIDTH),
                this.fractioner,
                metadata.formatter());
    }

    private final HateosContentType contentTypeJson;

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
