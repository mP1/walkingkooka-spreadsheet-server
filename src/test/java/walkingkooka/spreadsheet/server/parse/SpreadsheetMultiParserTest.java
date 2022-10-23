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

package walkingkooka.spreadsheet.server.parse;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePatterns;
import walkingkooka.util.FunctionTesting;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMultiParserTest extends SpreadsheetParserTestCase<SpreadsheetMultiParser>
        implements FunctionTesting<SpreadsheetMultiParser, SpreadsheetMultiParseRequest, SpreadsheetMultiParseResponse> {

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testWithNullSpreadsheetEngineContext() {
        assertThrows(NullPointerException.class, () -> SpreadsheetMultiParser.with(null));
    }

    @Test
    public void testParseUnknownParserFails() {
        this.applyAndCheck2("#.##",
                "unknown-parser",
                illegalArgumentException("Unknown parser \"unknown-parser\"")
        );
    }

    @Test
    public void testParseDateFormat() {
        final String pattern = "yyyy/mm/dd";
        this.applyAndCheck2(pattern,
                SpreadsheetMultiParser.SPREADSHEET_DATE_FORMATTER,
                SpreadsheetParsePatterns.parseDateFormatPattern(pattern));
    }

    @Test
    public void testParseDateParser() {
        final String pattern = "yyyy/mm/dd";
        this.applyAndCheck2(pattern,
                SpreadsheetMultiParser.SPREADSHEET_DATE_PARSERS,
                SpreadsheetParsePatterns.parseDateParsePatterns(pattern));
    }


    @Test
    public void testParseDateTimeFormat() {
        final String pattern = "yyyy/mm/dd hh:mm";
        this.applyAndCheck2(pattern,
                SpreadsheetMultiParser.SPREADSHEET_DATE_TIME_FORMATTER,
                SpreadsheetParsePatterns.parseDateTimeFormatPattern(pattern));
    }

    @Test
    public void testParseDateTimeParser() {
        final String pattern = "yyyy/mm/dd";
        this.applyAndCheck2(pattern,
                SpreadsheetMultiParser.SPREADSHEET_DATE_TIME_PARSERS,
                SpreadsheetParsePatterns.parseDateTimeParsePatterns(pattern));
    }

    @Test
    public void testParseTextFormat() {
        final String pattern = "@ \"Hello\"";
        this.applyAndCheck2(pattern,
                SpreadsheetMultiParser.SPREADSHEET_TEXT_FORMATTER,
                SpreadsheetParsePatterns.parseTextFormatPattern(pattern));
    }

    @Test
    public void testParseTimeFormat() {
        final String pattern = "hh:mm";
        this.applyAndCheck2(pattern,
                SpreadsheetMultiParser.SPREADSHEET_TIME_FORMATTER,
                SpreadsheetParsePatterns.parseTimeFormatPattern(pattern));
    }

    @Test
    public void testParseTimeParser() {
        final String pattern = "hh:mm";
        this.applyAndCheck2(pattern,
                SpreadsheetMultiParser.SPREADSHEET_TIME_PARSERS,
                SpreadsheetParsePatterns.parseTimeParsePatterns(pattern));
    }

    @Test
    public void testParseFails() {
        this.applyAndCheck2(
                "hh:mm:ss",
                SpreadsheetMultiParser.SPREADSHEET_DATE_PARSERS,
                illegalArgumentException("Invalid character 'h' at (1,1) \"hh:mm:ss\" expected ([ CONDITION ], [{ DAY | MONTH_MINUTE | YEAR | DATE_DATETIME_TIME }], [{(SEPARATOR, [ CONDITION ], [{ DAY | MONTH_MINUTE | YEAR | DATE_DATETIME_TIME }])}], [SEPARATOR])")
        );
    }

    private void applyAndCheck2(final String pattern,
                                final String parser,
                                final Object expected) {
        this.applyAndCheck(SpreadsheetMultiParseRequest.with(
                Lists.of(
                        SpreadsheetParseRequest.with(
                                pattern,
                                parser
                        )
                )
                ),
                SpreadsheetMultiParseResponse.with(
                        Lists.of(
                                expected
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
    public SpreadsheetMultiParser createFunction() {
        return SpreadsheetMultiParser.with(SpreadsheetEngineContexts.fake());
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetMultiParser> type() {
        return SpreadsheetMultiParser.class;
    }


    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
