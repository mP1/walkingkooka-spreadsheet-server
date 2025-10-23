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

import walkingkooka.Cast;
import walkingkooka.InvalidCharacterException;
import walkingkooka.ToStringBuilder;
import walkingkooka.collect.list.Lists;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatter;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterProviderSamplesContexts;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSample;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSampleList;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelectorTokenList;
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
 * A collection of components that are useful for a UI that accepts entry of a {@link SpreadsheetFormatterSelector}.
 */
public final class SpreadsheetFormatterSelectorEdit implements TreePrintable {

    public static SpreadsheetFormatterSelectorEdit parse(final String selector,
                                                         final SpreadsheetFormatterSelectorEditContext context) {
        Objects.requireNonNull(selector, "selector");
        Objects.requireNonNull(context, "context");

        SpreadsheetFormatterSelector spreadsheetFormatterSelector = null;
        String message = "";
        List<SpreadsheetFormatterSelectorToken> tokens = Lists.empty();
        Optional<SpreadsheetFormatterSelectorToken> next = Optional.empty();
        List<SpreadsheetFormatterSample> samples = Lists.empty();

        try {
            spreadsheetFormatterSelector = SpreadsheetFormatterSelector.parse(selector);

            final ProviderContext providerContext = context.providerContext();

            samples = context.spreadsheetFormatterSamples(
                spreadsheetFormatterSelector,
                SpreadsheetFormatterProvider.INCLUDE_SAMPLES,
                SpreadsheetFormatterProviderSamplesContexts.basic(
                    context,
                    providerContext
                )
            );

            final SpreadsheetFormatter formatter = context.spreadsheetFormatter(
                spreadsheetFormatterSelector,
                providerContext
            );
            try {
                tokens = formatter.tokens(context);
                next = context.spreadsheetFormatterNextToken(spreadsheetFormatterSelector);
            } catch (final InvalidCharacterException cause) {
                message = cause.setTextAndPosition(
                    selector,
                    spreadsheetFormatterSelector.name().textLength() + cause.position()
                ).getMessage();
            } catch (final IllegalArgumentException ignore) {
                // nop
            }
        } catch (final IllegalArgumentException cause) {
            message = cause.getMessage();
        }

        return SpreadsheetFormatterSelectorEdit.with(
            Optional.ofNullable(spreadsheetFormatterSelector),
            message,
            tokens,
            next,
            samples
        );
    }

    static SpreadsheetFormatterSelectorEdit with(final Optional<SpreadsheetFormatterSelector> selector,
                                                 final String message,
                                                 final List<SpreadsheetFormatterSelectorToken> tokens,
                                                 final Optional<SpreadsheetFormatterSelectorToken> next,
                                                 final List<SpreadsheetFormatterSample> samples) {
        return new SpreadsheetFormatterSelectorEdit(
            Objects.requireNonNull(selector, "selector"),
            Objects.requireNonNull(message, "message"),
            SpreadsheetFormatterSelectorTokenList.with(
                Objects.requireNonNull(tokens, "tokens")
            ),
            Objects.requireNonNull(next, "next"),
            SpreadsheetFormatterSampleList.with(
                Objects.requireNonNull(samples, "samples")
            )
        );
    }

    private SpreadsheetFormatterSelectorEdit(final Optional<SpreadsheetFormatterSelector> selector,
                                             final String message,
                                             final List<SpreadsheetFormatterSelectorToken> tokens,
                                             final Optional<SpreadsheetFormatterSelectorToken> next,
                                             final List<SpreadsheetFormatterSample> samples) {
        this.selector = selector;
        this.message = message;
        this.tokens = tokens;
        this.next = next;
        this.samples = samples;
    }

    public Optional<SpreadsheetFormatterSelector> selector() {
        return this.selector;
    }

    private final Optional<SpreadsheetFormatterSelector> selector;

    public String message() {
        return this.message;
    }

    private final String message;

    public List<SpreadsheetFormatterSelectorToken> tokens() {
        return this.tokens;
    }

    private final List<SpreadsheetFormatterSelectorToken> tokens;

    public Optional<SpreadsheetFormatterSelectorToken> next() {
        return this.next;
    }

