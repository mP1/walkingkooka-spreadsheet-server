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
import walkingkooka.net.UrlPath;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.ContentDispositionType;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.header.MediaTypeBoundary;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandlerTesting;
import walkingkooka.plugin.JarFileTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PluginHateosHttpEntityHandlerUploadTest
    implements HateosHttpEntityHandlerTesting<PluginHateosHttpEntityHandlerUpload,
    PluginName,
    PluginHateosResourceHandlerContext>,
    ToStringTesting<PluginHateosHttpEntityHandlerUpload>,
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
    public void testHandleAllIncompatibleAcceptFails() {
        final IllegalArgumentException thrown = this.handleAllFails(
            HttpEntity.EMPTY.setAccept(
                MediaType.TEXT_PLAIN.accept()
            ),
            this.parameters(),
            this.path(),
            new TestPluginHateosResourceHandlerContext(),
            IllegalArgumentException.class
        );

        this.checkEquals(
            "Accept: Got application/octet-stream require text/plain",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleAllMissingContentTypeFails() {
        final IllegalArgumentException thrown = this.handleAllFails(
            HttpEntity.EMPTY,
            this.parameters(),
            this.path(),
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
            this.path(),
            new TestPluginHateosResourceHandlerContext(),
            IllegalArgumentException.class
        );

        this.checkEquals(
            "Content-Type: Got text/plain, expected multipart/form-data or application/json",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleAllMultipartMissingFileFails() {
        final String boundary = "delimiter12345";

        final IllegalArgumentException thrown = this.handleAllFails(
            HttpEntity.EMPTY.setContentType(
                MediaType.MULTIPART_FORM_DATA.setBoundary(MediaTypeBoundary.parse(boundary))
            ).setBodyText(
                "--delimiter12345\r\n" +
                    "Content-Disposition: form-data; name=\"not-a-file\";\r\n" +
                    "\r\n" +
                    binaryToString(PLUGIN2.archive()) +
                    "\r\n" +
                    "--delimiter12345--"
            ),
            this.parameters(),
            this.path(),
            new TestPluginHateosResourceHandlerContext(),
            IllegalArgumentException.class
        );

        this.checkEquals(
            "Missing filename",
            thrown.getMessage()
        );
    }

    @Test
    public void testHandleMultipartAllCreate() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleAllAndCheck(
            this.multipart(), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
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
    public void testHandleMultipartAllUpdate() {
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
            this.multipart(), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
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
    public void testHandleBase64FileAllCreate() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleAllAndCheck(
            this.binaryAsBase64(), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
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
    public void testHandleBase64FileAllCreateAndAcceptBinary() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleAllAndCheck(
            this.binaryAsBase64()
                .setAccept(SpreadsheetServerMediaTypes.BINARY.accept()), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
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
    public void testHandleBase64FileAllUpdate() {
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
            this.binaryAsBase64(), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
            ).setContentLength()
        );

        this.checkEquals(
            PLUGIN2,
            context.pluginStore()
                .loadOrFail(PLUGIN2.name()),
            () -> context.pluginStore().toString()
        );
    }

    private HttpEntity binaryAsBase64() {
        return HttpEntity.EMPTY.setContentType(
            SpreadsheetServerMediaTypes.BASE64
        ).addHeader(
            HttpHeaderName.CONTENT_DISPOSITION,
            ContentDispositionType.ATTACHMENT.setFilename(
                ContentDispositionFileName.notEncoded("TestPlugin222.jar")
            )
        ).setBodyText(
            Base64.getEncoder()
                .encodeToString(
                    PLUGIN2.archive()
                        .value()
                )
        );
    }

    @Test
    public void testHandleBinaryFileAllCreate() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleAllAndCheck(
            this.binary(), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
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
    public void testHandleBinaryFileAllCreateAndAcceptBinary() {
        final TestPluginHateosResourceHandlerContext context = new TestPluginHateosResourceHandlerContext();

        this.handleAllAndCheck(
            this.binary()
                .setAccept(SpreadsheetServerMediaTypes.BINARY.accept()), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
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
    public void testHandleBinaryFileAllUpdate() {
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
            this.binary(), // entity
            HateosHttpEntityHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            context,
            HttpEntity.EMPTY.setContentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE
            ).setBodyText(
                toJson(PLUGIN2)
            ).setContentLength()
        );

        this.checkEquals(
            PLUGIN2,
            context.pluginStore()
                .loadOrFail(PLUGIN2.name()),
            () -> context.pluginStore().toString()
        );
    }

    private HttpEntity binary() {
        return HttpEntity.EMPTY.setContentType(
            SpreadsheetServerMediaTypes.BINARY
        ).addHeader(
            HttpHeaderName.CONTENT_DISPOSITION,
            ContentDispositionType.ATTACHMENT.setFilename(
                ContentDispositionFileName.notEncoded("TestPlugin222.jar")
            )
        ).setBody(
            PLUGIN2.archive()
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
        return this.multipart();
    }

    private HttpEntity multipart() {
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

        return Binary.with(
            JarFileTesting.jarFile(
                manifest,
                Maps.empty()
            )
        );
    }

    private static String toJson(final Plugin plugin) {
        return JSON_NODE_MARSHALL_CONTEXT.marshall(plugin)
            .toString();
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
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

        @Override
        public Indentation indentation() {
            return INDENTATION;
        }

        @Override
        public LineEnding lineEnding() {
            return LineEnding.NL;
        }

        @Override
        public MediaType contentType() {
            return SpreadsheetServerMediaTypes.CONTENT_TYPE;
        }

        @Override
        public JsonNode marshall(final Object value) {
            return JSON_NODE_MARSHALL_CONTEXT.marshall(value);
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
