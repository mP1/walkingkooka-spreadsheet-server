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

package walkingkooka.spreadsheet.server.datetimesymbols;

import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.Value;
import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.spreadsheet.server.locale.LocaleLanguageTag;
import walkingkooka.text.CharSequences;
import walkingkooka.text.HasText;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class DateTimeSymbolsHateosResource implements HateosResource<LocaleLanguageTag>,
    Value<DateTimeSymbols>,
    HasText,
    Comparable<DateTimeSymbolsHateosResource>,
    TreePrintable {

    public final static HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with("dateTimeSymbols");

    @GwtIncompatible
    public static DateTimeSymbolsHateosResource fromLocale(final Locale locale) {
        Objects.requireNonNull(locale, "locale");

        return with(
            LocaleLanguageTag.parse(
                locale.toLanguageTag()
            ),
            locale.getDisplayName(),
            DateTimeSymbols.fromDateFormatSymbols(
                new DateFormatSymbols(locale)
            )
        );
    }

    public static DateTimeSymbolsHateosResource with(final LocaleLanguageTag localeLanguageTag,
                                                     final String localeText,
                                                     final DateTimeSymbols dateTimeSymbols) {
        return new DateTimeSymbolsHateosResource(
            Objects.requireNonNull(localeLanguageTag, "localeLanguageTag"),
            CharSequences.failIfNullOrEmpty(localeText, "localeText"),
            Objects.requireNonNull(dateTimeSymbols, "dateTimeSymbols")
        );
    }

    private DateTimeSymbolsHateosResource(final LocaleLanguageTag localeLanguageTag,
                                          final String localeText,
                                          final DateTimeSymbols dateTimeSymbols) {
        this.localeLanguageTag = localeLanguageTag;
        this.localeText = localeText;
        this.dateTimeSymbols = dateTimeSymbols;
    }

    @Override
    public Optional<LocaleLanguageTag> id() {
        return Optional.of(this.localeLanguageTag);
    }

    @Override
    public String hateosLinkId() {
        return this.localeLanguageTag.toString();
    }

    private final LocaleLanguageTag localeLanguageTag;

    // Hext.............................................................................................................

    @Override
    public String text() {
        return this.localeText;
    }

    /**
     * The locale text, such as "English"
     */
    private final String localeText;

    // Value............................................................................................................

    @Override
    public DateTimeSymbols value() {
        return this.dateTimeSymbols;
    }

    private final DateTimeSymbols dateTimeSymbols;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
            this.localeLanguageTag,
            this.localeText,
            this.dateTimeSymbols
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof DateTimeSymbolsHateosResource &&
                this.equals0((DateTimeSymbolsHateosResource) other));
    }

    private boolean equals0(final DateTimeSymbolsHateosResource other) {
        return this.localeLanguageTag.equals(other.localeLanguageTag) &&
            this.localeText.equals(other.localeText) &&
            this.dateTimeSymbols.equals(other.dateTimeSymbols);
    }

    @Override
    public String toString() {
        return this.localeLanguageTag + " " + localeText + " " + this.dateTimeSymbols;
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final DateTimeSymbolsHateosResource other) {
        return this.localeLanguageTag.compareTo(other.localeLanguageTag);
    }

    // TreePrintable.....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.localeLanguageTag.toString());

        printer.indent();
        {
            printer.println(this.localeText);
        }
        printer.outdent();

        printer.indent();
        {
            this.dateTimeSymbols.printTree(printer);
        }
        printer.outdent();
    }

    // json.............................................................................................................

    /**
     * Factory that creates a {@link DateTimeSymbolsHateosResource} parse a {@link JsonNode}.
     */
    static DateTimeSymbolsHateosResource unmarshall(final JsonNode node,
                                                    final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        LocaleLanguageTag localeLanguageTag = null;
        String localeText = null;
        DateTimeSymbols dateTimeSymbols = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case LOCALE_PROPERTY_STRING:
                    localeLanguageTag = context.unmarshall(
                        child,
                        LocaleLanguageTag.class
                    );
                    break;
                case TEXT_PROPERTY_STRING:
                    localeText = context.unmarshall(
                        child,
                        String.class
                    );
                    break;
                case DATE_TIME_SYMBOLS_PROPERTY_STRING:
                    dateTimeSymbols = context.unmarshall(
                        child,
                        DateTimeSymbols.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
                    break;
            }
        }

        if (null == localeLanguageTag) {
            JsonNodeUnmarshallContext.missingProperty(LOCALE_PROPERTY, node);
        }
        if (null == dateTimeSymbols) {
            JsonNodeUnmarshallContext.missingProperty(DATE_TIME_SYMBOLS_PROPERTY, node);
        }

        return with(
            localeLanguageTag,
            localeText,
            dateTimeSymbols
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
            .set(LOCALE_PROPERTY, context.marshall(this.localeLanguageTag))
            .set(TEXT_PROPERTY, context.marshall(this.localeText))
            .set(DATE_TIME_SYMBOLS_PROPERTY, context.marshall(this.dateTimeSymbols));
    }

    private final static String LOCALE_PROPERTY_STRING = "locale";
    private final static String TEXT_PROPERTY_STRING = "text";
    private final static String DATE_TIME_SYMBOLS_PROPERTY_STRING = "dateTimeSymbols";

    private final static JsonPropertyName LOCALE_PROPERTY = JsonPropertyName.with(LOCALE_PROPERTY_STRING);
    private final static JsonPropertyName TEXT_PROPERTY = JsonPropertyName.with(TEXT_PROPERTY_STRING);
    private final static JsonPropertyName DATE_TIME_SYMBOLS_PROPERTY = JsonPropertyName.with(DATE_TIME_SYMBOLS_PROPERTY_STRING);

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(DateTimeSymbolsHateosResource.class),
            DateTimeSymbolsHateosResource::unmarshall,
            DateTimeSymbolsHateosResource::marshall,
            DateTimeSymbolsHateosResource.class
        );
    }
}
