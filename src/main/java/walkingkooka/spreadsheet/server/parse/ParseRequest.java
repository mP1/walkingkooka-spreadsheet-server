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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

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

    // Json.............................................................................................................

    private final static String TEXT_STRING = "text";
    private final static String PARSER_STRING = "parser";

    final static JsonPropertyName TEXT_PROPERTY = JsonPropertyName.with(TEXT_STRING);
    final static JsonPropertyName PARSER_PROPERTY = JsonPropertyName.with(PARSER_STRING);

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .set(TEXT_PROPERTY, context.marshall(this.text))
                .set(PARSER_PROPERTY, context.marshall(this.parser));
    }

    static ParseRequest unmarshall(final JsonNode node,
                                   final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        String text = null;
        String parser = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case TEXT_STRING:
                    text = child.stringOrFail();
                    break;
                case PARSER_STRING:
                    parser = child.stringOrFail();
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        if (null == text) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(TEXT_PROPERTY, node);
        }
        if (null == parser) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(PARSER_PROPERTY, node);
        }
        return with(text, parser);
    }

    static {
        JsonNodeContext.register("spreadsheet-parse-request",
                ParseRequest::unmarshall,
                ParseRequest::marshall,
                ParseRequest.class);
    }
}
