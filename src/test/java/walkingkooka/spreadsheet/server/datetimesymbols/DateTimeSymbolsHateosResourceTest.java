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
import walkingkooka.compare.ComparableTesting2;
import walkingkooka.datetime.DateTimeSymbols;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.locale.LocaleTag;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.text.DateFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class DateTimeSymbolsHateosResourceTest implements ComparableTesting2<DateTimeSymbolsHateosResource>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<DateTimeSymbolsHateosResource>,
    ClassTesting2<DateTimeSymbolsHateosResource> {

    private final static Locale LOCALE = Locale.forLanguageTag("EN-AU");

    private final static LocaleTag LOCALE_TAG = LocaleTag.with(LOCALE);

    private final static DateTimeSymbols DATE_TIME_SYMBOLS = DateTimeSymbols.fromDateFormatSymbols(
        new DateFormatSymbols(LOCALE)
    );

    @Test
    public void testWithNullLocaleTagFails() {
        assertThrows(
            NullPointerException.class,
            () -> DateTimeSymbolsHateosResource.with(
                null,
                DATE_TIME_SYMBOLS
            )
        );
    }

    @Test
    public void testWithNullDateTimeSymbolsFails() {
        assertThrows(
            NullPointerException.class,
            () -> DateTimeSymbolsHateosResource.with(
                LOCALE_TAG,
                null
            )
        );
    }

    @Test
    public void testWith() {
        final DateTimeSymbolsHateosResource resource = DateTimeSymbolsHateosResource.with(
            LOCALE_TAG,
            DATE_TIME_SYMBOLS
        );
        this.checkEquals(
            DATE_TIME_SYMBOLS,
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
            DateTimeSymbolsHateosResource.with(
                LOCALE_TAG,
                DATE_TIME_SYMBOLS
            ),
            "en-AU\n" +
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
                "      Sat.\n"
        );
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        final Locale locale = Locale.FRANCE;

        this.compareToAndCheckLess(
            DateTimeSymbolsHateosResource.with(
                LocaleTag.with(
                    locale
                ),
                DateTimeSymbols.fromDateFormatSymbols(
                    new DateFormatSymbols(locale)
                )
            )
        );
    }

    @Override
    public DateTimeSymbolsHateosResource createComparable() {
        return DateTimeSymbolsHateosResource.with(
            LOCALE_TAG,
            DATE_TIME_SYMBOLS
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "{\n" +
                "  \"localeTag\": \"en-AU\",\n" +
                "  \"dateTimeSymbols\": {\n" +
                "    \"ampms\": [\n" +
                "      \"am\",\n" +
                "      \"pm\"\n" +
                "    ],\n" +
                "    \"monthNames\": [\n" +
                "      \"January\",\n" +
                "      \"February\",\n" +
                "      \"March\",\n" +
                "      \"April\",\n" +
                "      \"May\",\n" +
                "      \"June\",\n" +
                "      \"July\",\n" +
                "      \"August\",\n" +
                "      \"September\",\n" +
                "      \"October\",\n" +
                "      \"November\",\n" +
                "      \"December\"\n" +
                "    ],\n" +
                "    \"monthNameAbbreviations\": [\n" +
                "      \"Jan.\",\n" +
                "      \"Feb.\",\n" +
                "      \"Mar.\",\n" +
                "      \"Apr.\",\n" +
                "      \"May\",\n" +
                "      \"Jun.\",\n" +
                "      \"Jul.\",\n" +
                "      \"Aug.\",\n" +
                "      \"Sep.\",\n" +
                "      \"Oct.\",\n" +
                "      \"Nov.\",\n" +
                "      \"Dec.\"\n" +
                "    ],\n" +
                "    \"weekDayNames\": [\n" +
                "      \"Sunday\",\n" +
                "      \"Monday\",\n" +
                "      \"Tuesday\",\n" +
                "      \"Wednesday\",\n" +
                "      \"Thursday\",\n" +
                "      \"Friday\",\n" +
                "      \"Saturday\"\n" +
                "    ],\n" +
                "    \"weekDayNameAbbreviations\": [\n" +
                "      \"Sun.\",\n" +
                "      \"Mon.\",\n" +
                "      \"Tue.\",\n" +
                "      \"Wed.\",\n" +
                "      \"Thu.\",\n" +
                "      \"Fri.\",\n" +
                "      \"Sat.\"\n" +
                "    ]\n" +
                "  }\n" +
                "}"
        );
    }

    @Override
    public DateTimeSymbolsHateosResource unmarshall(final JsonNode node,
                                                    final JsonNodeUnmarshallContext context) {
        return DateTimeSymbolsHateosResource.unmarshall(
            node,
            context
        );
    }

    @Override
    public DateTimeSymbolsHateosResource createJsonNodeMarshallingValue() {
        return DateTimeSymbolsHateosResource.with(
            LOCALE_TAG,
            DATE_TIME_SYMBOLS
        );
    }

    // class............................................................................................................

    @Override
    public Class<DateTimeSymbolsHateosResource> type() {
        return DateTimeSymbolsHateosResource.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
