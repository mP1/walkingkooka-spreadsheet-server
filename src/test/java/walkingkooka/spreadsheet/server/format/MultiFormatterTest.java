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

package walkingkooka.spreadsheet.server.format;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.convert.Converters;
import walkingkooka.datetime.DateTimeContexts;
import walkingkooka.math.DecimalNumberContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatters;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.tree.expression.ExpressionNumberConverterContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.util.FunctionTesting;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MultiFormatterTest extends FormatterTestCase<MultiFormatter>
        implements FunctionTesting<MultiFormatter, MultiFormatRequest, MultiFormatResponse> {

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testWithNullSpreadsheetFormatContext() {
        assertThrows(NullPointerException.class, () -> MultiFormatter.with(null));
    }

    @Test
    public void testFormatUnknownValueFails() {
        this.applyAndCheck(MultiFormatRequest.with(
                Lists.of(
                        FormatRequest.with(
                                "unknown1 ???",
                                "unknown2 ???"
                        )
                )
                ),
                MultiFormatResponse.with(
                        Lists.of(
                                illegalArgumentException("Invalid pattern \"unknown2 ???\"")
                        )
                )
        );
    }

    @Test
    public void testFormatLocalDate() {
        this.applyAndCheck(MultiFormatRequest.with(
                Lists.of(
                        FormatRequest.with(
                                LocalDate.of(1999, 12, 31),
                                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/dd/mm")
                        )
                )
                ),
                MultiFormatResponse.with(
                        Lists.of(
                                SpreadsheetText.with(SpreadsheetText.WITHOUT_COLOR, "1999/31/12")
                        )
                )
        );
    }

    @Test
    public void testFormatLocalDateAndLocalTime() {
        this.applyAndCheck(MultiFormatRequest.with(
                Lists.of(
                        FormatRequest.with(
                                LocalDate.of(1999, 12, 31),
                                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/dd/mm")
                        ),
                        FormatRequest.with(
                                LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                                SpreadsheetFormatPattern.parseDateTimeFormatPattern("yyyy/hh/mm")
                        )
                )
                ),
                MultiFormatResponse.with(
                        Lists.of(
                                SpreadsheetText.with(SpreadsheetText.WITHOUT_COLOR, "1999/31/12"),
                                SpreadsheetText.with(SpreadsheetText.WITHOUT_COLOR, "1999/12/58")
                        )
                )
        );
    }

    @Test
    public void testFormatFails() {
        this.applyAndCheck(MultiFormatRequest.with(
                Lists.of(
                        FormatRequest.with(
                                BigDecimal.ONE,
                                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/dd/mm")
                        )
                )
                ),
                MultiFormatResponse.with(
                        Lists.of(
                                illegalArgumentException("Unable to format 1")
                        )
                )
        );
    }

    private static IllegalArgumentException illegalArgumentException(final String message) {
        return new IllegalArgumentException(message) {
            private static final long serialVersionUID = 1L;

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(final Object other) {
                return this == other || other instanceof IllegalArgumentException && this.equals0((IllegalArgumentException) other);
            }

            private boolean equals0(final IllegalArgumentException other) {
                return this.getMessage().equals(other.getMessage());
            }
        };
    }

    @Override
    public MultiFormatter createFunction() {
        return MultiFormatter.with(
                SpreadsheetFormatterContexts.basic(function(),
                        function(),
                        1,
                        SpreadsheetFormatters.fake(),
                        ExpressionNumberConverterContexts.basic(Converters.collection(Lists.of(Converters.simple(), Converters.localDateLocalDateTime())),
                                ConverterContexts.basic(Converters.fake(), DateTimeContexts.locale(Locale.ENGLISH, 20), DecimalNumberContexts.american(MathContext.DECIMAL32)),
                                ExpressionNumberKind.DOUBLE
                        )
                )
        );
    }

    private static <S, D> Function<S, D> function() {
        return (s) -> {
            throw new UnsupportedOperationException();
        };
    }

    @Override
    public Class<MultiFormatter> type() {
        return MultiFormatter.class;
    }
}
