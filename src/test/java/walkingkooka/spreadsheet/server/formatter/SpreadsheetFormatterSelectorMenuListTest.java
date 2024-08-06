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

package walkingkooka.spreadsheet.server.formatter;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.ImmutableListTesting;
import walkingkooka.collect.list.ListTesting2;
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpreadsheetFormatterSelectorMenuListTest implements ListTesting2<SpreadsheetFormatterSelectorMenuList, SpreadsheetFormatterSelectorMenu>,
        ClassTesting<SpreadsheetFormatterSelectorMenuList>,
        ImmutableListTesting<SpreadsheetFormatterSelectorMenuList, SpreadsheetFormatterSelectorMenu>,
        JsonNodeMarshallingTesting<SpreadsheetFormatterSelectorMenuList>,
        SpreadsheetMetadataTesting {

    private final static SpreadsheetFormatterSelectorMenu MENU1 = SpreadsheetFormatterSelectorMenu.with(
            "Short",
            SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setText("yy/mm")
    );

    private final static SpreadsheetFormatterSelectorMenu MENU2 = SpreadsheetFormatterSelectorMenu.with(
            "Long",
            SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setText("yyyy/mm/dd")
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetFormatterSelectorMenuList.with(null)
        );
    }

    @Test
    public void testDoesntDoubleWrap() {
        final SpreadsheetFormatterSelectorMenuList list = this.createList();
        assertSame(
                list,
                SpreadsheetFormatterSelectorMenuList.with(list)
        );
    }

    @Test
    public void testGet() {
        this.getAndCheck(
                this.createList(),
                0, // index
                MENU1 // expected
        );
    }

    @Test
    public void testGet2() {
        this.getAndCheck(
                this.createList(),
                1, // index
                MENU2 // expected
        );
    }

    @Test
    public void testSetFails() {
        this.setFails(
                this.createList(),
                0, // index
                MENU1 // expected
        );
    }

    @Test
    public void testRemoveIndexFails() {
        final SpreadsheetFormatterSelectorMenuList list = this.createList();

        this.removeIndexFails(
                list,
                0
        );
    }

    @Test
    public void testRemoveElementFails() {
        final SpreadsheetFormatterSelectorMenuList list = this.createList();

        this.removeFails(
                list,
                list.get(0)
        );
    }

    @Override
    public SpreadsheetFormatterSelectorMenuList createList() {
        return SpreadsheetFormatterSelectorMenuList.with(
                Lists.of(
                        MENU1,
                        MENU2
                )
        );
    }

    @Override
    public Class<SpreadsheetFormatterSelectorMenuList> type() {
        return SpreadsheetFormatterSelectorMenuList.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // Json...........................................................................................................

    private final static String JSON = "[\n" +
            "  {\n" +
            "    \"label\": \"Short\",\n" +
            "    \"selector\": \"date-format-pattern yy/mm\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"label\": \"Long\",\n" +
            "    \"selector\": \"text-format-pattern yyyy/mm/dd\"\n" +
            "  }\n" +
            "]";

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
                this.createList(),
                JSON
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
                JSON,
                this.createList()
        );
    }

    @Override
    public SpreadsheetFormatterSelectorMenuList unmarshall(final JsonNode json,
                                                           final JsonNodeUnmarshallContext context) {
        return SpreadsheetFormatterSelectorMenuList.unmarshall(
                json,
                context
        );
    }

    @Override
    public SpreadsheetFormatterSelectorMenuList createJsonNodeMarshallingValue() {
        return this.createList();
    }

    // prepare..........................................................................................................

    @Test
    public void testPrepareWithSpreadsheetFormatterProviderFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetFormatterSelectorMenuList.prepare(
                        null,
                        SPREADSHEET_FORMATTER_CONTEXT
                )
        );
    }

    @Test
    public void testPrepareWithSpreadsheetFormatterContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetFormatterSelectorMenuList.prepare(
                        SPREADSHEET_FORMATTER_PROVIDER,
                        null
                )
        );
    }

    @Test
    public void testPrepare() {
        this.checkEquals(
                Lists.of(
                        SpreadsheetFormatterSelectorMenu.with(
                                "Short",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setText("d/m/yy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Medium",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setText("d mmm yyyy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Long",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setText("d mmmm yyyy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Full",
                                SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setText("dddd, d mmmm yyyy")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Short",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setText("d/m/yy, h:mm AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Medium",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setText("d mmm yyyy, h:mm:ss AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Long",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setText("d mmmm yyyy \\a\\t h:mm:ss AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Full",
                                SpreadsheetFormatterName.DATE_TIME_FORMAT_PATTERN.setText("dddd, d mmmm yyyy \\a\\t h:mm:ss AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "General",
                                SpreadsheetFormatterName.GENERAL.setText("")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Number",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setText("\"#,##0.###\"")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Integer",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setText("#,##0")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Percent",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setText("#,##0%")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Currency",
                                SpreadsheetFormatterName.NUMBER_FORMAT_PATTERN.setText("$#,##0.00")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Default",
                                SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setText("@")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Short",
                                SpreadsheetFormatterName.TIME_FORMAT_PATTERN.setText("h:mm AM/PM")
                        ),
                        SpreadsheetFormatterSelectorMenu.with(
                                "Long",
                                SpreadsheetFormatterName.TIME_FORMAT_PATTERN.setText("h:mm:ss AM/PM")
                        )
                ),
                SpreadsheetFormatterSelectorMenuList.prepare(
                        SPREADSHEET_FORMATTER_PROVIDER,
                        SPREADSHEET_FORMATTER_CONTEXT
                )
        );
    }
}
