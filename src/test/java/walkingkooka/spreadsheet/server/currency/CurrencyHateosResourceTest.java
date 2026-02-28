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
import walkingkooka.currency.HasCurrencyTesting;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.HasTextTesting;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CurrencyHateosResourceTest implements ComparableTesting2<CurrencyHateosResource>,
    HasTextTesting,
    HasCurrencyTesting,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<CurrencyHateosResource>,
    ClassTesting2<CurrencyHateosResource> {

    private final static Currency CURRENCY = Currency.getInstance("AUD");

    private final static CurrencyCode CURRENCY_CODE = CurrencyCode.fromCurrency(CURRENCY);

    private final static String TEXT = "Australian Dollar";

    @Test
    public void testWithNullCurrencyCodeFails() {
        assertThrows(
            NullPointerException.class,
            () -> CurrencyHateosResource.with(
                null,
                TEXT
            )
        );
    }

    @Test
    public void testWithNullTextFails() {
        assertThrows(
            NullPointerException.class,
            () -> CurrencyHateosResource.with(
                CURRENCY_CODE,
                null
            )
        );
    }

    @Test
    public void testWithEmptyTextFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> CurrencyHateosResource.with(
                CURRENCY_CODE,
                ""
            )
        );
    }


    @Test
    public void testWith() {
        final CurrencyHateosResource resource = CurrencyHateosResource.with(
            CURRENCY_CODE,
            TEXT
        );

        this.checkEquals(
            CurrencyCode.fromCurrency(CURRENCY),
            resource.value()
        );

        this.checkEquals(
            "AUD",
            resource.hateosLinkId()
        );

        this.textAndCheck(
            resource,
            TEXT
        );
    }

    // HasText..........................................................................................................

    @Test
    public void testText() {
        this.textAndCheck(
            CurrencyHateosResource.with(
                CURRENCY_CODE,
                TEXT
            ),
            TEXT
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrintable() {
        this.treePrintAndCheck(
            CurrencyHateosResource.with(
                CURRENCY_CODE,
                TEXT
            ),
            "AUD\n" +
                "  Australian Dollar\n"
        );
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        this.compareToAndCheckLess(
            CurrencyHateosResource.with(
                CurrencyCode.fromCurrency(
                    Currency.getInstance("NZD")
                ),
                "New Zealand ...."
            )
        );
    }

    @Override
    public CurrencyHateosResource createComparable() {
        return CurrencyHateosResource.with(
            CURRENCY_CODE,
            TEXT
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "{\n" +
                "  \"currencyCode\": \"AUD\",\n" +
                "  \"text\": \"Australian Dollar\"\n" +
                "}"
        );
    }

    @Override
    public CurrencyHateosResource unmarshall(final JsonNode node,
                                             final JsonNodeUnmarshallContext context) {
        return CurrencyHateosResource.unmarshall(
            node,
            context
        );
    }

    @Override
    public CurrencyHateosResource createJsonNodeMarshallingValue() {
        return CurrencyHateosResource.with(
            CURRENCY_CODE,
            TEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<CurrencyHateosResource> type() {
        return CurrencyHateosResource.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
