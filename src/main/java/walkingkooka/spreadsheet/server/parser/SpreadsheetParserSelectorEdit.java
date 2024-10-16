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

package walkingkooka.spreadsheet.server.parser;

import walkingkooka.Cast;
import walkingkooka.InvalidCharacterException;
import walkingkooka.ToStringBuilder;
import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviderSamplesContexts;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSampleList;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.parser.SpreadsheetParser;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelector;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorTokenList;
import walkingkooka.text.CharSequences;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.text.TextNode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A collection of components that are useful for a UI that accepts entry of a {@link SpreadsheetParserSelector}.
 */
public final class SpreadsheetParserSelectorEdit implements TreePrintable {

    public static SpreadsheetParserSelectorEdit parse(final String selector,
                                                      final SpreadsheetParserSelectorEditContext context) {
        Objects.requireNonNull(selector, "selector");
        Objects.requireNonNull(context, "context");

        SpreadsheetParserSelector spreadsheetParserSelector = null;
        String message = "";
        List<SpreadsheetParserSelectorToken> tokens = Lists.empty();
        Optional<SpreadsheetParserSelectorToken> next = Optional.empty();
        List<SpreadsheetFormatterSample> samples = Lists.empty();

        try {
            spreadsheetParserSelector = SpreadsheetParserSelector.parse(selector);

            try {
                final Optional<SpreadsheetFormatterSelector> maybeSpreadsheetFormatterSelector = context.spreadsheetFormatterSelector(spreadsheetParserSelector);
                if (maybeSpreadsheetFormatterSelector.isPresent()) {
                    samples = context.spreadsheetFormatterSamples(
                            maybeSpreadsheetFormatterSelector.get()
                                    .name(),
                            SpreadsheetFormatterProviderSamplesContexts.basic(context)
                    );
                }
            } catch (final RuntimeException ignore) {
                // ignored!
            }

            final SpreadsheetParser parser = context.spreadsheetParser(
                    spreadsheetParserSelector,
                    context
            );

            try {
                tokens = parser.tokens(context);
                next = context.spreadsheetParserNextToken(spreadsheetParserSelector);
            } catch (final InvalidCharacterException cause) {
                message = cause.setTextAndPosition(
                        selector,
                        spreadsheetParserSelector.name().textLength() + cause.position()
                ).getMessage();
            } catch (final IllegalArgumentException ignore) {
                // nop
            }
        } catch (final IllegalArgumentException cause) {
            message = cause.getMessage();
        }

        return SpreadsheetParserSelectorEdit.with(
                Optional.ofNullable(spreadsheetParserSelector),
                message,
                tokens,
                next,
                samples
        );
    }

    static SpreadsheetParserSelectorEdit with(final Optional<SpreadsheetParserSelector> selector,
                                              final String message,
                                              final List<SpreadsheetParserSelectorToken> tokens,
                                              final Optional<SpreadsheetParserSelectorToken> next,
                                              final List<SpreadsheetFormatterSample> samples) {
        return new SpreadsheetParserSelectorEdit(
                Objects.requireNonNull(selector, "selector"),
                Objects.requireNonNull(message, "message"),
                SpreadsheetParserSelectorTokenList.with(
                        Objects.requireNonNull(tokens, "tokens")
                ),
                Objects.requireNonNull(next, "next"),
                SpreadsheetFormatterSampleList.with(
                        Objects.requireNonNull(samples, "samples")
                )
        );
    }

    private SpreadsheetParserSelectorEdit(final Optional<SpreadsheetParserSelector> selector,
                                          final String message,
                                          final List<SpreadsheetParserSelectorToken> tokens,
                                          final Optional<SpreadsheetParserSelectorToken> next,
                                          final List<SpreadsheetFormatterSample> samples) {
        this.selector = selector;
        this.message = message;
        this.tokens = tokens;
        this.next = next;
        this.samples = samples;
    }

    public Optional<SpreadsheetParserSelector> selector() {
        return this.selector;
    }

    private final Optional<SpreadsheetParserSelector> selector;

    public String message() {
        return this.message;
    }

    private final String message;

    public List<SpreadsheetParserSelectorToken> tokens() {
        return this.tokens;
    }

    private List<SpreadsheetParserSelectorToken> tokens;

    public Optional<SpreadsheetParserSelectorToken> next() {
        return this.next;
    }

    private final Optional<SpreadsheetParserSelectorToken> next;

