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
import walkingkooka.net.UrlPath;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Optional;

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

    // pluginDownloadPathExtract........................................................................................

    @Test
    public void testPluginDownloadPathExtractWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> JarEntryInfoName.pluginDownloadPathExtract(null)
        );
    }

    @Test
    public void testPluginDownloadPathExtractWithInvalidPathFails() {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> JarEntryInfoName.pluginDownloadPathExtract(
                UrlPath.parse("/api/plugin/PluginName/not-download/file2.txt")
            )
        );

        this.checkEquals(
            "Invalid plugin download path =/api/plugin/PluginName/not-download/file2.txt",
            thrown.getMessage()
        );
    }

    @Test
    public void testPluginDownloadPathExtractMissingFilename() {
        this.pluginDownloadPathExtractAndCheck(
            UrlPath.parse("/api/plugin/PluginName123/download"),
            Optional.empty()
        );
    }

    @Test
    public void testPluginDownloadPathExtractWithFilename() {
        this.pluginDownloadPathExtractAndCheck(
            UrlPath.parse("/api/plugin/PluginName123/download/file123.txt"),
            Optional.of(
                JarEntryInfoName.with("/file123.txt")
            )
        );
    }

    @Test
    public void testPluginDownloadPathExtractWithFilename2() {
        this.pluginDownloadPathExtractAndCheck(
            UrlPath.parse("/api/plugin/PluginName123/download/META-INF/MANIFEST.MF"),
            Optional.of(
                JarEntryInfoName.MANIFEST_MF
            )
        );
    }

    private void pluginDownloadPathExtractAndCheck(final UrlPath path,
                                                   final Optional<JarEntryInfoName> expected) {
        this.checkEquals(
            expected,
            JarEntryInfoName.pluginDownloadPathExtract(path)
        );
    }

    // Class............................................................................................................

    @Override
    public Class<JarEntryInfoName> type() {
        return JarEntryInfoName.class;
    }
}
