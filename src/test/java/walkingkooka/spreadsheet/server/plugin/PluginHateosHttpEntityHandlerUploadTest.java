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
import walkingkooka.net.header.MediaType;
import walkingkooka.net.header.MediaTypeBoundary;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class PluginHateosHttpEntityHandlerUploadTest
        implements HateosHttpEntityHandlerTesting<PluginHateosHttpEntityHandlerUpload,
        PluginName,
        PluginHateosResourceHandlerContext>,
        ToStringTesting<PluginHateosHttpEntityHandlerUpload>,
        SpreadsheetMetadataTesting {

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
    public void testHandleAllMissingContentTypeFails() {
        final IllegalArgumentException thrown = this.handleAllFails(
                HttpEntity.EMPTY,
                this.parameters(),
                new TestPluginHateosResourceHandlerContext(),
                IllegalArgumentException.class
        );

        this.checkEquals(
                "Missing Content-Type",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleAllContentTypeNotMultipartFails() {
        final IllegalArgumentException thrown = this.handleAllFails(
                HttpEntity.EMPTY.setContentType(
                        MediaType.TEXT_PLAIN
                ),
                this.parameters(),
                new TestPluginHateosResourceHandlerContext(),
                IllegalArgumentException.class
        );

        this.checkEquals(
                "Content-Type: Expected multipart/form-data or application/json",
                thrown.getMessage()
        );
    }

    @Test
    public void testHandleAllCreate() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleAllAndCheck(
                entity(), // entity
                Maps.empty(), // parameters
                context,
                HttpEntity.EMPTY.setContentType(
                        MediaType.BINARY
                ).setBody(
                        PLUGIN2.archive()
                ).setContentLength()
        );

        this.checkEquals(
                PLUGIN2,
                context.pluginStore()
                        .loadOrFail(PLUGIN2.name()),
                () -> context.pluginStore().toString()
        );
    }

    @Test
    public void testHandleAllUpdate() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        context.pluginStore()
                .save(
                        Plugin.with(
                                PluginName.with("TestPlugin222"),
                                "old.jar",
                                jarFile("TestPlugin222"),
                                USER,
                                NOW.now()
                        )
                );

        this.handleAllAndCheck(
                entity(), // entity
                Maps.empty(), // parameters
                context,
                HttpEntity.EMPTY.setContentType(
                        MediaType.BINARY
                ).setBody(
                        PLUGIN2.archive()
                ).setContentLength()
        );

        this.checkEquals(
                PLUGIN2,
                context.pluginStore()
                        .loadOrFail(PLUGIN2.name()),
                () -> context.pluginStore().toString()
        );
    }

    @Override
    public PluginHateosHttpEntityHandlerUpload createHandler() {
        return PluginHateosHttpEntityHandlerUpload.INSTANCE;
    }

    @Override
    public PluginName id() {
        return PluginName.with("test-plugin-999");
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

    //Content-Type: multipart/form-data;boundary="delimiter12345"
    //
    //--delimiter12345
    //Content-Disposition: form-data; name="field2"; filename="example.txt"
    //
    //value2
    //--delimiter12345--
    @Override
    public HttpEntity entity() {
        final String boundary = "delimiter12345";

        return HttpEntity.EMPTY.setContentType(
                MediaType.MULTIPART_FORM_DATA.setBoundary(MediaTypeBoundary.parse(boundary))
        ).setBodyText(
                "--delimiter12345\r\n" +
                        "Content-Disposition: form-data; name=\"field2\"; filename=\"TestPlugin222.jar\"\r\n" +
                        "\r\n" +
                        binaryToString(PLUGIN2.archive()) +
                        "\r\n" +
                        "--delimiter12345--"
        );
    }

    private static String binaryToString(final Binary binary) {
        return new String(
                binary.value(),
                HttpEntity.DEFAULT_BODY_CHARSET
        );
    }

    private static Binary jarFile(final String pluginName) {
        final String manifest = (
                "Manifest-Version: 1.0\r\n" +
                        "plugin-name: PluginName\r\n" +
                        "plugin-provider-factory-className: example.PluginName\r\n"
        )
                .replace(
                        "PluginName",
                        pluginName
                );

        try (final ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            final Manifest manifestEntry = new Manifest();
            manifestEntry.read(
                    new ByteArrayInputStream(
                            manifest.getBytes(Charset.defaultCharset())
                    )
            );

            final JarOutputStream jarOut = new JarOutputStream(
                    bytes,
                    manifestEntry
            );

            jarOut.flush();
            jarOut.finish();
            jarOut.close();

            return Binary.with(
                    bytes.toByteArray()
            );
        } catch (final IOException io) {
            throw new RuntimeException(io);
        }
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
                "file upload PluginStore"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return Plugin.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Upload";
    }

    // Class............................................................................................................

    @Override
    public Class<PluginHateosHttpEntityHandlerUpload> type() {
        return PluginHateosHttpEntityHandlerUpload.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