    public List<SpreadsheetFormatterSample> samples() {
        return this.samples;
    }

    private final List<SpreadsheetFormatterSample> samples;

    // HashCodeEqualsDefined..........................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
                this.selector,
                this.message,
                this.tokens,
                this.next,
                this.samples
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof SpreadsheetParserSelectorEdit &&
                        this.equals0(Cast.to(other));
    }

    private boolean equals0(final SpreadsheetParserSelectorEdit other) {
        return this.selector.equals(other.selector) &&
                this.message.equals(other.message) &&
                this.tokens.equals(other.tokens) &&
                this.next.equals(other.next) &&
                this.samples.equals(other.samples);
    }

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .value(this.selector)
                .value(this.message)
                .value(this.tokens)
                .value(this.next)
                .value(this.samples)
                .build();
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        Objects.requireNonNull(printer, "printer");

        printer.println(this.getClass().getSimpleName());
        printer.indent();
        {

            if (this.selector.isPresent()) {
                printer.println("selector");
                printer.indent();
                {
                    this.selector.get()
                            .printTree(printer);
                }
                printer.outdent();
            }

            final String message = this.message;
            if (false == CharSequences.isNullOrEmpty(message)) {
                printer.println("message");
                printer.indent();
                {
                    printer.println(message);
                }
                printer.outdent();
            }

            if (this.tokens.isNotEmpty()) {
                printer.println("text-components");
                printer.indent();
                {
                    for (final SpreadsheetParserSelectorToken textComponent : this.tokens) {
                        textComponent.printTree(printer);
                    }
                }
                printer.outdent();
            }

            if (this.next.isPresent()) {
                printer.println("next");
                printer.indent();
                {
                    this.next.get()
                            .printTree(printer);
                }
                printer.outdent();
            }

            if (this.samples.isNotEmpty()) {
                printer.println("samples");
                printer.indent();
                {
                    for (final SpreadsheetFormatterSample sample : this.samples) {
                        sample.printTree(printer);
                    }
                }
                printer.outdent();
            }
        }

        printer.outdent();
    }

    // JsonNodeContext..................................................................................................

    /**
     * Factory that creates a {@link SpreadsheetParserSelectorEdit} parse a {@link JsonNode}.
     */
    static SpreadsheetParserSelectorEdit unmarshall(final JsonNode node,
                                                    final JsonNodeUnmarshallContext context) {
        SpreadsheetParserSelector selector = null;
        String message = null;
        List<SpreadsheetParserSelectorToken> tokens = null;
        SpreadsheetParserSelectorToken next = null;
        List<SpreadsheetFormatterSample> samples = null;

        for (JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case SELECTOR_PROPERTY_STRING:
                    selector = context.unmarshall(
                            child,
                            SpreadsheetParserSelector.class
                    );
                    break;
                case MESSAGE_PROPERTY_STRING:
                    message = context.unmarshall(
                            child,
                            String.class
                    );
                    break;
                case TOKENS_PROPERTY_STRING:
                    tokens = context.unmarshall(
                            child,
                            SpreadsheetParserSelectorTokenList.class
                    );
                    break;
                case NEXT_PROPERTY_STRING:
                    next = context.unmarshall(
                            child,
                            SpreadsheetParserSelectorToken.class
                    );
                    break;
                case SAMPLES_PROPERTY_STRING:
                    samples = context.unmarshall(
                            child,
                            SpreadsheetFormatterSampleList.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        return with(
                Optional.ofNullable(selector),
                message,
                tokens,
                Optional.ofNullable(next),
                samples
        );
    }

    /**
     * <pre>
     * {
     *   "selector": "date-parse-pattern dd/mm/yyyy",
     *   "message": "",
     *   "tokens": [
     *     {
     *       "label": "dd",
     *       "text": "dd",
     *       "alternatives": [
     *         {
     *           "label": "d",
     *           "text": "d"
     *         },
     *         {
     *           "label": "ddd",
     *           "text": "ddd"
     *         },
     *         {
     *           "label": "dddd",
     *           "text": "dddd"
     *         }
     *       ]
     *     },
     *     {
     *       "label": "/",
     *       "text": "/"
     *     },
     *     {
     *       "label": "mm",
     *       "text": "mm",
     *       "alternatives": [
     *         {
     *           "label": "m",
     *           "text": "m"
     *         },
     *         {
     *           "label": "mmm",
     *           "text": "mmm"
     *         },
     *         {
     *           "label": "mmmm",
     *           "text": "mmmm"
     *         },
     *         {
     *           "label": "mmmmm",
     *           "text": "mmmmm"
     *         }
     *       ]
     *     },
     *     {
     *       "label": "/",
     *       "text": "/"
     *     },
     *     {
     *       "label": "yyyy",
     *       "text": "yyyy",
     *       "alternatives": [
     *         {
     *           "label": "yy",
     *           "text": "yy"
     *         }
     *       ]
     *     }
     *   ],
     *   "next": {
     *     "alternatives": [
     *       {
     *         "label": "d",
     *         "text": "d"
     *       },
     *       {
     *         "label": "dd",
     *         "text": "dd"
     *       },
     *       {
     *         "label": "ddd",
     *         "text": "ddd"
     *       },
     *       {
     *         "label": "dddd",
     *         "text": "dddd"
     *       },
     *       {
     *         "label": "m",
     *         "text": "m"
     *       },
     *       {
     *         "label": "mm",
     *         "text": "mm"
     *       },
     *       {
     *         "label": "mmm",
     *         "text": "mmm"
     *       },
     *       {
     *         "label": "mmmm",
     *         "text": "mmmm"
     *       },
     *       {
     *         "label": "mmmmm",
     *         "text": "mmmmm"
     *       }
     *     ]
     *   },
     *   "samples": [
     *     {
     *       "label": "Short",
     *       "selector": "date-format-pattern d/m/yy",
     *       "value": {
     *         "type": "text",
     *         "value": "31/12/99"
     *       }
     *     },
     *     {
     *       "label": "Medium",
     *       "selector": "date-format-pattern d mmm yyyy",
     *       "value": {
     *         "type": "text",
     *         "value": "31 Dec. 1999"
     *       }
     *     },
     *     {
     *       "label": "Long",
     *       "selector": "date-format-pattern d mmmm yyyy",
     *       "value": {
     *         "type": "text",
     *         "value": "31 December 1999"
     *       }
     *     },
     *     {
     *       "label": "Full",
     *       "selector": "date-format-pattern dddd, d mmmm yyyy",
     *       "value": {
     *         "type": "text",
     *         "value": "Friday, 31 December 1999"
     *       }
     *     }
     *   ]
     * }
     * </pre>
     */
    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .setChildren(
                        Lists.of(
                                context.marshall(this.selector.orElse(null)).setName(SELECTOR_PROPERTY),
                                context.marshall(this.message).setName(MESSAGE_PROPERTY),
                                context.marshall(this.tokens).setName(TOKENS_PROPERTY),
                                context.marshall(this.next.orElse(null)).setName(NEXT_PROPERTY),
                                context.marshall(this.samples).setName(SAMPLES_PROPERTY)
                        )
                );
    }

    private final static String SELECTOR_PROPERTY_STRING = "selector";
    private final static String MESSAGE_PROPERTY_STRING = "message";
    private final static String TOKENS_PROPERTY_STRING = "tokens";
    private final static String NEXT_PROPERTY_STRING = "next";
    private final static String SAMPLES_PROPERTY_STRING = "samples";

    // @VisibleForTesting

    final static JsonPropertyName SELECTOR_PROPERTY = JsonPropertyName.with(SELECTOR_PROPERTY_STRING);
    final static JsonPropertyName MESSAGE_PROPERTY = JsonPropertyName.with(MESSAGE_PROPERTY_STRING);
    final static JsonPropertyName TOKENS_PROPERTY = JsonPropertyName.with(TOKENS_PROPERTY_STRING);
    final static JsonPropertyName NEXT_PROPERTY = JsonPropertyName.with(NEXT_PROPERTY_STRING);

    final static JsonPropertyName SAMPLES_PROPERTY = JsonPropertyName.with(SAMPLES_PROPERTY_STRING);

    static {
        SpreadsheetParserSelectorTokenList.with(Lists.empty());
        SpreadsheetFormatterSampleList.with(
                Lists.of(
                        SpreadsheetFormatterSample.with(
                                "Label",
                                SpreadsheetFormatterSelector.DEFAULT_TEXT_FORMAT,
                                TextNode.text("123")
                        )
                )
        );

        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetParserSelectorEdit.class),
                SpreadsheetParserSelectorEdit::unmarshall,
                SpreadsheetParserSelectorEdit::marshall,
                SpreadsheetParserSelectorEdit.class
        );
    }
}
