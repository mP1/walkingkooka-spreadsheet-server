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
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.ToStringTesting;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class JarEntryInfoTest implements HashCodeEqualsDefinedTesting2<JarEntryInfo>,
    ToStringTesting<JarEntryInfo>,
    JsonNodeMarshallingTesting<JarEntryInfo>,
    TreePrintableTesting {

    private final static JarEntryInfoName NAME = JarEntryInfoName.MANIFEST_MF;

    private final static OptionalLong SIZE = OptionalLong.of(1111);

    private final static OptionalLong COMPRESSED_SIZE = OptionalLong.of(222);

    private final static OptionalInt METHOD = OptionalInt.of(1);

    private final static OptionalLong CRC = OptionalLong.of(999);

    private final static Optional<LocalDateTime> CREATE = Optional.of(
        LocalDateTime.of(
            1999,
            12,
            31,
            12,
            58,
            59
        )
    );

    private final static Optional<LocalDateTime> LAST_MODIFIED = Optional.of(
        LocalDateTime.of(
            2000,
            1,
            2,
            3,
            45,
            59
        )
    );

    // with.............................................................................................................

    @Test
    public void testWithNullNameFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfo.with(
                null,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNullSizeFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfo.with(
                NAME,
                null,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNegativeSizeFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> JarEntryInfo.with(
                NAME,
                OptionalLong.of(-1),
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNullCompressedSizeFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfo.with(
                NAME,
                SIZE,
                null,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNegativeCompressedSizeFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> JarEntryInfo.with(
                NAME,
                SIZE,
                OptionalLong.of(-1),
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNullMethodFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                null,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNegativeMethodFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                OptionalInt.of(-1),
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNullCreateFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                null,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testWithNullLastModifiedFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                null
            )
        );
    }

    // hashCode/equals..................................................................................................

    @Test
    public void testEqualsDifferentName() {
        this.checkNotEquals(
            JarEntryInfo.with(
                JarEntryInfoName.with("/different"),
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testEqualsDifferentSize() {
        this.checkNotEquals(
            JarEntryInfo.with(
                NAME,
                OptionalLong.of(999),
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testEqualsDifferentCompressedSize() {
        this.checkNotEquals(
            JarEntryInfo.with(
                NAME,
                SIZE,
                OptionalLong.of(999),
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testEqualsDifferentMethod() {
        this.checkNotEquals(
            JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                OptionalInt.of(999),
                CRC,
                CREATE,
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testEqualsDifferentCreate() {
        this.checkNotEquals(
            JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                Optional.empty(),
                LAST_MODIFIED
            )
        );
    }

    @Test
    public void testEqualsDifferentLastModified() {
        this.checkNotEquals(
            JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                Optional.of(LocalDateTime.MAX)
            )
        );
    }

    @Override
    public JarEntryInfo createObject() {
        return JarEntryInfo.with(
            NAME,
            SIZE,
            COMPRESSED_SIZE,
            METHOD,
            CRC,
            CREATE,
            LAST_MODIFIED
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToStringDirectory() {
        this.toStringAndCheck(
            JarEntryInfo.with(
                JarEntryInfoName.with("/dir123/"),
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            ),
            "\"/dir123/\" \"(directory)\" size=1111 compressedSize=222 method=1 crc=999 create=1999-12-31T12:58:59 lastModified=2000-01-02T03:45:59"
        );
    }

    @Test
    public void testToStringFile() {
        this.toStringAndCheck(
            JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            ),
            "\"/META-INF/MANIFEST.MF\" \"(file)\" size=1111 compressedSize=222 method=1 crc=999 create=1999-12-31T12:58:59 lastModified=2000-01-02T03:45:59"
        );
    }

    @Test
    public void testToStringEmptyFile() {
        this.toStringAndCheck(
            JarEntryInfo.with(
                NAME,
                OptionalLong.of(0),
                OptionalLong.of(0),
                METHOD,
                CRC,
                CREATE,
                LAST_MODIFIED
            ),
            "\"/META-INF/MANIFEST.MF\" \"(file)\" size=0 compressedSize=0 method=1 crc=999 create=1999-12-31T12:58:59 lastModified=2000-01-02T03:45:59"
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "{\n" +
                "  \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                "  \"size\": \"1111\",\n" +
                "  \"compressedSize\": \"222\",\n" +
                "  \"method\": 1,\n" +
                "  \"crc\": \"999\",\n" +
                "  \"create\": \"1999-12-31T12:58:59\",\n" +
                "  \"lastModified\": \"2000-01-02T03:45:59\"\n" +
                "}"
        );
    }

    @Test
    public void testMarshallMissingSizeCompressedSizeMethodCrc() {
        this.marshallAndCheck(
            JarEntryInfo.with(
                NAME,
                OptionalLong.empty(), // size
                OptionalLong.empty(), // compressedSize
                OptionalInt.empty(), // method
                OptionalLong.empty(), // crc
                CREATE,
                LAST_MODIFIED
            ),
            "{\n" +
                "  \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                "  \"create\": \"1999-12-31T12:58:59\",\n" +
                "  \"lastModified\": \"2000-01-02T03:45:59\"\n" +
                "}"
        );
    }

    @Test
    public void testMarshallMissingCreateAndLastModified() {
        this.marshallAndCheck(
            JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                Optional.empty(),
                Optional.empty()
            ),
            "{\n" +
                "  \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                "  \"size\": \"1111\",\n" +
                "  \"compressedSize\": \"222\",\n" +
                "  \"method\": 1,\n" +
                "  \"crc\": \"999\"\n" +
                "}"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "{\n" +
                "  \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                "  \"size\": \"1111\",\n" +
                "  \"compressedSize\": \"222\",\n" +
                "  \"method\": 1,\n" +
                "  \"crc\": \"999\",\n" +
                "  \"create\": \"1999-12-31T12:58:59\",\n" +
                "  \"lastModified\": \"2000-01-02T03:45:59\"\n" +
                "}",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Override
    public JarEntryInfo unmarshall(final JsonNode json,
                                   final JsonNodeUnmarshallContext context) {
        return JarEntryInfo.unmarshall(
            json,
            context
        );
    }

    @Override
    public JarEntryInfo createJsonNodeMarshallingValue() {
        return this.createObject();
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.createObject(),
            "/META-INF/MANIFEST.MF (file)\n" +
                "  size: 1111\n" +
                "  compressedSize: 222\n" +
                "  method: 1\n" +
                "  crc: 999\n" +
                "  create: 1999-12-31T12:58:59\n" +
                "  lastModified: 2000-01-02T03:45:59\n"
        );
    }

    @Test
    public void testTreePrintMissingSizeCompressedSizeMethodCrc() {
        this.treePrintAndCheck(
            JarEntryInfo.with(
                NAME,
                OptionalLong.empty(), // size
                OptionalLong.empty(), // compressedSize
                OptionalInt.empty(), // method
                OptionalLong.empty(), // crc
                CREATE,
                LAST_MODIFIED
            ),
            "/META-INF/MANIFEST.MF (file)\n" +
                "  create: 1999-12-31T12:58:59\n" +
                "  lastModified: 2000-01-02T03:45:59\n"
        );
    }

    @Test
    public void testTreePrintMissingCreateLastModified() {
        this.treePrintAndCheck(
            JarEntryInfo.with(
                NAME,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CRC,
                Optional.empty(),
                Optional.empty()
            ),
            "/META-INF/MANIFEST.MF (file)\n" +
                "  size: 1111\n" +
                "  compressedSize: 222\n" +
                "  method: 1\n" +
                "  crc: 999\n"
        );
    }

    // class............................................................................................................

    @Override
    public Class<JarEntryInfo> type() {
        return JarEntryInfo.class;
    }
}
