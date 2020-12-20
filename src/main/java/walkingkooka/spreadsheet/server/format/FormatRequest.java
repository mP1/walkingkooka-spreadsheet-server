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

import walkingkooka.text.CharSequences;

import java.util.Objects;

/**
 * Represents a single request to format a value using a compatible pattern. Examples might include a {@link java.time.LocalDate} and a {@link walkingkooka.spreadsheet.format.pattern.SpreadsheetDateFormatPattern}.
 */
final class FormatRequest {

    static FormatRequest with(final Object value, final Object pattern) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(pattern, "pattern");

        return new FormatRequest(value, pattern);
    }

    private FormatRequest(final Object value, final Object pattern) {
        this.value = value;
        this.pattern = pattern;
    }

    Object value() {
        return this.value;
    }

    private final Object value;

    Object pattern() {
        return this.pattern;
    }

    private final Object pattern;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(this.value(), this.pattern());
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof FormatRequest && this.equals0((FormatRequest) other);
    }

    private boolean equals0(final FormatRequest other) {
        return this.value.equals(other.value) &&
                this.pattern.equals(other.pattern);
    }

    @Override
    public String toString() {
        return CharSequences.quoteIfChars(this.value()) + " " + this.pattern();
    }
}
