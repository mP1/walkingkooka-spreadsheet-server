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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.currency.CurrencyContext;
import walkingkooka.currency.FakeCurrencyContext;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Currency;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CurrencyHateosResourceSetTest implements ImmutableSortedSetTesting<CurrencyHateosResourceSet, CurrencyHateosResource>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<CurrencyHateosResourceSet> {

    private final static CurrencyHateosResource AUD = CurrencyHateosResource.fromCurrency(
        Currency.getInstance("AUD")
    );

    private final static CurrencyHateosResource NZD = CurrencyHateosResource.fromCurrency(
        Currency.getInstance("NZD")
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> CurrencyHateosResourceSet.with(null)
        );
    }

    @Test
    public void testDeleteBecomesEmpty() {
        assertSame(
            CurrencyHateosResourceSet.EMPTY,
            CurrencyHateosResourceSet.EMPTY.concat(AUD)
                .delete(AUD)
        );
    }

    @Test
    public void testSetElementsWithCurrencyHateosResourceSet() {
        final CurrencyHateosResourceSet set = CurrencyHateosResourceSet.with(
            SortedSets.of(
                CurrencyHateosResource.fromCurrency(
                    Currency.getInstance("USD")
                )
            )
        );

        assertSame(
            set,
            set.setElements(set)
        );
    }

    @Test
    public void testSetElementsWithCurrencyHateosResourceSet2() {
        final CurrencyHateosResourceSet set = this.createSet();
        final CurrencyHateosResourceSet set2 = this.createSet();

        assertSame(
            set2,
            set.setElements(
                set2
            )
        );
    }

    @Override
    public CurrencyHateosResourceSet createSet() {
        final SortedSet<CurrencyHateosResource> sortedSet = SortedSets.tree();

        sortedSet.add(AUD);
        sortedSet.add(NZD);

        return CurrencyHateosResourceSet.with(
            SortedSets.of(
                AUD,
                NZD
            )
        );
    }

    // filter...........................................................................................................

    private final static Currency AUD_CURRENCY = Currency.getInstance("AUD");
    private final static Currency NZD_CURRENCY = Currency.getInstance("NZD");
    private final static Currency EUR_CURRENCY = Currency.getInstance("EUR");

    private final static String AUD_TEXT = "Australia Dollar";
    private final static String NZD_TEXT = "New Zealand Dollar";
    private final static String EUR_TEXT = "Euro 123";

    private final static CurrencyContext CONTEXT = new FakeCurrencyContext() {

        @Override
        public Set<Currency> findByCurrencyText(final String text,
                                                final int offset,
                                                final int count) {
            return Sets.of(
                AUD_CURRENCY,
                NZD_CURRENCY,
                EUR_CURRENCY
            );
        }

        @Override
        public Optional<String> currencyText(final Currency currency) {
            return Optional.ofNullable(
                AUD_CURRENCY.equals(currency) ?
                    AUD_TEXT :
                    NZD_CURRENCY.equals(currency) ?
                        NZD_TEXT :
                        EUR_CURRENCY.equals(currency) ?
                            EUR_TEXT :
                            null
            );
        }
    };

    @Test
    public void testFilterMatchesNone() {
        this.filterAndCheck(
            "Z",
            CONTEXT
        );
    }

    @Test
    public void testFilterMatchesSome() {
        this.filterAndCheck(
            "Australia",
            CONTEXT,
            CurrencyHateosResource.with(
                CurrencyCode.with(AUD_CURRENCY),
                AUD_TEXT
            )
        );
    }

    @Test
    public void testFilterMatchesSome2() {
        this.filterAndCheck(
            EUR_TEXT,
            CONTEXT,
            CurrencyHateosResource.with(
                CurrencyCode.with(EUR_CURRENCY),
                EUR_TEXT
            )
        );
    }

    private void filterAndCheck(final String startsWith,
                                final CurrencyContext context,
                                final CurrencyHateosResource... expected) {
        this.filterAndCheck(
            startsWith,
            context,
            Sets.of(expected)
        );
    }

    private void filterAndCheck(final String startsWith,
                                final CurrencyContext context,
                                final Set<CurrencyHateosResource> expected) {
        this.checkEquals(
            expected,
            CurrencyHateosResourceSet.filter(
                startsWith,
                context
            )
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.createSet(),
            "AUD\n" +
                "  Australian Dollar\n" +
                "NZD\n" +
                "  New Zealand Dollar\n"
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "[\n" +
                "  {\n" +
                "    \"currencyCode\": \"AUD\",\n" +
                "    \"text\": \"Australian Dollar\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"currencyCode\": \"NZD\",\n" +
                "    \"text\": \"New Zealand Dollar\"\n" +
                "  }\n" +
                "]"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "[\n" +
                "  {\n" +
                "    \"currencyCode\": \"AUD\",\n" +
                "    \"text\": \"Australian Dollar\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"currencyCode\": \"NZD\",\n" +
                "    \"text\": \"New Zealand Dollar\"\n" +
                "  }\n" +
                "]",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Test
    public void testMarshallUnmarshallAllAvailableCurrencyHateosResources() {
        final SortedSet<CurrencyHateosResource> currencies = SortedSets.tree();
        currencies.addAll(
            Currency.getAvailableCurrencies()
                .stream()
                .map(CurrencyHateosResource::fromCurrency)
                .collect(Collectors.toList())
        );

        this.checkNotEquals(
            SortedSets.empty(),
            currencies
        );

        this.marshallRoundTripTwiceAndCheck(
            CurrencyHateosResourceSet.with(currencies)
        );
    }

    @Override
    public CurrencyHateosResourceSet unmarshall(final JsonNode jsonNode,
                                                final JsonNodeUnmarshallContext context) {
        return CurrencyHateosResourceSet.unmarshall(
            jsonNode,
            context
        );
    }

    @Override
    public CurrencyHateosResourceSet createJsonNodeMarshallingValue() {
        return this.createSet();
    }

    // class............................................................................................................

    @Override
    public Class<CurrencyHateosResourceSet> type() {
        return CurrencyHateosResourceSet.class;
    }
}