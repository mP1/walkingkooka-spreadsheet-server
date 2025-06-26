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

package walkingkooka.spreadsheet.server.decimalnumbersymbols;

import org.junit.jupiter.api.Test;
import walkingkooka.compare.ComparableTesting2;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.locale.LocaleTag;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class DecimalNumberSymbolsHateosResourceTest implements ComparableTesting2<DecimalNumberSymbolsHateosResource>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<DecimalNumberSymbolsHateosResource>,
    ClassTesting2<DecimalNumberSymbolsHateosResource> {

    private final static Locale LOCALE = Locale.forLanguageTag("EN-AU");

    private final static LocaleTag LOCALE_TAG = LocaleTag.with(LOCALE);

    private final static DecimalNumberSymbols DECIMAL_NUMBER_SYMBOLS = DecimalNumberSymbols.fromDecimalFormatSymbols(
        '+',
        new DecimalFormatSymbols(LOCALE)
    );

    @Test
    public void testWithNullLocaleTagFails() {
        assertThrows(
            NullPointerException.class,
            () -> DecimalNumberSymbolsHateosResource.with(
                null,
                DECIMAL_NUMBER_SYMBOLS
            )
        );
    }

    @Test
    public void testWithNullDecimalNumberSymbolsFails() {
        assertThrows(
            NullPointerException.class,
            () -> DecimalNumberSymbolsHateosResource.with(
                LOCALE_TAG,
                null
            )
        );
    }

    @Test
    public void testWith() {
        final DecimalNumberSymbolsHateosResource resource = DecimalNumberSymbolsHateosResource.with(
            LOCALE_TAG,
            DECIMAL_NUMBER_SYMBOLS
        );
        this.checkEquals(
            DECIMAL_NUMBER_SYMBOLS,
            resource.value()
        );

        this.checkEquals(
            "en-AU",
            resource.hateosLinkId()
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrintable() {
        this.treePrintAndCheck(
            DecimalNumberSymbolsHateosResource.with(
                LOCALE_TAG,
                DECIMAL_NUMBER_SYMBOLS
            ),
            "en-AU\n" +
                "  DecimalNumberSymbols\n" +
                "    negativeSign\n" +
                "      '-'\n" +
                "    positiveSign\n" +
                "      '+'\n" +
                "    zeroDigit\n" +
                "      '0'\n" +
                "    currencySymbol\n" +
                "      \"$\"\n" +
                "    decimalSeparator\n" +
                "      '.'\n" +
                "    exponentSymbol\n" +
                "      \"e\"\n" +
                "    groupSeparator\n" +
                "      ','\n" +
                "    infinitySymbol\n" +
                "      \"∞\"\n" +
                "    monetaryDecimalSeparator\n" +
                "      '.'\n" +
                "    nanSymbol\n" +
                "      \"NaN\"\n" +
                "    percentSymbol\n" +
                "      '%'\n" +
                "    permillSymbol\n" +
                "      '‰'\n"
        );
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        final Locale locale = Locale.FRANCE;

        this.compareToAndCheckLess(
            DecimalNumberSymbolsHateosResource.with(
                LocaleTag.with(
                    locale
                ),
                DecimalNumberSymbols.fromDecimalFormatSymbols(
                    '+',
                    new DecimalFormatSymbols(locale)
                )
            )
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResource createComparable() {
        return DecimalNumberSymbolsHateosResource.with(
            LOCALE_TAG,
            DECIMAL_NUMBER_SYMBOLS
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "{\n" +
                "  \"localeTag\": \"en-AU\",\n" +
                "  \"decimalNumberSymbols\": {\n" +
                "    \"negativeSign\": \"-\",\n" +
                "    \"positiveSign\": \"+\",\n" +
                "    \"zeroDigit\": \"0\",\n" +
                "    \"currencySymbol\": \"$\",\n" +
                "    \"decimalSeparator\": \".\",\n" +
                "    \"exponentSymbol\": \"e\",\n" +
                "    \"groupSeparator\": \",\",\n" +
                "    \"infinitySymbol\": \"∞\",\n" +
                "    \"monetaryDecimalSeparator\": \".\",\n" +
                "    \"nanSymbol\": \"NaN\",\n" +
                "    \"percentSymbol\": \"%\",\n" +
                "    \"permillSymbol\": \"‰\"\n" +
                "  }\n" +
                "}"
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResource unmarshall(final JsonNode node,
                                                    final JsonNodeUnmarshallContext context) {
        return DecimalNumberSymbolsHateosResource.unmarshall(
            node,
            context
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResource createJsonNodeMarshallingValue() {
        return DecimalNumberSymbolsHateosResource.with(
            LOCALE_TAG,
            DECIMAL_NUMBER_SYMBOLS
        );
    }

    // class............................................................................................................

    @Override
    public Class<DecimalNumberSymbolsHateosResource> type() {
        return DecimalNumberSymbolsHateosResource.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
