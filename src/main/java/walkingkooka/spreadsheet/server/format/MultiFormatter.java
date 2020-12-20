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

import walkingkooka.spreadsheet.format.HasSpreadsheetFormatter;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.text.CharSequences;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Supports processing several requests one by one and storing the returned value or {@link Throwable} for each.
 * The following pattern/formatters are supported.
 * <ul>
 *     <li>{@link walkingkooka.spreadsheet.format.SpreadsheetFormatter}</li>
 * </ul>
 */
final class MultiFormatter implements Function<MultiFormatRequest, MultiFormatResponse> {

    static MultiFormatter with(final SpreadsheetFormatterContext spreadsheetFormatterContext) {
        Objects.requireNonNull(spreadsheetFormatterContext, "spreadsheetFormatterContext");

        return new MultiFormatter(spreadsheetFormatterContext);
    }

    private MultiFormatter(final SpreadsheetFormatterContext spreadsheetFormatterContext) {
        super();
        this.spreadsheetFormatterContext = spreadsheetFormatterContext;
    }

    @Override
    public MultiFormatResponse apply(final MultiFormatRequest request) {
        Objects.requireNonNull(request, "request");

        return MultiFormatResponse.with(
                request.requests()
                        .stream()
                        .map(this::formatOrThrowable)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Returns the formatted value or the thrown {@link Throwable}.
     */
    private Object formatOrThrowable(final FormatRequest request) {
        Object result;
        try {
            result = this.format(request);
        } catch (final Throwable cause) {
            cause.printStackTrace();
            result = cause;
        }
        return result;
    }

    /**
     * Handles the request returning the formatted value or throwing a {@link Exception} if formatting failed.
     */
    private Object format(final FormatRequest request) {
        Object formatted;

        do {
            final Object value = request.value();
            final Object pattern = request.pattern();

            if (pattern instanceof HasSpreadsheetFormatter) {
                final HasSpreadsheetFormatter hasSpreadsheetFormatter = (HasSpreadsheetFormatter) pattern;
                formatted = hasSpreadsheetFormatter.formatter()
                        .format(value, this.spreadsheetFormatterContext)
                        .orElseThrow(() -> this.formatFail(value, pattern));
                break;
            }

            throw new IllegalArgumentException("Invalid pattern " + CharSequences.quoteIfChars(pattern.toString()));
        } while (false);

        return formatted;
    }

    /**
     * Reports that formatting the value failed but the formatter did not throw an exception.
     */
    private IllegalArgumentException formatFail(final Object value, final Object pattern) {
        return new IllegalArgumentException("Unable to format " + CharSequences.quoteIfChars(value));
    }

    private final SpreadsheetFormatterContext spreadsheetFormatterContext;

    @Override
    public String toString() {
        return this.spreadsheetFormatterContext.toString();
    }
}
