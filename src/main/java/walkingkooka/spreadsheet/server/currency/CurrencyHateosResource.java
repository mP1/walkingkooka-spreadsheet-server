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

package walkingkooka.spreadsheet.server.currency;

import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.Value;
import walkingkooka.currency.HasCurrency;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.text.CharSequences;
import walkingkooka.text.HasText;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

/**
 * A proxy for a {@link Currency} holding the unique currency tag and label or text for display
 */
public final class CurrencyHateosResource implements HateosResource<CurrencyCode>,
    Value<String>,
    HasCurrency,
    HasText,
    Comparable<CurrencyHateosResource>,
    TreePrintable {

    public final static HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with("currency");

    @GwtIncompatible
    public static CurrencyHateosResource fromCurrency(final Currency currency) {
        Objects.requireNonNull(currency, "currency");

        final String displayName = CurrencyHateosResourceGetDisplay.getDisplay(currency);

        return with(
            CurrencyCode.with(currency),
            displayName.isEmpty() ?
                currency.getCurrencyCode() :
                displayName
        );
    }

    public static CurrencyHateosResource with(final CurrencyCode currencyCode,
                                              final String text) {
        return new CurrencyHateosResource(
            Objects.requireNonNull(currencyCode, "currencyCode"),
            CharSequences.failIfNullOrEmpty(text, "text")
        );
    }

    private CurrencyHateosResource(final CurrencyCode currencyCode,
                                   final String text) {
        this.currencyCode = currencyCode;
        this.text = text;
    }

    @Override
    public Optional<CurrencyCode> id() {
        return Optional.of(this.currencyCode);
    }

    @Override
    public String hateosLinkId() {
        return this.currencyCode.toString();
    }

    @Override
    public Currency currency() {
        return this.currencyCode.value();
    }

    private final CurrencyCode currencyCode;

    @Override
    public String value() {
        return this.text;
    }

    // HasText..........................................................................................................

    @Override
    public String text() {
        return this.text;
    }

    private final String text;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
            this.currencyCode,
            this.text
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof CurrencyHateosResource &&
                this.equals0((CurrencyHateosResource) other));
    }

    private boolean equals0(final CurrencyHateosResource other) {
        return this.currencyCode.equals(other.currencyCode) &&
            this.text.equals(other.text);
    }

    @Override
    public String toString() {
        return this.currencyCode + " " + CharSequences.quoteIfChars(this.text);
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final CurrencyHateosResource other) {
        return this.currencyCode.compareTo(other.currencyCode);
    }

    // TreePrintable.....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.currencyCode.toString());
        printer.indent();
        {
            printer.println(this.text());
        }
        printer.outdent();
    }

    // json.............................................................................................................

    /**
     * Factory that creates a {@link CurrencyHateosResource} parse a {@link JsonNode}.
     */
    static CurrencyHateosResource unmarshall(final JsonNode node,
                                             final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        CurrencyCode currencyCode = null;
        String text = null;

        for (JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case CURRENCY_TAG_PROPERTY_STRING:
                    currencyCode = context.unmarshall(
                        child,
                        CurrencyCode.class
                    );
                    break;
                case TEXT_PROPERTY_STRING:
                    text = context.unmarshall(
                        child,
                        String.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        if (null == currencyCode) {
            JsonNodeUnmarshallContext.missingProperty(CURRENCY_TAG_PROPERTY, node);
        }
        if (null == text) {
            JsonNodeUnmarshallContext.missingProperty(TEXT_PROPERTY, node);
        }

        return new CurrencyHateosResource(
            currencyCode,
            text
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
            .set(CURRENCY_TAG_PROPERTY, context.marshall(this.currencyCode))
            .set(TEXT_PROPERTY, context.marshall(this.text));
    }

    private final static String CURRENCY_TAG_PROPERTY_STRING = "currencyCode";
    private final static String TEXT_PROPERTY_STRING = "text";

    private final static JsonPropertyName CURRENCY_TAG_PROPERTY = JsonPropertyName.with(CURRENCY_TAG_PROPERTY_STRING);
    private final static JsonPropertyName TEXT_PROPERTY = JsonPropertyName.with(TEXT_PROPERTY_STRING);


    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(CurrencyHateosResource.class),
            CurrencyHateosResource::unmarshall,
            CurrencyHateosResource::marshall,
            CurrencyHateosResource.class
        );
    }
}
