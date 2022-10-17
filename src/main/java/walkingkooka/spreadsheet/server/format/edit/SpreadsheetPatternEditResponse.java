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

package walkingkooka.spreadsheet.server.format.edit;

import walkingkooka.ToStringBuilder;
import walkingkooka.UsesToStringBuilder;
import walkingkooka.collect.list.Lists;
import walkingkooka.text.CharSequences;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;
import java.util.Objects;

/**
 * A request from the UI that accepts a pattern as text with value values as text. This will be tokenized and
 * the value values formatted.
 */
public final class SpreadsheetPatternEditResponse implements TreePrintable, UsesToStringBuilder {

    public final static List<SpreadsheetPatternEditToken> EMPTY_TOKENS = Lists.empty();

    public final static List<String> EMPTY_VALUES = Lists.empty();

    public static SpreadsheetPatternEditResponse with(final List<SpreadsheetPatternEditToken> tokens,
                                                      final List<String> values) {
        return new SpreadsheetPatternEditResponse(
                Objects.requireNonNull(tokens, "tokens"),
                Objects.requireNonNull(values, "values")
        );
    }

    private SpreadsheetPatternEditResponse(final List<SpreadsheetPatternEditToken> tokens,
                                           final List<String> values) {
        this.tokens = Lists.immutable(tokens);
        this.values = Lists.immutable(values);
    }

    /**
     * The tokens for the patterns provided in the request.
     */
    public List<SpreadsheetPatternEditToken> tokens() {
        return this.tokens;
    }

    private final List<SpreadsheetPatternEditToken> tokens;

    /**
     * Getter that returns the values formatted/parsed
     */
    public List<String> values() {
        return this.values;
    }

    private final List<String> values;

    // Object..........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
                this.tokens,
                this.values
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof SpreadsheetPatternEditResponse &&
                        this.equals0((SpreadsheetPatternEditResponse) other);
    }

    private boolean equals0(final SpreadsheetPatternEditResponse other) {
        return this.tokens.equals(other.tokens) &&
                this.values.equals(other.values);
    }

    @Override
    public String toString() {
        return ToStringBuilder.buildFrom(this);
    }

    @Override
    public void buildToString(final ToStringBuilder builder) {
        builder.value(this.tokens)
                .value(this.values);
    }

    // json.............................................................................................................

    private final static String KIND_STRING = "tokens";
    private final static String VALUES_STRING = "values";

    final static JsonPropertyName KIND_PROPERTY = JsonPropertyName.with(KIND_STRING);
    final static JsonPropertyName VALUES_PROPERTY = JsonPropertyName.with(VALUES_STRING);

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .set(KIND_PROPERTY, context.marshallCollection(this.tokens))
                .set(VALUES_PROPERTY, context.marshallCollection(this.values));
    }

    static SpreadsheetPatternEditResponse unmarshall(final JsonNode json,
                                                     final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(json, "json");

        List<SpreadsheetPatternEditToken> tokens = EMPTY_TOKENS;
        List<String> values = EMPTY_VALUES;

        for (final JsonNode child : json.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case KIND_STRING:
                    tokens = context.unmarshallList(
                            child,
                            SpreadsheetPatternEditToken.class
                    );
                    break;
                case VALUES_STRING:
                    values = context.unmarshallList(
                            child,
                            String.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, json);
                    break;
            }
        }

        return with(tokens, values);
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetPatternEditResponse.class),
                SpreadsheetPatternEditResponse::unmarshall,
                SpreadsheetPatternEditResponse::marshall,
                SpreadsheetPatternEditResponse.class
        );
    }

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println("tokens");
        printer.indent();
        {
            for (final SpreadsheetPatternEditToken token : this.tokens()) {
                token.printTree(printer);
            }
        }
        printer.outdent();

        final List<String> values = this.values();
        if (!values.isEmpty()) {
            printer.println("values");
            printer.indent();
            {
                printer.indent();
                {
                    for (final String valueValue : values) {
                        printer.println(CharSequences.quoteAndEscape(valueValue));
                    }
                }

                printer.outdent();
            }
            printer.outdent();
        }
    }
}
