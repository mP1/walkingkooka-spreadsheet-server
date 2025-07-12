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
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class DecimalNumberSymbolsHateosResourceSetTest implements ImmutableSortedSetTesting<DecimalNumberSymbolsHateosResourceSet, DecimalNumberSymbolsHateosResource>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<DecimalNumberSymbolsHateosResourceSet> {

    private final static DecimalNumberSymbolsHateosResource EN_AU = DecimalNumberSymbolsHateosResource.fromLocale(
        Locale.forLanguageTag("EN-AU")
    );

    private final static DecimalNumberSymbolsHateosResource EN_NZ = DecimalNumberSymbolsHateosResource.fromLocale(
        Locale.forLanguageTag("EN-NZ")
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> DecimalNumberSymbolsHateosResourceSet.with(null)
        );
    }

    @Test
    public void testDeleteBecomesEmpty() {
        assertSame(
            DecimalNumberSymbolsHateosResourceSet.EMPTY,
            DecimalNumberSymbolsHateosResourceSet.EMPTY.concat(EN_AU)
                .delete(EN_AU)
        );
    }

    @Test
    public void testSetElementsWithDecimalNumberSymbolsHateosResourceSet() {
        final DecimalNumberSymbolsHateosResourceSet set = DecimalNumberSymbolsHateosResourceSet.with(
            SortedSets.of(
                DecimalNumberSymbolsHateosResource.fromLocale(Locale.US)
            )
        );

        assertSame(
            set,
            set.setElements(set)
        );
    }

    @Test
    public void testSetElementsWithDecimalNumberSymbolsHateosResourceSet2() {
        final DecimalNumberSymbolsHateosResourceSet set = this.createSet();
        final DecimalNumberSymbolsHateosResourceSet set2 = this.createSet();

        assertSame(
            set2,
            set.setElements(
                set2
            )
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResourceSet createSet() {
        final SortedSet<DecimalNumberSymbolsHateosResource> sortedSet = SortedSets.tree();

        final DecimalNumberSymbolsHateosResource english = DecimalNumberSymbolsHateosResource.fromLocale(
            Locale.forLanguageTag("EN")
        );

        sortedSet.add(english);
        sortedSet.add(EN_AU);
        sortedSet.add(EN_NZ);

        return DecimalNumberSymbolsHateosResourceSet.with(
            SortedSets.of(
                english,
                EN_AU,
                EN_NZ
            )
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.createSet(),
            "en\n" +
                "  DecimalNumberSymbols\n" +
                "    negativeSign\n" +
                "      '-'\n" +
                "    positiveSign\n" +
                "      '+'\n" +
                "    zeroDigit\n" +
                "      '0'\n" +
                "    currencySymbol\n" +
                "      \"¤\"\n" +
                "    decimalSeparator\n" +
                "      '.'\n" +
                "    exponentSymbol\n" +
                "      \"E\"\n" +
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
                "      '‰'\n" +
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
                "      '‰'\n" +
                "en-NZ\n" +
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
                "      \"E\"\n" +
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

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "[\n" +
                "  {\n" +
                "    \"localeTag\": \"en\",\n" +
                "    \"decimalNumberSymbols\": {\n" +
                "      \"negativeSign\": \"-\",\n" +
                "      \"positiveSign\": \"+\",\n" +
                "      \"zeroDigit\": \"0\",\n" +
                "      \"currencySymbol\": \"¤\",\n" +
                "      \"decimalSeparator\": \".\",\n" +
                "      \"exponentSymbol\": \"E\",\n" +
                "      \"groupSeparator\": \",\",\n" +
                "      \"infinitySymbol\": \"∞\",\n" +
                "      \"monetaryDecimalSeparator\": \".\",\n" +
                "      \"nanSymbol\": \"NaN\",\n" +
                "      \"percentSymbol\": \"%\",\n" +
                "      \"permillSymbol\": \"‰\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-AU\",\n" +
                "    \"decimalNumberSymbols\": {\n" +
                "      \"negativeSign\": \"-\",\n" +
                "      \"positiveSign\": \"+\",\n" +
                "      \"zeroDigit\": \"0\",\n" +
                "      \"currencySymbol\": \"$\",\n" +
                "      \"decimalSeparator\": \".\",\n" +
                "      \"exponentSymbol\": \"e\",\n" +
                "      \"groupSeparator\": \",\",\n" +
                "      \"infinitySymbol\": \"∞\",\n" +
                "      \"monetaryDecimalSeparator\": \".\",\n" +
                "      \"nanSymbol\": \"NaN\",\n" +
                "      \"percentSymbol\": \"%\",\n" +
                "      \"permillSymbol\": \"‰\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-NZ\",\n" +
                "    \"decimalNumberSymbols\": {\n" +
                "      \"negativeSign\": \"-\",\n" +
                "      \"positiveSign\": \"+\",\n" +
                "      \"zeroDigit\": \"0\",\n" +
                "      \"currencySymbol\": \"$\",\n" +
                "      \"decimalSeparator\": \".\",\n" +
                "      \"exponentSymbol\": \"E\",\n" +
                "      \"groupSeparator\": \",\",\n" +
                "      \"infinitySymbol\": \"∞\",\n" +
                "      \"monetaryDecimalSeparator\": \".\",\n" +
                "      \"nanSymbol\": \"NaN\",\n" +
                "      \"percentSymbol\": \"%\",\n" +
                "      \"permillSymbol\": \"‰\"\n" +
                "    }\n" +
                "  }\n" +
                "]"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "[\n" +
                "  {\n" +
                "    \"localeTag\": \"en\",\n" +
                "    \"decimalNumberSymbols\": {\n" +
                "      \"negativeSign\": \"-\",\n" +
                "      \"positiveSign\": \"+\",\n" +
                "      \"zeroDigit\": \"0\",\n" +
                "      \"currencySymbol\": \"¤\",\n" +
                "      \"decimalSeparator\": \".\",\n" +
                "      \"exponentSymbol\": \"E\",\n" +
                "      \"groupSeparator\": \",\",\n" +
                "      \"infinitySymbol\": \"∞\",\n" +
                "      \"monetaryDecimalSeparator\": \".\",\n" +
                "      \"nanSymbol\": \"NaN\",\n" +
                "      \"percentSymbol\": \"%\",\n" +
                "      \"permillSymbol\": \"‰\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-AU\",\n" +
                "    \"decimalNumberSymbols\": {\n" +
                "      \"negativeSign\": \"-\",\n" +
                "      \"positiveSign\": \"+\",\n" +
                "      \"zeroDigit\": \"0\",\n" +
                "      \"currencySymbol\": \"$\",\n" +
                "      \"decimalSeparator\": \".\",\n" +
                "      \"exponentSymbol\": \"e\",\n" +
                "      \"groupSeparator\": \",\",\n" +
                "      \"infinitySymbol\": \"∞\",\n" +
                "      \"monetaryDecimalSeparator\": \".\",\n" +
                "      \"nanSymbol\": \"NaN\",\n" +
                "      \"percentSymbol\": \"%\",\n" +
                "      \"permillSymbol\": \"‰\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-NZ\",\n" +
                "    \"decimalNumberSymbols\": {\n" +
                "      \"negativeSign\": \"-\",\n" +
                "      \"positiveSign\": \"+\",\n" +
                "      \"zeroDigit\": \"0\",\n" +
                "      \"currencySymbol\": \"$\",\n" +
                "      \"decimalSeparator\": \".\",\n" +
                "      \"exponentSymbol\": \"E\",\n" +
                "      \"groupSeparator\": \",\",\n" +
                "      \"infinitySymbol\": \"∞\",\n" +
                "      \"monetaryDecimalSeparator\": \".\",\n" +
                "      \"nanSymbol\": \"NaN\",\n" +
                "      \"percentSymbol\": \"%\",\n" +
                "      \"permillSymbol\": \"‰\"\n" +
                "    }\n" +
                "  }\n" +
                "]",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Test
    public void testMarshallUnmarshallAllAvailableDecimalNumberSymbolsHateosResources() {
        final SortedSet<DecimalNumberSymbolsHateosResource> locales = SortedSets.tree();
        locales.addAll(
                Arrays.stream(
                    Locale.getAvailableLocales()
                ).filter(l -> false == l.getLanguage().contains("ar") // BUG: JRE has some AR locales with with RTL \\u200E and not the actual printable char.
            ).map(DecimalNumberSymbolsHateosResource::fromLocale)
            .collect(Collectors.toList())
        );

        this.checkNotEquals(
            SortedSets.empty(),
            locales
        );

        this.marshallRoundTripTwiceAndCheck(
            DecimalNumberSymbolsHateosResourceSet.with(locales)
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResourceSet unmarshall(final JsonNode jsonNode,
                                                            final JsonNodeUnmarshallContext context) {
        return DecimalNumberSymbolsHateosResourceSet.unmarshall(
            jsonNode,
            context
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResourceSet createJsonNodeMarshallingValue() {
        return this.createSet();
    }

    // class............................................................................................................

    @Override
    public Class<DecimalNumberSymbolsHateosResourceSet> type() {
        return DecimalNumberSymbolsHateosResourceSet.class;
    }
}