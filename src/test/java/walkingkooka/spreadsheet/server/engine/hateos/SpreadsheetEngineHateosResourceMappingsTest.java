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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.net.http.server.hateos.HateosHandlers;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetCellBox;
import walkingkooka.spreadsheet.SpreadsheetCoordinates;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
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

public final class SpreadsheetEngineHateosResourceMappingsTest implements ClassTesting2<SpreadsheetEngineHateosResourceMappings> {

    private final static AbsoluteUrl URL = Url.parseAbsolute("http://example.com/");

    // cell.............................................................................................................

    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> FILL_CELLS = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_CLEAR = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_SKIP = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_FORCE = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_COMPUTE_IF = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> SAVE_CELL = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> DELETE_CELL = HateosHandlers.fake();

    @Test
    public void testCellNullFillCellsFails() {
        this.cellFails(
                null,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL
        );
    }

    @Test
    public void testCellNullLoadCellClearFails() {
        this.cellFails(
                FILL_CELLS,
                null,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL
        );
    }

    @Test
    public void testCellNullLoadCellSkipFails() {
        this.cellFails(
                FILL_CELLS,
                LOAD_CELL_CLEAR,
                null,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL
        );
    }

    @Test
    public void testCellNullLoadCellForceFails() {
        this.cellFails(
                FILL_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                null,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL
        );
    }

    @Test
    public void testCellNullLoadCellComputeIfFails() {
        this.cellFails(
                FILL_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                null,
                SAVE_CELL,
                DELETE_CELL
        );
    }

    @Test
    public void testCellNullSaveCellFails() {
        this.cellFails(
                FILL_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                null,
                DELETE_CELL
        );
    }

    @Test
    public void testCellNullDeleteCellFails() {
        this.cellFails(
                FILL_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                null
        );
    }

    private void cellFails(final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell) {
        assertThrows(NullPointerException.class, () -> {
            SpreadsheetEngineHateosResourceMappings.cell(fillCells,
                    loadCellClearValueErrorSkipEvaluate,
                    loadCellSkipEvaluate,
                    loadCellForceRecompute,
                    loadCellComputeIfNecessary,
                    saveCell,
                    deleteCell);
        });
    }

    @Test
    public void testRouteCellInvalidFails() {
        this.routeCellAndCheck(HttpMethod.GET, "/invalid/A1", HttpStatusCode.NOT_FOUND);
    }

    @Test
    public void testRouteCellGetLoadCell() {
        this.routeCellAndCheck(HttpMethod.GET, "/cell/A1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteCellPostSaveCell() {
        this.routeCellAndCheck(HttpMethod.POST, "/cell/A1", HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void testRouteCellPutFails() {
        this.routeCellAndCheck(HttpMethod.PUT, "/cell/A1", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteCellDelete() {
        this.routeCellAndCheck(HttpMethod.DELETE, "/cell/A1", HttpStatusCode.BAD_REQUEST);
    }

    // cell/SpreadsheetEngineEvaluation..................................................................................

    @Test
    public void testRouteCellGetLoadCellSpreadsheetEngineEvaluation() {
        for (final SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            this.routeCellAndCheck(
                    HttpMethod.GET,
                    "/cell/A1/" + evaluation.toLinkRelation().toString(),
                    HttpStatusCode.NOT_IMPLEMENTED
            );
        }
    }

    @Test
    public void testRouteFillCellsPost() {
        this.routeCellAndCheck(HttpMethod.POST,
                "/cell/A1:B2/fill",
                JsonNodeMarshallContexts.basic().marshall(
                        SpreadsheetDelta.with(Sets.of(SpreadsheetCell.with(SpreadsheetCellReference.parseCellReference("B99"), SpreadsheetFormula.with("1"))))
                ).toString(),
                HttpStatusCode.NOT_IMPLEMENTED);
    }

    private void routeCellAndCheck(final HttpMethod method,
                                   final String url,
                                   final HttpStatusCode statusCode) {
        this.routeCellAndCheck(method, url, "", statusCode);
    }

    private void routeCellAndCheck(final HttpMethod method,
                                   final String url,
                                   final String body,
                                   final HttpStatusCode statusCode) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.cell(
                        SpreadsheetEngineHateosHandlers.fillCells(engine, context),
                        SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, engine, context),
                        SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, engine, context),
                        SpreadsheetEngineHateosHandlers.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHateosHandlers.saveCell(engine, context),
                        SpreadsheetEngineHateosHandlers.deleteCell(engine, context)
                ),
                method,
                url,
                body,
                statusCode
        );
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

    // cellBox..........................................................................................................

    @Test
    public void testCellBoxNullHateosHandlerFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosResourceMappings.cellBox(null));
    }

    // column...........................................................................................................

