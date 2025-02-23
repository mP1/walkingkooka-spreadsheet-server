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
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetExpressionFunctionNames;
import walkingkooka.spreadsheet.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.compare.SpreadsheetColumnOrRowSpreadsheetComparatorNames;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetDeltaHttpMappingsTest implements ClassTesting2<SpreadsheetDeltaHttpMappings>,
    SpreadsheetMetadataTesting {

    private final static MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;

    private final static AbsoluteUrl URL = Url.parseAbsolute("https://example.com/");
    private final static Indentation INDENTATION = Indentation.SPACES2;
    private final static LineEnding LINE_ENDING = LineEnding.NL;

    private final static int DEFAULT_MAX = 999;

    // cell.............................................................................................................

    @Test
    public void testCellWithNullEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetDeltaHttpMappings.cell(
                null,
                DEFAULT_MAX
            )
        );
    }

    @Test
    public void testCellWithInvalidDefaultMaxFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> SpreadsheetDeltaHttpMappings.cell(
                SpreadsheetEngines.fake(),
                -1 // defaultMax
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
            "Label not found: \"UnknownLabel123\""
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
    public void testRouteCellPostSaveCellWithEmptyBody() {
        this.routeCellFails(
            HttpMethod.POST,
            "/cell/A1",
            "",
            IllegalArgumentException.class,
            "Missing resource"
        );
    }

    @Test
    public void testRouteCellPostSaveCellLabelWithEmptyBody() {
        this.routeCellFails(
            HttpMethod.POST,
            "/cell/Label123",
            "",
            IllegalArgumentException.class,
            "Missing resource"
        );
    }

    @Test
    public void testRouteCellPostSaveCellUnknownLabelFails() {
        this.routeCellAndCheck(
            HttpMethod.POST,
            "/cell/UnknownLabel456",
            HttpStatusCode.BAD_REQUEST,
            "Label not found: \"UnknownLabel456\""
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
            "Label not found: \"UnknownLabel789\""
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
        this.routeCellAndCheck(
            method,
            url,
            statusCode,
            null
        );
    }

    private void routeCellAndCheck(final HttpMethod method,
                                   final String url,
                                   final HttpStatusCode statusCode,
                                   final String message) {
        this.routeCellAndCheck(
            method,
            url,
            "",
            statusCode,
            message
        );
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

        this.routeAndCheck(
            SpreadsheetDeltaHttpMappings.cell(
                engine,
                DEFAULT_MAX
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
        return this.route(
            SpreadsheetDeltaHttpMappings.cell(
                this.engine(),
                DEFAULT_MAX
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
            () -> this.routeCell(
                method,
                url,
                body
            )
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
        this.checkEquals(
            message,
            throwable.getMessage(),
            "message"
        );
    }

    private SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine() {

            @Override
            public SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> range,
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

    private SpreadsheetEngineHateosResourceHandlerContext context() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetLabelName label123 = SpreadsheetExpressionReference.labelName("Label123");

        final SpreadsheetLabelStore labelStore = SpreadsheetLabelStores.treeMap();
        labelStore.save(
            label123.setLabelMappingReference(a1)
        );

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataTesting.spreadsheetMetadataStore();
        metadataStore.save(METADATA_EN_AU);

        return new FakeSpreadsheetEngineHateosResourceHandlerContext() {

            @Override
            public MediaType contentType() {
                return CONTENT_TYPE;
            }

            @Override
            public SpreadsheetMetadata spreadsheetMetadata() {
                return METADATA_EN_AU;
            }

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetLabelStore labels() {
                        return labelStore;
                    }

                    @Override
                    public SpreadsheetMetadataStore metadatas() {
                        return metadataStore;
                    }
                };
            }

            // required by GET comparators

            @Override
            public SpreadsheetComparatorInfoSet spreadsheetComparatorInfos() {
                return SpreadsheetComparatorInfoSet.EMPTY.concat(
                    SpreadsheetComparatorInfo.with(
                        Url.parseAbsolute("https://example.com/comparator-1"),
                        SpreadsheetComparatorName.with("comparator-1")
                    )
                );
            }

            @Override
            public SpreadsheetFormatterInfoSet spreadsheetFormatterInfos() {
                return SpreadsheetFormatterInfoSet.EMPTY.concat(
                    SpreadsheetFormatterInfo.with(
                        Url.parseAbsolute("https://example.com/formatter-1"),
                        SpreadsheetFormatterName.with("formatter-1")
                    )
                );
            }

            @Override
            public ExpressionFunctionInfoSet expressionFunctionInfos() {
                return ExpressionFunctionInfoSet.with(
                    Sets.of(
                        ExpressionFunctionInfo.with(
                            Url.parseAbsolute("https://example.com/expression-function-1"),
                            ExpressionFunctionName.with("ExpressionFunction1")
                                .setCaseSensitivity(SpreadsheetExpressionFunctionNames.CASE_SENSITIVITY)
                        ),
                        ExpressionFunctionInfo.with(
                            Url.parseAbsolute("https://example.com/expression-function-2"),
                            ExpressionFunctionName.with("ExpressionFunction2")
                                .setCaseSensitivity(SpreadsheetExpressionFunctionNames.CASE_SENSITIVITY)
                        )
                    )
                );
            }

            @Override
            public SpreadsheetParserInfoSet spreadsheetParserInfos() {
                return SpreadsheetParserInfoSet.EMPTY.concat(
                    SpreadsheetParserInfo.with(
                        Url.parseAbsolute("https://example.com/parser-1"),
                        SpreadsheetParserName.with("parser-1")
                    )
                );
            }

            @Override
            public JsonNode marshall(final Object value) {
                return JsonNodeMarshallContexts.basic()
                    .marshall(value);
            }

            @Override
            public <T> T unmarshall(final JsonNode json,
                                    final Class<T> type) {
                return JsonNodeUnmarshallContexts.basic(
                    ExpressionNumberKind.DEFAULT,
                    MathContext.UNLIMITED
                ).unmarshall(
                    json,
                    type
                );
            }

            @Override
            public SpreadsheetSelection resolveLabel(final SpreadsheetLabelName spreadsheetLabelName) {
                return this.storeRepository()
                    .labels()
                    .resolveLabelOrFail(spreadsheetLabelName)
                    .toCell();
            }
        };
    }

    // column...........................................................................................................

    @Test
    public void testColumnWithNullEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetDeltaHttpMappings.column(
                null
            )
        );
    }

    @Test
    public void testRouteClearColumnsOnePost() {
        this.routeColumnAndCheck(
            HttpMethod.POST,
            "/column/A/clear",
            "",
            HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteClearColumnRangePost() {
        this.routeColumnAndCheck(
            HttpMethod.POST,
            "/column/A:B/clear",
            "",
            HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteColumnsInsertAfterPostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/insert-after", IllegalArgumentException.class);
    }

    @Test
    public void testRouteColumnRangeInsertAfterPostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/insert-after", IllegalArgumentException.class);
    }

    @Test
    public void testRouteColumnsInsertAfterPost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/insert-after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteColumnRangeInsertAfterPost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/insert-after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteColumnsInsertBeforePostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/insert-before", IllegalArgumentException.class);
    }

    @Test
    public void testRouteColumnRangeInsertBeforePostMissingCountFails() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/insert-before", IllegalArgumentException.class);
    }

    @Test
    public void testRouteColumnsInsertBeforePost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A/insert-before?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteColumnRangeInsertBeforePost() {
        this.routeColumnAndCheck(HttpMethod.POST, "/column/A:B/insert-before?count=1", UnsupportedOperationException.class);
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
        this.routeAndCheck(
            SpreadsheetDeltaHttpMappings.column(
                this.engine()
            ),
            method,
            url,
            body,
            statusCode
        );
    }

    private void routeColumnAndCheck(final HttpMethod method,
                                     final String url,
                                     final Class<? extends Throwable> thrown) {
        assertThrows(
            thrown,
            () -> this.route(
                SpreadsheetDeltaHttpMappings.column(
                    this.engine()
                ),
                method,
                url,
                ""
            )
        );
    }

    // row...........................................................................................................

    @Test
    public void testRowWithNullEngineFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetDeltaHttpMappings.row(
                null
            )
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
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/insert-after", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowRangeAfterPostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/insert-after", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowsAfterPost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/insert-after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowRangeAfterPost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/insert-after?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowsBeforePostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/insert-before", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowRangeBeforePostMissingCountFails() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/insert-before", IllegalArgumentException.class);
    }

    @Test
    public void testRouteRowsBeforePost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1/insert-before?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowRangeBeforePost() {
        this.routeRowAndCheck(HttpMethod.POST, "/row/1:2/insert-before?count=1", UnsupportedOperationException.class);
    }

    @Test
    public void testRouteRowsClearOnePost() {
        this.routeRowAndCheck(
            HttpMethod.POST,
            "/row/1/clear",
            "",
            HttpStatusCode.OK
        );
    }

    @Test
    public void testRouteRowRangeClearPost() {
        this.routeRowAndCheck(
            HttpMethod.POST,
            "/row/1:2/clear",
            "",
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
        this.routeAndCheck(
            SpreadsheetDeltaHttpMappings.row(
                this.engine()
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
        assertThrows(
            thrown,
            () -> this.route(
                SpreadsheetDeltaHttpMappings.row(
                    this.engine()
                ),
                method,
                url,
                ""
            )
        );
    }

    // helpers..........................................................................................................

    private void routeAndCheck(final HateosResourceMapping<?, ?, ?, ?, ?> mapping,
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

    private void routeAndCheck(final HateosResourceMapping<?, ?, ?, ?, ?> mapping,
                               final HttpMethod method,
                               final String url,
                               final String requestBody,
                               final HttpStatusCode statusCode,
                               final String message
    ) {
        final HttpRequest request = this.request(method, URL + url, requestBody);
        final Optional<HttpHandler> possible = HateosResourceMapping.router(
            URL,
            Sets.of(mapping),
            INDENTATION,
            LINE_ENDING,
            this.context()
        ).route(request.routerParameters());

        this.checkNotEquals(
            Optional.empty(),
            possible,
            () -> method + " " + URL + url
        );

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

    private HttpResponse route(final HateosResourceMapping<?, ?, ?, ?, ?> mapping,
                               final HttpMethod method,
                               final String url,
                               final String requestBody) {
        final HttpRequest request = this.request(method, URL + url, requestBody);
        final Optional<HttpHandler> possible = HateosResourceMapping.router(
            URL,
            Sets.of(mapping),
            INDENTATION,
            LINE_ENDING,
            this.context()
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
                return Maps.of(
                    HttpHeaderName.ACCEPT, Lists.of(CONTENT_TYPE.accept()),
                    HttpHeaderName.CONTENT_TYPE, Lists.of(CONTENT_TYPE.setCharset(CharsetName.UTF_8)),
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

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetDeltaHttpMappings> type() {
        return SpreadsheetDeltaHttpMappings.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
