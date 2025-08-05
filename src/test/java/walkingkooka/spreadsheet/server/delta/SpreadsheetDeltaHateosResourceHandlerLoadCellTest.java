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
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetCellFindQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetCellQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.expression.SpreadsheetFunctionName;
import walkingkooka.spreadsheet.formula.parser.SpreadsheetFormulaParserToken;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStore;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStore;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.viewport.AnchoredSpreadsheetSelection;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewport;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportAnchor;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportNavigation;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportRectangle;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportWindows;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.text.Length;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;
import walkingkooka.validation.ValidationValueTypeName;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetDeltaHateosResourceHandlerLoadCellTest
    extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerLoadCell,
    SpreadsheetCellReference> {

    private final static SpreadsheetEngineEvaluation EVALUATION = SpreadsheetEngineEvaluation.FORCE_RECOMPUTE;

    @Test
    public void testWithNullEvaluationFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                null
            )
        );
    }

    // handle...........................................................................................................

    @Test
    public void testHandleOneLoadCell() {
        this.handleOneAndCheck(
            this.id(),
            this.resource(),
            this.parameters(),
            this.path(),
            this.context(),
            Optional.of(this.spreadsheetDelta())
        );
    }

    @Test
    public void testHandleOneLoadCellWithDeltaPropertiesCells() {
        this.handleOneLoadCellAndCheck(
            "cells",
            null, // window
            null, // query
            METADATA,
            SpreadsheetDelta.EMPTY
                .setCells(this.cells())
        );
    }

    @Test
    public void testHandleOneLoadCellWithDeltaPropertiesCellsAndLabels() {
        this.handleOneLoadCellAndCheck(
            "cells,labels",
            null, // window
            null, // query
            METADATA,
            SpreadsheetDelta.EMPTY
                .setCells(this.cells())
                .setLabels(this.labels())
        );
    }

    @Test
    public void testHandleOneLoadCellWithDeltaPropertiesCellsAndQuery() {
        this.handleOneLoadCellAndCheck(
            "cells",
            null, // window
            "true()", // query
            METADATA,
            SpreadsheetDelta.EMPTY
                .setCells(this.cells())
                .setMatchedCells(
                    cells().stream()
                        .map(SpreadsheetCell::reference)
                        .collect(Collectors.toSet())
                )
        );
    }

    @Test
    public void testHandleOneLoadCellWithHighlightingAndFindQuery() {
        this.handleOneLoadCellAndCheck(
            "cells",
            null, // window
            null, // query
            METADATA.set(
                SpreadsheetMetadataPropertyName.FIND_HIGHLIGHTING,
                true
            ).set(
                SpreadsheetMetadataPropertyName.FIND_QUERY,
                SpreadsheetCellQuery.parse("true()")
            ),
            SpreadsheetDelta.EMPTY
                .setCells(this.cells())
                .setMatchedCells(
                    cells().stream()
                        .map(SpreadsheetCell::reference)
                        .collect(Collectors.toSet())
                )
        );
    }

    private void handleOneLoadCellAndCheck(final String deltaProperties,
                                           final String window,
                                           final String query,
                                           final SpreadsheetMetadata metadata,
                                           final SpreadsheetDelta expected) {
        final SpreadsheetCellReference id = this.id();

        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        if (null != deltaProperties) {
            parameters.put(
                SpreadsheetDeltaProperties.PROPERTIES,
                Lists.of(deltaProperties)
            );
        }
        if (null != window) {
            parameters.put(SpreadsheetDeltaUrlQueryParameters.WINDOW, Lists.of(window));
        }
        if (null != query) {
            parameters.put(SpreadsheetCellFindQuery.QUERY, Lists.of(query));
        }

        this.handleOneAndCheck(
            SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                EVALUATION
            ),
            id,
            Optional.empty(),
            parameters,
            UrlPath.EMPTY,
            new TestSpreadsheetEngineHateosResourceHandlerContext() {

                @Override
                public SpreadsheetEngine spreadsheetEngine() {
                    return new FakeSpreadsheetEngine() {
                        @Override
                        public SpreadsheetDelta loadCells(final SpreadsheetSelection selection,
                                                          final SpreadsheetEngineEvaluation evaluation,
                                                          final Set<SpreadsheetDeltaProperties> dp,
                                                          final SpreadsheetEngineContext context) {
                            assertSame(EVALUATION, evaluation, "evaluation");
                            checkEquals(
                                SpreadsheetDeltaProperties.parse(deltaProperties),
                                dp,
                                "deltaProperties"
                            );
                            assertNotNull(context, "context");

                            SpreadsheetDelta result = SpreadsheetDelta.EMPTY;
                            if (dp.contains(SpreadsheetDeltaProperties.CELLS)) {
                                result = result.setCells(cells());
                            }
                            if (dp.contains(SpreadsheetDeltaProperties.LABELS)) {
                                result = result.setLabels(labels());
                            }

                            return result;
                        }

                        @Override
                        public Set<SpreadsheetCell> filterCells(final Set<SpreadsheetCell> cells,
                                                                final ValidationValueTypeName valueType,
                                                                final Expression expression,
                                                                final SpreadsheetEngineContext context) {
                            return cells;
                        }
                    };
                }

                @Override
                public SpreadsheetFormulaParserToken parseFormula(final TextCursor formula,
                                                                  final Optional<SpreadsheetCell> cell) {
                    final TextCursorSavePoint begin = formula.save();
                    formula.end();
                    final String text = begin.textBetween()
                        .toString();
                    checkEquals("true()", text, "formula text");

                    return SpreadsheetFormulaParserToken.functionName(
                        SpreadsheetFunctionName.with("true"),
                        text
                    );
                }

                @Override
                public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken token) {
                    return Optional.of(
                        Expression.value(true)
                    );
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return metadata;
                }
            },
            Optional.of(expected)
        );
    }

    // handleRange......................................................................................................

    @Test
    public void testHandleRangeLoadCellRange() {
        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetDeltaHateosResourceHandlerLoadCell handler = SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
            EVALUATION
        );
        this.handleRangeAndCheck(
            handler,
            this.range(),
            this.collectionResource(),
            this.parameters(),
            this.path(),
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> range,
                                                                   final SpreadsheetEngineEvaluation evaluation,
                                                                   final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                                   final SpreadsheetEngineContext context) {
                        assertSame(EVALUATION, evaluation, "evaluation");
                        assertNotNull(context, "context");

                        return SpreadsheetDelta.EMPTY.setCells(Sets.of(b1, b2, b3));
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public String toString() {
                        return "loadCells";
                    }
                }
            ),
            Optional.of(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        b1,
                        b2,
                        b3
                    )
                )
            )
        );
    }

    @Test
    public void testHandleRangeLoadCellRangeWithQuery() {
        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetDeltaHateosResourceHandlerLoadCell handler = SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
            EVALUATION
        );
        this.handleRangeAndCheck(
            handler,
            this.range(),
            this.collectionResource(),
            Maps.of(
                SpreadsheetCellQuery.QUERY,
                Lists.of(
                    "query=true()"
                )
            ),
            UrlPath.EMPTY,
            new TestSpreadsheetEngineHateosResourceHandlerContext() {

                @Override
                public SpreadsheetEngine spreadsheetEngine() {
                    return new FakeSpreadsheetEngine() {

                        @Override
                        public SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> range,
                                                                       final SpreadsheetEngineEvaluation evaluation,
                                                                       final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                                       final SpreadsheetEngineContext context) {
                            assertSame(EVALUATION, evaluation, "evaluation");
                            assertNotNull(context, "context");

                            return SpreadsheetDelta.EMPTY.setCells(
                                Sets.of(
                                    b1,
                                    b2,
                                    b3
                                )
                            );
                        }

                        @Override
                        public Set<SpreadsheetCell> filterCells(final Set<SpreadsheetCell> cells,
                                                                final ValidationValueTypeName valueType,
                                                                final Expression expression,
                                                                final SpreadsheetEngineContext context) {
                            return cells.stream()
                                .filter(b1::equals)
                                .collect(Collectors.toSet());
                        }

                        @Override
                        public double columnWidth(final SpreadsheetColumnReference column,
                                                  final SpreadsheetEngineContext context) {
                            return 0;
                        }

                        @Override
                        public double rowHeight(final SpreadsheetRowReference row,
                                                final SpreadsheetEngineContext context) {
                            return 0;
                        }

                        @Override
                        public String toString() {
                            return "loadCells";
                        }
                    };
                }

                @Override
                public SpreadsheetFormulaParserToken parseFormula(final TextCursor formula,
                                                                  final Optional<SpreadsheetCell> cell) {
                    final TextCursorSavePoint begin = formula.save();
                    formula.end();
                    final String text = begin.textBetween()
                        .toString();
                    return SpreadsheetFormulaParserToken.functionName(
                        SpreadsheetFunctionName.with("true"),
                        text
                    );
                }

                @Override
                public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken token) {
                    return Optional.of(
                        Expression.value(true)
                    );
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return METADATA;
                }
            },
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            b1, b2, b3
                        )
                    ).setMatchedCells(
                        Sets.of(
                            b1.reference()
                        )
                    )
            )
        );
    }

    @Test
    public void testHandleRangeLoadCellRangeWithWindowParameter() {
        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetCellRangeReference window = b1.reference()
            .toCellRange();

        final SpreadsheetDeltaHateosResourceHandlerLoadCell handler = SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
            EVALUATION
        );
        this.handleRangeAndCheck(
            handler,
            this.range(),
            this.collectionResource(),
            Maps.of(
                SpreadsheetDeltaUrlQueryParameters.WINDOW,
                List.of(
                    window.toString()
                )
            ),
            UrlPath.EMPTY,
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> range,
                                                                   final SpreadsheetEngineEvaluation evaluation,
                                                                   final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                                   final SpreadsheetEngineContext context) {
                        assertSame(EVALUATION, evaluation, "evaluation");
                        checkEquals(SpreadsheetDeltaProperties.ALL, deltaProperties, "deltaProperties");
                        assertNotNull(context, "context");

                        return SpreadsheetDelta.EMPTY.setCells(
                            Sets.of(b1, b2, b3)
                        );
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public String toString() {
                        return "loadCells";
                    }
                }
            ),
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(b1, b2)
                    ).setWindow(
                        SpreadsheetViewportWindows.with(
                            Sets.of(window)
                        )
                    )
            )
        );
    }

    // handleAll........................................................................................................

    @Test
    public void testLoadCellRangeMissingWindowParametersFails() {
        this.loadCellRangeFails(
            Maps.empty(),
            "Missing: home, width, height, includeFrozenColumnsRows");
    }

    @Test
    public void testLoadCellRangeInvalidHomeParameterFails() {
        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();

        parameters.put(SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of("!Invalid"));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("123"));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("456"));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));

        this.loadCellRangeFails(
            parameters,
            "Invalid home=\"!Invalid\""
        );
    }

    private void loadCellRangeFails(final Map<HttpRequestAttribute<?>, Object> parameters,
                                    final String message) {
        final IllegalArgumentException thrown = this.handleAllFails(
            Optional.empty(),
            parameters,
            UrlPath.EMPTY,
            this.context(),
            IllegalArgumentException.class
        );
        this.checkEquals(
            message,
            thrown.getMessage(),
            "message"
        );
    }

    @Test
    public void testLoadCellRangeFilteredNone() {
        this.loadCellRangeFilteredAndCheck(
            null,
            null,
            null,
            null,
            null
        );
    }

    @Test
    public void testLoadCellRangeFilteredCell() {
        this.loadCellRangeFilteredAndCheck(
            "cell",
            "A9",
            null,
            null, // deltaProperties
            SpreadsheetSelection.parseCell("A9")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeFilteredCellRange() {
        this.loadCellRangeFilteredAndCheck(
            "cell-range",
            "A9:A99",
            SpreadsheetViewportAnchor.TOP_LEFT.kebabText(),
            null, // deltaProperties
            SpreadsheetSelection.parseCellRange("A9:A99")
                .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testLoadCellRangeFilteredColumn() {
        this.loadCellRangeFilteredAndCheck(
            "column",
            "B",
            null, // anchor
            null, // deltaProperties
            SpreadsheetSelection.parseColumn("B")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeFilteredColumnRange() {
        this.loadCellRangeFilteredAndCheck(
            "column-range",
            "B:D",
            SpreadsheetViewportAnchor.LEFT.kebabText(),
            null, // deltaProperties
            SpreadsheetSelection.parseColumnRange("B:D")
                .setAnchor(SpreadsheetViewportAnchor.LEFT)
        );
    }

    @Test
    public void testLoadCellRangeFilteredRow() {
        this.loadCellRangeFilteredAndCheck(
            "row",
            "99",
            null, // anchor
            null, // deltaProperties
            SpreadsheetSelection.parseRow("99")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeFilteredrowRange() {
        this.loadCellRangeFilteredAndCheck(
            "row-range",
            "98:99",
            SpreadsheetViewportAnchor.TOP.kebabText(),
            null, // deltaProperties
            SpreadsheetSelection.parseRowRange("98:99")
                .setAnchor(SpreadsheetViewportAnchor.TOP)
        );
    }

    @Test
    public void testLoadCellRangeFilteredCells() {
        this.loadCellRangeFilteredAndCheck(
            null,
            null,
            null,
            "cells",
            null
        );
    }

    @Test
    public void testLoadCellRangeFilteredCellsAndLabels() {
        this.loadCellRangeFilteredAndCheck(
            null,
            null,
            null,
            "cells,labels",
            null
        );
    }

    private void loadCellRangeFilteredAndCheck(final String selectionType,
                                               final String selectionText,
                                               final String anchor,
                                               final String deltaProperties,
                                               final AnchoredSpreadsheetSelection expected) {
        // B1, B2, B3
        // C1, C2, C3

        final SpreadsheetCell b1 = this.b1();
        final SpreadsheetCell b2 = this.b2();
        final SpreadsheetCell b3 = this.b3();

        final SpreadsheetCell c1 = this.c1();
        final SpreadsheetCell c2 = this.c2();
        final SpreadsheetCell c3 = this.c3();

        final Range<SpreadsheetCellReference> range = this.range();

        final SpreadsheetCellReference home = SpreadsheetSelection.parseCell("B2");
        final int width = 33;
        final int height = 44;


        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        parameters.put(SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of(home.toString()));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("" + width));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("" + height));

        if (null != selectionType) {
            parameters.put(SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of(selectionType));
            parameters.put(SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of(selectionText));

            if (null != anchor) {
                parameters.put(SpreadsheetDeltaUrlQueryParameters.SELECTION_ANCHOR, Lists.of(anchor));
            }
        }

        parameters.put(SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));
        parameters.put(
            SpreadsheetDeltaProperties.PROPERTIES,
            Lists.of(deltaProperties)
        );

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataTesting.spreadsheetMetadataStore();
        metadataStore.save(METADATA_EN_AU);

        this.handleAllAndCheck(
            SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                EVALUATION
            ),
            Optional.empty(),
            parameters,
            UrlPath.EMPTY,
            new FakeSpreadsheetEngineHateosResourceHandlerContext() {

                @Override
                public SpreadsheetEngine spreadsheetEngine() {
                    return new FakeSpreadsheetEngine() {

                        @Override
                        public SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> r,
                                                                       final SpreadsheetEngineEvaluation evaluation,
                                                                       final Set<SpreadsheetDeltaProperties> dp,
                                                                       final SpreadsheetEngineContext context) {
                            checkEquals(
                                Sets.of(
                                    SpreadsheetSelection.cellRange(range)
                                ),
                                r,
                                "range"
                            );
                            checkEquals(EVALUATION, evaluation, "evaluation");
                            checkEquals(
                                SpreadsheetDeltaProperties.parse(deltaProperties),
                                dp,
                                "deltaProperties"
                            );

                            return SpreadsheetDelta.EMPTY
                                .setCells(
                                    Sets.of(
                                        b1, b2, b3, c1, c2, c3
                                    )
                                );
                        }

                        @Override
                        public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                                 final boolean includeFrozenColumnsRows,
                                                                 final Optional<SpreadsheetSelection> s,
                                                                 final SpreadsheetEngineContext context) {
                            checkEquals(
                                SpreadsheetViewportRectangle.with(
                                    home,
                                    width,
                                    height
                                ),
                                viewportRectangle,
                                "viewport"
                            );
                            return SpreadsheetViewportWindows.with(
                                Sets.of(
                                    SpreadsheetSelection.cellRange(range)
                                )
                            );
                        }

                        @Override
                        public double columnWidth(final SpreadsheetColumnReference column,
                                                  final SpreadsheetEngineContext context) {
                            return COLUMN_WIDTH;
                        }

                        @Override
                        public double rowHeight(final SpreadsheetRowReference row,
                                                final SpreadsheetEngineContext context) {
                            return ROW_HEIGHT;
                        }

                        @Override
                        public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                                      final SpreadsheetEngineContext context) {
                            return Optional.of(viewport);
                        }
                    };
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return METADATA_EN_AU;
                }

                @Override
                public SpreadsheetStoreRepository storeRepository() {
                    return new FakeSpreadsheetStoreRepository() {
                        @Override
                        public SpreadsheetCellStore cells() {
                            return SpreadsheetCellStores.fake();
                        }

                        @Override
                        public SpreadsheetMetadataStore metadatas() {
                            return metadataStore;
                        }
                    };
                }

                @Override
                public Optional<SpreadsheetSelection> resolveIfLabel(final SpreadsheetSelection selection) {
                    return Optional.of(selection);
                }
            },
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setCells(Sets.of(b1, b2, b3, c1, c2, c3))
                    .setWindow(
                        SpreadsheetViewportWindows.parse("B1:C3")
                    ).setViewport(
                        Optional.of(
                            home.viewportRectangle(width, height)
                                .viewport()
                                .setAnchoredSelection(
                                    Optional.ofNullable(expected)
                                )
                        )
                    )
            )
        );
    }

    @Test
    public void testLoadCellRangeNoFrozenColumnRows() {
        this.loadCellRangeFilteredAndCheck(
            "A1",
            0, // frozenColumns
            0, // frozenRows
            "A1:D4", // range
            "A1:D4" // window
        );
    }

    //   A B C D
    // 1 C v v v
    // 2 C v v v
    // 3 C v v v
    // 4 C v v v
    @Test
    public void testLoadCellRange1FrozenColumn() {
        this.loadCellRangeFilteredAndCheck(
            "A1",
            1, // frozenColumns
            0, // frozenRows
            "A1:A4,B1:D4", // range
            "A1:A4,B1:D4" // window
        );
    }

    private void loadCellRangeFilteredAndCheck(final String home,
                                               final int frozenColumns,
                                               final int frozenRows,
                                               final String range,
                                               final String window) {

        final SpreadsheetViewportWindows spreadsheetViewportWindows = SpreadsheetViewportWindows.parse(window);
        final SpreadsheetCellRangeReference viewportWindowRange = spreadsheetViewportWindows.last()
            .get();

        final double viewportWidth = viewportWindowRange.width() * COLUMN_WIDTH;
        final double viewportHeight = viewportWindowRange.height() * ROW_HEIGHT;

        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        parameters.put(SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of(home));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("" + viewportWidth));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("" + viewportHeight));

        parameters.put(SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("true"));

        this.handleAllAndCheck(
            SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                EVALUATION
            ),
            Optional.empty(),
            parameters,
            UrlPath.EMPTY,
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> r,
                                                                   final SpreadsheetEngineEvaluation evaluation,
                                                                   final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                                   final SpreadsheetEngineContext context) {
                        checkEquals(EVALUATION, evaluation, "evaluation");

                        return SpreadsheetDelta.EMPTY;
                    }

                    @Override
                    public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                             final boolean includeFrozenColumnsRows,
                                                             final Optional<SpreadsheetSelection> selection,
                                                             final SpreadsheetEngineContext context) {
                        if (range.equals("throw")) {
                            throw new UnsupportedOperationException();
                        }
                        checkEquals(SpreadsheetDelta.NO_VIEWPORT, selection);

                        return SpreadsheetViewportWindows.parse(range);
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return COLUMN_WIDTH;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        return ROW_HEIGHT;
                    }

                    @Override
                    public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport viewport,
                                                                  final SpreadsheetEngineContext context) {
                        return Optional.of(viewport);
                    }
                }
            ),
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setViewport(
                        Optional.of(
                            SpreadsheetSelection.parseCell(home)
                                .viewportRectangle(
                                    viewportWidth,
                                    viewportHeight
                                ).viewport()
                        )
                    )
                    .setWindow(
                        spreadsheetViewportWindows
                    )
            )
        );
    }

    // load cells navigation...........................................................................................

    @Test
    public void testLoadCellRangeNavigateCellLeft() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseCell("B2")
                .setDefaultAnchor(),
            Lists.of(
                SpreadsheetViewportNavigation.leftColumn()
            ),
            SpreadsheetSelection.parseCell("A2")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRight() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseCell("B2")
                .setDefaultAnchor(),
            Lists.of(
                SpreadsheetViewportNavigation.rightColumn()
            ),
            SpreadsheetSelection.parseCell("C2")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeNavigateColumnLeft() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseColumn("C")
                .setDefaultAnchor(),
            Lists.of(
                SpreadsheetViewportNavigation.leftColumn()
            ),
            SpreadsheetSelection.parseColumn("B")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeNavigateColumnDown() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseColumn("C")
                .setDefaultAnchor(),
            Lists.of(
                SpreadsheetViewportNavigation.downRow()
            ),
            SpreadsheetSelection.parseColumn("C")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeNavigateRowDown() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseRow("3")
                .setDefaultAnchor(),
            Lists.of(
                SpreadsheetViewportNavigation.downRow()
            ),
            SpreadsheetSelection.parseRow("4")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellExtendRight() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseCell("B2")
                .setDefaultAnchor(),
            Lists.of(
                SpreadsheetViewportNavigation.extendRightColumn()
            ),
            SpreadsheetSelection.parseCellRange("B2:C2")
                .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellExtendLeft() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseCell("B2")
                .setDefaultAnchor(),
            Lists.of(
                SpreadsheetViewportNavigation.extendLeftColumn()
            ),
            SpreadsheetSelection.parseCellRange("A2:B2")
                .setAnchor(SpreadsheetViewportAnchor.BOTTOM_RIGHT)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRangeExtendRight() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseCellRange("A2:B2")
                .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT),
            Lists.of(
                SpreadsheetViewportNavigation.extendRightColumn()
            ),
            SpreadsheetSelection.parseCellRange("A2:C2")
                .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRangeExtendRightBecomesCell() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseCellRange("B2:C2")
                .setAnchor(SpreadsheetViewportAnchor.TOP_RIGHT),
            Lists.of(
                SpreadsheetViewportNavigation.extendRightColumn()
            ),
            SpreadsheetSelection.parseCell("C2")
                .setDefaultAnchor()
        );
    }

    @Test
    public void testLoadCellRangeNavigateCellRangeExtendUpBecomesCell() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            SpreadsheetSelection.parseCellRange("C1:C2")
                .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT),
            Lists.of(
                SpreadsheetViewportNavigation.extendUpRow()
            ),
            SpreadsheetSelection.parseCell("C1")
                .setDefaultAnchor()
        );
    }

    private void loadCellRangeNavigateAndCheck(final String home,
                                               final AnchoredSpreadsheetSelection anchoredSpreadsheetSelection,
                                               final List<SpreadsheetViewportNavigation> navigations,
                                               final AnchoredSpreadsheetSelection expected) {
        this.loadCellRangeNavigateAndCheck(
            home,
            Optional.of(
                anchoredSpreadsheetSelection
            ),
            navigations,
            "A1:E5",
            home, // expected home
            Optional.of(expected) // expected selection
        );
    }

    @Test
    public void testLoadCellRangeNavigationRightPixelsWithSelection() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            Optional.of(
                SpreadsheetSelection.parseCellRange("C1:C2")
                    .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
            ),
            Lists.of(
                SpreadsheetViewportNavigation.extendRightPixel(401)
            ),
            "F1:J5", // window
            "F1", // expectedHome
            Optional.of(
                SpreadsheetSelection.parseCellRange("C1:H2")
                    .setAnchor(SpreadsheetViewportAnchor.TOP_LEFT)
            )// expected selection
        );
    }

    @Test
    public void testLoadCellRangeNavigationRightPixels() {
        this.loadCellRangeNavigateAndCheck(
            "A1",
            Optional.empty(),
            Lists.of(
                SpreadsheetViewportNavigation.extendRightPixel(401)
            ),
            "F1:J5", // window
            "F1", // expectedHome
            Optional.empty()// expected selection
        );
    }

    private void loadCellRangeNavigateAndCheck(final String home,
                                               final Optional<AnchoredSpreadsheetSelection> anchoredSpreadsheetSelection,
                                               final List<SpreadsheetViewportNavigation> navigations,
                                               final String window,
                                               final String expectedHome,
                                               final Optional<AnchoredSpreadsheetSelection> expectedAnchoredSelection) {
        final int width = 400;
        final int height = 150;

        final Map<HttpRequestAttribute<?>, Object> parameters = Maps.sorted();
        parameters.put(SpreadsheetDeltaUrlQueryParameters.HOME, Lists.of(home));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.WIDTH, Lists.of("" + width)); // 4x3
        parameters.put(SpreadsheetDeltaUrlQueryParameters.HEIGHT, Lists.of("" + height));
        parameters.put(SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS, Lists.of("false"));

        if (anchoredSpreadsheetSelection.isPresent()) {
            final SpreadsheetSelection selection = anchoredSpreadsheetSelection.get()
                .selection();
            parameters.put(SpreadsheetDeltaUrlQueryParameters.SELECTION, Lists.of(selection.toString()));
            parameters.put(SpreadsheetDeltaUrlQueryParameters.SELECTION_TYPE, Lists.of(selection.selectionTypeName()));
            parameters.put(
                SpreadsheetDeltaUrlQueryParameters.SELECTION_ANCHOR,
                Lists.of(
                    anchoredSpreadsheetSelection.get()
                        .anchor()
                        .kebabText()
                )
            );

        }
        parameters.put(
            SpreadsheetDeltaUrlQueryParameters.NAVIGATION,
            Lists.of(
                SpreadsheetViewport.SEPARATOR.toSeparatedString(
                    navigations,
                    SpreadsheetViewportNavigation::text
                )
            )
        );

        final SpreadsheetViewportWindows spreadsheetViewportWindows = SpreadsheetViewportWindows.parse(window);

        final SpreadsheetMetadata metadata = METADATA_EN_AU.set(
            SpreadsheetMetadataPropertyName.STYLE,
            TextStyle.EMPTY
                .set(
                    TextStylePropertyName.WIDTH,
                    Length.pixel(COLUMN_WIDTH
                    )
                )
                .set(
                    TextStylePropertyName.HEIGHT,
                    Length.pixel(ROW_HEIGHT)
                )
        );

        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataTesting.spreadsheetMetadataStore();
        metadataStore.save(metadata);

        this.handleAllAndCheck(
            SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
                EVALUATION
            ),
            Optional.empty(),
            parameters,
            UrlPath.EMPTY,
            new FakeSpreadsheetEngineHateosResourceHandlerContext() {

                @Override
                public SpreadsheetEngine spreadsheetEngine() {
                    return new FakeSpreadsheetEngine() {

                        @Override
                        public SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> r,
                                                                       final SpreadsheetEngineEvaluation evaluation,
                                                                       final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                                       final SpreadsheetEngineContext context) {
                            checkEquals(EVALUATION, evaluation, "evaluation");

                            return SpreadsheetDelta.EMPTY;
                        }

                        @Override
                        public SpreadsheetViewportWindows window(final SpreadsheetViewportRectangle viewportRectangle,
                                                                 final boolean includeFrozenColumnsRows,
                                                                 final Optional<SpreadsheetSelection> selection,
                                                                 final SpreadsheetEngineContext context) {
                            return spreadsheetViewportWindows;
                        }

                        @Override
                        public double columnWidth(final SpreadsheetColumnReference column,
                                                  final SpreadsheetEngineContext context) {
                            return 100;
                        }

                        @Override
                        public double rowHeight(final SpreadsheetRowReference row,
                                                final SpreadsheetEngineContext context) {
                            return 50;
                        }

                        @Override
                        public Optional<SpreadsheetViewport> navigate(final SpreadsheetViewport s,
                                                                      final SpreadsheetEngineContext context) {
                            return SpreadsheetEngines.basic()
                                .navigate(s, context);
                        }
                    };
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return metadata;
                }

                @Override
                public SpreadsheetStoreRepository storeRepository() {
                    return new FakeSpreadsheetStoreRepository() {
                        @Override
                        public SpreadsheetCellStore cells() {
                            return this.cells;
                        }

                        private final SpreadsheetCellStore cells = SpreadsheetCellStores.treeMap();

                        @Override
                        public SpreadsheetColumnStore columns() {
                            return this.columns;
                        }

                        private final SpreadsheetColumnStore columns = SpreadsheetColumnStores.treeMap();

                        @Override
                        public SpreadsheetMetadataStore metadatas() {
                            return metadataStore;
                        }

                        @Override
                        public SpreadsheetRowStore rows() {
                            return this.rows;
                        }

                        private final SpreadsheetRowStore rows = SpreadsheetRowStores.treeMap();
                    };
                }

                @Override
                public Optional<SpreadsheetSelection> resolveIfLabel(final SpreadsheetSelection selection) {
                    if (selection.isLabelName()) {
                        throw new UnsupportedOperationException("Labels like " + selection + " are not supported in this test");
                    }
                    return Optional.of(selection);
                }
            },
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setViewport(
                        Optional.of(
                            SpreadsheetSelection.parseCell(expectedHome)
                                .viewportRectangle(
                                    width,
                                    height
                                ).viewport()
                                .setAnchoredSelection(expectedAnchoredSelection)
                        )
                    ).setWindow(spreadsheetViewportWindows)
            )
        );
    }

    // helpers..........................................................................................................

    private SpreadsheetCell b1() {
        return this.cell("B1", "1");
    }

    private SpreadsheetCell b2() {
        return this.cell("B2", "2");
    }

    private SpreadsheetCell b3() {
        return this.cell("B3", "3");
    }

    private SpreadsheetCell c1() {
        return this.cell("c1", "4");
    }

    private SpreadsheetCell c2() {
        return this.cell("c2", "5");
    }

    private SpreadsheetCell c3() {
        return this.cell("c3", "6");
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.loadCell " + EVALUATION);
    }

    @Override
    public SpreadsheetDeltaHateosResourceHandlerLoadCell createHandler() {
        return SpreadsheetDeltaHateosResourceHandlerLoadCell.with(
            EVALUATION
        );
    }

    @Override
    public SpreadsheetCellReference id() {
        return this.spreadsheetCellReference();
    }

    private SpreadsheetCellReference spreadsheetCellReference() {
        return SpreadsheetSelection.parseCell("B2");
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetCellRangeReference.parseCellRange("B1:C3").range();
    }

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosResourceHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        final SpreadsheetMetadataStore store = SpreadsheetMetadataTesting.spreadsheetMetadataStore();
        store.save(METADATA);

        return new FakeSpreadsheetEngineHateosResourceHandlerContext() {

            @Override
            public SpreadsheetEngine spreadsheetEngine() {
                return new FakeSpreadsheetEngine() {
                    @Override
                    public SpreadsheetDelta loadCells(final SpreadsheetSelection selection,
                                                      final SpreadsheetEngineEvaluation evaluation,
                                                      final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                      final SpreadsheetEngineContext context) {
                        Objects.requireNonNull(selection, "selection");
                        Objects.requireNonNull(evaluation, "evaluation");
                        Objects.requireNonNull(deltaProperties, "deltaProperties");
                        Objects.requireNonNull(context, "context");

                        checkEquals(
                            SpreadsheetDeltaHateosResourceHandlerLoadCellTest.this.spreadsheetCellReference(),
                            selection,
                            "selection"
                        );
                        checkEquals(EVALUATION, evaluation, "evaluation");
                        checkNotEquals(null, context, "context");

                        return spreadsheetDelta();
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        return 0;
                    }
                };
            }

            @Override
            public SpreadsheetMetadata spreadsheetMetadata() {
                return METADATA_EN_AU;
            }

            @Override
            public SpreadsheetStoreRepository storeRepository() {
                return new FakeSpreadsheetStoreRepository() {
                    @Override
                    public SpreadsheetMetadataStore metadatas() {
                        return store;
                    }
                };
            }
        };
    }

    private SpreadsheetDelta spreadsheetDelta() {
        return SpreadsheetDelta.EMPTY.setCells(Sets.of(this.cell()));
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerLoadCell> type() {
        return SpreadsheetDeltaHateosResourceHandlerLoadCell.class;
    }
}
