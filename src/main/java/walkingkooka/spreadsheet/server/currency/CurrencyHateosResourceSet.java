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

import walkingkooka.collect.iterator.Iterators;
import walkingkooka.collect.set.ImmutableSortedSetDefaults;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.currency.CurrencyContext;
import walkingkooka.currency.CurrencyContexts;
import walkingkooka.text.CharacterConstant;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable {@link Set} containing unique {@link CurrencyHateosResource currencies}.
 */
public final class CurrencyHateosResourceSet extends AbstractSet<CurrencyHateosResource>
    implements ImmutableSortedSetDefaults<CurrencyHateosResourceSet, CurrencyHateosResource>,
    TreePrintable {

    /**
     * An empty {@link CurrencyHateosResourceSet}.
     */
    public static final CurrencyHateosResourceSet EMPTY = new CurrencyHateosResourceSet(SortedSets.empty());

    /**
     * The comma which separates the CSV text representation.
     */
    public static final CharacterConstant SEPARATOR = CharacterConstant.COMMA;

    /**
     * Handy filter that creates a {@link CurrencyHateosResource} for each locale that starts with that given text
     */
    public static Set<CurrencyHateosResource> filter(final String startsWith,
                                                     final CurrencyContext context) {

        final Set<CurrencyHateosResource> matched = Sets.ordered();

        for (final Currency currency : context.findByCurrencyText(
            startsWith,
            0,
            Integer.MAX_VALUE
        )) {
            final String currencyText = context.currencyText(currency)
                .orElse(null);

            if (null != currencyText && (CurrencyContexts.CASE_SENSITIVITY.startsWith(currencyText, startsWith) || CurrencyContexts.CASE_SENSITIVITY.equals(currencyText, startsWith))) {
                matched.add(
                    CurrencyHateosResource.with(
                        CurrencyCode.with(currency),
                        currencyText
                    )
                );
            }
        }

        return matched;
    }

    /**
     * Factory that creates {@link CurrencyHateosResourceSet} with the given currencies.
     */
    public static CurrencyHateosResourceSet with(final Collection<CurrencyHateosResource> currencies) {
        return EMPTY.setElements(currencies);
    }

    private static CurrencyHateosResourceSet withCopy(final SortedSet<CurrencyHateosResource> currencies) {
        return currencies.isEmpty() ?
            EMPTY :
            new CurrencyHateosResourceSet(currencies);
    }

    private CurrencyHateosResourceSet(final SortedSet<CurrencyHateosResource> currencies) {
        super();
        this.currencies = currencies;
    }

    // ImmutableSortedSet...............................................................................................

    @Override
    public Iterator<CurrencyHateosResource> iterator() {
        return Iterators.readOnly(
            this.currencies.iterator()
        );
    }

    @Override
    public int size() {
        return this.currencies.size();
    }

    @Override
    public Comparator<CurrencyHateosResource> comparator() {
        return null;
    }

    @Override
    public CurrencyHateosResourceSet subSet(final CurrencyHateosResource from,
                                            final CurrencyHateosResource to) {
        return withCopy(
            this.currencies.subSet(
                from,
                to
            )
        );
    }

    @Override
    public CurrencyHateosResourceSet headSet(final CurrencyHateosResource locale) {
        return withCopy(
            this.currencies.headSet(locale)
        );
    }

    @Override
    public CurrencyHateosResourceSet tailSet(final CurrencyHateosResource locale) {
        return withCopy(
            this.currencies.tailSet(locale)
        );
    }

    @Override
    public CurrencyHateosResource first() {
        return this.currencies.first();
    }

    @Override
    public CurrencyHateosResource last() {
        return this.currencies.last();
    }

    @Override
    public SortedSet<CurrencyHateosResource> toSet() {
        return new TreeSet<>(this.currencies);
    }

    @Override
    public CurrencyHateosResourceSet setElements(final Collection<CurrencyHateosResource> currencies) {
        final CurrencyHateosResourceSet localeHateosResourceSet;

        if (currencies instanceof CurrencyHateosResourceSet) {
            localeHateosResourceSet = (CurrencyHateosResourceSet) currencies;
        } else {
            final TreeSet<CurrencyHateosResource> copy = new TreeSet<>(
                Objects.requireNonNull(currencies, "currencies")
            );
            localeHateosResourceSet = this.currencies.equals(copy) ?
                this :
                withCopy(copy);
        }

        return localeHateosResourceSet;
    }

    private final SortedSet<CurrencyHateosResource> currencies;

    @Override
    public void elementCheck(final CurrencyHateosResource locale) {
        Objects.requireNonNull(locale, "locale");
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        for (final CurrencyHateosResource resource : this) {
            resource.printTree(printer);
        }
    }

    // Json.............................................................................................................

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    static CurrencyHateosResourceSet unmarshall(final JsonNode node,
                                                final JsonNodeUnmarshallContext context) {
        return with(
            new TreeSet<>(
                context.unmarshallSet(
                    node,
                    CurrencyHateosResource.class
                )
            )
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(CurrencyHateosResourceSet.class),
            CurrencyHateosResourceSet::unmarshall,
            CurrencyHateosResourceSet::marshall,
            CurrencyHateosResourceSet.class
        );
    }
}