    @Test
    public void testColumnNullDeleteFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosResourceMappings.column(null, HateosHandlers.fake()));
    }

    @Test
    public void testColumnNullInsertFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosResourceMappings.column(HateosHandlers.fake(), null));
    }

    @Test
    public void testRouteColumnsInvalidFails() {
        this.routeColumnAndCheck(HttpMethod.GET, "/invalid", HttpStatusCode.NOT_FOUND);
    }

    @Test
    public void testRouteColumnsGetFails() {
        this.routeColumnAndCheck(HttpMethod.GET, "/column/A", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteColumnsPost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteColumnsPutFails() {
        this.routeColumnAndCheck(HttpMethod.PUT, "/column/A", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteColumnsDelete() {
        this.routeColumnAndCheck(HttpMethod.DELETE, "/column/A", HttpStatusCode.NOT_IMPLEMENTED);
    }

    private void routeColumnAndCheck(final HttpMethod method,
                                     final String url,
                                     final HttpStatusCode statusCode) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.column(
                        SpreadsheetEngineHateosHandlers.deleteColumns(engine, context),
                        SpreadsheetEngineHateosHandlers.insertColumns(engine, context)
                ),
                method,
                url,
                "",
                statusCode
        );
    }

    // row...........................................................................................................

    @Test
    public void testRowNullDeleteFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosResourceMappings.row(null, HateosHandlers.fake()));
    }

    @Test
    public void testRowNullInsertFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosResourceMappings.row(HateosHandlers.fake(), null));
    }

    @Test
    public void testRouteRowsInvalidFails() {
        this.routeRowAndCheck(HttpMethod.GET, "/invalid", HttpStatusCode.NOT_FOUND);
    }

    @Test
    public void testRouteRowsGetFails() {
        this.routeRowAndCheck(HttpMethod.GET, "/row/1", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteRowsPost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteRowsPutFails() {
        this.routeRowAndCheck(HttpMethod.PUT, "/row/1", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteRowsDelete() {
        this.routeRowAndCheck(HttpMethod.DELETE, "/row/1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    private void routeRowAndCheck(final HttpMethod method,
                                  final String url,
                                  final HttpStatusCode statusCode) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.row(
                        SpreadsheetEngineHateosHandlers.deleteRows(engine, context),
                        SpreadsheetEngineHateosHandlers.insertRows(engine, context)
                ),
                method,
                url,
                "",
                statusCode
        );
    }

    // viewport..........................................................................................................

    @Test
    public void testViewportNullHateosHandlerFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosResourceMappings.viewport(null));
    }

    private final static String COMPUTE_RANGE_URL = "/viewport/A1:100:200";

    @Test
    public void testRouteComputeRangeInvalidFails() {
        this.routeComputeRangeAndCheck(HttpMethod.GET, "/invalid/A1:100:200", HttpStatusCode.NOT_FOUND);
    }

    @Test
    public void testRouteComputeRangeGet() {
        this.routeComputeRangeAndCheck(HttpMethod.GET, COMPUTE_RANGE_URL, HttpStatusCode.OK);
    }

    @Test
    public void testRouteComputeRangePostFails() {
        this.routeComputeRangeAndCheck(HttpMethod.POST, COMPUTE_RANGE_URL, HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteComputeRangePutFails() {
        this.routeComputeRangeAndCheck(HttpMethod.PUT, COMPUTE_RANGE_URL, HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteComputeRangeDeleteFails() {
        this.routeComputeRangeAndCheck(HttpMethod.DELETE, COMPUTE_RANGE_URL, HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    private void routeComputeRangeAndCheck(final HttpMethod method,
                                           final String url,
                                           final HttpStatusCode statusCode) {
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.viewport(SpreadsheetEngineHateosHandlers.computeRange(this.engine(), this.engineContext())),
                method,
                url,
                "",
                statusCode
        );
    }

    // helpers..........................................................................................................

    private void routeAndCheck(final HateosResourceMapping<?, ?, ?, ?> mapping,
                               final HttpMethod method,
                               final String url,
                               final String requestBody,
                               final HttpStatusCode statusCode
    ) {
        final HttpRequest request = this.request(method, URL + url, requestBody);
        final Optional<BiConsumer<HttpRequest, HttpResponse>> possible = HateosResourceMapping.router(URL,
                contentType(),
                Sets.of(mapping))
                .route(request.routerParameters());
        assertNotEquals(Optional.empty(),
                possible,
                () -> method + " " + URL + url);
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get().accept(request, response);
            assertEquals(statusCode,
                    response.status().map(HttpStatus::value).orElse(null),
                    () -> "status " + request + " " + response + "\n" + possible);
        }
    }

    private HttpRequest request(final HttpMethod method,
                                final String url,
                                final String bodyText) {
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
                final MediaType contentType = HateosContentType.JSON_CONTENT_TYPE;

                return Maps.of(
                        HttpHeaderName.ACCEPT, Lists.of(contentType.accept()),
                        HttpHeaderName.CONTENT_TYPE, Lists.of(contentType.setCharset(CharsetName.UTF_8)),
                        HttpHeaderName.CONTENT_LENGTH, Lists.of(Long.valueOf(this.bodyLength()))
                );
            }

            @Override
            public String bodyText() {
                return bodyText;
            }

            @Override
            public long bodyLength() {
                return bodyText.length();
            }
        };
    }

    private HateosContentType contentType() {
        return HateosContentType.json(
                JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.basic(ExpressionNumberKind.BIG_DECIMAL, MathContext.DECIMAL32)),
                JsonNodeMarshallContexts.basic()
        );
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetEngineHateosResourceMappings> type() {
        return SpreadsheetEngineHateosResourceMappings.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
