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
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.convert.SpreadsheetConverterContexts;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatters;
import walkingkooka.spreadsheet.format.SpreadsheetText;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.expression.ExpressionNumberConverterContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.util.FunctionTesting;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMultiFormatterTest extends SpreadsheetFormatterTestCase<SpreadsheetMultiFormatter>
        implements FunctionTesting<SpreadsheetMultiFormatter, SpreadsheetMultiFormatRequest, SpreadsheetMultiFormatResponse> {

    private final static Supplier<LocalDateTime> NOW = LocalDateTime::now;

    private final static Function<SpreadsheetSelection, SpreadsheetSelection> RESOLVE_IF_LABEL = (s) -> {
        throw new UnsupportedOperationException();
    };

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testWithNullSpreadsheetEngineContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetMultiFormatter.with(
                        null,
                        NOW
                )
        );
    }

    @Test
    public void testWithNullNowFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetMultiFormatter.with(
                        SpreadsheetEngineContexts.fake(),
                        null
                )
        );
    }

    @Test
    public void testFormatUnknownValueFails() {
        this.applyAndCheck(SpreadsheetMultiFormatRequest.with(
                        Lists.of(
                                SpreadsheetFormatRequest.with(
                                        "unknown1 ???",
                                        "unknown2 ???"
                                )
                )
                ),
                SpreadsheetMultiFormatResponse.with(
                        Lists.of(
                                exception("Invalid pattern \"unknown2 ???\"")
                        )
                )
        );
    }

    @Test
    public void testFormatLocalDate() {
        this.applyAndCheck(SpreadsheetMultiFormatRequest.with(
                Lists.of(
                        SpreadsheetFormatRequest.with(
                                LocalDate.of(1999, 12, 31),
                                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/dd/mm")
                        )
                )
                ),
                SpreadsheetMultiFormatResponse.with(
                        Lists.of(
                                SpreadsheetText.with("1999/31/12")
                        )
                )
        );
    }

    @Test
    public void testFormatLocalDateAndLocalTimeSpreadsheetDateFormatPattern() {
        this.applyAndCheck(SpreadsheetMultiFormatRequest.with(
                Lists.of(
                        SpreadsheetFormatRequest.with(
                                LocalDate.of(1999, 12, 31),
                                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/dd/mm")
                        ),
                        SpreadsheetFormatRequest.with(
                                LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                                SpreadsheetFormatPattern.parseDateTimeFormatPattern("yyyy/hh/mm")
                        )
                )
                ),
                SpreadsheetMultiFormatResponse.with(
                        Lists.of(
                                SpreadsheetText.with("1999/31/12"),
                                SpreadsheetText.with("1999/12/58")
                        )
                )
        );
    }

    @Test
    public void testFormatLocalDateAndLocalTimeSpreadsheetLocaleDefaultDateTimeFormat() {
        this.applyAndCheck(SpreadsheetMultiFormatRequest.with(
                Lists.of(
                        SpreadsheetFormatRequest.with(
                                LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                                SpreadsheetLocaleDefaultDateTimeFormat.INSTANCE
                        )
                )
                ),
                SpreadsheetMultiFormatResponse.with(
                        Lists.of(
                                "31 December 1999, 12:58:59 pm"
                        )
                )
        );
    }

    @Test
    public void testFormatFails() {
        this.applyAndCheck(SpreadsheetMultiFormatRequest.with(
                Lists.of(
                        SpreadsheetFormatRequest.with(
                                BigDecimal.ONE,
                                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/dd/mm")
                        )
                )
                ),
                SpreadsheetMultiFormatResponse.with(
                        Lists.of(
                                exception("Failed to convert 1 (java.math.BigDecimal) to java.time.LocalDateTime")
                        )
                )
        );
    }

    /**
     * This {@link Exception} is equal to any exception with an equal message.
     */
    private static Exception exception(final String message) {
        return new Exception(message) {
            private static final long serialVersionUID = 1L;

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(final Object other) {
                return this == other || other instanceof Exception && this.equals0((Exception) other);
            }

            private boolean equals0(final Exception other) {
                return this.getMessage().equals(other.getMessage());
            }
        };
    }

    @Override
    public SpreadsheetMultiFormatter createFunction() {
        return SpreadsheetMultiFormatter.with(
                new FakeSpreadsheetEngineContext() {

                    @Override
                    public SpreadsheetMetadata metadata() {
                        return SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                                .loadFromLocale()
                                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetPattern.DEFAULT_TEXT_FORMAT_PATTERN);

                    }

                    @Override
                    public Optional<SpreadsheetText> format(final Object value,
                                                            final SpreadsheetFormatter formatter) {
                        return formatter.format(value,
                                SpreadsheetFormatterContexts.basic(
                                        function(),
                                        function(),
                                        1,
                                        SpreadsheetFormatters.fake(),
                                        SpreadsheetConverterContexts.basic(
                                                Converters.collection(
                                                        Lists.of(
                                                                Converters.simple(),
                                                                Converters.localDateLocalDateTime()
                                                        )
                                                ),
                                                RESOLVE_IF_LABEL,
                                                ExpressionNumberConverterContexts.basic(
                                                        Converters.fake(),
                                                        ConverterContexts.basic(
                                                                Converters.fake(),
                                                                DateTimeContexts.locale(
                                                                        Locale.ENGLISH,
                                                                        1900,
                                                                        20,
                                                                        NOW
                                                                ),
                                                                DecimalNumberContexts.american(MathContext.DECIMAL32)
                                                        ),
                                                        ExpressionNumberKind.DOUBLE
                                                )
                                        )
                                )
                        );
                    }
                },
                NOW
        );
    }

    private static <S, D> Function<S, D> function() {
        return (s) -> {
            throw new UnsupportedOperationException();
        };
    }


    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetMultiFormatter> type() {
        return SpreadsheetMultiFormatter.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
