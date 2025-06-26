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
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Locale;
import java.util.Objects;

/**
 * An id that uniquely identifies a {@link Locale} for a {@link walkingkooka.net.http.server.hateos.HateosResourceHandler}.
 */
public final class LocaleTag implements Comparable<LocaleTag>, Value<Locale> {

    public static final String HATEOS_RESOURCE_NAME_STRING = "locale";

    public static final HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with(HATEOS_RESOURCE_NAME_STRING);

    public static LocaleTag parse(final String text) {
        return with(
            Locale.forLanguageTag(text)
        );
    }

    public static LocaleTag with(final Locale locale) {
        return new LocaleTag(
            Objects.requireNonNull(locale)
        );
    }

    private LocaleTag(final Locale locale) {
        super();
        this.locale = locale;
    }

    // Value............................................................................................................

    @Override
    public Locale value() {
        return this.locale;
    }

    private final Locale locale;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return this.locale.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof LocaleTag &&
                this.equals0((LocaleTag) other));
    }

    private boolean equals0(final LocaleTag other) {
        return this.locale.equals(other.locale);
    }

    @Override
    public String toString() {
        return this.locale.toLanguageTag();
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final LocaleTag other) {
        return this.locale.toLanguageTag()
            .compareTo(other.locale.toLanguageTag());
    }

    // json.............................................................................................................

    static LocaleTag unmarshall(final JsonNode node, 
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
            JsonNodeContext.computeTypeName(LocaleTag.class),
            LocaleTag::unmarshall,
            LocaleTag::marshall,
            LocaleTag.class
        );
    }
}
