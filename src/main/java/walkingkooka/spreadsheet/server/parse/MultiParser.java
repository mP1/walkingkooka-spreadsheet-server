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

import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePatterns;
import walkingkooka.text.CharSequences;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Supports processing several parse requests one by one and storing the returned value or {@link Throwable} for each.
 * The following pattern/parsers are supported.
 * <ul>
 *     <li>{@link SpreadsheetFormatPattern}</li>
 * </ul>
 */
final class MultiParser implements Function<MultiParseRequest, MultiParseResponse> {

    static MultiParser with(final SpreadsheetEngineContext engineContext) {
        Objects.requireNonNull(engineContext, "engineContext");

        return new MultiParser(engineContext);
    }

    private MultiParser(final SpreadsheetEngineContext engineContext) {
        super();
        this.engineContext = engineContext;
    }

    @Override
    public MultiParseResponse apply(final MultiParseRequest request) {
        Objects.requireNonNull(request, "request");

        return MultiParseResponse.with(
                request.requests()
                        .stream()
                        .map(this::parseOrThrowable)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Returns the parser value or the thrown {@link Throwable}.
     */
    private Object parseOrThrowable(final ParseRequest request) {
        Object result;
        try {
            result = this.parse(request);
        } catch (final Throwable cause) {
            result = cause;
        }
        return result;
    }

    final static String SPREADSHEET_DATE_FORMATTER = "spreadsheet-date-formatter";
    final static String SPREADSHEET_DATE_PARSERS = "spreadsheet-date-parsers";

    final static String SPREADSHEET_DATE_TIME_FORMATTER = "spreadsheet-date-time-formatter";
    final static String SPREADSHEET_DATE_TIME_PARSERS = "spreadsheet-date-time-parsers";

    final static String SPREADSHEET_NUMBER_FORMATTER = "spreadsheet-number-formatter";
    final static String SPREADSHEET_NUMBER_PARSERS = "spreadsheet-number-parsers";

    final static String SPREADSHEET_TEXT_FORMATTER = "spreadsheet-text-formatter";

    final static String SPREADSHEET_TIME_FORMATTER = "spreadsheet-time-formatter";
    final static String SPREADSHEET_TIME_PARSERS = "spreadsheet-time-parsers";

    /**
     * Handles the request returning the parsed value or throwing a {@link Exception} if parsing failed.
     */
    private Object parse(final ParseRequest request) {
        Object parsed;

        final String text = request.text();
        final String parser = request.parser(); // the name of the parser

        switch (parser) {
            case SPREADSHEET_DATE_FORMATTER:
                parsed = SpreadsheetFormatPattern.parseDateFormatPattern(text);
                break;
            case SPREADSHEET_DATE_PARSERS:
                parsed = SpreadsheetParsePatterns.parseDateParsePatterns(text);
                break;
            case SPREADSHEET_DATE_TIME_FORMATTER:
                parsed = SpreadsheetFormatPattern.parseDateTimeFormatPattern(text);
                break;
            case SPREADSHEET_DATE_TIME_PARSERS:
                parsed = SpreadsheetParsePatterns.parseDateTimeParsePatterns(text);
                break;
            case SPREADSHEET_NUMBER_FORMATTER:
                parsed = SpreadsheetFormatPattern.parseNumberFormatPattern(text);
                break;
            case SPREADSHEET_NUMBER_PARSERS:
                parsed = SpreadsheetParsePatterns.parseNumberParsePatterns(text);
                break;
            case SPREADSHEET_TEXT_FORMATTER:
                parsed = SpreadsheetFormatPattern.parseTextFormatPattern(text);
                break;
            case SPREADSHEET_TIME_FORMATTER:
                parsed = SpreadsheetFormatPattern.parseTimeFormatPattern(text);
                break;
            case SPREADSHEET_TIME_PARSERS:
                parsed = SpreadsheetParsePatterns.parseTimeParsePatterns(text);
                break;
            default:
                throw new IllegalArgumentException("Unknown parser " + CharSequences.quoteAndEscape(parser));
        }

        return parsed;
    }

    // TODO will be used by other parse requests
    private final SpreadsheetEngineContext engineContext;

    @Override
    public String toString() {
        return this.engineContext.toString();
    }
}
