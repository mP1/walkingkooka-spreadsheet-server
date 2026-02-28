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
import walkingkooka.compare.Comparators;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Locale;
import java.util.Objects;

/**
 * An id that uniquely identifies a {@link Locale} using its language-tag such as EN, EN-AU for a {@link walkingkooka.net.http.server.hateos.HateosResourceHandler}.
 */
public final class LocaleTag implements Comparable<LocaleTag>, Value<String> {

    public final static CaseSensitivity CASE_SENSITIVITY = CaseSensitivity.INSENSITIVE;

    public static LocaleTag fromLocale(final Locale locale) {
        return new LocaleTag(
            Objects.requireNonNull(locale, "locale")
                .toLanguageTag()
        );
    }

    public static LocaleTag parse(final String languageTag) {
        return new LocaleTag(
            Objects.requireNonNull(languageTag)
        );
    }

    private LocaleTag(final String languageTag) {
        super();

        this.languageTag = languageTag;
    }

    // Value............................................................................................................

    @Override
    public String value() {
        return this.languageTag;
    }

    private final String languageTag;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return CASE_SENSITIVITY.hash(
            this.languageTag
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            (other instanceof LocaleTag &&
                this.equals0((LocaleTag) other));
    }

    private boolean equals0(final LocaleTag other) {
        return this.compareTo(other) == Comparators.EQUAL;
    }

    @Override
    public String toString() {
        return this.languageTag;
    }

    // Comparable.......................................................................................................

    @Override
    public int compareTo(final LocaleTag other) {
        return CASE_SENSITIVITY.comparator()
            .compare(
                this.languageTag,
                other.languageTag
            );
    }

    // json.............................................................................................................

    static LocaleTag unmarshall(final JsonNode node,
                                final JsonNodeUnmarshallContext context) {
        return parse(
            context.unmarshall(
                node,
                String.class
            )
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshall(
            this.languageTag
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(LocaleTag.class),
            LocaleTag::unmarshall,
            LocaleTag::marshall,
            LocaleTag.class
        );
    }
}
