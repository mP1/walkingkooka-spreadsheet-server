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

import walkingkooka.Value;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.text.HasText;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Wrapper that holds a {@link Locale}.
 */
public final class LocaleHateosResource implements HateosResource<LocaleTag>,
    Value<Locale>,
    HasText,
    Comparable<LocaleHateosResource> {

    public final static HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with("locale");

    public static LocaleHateosResource with(final Locale locale) {
        return new LocaleHateosResource(
            Objects.requireNonNull(locale, "locale")
        );
    }

    private LocaleHateosResource(final Locale locale) {
        this.localeTag = LocaleTag.with(locale);
        this.locale = locale;
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
    public Locale value() {
        return this.locale;
    }

    private final Locale locale;

    // HasText..........................................................................................................

    @Override
    public String text() {
        return this.localeTag.toString();
    }

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.locale.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof LocaleHateosResource &&
                this.equals0((LocaleHateosResource) other));
    }

    private boolean equals0(final LocaleHateosResource other) {
        return this.locale.equals(other.locale);
    }

    @Override
    public String toString() {
        return this.locale.toLanguageTag();
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final LocaleHateosResource other) {
        return this.localeTag.compareTo(other.localeTag);
    }

    // json.............................................................................................................

    static LocaleHateosResource unmarshall(final JsonNode node,
                                           final JsonNodeUnmarshallContext context) {
        return with(
            context.unmarshall(
                node,
                Locale.class
            )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshall(
            this.locale
        );
    }

    static {
        Locale.getDefault();

        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(LocaleHateosResource.class),
            LocaleHateosResource::unmarshall,
            LocaleHateosResource::marshall,
            LocaleHateosResource.class
        );
    }
}
