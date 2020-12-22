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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Objects;

/**
 * Represents a single request to format a value using a compatible pattern. Examples might include a {@link java.time.LocalDate} and a {@link walkingkooka.spreadsheet.format.pattern.SpreadsheetDateFormatPattern}.
 */
public final class FormatRequest {

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

    private final static String VALUE_STRING = "value";
    private final static String PATTERN_STRING = "pattern";

    final static JsonPropertyName VALUE_PROPERTY = JsonPropertyName.with(VALUE_STRING);
    final static JsonPropertyName PATTERN_PROPERTY = JsonPropertyName.with(PATTERN_STRING);


    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .set(VALUE_PROPERTY, context.marshallWithType(this.value))
                .set(PATTERN_PROPERTY, context.marshallWithType(this.pattern));
    }

    static FormatRequest unmarshall(final JsonNode node,
                                    final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        Object value = null;
        Object pattern = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case VALUE_STRING:
                    value = context.unmarshallWithType(child);
                    break;
                case PATTERN_STRING:
                    pattern = context.unmarshallWithType(child);
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        if (null == value) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(VALUE_PROPERTY, node);
        }
        if (null == pattern) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(PATTERN_PROPERTY, node);
        }
        return with(value, pattern);
    }

    static {
        JsonNodeContext.register("spreadsheet-format-request",
                FormatRequest::unmarshall,
                FormatRequest::marshall,
                FormatRequest.class);
    }
}
