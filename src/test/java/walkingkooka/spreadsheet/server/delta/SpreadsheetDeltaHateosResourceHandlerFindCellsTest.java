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
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetCellFindQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.expression.SpreadsheetFunctionName;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.formula.parser.SpreadsheetFormulaParserToken;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReferencePath;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.text.cursor.TextCursor;
import walkingkooka.text.cursor.TextCursorSavePoint;
import walkingkooka.tree.expression.Expression;
import walkingkooka.validation.ValidationValueTypeName;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaHateosResourceHandlerFindCellsTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerFindCells,
    SpreadsheetCellReference> {

    private final static SpreadsheetCellRangeReferencePath PATH = SpreadsheetCellRangeReferencePath.LRTD;
    private final static int OFFSET = 12;
    private final static int COUNT = 34;
    private final static ValidationValueTypeName VALUE_TYPE = ValidationValueTypeName.with("test-value-type");
    private final static Expression EXPRESSION = Expression.call(
        Expression.namedFunction(
            SpreadsheetExpressionFunctions.name("test123")
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
        final int count = COUNT;
        final ValidationValueTypeName valueType = VALUE_TYPE;
        final Expression expression = EXPRESSION;

        this.handleOneAndCheck(
            b2, // reference
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellFindQuery.CELL_RANGE_PATH, Lists.of(path.name()),
                SpreadsheetCellFindQuery.OFFSET, Lists.of(String.valueOf(offset)),
                SpreadsheetCellFindQuery.COUNT, Lists.of(String.valueOf(count)),
                SpreadsheetCellFindQuery.VALUE_TYPE, Lists.of(valueType.value()),
                SpreadsheetCellFindQuery.QUERY, Lists.of(String.valueOf(expression))
            ), // parameters
            UrlPath.EMPTY,
            new TestSpreadsheetEngineHateosResourceHandlerContext() {

                @Override
                public SpreadsheetEngine spreadsheetEngine() {
                    return new FakeSpreadsheetEngine() {

                        @Override
                        public SpreadsheetDelta findCells(final SpreadsheetCellRangeReference r,
                                                          final SpreadsheetCellRangeReferencePath p,
                                                          final int o,
                                                          final int c,
                                                          final ValidationValueTypeName v,
                                                          final Expression e,
                                                          final Set<SpreadsheetDeltaProperties> properties,
                                                          final SpreadsheetEngineContext context) {
                            checkEquals(b2.toCellRange(), r, "range");
                            checkEquals(path, p, "path");
                            checkEquals(offset, o, "offset");
                            checkEquals(count, c, "count");
                            checkEquals(valueType, v, "valueType");
                            checkEquals(expression, e, "expression");

                            return SpreadsheetDelta.EMPTY.setCells(found);
                        }
                    };
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return METADATA;
                }

                @Override
                public SpreadsheetFormulaParserToken parseFormula(final TextCursor formula,
                                                                  final Optional<SpreadsheetCell> cell) {
                    final TextCursorSavePoint begin = formula.save();
                    formula.end();
                    final String text = begin.textBetween()
                        .toString();
                    checkEquals(EXPRESSION.toString(), text);

                    return SpreadsheetFormulaParserToken.functionName(
                        SpreadsheetFunctionName.with("test123"),
                        text
                    );
                }

                @Override
                public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken token) {
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
        final int count = COUNT;
        final ValidationValueTypeName valueType = VALUE_TYPE;
        final Expression expression = EXPRESSION;

        this.handleRangeAndCheck(
            range.range(), // reference
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellFindQuery.CELL_RANGE_PATH, Lists.of(path.name()),
                SpreadsheetCellFindQuery.OFFSET, Lists.of("" + offset),
                SpreadsheetCellFindQuery.COUNT, Lists.of("" + count),
                SpreadsheetCellFindQuery.VALUE_TYPE, Lists.of("" + valueType),
                SpreadsheetCellFindQuery.QUERY, Lists.of("" + expression)
            ), // parameters
            UrlPath.EMPTY,
            new TestSpreadsheetEngineHateosResourceHandlerContext() {

                @Override
                public SpreadsheetEngine spreadsheetEngine() {
                    return new FakeSpreadsheetEngine() {

                        @Override
                        public SpreadsheetDelta findCells(final SpreadsheetCellRangeReference r,
                                                          final SpreadsheetCellRangeReferencePath p,
                                                          final int o,
                                                          final int c,
                                                          final ValidationValueTypeName v,
                                                          final Expression e,
                                                          final Set<SpreadsheetDeltaProperties> properties,
                                                          final SpreadsheetEngineContext context) {
                            checkEquals(range, r, "range");
                            checkEquals(path, p, "path");
                            checkEquals(offset, o, "offset");
                            checkEquals(count, c, "count");
                            checkEquals(valueType, v, "valueType");
                            checkEquals(expression, e, "expression");

                            return SpreadsheetDelta.EMPTY.setCells(found);
                        }
                    };
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return METADATA;
                }

                @Override
                public SpreadsheetFormulaParserToken parseFormula(final TextCursor formula,
                                                                  final Optional<SpreadsheetCell> cell) {
                    final TextCursorSavePoint begin = formula.save();
                    formula.end();
                    final String text = begin.textBetween()
                        .toString();
                    checkEquals(EXPRESSION.toString(), text);

                    return SpreadsheetFormulaParserToken.functionName(
                        SpreadsheetFunctionName.with("test123"),
                        text
                    );
                }

                @Override
                public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken token) {
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
        final int count = COUNT;
        final ValidationValueTypeName valueType = VALUE_TYPE;
        final Expression expression = EXPRESSION;

        this.handleRangeAndCheck(
            range.range(), // reference
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetCellFindQuery.CELL_RANGE_PATH, Lists.of(path.name()),
                SpreadsheetCellFindQuery.OFFSET, Lists.of("" + offset),
                SpreadsheetCellFindQuery.COUNT, Lists.of("" + count),
                SpreadsheetCellFindQuery.VALUE_TYPE, Lists.of("" + valueType),
                SpreadsheetCellFindQuery.QUERY, Lists.of("" + expression)
            ), // parameters
            UrlPath.EMPTY,
            new TestSpreadsheetEngineHateosResourceHandlerContext() {

                @Override
                public SpreadsheetEngine spreadsheetEngine() {
                    return new FakeSpreadsheetEngine() {

                        @Override
                        public SpreadsheetDelta findCells(final SpreadsheetCellRangeReference r,
                                                          final SpreadsheetCellRangeReferencePath p,
                                                          final int o,
                                                          final int c,
                                                          final ValidationValueTypeName v,
                                                          final Expression e,
                                                          final Set<SpreadsheetDeltaProperties> properties,
                                                          final SpreadsheetEngineContext context) {
                            checkEquals(range, r, "range");
                            checkEquals(path, p, "path");
                            checkEquals(offset, o, "offset");
                            checkEquals(count, c, "count");
                            checkEquals(valueType, v, "valueType");
                            checkEquals(expression, e, "expression");

                            return SpreadsheetDelta.EMPTY.setCells(found);
                        }
                    };
                }

                @Override
                public SpreadsheetMetadata spreadsheetMetadata() {
                    return METADATA;
                }

                @Override
                public SpreadsheetFormulaParserToken parseFormula(final TextCursor formula,
                                                                  final Optional<SpreadsheetCell> cell) {
                    final TextCursorSavePoint begin = formula.save();
                    formula.end();
                    final String text = begin.textBetween()
                        .toString();
                    checkEquals(EXPRESSION.toString(), text);

                    return SpreadsheetFormulaParserToken.functionName(
                        SpreadsheetFunctionName.with("test123"),
                        text
                    );
                }

                @Override
                public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken token) {
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
            public SpreadsheetFormulaParserToken parseFormula(final TextCursor formula,
                                                              final Optional<SpreadsheetCell> cell) {
                final TextCursorSavePoint begin = formula.save();
                formula.end();
                final String text = begin.textBetween()
                    .toString();
                checkEquals(EXPRESSION.toString(), text);

                return SpreadsheetFormulaParserToken.functionName(
                    SpreadsheetFunctionName.with("test123"),
                    text
                );
            }

            @Override
            public Optional<Expression> toExpression(final SpreadsheetFormulaParserToken token) {
                return Optional.of(EXPRESSION);
            }
        };

        this.handleRangeAndCheck(
            range.range(), // reference
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS, // parameters
            UrlPath.EMPTY,
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta findCells(final SpreadsheetCellRangeReference r,
                                                      final SpreadsheetCellRangeReferencePath p,
                                                      final int o,
                                                      final int c,
                                                      final ValidationValueTypeName v,
                                                      final Expression e,
                                                      final Set<SpreadsheetDeltaProperties> properties,
                                                      final SpreadsheetEngineContext context) {
                        checkEquals(range, r, "range");
                        checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_CELL_RANGE_PATH, p, "path");
                        checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_OFFSET, o, "offset");
                        checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_COUNT, c, "count");
                        checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_VALUE_TYPE, v, "valueType");
                        checkEquals(SpreadsheetDeltaHateosResourceHandlerFindCells.DEFAULT_QUERY, e, "expression");

                        return SpreadsheetDelta.EMPTY.setCells(found);
                    }
                }
            ),
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
    public SpreadsheetDeltaHateosResourceHandlerFindCells createHandler() {
        return SpreadsheetDeltaHateosResourceHandlerFindCells.INSTANCE;
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
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerFindCells> type() {
        return SpreadsheetDeltaHateosResourceHandlerFindCells.class;
    }
}
