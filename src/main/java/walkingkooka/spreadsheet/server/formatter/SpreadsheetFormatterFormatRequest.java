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

package walkingkooka.spreadsheet.server.formatter;

import walkingkooka.ToStringBuilder;
import walkingkooka.Value;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Objects;

/**
 * An individual item with a list of requests to the POST format API.
 */
public final class SpreadsheetFormatterFormatRequest<T> implements Value<T> {

    public static <T> SpreadsheetFormatterFormatRequest<T> with(final SpreadsheetFormatterSelector selector,
                                                                final T value) {
        return new SpreadsheetFormatterFormatRequest<>(
            Objects.requireNonNull(selector, "selector"),
            value
        );
    }

    private SpreadsheetFormatterFormatRequest(final SpreadsheetFormatterSelector selector,
                                              final T value) {
        this.selector = selector;
        this.value = value;
    }

    public SpreadsheetFormatterSelector selector() {
        return this.selector;
    }

    private final SpreadsheetFormatterSelector selector;

    @Override
    public T value() {
        return this.value;
    }

    private final T value;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
            this.selector,
            this.value
        );
    }

    @Override
    public boolean equals(final Object other) {
        return other == this ||
            other instanceof SpreadsheetFormatterFormatRequest && this.equals0((SpreadsheetFormatterFormatRequest<?>) other);
    }

    private boolean equals0(final SpreadsheetFormatterFormatRequest<?> other) {
        return this.selector.equals(other.selector) &&
            Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return ToStringBuilder.empty()
            .value(this.selector)
            .value(this.value)
            .build();
    }

    // json.............................................................................................................

    static SpreadsheetFormatterFormatRequest<?> unmarshall(final JsonNode node,
                                                           final JsonNodeUnmarshallContext context) {
        SpreadsheetFormatterSelector selector = null;
        Object value = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case SELECTOR_PROPERTY_STRING:
                    selector = context.unmarshall(
                        child,
                        SpreadsheetFormatterSelector.class
                    );
                    break;
                case VALUE_PROPERTY_STRING:
                    value = context.unmarshallWithType(child);
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        if (null == selector) {
            JsonNodeUnmarshallContext.missingProperty(SELECTOR_PROPERTY, node);
        }
        return new SpreadsheetFormatterFormatRequest<Object>(
            selector,
            value
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        JsonObject object = JsonNode.object()
            .set(SELECTOR_PROPERTY, context.marshall(this.selector));

        final T value = this.value;
        if (null != value) {
            object = object.set(
                VALUE_PROPERTY,
                context.marshallWithType(value)
            );
        }

        return object;
    }

    private final static String SELECTOR_PROPERTY_STRING = "selector";
    private final static String VALUE_PROPERTY_STRING = "value";

    // @VisibleForTesting
    final static JsonPropertyName SELECTOR_PROPERTY = JsonPropertyName.with(SELECTOR_PROPERTY_STRING);
    final static JsonPropertyName VALUE_PROPERTY = JsonPropertyName.with(VALUE_PROPERTY_STRING);

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(SpreadsheetFormatterFormatRequest.class),
            SpreadsheetFormatterFormatRequest::unmarshall,
            SpreadsheetFormatterFormatRequest::marshall,
            SpreadsheetFormatterFormatRequest.class
        );
    }
}
