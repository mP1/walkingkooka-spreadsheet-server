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

package walkingkooka.spreadsheet.server.locale;

import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.Value;
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

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * A proxy for a {@link Locale} holding the unique locale tag and label or text for display
 */
public final class LocaleHateosResource implements HateosResource<LocaleLanguageTag>,
    Value<LocaleLanguageTag>,
    HasText,
    Comparable<LocaleHateosResource>,
    TreePrintable {

    public final static HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with("locale");

    @GwtIncompatible
    public static LocaleHateosResource fromLocale(final Locale locale) {
        Objects.requireNonNull(locale, "locale");

        final String displayName = LocaleHateosResourceGetDisplay.getDisplay(locale);

        return with(
            LocaleLanguageTag.fromLocale(locale),
            displayName.isEmpty() ?
                locale.toLanguageTag() :
                displayName
        );
    }

    public static LocaleHateosResource with(final LocaleLanguageTag localeLanguageTag,
                                            final String text) {
        return new LocaleHateosResource(
            Objects.requireNonNull(localeLanguageTag, "localeLanguageTag"),
            CharSequences.failIfNullOrEmpty(text, "text")
        );
    }

    private LocaleHateosResource(final LocaleLanguageTag localeLanguageTag,
                                 final String text) {
        this.localeLanguageTag = localeLanguageTag;
        this.text = text;
    }

    @Override
    public Optional<LocaleLanguageTag> id() {
        return Optional.of(this.localeLanguageTag);
    }

    @Override
    public String hateosLinkId() {
        return this.localeLanguageTag.toString();
    }

    @Override
    public LocaleLanguageTag value() {
        return this.localeLanguageTag;
    }

    private final LocaleLanguageTag localeLanguageTag;

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
            this.localeLanguageTag,
            this.text
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof LocaleHateosResource &&
                this.equals0((LocaleHateosResource) other));
    }

    private boolean equals0(final LocaleHateosResource other) {
        return this.localeLanguageTag.equals(other.localeLanguageTag) &&
            this.text.equals(other.text);
    }

    @Override
    public String toString() {
        return this.localeLanguageTag + " " + CharSequences.quoteIfChars(this.text);
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final LocaleHateosResource other) {
        return this.localeLanguageTag.compareTo(other.localeLanguageTag);
    }

    // TreePrintable.....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        printer.println(this.localeLanguageTag.toString());
        printer.indent();
        {
            printer.println(this.text());
        }
        printer.outdent();
    }

    // json.............................................................................................................

    /**
     * Factory that creates a {@link LocaleHateosResource} parse a {@link JsonNode}.
     */
    static LocaleHateosResource unmarshall(final JsonNode node,
                                           final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        LocaleLanguageTag localeLanguageTag = null;
        String text = null;

        for (JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();
            switch (name.value()) {
                case LOCALE_PROPERTY_STRING:
                    localeLanguageTag = context.unmarshall(
                        child,
                        LocaleLanguageTag.class
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

        if (null == localeLanguageTag) {
            JsonNodeUnmarshallContext.missingProperty(LOCALE_PROPERTY, node);
        }
        if (null == text) {
            JsonNodeUnmarshallContext.missingProperty(TEXT_PROPERTY, node);
        }

        return new LocaleHateosResource(
            localeLanguageTag,
            text
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
            .set(LOCALE_PROPERTY, context.marshall(this.localeLanguageTag))
            .set(TEXT_PROPERTY, context.marshall(this.text));
    }

    private final static String LOCALE_PROPERTY_STRING = "locale";
    private final static String TEXT_PROPERTY_STRING = "text";

    private final static JsonPropertyName LOCALE_PROPERTY = JsonPropertyName.with(LOCALE_PROPERTY_STRING);
    private final static JsonPropertyName TEXT_PROPERTY = JsonPropertyName.with(TEXT_PROPERTY_STRING);


    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(LocaleHateosResource.class),
            LocaleHateosResource::unmarshall,
            LocaleHateosResource::marshall,
            LocaleHateosResource.class
        );
    }
}
