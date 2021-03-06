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

package walkingkooka.spreadsheet.server.sample;

import walkingkooka.Cast;
import walkingkooka.Either;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.Converter;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.convert.Converters;
import walkingkooka.datetime.DateTimeContext;
import walkingkooka.datetime.DateTimeContexts;
import walkingkooka.math.DecimalNumberContext;
import walkingkooka.math.DecimalNumberContexts;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.format.SpreadsheetFormatException;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatters;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatExpressionParserToken;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParserContexts;
import walkingkooka.spreadsheet.format.parser.SpreadsheetFormatParsers;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.SpreadsheetParserContexts;
import walkingkooka.spreadsheet.parser.SpreadsheetParserToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParsers;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.text.cursor.TextCursors;
import walkingkooka.text.cursor.parser.ParserReporters;
import walkingkooka.tree.expression.Expression;
import walkingkooka.tree.expression.ExpressionEvaluationContexts;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.expression.ExpressionNumberConverterContext;
import walkingkooka.tree.expression.ExpressionNumberConverterContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.ExpressionReference;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;

import java.math.MathContext;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class Sample {

    public static void main(final String[] args) {
        new Sample().testWithCellReference();
    }

    private final static ExpressionNumberKind EXPRESSION_NUMBER_KIND = ExpressionNumberKind.DEFAULT;
    private final static char VALUE_SEPARATOR = ',';

    public void testWithCellReference() {
        final SpreadsheetCellStore cellStore = cellStore();
        final SpreadsheetLabelStore labelStore = SpreadsheetLabelStores.treeMap();

        final SpreadsheetEngine engine = engine(cellStore, labelStore);
        final SpreadsheetEngineContext engineContext = engineContext(engine, labelStore);

        engine.saveCell(SpreadsheetCell.with(SpreadsheetCellReference.parseCellReference("A1"), SpreadsheetFormula.with("12+B2")), engineContext);

        final SpreadsheetDelta delta = engine.saveCell(SpreadsheetCell.with(SpreadsheetCellReference.parseCellReference("B2"), SpreadsheetFormula.with("34")), engineContext);

        final Set<String> saved = delta.cells()
                .stream()
                .map(c -> c.formula().value().get().toString())
                .collect(Collectors.toCollection(Sets::sorted));

        // a1=12+b2
        // a1=12+34
        // b2=34
        checkEquals(Sets.of("46", "34"), saved, "saved cells formatted value");
    }

    private static SpreadsheetCellStore cellStore() {
        return SpreadsheetCellStores.treeMap();
    }

    private static Converter<ExpressionNumberConverterContext> converter() {
        return Converters.collection(Lists.of(Converters.simple(),
                ExpressionNumber.toConverter(Converters.numberNumber())));
    }

    private static ExpressionNumberConverterContext converterContext() {
        return ExpressionNumberConverterContexts.basic(converter(),
                ConverterContexts.basic(Converters.fake(),
                        dateTimeContext(),
                        decimalNumberContext()),
                EXPRESSION_NUMBER_KIND);
    }

    private static DateTimeContext dateTimeContext() {
        return DateTimeContexts.locale(
                decimalNumberContext().locale(),
                1900,
                50
        );
    }

    private static DecimalNumberContext decimalNumberContext() {
        return DecimalNumberContexts.american(MathContext.DECIMAL32);
    }

    private static Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>> functions() {
        return (n) -> {
            throw new UnknownExpressionFunctionException(n);
        };
    }

    private static SpreadsheetEngine engine(final SpreadsheetCellStore cellStore,
                                            final SpreadsheetLabelStore labelStore) {
        return SpreadsheetEngines.basic(
                SpreadsheetId.with(123),
                SpreadsheetMetadata.EMPTY,
                cellStore,
                SpreadsheetExpressionReferenceStores.treeMap(),
                labelStore,
                SpreadsheetExpressionReferenceStores.treeMap(),
                SpreadsheetRangeStores.treeMap(),
                SpreadsheetRangeStores.treeMap());
    }

    private static SpreadsheetEngineContext engineContext(final SpreadsheetEngine engine,
                                                          final SpreadsheetLabelStore labelStore) {
        return new FakeSpreadsheetEngineContext() {

            @Override
            public ExpressionNumberKind expressionNumberKind() {
                return EXPRESSION_NUMBER_KIND;
            }

            @Override
            public SpreadsheetParserToken parseFormula(final String formula) {
                return Cast.to(SpreadsheetParsers.expression()
                        .orFailIfCursorNotEmpty(ParserReporters.basic())
                        .parse(TextCursors.charSequence(formula), SpreadsheetParserContexts.basic(DateTimeContexts.fake(), converterContext(), EXPRESSION_NUMBER_KIND, VALUE_SEPARATOR))
                        .get());
            }

            @Override
            public Object evaluate(final Expression node) {
                return node.toValue(ExpressionEvaluationContexts.basic(EXPRESSION_NUMBER_KIND, functions(), references(), converterContext()));
            }

            private Function<ExpressionReference, Optional<Expression>> references() {
                return SpreadsheetEngines.expressionEvaluationContextExpressionReferenceExpressionFunction(engine, labelStore, this);
            }

            @Override
            public <T> Either<T, String> convert(final Object value, final Class<T> target) {
                checkEquals(Boolean.class,
                        target,
                        "Only support converting to Boolean=" + value);
                return Either.left(Cast.to(Boolean.parseBoolean(String.valueOf(value))));
            }

            @Override
            public SpreadsheetFormatter parsePattern(final String pattern) {
                final SpreadsheetFormatExpressionParserToken token = SpreadsheetFormatParsers.expression()
                        .orFailIfCursorNotEmpty(ParserReporters.basic())
                        .parse(TextCursors.charSequence(pattern), SpreadsheetFormatParserContexts.basic())
                        .get()
                        .cast(SpreadsheetFormatExpressionParserToken.class);
                return SpreadsheetFormatters.expression(token, (v) -> {
                    throw new UnsupportedOperationException();
                });
            }

            @Override
            public SpreadsheetFormatter defaultSpreadsheetFormatter() {
                return Sample.defaultSpreadsheetFormatter();
            }

            @Override
            public Optional<SpreadsheetText> format(final Object value,
                                                    final SpreadsheetFormatter formatter) {
                checkEquals(false, value instanceof Optional, "Value must not be optional" + value);
                return formatter.format(value, formatterContext());
            }
        };
    }

    /**
     * A {@link SpreadsheetFormatter} that accepts all values and creates a {@link SpreadsheetText} with {@link Object#toString()} and no colour.
     */
    private static SpreadsheetFormatter defaultSpreadsheetFormatter() {
        return new SpreadsheetFormatter() {
            @Override
            public boolean canFormat(final Object value,
                                     final SpreadsheetFormatterContext context) throws SpreadsheetFormatException {
                return true;
            }

            @Override
            public Optional<SpreadsheetText> format(final Object value,
                                                    final SpreadsheetFormatterContext context) throws SpreadsheetFormatException {
                return Optional.of(SpreadsheetText.with(SpreadsheetText.WITHOUT_COLOR, String.valueOf(value)));
            }
        };
    }

    /**
     * A {@lnk SpreadsheetFormatterContext} that is fully functional except for translating colour numbers and colour names to a {@link Color}.
     */
    private static SpreadsheetFormatterContext formatterContext() {
        return SpreadsheetFormatterContexts.basic(
                (n) -> Optional.empty(), // always nothing
                (n) -> {
                    throw new UnsupportedOperationException();
                },
                80,
                defaultSpreadsheetFormatter(),
                converterContext()
        );
    }

    private static void checkEquals(final Object expected, final Object actual, final String message) {
        assertEquals(expected, actual, message);
    }
}
