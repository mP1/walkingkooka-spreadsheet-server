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

import walkingkooka.text.CharSequences;

import java.util.Objects;

/**
 * Represents a single request to pattern a text using a compatible pattern. Examples might include a {@link String} and the type name.
 */
final class ParseRequest {

    static ParseRequest with(final String text, final String parser) {
        Objects.requireNonNull(text, "text");
        CharSequences.failIfNullOrEmpty(parser, "parser");

        return new ParseRequest(text, parser);
    }

    private ParseRequest(final String text, final String parser) {
        this.text = text;
        this.parser = parser;
    }

    String text () {
        return this.text;
    }

    private final String text ;

    String parser() {
        return this.parser;
    }

    private final String parser;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(this.text(), this.parser());
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof ParseRequest && this.equals0((ParseRequest) other);
    }

    private boolean equals0(final ParseRequest other) {
        return this.text.equals(other.text) &&
                this.parser.equals(other.parser);
    }

    @Override
    public String toString() {
        return CharSequences.quoteAndEscape(this.text()) + " " + CharSequences.quoteAndEscape(this.parser());
    }
}
