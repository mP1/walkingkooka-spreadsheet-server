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
import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPatternKind;
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
 * A request from the UI that accepts a pattern as text with sample values as text. This will be tokenized and
 * the sample values formatted.
 */
public final class SpreadsheetPatternEditRequest implements TreePrintable, UsesToStringBuilder {

    public static SpreadsheetPatternEditRequest with(final SpreadsheetPatternKind kind,
                                                     final String pattern,
                                                     final List<String> samples) {
        return new SpreadsheetPatternEditRequest(
                Objects.requireNonNull(kind, "kind"),
                Objects.requireNonNull(pattern, "pattern"),
                Objects.requireNonNull(samples, "samples")
        );
    }

    private SpreadsheetPatternEditRequest(final SpreadsheetPatternKind kind,
                                          final String pattern,
                                          final List<String> samples) {
        this.kind = kind;
        this.pattern = pattern;
        this.samples = Lists.immutable(samples);
    }

    /**
     * The pattern type
     */
    public SpreadsheetPatternKind kind() {
        return this.kind;
    }

    private final SpreadsheetPatternKind kind;

    /**
     * Then unparsed pattern which will be parsed and tokenized.
     */
    public String pattern() {
        return this.pattern;
    }

    private final String pattern;

    /**
     * Getter that returns the values to be formatted and displayed as samples.
     */
    public List<String> samples() {
        return this.samples;
    }

    private final List<String> samples;

    @Override
    public int hashCode() {
        return Objects.hash(
                this.kind,
                this.pattern,
                this.samples
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof SpreadsheetPatternEditRequest &&
                        this.equals0((SpreadsheetPatternEditRequest) other);
    }

    private boolean equals0(final SpreadsheetPatternEditRequest other) {
        return this.kind.equals(other.kind) &&
                this.pattern.equals(other.pattern) &&
                this.samples.equals(other.samples);
    }

    @Override
    public String toString() {
        return ToStringBuilder.buildFrom(this);
    }

    @Override
    public void buildToString(final ToStringBuilder builder) {
        builder.disable(ToStringBuilderOption.QUOTE)
                .value(this.kind.typeName())
                .enable(ToStringBuilderOption.QUOTE)
                .value(this.pattern)
                .value(this.samples);
    }

    // json.............................................................................................................

    private final static String KIND_STRING = "kind";
    private final static String PATTERN_STRING = "pattern";
    private final static String SAMPLES_STRING = "samples";

    final static JsonPropertyName KIND_PROPERTY = JsonPropertyName.with(KIND_STRING);
    final static JsonPropertyName PATTERN_PROPERTY = JsonPropertyName.with(PATTERN_STRING);
    final static JsonPropertyName SAMPLES_PROPERTY = JsonPropertyName.with(SAMPLES_STRING);

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .set(KIND_PROPERTY, context.marshall(this.kind.typeName()))
                .set(PATTERN_PROPERTY, context.marshall(this.pattern))
                .set(SAMPLES_PROPERTY, context.marshallCollection(this.samples));
    }

    static SpreadsheetPatternEditRequest unmarshall(final JsonNode json,
                                                    final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(json, "json");

        SpreadsheetPatternKind kind = null;
        String pattern = null;
        List<String> samples = null;

        for (final JsonNode child : json.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case KIND_STRING:
                    kind = SpreadsheetPatternKind.fromTypeName(
                            child.stringOrFail()
                    );
                    break;
                case PATTERN_STRING:
                    pattern = context.unmarshall(
                            child,
                            String.class
                    );
                    break;
                case SAMPLES_STRING:
                    samples = context.unmarshallList(
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
        if (null == samples) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(PATTERN_PROPERTY, json);
        }

        return with(kind, pattern, samples);
    }

    static {
        SpreadsheetPatternKind.DATE_PARSE_PATTERN.toString();

        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetPatternEditRequest.class),
                SpreadsheetPatternEditRequest::unmarshall,
                SpreadsheetPatternEditRequest::marshall,
                SpreadsheetPatternEditRequest.class
        );
    }

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.kind.typeName());

        printer.indent();
        {
            printer.println(CharSequences.quoteAndEscape(this.pattern));

            printer.indent();
            {
                for (final String sampleValue : this.samples) {
                    printer.println(CharSequences.quoteAndEscape(sampleValue));
                }
            }

            printer.outdent();
        }
        printer.outdent();
    }
}
