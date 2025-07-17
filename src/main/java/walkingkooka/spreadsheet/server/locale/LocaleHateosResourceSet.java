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

import walkingkooka.collect.iterator.Iterators;
import walkingkooka.collect.set.ImmutableSortedSetDefaults;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.text.CharacterConstant;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable {@link Set} containing unique {@link LocaleHateosResource locales}.
 */
public final class LocaleHateosResourceSet extends AbstractSet<LocaleHateosResource>
    implements ImmutableSortedSetDefaults<LocaleHateosResourceSet, LocaleHateosResource>,
    TreePrintable {

    /**
     * An empty {@link LocaleHateosResourceSet}.
     */
    public static final LocaleHateosResourceSet EMPTY = new LocaleHateosResourceSet(SortedSets.empty());

    /**
     * The comma which separates the CSV text representation.
     */
    public static final CharacterConstant SEPARATOR = CharacterConstant.COMMA;

    /**
     * Handy filter that creates a {@link LocaleHateosResource} for each locale that starts with that given text
     */
    public static Set<LocaleHateosResource> filter(final String startsWith,
                                                   final LocaleContext context) {

        final Set<LocaleHateosResource> matched = Sets.ordered();

        for (final Locale locale : context.findByLocaleText(
            startsWith,
            0,
            Integer.MAX_VALUE
        )) {
            final String localeText = context.localeText(locale)
                .orElse(null);

            if (null != localeText && (LocaleContexts.CASE_SENSITIVITY.startsWith(localeText, startsWith) || LocaleContexts.CASE_SENSITIVITY.equals(localeText, startsWith))) {
                matched.add(
                    LocaleHateosResource.with(
                        LocaleTag.with(locale),
                        localeText
                    )
                );
            }
        }

        return matched;
    }

    /**
     * Factory that creates {@link LocaleHateosResourceSet} with the given locales.
     */
    public static LocaleHateosResourceSet with(final SortedSet<LocaleHateosResource> locales) {
        return EMPTY.setElements(locales);
    }

    private static LocaleHateosResourceSet withCopy(final SortedSet<LocaleHateosResource> locales) {
        return locales.isEmpty() ?
            EMPTY :
            new LocaleHateosResourceSet(locales);
    }

    private LocaleHateosResourceSet(final SortedSet<LocaleHateosResource> locales) {
        super();
        this.locales = locales;
    }

    // ImmutableSortedSet...............................................................................................

    @Override
    public Iterator<LocaleHateosResource> iterator() {
        return Iterators.readOnly(
            this.locales.iterator()
        );
    }

    @Override
    public int size() {
        return this.locales.size();
    }

    @Override
    public Comparator<LocaleHateosResource> comparator() {
        return null;
    }

    @Override
    public LocaleHateosResourceSet subSet(final LocaleHateosResource from,
                                          final LocaleHateosResource to) {
        return withCopy(
            this.locales.subSet(
                from,
                to
            )
        );
    }

    @Override
    public LocaleHateosResourceSet headSet(final LocaleHateosResource locale) {
        return withCopy(
            this.locales.headSet(locale)
        );
    }

    @Override
    public LocaleHateosResourceSet tailSet(final LocaleHateosResource locale) {
        return withCopy(
            this.locales.tailSet(locale)
        );
    }

    @Override
    public LocaleHateosResource first() {
        return this.locales.first();
    }

    @Override
    public LocaleHateosResource last() {
        return this.locales.last();
    }

    @Override
    public SortedSet<LocaleHateosResource> toSet() {
        return new TreeSet<>(this.locales);
    }

    @Override
    public LocaleHateosResourceSet setElements(final SortedSet<LocaleHateosResource> locales) {
        final LocaleHateosResourceSet localeHateosResourceSet;

        if (locales instanceof LocaleHateosResourceSet) {
            localeHateosResourceSet = (LocaleHateosResourceSet) locales;
        } else {
            final TreeSet<LocaleHateosResource> copy = new TreeSet<>(
                Objects.requireNonNull(locales, "locales")
            );
            localeHateosResourceSet = this.locales.equals(copy) ?
                this :
                withCopy(copy);
        }

        return localeHateosResourceSet;
    }

    private final SortedSet<LocaleHateosResource> locales;

    @Override
    public void elementCheck(final LocaleHateosResource locale) {
        Objects.requireNonNull(locale, "locale");
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        for (final LocaleHateosResource resource : this) {
            resource.printTree(printer);
        }
    }

    // Json.............................................................................................................

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static LocaleHateosResourceSet unmarshall(final JsonNode node,
                                              final JsonNodeUnmarshallContext context) {
        return with(
            new TreeSet<>(
                context.unmarshallSet(
                    node,
                    LocaleHateosResource.class
                )
            )
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(LocaleHateosResourceSet.class),
            LocaleHateosResourceSet::unmarshall,
            LocaleHateosResourceSet::marshall,
            LocaleHateosResourceSet.class
        );
    }
}
