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

package walkingkooka.spreadsheet.server.currencyexchangerate;

import org.junit.jupiter.api.Test;
import walkingkooka.compare.ComparableTesting2;
import walkingkooka.currency.CurrencyExchange;
import walkingkooka.net.http.server.hateos.HateosResourceTesting2;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallerTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CurrencyExchangeHateosResourceTest implements HateosResourceTesting2<CurrencyExchangeHateosResource, CurrencyExchange>,
    ComparableTesting2<CurrencyExchangeHateosResource>,
    JsonNodeMarshallerTesting<CurrencyExchangeHateosResource>,
    ClassTesting2<CurrencyExchangeHateosResource> {

    
    private final static CurrencyExchange CURRENCY_EXCHANGE = CurrencyExchange.parse("AUD-NZD");

    @Test
    public void testWithNullCurrencyExchangeFails() {
        assertThrows(
            NullPointerException.class,
            () -> CurrencyExchangeHateosResource.with(null)
        );
    }
    
    @Test
    public void testWith() {
        this.hateosLinkIdAndCheck(
            CURRENCY_EXCHANGE.text()
        );
    }

    @Override
    public CurrencyExchangeHateosResource createHateosResource() {
        return CurrencyExchangeHateosResource.with(CURRENCY_EXCHANGE);
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrintable() {
        this.treePrintAndCheck(
            CurrencyExchangeHateosResource.with(CURRENCY_EXCHANGE),
            "AUD to NZD\n"
        );
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        this.compareToAndCheckLess(
            CurrencyExchangeHateosResource.with(
                CurrencyExchange.parse("CAD-NZD")
            )
        );
    }

    @Override
    public CurrencyExchangeHateosResource createComparable() {
        return CurrencyExchangeHateosResource.with(CURRENCY_EXCHANGE);
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "\"AUD-NZD\""
        );
    }

    @Override
    public CurrencyExchangeHateosResource unmarshall(final JsonNode node,
                                                     final JsonNodeUnmarshallContext context) {
        return CurrencyExchangeHateosResource.unmarshall(
            node,
            context
        );
    }

    @Override
    public CurrencyExchangeHateosResource createJsonNodeMarshallingValue() {
        return this.createComparable();
    }

    // class............................................................................................................

    @Override
    public Class<CurrencyExchangeHateosResource> type() {
        return CurrencyExchangeHateosResource.class;
    }
}
