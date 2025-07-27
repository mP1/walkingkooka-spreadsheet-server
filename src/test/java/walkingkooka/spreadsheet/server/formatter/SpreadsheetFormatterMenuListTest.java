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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpreadsheetFormatterMenuListTest implements ListTesting2<SpreadsheetFormatterMenuList, SpreadsheetFormatterMenu>,
    ClassTesting<SpreadsheetFormatterMenuList>,
    ImmutableListTesting<SpreadsheetFormatterMenuList, SpreadsheetFormatterMenu>,
    JsonNodeMarshallingTesting<SpreadsheetFormatterMenuList> {

    private final static SpreadsheetFormatterMenu MENU1 = SpreadsheetFormatterMenu.with(
        "Short",
        SpreadsheetFormatterName.DATE_FORMAT_PATTERN.setValueText("yy/mm")
    );

    private final static SpreadsheetFormatterMenu MENU2 = SpreadsheetFormatterMenu.with(
        "Long",
        SpreadsheetFormatterName.TEXT_FORMAT_PATTERN.setValueText("yyyy/mm/dd")
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetFormatterMenuList.with(null)
        );
    }

    @Test
    public void testWithIncludesNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetFormatterMenuList.with(
                Arrays.asList(
                    MENU1,
                    null
                )
            )
        );
    }

    @Test
    public void testDoesntDoubleWrap() {
        final SpreadsheetFormatterMenuList list = this.createList();
        assertSame(
            list,
            SpreadsheetFormatterMenuList.with(list)
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
        final SpreadsheetFormatterMenuList list = this.createList();

        this.removeIndexFails(
            list,
            0
        );
    }

    @Test
    public void testRemoveElementFails() {
        final SpreadsheetFormatterMenuList list = this.createList();

        this.removeFails(
            list,
            list.get(0)
        );
    }

    @Override
    public SpreadsheetFormatterMenuList createList() {
        return SpreadsheetFormatterMenuList.with(
            Lists.of(
                MENU1,
                MENU2
            )
        );
    }

    @Override
    public Class<SpreadsheetFormatterMenuList> type() {
        return SpreadsheetFormatterMenuList.class;
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
    public SpreadsheetFormatterMenuList unmarshall(final JsonNode json,
                                                   final JsonNodeUnmarshallContext context) {
        return SpreadsheetFormatterMenuList.unmarshall(
            json,
            context
        );
    }

    @Override
    public SpreadsheetFormatterMenuList createJsonNodeMarshallingValue() {
        return this.createList();
    }
}
