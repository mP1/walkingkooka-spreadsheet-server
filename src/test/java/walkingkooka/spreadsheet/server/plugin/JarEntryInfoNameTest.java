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
import walkingkooka.InvalidCharacterException;
import walkingkooka.naming.NameTesting;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final public class JarEntryInfoNameTest implements NameTesting<JarEntryInfoName, JarEntryInfoName>,
        JsonNodeMarshallingTesting<JarEntryInfoName> {

    @Test
    public void testWithMissingLeadingSlashFails() {
        assertThrows(
                InvalidCharacterException.class,
                () -> JarEntryInfoName.with(
                        "Missing-leading-slash"
                )
        );
    }

    @Test
    public void testWithManifest() {
        assertSame(
                JarEntryInfoName.MANIFEST_MF,
                JarEntryInfoName.with(
                        JarEntryInfoName.MANIFEST_MF_STRING
                )
        );
    }

    @Test
    public void testWithManifestDifferentCase() {
        assertSame(
                JarEntryInfoName.MANIFEST_MF,
                JarEntryInfoName.with(
                        "/meta-inf/Manifest.MF"
                )
        );
    }

    // Name.............................................................................................................

    @Override
    public JarEntryInfoName createName(final String name) {
        return JarEntryInfoName.with(name);
    }

    @Override
    public CaseSensitivity caseSensitivity() {
        return CaseSensitivity.SENSITIVE;
    }

    @Override
    public String nameText() {
        return "/file.txt";
    }

    @Override
    public String differentNameText() {
        return "/different/file.txt";
    }

    @Override
    public String nameTextLess() {
        return "/1.txt";
    }

    // Comparable.......................................................................................................

    @Test
    public void testCompareToManifestWithManifest() {
        this.compareToAndCheckEquals(
                JarEntryInfoName.MANIFEST_MF,
                JarEntryInfoName.MANIFEST_MF
        );
    }

    @Test
    public void testCompareToManifestWithOther() {
        this.compareToAndCheckLess(
                JarEntryInfoName.MANIFEST_MF,
                JarEntryInfoName.with("/z")
        );
    }

    @Test
    public void testCompareToOtherWithManifest() {
        this.compareToAndCheckMore(
                JarEntryInfoName.with("/a"),
                JarEntryInfoName.MANIFEST_MF
        );
    }

    // json.............................................................................................................

    @Override
    public JarEntryInfoName unmarshall(final JsonNode json,
                                       final JsonNodeUnmarshallContext context) {
        return JarEntryInfoName.unmarshall(
                json,
                context
        );
    }

    @Override
    public JarEntryInfoName createJsonNodeMarshallingValue() {
        return JarEntryInfoName.with("/dir1/file2.txt");
    }

    // Class............................................................................................................

    @Override
    public Class<JarEntryInfoName> type() {
        return JarEntryInfoName.class;
    }
}
