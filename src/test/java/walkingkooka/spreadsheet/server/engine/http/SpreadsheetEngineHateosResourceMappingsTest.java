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
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestParameterName;
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
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineHateosResourceMappingsTest implements ClassTesting2<SpreadsheetEngineHateosResourceMappings> {

    private final static AbsoluteUrl URL = Url.parseAbsolute("https://example.com/");
    private final static Indentation INDENTATION = Indentation.with("  ");
    private final static LineEnding LINE_ENDING = LineEnding.NL;

    // cell.............................................................................................................

    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> CLEAR_CELLS = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> FILL_CELLS = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_CLEAR = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_SKIP = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_FORCE = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_COMPUTE_IF = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> SAVE_CELL = HateosHandlers.fake();
    private final static HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> DELETE_CELL = HateosHandlers.fake();
    private final static Function<SpreadsheetLabelName, SpreadsheetCellReference> LABEL_TO_CELL_REFERENCE = (l) -> {
        throw new UnsupportedOperationException();
    };

    @Test
    public void testCellNullClearCellsFails() {
        this.cellFails(
                null,
                FILL_CELLS,
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
    public void testCellNullFillCellsFails() {
        this.cellFails(
                CLEAR_CELLS,
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
                CLEAR_CELLS,
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
                CLEAR_CELLS,
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
                CLEAR_CELLS,
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
                CLEAR_CELLS,
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
                CLEAR_CELLS,
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
                CLEAR_CELLS,
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
                CLEAR_CELLS,
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

    private void cellFails(final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> clearCells,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell,
                           final HateosHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell,
                           final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference) {
        assertThrows(
                NullPointerException.class,
                () ->
                        SpreadsheetEngineHateosResourceMappings.cell(
                                clearCells,
                                fillCells,
                                loadCellClearValueErrorSkipEvaluate,
                                loadCellSkipEvaluate,
                                loadCellForceRecompute,
                                loadCellComputeIfNecessary,
                                saveCell,
                                deleteCell,
                                labelToCellReference)
        );
    }

    @Test
    public void testRouteCellInvalidFails() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/invalid/A1",
                HttpStatusCode.NOT_FOUND
        );
    }

    @Test
    public void testRouteCellGetLoadCell() {
        this.routeCellFails(
                HttpMethod.GET,
                "/cell/A1",
                UnsupportedOperationException.class
        );
    }

    @Test
    public void testRouteCellGetLoadCellLabelFails() {
        this.routeCellFails(
                HttpMethod.GET,
                "/cell/Label123",
                UnsupportedOperationException.class
        );
    }

    @Test
    public void testRouteCellGetLoadCellUnknownLabelFails() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/UnknownLabel123",
                HttpStatusCode.BAD_REQUEST,
                "Label not found: UnknownLabel123"
        );
    }

    @Test
    public void testRouteCellGetLoadCellViewport() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/*?home=A1&xOffset=0&yOffset=0&width=1000&height=700&includeFrozenColumnsRows=false",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellGetLoadCellViewportEvaluation() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/*/force-recompute?home=A1&xOffset=0&yOffset=0&width=1000&height=700&includeFrozenColumnsRows=false",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellPostSaveCell() {
        this.routeCellFails(
                HttpMethod.POST,
                "/cell/A1",
                "",
                IllegalArgumentException.class,
                "Required resource missing"
        );
    }

    @Test
    public void testRouteCellPostSaveCellLabel() {
        this.routeCellFails(
                HttpMethod.POST,
                "/cell/Label123",
                "",
                IllegalArgumentException.class,
                "Required resource missing"
        );
    }

    @Test
    public void testRouteCellPostSaveCellUnknownLabelFails() {
        this.routeCellAndCheck(
                HttpMethod.POST,
                "/cell/UnknownLabel456",
                HttpStatusCode.BAD_REQUEST,
                "Label not found: UnknownLabel456"
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
                "Label not found: UnknownLabel789"
        );
    }

    // cell/SpreadsheetEngineEvaluation..................................................................................

    @Test
    public void testRouteCellGetLoadCellSpreadsheetEngineEvaluation() {
        for (final SpreadsheetEngineEvaluation evaluation : SpreadsheetEngineEvaluation.values()) {
            this.routeCellFails(
                    HttpMethod.GET,
                    "/cell/A1/" + evaluation.toLinkRelation().toString(),
                    UnsupportedOperationException.class
            );
        }
    }

    @Test
    public void testRouteClearCellsOnePost() {
        this.routeCellAndCheck(
                HttpMethod.POST,
                "/cell/A1/clear",
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                SpreadsheetDelta.EMPTY
                        ).toString(),
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteClearCellRangePost() {
        this.routeCellAndCheck(
                HttpMethod.POST,
                "/cell/A1:B2/clear",
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                SpreadsheetDelta.EMPTY
                        ).toString(),
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteFillCellsPost() {
        this.routeCellAndCheck(
                HttpMethod.POST,
                "/cell/A1:B2/fill",
                JsonNodeMarshallContexts.basic().marshall(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                SpreadsheetSelection.parseCell("B99")
                                                        .setFormula(
                                                                SpreadsheetFormula.EMPTY
                                                                        .setText("1")
                                                        )
                                        )
                                )
                ).toString(),
                HttpStatusCode.OK
        );
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
                        SpreadsheetEngineHttps.clearCells(engine, context),
                        SpreadsheetEngineHttps.fillCells(engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHttps.saveCell(engine, context),
                        SpreadsheetEngineHttps.deleteCell(engine, context),
                        (e) -> context.storeRepository()
                                .labels()
                                .cellReferenceOrRangeOrFail(e)
                                .toCellOrFail()
                ),
                method,
                url,
                body,
                statusCode,
                message
        );
    }

    private HttpResponse routeCell(final HttpMethod method,
                                   final String url,
                                   final String body) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();
        return this.route(
                SpreadsheetEngineHateosResourceMappings.cell(
                        SpreadsheetEngineHttps.clearCells(engine, context),
                        SpreadsheetEngineHttps.fillCells(engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, engine, context),
                        SpreadsheetEngineHttps.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE, engine, context),
                        SpreadsheetEngineHttps.saveCell(engine, context),
                        SpreadsheetEngineHttps.deleteCell(engine, context),
                        (e) -> context.storeRepository()
                                .labels()
                                .cellReferenceOrRangeOrFail(e)
                                .toCellOrFail()
                ),
                method,
                url,
                body
        );
    }

    private void routeCellFails(final HttpMethod method,
                                final String url,
                                final Class<? extends Throwable> thrown) {
        this.routeCellFails(
                method,
                url,
                "",
                thrown
        );
    }

    private Throwable routeCellFails(final HttpMethod method,
                                     final String url,
                                     final String body,
                                     final Class<? extends Throwable> thrown) {
        return assertThrows(
                thrown,
                () -> this.routeCell(method, url, body)
        );
    }

    private void routeCellFails(final HttpMethod method,
                                final String url,
                                final String body,
                                final Class<? extends Throwable> thrown,
                                final String message) {
        final Throwable throwable = this.routeCellFails(
                method,
                url,
                body,
                thrown
        );
        this.checkEquals(message, throwable.getMessage(), "message");
    }

    private SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> range,
                                              final SpreadsheetEngineEvaluation evaluation,
                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                              final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY;
            }

            @Override
            public SpreadsheetDelta deleteCells(final SpreadsheetSelection selection,
                                                final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY
                        .setDeletedCells(
                                selection.toCellRangeOrFail()
                                        .cellStream()
                                        .collect(Collectors.toSet())
                        );
            }

            @Override
            public Set<SpreadsheetCellRange> window(final SpreadsheetViewport viewport,
                                                    final boolean includeFrozenColumnsRows,
                                                    final Optional<SpreadsheetSelection> selection,
                                                    final SpreadsheetEngineContext context) {
                return Sets.of(
                        SpreadsheetCellRange.parseCellRange("B2:C3")
                );
            }

            @Override
            public SpreadsheetDelta fillCells(final Collection<SpreadsheetCell> cells,
                                              final SpreadsheetCellRange from,
                                              final SpreadsheetCellRange to,
                                              final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY;
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
            public SpreadsheetMetadata metadata() {
                return SpreadsheetMetadata.EMPTY.set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                        .loadFromLocale();
            }

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
    public void testColumnNullClearFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.column(null, HateosHandlers.fake(), HateosHandlers.fake(), HateosHandlers.fake())
        );
    }

    @Test
    public void testColumnNullDeleteFails() {
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
    public void testRouteColumnsPostFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteClearColumnsOnePost() {
        this.routeColumnAndCheck(
                HttpMethod.POST,
                "/column/A/clear",
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                SpreadsheetDelta.EMPTY
                        ).toString(),
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteClearColumnRangePost() {
        this.routeColumnAndCheck(
                HttpMethod.POST,
                "/column/A:B/clear",
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                SpreadsheetDelta.EMPTY
                        ).toString(),
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteColumnsInsertAfterPostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/after", IllegalArgumentException.class);
    }

    @Test
    public void testRouteColumnRangeInsertAfterPostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/after", IllegalArgumentException.class);
    }
    
    @Test
    public void testRouteColumnsInsertAfterPost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteColumnRangeInsertAfterPost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteColumnsInsertBeforePostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/before", IllegalArgumentException.class);
    }

    @Test
    public void testRouteColumnRangeInsertBeforePostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/before", IllegalArgumentException.class);
    }
    
    @Test
    public void testRouteColumnsInsertBeforePost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/before?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteColumnRangeInsertBeforePost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/before?count=1", UnsupportedOperationException.class);
    }
    
    @Test
    public void testRouteColumnsPutFails() {
        this.routeColumnAndCheck(HttpMethod.PUT, "/column/A", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteColumnsDelete() {
        this.routeColumnAndCheck(HttpMethod.DELETE, "/column/A", UnsupportedOperationException.class);
    }

    private void routeColumnAndCheck(final HttpMethod method,
                                     final String url,
                                     final HttpStatusCode statusCode) {
        this.routeColumnAndCheck(
                method,
                url,
                "",
                statusCode
        );
    }

    private void routeColumnAndCheck(final HttpMethod method,
                                     final String url,
                                     final String body,
                                     final HttpStatusCode statusCode) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.column(
                        SpreadsheetEngineHttps.clearColumns(engine, context),
                        SpreadsheetEngineHttps.deleteColumns(engine, context),
                        SpreadsheetEngineHttps.insertAfterColumns(engine, context),
                        SpreadsheetEngineHttps.insertBeforeColumns(engine, context)
                ),
                method,
                url,
                body,
                statusCode
        );
    }

    private void routeColumnAndCheck(final HttpMethod method,
                                     final String url,
                                     final Class<? extends Throwable> thcolumnn) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();

        assertThrows(
                thcolumnn,
                () -> this.route(
                        SpreadsheetEngineHateosResourceMappings.column(
                                SpreadsheetEngineHttps.clearColumns(engine, context),
                                SpreadsheetEngineHttps.deleteColumns(engine, context),
                                SpreadsheetEngineHttps.insertAfterColumns(engine, context),
                                SpreadsheetEngineHttps.insertBeforeColumns(engine, context)
                        ),
                        method,
                        url,
                        ""
                )
        );
    }

    // row...........................................................................................................

    @Test
    public void testRowNullClearFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappings.row(null, HateosHandlers.fake(), HateosHandlers.fake(), HateosHandlers.fake())
        );
    }

    @Test
    public void testRowNullDeleteFails() {
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
    public void testRouteRowsPostFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteRowsAfterPostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/after", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowRangeAfterPostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/after", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowsAfterPost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowRangeAfterPost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowsBeforePostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/before", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowRangeBeforePostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/before", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowsBeforePost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/before?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowRangeBeforePost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/before?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowsClearOnePost() {
        this.routeRowAndCheck(
                HttpMethod.POST,
                "/row/1/clear",
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                SpreadsheetDelta.EMPTY
                        ).toString(),
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteRowRangeClearPost() {
        this.routeRowAndCheck(
                HttpMethod.POST,
                "/row/1:2/clear",
                JsonNodeMarshallContexts.basic()
                        .marshall(
                                SpreadsheetDelta.EMPTY
                        ).toString(),
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteRowsPutFails() {
        this.routeRowAndCheck(HttpMethod.PUT, "/row/1", HttpStatusCode.METHOD_NOT_ALLOWED);
    }

    @Test
    public void testRouteRowsDelete() {
        this.routeRowAndCheck(HttpMethod.DELETE, "/row/1", UnsupportedOperationException.class);
    }

    private void routeRowAndCheck(final HttpMethod method,
                                  final String url,
                                  final HttpStatusCode statusCode) {
        this.routeRowAndCheck(
                method,
                url,
                "",
                statusCode
        );
    }

    private void routeRowAndCheck(final HttpMethod method,
                                  final String url,
                                  final String body,
                                  final HttpStatusCode statusCode) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();
        this.routeAndCheck(
                SpreadsheetEngineHateosResourceMappings.row(
                        SpreadsheetEngineHttps.clearRows(engine, context),
                        SpreadsheetEngineHttps.deleteRows(engine, context),
                        SpreadsheetEngineHttps.insertAfterRows(engine, context),
                        SpreadsheetEngineHttps.insertBeforeRows(engine, context)
                ),
                method,
                url,
                body,
                statusCode
        );
    }

    private void routeRowAndCheck(final HttpMethod method,
                                  final String url,
                                  final Class<? extends Throwable> thrown) {
        final SpreadsheetEngine engine = this.engine();
        final SpreadsheetEngineContext context = this.engineContext();

        assertThrows(
                thrown,
                () -> this.route(
                        SpreadsheetEngineHateosResourceMappings.row(
                                SpreadsheetEngineHttps.clearRows(engine, context),
                                SpreadsheetEngineHttps.deleteRows(engine, context),
                                SpreadsheetEngineHttps.insertAfterRows(engine, context),
                                SpreadsheetEngineHttps.insertBeforeRows(engine, context)
                        ),
                        method,
                        url,
                        ""
                )
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
        final Optional<BiConsumer<HttpRequest, HttpResponse>> possible = HateosResourceMapping.router(
                        URL,
                        contentType(),
                        Sets.of(mapping),
                        INDENTATION,
                        LINE_ENDING
                )
                .route(request.routerParameters());
        this.checkNotEquals(Optional.empty(),
                possible,
                () -> method + " " + URL + url);
        if (possible.isPresent()) {
            final HttpResponse response = HttpResponses.recording();
            possible.get().accept(request, response);
            this.checkEquals(statusCode,
                    response.status().map(HttpStatus::value).orElse(null),
                    () -> "status code: " + request + " " + response + "\n" + possible);
            if (null != message) {
                this.checkEquals(message,
                        response.status().map(HttpStatus::message).orElse(null),
                        () -> "status message: " + request + " " + response + "\n" + possible);
            }
        }
    }

    private HttpResponse route(final HateosResourceMapping<?, ?, ?, ?> mapping,
                               final HttpMethod method,
                               final String url,
                               final String requestBody) {
        final HttpRequest request = this.request(method, URL + url, requestBody);
        final Optional<BiConsumer<HttpRequest, HttpResponse>> possible = HateosResourceMapping.router(
                URL,
                contentType(),
                Sets.of(mapping),
                INDENTATION,
                LINE_ENDING
        ).route(request.routerParameters());
        this.checkNotEquals(Optional.empty(),
                possible,
                () -> method + " " + URL + url);
        final HttpResponse response = HttpResponses.recording();
        possible.get()
                .accept(request, response);
        return response;
    }

    private HttpRequest request(final HttpMethod method,
                                final String url,
                                final String bodyText) {
        return new FakeHttpRequest() {

            @Override
            public HttpTransport transport() {
                return HttpTransport.UNSECURED;
            }

            @Override
            public HttpProtocolVersion protocolVersion() {
                return HttpProtocolVersion.VERSION_1_0;
            }

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

            @Override
            public Map<HttpRequestParameterName, List<String>> parameters() {
                return Maps.empty();
            }
        };
    }

    private HateosContentType contentType() {
        return HateosContentType.json(
                JsonNodeUnmarshallContexts.basic(
                        ExpressionNumberKind.BIG_DECIMAL,
                        MathContext.DECIMAL32
                ),
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
