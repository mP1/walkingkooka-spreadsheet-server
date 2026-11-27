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
import walkingkooka.reflect.PublicClassTesting;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterSelector;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallerTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;

public class SpreadsheetFormatterMenuListTest implements ListTesting2<SpreadsheetFormatterMenuList, SpreadsheetFormatterMenu>,
    PublicClassTesting<SpreadsheetFormatterMenuList>,
    ImmutableListTesting<SpreadsheetFormatterMenuList, SpreadsheetFormatterMenu>,
    JsonNodeMarshallerTesting<SpreadsheetFormatterMenuList> {

    private final static SpreadsheetFormatterMenu MENU1 = SpreadsheetFormatterMenu.with(
        "Formatter-111",
        SpreadsheetFormatterSelector.parse("formatter-111")
    );

    private final static SpreadsheetFormatterMenu MENU2 = SpreadsheetFormatterMenu.with(
        "Formatter-222",
        SpreadsheetFormatterSelector.parse("formatter-222")
    );

    private final static SpreadsheetFormatterMenu MENU3 = SpreadsheetFormatterMenu.with(
        "Formatter-333",
        SpreadsheetFormatterSelector.parse("formatter-333")
    );

    private final static SpreadsheetFormatterMenu MENU4 = SpreadsheetFormatterMenu.with(
        "Formatter-444",
        SpreadsheetFormatterSelector.parse("formatter-444")
    );

    private final static SpreadsheetFormatterMenu MENU5 = SpreadsheetFormatterMenu.with(
        "Formatter-555",
        SpreadsheetFormatterSelector.parse("formatter-555")
    );
    
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

    // subList..........................................................................................................

    private final static List<SpreadsheetFormatterMenu> ELEMENTS = Lists.of(
        MENU1,
        MENU2,
        MENU3,
        MENU4,
        MENU5
    );

    @Test
    public void testSubList() {
        this.subListAndCheck(
            new SpreadsheetFormatterMenuList(ELEMENTS),
            0,
            2,
            new SpreadsheetFormatterMenuList(
                Lists.of(
                    MENU1,
                    MENU2
                )
            )
        );
    }

    @Test
    public void testSubList2() {
        this.subListAndCheck(
            new SpreadsheetFormatterMenuList(ELEMENTS),
            2,
            4,
            new SpreadsheetFormatterMenuList(
                Lists.of(
                    MENU3,
                    MENU4
                )
            )
        );
    }

    @Test
    public void testSubListSubList() {
        this.subListAndCheck(
            new SpreadsheetFormatterMenuList(ELEMENTS)
                .subList(
                    1,
                    4
                ), // 2, 3, 4
            1,
            3,
            new SpreadsheetFormatterMenuList(
                Lists.of(
                    MENU3,
                    MENU4
                )
            )
        );
    }
    
    @Override
    public SpreadsheetFormatterMenuList createList() {
        return new SpreadsheetFormatterMenuList(
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

    // Json...........................................................................................................

    private final static String JSON = "[\n" +
        "  {\n" +
        "    \"label\": \"Formatter-111\",\n" +
        "    \"selector\": \"formatter-111\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"label\": \"Formatter-222\",\n" +
        "    \"selector\": \"formatter-222\"\n" +
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

    // firstOrEmpty.....................................................................................................

    @Test
    public void testFirstOrEmptyWhenEmpty() {
        this.firstOrEmptyAndCheck(
            SpreadsheetFormatterMenuList.EMPTY
        );
    }

    @Test
    public void testFirstOrEmptyWhenNotEmpty() {
        this.firstOrEmptyAndCheck(
            SpreadsheetFormatterMenuList.EMPTY.setElements(
                Lists.of(
                    MENU1,
                    MENU2
                )
            ),
            MENU1
        );
    }
}
