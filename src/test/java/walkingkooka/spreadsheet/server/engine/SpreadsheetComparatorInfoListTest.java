
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

package walkingkooka.spreadsheet.server.engine;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.ImmutableListTesting;
import walkingkooka.collect.list.Lists;
import walkingkooka.net.Url;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetComparatorInfoListTest implements ImmutableListTesting<SpreadsheetComparatorInfoList, SpreadsheetComparatorInfo>,
        ClassTesting<SpreadsheetComparatorInfoList>,
        JsonNodeMarshallingTesting<SpreadsheetComparatorInfoList> {

    private final static SpreadsheetComparatorInfo INFO1 = SpreadsheetComparatorInfo.with(
            Url.parseAbsolute("https://example.com/comparator-1"),
            SpreadsheetComparatorName.with("comparator-1")
    );

    private final static SpreadsheetComparatorInfo INFO2 = SpreadsheetComparatorInfo.with(
            Url.parseAbsolute("https://example.com/comparator-2"),
            SpreadsheetComparatorName.with("comparator-2")
    );

    @Test
    public void testWithNullFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetComparatorInfoList.with(null)
        );
    }

    @Test
    public void testWithEmptyFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetComparatorInfoList.with(
                        Lists.empty()
                )
        );
    }

    @Test
    public void testDoesntDoubleWrap() {
        final SpreadsheetComparatorInfoList list = this.createList();
        assertSame(
                list,
                SpreadsheetComparatorInfoList.with(list)
        );
    }

    @Test
    public void testGetIndex0() {
        this.getAndCheck(
                this.createList(),
                0, // index
                INFO1
        );
    }

    @Test
    public void testGetIndex1() {
        this.getAndCheck(
                this.createList(),
                1, // index
                INFO2
        );
    }

    @Test
    public void testSetFails() {
        this.setFails(
                this.createList(),
                0, // index
                SpreadsheetComparatorInfo.with(
                        Url.parseAbsolute("https://example.com/set-new-element"),
                        SpreadsheetComparatorName.with("set-new-element-comparator")
                ) // expected
        );
    }

    @Test
    public void testRemoveIndexFails() {
        final SpreadsheetComparatorInfoList list = this.createList();

        this.removeIndexFails(
                list,
                0
        );
    }

    @Test
    public void testRemoveElementFails() {
        final SpreadsheetComparatorInfoList list = this.createList();

        this.removeFails(
                list,
                list.get(0)
        );
    }

    @Override
    public SpreadsheetComparatorInfoList createList() {
        return SpreadsheetComparatorInfoList.with(
                Lists.of(
                        INFO1,
                        INFO2
                )
        );
    }

    @Override
    public Class<SpreadsheetComparatorInfoList> type() {
        return SpreadsheetComparatorInfoList.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // Json...........................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
                this.createJsonNodeMarshallingValue(),
                "[\n" +
                        "  {\n" +
                        "    \"url\": \"https://example.com/comparator-1\",\n" +
                        "    \"name\": \"comparator-1\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"url\": \"https://example.com/comparator-2\",\n" +
                        "    \"name\": \"comparator-2\"\n" +
                        "  }\n" +
                        "]"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
                "[\n" +
                        "  {\n" +
                        "    \"url\": \"https://example.com/comparator-1\",\n" +
                        "    \"name\": \"comparator-1\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"url\": \"https://example.com/comparator-2\",\n" +
                        "    \"name\": \"comparator-2\"\n" +
                        "  }\n" +
                        "]",
                SpreadsheetComparatorInfoList.with(
                        Lists.of(
                                INFO1,
                                INFO2
                        )
                )
        );
    }

    @Override
    public SpreadsheetComparatorInfoList unmarshall(final JsonNode json,
                                                    final JsonNodeUnmarshallContext context) {
        return SpreadsheetComparatorInfoList.unmarshall(
                json,
                context
        );
    }

    @Override
    public SpreadsheetComparatorInfoList createJsonNodeMarshallingValue() {
        return this.createList();
    }

    // ImmutableList....................................................................................................

    @Test
    public void testSwap() {
        this.swapAndCheck(
                SpreadsheetComparatorInfoList.with(
                        Lists.of(
                                INFO1,
                                INFO2
                        )
                ),
                1,
                0,
                SpreadsheetComparatorInfoList.with(
                        Lists.of(
                                INFO2,
                                INFO1
                        )
                )
        );
    }
}
