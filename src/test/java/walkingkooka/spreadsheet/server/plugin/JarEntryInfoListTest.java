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

package walkingkooka.spreadsheet.server.plugin;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.ImmutableListTesting;
import walkingkooka.collect.list.Lists;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.HasTextTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;

public final class JarEntryInfoListTest implements ImmutableListTesting<JarEntryInfoList, JarEntryInfo>,
        ClassTesting<JarEntryInfoList>,
        HasTextTesting,
        JsonNodeMarshallingTesting<JarEntryInfoList> {

    private final static LocalDateTime CREATE = LocalDateTime.of(
            1999,
            12,
            31,
            12,
            58,
            59
    );

    private final static LocalDateTime LAST_MODIFIED = LocalDateTime.of(
            2000,
            1,
            2,
            3,
            45,
            59
    );

    private final static JarEntryInfo INFO1 = jarEntryInfo(
            "/file111",
            false, // directory
            111
    );

    private final static JarEntryInfo INFO2 = jarEntryInfo(
            "/file222",
            false, // directory
            222
    );

    private final static JarEntryInfo INFO3 = jarEntryInfo(
            "/file333",
            false, // directory
            333
    );

    private final static JarEntryInfo INFO4 = jarEntryInfo(
            "/file444",
            false, // directory
            444
    );

    private static JarEntryInfo jarEntryInfo(final String name,
                                             final boolean directory,
                                             final long size) {
        return JarEntryInfo.with(
                name,
                directory,
                size,
                size,
                1, // method
                CREATE,
                LAST_MODIFIED
        );
    }

    @Test
    public void testDoesntDoubleWrap() {
        final JarEntryInfoList list = this.createList();
        assertSame(
                list,
                JarEntryInfoList.with(list)
        );
    }

    @Test
    public void testGet() {
        this.getAndCheck(
                this.createList(),
                0, // index
                INFO1// expected
        );
    }

    @Test
    public void testSetFails() {
        this.setFails(
                this.createList(),
                0, // index
                INFO4 // expected
        );
    }

    @Test
    public void testRemoveIndexFails() {
        final JarEntryInfoList list = this.createList();

        this.removeIndexFails(
                list,
                0
        );
    }

    @Test
    public void testRemoveElementFails() {
        final JarEntryInfoList list = this.createList();

        this.removeFails(
                list,
                list.get(0)
        );
    }

    // ImmutableList....................................................................................................

    @Test
    public void testSwap() {
        this.swapAndCheck(
                JarEntryInfoList.with(
                        Lists.of(
                                INFO1,
                                INFO2,
                                INFO3
                        )
                ),
                0,
                2,
                JarEntryInfoList.with(
                        Lists.of(
                                INFO3,
                                INFO2,
                                INFO1
                        )
                )
        );
    }

    // ImmutableListTesting.............................................................................................

    @Override
    public JarEntryInfoList createList() {
        return JarEntryInfoList.with(
                Lists.of(
                        INFO1,
                        INFO2,
                        INFO3
                )
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
                this.createList(),
                "[\n" +
                        "  {\n" +
                        "    \"name\": \"/file111\",\n" +
                        "    \"directory\": false,\n" +
                        "    \"size\": \"111\",\n" +
                        "    \"compressedSize\": \"111\",\n" +
                        "    \"method\": 1,\n" +
                        "    \"create\": \"1999-12-31T12:58:59\",\n" +
                        "    \"lastModified\": \"2000-01-02T03:45:59\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"name\": \"/file222\",\n" +
                        "    \"directory\": false,\n" +
                        "    \"size\": \"222\",\n" +
                        "    \"compressedSize\": \"222\",\n" +
                        "    \"method\": 1,\n" +
                        "    \"create\": \"1999-12-31T12:58:59\",\n" +
                        "    \"lastModified\": \"2000-01-02T03:45:59\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"name\": \"/file333\",\n" +
                        "    \"directory\": false,\n" +
                        "    \"size\": \"333\",\n" +
                        "    \"compressedSize\": \"333\",\n" +
                        "    \"method\": 1,\n" +
                        "    \"create\": \"1999-12-31T12:58:59\",\n" +
                        "    \"lastModified\": \"2000-01-02T03:45:59\"\n" +
                        "  }\n" +
                        "]"
        );
    }

    @Override
    public JarEntryInfoList unmarshall(final JsonNode jsonNode,
                                       final JsonNodeUnmarshallContext context) {
        return JarEntryInfoList.unmarshall(
                jsonNode,
                context
        );
    }

    @Override
    public JarEntryInfoList createJsonNodeMarshallingValue() {
        return this.createList();
    }

    // class............................................................................................................

    @Override
    public Class<JarEntryInfoList> type() {
        return JarEntryInfoList.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
