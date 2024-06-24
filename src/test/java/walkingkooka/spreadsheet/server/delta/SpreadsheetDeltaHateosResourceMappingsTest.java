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
package walkingkooka.spreadsheet.server.delta;

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
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlers;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.compare.SpreadsheetColumnOrRowSpreadsheetComparatorNames;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.format.SpreadsheetParserName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetDeltaHateosResourceMappingsTest implements ClassTesting2<SpreadsheetDeltaHateosResourceMappings> {

    private final static AbsoluteUrl URL = Url.parseAbsolute("https://example.com/");
    private final static Indentation INDENTATION = Indentation.SPACES2;
    private final static LineEnding LINE_ENDING = LineEnding.NL;

    private final static int DEFAULT_MAX = 999;

    // cell.............................................................................................................

    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> CLEAR_CELLS = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> FILL_CELLS = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> FIND_CELLS = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_CLEAR = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_SKIP = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_FORCE = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> LOAD_CELL_COMPUTE_IF = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> SAVE_CELL = HateosResourceHandlers.fake();
    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> DELETE_CELL = HateosResourceHandlers.fake();

    private final static HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> SORT_CELLS = HateosResourceHandlers.fake();
    private final static Function<SpreadsheetLabelName, SpreadsheetCellReference> LABEL_TO_CELL_REFERENCE = (l) -> {
        throw new UnsupportedOperationException();
    };

    @Test
    public void testCellNullFillCellsFails() {
        this.cellFails(
                null,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullFindCellsFails() {
        this.cellFails(
                FILL_CELLS,
                null,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullLoadCellClearFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                null,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullLoadCellSkipFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                null,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullLoadCellForceFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                null,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullLoadCellComputeIfFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                null,
                SAVE_CELL,
                DELETE_CELL,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullSaveCellFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                null,
                DELETE_CELL,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullDeleteCellFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                null,
                SORT_CELLS,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullSortCellsFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                null,
                LABEL_TO_CELL_REFERENCE
        );
    }

    @Test
    public void testCellNullLabelToCellReferenceFails() {
        this.cellFails(
                FILL_CELLS,
                FIND_CELLS,
                LOAD_CELL_CLEAR,
                LOAD_CELL_SKIP,
                LOAD_CELL_FORCE,
                LOAD_CELL_COMPUTE_IF,
                SAVE_CELL,
                DELETE_CELL,
                SORT_CELLS,
                null
        );
    }

    private void cellFails(final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> fillCells,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> findCells,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellClearValueErrorSkipEvaluate,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellSkipEvaluate,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellForceRecompute,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> loadCellComputeIfNecessary,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> saveCell,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> deleteCell,
                           final HateosResourceHandler<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta> sortCells,
                           final Function<SpreadsheetLabelName, SpreadsheetCellReference> labelToCellReference) {
        assertThrows(
                NullPointerException.class,
                () ->
                        SpreadsheetDeltaHateosResourceMappings.cell(
                                fillCells,
                                findCells,
                                loadCellClearValueErrorSkipEvaluate,
                                loadCellSkipEvaluate,
                                loadCellForceRecompute,
                                loadCellComputeIfNecessary,
                                saveCell,
                                deleteCell,
                                sortCells,
                                labelToCellReference
                        )
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
                "/cell/*?home=A1&width=1000&height=700&includeFrozenColumnsRows=false",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellGetLoadCellViewportEvaluation() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/*/force-recompute?home=A1&width=1000&height=700&includeFrozenColumnsRows=false",
                HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteCellGetSortCells() {
        this.routeCellAndCheck(
                HttpMethod.GET,
                "/cell/A1:C3/sort?comparators=A=day-of-month;B=month-of-year;C=year",
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
                HttpStatusCode.BAD_REQUEST
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
                HttpStatusCode.BAD_REQUEST
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
                SpreadsheetDeltaHateosResourceMappings.cell(
                        SpreadsheetDeltaHttps.fillCells(engine, context),
                        SpreadsheetDeltaHttps.findCells(DEFAULT_MAX, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE, engine, context),
                        SpreadsheetDeltaHttps.saveCell(engine, context),
                        SpreadsheetDeltaHttps.deleteCell(engine, context),
                        SpreadsheetDeltaHttps.sortCells(engine, context),
                        (e) -> context.storeRepository()
                                .labels()
                                .cellReferenceOrRangeOrFail(e)
                                .toCell()
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
                SpreadsheetDeltaHateosResourceMappings.cell(
                        SpreadsheetDeltaHttps.fillCells(engine, context),
                        SpreadsheetDeltaHttps.findCells(DEFAULT_MAX, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.CLEAR_VALUE_ERROR_SKIP_EVALUATE, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.FORCE_RECOMPUTE, engine, context),
                        SpreadsheetDeltaHttps.loadCell(SpreadsheetEngineEvaluation.SKIP_EVALUATE, engine, context),
                        SpreadsheetDeltaHttps.saveCell(engine, context),
                        SpreadsheetDeltaHttps.deleteCell(engine, context),
                        SpreadsheetDeltaHttps.sortCells(engine, context),
                        (e) -> context.storeRepository()
                                .labels()
                                .cellReferenceOrRangeOrFail(e)
                                .toCell()
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
            public SpreadsheetDelta loadCells(final Set<SpreadsheetCellRangeReference> range,
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
                                selection.toCellRange()
                                        .cellStream()
                                        .collect(Collectors.toSet())
                        );
            }

            @Override
            public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                     final boolean includeFrozenColumnsRows,
                                                     final Optional<SpreadsheetSelection> selection,
                                                     final SpreadsheetEngineContext context) {
                return SpreadsheetViewportWindows.parse("B2:C3");
            }

            @Override
            public SpreadsheetDelta fillCells(final Collection<SpreadsheetCell> cells,
                                              final SpreadsheetCellRangeReference from,
                                              final SpreadsheetCellRangeReference to,
                                              final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY;
            }

            @Override
            public SpreadsheetDelta sortCells(final SpreadsheetCellRangeReference cellRange,
                                              final List<SpreadsheetColumnOrRowSpreadsheetComparatorNames> comparatorNames,
                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                              final SpreadsheetEngineContext context) {
                return SpreadsheetDelta.EMPTY.setCells(
                        Sets.of(
                                SpreadsheetSelection.A1.setFormula(SpreadsheetFormula.EMPTY.setText("=1+2"))
                        )
                );
            }

            @Override
            public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                          final SpreadsheetEngineContext context) {
                checkEquals(
                        Lists.empty(),
                        viewport.navigations(),
                        "navigations"
                );
                return Optional.of(viewport);
            }
        };
    }

    private SpreadsheetEngineContext engineContext() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetLabelName label123 = SpreadsheetExpressionReference.labelName("Label123");

        final SpreadsheetLabelStore labelStore = SpreadsheetLabelStores.treeMap();
        labelStore.save(label123.mapping(a1));

        return new FakeSpreadsheetEngineContext() {

            @Override
            public SpreadsheetMetadata spreadsheetMetadata() {
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

            // required by GET comparators

            @Override
            public Set<SpreadsheetComparatorInfo> spreadsheetComparatorInfos() {
                return Sets.of(
                        SpreadsheetComparatorInfo.with(
                                Url.parseAbsolute("https://example.com/comparator-1"),
                                SpreadsheetComparatorName.with("comparator-1")
                        )
                );
            }

            @Override
            public Set<SpreadsheetFormatterInfo> spreadsheetFormatterInfos() {
                return Sets.of(
                        SpreadsheetFormatterInfo.with(
                                Url.parseAbsolute("https://example.com/formatter-1"),
                                SpreadsheetFormatterName.with("formatter-1")
                        )
                );
            }

            @Override
            public Set<ExpressionFunctionInfo> expressionFunctionInfos() {
                return Sets.of(
                        ExpressionFunctionInfo.with(
                                Url.parseAbsolute("https://example.com/expression-function-1"),
                                FunctionExpressionName.with("ExpressionFunction1")
                        ),
                        ExpressionFunctionInfo.with(
                                Url.parseAbsolute("https://example.com/expression-function-2"),
                                FunctionExpressionName.with("ExpressionFunction2")
                        )
                );
            }

            @Override
            public Set<SpreadsheetParserInfo> spreadsheetParserInfos() {
                return Sets.of(
                        SpreadsheetParserInfo.with(
                                Url.parseAbsolute("https://example.com/parser-1"),
                                SpreadsheetParserName.with("parser-1")
                        )
                );
            }
        };
    }

    // column...........................................................................................................

    @Test
    public void testColumnNullClearFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaHateosResourceMappings.column(null, HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), HateosResourceHandlers.fake())
        );
    }

    @Test
    public void testColumnNullDeleteFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaHateosResourceMappings.column(HateosResourceHandlers.fake(), null, HateosResourceHandlers.fake(), HateosResourceHandlers.fake())
        );
    }

    @Test
    public void testColumnNullInsertAfterFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaHateosResourceMappings.column(HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), null, HateosResourceHandlers.fake())
        );
    }

    @Test
    public void testColumnNullInsertBeforeFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaHateosResourceMappings.column(HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), null)
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
                SpreadsheetDeltaHateosResourceMappings.column(
                        SpreadsheetDeltaHttps.clearColumns(engine, context),
                        SpreadsheetDeltaHttps.deleteColumns(engine, context),
                        SpreadsheetDeltaHttps.insertAfterColumns(engine, context),
                        SpreadsheetDeltaHttps.insertBeforeColumns(engine, context)
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
                        SpreadsheetDeltaHateosResourceMappings.column(
                                SpreadsheetDeltaHttps.clearColumns(engine, context),
                                SpreadsheetDeltaHttps.deleteColumns(engine, context),
                                SpreadsheetDeltaHttps.insertAfterColumns(engine, context),
                                SpreadsheetDeltaHttps.insertBeforeColumns(engine, context)
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
                () -> SpreadsheetDeltaHateosResourceMappings.row(null, HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), HateosResourceHandlers.fake())
        );
    }

    @Test
    public void testRowNullDeleteFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaHateosResourceMappings.row(HateosResourceHandlers.fake(), null, HateosResourceHandlers.fake(), HateosResourceHandlers.fake())
        );
    }

    @Test
    public void testRowNullInsertAfterFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaHateosResourceMappings.row(HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), null, HateosResourceHandlers.fake())
        );
    }

    @Test
    public void testRowNullInsertBeforeFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetDeltaHateosResourceMappings.row(HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), HateosResourceHandlers.fake(), null)
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
                SpreadsheetDeltaHateosResourceMappings.row(
                        SpreadsheetDeltaHttps.clearRows(engine, context),
                        SpreadsheetDeltaHttps.deleteRows(engine, context),
                        SpreadsheetDeltaHttps.insertAfterRows(engine, context),
                        SpreadsheetDeltaHttps.insertBeforeRows(engine, context)
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
                        SpreadsheetDeltaHateosResourceMappings.row(
                                SpreadsheetDeltaHttps.clearRows(engine, context),
                                SpreadsheetDeltaHttps.deleteRows(engine, context),
                                SpreadsheetDeltaHttps.insertAfterRows(engine, context),
                                SpreadsheetDeltaHttps.insertBeforeRows(engine, context)
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
        final Optional<HttpHandler> possible = HateosResourceMapping.router(
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
            possible.get()
                    .handle(
                            request,
                            response
                    );
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
        final Optional<HttpHandler> possible = HateosResourceMapping.router(
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
                .handle(
                        request,
                        response
                );
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

            @SuppressWarnings("UnnecessaryBoxing")
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
    public Class<SpreadsheetDeltaHateosResourceMappings> type() {
        return SpreadsheetDeltaHateosResourceMappings.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