    private final Optional<SpreadsheetFormatterSelectorToken> next;

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
            other instanceof SpreadsheetFormatterSelectorEdit &&
                this.equals0(Cast.to(other));
    }

    private boolean equals0(final SpreadsheetFormatterSelectorEdit other) {
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

            if (false == this.tokens.isEmpty()) {
                printer.println("text-components");
                printer.indent();
                {
                    for (final SpreadsheetFormatterSelectorToken textComponent : this.tokens) {
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

            if (false == this.samples.isEmpty()) {
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
     * Factory that creates a {@link SpreadsheetFormatterSelectorEdit} parse a {@link JsonNode}.
     */
    static SpreadsheetFormatterSelectorEdit unmarshall(final JsonNode node,
                                                       final JsonNodeUnmarshallContext context) {
        Optional<SpreadsheetFormatterSelector> selector = Optional.empty();
        String message = "";
        List<SpreadsheetFormatterSelectorToken> tokens = Lists.empty();
        Optional<SpreadsheetFormatterSelectorToken> next = Optional.empty();
        List<SpreadsheetFormatterSample> samples = Lists.empty();

        for (JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case SELECTOR_PROPERTY_STRING:
                    selector = context.unmarshallOptional(
                        child,
                        SpreadsheetFormatterSelector.class
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
                        SpreadsheetFormatterSelectorTokenList.class
                    );
                    break;
                case NEXT_PROPERTY_STRING:
                    next = context.unmarshallOptional(
                        child,
                        SpreadsheetFormatterSelectorToken.class
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
            selector,
            message,
            tokens,
            next,
            samples
        );
    }

    /**
     * <pre>
     * {
     *   "selector": "date dd/mm/yyyy",
     *   "message": "Hello",
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
     *       "selector": "date d/m/yy",
     *       "value": {
     *         "type": "text",
     *         "value": "31/12/99"
     *       }
     *     },
     *     {
     *       "label": "Medium",
     *       "selector": "date d mmm yyyy",
     *       "value": {
     *         "type": "text",
     *         "value": "31 Dec. 1999"
     *       }
     *     },
     *     {
     *       "label": "Long",
     *       "selector": "date d mmmm yyyy",
     *       "value": {
     *         "type": "text",
     *         "value": "31 December 1999"
     *       }
     *     },
     *     {
     *       "label": "Full",
     *       "selector": "date dddd, d mmmm yyyy",
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
        final List<JsonNode> children = Lists.array();

        final Optional<SpreadsheetFormatterSelector> selector = this.selector;
        if(selector.isPresent()) {
            children.add(
                context.marshallOptional(selector)
                    .setName(SELECTOR_PROPERTY)
            );
        }

        final String message = this.message;
        if (false == CharSequences.isNullOrEmpty(message)) {
            children.add(
                context.marshall(message)
                    .setName(MESSAGE_PROPERTY)
            );
        }

        final List<SpreadsheetFormatterSelectorToken> tokens = this.tokens;
        if(false == tokens.isEmpty()) {
            children.add(
                context.marshall(tokens)
                    .setName(TOKENS_PROPERTY)
            );
        }

        final Optional<SpreadsheetFormatterSelectorToken> next = this.next;
        if(next.isPresent()) {
            children.add(
                context.marshallOptional(next)
                    .setName(NEXT_PROPERTY)
            );
        }

        final List<SpreadsheetFormatterSample> samples = this.samples;
        if(false == samples.isEmpty()) {
            children.add(
                context.marshall(this.samples)
                    .setName(SAMPLES_PROPERTY)
            );
        }

        return JsonNode.object()
            .setChildren(children);
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
        SpreadsheetFormatterSelectorTokenList.with(Lists.empty());
        SpreadsheetFormatterSampleList.with(
            Lists.of(
                SpreadsheetFormatterSample.with(
                    "Label",
                    SpreadsheetFormatterSelector.DEFAULT_TEXT_FORMAT,
                    TextNode.EMPTY_TEXT
                )
            )
        );

        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(SpreadsheetFormatterSelectorEdit.class),
            SpreadsheetFormatterSelectorEdit::unmarshall,
            SpreadsheetFormatterSelectorEdit::marshall,
            SpreadsheetFormatterSelectorEdit.class
        );
    }
}