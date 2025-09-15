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
import walkingkooka.collect.map.Maps;
import walkingkooka.plugin.JarFileTesting;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.HasTextTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class JarEntryInfoListTest implements ImmutableListTesting<JarEntryInfoList, JarEntryInfo>,
    ClassTesting<JarEntryInfoList>,
    HasTextTesting,
    JsonNodeMarshallingTesting<JarEntryInfoList>,
    JarFileTesting {

    private final static JarEntryInfo INFO1 = jarEntryInfo(
        "/file111",
        111
    );

    private final static JarEntryInfo INFO2 = jarEntryInfo(
        "/file222",
        222
    );

    private final static JarEntryInfo INFO3 = jarEntryInfo(
        "/file333",
        333
    );

    private final static JarEntryInfo INFO4 = jarEntryInfo(
        "/file444",
        444
    );

    private static JarEntryInfo jarEntryInfo(final String name,
                                             final long size) {
        return JarEntryInfo.with(
            JarEntryInfoName.with(name),
            OptionalLong.of(size),
            OptionalLong.of(size),
            OptionalInt.of(1), // method
            OptionalLong.of(999), // crc
            Optional.of(CREATE),
            Optional.of(LAST_MODIFIED)
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
            new JarEntryInfoList(
                Lists.of(
                    INFO1,
                    INFO2,
                    INFO3
                )
            ),
            0,
            2,
            new JarEntryInfoList(
                Lists.of(
                    INFO3,
                    INFO2,
                    INFO1
                )
            )
        );
    }

    // setElements......................................................................................................

    @Test
    public void testSetElementsIncludesNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createList()
                .setElements(
                    Arrays.asList(
                        INFO1,
                        null
                    )
                )
        );
    }

    @Test
    public void testSetElementsDoesntDoubleWrap() {
        final JarEntryInfoList list = this.createList();
        assertSame(
            list,
            list.setElements(list)
        );
    }

    @Test
    public void testSetElementsWithEmpty() {
        assertSame(
            JarEntryInfoList.EMPTY,
            new JarEntryInfoList(
                Lists.of(
                    INFO1,
                    INFO2,
                    INFO3
                )
            ).setElements(Lists.empty())
        );
    }

    // ImmutableListTesting.............................................................................................

    @Override
    public JarEntryInfoList createList() {
        return new JarEntryInfoList(
            Lists.of(
                INFO1,
                INFO2,
                INFO3
            )
        );
    }

    // readJarFile......................................................................................................

    @Test
    public void testReadJarFileWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfoList.readJarFile(null)
        );
    }

    @Test
    public void testReadJarFile() {
        final byte[] jar = JarFileTesting.jarFile(
            "Manifest-Version: 1.0\r\n" +
                "Key111: Value111\r\n" +
                "\r\n",
            Maps.of(
                "file111", "File111".getBytes(StandardCharsets.UTF_8),
                "file222", "File222".getBytes(StandardCharsets.UTF_8),
                "file333", "File333".getBytes(StandardCharsets.UTF_8)
            )
        );

        this.checkEquals(
            new JarEntryInfoList(
                Lists.of(
                    JarEntryInfo.with(
                        JarEntryInfoName.MANIFEST_MF,
                        OptionalLong.empty(), // size
                        OptionalLong.empty(), // compressedSize
                        OptionalInt.of(8), // method
                        OptionalLong.empty(), // crc
                        Optional.of(CREATE),
                        Optional.of(LAST_MODIFIED)
                    ),
                    JarEntryInfo.with(
                        JarEntryInfoName.with("/file111"),
                        OptionalLong.empty(), // size
                        OptionalLong.empty(), // compressedSize
                        OptionalInt.of(8), // method
                        OptionalLong.empty(), // crc
                        Optional.of(CREATE),
                        Optional.of(LAST_MODIFIED)
                    ),
                    JarEntryInfo.with(
                        JarEntryInfoName.with("/file222"),
                        OptionalLong.empty(), // size
                        OptionalLong.empty(), // compressedSize
                        OptionalInt.of(8), // method
                        OptionalLong.empty(), // crc
                        Optional.of(CREATE),
                        Optional.of(LAST_MODIFIED)
                    ),
                    JarEntryInfo.with(
                        JarEntryInfoName.with("/file333"),
                        OptionalLong.empty(), // size
                        OptionalLong.empty(), // compressedSize
                        OptionalInt.of(8), // method
                        OptionalLong.empty(), // crc
                        Optional.of(CREATE),
                        Optional.of(LAST_MODIFIED)
                    )
                )
            ),
            JarEntryInfoList.readJarFile(
                new ByteArrayInputStream(jar)
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
                "    \"size\": \"111\",\n" +
                "    \"compressedSize\": \"111\",\n" +
                "    \"method\": 1,\n" +
                "    \"crc\": \"999\",\n" +
                "    \"create\": \"1999-12-31T12:58\",\n" +
                "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"/file222\",\n" +
                "    \"size\": \"222\",\n" +
                "    \"compressedSize\": \"222\",\n" +
                "    \"method\": 1,\n" +
                "    \"crc\": \"999\",\n" +
                "    \"create\": \"1999-12-31T12:58\",\n" +
                "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name\": \"/file333\",\n" +
                "    \"size\": \"333\",\n" +
                "    \"compressedSize\": \"333\",\n" +
                "    \"method\": 1,\n" +
                "    \"crc\": \"999\",\n" +
                "    \"create\": \"1999-12-31T12:58\",\n" +
                "    \"lastModified\": \"2000-01-02T04:58\"\n" +
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
        return JavaVisibility.PUBLIC;
    }
}
