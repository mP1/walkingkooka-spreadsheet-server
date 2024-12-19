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
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.ContentDispositionType;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.server.HttpHandlerTesting;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.plugin.JarFileTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.store.FakePluginStore;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class PluginFileDownloadHttpHandlerTest implements HttpHandlerTesting<PluginFileDownloadHttpHandler>,
        JarFileTesting,
        SpreadsheetMetadataTesting {

    private final static AbsoluteUrl BASE = Url.parseAbsolute("https://example.com/api/plugin");

    private final static PluginName PLUGIN_NAME = PluginName.with("TestPlugin123");

    private final static String CONTENT = "Hello";

    private final static Binary JAR_FILE = Binary.with(
            JarFileTesting.jarFile(
                    "ManifestVersion: 1.0\r\n",
                    Maps.of(
                            "dir111/sub111/Example111.java", CONTENT.getBytes(StandardCharsets.UTF_8),
                            "file222", "File222.bin".getBytes(StandardCharsets.UTF_8),
                            "file333", "File333.gif".getBytes(StandardCharsets.UTF_8)
                    )
            )
    );

    private final static PluginStore PLUGIN_STORE = new FakePluginStore() {
        @Override
        public Optional<Plugin> load(final PluginName id) {
            return Optional.ofNullable(
                    PLUGIN_NAME.equals(id) ?
                            Plugin.with(
                                    id,
                                    "TestPlugin123.jar", // filename
                                    JAR_FILE,
                                    USER,
                                    NOW.now()
                            ) :
                            null
            );
        }
    };

    private final static MediaType FILE_CONTENT_TYPE = MediaType.parse("text/custom123");

    private static final HttpEntity HTTP_ENTITY_WITH_FILE = HttpEntity.EMPTY.setContentType(FILE_CONTENT_TYPE)
            .addHeader(
                    HttpHeaderName.CONTENT_DISPOSITION,
                    ContentDispositionType.ATTACHMENT.setFilename(
                            ContentDispositionFileName.notEncoded("/dir111/sub111/Example111.java")
                    )
            ).setBodyText(CONTENT)
            .setContentLength();

    private final static MediaType JAR_CONTENT_TYPE = MediaType.parse("binary/jar");

    private static final HttpEntity HTTP_ENTITY_WITH_JAR = HttpEntity.EMPTY.setContentType(JAR_CONTENT_TYPE)
            .addHeader(
                    HttpHeaderName.CONTENT_DISPOSITION,
                    ContentDispositionType.ATTACHMENT.setFilename(
                            ContentDispositionFileName.notEncoded("TestPlugin123.jar")
                    )
            ).setBody(JAR_FILE)
            .setContentLength();

    private final BiFunction<String, Binary, MediaType> CONTENT_TYPE_DETECTOR = (filename, binary) ->
            filename.endsWith(".java") ?
                    FILE_CONTENT_TYPE :
                    JAR_CONTENT_TYPE;

    @Test
    public void testWithNullBaseFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginFileDownloadHttpHandler.with(
                        null,
                        PLUGIN_STORE,
                        CONTENT_TYPE_DETECTOR
                )
        );
    }

    @Test
    public void testWithNullPluginStoreFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginFileDownloadHttpHandler.with(
                        BASE,
                        null,
                        CONTENT_TYPE_DETECTOR
                )
        );
    }

    @Test
    public void testWithNullContentTypeDetectorFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginFileDownloadHttpHandler.with(
                        BASE,
                        PLUGIN_STORE,
                        null
                )
        );
    }

    @Test
    public void testHandlePluginNameNotFound() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.NO_CONTENT.status());

        this.handleAndCheck(
                "/api/plugin/UnknownPluginNotFound/download/dir111/sub111/file111.txt",
                Accept.DEFAULT,
                response
        );
    }

    @Test
    public void testHandleFileNotFound() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.NO_CONTENT.status());

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download/unknown-file",
                Accept.DEFAULT,
                response
        );
    }

    @Test
    public void testHandleWithoutFilePathAndAcceptMissing() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(HTTP_ENTITY_WITH_JAR);

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download",
                null, // accept missing
                response
        );
    }

    @Test
    public void testHandleWithoutFilePathAndAcceptStarStar() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(HTTP_ENTITY_WITH_JAR);

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download",
                Accept.DEFAULT,
                response
        );
    }

    @Test
    public void testHandleWithoutFilePathAndAcceptExact() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(HTTP_ENTITY_WITH_JAR);

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download",
                Accept.with(
                        Lists.of(JAR_CONTENT_TYPE)
                ),
                response
        );
    }

    @Test
    public void testHandleWithoutFilePathAndAcceptTestFalse() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.NO_CONTENT.status());
        response.setEntity(HttpEntity.EMPTY);

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download",
                Accept.parse("image/jpg"),
                response
        );
    }

    @Test
    public void testHandleOnlySlash() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.NO_CONTENT.status());

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download/",
                Accept.DEFAULT,
                response
        );
    }

    @Test
    public void testHandleExistingFileAcceptMissing() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(HTTP_ENTITY_WITH_FILE);

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download/dir111/sub111/Example111.java",
                null, // accept missing
                response
        );
    }

    @Test
    public void testHandleExistingFileAcceptStarStar() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.OK.status());
        response.setEntity(HTTP_ENTITY_WITH_FILE);

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download/dir111/sub111/Example111.java",
                Accept.with(
                        Lists.of(FILE_CONTENT_TYPE)
                ),
                response
        );
    }

    @Test
    public void testHandleExistingFileAcceptTestFalse() {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(HttpStatusCode.NO_CONTENT.status());
        response.setEntity(HttpEntity.EMPTY);

        this.handleAndCheck(
                "/api/plugin/TestPlugin123/download/dir111/sub111/Example111.java",
                Accept.parse("image/jpg"),
                response
        );
    }

    private void handleAndCheck(final String url,
                                final Accept accept,
                                final HttpResponse expected) {
        this.handleAndCheck(
                HttpRequests.get(
                        HttpTransport.UNSECURED,
                        Url.parseRelative(url),
                        HttpProtocolVersion.VERSION_1_0,
                        null == accept ?
                                HttpEntity.EMPTY :
                                HttpEntity.EMPTY.addHeader(
                                        HttpHeaderName.ACCEPT,
                                        accept
                                )
                ),
                expected
        );
    }

    @Override
    public PluginFileDownloadHttpHandler createHttpHandler() {
        return PluginFileDownloadHttpHandler.with(
                BASE,
                PLUGIN_STORE,
                CONTENT_TYPE_DETECTOR
        );
    }

    // class............................................................................................................

    @Override
    public Class<PluginFileDownloadHttpHandler> type() {
        return PluginFileDownloadHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
