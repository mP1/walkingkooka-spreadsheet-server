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
package walkingkooka.spreadsheet.server.engine.http;

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
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.ExpressionNumberContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private final static Function<SpreadsheetLabelName, Optional<SpreadsheetCellReference>> LABEL_TO_CELL_REFERENCE = (l) -> {
        throw new UnsupportedOperationException();
    };

    @Test
    public void testCellNullFillCellsFails() {
        this.cellFails(
                null,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                LABEL_TO_CELL_REFERENCE
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
                DELETE_CELL,
                LABEL_TO_CELL_REFERENCE
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
                DELETE_CELL,
                LABEL_TO_CELL_REFERENCE
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
                DELETE_CELL,
                LABEL_TO_CELL_REFERENCE
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
                DELETE_CELL,
                LABEL_TO_CELL_REFERENCE
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
                DELETE_CELL,
                LABEL_TO_CELL_REFERENCE
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
                null,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullLabelToCellReferenceFails() {
        this.cellFails(
                FILL_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                null
        );
    }

    private void cellFails(final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell,
                           final Function<SpreadsheetLabelName, Optional<SpreadsheetCellReference>> labelToCellReference) {
        assertThrows(NullPointerException.class, () -> {
            SpreadsheetEngineHateosResourceMappings.cell(fillCells,
                    loadCellClearValueErrorSkipEvaluate,
                    loadCellSkipEvaluate,
                    loadCellForceRecompute,
                    loadCellComputeIfNecessary,
                    saveCell,
                    deleteCell,
                    labelToCellReference);
        });
    }

    @Test
    public void testRouteCellInvalidFails() {
        this.routeCellAndCheck(HttpMethod.GET, "/invalid/A1", HttpStatusCode.NOT_FOUND);
    }

    @Test
    public void testRouteCellGetLoadCell() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/A1",
                HttpStatusCode.NOT_IMPLEMENTED,
                "Not implemented"
        );
    }

    @Test
    public void testRouteCellGetLoadCellLabel() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/Label123",
                HttpStatusCode.NOT_IMPLEMENTED,
                "Not implemented"
        );
    }

    @Test
    public void testRouteCellGetLoadCellUnknownLabelFails() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/UnknownLabel123",
                HttpStatusCode.BAD_REQUEST,
                "Unknown label \"UnknownLabel123\""
        );
    }

    @Test
    public void testRouteCellGetLoadCellViewport() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/*?home=A1&xOffset=0&yOffset=0&width=1000&height=700",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellGetLoadCellViewportEvaluation() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=1000&height=700",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellPostSaveCell() {
        this.routeCellAndCheck(
                HttpMethod.POST,
                "/cell/A1",
                HttpStatusCode.BAD_REQUEST,
                "Required resource missing"
        );
    }

    @Test
    public void testRouteCellPostSaveCellLabel() {
        this.routeCellAndCheck(
                HttpMethod.POST,
                "/cell/Label123",
                HttpStatusCode.BAD_REQUEST,
                "Required resource missing"
        );
    }

    @Test
    public void testRouteCellPostSaveCellUnknownLabelFails() {
        this.routeCellAndCheck(
                HttpMethod.POST,
                "/cell/UnknownLabel456",
                HttpStatusCode.BAD_REQUEST,
                "Unknown label \"UnknownLabel456\""
        );
    }

    @Test
    public void testRouteCellPutFails() {
        this.routeCellAndCheck(HttpMethod.PUT, "/cell/A1", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteCellDelete() {
        this.routeCellAndCheck(
                HttpMethod.DELETE,
                "/cell/A1",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellDeleteLabel() {
        this.routeCellAndCheck(
                HttpMethod.DELETE,
                "/cell/Label123",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellDeleteLabelUnknownFails() {
        this.routeCellAndCheck(
                HttpMethod.DELETE,
                "/cell/UnknownLabel789",
                HttpStatusCode.BAD_REQUEST,
                "Unknown label \"UnknownLabel789\""
        );
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
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetCell.with(SpreadsheetSelection.parseCell("B99"), SpreadsheetFormula.with("1"))
                                        )
                                )
                ).toString(),
                HttpStatusCode.NOT_IMPLEMENTED);
    }

    private void routeCellAndCheck(final HttpMethod method,
                                   final String url,
                                   final HttpStatusCode statusCode) {
        this.routeCellAndCheck(method, url, statusCode, null);
    }

    private void routeCellAndCheck(final HttpMethod method,
                                   final String url,
                                   final HttpStatusCode statusCode,
                                   final String message) {
        this.routeCellAndCheck(method, url, "", statusCode, message);
    }

    private void routeCellAndCheck(final HttpMethod method,
                                   final String url,
                                   final String body,
                                   final HttpStatusCode statusCode) {
        this.routeCellAndCheck(
                method,
                url,
                body,
                statusCode,
                null
        );
    }

    private void routeCellAndCheck(final HttpMethod method,
                                   final String url,
                                   final String body,
                                   final HttpStatusCode statusCode,
                                   final String message) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.cell(
                        SpreadsheetEngineHttps.fillCells(engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHttps.saveCell(engine, context),
                        SpreadsheetEngineHttps.deleteCell(engine, context),
                        context.storeRepository().labels()::cellReference
                ),
                method,
                url,
                body,
                statusCode,
                message
        );
    }

    private SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetDelta loadCells(final SpreadsheetCellRange range,
                                              final SpreadsheetEngineEvaluation evaluation,
                                              final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY;
            }

            @Override
            public SpreadsheetDelta deleteCell(final SpreadsheetCellReference cell,
                                               final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                Sets.of(
                                        cell
                                )
                        );
            }

            @Override
            public SpreadsheetCellRange range(final SpreadsheetViewport viewport,
                                              final Optional<SpreadsheetSelection> selection,
                                              final SpreadsheetEngineContext context) {
                return SpreadsheetCellRange.parseCellRange("B2:C3");
            }
        };
    }

    private SpreadsheetEngineContext engineContext() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetLabelName label123 = SpreadsheetExpressionReference.labelName("Label123");

        final SpreadsheetLabelStore labelStore = SpreadsheetLabelStores.treeMap();
        labelStore.save(label123.mapping(a1));

        return new FakeSpreadsheetEngineContext() {
            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetLabelStore labels() {
                        return labelStore;
                    }
                };
            }
        };
    }

    // column...........................................................................................................

    @Test
    public void testColumnNullDeleteFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.column(null, HateosHandlers.fake(), HateosHandlers.fake(), HateosHandlers.fake())
        );
    }

    @Test
    public void testColumnNullInsertFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.column(HateosHandlers.fake(), null, HateosHandlers.fake(), HateosHandlers.fake())
        );
    }

    @Test
    public void testColumnNullInsertAfterFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.column(HateosHandlers.fake(), HateosHandlers.fake(), null, HateosHandlers.fake())
        );
    }

    @Test
    public void testColumnNullInsertBeforeFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.column(HateosHandlers.fake(), HateosHandlers.fake(), HateosHandlers.fake(), null)
        );
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
    public void testRouteColumnsInsertAfterPostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/after", HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void testRouteColumnRangeInsertAfterPostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/after", HttpStatusCode.BAD_REQUEST);
    }
    
    @Test
    public void testRouteColumnsInsertAfterPost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/after?count=1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteColumnRangeInsertAfterPost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/after?count=1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteColumnsInsertBeforePostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/before", HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void testRouteColumnRangeInsertBeforePostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/before", HttpStatusCode.BAD_REQUEST);
    }
    
    @Test
    public void testRouteColumnsInsertBeforePost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/before?count=1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteColumnRangeInsertBeforePost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/before?count=1", HttpStatusCode.NOT_IMPLEMENTED);
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
                        SpreadsheetEngineHttps.deleteColumns(engine, context),
                        SpreadsheetEngineHttps.insertColumns(engine, context),
                        SpreadsheetEngineHttps.insertAfterColumns(engine, context),
                        SpreadsheetEngineHttps.insertBeforeColumns(engine, context)
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
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.row(null, HateosHandlers.fake(), HateosHandlers.fake(), HateosHandlers.fake())
        );
    }

    @Test
    public void testRowNullInsertFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.row(HateosHandlers.fake(), null, HateosHandlers.fake(), HateosHandlers.fake())
        );
    }

    @Test
    public void testRowNullInsertAfterFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.row(HateosHandlers.fake(), HateosHandlers.fake(), null, HateosHandlers.fake())
        );
    }

    @Test
    public void testRowNullInsertBeforeFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.row(HateosHandlers.fake(), HateosHandlers.fake(), HateosHandlers.fake(), null)
        );
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
    public void testRouteRowsAfterPostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/after", HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void testRouteRowRangeAfterPostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/after", HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void testRouteRowsAfterPost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/after?count=1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteRowRangeAfterPost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/after?count=1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteRowsBeforePostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/before", HttpStatusCode.BAD_REQUEST);
    }

    @Test
    public void testRouteRowRangeBeforePostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/before", HttpStatusCode.BAD_REQUEST);
    }
    
    @Test
    public void testRouteRowsBeforePost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/before?count=1", HttpStatusCode.NOT_IMPLEMENTED);
    }

    @Test
    public void testRouteRowRangeBeforePost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/before?count=1", HttpStatusCode.NOT_IMPLEMENTED);
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
                        SpreadsheetEngineHttps.deleteRows(engine, context),
                        SpreadsheetEngineHttps.insertRows(engine, context),
                        SpreadsheetEngineHttps.insertAfterRows(engine, context),
                        SpreadsheetEngineHttps.insertBeforeRows(engine, context)
                ),
                method,
                url,
                "",
                statusCode
        );
    }

    // range..........................................................................................................

    @Test
    public void testRangeNullHateosHandlerFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetEngineHateosResourceMappings.range(null));
    }

    private final static String RANGE_URL = "/range/A1:0:0:100:200";

    @Test
    public void testRouteRangeInvalidFails() {
        this.routeRangeAndCheck(HttpMethod.GET, "/invalid/A1:100:200", HttpStatusCode.NOT_FOUND);
    }

    @Test
    public void testRouteRangeGet() {
        this.routeRangeAndCheck(HttpMethod.GET, RANGE_URL, HttpStatusCode.OK);
    }

    @Test
    public void testRouteRangePostFails() {
        this.routeRangeAndCheck(HttpMethod.POST, RANGE_URL, HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteRangePutFails() {
        this.routeRangeAndCheck(HttpMethod.PUT, RANGE_URL, HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteRangeDeleteFails() {
        this.routeRangeAndCheck(HttpMethod.DELETE, RANGE_URL, HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    private void routeRangeAndCheck(final HttpMethod method,
                                    final String url,
                                    final HttpStatusCode statusCode) {
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.range(SpreadsheetEngineHttps.range(this.engine(), this.engineContext())),
                method,
                url,
                "",
                statusCode,
                null
        );
    }

    // helpers..........................................................................................................

    private void routeAndCheck(final HateosResourceMapping<?, ?, ?, ?> mapping,
                               final HttpMethod method,
                               final String url,
                               final String requestBody,
                               final HttpStatusCode statusCode) {
        this.routeAndCheck(
                mapping,
                method,
                url,
                requestBody,
                statusCode,
                null
        );
    }

    private void routeAndCheck(final HateosResourceMapping<?, ?, ?, ?> mapping,
                               final HttpMethod method,
                               final String url,
                               final String requestBody,
                               final HttpStatusCode statusCode,
                               final String message
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
                    () -> "status code: " + request + " " + response + "\n" + possible);
            if (null != message) {
                assertEquals(message,
                        response.status().map(HttpStatus::message).orElse(null),
                        () -> "status message: " + request + " " + response + "\n" + possible);
            }
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
