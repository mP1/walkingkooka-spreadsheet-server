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
import walkingkooka.ToStringBuilderOption;
import walkingkooka.UsesToStringBuilder;
import walkingkooka.text.CharSequences;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Objects;

/**
 * Represents a token within a pattern. The response to the service will parse a String into a {@link walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern}
 * and then return the individual tokens within the pattern for the UI to display and the user edit.
 */
public final class SpreadsheetPatternEditToken implements TreePrintable, UsesToStringBuilder {

    public static SpreadsheetPatternEditToken with(final SpreadsheetPatternEditTokenKind kind,
                                                   final String pattern) {
        return new SpreadsheetPatternEditToken(
                Objects.requireNonNull(kind, "kind"),
                CharSequences.failIfNullOrEmpty(pattern, "pattern")
        );
    }

    private SpreadsheetPatternEditToken(final SpreadsheetPatternEditTokenKind kind,
                                        final String pattern) {
        this.kind = kind;
        this.pattern = pattern;
    }

    /**
     * The pattern component type
     */
    public SpreadsheetPatternEditTokenKind kind() {
        return this.kind;
    }

    private final SpreadsheetPatternEditTokenKind kind;

    /**
     * Part of a larger pattern wholly represented by this token.
     */
    public String pattern() {
        return this.pattern;
    }

    private final String pattern;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
                this.kind,
                this.pattern
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof SpreadsheetPatternEditToken &&
                        this.equals0((SpreadsheetPatternEditToken) other);
    }

    private boolean equals0(final SpreadsheetPatternEditToken other) {
        return this.kind.equals(other.kind) &&
                this.pattern.equals(other.pattern);
    }

    @Override
    public String toString() {
        return ToStringBuilder.buildFrom(this);
    }

    @Override
    public void buildToString(final ToStringBuilder builder) {
        builder.disable(ToStringBuilderOption.QUOTE)
                .value(this.kind)
                .enable(ToStringBuilderOption.QUOTE)
                .value(this.pattern);
    }

    // json.............................................................................................................

    private final static String KIND_STRING = "kind";
    private final static String PATTERN_STRING = "pattern";

    final static JsonPropertyName KIND_PROPERTY = JsonPropertyName.with(KIND_STRING);
    final static JsonPropertyName PATTERN_PROPERTY = JsonPropertyName.with(PATTERN_STRING);

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .set(KIND_PROPERTY, context.marshall(this.kind.name()))
                .set(PATTERN_PROPERTY, context.marshall(this.pattern));
    }

    static SpreadsheetPatternEditToken unmarshall(final JsonNode json,
                                                  final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(json, "json");

        SpreadsheetPatternEditTokenKind kind = null;
        String pattern = null;

        for (final JsonNode child : json.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case KIND_STRING:
                    kind = SpreadsheetPatternEditTokenKind.valueOf(
                            child.stringOrFail()
                    );
                    break;
                case PATTERN_STRING:
                    pattern = context.unmarshall(
                            child,
                            String.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, json);
                    break;
            }
        }

        if (null == kind) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(KIND_PROPERTY, json);
        }
        if (null == pattern) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(PATTERN_PROPERTY, json);
        }

        return with(
                kind,
                pattern
        );
    }

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetPatternEditToken.class),
                SpreadsheetPatternEditToken::unmarshall,
                SpreadsheetPatternEditToken::marshall,
                SpreadsheetPatternEditToken.class
        );
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.kind.name());

        printer.indent();
        {
            printer.println(CharSequences.quoteAndEscape(this.pattern));
        }
        printer.outdent();
    }
}
