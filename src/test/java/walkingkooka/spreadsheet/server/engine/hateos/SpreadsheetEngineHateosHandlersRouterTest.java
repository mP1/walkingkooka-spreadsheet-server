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
package walkingkooka.spreadsheet.server.engine.hateos;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetCellBox;
import walkingkooka.spreadsheet.SpreadsheetCoordinates;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.tree.expression.ExpressionNumberContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineHateosHandlersRouterTest implements ClassTesting2<SpreadsheetEngineHateosHandlersRouter> {

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.DEFAULT;
    private final static String URL = "http://example.com/api";

    // spreadsheetCellColumnRowRouter...................................................................................

    @Test
    public void testRouterBaseNullFails() {
        this.routerFails(null,
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterContentTypeNullFails() {
        this.routerFails(this.base(),
                null,
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterCellBoxNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                null,
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterComputeRangeNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                null,
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterDeleteColumnsHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                null,
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterDeleteRowsHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                null,
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterFillCellsHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                null,
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterInsertColumnsHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                null,
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterInsertRowsHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                null,
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterLoadCellsClearValueErrorSkipEvaluateHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                null,
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterLoadCellsComputeIfNecessaryHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                null,
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterLoadCellsForceRecomputeHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                null,
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterLoadCellsSkipEvaluateHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                null,
                this.saveCell(),
                this.deleteCell());
    }

    @Test
    public void testRouterSaveCellsHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                null,
                this.deleteCell());
    }

    @Test
    public void testRouterDeleteCellHandlerNullFails() {
        this.routerFails(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                null);
    }

    private void routerFails(final AbsoluteUrl base,
                             final HateosContentType contentType,
                             final HateosHandler<SpreadsheetCoordinates,
                                     SpreadsheetCellBox,
                                     SpreadsheetCellBox> cellBox,
                             final HateosHandler<SpreadsheetViewport,
                                     SpreadsheetRange,
                                     SpreadsheetRange> computeRange,
                             final HateosHandler<SpreadsheetColumnReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> deleteColumns,
                             final HateosHandler<SpreadsheetRowReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> deleteRows,
                             final HateosHandler<SpreadsheetCellReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> fillCells,
                             final HateosHandler<SpreadsheetColumnReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> insertColumns,
                             final HateosHandler<SpreadsheetRowReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> insertRows,
                             final HateosHandler<SpreadsheetCellReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                             final HateosHandler<SpreadsheetCellReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> loadCellSkipEvaluate,
                             final HateosHandler<SpreadsheetCellReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> loadCellForceRecompute,
                             final HateosHandler<SpreadsheetCellReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> loadCellComputeIfNecessary,
                             final HateosHandler<SpreadsheetCellReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> saveCell,
                             final HateosHandler<SpreadsheetCellReference,
                                     SpreadsheetDelta,
                                     SpreadsheetDelta> deleteCell) {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosHandlersRouter.router(base,
                contentType,
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
                deleteCell));
    }

    // cell.............................................................................................................

    @Test
    public void testRouteCellGetLoadCell() {
        this.routeAndCheck(HttpMethod.GET, URL + "/cell/A1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteCellPostSaveCell() {
        this.routeAndCheck(HttpMethod.POST, URL + "/cell/A1", HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void testRouteCellPutFails() {
        this.routeAndFail(HttpMethod.PUT, URL + "/cell/A1");
    }

    @Test
    public void testRouteCellDelete() {
        this.routeAndCheck(HttpMethod.DELETE, URL + "/cell/A1", HttpStatusCode.BAD_REQUEST); // requires SpreadsheetDelta with 0 cells
    }

    // cell/SpreadsheetEngineEvaluation..................................................................................

    @Test
    public void testRouteCellGetLoadCellSpreadsheetEngineEvaluation() {
        for (SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            this.routeAndCheck(HttpMethod.GET, URL + "/cell/A1/" + evaluation.toLinkRelation().toString(), HttpStatusCode.NOT_IMPLEMENTED);
        }
    }

    // column...........................................................................................................

    @Test
    public void testRouteColumnsGetFails() {
        this.routeAndFail(HttpMethod.GET, URL + "/column/A");
    }

    @Test
    public void testRouteColumnsPost() {
        this.routeAndCheck(HttpMethod.POST, URL + "/column/A", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteColumnsPutFails() {
        this.routeAndFail(HttpMethod.PUT, URL + "/column/A");
    }

    @Test
    public void testRouteColumnsDelete() {
        this.routeAndCheck(HttpMethod.DELETE, URL + "/column/A", HttpStatusCode.NOT_IMPLEMENTED);
    }

    // row..............................................................................................................

    @Test
    public void testRouteRowsGetFails() {
        this.routeAndFail(HttpMethod.GET, URL + "/row/1");
    }

    @Test
    public void testRouteRowsPost() {
        this.routeAndCheck(HttpMethod.POST, URL + "/row/1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteRowsPutFails() {
        this.routeAndFail(HttpMethod.PUT, URL + "/row/1");
    }

    @Test
    public void testRouteRowsDelete() {
        this.routeAndCheck(HttpMethod.DELETE, URL + "/row/1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    // fillCells........................................................................................................

    @Test
    public void testRouteFillCellsGetFails() {
        this.routeAndFail(HttpMethod.GET, URL + "/cell/A1:B2/fill");
    }

    @Test
    public void testRouteFillCellsPost() {
        this.routeAndCheck(HttpMethod.POST, URL + "/cell/A1:B2/fill", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteFillCellsPutFails() {
        this.routeAndFail(HttpMethod.PUT, URL + "/cell/A1:B2/fill");
    }

    @Test
    public void testRouteFillCellsDeleteFails() {
        this.routeAndFail(HttpMethod.DELETE, URL + "/cell/A1:B2/fill");
    }

    // computeRange......................................................................................................

    private final static String CELLBOX_URL = URL + "/cellbox/200,400";

    @Test
    public void testRouteCellBoxGet() {
        this.routeAndCheck(HttpMethod.GET, CELLBOX_URL, HttpStatusCode.OK);
    }

    @Test
    public void testRouteCellBoxPostFails() {
        this.routeAndFail(HttpMethod.POST, CELLBOX_URL);
    }

    @Test
    public void testRouteCellBoxPutFails() {
        this.routeAndFail(HttpMethod.PUT, CELLBOX_URL);
    }

    @Test
    public void testRouteCellBoxDeleteFails() {
        this.routeAndFail(HttpMethod.DELETE, CELLBOX_URL);
    }

    // viewport.........................................................................................................

    private final static String COMPUTE_RANGE_URL = URL + "/viewport/A1:100:200";

    @Test
    public void testRouteComputeRangeGet() {
        this.routeAndCheck(HttpMethod.GET, COMPUTE_RANGE_URL, HttpStatusCode.OK);
    }

    @Test
    public void testRouteComputeRangePostFails() {
        this.routeAndFail(HttpMethod.POST, COMPUTE_RANGE_URL);
    }

    @Test
    public void testRouteComputeRangePutFails() {
        this.routeAndFail(HttpMethod.PUT, COMPUTE_RANGE_URL);
    }

    @Test
    public void testRouteComputeRangeDeleteFails() {
        this.routeAndFail(HttpMethod.DELETE, COMPUTE_RANGE_URL);
    }

    // helpers..........................................................................................................

    private void routeAndCheck(final HttpMethod method,
                               final String url,
                               final HttpStatusCode statusCode) {
        final HttpRequest request = this.request(method, url);
        final Optional<BiConsumer<HttpRequest, HttpResponse>> possible = this.route(request);
        assertNotEquals(Optional.empty(),
                possible,
                () -> method + " " + url);
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get().accept(request, response);
            assertEquals(statusCode,
                    response.status().map(HttpStatus::value).orElse(null),
                    () -> "status " + request + " " + response + "\n" + possible);
        }
    }

    private Optional<BiConsumer<HttpRequest, HttpResponse>> route(final HttpRequest request) {
        final Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router = SpreadsheetEngineHateosHandlersRouter.router(this.base(),
                this.contentType(),
                this.cellBox(),
                this.computeRange(),
                this.deleteColumns(),
                this.deleteRows(),
                this.fillCells(),
                this.insertColumns(),
                this.insertRows(),
                this.loadCellClearValueErrorSkipEvaluate(),
                this.loadCellComputeIfNecessary(),
                this.loadCellForceRecompute(),
                this.loadCellSkipEvaluate(),
                this.saveCell(),
                this.deleteCell());
        return router.route(request.routerParameters());
    }

    private HttpRequest request(final HttpMethod method,
                                final String url) {
        return new FakeHttpRequest() {

            @Override
            public RelativeUrl url() {
                return Url.parseAbsolute(url).relativeUrl();
            }

            @Override
            public HttpMethod method() {
                return method;
            }

            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                return Maps.of(HttpHeaderName.CONTENT_TYPE, Lists.of(HateosContentType.JSON_CONTENT_TYPE.setCharset(CharsetName.UTF_8)));
            }

            @Override
            public String bodyText() {
                return "";
            }
        };
    }

    private void routeAndFail(final HttpMethod method,
                              final String url) {
        final HttpRequest request = this.request(method, url);
        final Optional<BiConsumer<HttpRequest, HttpResponse>> possible = this.route(request);
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get().accept(request, response);
            assertEquals(HttpStatusCode.METHOD_NOT_ALLOWED,
                    response.status().map(HttpStatus::value).orElse(null),
                    () -> "status " + request + " " + response + "\n" + possible);
        }
    }

    private AbsoluteUrl base() {
        return Url.parseAbsolute(URL);
    }

    private HateosContentType contentType() {
        return HateosContentType.json(JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.basic(EXPRESSION_NUMBER_KIND, MathContext.DECIMAL32)),
                JsonNodeMarshallContexts.basic());
    }

    private HateosHandler<SpreadsheetCoordinates, SpreadsheetCellBox, SpreadsheetCellBox> cellBox() {
        return SpreadsheetEngineHateosHandlers.cellBox(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetViewport, SpreadsheetRange, SpreadsheetRange> computeRange() {
        return SpreadsheetEngineHateosHandlers.computeRange(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> deleteColumns() {
        return SpreadsheetEngineHateosHandlers.deleteColumns(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> deleteRows() {
        return SpreadsheetEngineHateosHandlers.deleteRows(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells() {
        return SpreadsheetEngineHateosHandlers.fillCells(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta> insertColumns() {
        return SpreadsheetEngineHateosHandlers.insertColumns(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta> insertRows() {
        return SpreadsheetEngineHateosHandlers.insertRows(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate() {
        return this.loadCell(SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE);
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary() {
        return this.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY);
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute() {
        return this.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE);
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate() {
        return this.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE);
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCell(final SpreadsheetEngineEvaluation evaluation) {
        return SpreadsheetEngineHateosHandlers.loadCell(evaluation, this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell() {
        return SpreadsheetEngineHateosHandlers.saveCell(this.engine(), this.engineContext());
    }

    private HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell() {
        return SpreadsheetEngineHateosHandlers.deleteCell(this.engine(), this.engineContext());
    }

    private SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetCellBox cellBox(final SpreadsheetCoordinates coords) {
                return SpreadsheetCellReference.parseCellReference("B2:C3")
                        .cellBox(1, 2, 3, 4);
            }

            @Override
            public SpreadsheetRange computeRange(final SpreadsheetViewport rectangle) {
                return SpreadsheetRange.parseRange("B2:C3");
            }
        };
    }

    private SpreadsheetEngineContext engineContext() {
        return SpreadsheetEngineContexts.fake();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetEngineHateosHandlersRouter> type() {
        return SpreadsheetEngineHateosHandlersRouter.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
