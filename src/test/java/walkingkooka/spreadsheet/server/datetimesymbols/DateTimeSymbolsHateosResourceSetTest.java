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

package walkingkooka.spreadsheet.server.datetimesymbols;

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

public final class DateTimeSymbolsHateosResourceSetTest implements ImmutableSortedSetTesting<DateTimeSymbolsHateosResourceSet, DateTimeSymbolsHateosResource>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<DateTimeSymbolsHateosResourceSet> {

    private final static DateTimeSymbolsHateosResource EN_AU = DateTimeSymbolsHateosResource.fromLocale(
        Locale.forLanguageTag("EN-AU")
    );

    private final static DateTimeSymbolsHateosResource EN_NZ = DateTimeSymbolsHateosResource.fromLocale(
        Locale.forLanguageTag("EN-NZ")
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> DateTimeSymbolsHateosResourceSet.with(null)
        );
    }

    @Test
    public void testDeleteBecomesEmpty() {
        assertSame(
            DateTimeSymbolsHateosResourceSet.EMPTY,
            DateTimeSymbolsHateosResourceSet.EMPTY.concat(EN_AU)
                .delete(EN_AU)
        );
    }

    @Test
    public void testSetElementsWithDateTimeSymbolsHateosResourceSet() {
        final DateTimeSymbolsHateosResourceSet set = DateTimeSymbolsHateosResourceSet.with(
            SortedSets.of(
                DateTimeSymbolsHateosResource.fromLocale(Locale.US)
            )
        );

        assertSame(
            set,
            set.setElements(set)
        );
    }

    @Test
    public void testSetElementsWithDateTimeSymbolsHateosResourceSet2() {
        final DateTimeSymbolsHateosResourceSet set = this.createSet();
        final DateTimeSymbolsHateosResourceSet set2 = this.createSet();

        assertSame(
            set2,
            set.setElements(
                set2
            )
        );
    }

    @Override
    public DateTimeSymbolsHateosResourceSet createSet() {
        final SortedSet<DateTimeSymbolsHateosResource> sortedSet = SortedSets.tree();

        final DateTimeSymbolsHateosResource english = DateTimeSymbolsHateosResource.fromLocale(
            Locale.forLanguageTag("EN")
        );

        sortedSet.add(english);
        sortedSet.add(EN_AU);
        sortedSet.add(EN_NZ);

        return DateTimeSymbolsHateosResourceSet.with(
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
                "  English\n" +
                "  DateTimeSymbols\n" +
                "    ampms\n" +
                "      AM\n" +
                "      PM\n" +
                "    monthNames\n" +
                "      January\n" +
                "      February\n" +
                "      March\n" +
                "      April\n" +
                "      May\n" +
                "      June\n" +
                "      July\n" +
                "      August\n" +
                "      September\n" +
                "      October\n" +
                "      November\n" +
                "      December\n" +
                "    monthNameAbbreviations\n" +
                "      Jan\n" +
                "      Feb\n" +
                "      Mar\n" +
                "      Apr\n" +
                "      May\n" +
                "      Jun\n" +
                "      Jul\n" +
                "      Aug\n" +
                "      Sep\n" +
                "      Oct\n" +
                "      Nov\n" +
                "      Dec\n" +
                "    weekDayNames\n" +
                "      Sunday\n" +
                "      Monday\n" +
                "      Tuesday\n" +
                "      Wednesday\n" +
                "      Thursday\n" +
                "      Friday\n" +
                "      Saturday\n" +
                "    weekDayNameAbbreviations\n" +
                "      Sun\n" +
                "      Mon\n" +
                "      Tue\n" +
                "      Wed\n" +
                "      Thu\n" +
                "      Fri\n" +
                "      Sat\n" +
                "en-AU\n" +
                "  English (Australia)\n" +
                "  DateTimeSymbols\n" +
                "    ampms\n" +
                "      am\n" +
                "      pm\n" +
                "    monthNames\n" +
                "      January\n" +
                "      February\n" +
                "      March\n" +
                "      April\n" +
                "      May\n" +
                "      June\n" +
                "      July\n" +
                "      August\n" +
                "      September\n" +
                "      October\n" +
                "      November\n" +
                "      December\n" +
                "    monthNameAbbreviations\n" +
                "      Jan.\n" +
                "      Feb.\n" +
                "      Mar.\n" +
                "      Apr.\n" +
                "      May\n" +
                "      Jun.\n" +
                "      Jul.\n" +
                "      Aug.\n" +
                "      Sep.\n" +
                "      Oct.\n" +
                "      Nov.\n" +
                "      Dec.\n" +
                "    weekDayNames\n" +
                "      Sunday\n" +
                "      Monday\n" +
                "      Tuesday\n" +
                "      Wednesday\n" +
                "      Thursday\n" +
                "      Friday\n" +
                "      Saturday\n" +
                "    weekDayNameAbbreviations\n" +
                "      Sun.\n" +
                "      Mon.\n" +
                "      Tue.\n" +
                "      Wed.\n" +
                "      Thu.\n" +
                "      Fri.\n" +
                "      Sat.\n" +
                "en-NZ\n" +
                "  English (New Zealand)\n" +
                "  DateTimeSymbols\n" +
                "    ampms\n" +
                "      AM\n" +
                "      PM\n" +
                "    monthNames\n" +
                "      January\n" +
                "      February\n" +
                "      March\n" +
                "      April\n" +
                "      May\n" +
                "      June\n" +
                "      July\n" +
                "      August\n" +
                "      September\n" +
                "      October\n" +
                "      November\n" +
                "      December\n" +
                "    monthNameAbbreviations\n" +
                "      Jan\n" +
                "      Feb\n" +
                "      Mar\n" +
                "      Apr\n" +
                "      May\n" +
                "      Jun\n" +
                "      Jul\n" +
                "      Aug\n" +
                "      Sep\n" +
                "      Oct\n" +
                "      Nov\n" +
                "      Dec\n" +
                "    weekDayNames\n" +
                "      Sunday\n" +
                "      Monday\n" +
                "      Tuesday\n" +
                "      Wednesday\n" +
                "      Thursday\n" +
                "      Friday\n" +
                "      Saturday\n" +
                "    weekDayNameAbbreviations\n" +
                "      Sun\n" +
                "      Mon\n" +
                "      Tue\n" +
                "      Wed\n" +
                "      Thu\n" +
                "      Fri\n" +
                "      Sat\n"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "[\n" +
                "  {\n" +
                "    \"localeTag\": \"en\",\n" +
                "    \"text\": \"English\",\n" +
                "    \"dateTimeSymbols\": {\n" +
                "      \"ampms\": [\n" +
                "        \"AM\",\n" +
                "        \"PM\"\n" +
                "      ],\n" +
                "      \"monthNames\": [\n" +
                "        \"January\",\n" +
                "        \"February\",\n" +
                "        \"March\",\n" +
                "        \"April\",\n" +
                "        \"May\",\n" +
                "        \"June\",\n" +
                "        \"July\",\n" +
                "        \"August\",\n" +
                "        \"September\",\n" +
                "        \"October\",\n" +
                "        \"November\",\n" +
                "        \"December\"\n" +
                "      ],\n" +
                "      \"monthNameAbbreviations\": [\n" +
                "        \"Jan\",\n" +
                "        \"Feb\",\n" +
                "        \"Mar\",\n" +
                "        \"Apr\",\n" +
                "        \"May\",\n" +
                "        \"Jun\",\n" +
                "        \"Jul\",\n" +
                "        \"Aug\",\n" +
                "        \"Sep\",\n" +
                "        \"Oct\",\n" +
                "        \"Nov\",\n" +
                "        \"Dec\"\n" +
                "      ],\n" +
                "      \"weekDayNames\": [\n" +
                "        \"Sunday\",\n" +
                "        \"Monday\",\n" +
                "        \"Tuesday\",\n" +
                "        \"Wednesday\",\n" +
                "        \"Thursday\",\n" +
                "        \"Friday\",\n" +
                "        \"Saturday\"\n" +
                "      ],\n" +
                "      \"weekDayNameAbbreviations\": [\n" +
                "        \"Sun\",\n" +
                "        \"Mon\",\n" +
                "        \"Tue\",\n" +
                "        \"Wed\",\n" +
                "        \"Thu\",\n" +
                "        \"Fri\",\n" +
                "        \"Sat\"\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-AU\",\n" +
                "    \"text\": \"English (Australia)\",\n" +
                "    \"dateTimeSymbols\": {\n" +
                "      \"ampms\": [\n" +
                "        \"am\",\n" +
                "        \"pm\"\n" +
                "      ],\n" +
                "      \"monthNames\": [\n" +
                "        \"January\",\n" +
                "        \"February\",\n" +
                "        \"March\",\n" +
                "        \"April\",\n" +
                "        \"May\",\n" +
                "        \"June\",\n" +
                "        \"July\",\n" +
                "        \"August\",\n" +
                "        \"September\",\n" +
                "        \"October\",\n" +
                "        \"November\",\n" +
                "        \"December\"\n" +
                "      ],\n" +
                "      \"monthNameAbbreviations\": [\n" +
                "        \"Jan.\",\n" +
                "        \"Feb.\",\n" +
                "        \"Mar.\",\n" +
                "        \"Apr.\",\n" +
                "        \"May\",\n" +
                "        \"Jun.\",\n" +
                "        \"Jul.\",\n" +
                "        \"Aug.\",\n" +
                "        \"Sep.\",\n" +
                "        \"Oct.\",\n" +
                "        \"Nov.\",\n" +
                "        \"Dec.\"\n" +
                "      ],\n" +
                "      \"weekDayNames\": [\n" +
                "        \"Sunday\",\n" +
                "        \"Monday\",\n" +
                "        \"Tuesday\",\n" +
                "        \"Wednesday\",\n" +
                "        \"Thursday\",\n" +
                "        \"Friday\",\n" +
                "        \"Saturday\"\n" +
                "      ],\n" +
                "      \"weekDayNameAbbreviations\": [\n" +
                "        \"Sun.\",\n" +
                "        \"Mon.\",\n" +
                "        \"Tue.\",\n" +
                "        \"Wed.\",\n" +
                "        \"Thu.\",\n" +
                "        \"Fri.\",\n" +
                "        \"Sat.\"\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"localeTag\": \"en-NZ\",\n" +
                "    \"text\": \"English (New Zealand)\",\n" +
                "    \"dateTimeSymbols\": {\n" +
                "      \"ampms\": [\n" +
                "        \"AM\",\n" +
                "        \"PM\"\n" +
                "      ],\n" +
                "      \"monthNames\": [\n" +
                "        \"January\",\n" +
                "        \"February\",\n" +
                "        \"March\",\n" +
                "        \"April\",\n" +
                "        \"May\",\n" +
                "        \"June\",\n" +
                "        \"July\",\n" +
                "        \"August\",\n" +
                "        \"September\",\n" +
                "        \"October\",\n" +
                "        \"November\",\n" +
                "        \"December\"\n" +
                "      ],\n" +
                "      \"monthNameAbbreviations\": [\n" +
                "        \"Jan\",\n" +
                "        \"Feb\",\n" +
                "        \"Mar\",\n" +
                "        \"Apr\",\n" +
                "        \"May\",\n" +
                "        \"Jun\",\n" +
                "        \"Jul\",\n" +
                "        \"Aug\",\n" +
                "        \"Sep\",\n" +
                "        \"Oct\",\n" +
                "        \"Nov\",\n" +
                "        \"Dec\"\n" +
                "      ],\n" +
                "      \"weekDayNames\": [\n" +
                "        \"Sunday\",\n" +
                "        \"Monday\",\n" +
                "        \"Tuesday\",\n" +
                "        \"Wednesday\",\n" +
                "        \"Thursday\",\n" +
                "        \"Friday\",\n" +
                "        \"Saturday\"\n" +
                "      ],\n" +
                "      \"weekDayNameAbbreviations\": [\n" +
                "        \"Sun\",\n" +
                "        \"Mon\",\n" +
                "        \"Tue\",\n" +
                "        \"Wed\",\n" +
                "        \"Thu\",\n" +
                "        \"Fri\",\n" +
                "        \"Sat\"\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "]",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Test
    public void testMarshallUnmarshallAllAvailableDateTimeSymbolsHateosResources() {
        final SortedSet<DateTimeSymbolsHateosResource> locales = SortedSets.tree();
        locales.addAll(
            Arrays.stream(
                    Locale.getAvailableLocales()
                ).filter(l -> false == l.getDisplayName().isEmpty())
                .map(DateTimeSymbolsHateosResource::fromLocale)
                .collect(Collectors.toList())
        );

        this.checkNotEquals(
            SortedSets.empty(),
            locales
        );

        this.marshallRoundTripTwiceAndCheck(
            DateTimeSymbolsHateosResourceSet.with(locales)
        );
    }

    @Override
    public DateTimeSymbolsHateosResourceSet unmarshall(final JsonNode jsonNode,
                                                       final JsonNodeUnmarshallContext context) {
        return DateTimeSymbolsHateosResourceSet.unmarshall(
            jsonNode,
            context
        );
    }

    @Override
    public DateTimeSymbolsHateosResourceSet createJsonNodeMarshallingValue() {
        return this.createSet();
    }

    // class............................................................................................................

    @Override
    public Class<DateTimeSymbolsHateosResourceSet> type() {
        return DateTimeSymbolsHateosResourceSet.class;
    }
}