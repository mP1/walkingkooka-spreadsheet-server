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
import walkingkooka.compare.ComparableTesting2;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.test.ParseStringTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CurrencyCodeTest implements ComparableTesting2<CurrencyCode>,
    JsonNodeMarshallingTesting<CurrencyCode>,
    ClassTesting2<CurrencyCode>,
    ParseStringTesting<CurrencyCode> {

    private final static Currency CURRENCY = Currency.getInstance("AUD");

    @Test
    public void testFromCurrencyNullCurrencyFails() {
        assertThrows(
            NullPointerException.class,
            () -> CurrencyCode.fromCurrency(null)
        );
    }

    @Test
    public void testFromCurrency() {
        final CurrencyCode currencyCode = CurrencyCode.fromCurrency(CURRENCY);
        this.checkEquals(
            CURRENCY.getCurrencyCode(),
            currencyCode.value()
        );
    }

    // parse............................................................................................................

    @Override
    public void testParseStringEmptyFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testParse() {
        this.parseStringAndCheck(
            "AUD",
            CurrencyCode.fromCurrency(CURRENCY)
        );
    }

    @Override
    public CurrencyCode parseString(final String text) {
        return CurrencyCode.parse(text);
    }

    @Override
    public Class<? extends RuntimeException> parseStringFailedExpected(final Class<? extends RuntimeException> thrown) {
        return thrown;
    }

    @Override
    public RuntimeException parseStringFailedExpected(final RuntimeException thrown) {
        return thrown;
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        this.compareToAndCheckLess(
            CurrencyCode.fromCurrency(
                Currency.getInstance("NZD")
            )
        );
    }

    @Override
    public CurrencyCode createComparable() {
        return CurrencyCode.fromCurrency(CURRENCY);
    }

    // json.............................................................................................................

    @Override
    public CurrencyCode unmarshall(final JsonNode node,
                                   final JsonNodeUnmarshallContext context) {
        return CurrencyCode.unmarshall(
            node,
            context
        );
    }

    @Override
    public CurrencyCode createJsonNodeMarshallingValue() {
        return CurrencyCode.fromCurrency(CURRENCY);
    }

    // class............................................................................................................

    @Override
    public Class<CurrencyCode> type() {
        return CurrencyCode.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
