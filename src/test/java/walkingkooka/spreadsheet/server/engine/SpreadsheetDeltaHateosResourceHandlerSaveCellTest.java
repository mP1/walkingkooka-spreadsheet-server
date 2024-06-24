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

package walkingkooka.spreadsheet.server.engine;

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetErrorKind;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.expression.SpreadsheetFunctionName;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.text.TextNode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaHateosResourceHandlerSaveCellTest
        extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerSaveCell,
        SpreadsheetCellReference> {

    // handle...........................................................................................................

    @Test
    public void testHandleSaveCellMissingFails() {
        this.handleOneFails(this.id(),
                Optional.empty(),
                this.parameters(),
                IllegalArgumentException.class);
    }

    @Test
    public void testHandleSaveCell() {
        final SpreadsheetCell cell = this.cell();

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta saveCell(final SpreadsheetCell c,
                                                             final SpreadsheetEngineContext context) {
                                Objects.requireNonNull(context, "context");

                                checkEquals(cell, c, "cell");
                                checkNotEquals(null, context, "context");

                                return saved();
                            }
                        }
                ),
                this.id(),
                this.resource(),
                this.parameters(),
                Optional.of(
                        this.saved()
                )
        );
    }

    @Test
    public void testHandleSaveMultipleCellsFails() {
        final SpreadsheetCell cell = this.cell();
        final SpreadsheetCell z99 = SpreadsheetSelection.parseCell("Z99")
                .setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText("99")
                );

        this.handleOneFails(this.id(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(cell, z99)
                                )
                ),
                this.parameters(),
                IllegalArgumentException.class);
    }

    @Test
    public void testHandleSaveWithQuery() {
        final SpreadsheetCell unsaved1 = this.cell();

        final SpreadsheetCell saved1 = unsaved1.setFormattedValue(Optional.of(TextNode.text("FORMATTED1")));
        final SpreadsheetCell saved2 = this.cellOutsideWindow().setFormattedValue(Optional.of(TextNode.text("FORMATTED2")));

        final SpreadsheetViewportWindows window = this.window();
        final String query = "=true()";

        this.handleOneAndCheck(
                SpreadsheetDeltaHateosResourceHandlerSaveCell.with(
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta saveCell(final SpreadsheetCell cell,
                                                             final SpreadsheetEngineContext context) {
                                Objects.requireNonNull(context, "context");

                                checkEquals(SpreadsheetDeltaHateosResourceHandlerSaveCellTest.this.cell(), cell, "cell");
                                checkNotEquals(null, context, "context");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(Sets.of(saved1, saved2))
                                        .setWindow(window);
                            }

                            @Override
                            public Set<SpreadsheetCell> filterCells(final Set<SpreadsheetCell> cells,
                                                                    final String valueType,
                                                                    final Expression expression,
                                                                    final SpreadsheetEngineContext context) {
                                return cells;
                            }
                        },
                        new FakeSpreadsheetEngineContext() {
                            @Override
                            public SpreadsheetParserToken parseFormula(final TextCursor formula) {
                                final TextCursorSavePoint begin = formula.save();
                                formula.end();
                                final String text = begin.textBetween()
                                        .toString();
                                checkEquals(query, text);
                                return SpreadsheetParserToken.functionName(
                                        SpreadsheetFunctionName.with("true"),
                                        text
                                );
                            }

                            @Override
                            public Optional<Expression> toExpression(final SpreadsheetParserToken token) {
                                return Optional.of(
                                        Expression.value(true)
                                );
                            }
                        }
                ),
                this.id(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(unsaved1))
                                .setWindow(window)
                ),
                Maps.of(
                        SpreadsheetEngineHttps.QUERY,
                        Lists.of(query)
                ),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(saved1)
                                ).setMatchedCells(
                                        Sets.of(
                                                saved1.reference()
                                        )
                                )
                                .setWindow(window)
                )
        );
    }

    @Test
    public void testHandleSaveWithWindowFilter() {
        final SpreadsheetCell unsaved1 = this.cell();

        final SpreadsheetCell saved1 = unsaved1.setFormattedValue(Optional.of(TextNode.text("FORMATTED1")));
        final SpreadsheetCell saved2 = this.cellOutsideWindow().setFormattedValue(Optional.of(TextNode.text("FORMATTED2")));

        final SpreadsheetViewportWindows window = this.window();

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta saveCell(final SpreadsheetCell cell,
                                                             final SpreadsheetEngineContext context) {
                                Objects.requireNonNull(context, "context");

                                checkEquals(SpreadsheetDeltaHateosResourceHandlerSaveCellTest.this.cell(), cell, "cell");
                                checkNotEquals(null, context, "context");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(Sets.of(saved1, saved2))
                                        .setWindow(window);
                            }
                        }
                ),
                this.id(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(unsaved1))
                                .setWindow(window)
                ),
                this.parameters(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(Sets.of(saved1))
                                .setWindow(window)
                )
        );
    }

    // handleRange.................................................................................................

    @Test
    public void testHandleCollectionBatchSaves() {
        final SpreadsheetCell b2 = this.cell("B2", "1");
        final SpreadsheetCell c3 = this.cell("C3", "2");
        final SpreadsheetCellRangeReference range = SpreadsheetCellRangeReference.fromCells(Lists.of(b2.reference(), c3.reference()));

        final SpreadsheetCell d4 = this.cell("C4", "3");
        final SpreadsheetDelta result = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(b2, c3, d4)
                );

        this.handleRangeAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta fillCells(final Collection<SpreadsheetCell> cells,
                                                              final SpreadsheetCellRangeReference from,
                                                              final SpreadsheetCellRangeReference to,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(Sets.of(b2, c3), new LinkedHashSet<>(cells), "cells");
                                checkEquals(range, from, "from");
                                checkEquals(range, to, "to");

                                return result;
                            }
                        }),
                range.range(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(Sets.of(b2, c3))
                ),
                this.parameters(),
                Optional.of(
                        result
                )
        );
    }

    @Test
    public void testHandleCollectionBatchSavesFiltersWindow() {
        final SpreadsheetCell unsaved1 = this.cell("B2", "1");
        final SpreadsheetCell unsaved2 = this.cell("B3", "2");

        final SpreadsheetCellRangeReference range = SpreadsheetCellRangeReference.fromCells(Lists.of(unsaved1.reference(), unsaved2.reference()));

        final SpreadsheetCell saved1 = unsaved1.setFormattedValue(Optional.of(TextNode.text("FORMATTED1")));
        final SpreadsheetCell saved2 = unsaved2.setFormattedValue(Optional.of(TextNode.text("FORMATTED2")));
        final SpreadsheetCell saved3 = this.cellOutsideWindow().setFormattedValue(Optional.of(TextNode.text("FORMATTED3")));

        final SpreadsheetViewportWindows window = this.window();

        this.handleRangeAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta fillCells(final Collection<SpreadsheetCell> cells,
                                                              final SpreadsheetCellRangeReference from,
                                                              final SpreadsheetCellRangeReference to,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(Sets.of(unsaved1, unsaved2), new LinkedHashSet<>(cells), "cells");
                                checkEquals(range, from, "from");
                                checkEquals(range, to, "to");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(saved1, saved2, saved3)
                                        );
                            }
                        }),
                range.range(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(Sets.of(unsaved1, unsaved2))
                                .setWindow(window)
                ),
                this.parameters(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(saved1, saved2)
                                ).setWindow(window)
                )
        );
    }

    private final static double WIDTH = 50;
    private final static double HEIGHT = 20;

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.saveCell");
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerSaveCell createHandler(final SpreadsheetEngine engine,
                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerSaveCell.with(engine, context);
    }

    private SpreadsheetDeltaHateosResourceHandlerSaveCell createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerSaveCell.with(engine, this.engineContext());
    }

    @Override
    public SpreadsheetCellReference id() {
        return SpreadsheetSelection.A1;
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetSelection.parseCellRange("B2:D4")
                .range();
    }

    @Override
    public Optional<SpreadsheetDelta> resource() {
        final SpreadsheetCell cell = this.cell();
        return Optional.of(
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(cell)
                        )
        );
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.of(
                SpreadsheetDelta.EMPTY.setCells(
                        Sets.of(this.cell())
                )
        );
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    SpreadsheetEngineContext engineContext() {
        return SpreadsheetEngineContexts.fake();
    }

    private SpreadsheetDelta saved() {
        return SpreadsheetDelta.EMPTY
                .setCells(Sets.of(savedCell()))
                .setLabels(this.labels());
    }

    private SpreadsheetCell savedCell() {
        return this.cell()
                .setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText("1+2")
                                .setValue(
                                        Optional.of(
                                                SpreadsheetErrorKind.VALUE.setMessage("Error something")
                                        )
                                )
                );
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerSaveCell> type() {
        return Cast.to(SpreadsheetDeltaHateosResourceHandlerSaveCell.class);
    }
}
