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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetCellFindQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.expression.SpreadsheetFunctionName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReferencePath;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContexts;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionFunctionName;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetDeltaHateosResourceHandlerFindCellsTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerFindCells,
        SpreadsheetCellReference> {

    private final static int DEFAULT_MAX = 99;

    @Test
    public void testWithNegativeDefaultMax() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                        -1,
                        SpreadsheetEngines.fake()
                )
        );
    }

    private final static SpreadsheetCellRangeReferencePath PATH = SpreadsheetCellRangeReferencePath.LRTD;
    private final static int OFFSET = 12;
    private final static int MAX = 34;
    private final static String VALUE_TYPE = "test-value-type";
    private final static Expression EXPRESSION = Expression.call(
            Expression.namedFunction(
                    ExpressionFunctionName.with("test123")
            ),
            Expression.NO_CHILDREN
    );

    @Test
    public void testHandleOne() {
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell b2Cell = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );
        final Set<SpreadsheetCell> found = Sets.of(
                b2Cell
        );

        final SpreadsheetCellRangeReferencePath path = PATH;
        final int offset = OFFSET;
        final int max = MAX;
        final String valueType = VALUE_TYPE;
        final Expression expression = EXPRESSION;

        this.handleOneAndCheck(
                SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                        99,
                        new FakeSpreadsheetEngine() {

                            @Override
                            public Set<SpreadsheetCell> findCells(final SpreadsheetCellRangeReference r,
                                                                  final SpreadsheetCellRangeReferencePath p,
                                                                  final int o,
                                                                  final int m,
                                                                  final String v,
                                                                  final Expression e,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(b2.toCellRange(), r, "range");
                                checkEquals(path, p, "path");
                                checkEquals(offset, o, "offset");
                                checkEquals(max, m, "max");
                                checkEquals(valueType, v, "valueType");
                                checkEquals(expression, e, "expression");

                                return found;
                            }
                        }
                ),
                b2, // reference
                Optional.empty(), // resource
                Maps.of(
                        SpreadsheetCellFindQuery.CELL_RANGE_PATH, Lists.of(path.name()),
                        SpreadsheetCellFindQuery.OFFSET, Lists.of("" + offset),
                        SpreadsheetCellFindQuery.MAX, Lists.of("" + max),
                        SpreadsheetCellFindQuery.VALUE_TYPE, Lists.of("" + valueType),
                        SpreadsheetCellFindQuery.QUERY, Lists.of("" + expression)
                ), // parameters
                new TestSpreadsheetHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadata spreadsheetMetadata() {
                        return METADATA;
                    }

                    @Override
                    public SpreadsheetParserToken parseFormula(final TextCursor formula) {
                        final TextCursorSavePoint begin = formula.save();
                        formula.end();
                        final String text = begin.textBetween()
                                .toString();
                        checkEquals(EXPRESSION.toString(), text);

                        return SpreadsheetParserToken.functionName(
                                SpreadsheetFunctionName.with("test123"),
                                text
                        );
                    }

                    @Override
                    public Optional<Expression> toExpression(final SpreadsheetParserToken token) {
                        return Optional.of(EXPRESSION);
                    }
                },
                Optional.of(
                        SpreadsheetDelta.EMPTY.setCells(
                                found
                        )
                )
        );
    }

    @Test
    public void testHandleRange() {
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell b2Cell = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCell c3Cell = c3.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final Set<SpreadsheetCell> found = Sets.of(
                b2Cell,
                c3Cell
        );

        final SpreadsheetCellRangeReference range = b2.cellRange(c3);
        final SpreadsheetCellRangeReferencePath path = PATH;
        final int offset = OFFSET;
        final int max = MAX;
        final String valueType = VALUE_TYPE;
        final Expression expression = EXPRESSION;

        this.handleRangeAndCheck(
                SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                        99,
                        new FakeSpreadsheetEngine() {

                            @Override
                            public Set<SpreadsheetCell> findCells(final SpreadsheetCellRangeReference r,
                                                                  final SpreadsheetCellRangeReferencePath p,
                                                                  final int o,
                                                                  final int m,
                                                                  final String v,
                                                                  final Expression e,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(range, r, "range");
                                checkEquals(path, p, "path");
                                checkEquals(offset, o, "offset");
                                checkEquals(max, m, "max");
                                checkEquals(valueType, v, "valueType");
                                checkEquals(expression, e, "expression");

                                return found;
                            }
                        }
                ),
                range.range(), // reference
                Optional.empty(), // resource
                Maps.of(
                        SpreadsheetCellFindQuery.CELL_RANGE_PATH, Lists.of(path.name()),
                        SpreadsheetCellFindQuery.OFFSET, Lists.of("" + offset),
                        SpreadsheetCellFindQuery.MAX, Lists.of("" + max),
                        SpreadsheetCellFindQuery.VALUE_TYPE, Lists.of("" + valueType),
                        SpreadsheetCellFindQuery.QUERY, Lists.of("" + expression)
                ), // parameters
                new TestSpreadsheetHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadata spreadsheetMetadata() {
                        return METADATA;
                    }

                    @Override
                    public SpreadsheetParserToken parseFormula(final TextCursor formula) {
                        final TextCursorSavePoint begin = formula.save();
                        formula.end();
                        final String text = begin.textBetween()
                                .toString();
                        checkEquals(EXPRESSION.toString(), text);

                        return SpreadsheetParserToken.functionName(
                                SpreadsheetFunctionName.with("test123"),
                                text
                        );
                    }

                    @Override
                    public Optional<Expression> toExpression(final SpreadsheetParserToken token) {
                        return Optional.of(EXPRESSION);
                    }
                },
                Optional.of(
                        SpreadsheetDelta.EMPTY.setCells(
                                found
                        )
                )
        );
    }

    @Test
    public void testHandleAll() {
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell b2Cell = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCell c3Cell = c3.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final Set<SpreadsheetCell> found = Sets.of(
                b2Cell,
                c3Cell
        );

        final SpreadsheetCellRangeReference range = SpreadsheetSelection.ALL_CELLS;
        final SpreadsheetCellRangeReferencePath path = PATH;
        final int offset = OFFSET;
        final int max = MAX;
        final String valueType = VALUE_TYPE;
        final Expression expression = EXPRESSION;

        this.handleRangeAndCheck(
                SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                        99,
                        new FakeSpreadsheetEngine() {

                            @Override
                            public Set<SpreadsheetCell> findCells(final SpreadsheetCellRangeReference r,
                                                                  final SpreadsheetCellRangeReferencePath p,
                                                                  final int o,
                                                                  final int m,
                                                                  final String v,
                                                                  final Expression e,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(range, r, "range");
                                checkEquals(path, p, "path");
                                checkEquals(offset, o, "offset");
                                checkEquals(max, m, "max");
                                checkEquals(valueType, v, "valueType");
                                checkEquals(expression, e, "expression");

                                return found;
                            }
                        }
                ),
                range.range(), // reference
                Optional.empty(), // resource
                Maps.of(
                        SpreadsheetCellFindQuery.CELL_RANGE_PATH, Lists.of(path.name()),
                        SpreadsheetCellFindQuery.OFFSET, Lists.of("" + offset),
                        SpreadsheetCellFindQuery.MAX, Lists.of("" + max),
                        SpreadsheetCellFindQuery.VALUE_TYPE, Lists.of("" + valueType),
                        SpreadsheetCellFindQuery.QUERY, Lists.of("" + expression)
                ), // parameters
                new TestSpreadsheetHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadata spreadsheetMetadata() {
                        return METADATA;
                    }

                    @Override
                    public SpreadsheetParserToken parseFormula(final TextCursor formula) {
                        final TextCursorSavePoint begin = formula.save();
                        formula.end();
                        final String text = begin.textBetween()
                                .toString();
                        checkEquals(EXPRESSION.toString(), text);

                        return SpreadsheetParserToken.functionName(
                                SpreadsheetFunctionName.with("test123"),
                                text
                        );
                    }

                    @Override
                    public Optional<Expression> toExpression(final SpreadsheetParserToken token) {
                        return Optional.of(EXPRESSION);
                    }
                },
                Optional.of(
                        SpreadsheetDelta.EMPTY.setCells(
                                found
                        )
                )
        );
    }

    @Test
    public void testRangeWithoutQueryParameters() {
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell b2Cell = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCell c3Cell = c3.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final Set<SpreadsheetCell> found = Sets.of(
                b2Cell,
                c3Cell
        );

        final SpreadsheetCellRangeReference range = b2.cellRange(c3);

        new FakeSpreadsheetEngineContext() {
            @Override
            public SpreadsheetParserToken parseFormula(final TextCursor formula) {
                final TextCursorSavePoint begin = formula.save();
                formula.end();
                final String text = begin.textBetween()
                        .toString();
                checkEquals(EXPRESSION.toString(), text);

                return SpreadsheetParserToken.functionName(
                        SpreadsheetFunctionName.with("test123"),
                        text
                );
            }

            @Override
            public Optional<Expression> toExpression(final SpreadsheetParserToken token) {
                return Optional.of(EXPRESSION);
            }
        };

        this.handleRangeAndCheck(
                SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                        DEFAULT_MAX,
                        new FakeSpreadsheetEngine() {

                            @Override
                            public Set<SpreadsheetCell> findCells(final SpreadsheetCellRangeReference r,
                                                                  final SpreadsheetCellRangeReferencePath p,
                                                                  final int o,
                                                                  final int m,
                                                                  final String v,
                                                                  final Expression e,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(range, r, "range");
                                checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_CELL_RANGE_PATH, p, "path");
                                checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_OFFSET, o, "offset");
                                checkEquals(DEFAULT_MAX, m, "max");
                                checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_VALUE_TYPE, v, "valueType");
                                checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_QUERY, e, "expression");

                                return found;
                            }
                        }
                ),
                range.range(), // reference
                Optional.empty(), // resource
                Maps.empty(), // parameters
                this.context(),
                Optional.of(
                        SpreadsheetDelta.EMPTY.setCells(
                                found
                        )
                )
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                SpreadsheetEngine.class.getSimpleName() + ".findCells"
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerFindCells createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerFindCells.with(
                DEFAULT_MAX,
                engine
        );
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.of(
                SpreadsheetCellFindQuery.QUERY, Lists.of("true")
        );
    }

    @Override
    public SpreadsheetCellReference id() {
        return SpreadsheetSelection.A1;
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetSelection.parseCellRange("A1:B2")
                .range(); // url has TO
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
    public SpreadsheetHateosResourceHandlerContext context() {
        return SpreadsheetHateosResourceHandlerContexts.fake();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerFindCells> type() {
        return SpreadsheetDeltaHateosResourceHandlerFindCells.class;
    }
}
