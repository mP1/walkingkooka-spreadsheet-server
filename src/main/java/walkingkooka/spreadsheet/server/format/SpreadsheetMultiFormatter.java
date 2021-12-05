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

import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.HasSpreadsheetFormatter;
import walkingkooka.text.CharSequences;

import java.time.LocalDateTime;
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
final class SpreadsheetMultiFormatter implements Function<SpreadsheetMultiFormatRequest, SpreadsheetMultiFormatResponse> {

    static SpreadsheetMultiFormatter with(final SpreadsheetEngineContext engineContext) {
        Objects.requireNonNull(engineContext, "engineContext");

        return new SpreadsheetMultiFormatter(engineContext);
    }

    private SpreadsheetMultiFormatter(final SpreadsheetEngineContext engineContext) {
        super();
        this.engineContext = engineContext;
    }

    @Override
    public SpreadsheetMultiFormatResponse apply(final SpreadsheetMultiFormatRequest request) {
        Objects.requireNonNull(request, "request");

        return SpreadsheetMultiFormatResponse.with(
                request.requests()
                        .stream()
                        .map(this::formatOrThrowable)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Returns the formatted value or the thrown {@link Throwable}.
     */
    private Object formatOrThrowable(final SpreadsheetFormatRequest request) {
        Object result;
        try {
            result = this.format(request);
        } catch (final Throwable cause) {
            result = cause;
        }
        return result;
    }

    /**
     * Handles the request returning the formatted value or throwing a {@link Exception} if formatting failed.
     */
    private Object format(final SpreadsheetFormatRequest request) {
        Object formatted;

        do {
            final Object value = request.value();
            final Object pattern = request.pattern();

            if (pattern instanceof HasSpreadsheetFormatter) {
                final HasSpreadsheetFormatter hasSpreadsheetFormatter = (HasSpreadsheetFormatter) pattern;
                formatted = this.engineContext.format(value, hasSpreadsheetFormatter.formatter())
                        .orElseThrow(() -> this.formatFail(value, pattern));
                break;
            }
            if (pattern instanceof SpreadsheetLocaleDefaultDateTimeFormat) {
                final SpreadsheetLocaleDefaultDateTimeFormat format = (SpreadsheetLocaleDefaultDateTimeFormat) pattern;
                formatted = format.format(
                        (LocalDateTime) value,
                        this.engineContext.metadata()
                                .formatterContext()
                );
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
        return new IllegalArgumentException("Unable to format " + CharSequences.quoteIfChars(value) + " with " + CharSequences.quoteIfChars(pattern));
    }

    private final SpreadsheetEngineContext engineContext;

    @Override
    public String toString() {
        return this.engineContext.toString();
    }

    static {
        SpreadsheetMultiFormatRequest.init();
        SpreadsheetMultiFormatResponse.init();
    }
}
