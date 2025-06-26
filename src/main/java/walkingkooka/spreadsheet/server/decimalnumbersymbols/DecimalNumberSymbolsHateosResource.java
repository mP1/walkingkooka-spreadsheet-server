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

package walkingkooka.spreadsheet.server.decimalnumbersymbols;

import walkingkooka.Value;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.spreadsheet.server.locale.LocaleTag;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class DecimalNumberSymbolsHateosResource implements HateosResource<LocaleTag>,
    Value<DecimalNumberSymbols>,
    Comparable<DecimalNumberSymbolsHateosResource>,
    TreePrintable {

    public final static HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with("decimalNumberSymbols");

    public static DecimalNumberSymbolsHateosResource fromLocale(final Locale locale) {
        Objects.requireNonNull(locale, "locale");

        return new DecimalNumberSymbolsHateosResource(
            LocaleTag.with(locale),
            DecimalNumberSymbols.fromDecimalFormatSymbols(
                '+',
                new DecimalFormatSymbols(locale)
            )
        );
    }
    
    public static DecimalNumberSymbolsHateosResource with(final LocaleTag localeTag,
                                                          final DecimalNumberSymbols decimalNumberSymbols) {
        return new DecimalNumberSymbolsHateosResource(
            Objects.requireNonNull(localeTag, "localeTag"),
            Objects.requireNonNull(decimalNumberSymbols, "decimalNumberSymbols")
        );
    }

    private DecimalNumberSymbolsHateosResource(final LocaleTag localeTag,
                                               final DecimalNumberSymbols decimalNumberSymbols) {
        this.localeTag = localeTag;
        this.decimalNumberSymbols = decimalNumberSymbols;
    }

    @Override
    public Optional<LocaleTag> id() {
        return Optional.of(this.localeTag);
    }

    @Override
    public String hateosLinkId() {
        return this.localeTag.toString();
    }

    private final LocaleTag localeTag;

    @Override
    public DecimalNumberSymbols value() {
        return this.decimalNumberSymbols;
    }

    private final DecimalNumberSymbols decimalNumberSymbols;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
            this.localeTag,
            this.decimalNumberSymbols
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof DecimalNumberSymbolsHateosResource &&
                this.equals0((DecimalNumberSymbolsHateosResource) other));
    }

    private boolean equals0(final DecimalNumberSymbolsHateosResource other) {
        return this.localeTag.equals(other.localeTag) &&
            this.decimalNumberSymbols.equals(other.decimalNumberSymbols);
    }

    @Override
    public String toString() {
        return this.localeTag + " " + this.decimalNumberSymbols;
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final DecimalNumberSymbolsHateosResource other) {
        return this.localeTag.compareTo(other.localeTag);
    }

    // TreePrintable.....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.localeTag.toString());
        printer.indent();
        {
            this.decimalNumberSymbols.printTree(printer);
        }
        printer.outdent();
    }

    // json.............................................................................................................

    /**
     * Factory that creates a {@link DecimalNumberSymbolsHateosResource} parse a {@link JsonNode}.
     */
    static DecimalNumberSymbolsHateosResource unmarshall(final JsonNode node,
                                                         final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        LocaleTag localeTag = null;
        DecimalNumberSymbols decimalNumberSymbols = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case LOCALE_TAG_PROPERTY_STRING:
                    localeTag = context.unmarshall(
                        child,
                        LocaleTag.class
                    );
                    break;
                case DECIMAL_NUMBER_SYMBOLS_PROPERTY_STRING:
                    decimalNumberSymbols = context.unmarshall(
                        child,
                        DecimalNumberSymbols.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        if (null == localeTag) {
            JsonNodeUnmarshallContext.missingProperty(LOCALE_TAG_PROPERTY, node);
        }
        if (null == decimalNumberSymbols) {
            JsonNodeUnmarshallContext.missingProperty(DECIMAL_NUMBER_SYMBOLS_PROPERTY, node);
        }

        return new DecimalNumberSymbolsHateosResource(
            localeTag,
            decimalNumberSymbols
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
            .set(LOCALE_TAG_PROPERTY, context.marshall(this.localeTag))
            .set(DECIMAL_NUMBER_SYMBOLS_PROPERTY, context.marshall(this.decimalNumberSymbols));
    }

    private final static String LOCALE_TAG_PROPERTY_STRING = "localeTag";
    private final static String DECIMAL_NUMBER_SYMBOLS_PROPERTY_STRING = "decimalNumberSymbols";

    private final static JsonPropertyName LOCALE_TAG_PROPERTY = JsonPropertyName.with(LOCALE_TAG_PROPERTY_STRING);
    private final static JsonPropertyName DECIMAL_NUMBER_SYMBOLS_PROPERTY = JsonPropertyName.with(DECIMAL_NUMBER_SYMBOLS_PROPERTY_STRING);

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(DecimalNumberSymbolsHateosResource.class),
            DecimalNumberSymbolsHateosResource::unmarshall,
            DecimalNumberSymbolsHateosResource::marshall,
            DecimalNumberSymbolsHateosResource.class
        );
    }
}
