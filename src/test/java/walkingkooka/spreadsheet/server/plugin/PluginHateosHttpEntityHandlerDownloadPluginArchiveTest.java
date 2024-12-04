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
import walkingkooka.Binary;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.ContentDispositionType;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.plugin.JarFileTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PluginHateosHttpEntityHandlerDownloadPluginArchiveTest
        implements HateosHttpEntityHandlerTesting<PluginHateosHttpEntityHandlerDownloadPluginArchive,
        PluginName,
        PluginHateosResourceHandlerContext>,
        ToStringTesting<PluginHateosHttpEntityHandlerDownloadPluginArchive>,
        SpreadsheetMetadataTesting,
        JarFileTesting {

    // hateos...........................................................................................................

    private final static Plugin PLUGIN1 = plugin("TestPlugin111");

    private final static Plugin PLUGIN2 = plugin("TestPlugin222");

    private static Plugin plugin(final String pluginName) {
        return Plugin.with(
                PluginName.with(pluginName),
                pluginName + ".jar",
                jarFile(pluginName),
                USER,
                NOW.now()
        );
    }

    @Test
    public void testHandleOneNotAcceptBinaryFails() {
        final IllegalArgumentException thrown = this.handleOneFails(
                this.id(),
                HttpEntity.EMPTY.addHeader(
                        HttpHeaderName.ACCEPT,
                        MediaType.TEXT_PLAIN.accept()
                ),
                this.parameters(),
                new TestPluginHateosResourceHandlerContext(),
                IllegalArgumentException.class
        );

        this.checkEquals(
                "Accept: Got text/plain require application/octet-stream",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleOneDownload() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleOneAndCheck(
                this.id(),
                this.entity(), // entity
                Maps.empty(), // parameters
                context,
                HttpEntity.EMPTY.setContentType(
                        MediaType.BINARY
                ).addHeader(
                        HttpHeaderName.CONTENT_DISPOSITION,
                        ContentDispositionType.ATTACHMENT.setFilename(
                                ContentDispositionFileName.notEncoded("TestPlugin111.jar")
                        )
                ).setBody(
                        PLUGIN1.archive()
                ).setContentLength()
        );
    }

    @Test
    public void testHandleOneDownloadAbsentPlugin() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleOneAndCheck(
                PluginName.with("Plugin111"),
                this.entity(), // entity
                Maps.empty(), // parameters
                context,
                HttpEntity.EMPTY
        );
    }

    @Override
    public PluginHateosHttpEntityHandlerDownloadPluginArchive createHandler() {
        return PluginHateosHttpEntityHandlerDownloadPluginArchive.INSTANCE;
    }

    @Override
    public PluginName id() {
        return PLUGIN1.name();
    }

    @Override
    public Set<PluginName> manyIds() {
        return Sets.of(
                PLUGIN1.name(),
                PLUGIN2.name()
        );
    }

    @Override
    public Range<PluginName> range() {
        return Range.greaterThanEquals(
                PLUGIN1.name()
        ).and(
                Range.lessThanEquals(
                        PLUGIN2.name()
                )
        );
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public HttpEntity entity() {
        return HttpEntity.EMPTY.addHeader(
                HttpHeaderName.ACCEPT,
                SpreadsheetServerMediaTypes.BINARY.accept()
        );
    }

    private static Binary jarFile(final String pluginName) {
        final String manifest = (
                "Manifest-Version: 1.0\r\n" +
                        "plugin-name: PluginName\r\n" +
                        "plugin-provider-factory-className: example.PluginName\r\n"
        ).replace(
                "PluginName",
                pluginName
        );

        return Binary.with(
                JarFileTesting.jarFile(
                        manifest,
                        Maps.empty()
                )
        );
    }

    @Override
    public PluginHateosResourceHandlerContext context() {
        return new TestPluginHateosResourceHandlerContext();
    }

    final static class TestPluginHateosResourceHandlerContext extends FakePluginHateosResourceHandlerContext {

        TestPluginHateosResourceHandlerContext() {
            this.store = PluginStores.treeMap();
            this.store.save(PLUGIN1);
        }

        @Override
        public LocalDateTime now() {
            return NOW.now();
        }

        @Override
        public PluginStore pluginStore() {
            return this.store;
        }

        final PluginStore store;

        @Override
        public Optional<EmailAddress> user() {
            return Optional.of(USER);
        }
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                "plugin archive download PluginStore"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return Plugin.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "DownloadPluginArchive";
    }

    // Class............................................................................................................

    @Override
    public Class<PluginHateosHttpEntityHandlerDownloadPluginArchive> type() {
        return PluginHateosHttpEntityHandlerDownloadPluginArchive.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
