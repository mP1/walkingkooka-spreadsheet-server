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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class JarEntryInfoTest implements HashCodeEqualsDefinedTesting2<JarEntryInfo>,
        ToStringTesting<JarEntryInfo>,
        JsonNodeMarshallingTesting<JarEntryInfo> {

    private final static String NAME = "/META-INF/MANIFEST.MF";

    private final static boolean DIRECTORY = false;

    private final static long SIZE = 1111;

    private final static long COMPRESSED_SIZE = 222;

    private final static int METHOD = 1;

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

    // with.............................................................................................................

    @Test
    public void testWithNullNameFails() {
        assertThrows(
                NullPointerException.class,
                () -> JarEntryInfo.with(
                        null,
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
                        CREATE,
                        LAST_MODIFIED
                )
        );
    }

    @Test
    public void testWithEmptyNameFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> JarEntryInfo.with(
                        "",
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
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
                        DIRECTORY,
                        -1,
                        COMPRESSED_SIZE,
                        METHOD,
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
                        DIRECTORY,
                        SIZE,
                        -1,
                        METHOD,
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
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        -1,
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
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
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
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
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
                        "different",
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
                        CREATE,
                        LAST_MODIFIED
                )
        );
    }

    @Test
    public void testEqualsDifferentDirectory() {
        this.checkNotEquals(
                JarEntryInfo.with(
                        NAME,
                        false == DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
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
                        DIRECTORY,
                        10000 + SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
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
                        DIRECTORY,
                        SIZE,
                        10000 + COMPRESSED_SIZE,
                        METHOD,
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
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD + 1,
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
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
                        LocalDateTime.MAX,
                        LAST_MODIFIED
                )
        );
    }

    @Test
    public void testEqualsDifferentLastModified() {
        this.checkNotEquals(
                JarEntryInfo.with(
                        NAME,
                        DIRECTORY,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
                        CREATE,
                        LocalDateTime.MAX
                )
        );
    }

    @Override
    public JarEntryInfo createObject() {
        return JarEntryInfo.with(
                NAME,
                DIRECTORY,
                SIZE,
                COMPRESSED_SIZE,
                METHOD,
                CREATE,
                LAST_MODIFIED
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToStringDirectory() {
        this.toStringAndCheck(
                JarEntryInfo.with(
                        NAME,
                        true,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
                        CREATE,
                        LAST_MODIFIED
                ),
                "\"/META-INF/MANIFEST.MF\" \"(directory)\" size=1111 compressedSize=222 method=1 create=1999-12-31T12:58:59 lastModified=2000-01-02T03:45:59"
        );
    }

    @Test
    public void testToStringFile() {
        this.toStringAndCheck(
                JarEntryInfo.with(
                        NAME,
                        false,
                        SIZE,
                        COMPRESSED_SIZE,
                        METHOD,
                        CREATE,
                        LAST_MODIFIED
                ),
                "\"/META-INF/MANIFEST.MF\" \"(file)\" size=1111 compressedSize=222 method=1 create=1999-12-31T12:58:59 lastModified=2000-01-02T03:45:59"
        );
    }

    @Test
    public void testToStringEmptyFile() {
        this.toStringAndCheck(
                JarEntryInfo.with(
                        NAME,
                        false,
                        0,
                        0,
                        METHOD,
                        CREATE,
                        LAST_MODIFIED
                ),
                "\"/META-INF/MANIFEST.MF\" \"(file)\" size=0 compressedSize=0 method=1 create=1999-12-31T12:58:59 lastModified=2000-01-02T03:45:59"
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
                this.createJsonNodeMarshallingValue(),
                "{\n" +
                        "  \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                        "  \"directory\": false,\n" +
                        "  \"size\": \"1111\",\n" +
                        "  \"compressedSize\": \"222\",\n" +
                        "  \"method\": 1,\n" +
                        "  \"create\": \"1999-12-31T12:58:59\",\n" +
                        "  \"lastModified\": \"2000-01-02T03:45:59\"\n" +
                        "}"
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
                "{\n" +
                        "  \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                        "  \"directory\": false,\n" +
                        "  \"size\": \"1111\",\n" +
                        "  \"compressedSize\": \"222\",\n" +
                        "  \"method\": 1,\n" +
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

    // class............................................................................................................

    @Override
    public Class<JarEntryInfo> type() {
        return JarEntryInfo.class;
    }
}
