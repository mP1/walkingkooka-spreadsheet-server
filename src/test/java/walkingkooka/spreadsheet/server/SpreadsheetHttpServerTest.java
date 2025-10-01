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

package walkingkooka.spreadsheet.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import walkingkooka.Binary;
import walkingkooka.Cast;
import walkingkooka.Either;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.convert.provider.ConverterInfo;
import walkingkooka.convert.provider.ConverterInfoSet;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlQueryString;
import walkingkooka.net.header.Accept;
import walkingkooka.net.header.AcceptCharset;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.ContentDisposition;
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.ContentDispositionType;
import walkingkooka.net.header.ETag;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.header.MediaTypeBoundary;
import walkingkooka.net.header.MediaTypeDetector;
import walkingkooka.net.header.MediaTypeParameterName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.HttpProtocolVersion;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.HttpTransport;
import walkingkooka.net.http.json.JsonHttpHandlers;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.net.http.server.HttpRequests;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpResponses;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.JarFileTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.convert.provider.MissingConverterSet;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterInfo;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterInfoSet;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContext;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterInfo;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterInfoSet;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResource;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResourceSet;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResource;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResourceSet;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterMenuList;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterSelectorEdit;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResource;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceSet;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserSelectorEdit;
import walkingkooka.spreadsheet.server.plugin.JarEntryInfoList;
import walkingkooka.spreadsheet.server.plugin.JarEntryInfoName;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelReferencesStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.validation.form.SpreadsheetForms;
import walkingkooka.spreadsheet.validation.form.store.SpreadsheetFormStores;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportWindows;
import walkingkooka.storage.Storages;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.FakeExpressionFunction;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.expression.function.provider.FakeExpressionFunctionProvider;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.validation.form.Form;
import walkingkooka.validation.form.FormName;
import walkingkooka.validation.form.provider.FormHandlerInfo;
import walkingkooka.validation.form.provider.FormHandlerInfoSet;
import walkingkooka.validation.provider.ValidatorInfo;
import walkingkooka.validation.provider.ValidatorInfoSet;

import java.io.InputStream;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetHttpServerTest extends SpreadsheetHttpServerTestCase<SpreadsheetHttpServer>
    implements SpreadsheetMetadataTesting,
    JarFileTesting,
    TreePrintableTesting {

    private final static CharsetName CHARSET = CharsetName.UTF_8;

    private final static MediaType CONTENT_TYPE = MediaType.APPLICATION_JSON;

    private final static MediaType CONTENT_TYPE_UTF8 = CONTENT_TYPE.setCharset(CHARSET);

    private final static UrlPath FILE = UrlPath.parse("/file.txt");
    private final static MediaType FILE_CONTENT_TYPE = MediaType.parse("text/custom-file;charset=" + CHARSET.value());
    private final static LocalDateTime FILE_LAST_MODIFIED = LocalDateTime.of(2000, 12, 31, 12, 28, 29);
    private final static Binary FILE_BINARY = Binary.with(bytes("abc123", FILE_CONTENT_TYPE));
    private final static HttpStatus FILE_NOT_FOUND = HttpStatusCode.NOT_FOUND.setMessage("File not found custom message");

    private final static String DELTA = SpreadsheetDelta.class.getSimpleName();

    private final static Optional<String> NO_TRANSACTION_ID = Optional.empty();
    private final static Map<HttpHeaderName<?>, List<?>> NO_HEADERS_TRANSACTION_ID = HttpRequest.NO_HEADERS;

    private static final SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(1L);

    private static final ExpressionFunctionProvider<SpreadsheetExpressionEvaluationContext> EXPRESSION_FUNCTION_PROVIDER = new FakeExpressionFunctionProvider<>() {

        @Override
        public ExpressionFunction<?, SpreadsheetExpressionEvaluationContext> expressionFunction(final ExpressionFunctionName name,
                                                                                                final List<?> values,
                                                                                                final ProviderContext context) {
            switch (name.value().toLowerCase()) {
                case "expressionfunction1":
                    return Cast.to(
                        new TestFunction(
                            SpreadsheetExpressionFunctions.name("ExpressionFunction1")
                        )
                    );
                default:
                    throw new UnknownExpressionFunctionException(name);
            }
        }

        @Override
        public ExpressionFunctionInfoSet expressionFunctionInfos() {
            return SpreadsheetExpressionFunctions.infoSet(
                Sets.of(
                    SpreadsheetExpressionFunctions.info(
                        Url.parseAbsolute("https://example.com/expression-function-1"),
                        SpreadsheetExpressionFunctions.name("ExpressionFunction1")
                    ),
                    SpreadsheetExpressionFunctions.info(
                        Url.parseAbsolute("https://example.com/expression-function-2"),
                        SpreadsheetExpressionFunctions.name("ExpressionFunction2")
                    )
                )
            );
        }

        @Override
        public CaseSensitivity expressionFunctionNameCaseSensitivity() {
            return SpreadsheetExpressionFunctions.NAME_CASE_SENSITIVITY;
        }
    };

    static class TestFunction extends FakeExpressionFunction<Object, SpreadsheetExpressionEvaluationContext> {

        TestFunction(final ExpressionFunctionName name) {
            this.name = name;
        }

        @Override
        public Object apply(final List<Object> parameters,
                            final SpreadsheetExpressionEvaluationContext context) {
            return context.expressionNumberKind().create(123);
        }

        @Override
        public Optional<ExpressionFunctionName> name() {
            return Optional.of(this.name);
        }

        private final ExpressionFunctionName name;

        @Override
        public ExpressionFunction<Object, SpreadsheetExpressionEvaluationContext> setName(final Optional<ExpressionFunctionName> name) {
            return new TestFunction(name.get());
        }

        @Override
        public List<ExpressionFunctionParameter<?>> parameters(final int count) {
            return Lists.empty();
        }
    }

    // with.............................................................................................................

    private final static Indentation INDENTATION = Indentation.SPACES2;

    private final static LineEnding LINE_ENDING = LineEnding.NL;

    private final static MediaTypeDetector MEDIA_TYPE_DETECTOR = (filename, binary) ->
        filename.endsWith(".java") ?
            MediaType.parse("text/java") :
            MediaType.BINARY;

    private final static Function<UrlPath, Either<WebFile, HttpStatus>> FILE_SERVER = (p) -> {
        throw new UnsupportedOperationException();
    };

    private final static Function<HttpHandler, HttpServer> SERVER = (h) -> {
        throw new UnsupportedOperationException();
    };

    private final static SpreadsheetServerContext SPREADSHEET_SERVER_CONTEXT = SpreadsheetServerContexts.fake();
    
    @Test
    public void testWithNullMediaTypeDetectorFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetHttpServer.with(
                null,
                FILE_SERVER,
                SERVER,
                SPREADSHEET_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullFileServerFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetHttpServer.with(
                MEDIA_TYPE_DETECTOR,
                null,
                SERVER,
                SPREADSHEET_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullServerFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetHttpServer.with(
                MEDIA_TYPE_DETECTOR,
                FILE_SERVER,
                null,
                SPREADSHEET_SERVER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetServerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetHttpServer.with(
                MEDIA_TYPE_DETECTOR,
                FILE_SERVER,
                SERVER,
                null
            )
        );
    }

    // start............................................................................................................

    @Test
    public void testStartServer() {
        this.startServer();
    }

    // plugin...........................................................................................................

    @Test
    public void testPluginPostMultipartUploadFail() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpRequests.post(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/*/upload"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setContentType(MediaType.MULTIPART_FORM_DATA)
                    .setAccept(
                        MediaType.BINARY.accept()
                    )
            ), // request
            HttpStatusCode.BAD_REQUEST.status()
                .setMessage("Multipart, content-type missing boundary"),
            "java.lang.IllegalArgumentException: Multipart, content-type missing boundary"
        );
    }

    @Test
    public void testPluginPostMultipartUpload() {
        final TestHttpServer server = this.startServer();

        final PluginName pluginName = PluginName.with("TestPlugin111");

        final String manifest = "Manifest-Version: 1.0\r\n" +
            "plugin-name: TestPlugin111\r\n" +
            "plugin-provider-factory-className: example.TestPlugin111\r\n";

        final Binary jar = Binary.with(
            JarFileTesting.jarFile(
                manifest,
                Maps.empty()
            )
        );

        server.handleAndCheck(
            HttpRequests.post(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/*/upload"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setContentType(
                    MediaType.MULTIPART_FORM_DATA.setBoundary(
                        MediaTypeBoundary.parse("delimiter12345")
                    )
                ).setBodyText(
                    "--delimiter12345\r\n" +
                        "Content-Disposition: form-data; name=\"field2\"; filename=\"TestPlugin111.jar\"\r\n" +
                        "\r\n" +
                        new String(
                            jar.value(),
                            HttpEntity.DEFAULT_BODY_CHARSET
                        ) +
                        "\r\n" +
                        "--delimiter12345--"
                )
            ), // request
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(SpreadsheetServerMediaTypes.CONTENT_TYPE)
                    .setBodyText(
                        toJson(
                            Plugin.with(
                                pluginName,
                                "TestPlugin111.jar",
                                jar,
                                USER,
                                NOW.now()
                            )
                        )
                    ).setContentLength()
            )
        );
    }

    @Test
    public void testPluginPostBinaryUpload() {
        final TestHttpServer server = this.startServer();

        final PluginName pluginName = PluginName.with("TestPlugin111");

        final String manifest = "Manifest-Version: 1.0\r\n" +
            "plugin-name: TestPlugin111\r\n" +
            "plugin-provider-factory-className: example.TestPlugin111\r\n";

        final Binary jar = Binary.with(
            JarFileTesting.jarFile(
                manifest,
                Maps.empty()
            )
        );

        final ContentDisposition contentDisposition = ContentDispositionType.ATTACHMENT.setFilename(
            ContentDispositionFileName.notEncoded("TestPlugin111.jar")
        );

        server.handleAndCheck(
            HttpRequests.post(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/*/upload"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setContentType(SpreadsheetServerMediaTypes.BINARY)
                    .addHeader(
                        HttpHeaderName.CONTENT_DISPOSITION,
                        contentDisposition
                    ).setBody(
                        Binary.with(jar.value())
                    )
            ), // request
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(SpreadsheetServerMediaTypes.CONTENT_TYPE)
                    .setBodyText(
                        toJson(
                            Plugin.with(
                                pluginName,
                                "TestPlugin111.jar",
                                jar,
                                USER,
                                NOW.now()
                            )
                        )
                    ).setContentLength()
            )
        );
    }

    @Test
    public void testPluginGetInvalidPluginNameGivesBadRequest() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/plugin/!invalid-plugin-name",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.BAD_REQUEST.setMessage("Invalid character '!' at 0"),
            "walkingkooka.InvalidCharacterException: Invalid character '!' at 0"
        );
    }

    @Test
    public void testPluginGetUnknownPluginName() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/plugin/UnknownPluginName123",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.NO_CONTENT.setMessage("No content"),
            ""
        );
    }

    @Test
    public void testPluginGetAll() {
        final TestHttpServer server = this.startServer();

        // get all plugins
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/plugin/*",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[]",
                PluginSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testPluginDeleteUnknownPlugin() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.DELETE,
            "/api/plugin/UnknownPlugin123",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.NO_CONTENT.setMessage("No content"),
            ""
        );
    }

    @Test
    public void testPluginSavePlugin() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/plugin/TestPlugin111",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                Plugin.with(
                    PluginName.with("TestPlugin111"),
                    "plugin-TestPlugin111.jar",
                    Binary.with("Hello".getBytes(Charset.defaultCharset())),
                    USER,
                    LocalDateTime.of(
                        1999,
                        12,
                        31,
                        12,
                        58
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"name\": \"TestPlugin111\",\n" +
                    "  \"filename\": \"plugin-TestPlugin111.jar\",\n" +
                    "  \"archive\": \"SGVsbG8=\",\n" +
                    "  \"user\": \"user@example.com\",\n" +
                    "  \"timestamp\": \"1999-12-31T12:58\"\n" +
                    "}",
                Plugin.class.getSimpleName()
            )
        );
    }

    @Test
    public void testPluginDownloadGetAbsent() {
        final TestHttpServer server = this.startServer();

        // get all plugins
        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/TestPlugin111/download"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    SpreadsheetServerMediaTypes.BINARY.accept()
                )
            ),
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                HttpEntity.EMPTY
            )
        );
    }

    @Test
    public void testPluginDownloadGetWithAcceptAll() {
        final TestHttpServer server = this.startServer();

        final Plugin plugin = Plugin.with(
            PluginName.with("TestPlugin111"),
            "TestPlugin111-download.jar",
            Binary.with(
                JarFileTesting.jarFile(
                    "Manifest-Version: 1.0\r\n\rn",
                    Maps.of(
                        "dir111/file111.txt",
                        "Hello".getBytes(StandardCharsets.UTF_8)
                    )
                )
            ),
            USER,
            NOW.now()
        );

        server.pluginStore.save(plugin);

        // get all plugins
        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/TestPlugin111/download"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    MediaType.ALL.accept()
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(SpreadsheetServerMediaTypes.BINARY)
                    .addHeader(
                        HttpHeaderName.CONTENT_DISPOSITION,
                        ContentDispositionType.ATTACHMENT.setFilename(
                            ContentDispositionFileName.notEncoded("TestPlugin111-download.jar")
                        )
                    ).addHeader(
                        JsonHttpHandlers.X_CONTENT_TYPE_NAME,
                        JarEntryInfoName.class.getSimpleName()
                    ).setBody(
                        plugin.archive()
                    ).setContentLength()
            )
        );
    }

    @Test
    public void testPluginDownloadGetWithAcceptBinary() {
        final TestHttpServer server = this.startServer();

        final Plugin plugin = Plugin.with(
            PluginName.with("TestPlugin111"),
            "TestPlugin111-download.jar",
            Binary.with(
                JarFileTesting.jarFile(
                    "Manifest-Version: 1.0\r\n\rn",
                    Maps.of(
                        "dir111/file111.txt",
                        "Hello".getBytes(StandardCharsets.UTF_8)
                    )
                )
            ),
            USER,
            NOW.now()
        );

        server.pluginStore.save(plugin);

        // get all plugins
        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/TestPlugin111/download"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    SpreadsheetServerMediaTypes.BINARY.accept()
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(SpreadsheetServerMediaTypes.BINARY)
                    .addHeader(
                        HttpHeaderName.CONTENT_DISPOSITION,
                        ContentDispositionType.ATTACHMENT.setFilename(
                            ContentDispositionFileName.notEncoded("TestPlugin111-download.jar")
                        )
                    ).addHeader(
                        JsonHttpHandlers.X_CONTENT_TYPE_NAME,
                        JarEntryInfoName.class.getSimpleName()
                    ).setBody(
                        plugin.archive()
                    ).setContentLength()
            )
        );
    }

    @Test
    public void testPluginPluginNameListGetMissing() {
        final TestHttpServer server = this.startServer();

        // get all plugins
        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/TestPlugin111/list"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    SpreadsheetServerMediaTypes.CONTENT_TYPE.accept()
                )
            ),
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                HttpEntity.EMPTY
            )
        );
    }

    @Test
    public void testPluginListGet() {
        final TestHttpServer server = this.startServer();

        final Plugin plugin = Plugin.with(
            PluginName.with("TestPlugin111"),
            "TestPlugin111-download.jar",
            Binary.with(
                JarFileTesting.jarFile(
                    "Manifest-Version: 1.0\r\n" +
                        "plugin-name: PluginName\r\n" +
                        "plugin-provider-factory-className: example.PluginName\r\n",
                    Maps.of(
                        "file111.txt",
                        "Hello111".getBytes(StandardCharsets.UTF_8),
                        "dir222/",
                        new byte[0],
                        "file333.txt",
                        "Hello333".getBytes(StandardCharsets.UTF_8)
                    )
                )
            ),
            USER,
            NOW.now()
        );

        server.pluginStore.save(plugin);

        // list all plugins
        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/TestPlugin111/list"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    SpreadsheetServerMediaTypes.CONTENT_TYPE.accept()
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(SpreadsheetServerMediaTypes.CONTENT_TYPE)
                    .addHeader(HateosResourceMappings.X_CONTENT_TYPE_NAME, JarEntryInfoList.class.getSimpleName())
                    .setBodyText(
                        "[\n" +
                            "  {\n" +
                            "    \"name\": \"/META-INF/MANIFEST.MF\",\n" +
                            "    \"method\": 8,\n" +
                            "    \"create\": \"1999-12-31T12:58\",\n" +
                            "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"name\": \"/file111.txt\",\n" +
                            "    \"method\": 8,\n" +
                            "    \"create\": \"1999-12-31T12:58\",\n" +
                            "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"name\": \"/dir222/\",\n" +
                            "    \"method\": 8,\n" +
                            "    \"create\": \"1999-12-31T12:58\",\n" +
                            "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                            "  },\n" +
                            "  {\n" +
                            "    \"name\": \"/file333.txt\",\n" +
                            "    \"method\": 8,\n" +
                            "    \"create\": \"1999-12-31T12:58\",\n" +
                            "    \"lastModified\": \"2000-01-02T04:58\"\n" +
                            "  }\n" +
                            "]"
                    ).setContentLength()
            )
        );
    }


    @Test
    public void testPluginPluginNameFileDownloadGetMissing() {
        final TestHttpServer server = this.startServer();

        // get all plugins
        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/TestPlugin111/download/file-absent.txt"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY
            ),
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                HttpEntity.EMPTY
            )
        );
    }

    @Test
    public void testPluginPluginNameFileDownloadGet() {
        final TestHttpServer server = this.startServer();

        final String fileContent = "Hello";

        final Plugin plugin = Plugin.with(
            PluginName.with("TestPlugin111"),
            "TestPlugin111-download.jar",
            Binary.with(
                JarFileTesting.jarFile(
                    "Manifest-Version: 1.0\r\n\r\n",
                    Maps.of(
                        "example/Example.java",
                        fileContent.getBytes(StandardCharsets.UTF_8)
                    )
                )
            ),
            USER,
            NOW.now()
        );

        server.pluginStore.save(plugin);

        final String contentType = "text/java";

        // get all plugins
        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/TestPlugin111/download/example/Example.java"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    Accept.parse(contentType)
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(MediaType.parse(contentType))
                    .addHeader(
                        HttpHeaderName.CONTENT_DISPOSITION,
                        ContentDispositionType.ATTACHMENT.setFilename(
                            ContentDispositionFileName.notEncoded("/example/Example.java")
                        )
                    ).addHeader(
                        JsonHttpHandlers.X_CONTENT_TYPE_NAME,
                        JarEntryInfoName.class.getSimpleName()
                    ).setBodyText(fileContent)
                    .setContentLength()
            )
        );
    }

    @Test
    public void testPluginFilterGet() {
        final TestHttpServer server = this.startServer();

        final String fileContent = "Hello";

        final Plugin plugin1 = Plugin.with(
            PluginName.with("TestPlugin111"),
            "TestPlugin111-archive.jar",
            Binary.with(
                JarFileTesting.jarFile(
                    "Manifest-Version: 1.0\r\n\r\n",
                    Maps.of(
                        "example/Example.java",
                        fileContent.getBytes(StandardCharsets.UTF_8)
                    )
                )
            ),
            USER,
            NOW.now()
        );

        server.pluginStore.save(plugin1);

        final Plugin plugin2 = Plugin.with(
            PluginName.with("TestPlugin222"),
            "TestPlugin222-archive.jar",
            Binary.with(
                JarFileTesting.jarFile(
                    "Manifest-Version: 1.0\r\n\r\n",
                    Maps.of(
                        "example/Example.java",
                        fileContent.getBytes(StandardCharsets.UTF_8)
                    )
                )
            ),
            USER,
            NOW.now()
        );

        server.pluginStore.save(plugin2);

        final Plugin plugin3 = Plugin.with(
            PluginName.with("TestPlugin333"),
            "TestPlugin333-archive.jar",
            Binary.with(
                JarFileTesting.jarFile(
                    "Manifest-Version: 1.0\r\n\r\n",
                    Maps.of(
                        "example/Example.java",
                        fileContent.getBytes(StandardCharsets.UTF_8)
                    )
                )
            ),
            USER,
            NOW.now()
        );

        server.pluginStore.save(plugin3);

        server.handleAndCheck(
            HttpRequests.get(
                HttpTransport.UNSECURED,
                Url.parseRelative("/api/plugin/*/filter?query=*&offset=1&count=1"),
                HttpProtocolVersion.VERSION_1_0,
                HttpEntity.EMPTY.setAccept(
                    SpreadsheetServerMediaTypes.CONTENT_TYPE.accept()
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                toJson(
                    PluginSet.with(
                        SortedSets.of(plugin2)
                    )
                ),
                PluginSet.class.getSimpleName()
            )
        );
    }

    // spreadsheet......................................................................................................

    @Test
    public void testMetadataGetInvalidSpreadsheetIdGivesBadRequest() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/XYZ",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.BAD_REQUEST.setMessage("Invalid id \"XYZ\""),
            "java.lang.IllegalArgumentException: Invalid id \"XYZ\""
        );
    }

    @Test
    public void testMetadataGetUnknownNotFound() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/99",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.NO_CONTENT.status()
                .setMessage("SpreadsheetMetadata: Missing 99"),
            "SpreadsheetMetadata: Missing 99"
        );
    }

    @Test
    public void testMetadataPostCreate() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.CREATED.status(),
                this.createMetadata()
                    .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SPREADSHEET_ID)
            )
        );
        this.checkNotEquals(
            null,
            this.metadataStore.load(SPREADSHEET_ID),
            () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );
    }

    @Test
    public void testMetadataPostCreateWithTransactionIdHeader() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        this.checkNotEquals(
            null,
            this.metadataStore.load(SPREADSHEET_ID),
            () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );
    }

    @Test
    public void testMetadataPostCreateThenGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        this.checkNotEquals(
            null,
            this.metadataStore.load(SPREADSHEET_ID),
            () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );

        // fetch metadata back again.
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"spreadsheetId\": \"1\",\n" +
                    "  \"auditInfo\": {\n" +
                    "    \"createdBy\": \"user@example.com\",\n" +
                    "    \"createdTimestamp\": \"1999-12-31T12:58\",\n" +
                    "    \"modifiedBy\": \"user@example.com\",\n" +
                    "    \"modifiedTimestamp\": \"1999-12-31T12:58\"\n" +
                    "  },\n" +
                    "  \"autoHideScrollbars\": false,\n" +
                    "  \"cellCharacterWidth\": 1,\n" +
                    "  \"color1\": \"black\",\n" +
                    "  \"color2\": \"white\",\n" +
                    "  \"colorBlack\": 1,\n" +
                    "  \"colorWhite\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, boolean, boolean-to-text, collection, collection-to-list, color, color-to-color, color-to-number, date-time, date-time-symbols, decimal-number-symbols, environment, error-throwing, error-to-error, error-to-number, expression, form-and-validation, format-pattern-to-string, has-formatter-selector, has-host-address, has-parser-selector, has-spreadsheet-selection, has-style, has-text-node, has-validator-selector, json, jsonTo, locale, locale-to-text, null-to-number, number, number-to-color, number-to-number, number-to-text, plugins, spreadsheet-cell-set, spreadsheet-metadata, spreadsheet-selection-to-spreadsheet-selection, spreadsheet-selection-to-text, spreadsheet-value, style, system, template, text, text-node, text-to-boolean-list, text-to-color, text-to-csv-string-list, text-to-date-list, text-to-date-time-list, text-to-email-address, text-to-environment-value-name, text-to-error, text-to-expression, text-to-form-name, text-to-has-host-address, text-to-json, text-to-locale, text-to-number-list, text-to-object, text-to-spreadsheet-color-name, text-to-spreadsheet-formatter-selector, text-to-spreadsheet-id, text-to-spreadsheet-metadata, text-to-spreadsheet-metadata-color, text-to-spreadsheet-metadata-property-name, text-to-spreadsheet-name, text-to-spreadsheet-selection, text-to-spreadsheet-text, text-to-string-list, text-to-template-value-name, text-to-text, text-to-text-node, text-to-text-style, text-to-text-style-property-name, text-to-time-list, text-to-url, text-to-validation-error, text-to-validator-selector, text-to-value-type, to-boolean, to-json, to-number, to-styleable, to-validation-error-list, url, url-to-hyperlink, url-to-image\",\n" +
                    "  \"dateFormatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"dateParser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"dateTimeFormatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeOffset\": \"-25569\",\n" +
                    "  \"dateTimeParser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeSymbols\": {\n" +
                    "    \"ampms\": [\n" +
                    "      \"am\",\n" +
                    "      \"pm\"\n" +
                    "    ],\n" +
                    "    \"monthNames\": [\n" +
                    "      \"January\",\n" +
                    "      \"February\",\n" +
                    "      \"March\",\n" +
                    "      \"April\",\n" +
                    "      \"May\",\n" +
                    "      \"June\",\n" +
                    "      \"July\",\n" +
                    "      \"August\",\n" +
                    "      \"September\",\n" +
                    "      \"October\",\n" +
                    "      \"November\",\n" +
                    "      \"December\"\n" +
                    "    ],\n" +
                    "    \"monthNameAbbreviations\": [\n" +
                    "      \"Jan.\",\n" +
                    "      \"Feb.\",\n" +
                    "      \"Mar.\",\n" +
                    "      \"Apr.\",\n" +
                    "      \"May\",\n" +
                    "      \"Jun.\",\n" +
                    "      \"Jul.\",\n" +
                    "      \"Aug.\",\n" +
                    "      \"Sep.\",\n" +
                    "      \"Oct.\",\n" +
                    "      \"Nov.\",\n" +
                    "      \"Dec.\"\n" +
                    "    ],\n" +
                    "    \"weekDayNames\": [\n" +
                    "      \"Sunday\",\n" +
                    "      \"Monday\",\n" +
                    "      \"Tuesday\",\n" +
                    "      \"Wednesday\",\n" +
                    "      \"Thursday\",\n" +
                    "      \"Friday\",\n" +
                    "      \"Saturday\"\n" +
                    "    ],\n" +
                    "    \"weekDayNameAbbreviations\": [\n" +
                    "      \"Sun.\",\n" +
                    "      \"Mon.\",\n" +
                    "      \"Tue.\",\n" +
                    "      \"Wed.\",\n" +
                    "      \"Thu.\",\n" +
                    "      \"Fri.\",\n" +
                    "      \"Sat.\"\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"decimalNumberSymbols\": {\n" +
                    "    \"negativeSign\": \"-\",\n" +
                    "    \"positiveSign\": \"+\",\n" +
                    "    \"zeroDigit\": \"0\",\n" +
                    "    \"currencySymbol\": \"$\",\n" +
                    "    \"decimalSeparator\": \".\",\n" +
                    "    \"exponentSymbol\": \"e\",\n" +
                    "    \"groupSeparator\": \",\",\n" +
                    "    \"infinitySymbol\": \"∞\",\n" +
                    "    \"monetaryDecimalSeparator\": \".\",\n" +
                    "    \"nanSymbol\": \"NaN\",\n" +
                    "    \"percentSymbol\": \"%\",\n" +
                    "    \"permillSymbol\": \"‰\"\n" +
                    "  },\n" +
                    "  \"defaultFormHandler\": \"basic\",\n" +
                    "  \"defaultYear\": 2000,\n" +
                    "  \"errorFormatter\": \"badge-error text-format-pattern @\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expressionNumberKind\": \"BIG_DECIMAL\",\n" +
                    "  \"findConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"findFunctions\": \"@\",\n" +
                    "  \"formHandlers\": \"\",\n" +
                    "  \"formatters\": \"automatic, badge-error, collection, date-format-pattern, date-time-format-pattern, default-text, expression, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formattingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, plugins, style, text-node, template, url)\",\n" +
                    "  \"formattingFunctions\": \"@\",\n" +
                    "  \"formulaConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, template, url)\",\n" +
                    "  \"formulaFunctions\": \"@ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"functions\": \"@ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"generalNumberFormatDigitCount\": 8,\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"numberFormatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"numberParser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"roundingMode\": \"HALF_UP\",\n" +
                    "  \"scriptingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, plugins, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"scriptingFunctions\": \"@\",\n" +
                    "  \"showFormulaEditor\": true,\n" +
                    "  \"showFormulas\": false,\n" +
                    "  \"showGridLines\": true,\n" +
                    "  \"showHeadings\": true,\n" +
                    "  \"sortComparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sortConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, locale)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"textFormatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"timeFormatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"timeParser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"twoDigitYear\": 50,\n" +
                    "  \"validationConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, environment, error-throwing, expression, form-and-validation, locale, plugins, template)\",\n" +
                    "  \"validationFunctions\": \"@\",\n" +
                    "  \"validationValidators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"validators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"valueSeparator\": \",\"\n" +
                    "}",
                SpreadsheetMetadata.class.getSimpleName()
            )
        );
    }
    
    @Test
    public void testMetadataCreateAndPatch() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetMetadata loaded = this.metadataStore.loadOrFail(SPREADSHEET_ID);
        this.checkNotEquals(
            null,
            loaded,
            () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );

        // patch metadata
        final RoundingMode roundingMode = RoundingMode.FLOOR;

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            JsonNode.object()
                .set(
                    JsonPropertyName.with(
                        SpreadsheetMetadataPropertyName.ROUNDING_MODE.value()
                    ),
                    roundingMode.name()
                ).toJsonText(
                    INDENTATION,
                    LINE_ENDING
                ),
            this.response(
                HttpStatusCode.OK.status(),
                loaded.set(
                    SpreadsheetMetadataPropertyName.ROUNDING_MODE,
                    roundingMode
                )
            )
        );
    }

    @Test
    public void testMetdataPatchFails() {
        final TestHttpServer server = this.startServer();

        // patch metadata will fail with 204
        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            JsonNode.object()
                .set(
                    JsonPropertyName.with(
                        SpreadsheetMetadataPropertyName.ROUNDING_MODE.value()
                    ),
                    RoundingMode.FLOOR.name()
                )
                .toString(),
            HttpStatusCode.NO_CONTENT.setMessage("Unable to load spreadsheet with id=1"),
            "walkingkooka.store.MissingStoreException: Unable to load spreadsheet with id=1"
        );
    }

    // cell.............................................................................................................
    
    @Test
    public void testCellPostSaveApostropheString() {
        this.createSpreadsheetSaveCellAndCheck(
            "'Hello123'",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"'Hello123'\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"'\",\n" +
                "                  \"text\": \"'\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"Hello123'\",\n" +
                "                  \"text\": \"Hello123'\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"'Hello123'\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": \"Hello123'\"\n" +
                "        },\n" +
                "        \"value\": \"Hello123'\"\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Text Hello123'\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    @Test
    public void testCellPostSaveDateValue() {
        this.createSpreadsheetSaveCellAndCheck(
            "2000/12/31",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"2000/12/31\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"date-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"year-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 2000,\n" +
                "                  \"text\": \"2000\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"/\",\n" +
                "                  \"text\": \"/\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"month-number-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 12,\n" +
                "                  \"text\": \"12\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"/\",\n" +
                "                  \"text\": \"/\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"day-number-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 31,\n" +
                "                  \"text\": \"31\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"2000/12/31\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": {\n" +
                "            \"type\": \"local-date\",\n" +
                "            \"value\": \"2000-12-31\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"local-date\",\n" +
                "          \"value\": \"2000-12-31\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Date 2000/12/31\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    @Test
    public void testCellSavePostDateTimeValue() {
        this.createSpreadsheetSaveCellAndCheck(
            "2000/12/31 12:34",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"2000/12/31 12:34\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"date-time-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"year-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 2000,\n" +
                "                  \"text\": \"2000\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"/\",\n" +
                "                  \"text\": \"/\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"month-number-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 12,\n" +
                "                  \"text\": \"12\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"/\",\n" +
                "                  \"text\": \"/\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"day-number-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 31,\n" +
                "                  \"text\": \"31\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"whitespace-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \" \",\n" +
                "                  \"text\": \" \"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"hour-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 12,\n" +
                "                  \"text\": \"12\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \":\",\n" +
                "                  \"text\": \":\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"minute-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 34,\n" +
                "                  \"text\": \"34\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"2000/12/31 12:34\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": {\n" +
                "            \"type\": \"local-date-time\",\n" +
                "            \"value\": \"2000-12-31T12:34\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"local-date-time\",\n" +
                "          \"value\": \"2000-12-31T12:34\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"DateTime 2000/12/31 12:34\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    @Test
    public void testCellSavePostNumberValue() {
        this.createSpreadsheetSaveCellAndCheck(
            "123.456",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"123.456\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"123\",\n" +
                "                  \"text\": \"123\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"decimal-separator-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \".\",\n" +
                "                  \"text\": \".\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"456\",\n" +
                "                  \"text\": \"456\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"123.456\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": {\n" +
                "            \"type\": \"expression-number\",\n" +
                "            \"value\": \"123.456\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"expression-number\",\n" +
                "          \"value\": \"123.456\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Number 123.456\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    @Test
    public void testCellSavePostTimeValue() {
        this.createSpreadsheetSaveCellAndCheck(
            "12:34",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"12:34\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"time-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"hour-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 12,\n" +
                "                  \"text\": \"12\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \":\",\n" +
                "                  \"text\": \":\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"minute-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": 34,\n" +
                "                  \"text\": \"34\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"12:34\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": {\n" +
                "            \"type\": \"local-time\",\n" +
                "            \"value\": \"12:34\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"local-time\",\n" +
                "          \"value\": \"12:34\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Time 12:34\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    @Test
    public void testCellSavePostExpressionString() {
        this.createSpreadsheetSaveCellAndCheck(
            "=\"Hello 123\"",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=\\\"Hello 123\\\"\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"double-quote-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"\\\"\",\n" +
                "                        \"text\": \"\\\"\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"Hello 123\",\n" +
                "                        \"text\": \"Hello 123\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"double-quote-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"\\\"\",\n" +
                "                        \"text\": \"\\\"\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"\\\"Hello 123\\\"\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=\\\"Hello 123\\\"\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": \"Hello 123\"\n" +
                "        },\n" +
                "        \"value\": \"Hello 123\"\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Text Hello 123\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    @Test
    public void testCellSavePostExpressionNumber() {
        this.createSpreadsheetSaveCellAndCheck(
            "=1+2",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=1+2\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"1\",\n" +
                "                              \"text\": \"1\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"1\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"+\",\n" +
                "                        \"text\": \"+\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": [\n" +
                "                          {\n" +
                "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                            \"value\": {\n" +
                "                              \"value\": \"2\",\n" +
                "                              \"text\": \"2\"\n" +
                "                            }\n" +
                "                          }\n" +
                "                        ],\n" +
                "                        \"text\": \"2\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"1+2\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=1+2\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"add-expression\",\n" +
                "          \"value\": [\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"1\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"type\": \"value-expression\",\n" +
                "              \"value\": {\n" +
                "                \"type\": \"expression-number\",\n" +
                "                \"value\": \"2\"\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"expression-number\",\n" +
                "          \"value\": \"3\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Number 003.000\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    @Test
    public void testCellSaveSelectionUrlQueryParameterGet() {
        this.createSpreadsheetSaveCellAndCheck(
            "=\"Hello 123\"",
            "?home=A1&width=1000&height=800&selectionType=cell&selection=A2&window=",
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=\\\"Hello 123\\\"\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"double-quote-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"\\\"\",\n" +
                "                        \"text\": \"\\\"\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"Hello 123\",\n" +
                "                        \"text\": \"Hello 123\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"double-quote-symbol-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"\\\"\",\n" +
                "                        \"text\": \"\\\"\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"\\\"Hello 123\\\"\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=\\\"Hello 123\\\"\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": \"Hello 123\"\n" +
                "        },\n" +
                "        \"value\": \"Hello 123\"\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Text Hello 123\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"A\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"1\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 1,\n" +
                "  \"rowCount\": 1\n" +
                "}"
        );
    }

    // sort.............................................................................................................

    @Test
    public void testCellSortGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("'Zebra'")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Zebra'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Zebra'\",\n" +
                    "                  \"text\": \"Zebra'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Zebra'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Zebra'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Zebra'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Zebra'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // create another cell on the second row.
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("A2")
                                .setFormula(
                                    formula("'Avacado'")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Avacado'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Avacado'\",\n" +
                    "                  \"text\": \"Avacado'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Avacado'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Avacado'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Avacado'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Avacado'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // sort the two cells created, they should be swapped by the sort
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1:B2/sort?comparators=A=text",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Avacado'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Avacado'\",\n" +
                    "                  \"text\": \"Avacado'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Avacado'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Avacado'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Avacado'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Avacado'\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"A2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Zebra'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Zebra'\",\n" +
                    "                  \"text\": \"Zebra'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Zebra'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Zebra'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Zebra'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Zebra'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );
    }

    private TestHttpServer createSpreadsheetSaveCellAndCheck(final String formula,
                                                             final String responseJson) {
        return this.createSpreadsheetSaveCellAndCheck(formula, "", responseJson);
    }

    private TestHttpServer createSpreadsheetSaveCellAndCheck(final String formula,
                                                             final String queryParameters,
                                                             final String responseJson) {
        if (!queryParameters.isEmpty()) {
            UrlQueryString.parse(queryParameters);
        }
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.CREATED.status(),
                this.createMetadata()
                    .set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                        SPREADSHEET_ID
                    )
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1" + queryParameters,
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY
                .setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula(formula)
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                responseJson,
                DELTA
            )
        );

        return server;
    }

    @Test
    public void testCellSavePostThenSaveAnotherCellReferencingFirst() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY
                .setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula("=1+2")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1+2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"2\",\n" +
                    "                              \"text\": \"2\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1+2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1+2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"1\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"2\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"3\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 003.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("B2")
                                .setFormula(
                                    formula("=4+A1")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=4+A1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"4\",\n" +
                    "                              \"text\": \"4\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"4\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"cell-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"column-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"A\",\n" +
                    "                              \"text\": \"A\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"row-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"A1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"4+A1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=4+A1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"4\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"reference-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"spreadsheet-cell-reference\",\n" +
                    "                \"value\": \"A1\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"7\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 007.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellSavePostTwice() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("=1+2")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1+2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"2\",\n" +
                    "                              \"text\": \"2\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1+2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1+2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"1\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"2\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"3\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 003.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(HttpStatusCode.CREATED.status(),
                this.createMetadata()
                    .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(2L))
            )
        );

        this.checkEquals(2, this.metadataStore.count());

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/2/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("=3+4")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=3+4\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"3\",\n" +
                    "                              \"text\": \"3\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"3\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"4\",\n" +
                    "                              \"text\": \"4\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"4\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"3+4\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=3+4\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"3\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"4\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"7\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 007.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // create another cell in the first spreadsheet
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("B2")
                                .setFormula(
                                    formula("=4+A1")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=4+A1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"4\",\n" +
                    "                              \"text\": \"4\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"4\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"cell-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"column-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"A\",\n" +
                    "                              \"text\": \"A\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"row-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"A1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"4+A1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=4+A1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"4\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"reference-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"spreadsheet-cell-reference\",\n" +
                    "                \"value\": \"A1\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"7\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 007.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );
    }

    // not a perfect validation that function names are case-insensitive
    @Test
    public void testCellSavePostFormulaWithMixedCaseFunction() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("=expressionfunction1()")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=expressionfunction1()\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"named-function-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-name-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"expressionfunction1\",\n" +
                    "                        \"text\": \"expressionfunction1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-parameters-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-open-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"(\",\n" +
                    "                              \"text\": \"(\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-close-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \")\",\n" +
                    "                              \"text\": \")\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"()\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"expressionfunction1()\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=expressionfunction1()\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"call-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"callable\": {\n" +
                    "              \"type\": \"named-function-expression\",\n" +
                    "              \"value\": \"expressionfunction1\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"123\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 123.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellPatchInvalidCellFails() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/!!",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"formulaXXX\": {\n" +
                "    \"text\": \"=2\"\n" +
                "  }\n" +
                "}",
            HttpStatusCode.BAD_REQUEST
                .setMessage("Invalid character '!' at 0"),
            "Invalid character '!' at 0"
        );
    }

    @Test
    public void testCellPatchWithFormula() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("=1+2")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1+2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"2\",\n" +
                    "                              \"text\": \"2\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1+2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1+2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"1\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"2\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"3\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 003.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"a1\": {\n" +
                "      \"formula\": {\n" +
                "            \"text\": \"=2\"\n" +
                "          }\n" +
                "      }\n" +
                "    }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"2\",\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"2\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 002.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellSavePatchCellUnknownLabelFails() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/UnknownLabel123",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"ZZZ\": {\n" +
                "        \"formulaXXX\": {\n" +
                "           \"text\": \"'PatchedText123\"\n" +
                "        }\n" +
                "     }\n" +
                "  }\n" +
                "}",
            HttpStatusCode.BAD_REQUEST
                .setMessage("Label \"UnknownLabel123\" not found"),
            "Label \"UnknownLabel123\" not found"
        );
    }

    @Test
    public void testCellSavePatchCellLabel() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("ZZZ");
        final SpreadsheetCellReference cellReference = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingReference(cellReference);

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label/" + label,
            NO_HEADERS_TRANSACTION_ID,
            this.toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(mapping)
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/" + cellReference,
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            cellReference
                                .setFormula(
                                    formula("'Hello")
                                )
                        )
                    )
            ).replace(cellReference.toString(), label.toString()),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"ZZZ\",\n" +
                    "      \"reference\": \"B2\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"references\": {\n" +
                    "    \"B2\": [\n" +
                    "      {\n" +
                    "        \"type\": \"spreadsheet-label-name\",\n" +
                    "        \"value\": \"ZZZ\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // url and payload use label and not reference
        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/" + label,
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"ZZZ\": {\n" +
                "        \"formula\": {\n" +
                "           \"text\": \"'PatchedText123\"\n" +
                "        }\n" +
                "     }\n" +
                "  }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'PatchedText123\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"PatchedText123\",\n" +
                    "                  \"text\": \"PatchedText123\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'PatchedText123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"PatchedText123\"\n" +
                    "        },\n" +
                    "        \"value\": \"PatchedText123\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text PatchedText123\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"ZZZ\",\n" +
                    "      \"reference\": \"B2\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"references\": {\n" +
                    "    \"B2\": [\n" +
                    "      {\n" +
                    "        \"type\": \"spreadsheet-label-name\",\n" +
                    "        \"value\": \"ZZZ\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellDateTimeSymbolsPatch() {
        final TestHttpServer server = this.createSpreadsheetAndSaveCell();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"dateTimeSymbols\": {\n" +
                "        \"ampms\": [\n" +
                "          \"am\",\n" +
                "          \"pm\"\n" +
                "        ],\n" +
                "        \"monthNames\": [\n" +
                "          \"January\",\n" +
                "          \"February\",\n" +
                "          \"March\",\n" +
                "          \"April\",\n" +
                "          \"May\",\n" +
                "          \"June\",\n" +
                "          \"July\",\n" +
                "          \"August\",\n" +
                "          \"September\",\n" +
                "          \"October\",\n" +
                "          \"November\",\n" +
                "          \"December\"\n" +
                "        ],\n" +
                "        \"monthNameAbbreviations\": [\n" +
                "          \"Jan.\",\n" +
                "          \"Feb.\",\n" +
                "          \"Mar.\",\n" +
                "          \"Apr.\",\n" +
                "          \"May\",\n" +
                "          \"Jun.\",\n" +
                "          \"Jul.\",\n" +
                "          \"Aug.\",\n" +
                "          \"Sep.\",\n" +
                "          \"Oct.\",\n" +
                "          \"Nov.\",\n" +
                "          \"Dec.\"\n" +
                "        ],\n" +
                "        \"weekDayNames\": [\n" +
                "          \"Sunday\",\n" +
                "          \"Monday\",\n" +
                "          \"Tuesday\",\n" +
                "          \"Wednesday\",\n" +
                "          \"Thursday\",\n" +
                "          \"Friday\",\n" +
                "          \"Saturday\"\n" +
                "        ],\n" +
                "        \"weekDayNameAbbreviations\": [\n" +
                "          \"Sun.\",\n" +
                "          \"Mon.\",\n" +
                "          \"Tue.\",\n" +
                "          \"Wed.\",\n" +
                "          \"Thu.\",\n" +
                "          \"Fri.\",\n" +
                "          \"Sat.\"\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"999\",\n" +
                    "                        \"text\": \"999\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"999\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"999\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"dateTimeSymbols\": {\n" +
                    "        \"ampms\": [\n" +
                    "          \"am\",\n" +
                    "          \"pm\"\n" +
                    "        ],\n" +
                    "        \"monthNames\": [\n" +
                    "          \"January\",\n" +
                    "          \"February\",\n" +
                    "          \"March\",\n" +
                    "          \"April\",\n" +
                    "          \"May\",\n" +
                    "          \"June\",\n" +
                    "          \"July\",\n" +
                    "          \"August\",\n" +
                    "          \"September\",\n" +
                    "          \"October\",\n" +
                    "          \"November\",\n" +
                    "          \"December\"\n" +
                    "        ],\n" +
                    "        \"monthNameAbbreviations\": [\n" +
                    "          \"Jan.\",\n" +
                    "          \"Feb.\",\n" +
                    "          \"Mar.\",\n" +
                    "          \"Apr.\",\n" +
                    "          \"May\",\n" +
                    "          \"Jun.\",\n" +
                    "          \"Jul.\",\n" +
                    "          \"Aug.\",\n" +
                    "          \"Sep.\",\n" +
                    "          \"Oct.\",\n" +
                    "          \"Nov.\",\n" +
                    "          \"Dec.\"\n" +
                    "        ],\n" +
                    "        \"weekDayNames\": [\n" +
                    "          \"Sunday\",\n" +
                    "          \"Monday\",\n" +
                    "          \"Tuesday\",\n" +
                    "          \"Wednesday\",\n" +
                    "          \"Thursday\",\n" +
                    "          \"Friday\",\n" +
                    "          \"Saturday\"\n" +
                    "        ],\n" +
                    "        \"weekDayNameAbbreviations\": [\n" +
                    "          \"Sun.\",\n" +
                    "          \"Mon.\",\n" +
                    "          \"Tue.\",\n" +
                    "          \"Wed.\",\n" +
                    "          \"Thu.\",\n" +
                    "          \"Fri.\",\n" +
                    "          \"Sat.\"\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 999.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellDecimalNumberSymbolsPatch() {
        final TestHttpServer server = this.createSpreadsheetAndSaveCell();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"decimalNumberSymbols\": {\n" +
                "        \"negativeSign\": \"-\",\n" +
                "        \"positiveSign\": \"+\",\n" +
                "        \"zeroDigit\": \"0\",\n" +
                "        \"currencySymbol\": \"$\",\n" +
                "        \"decimalSeparator\": \".\",\n" +
                "        \"exponentSymbol\": \"e\",\n" +
                "        \"groupSeparator\": \",\",\n" +
                "        \"infinitySymbol\": \"∞\",\n" +
                "        \"monetaryDecimalSeparator\": \".\",\n" +
                "        \"nanSymbol\": \"NaN\",\n" +
                "        \"percentSymbol\": \"%\",\n" +
                "        \"permillSymbol\": \"‰\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"999\",\n" +
                    "                        \"text\": \"999\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"999\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"999\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"decimalNumberSymbols\": {\n" +
                    "        \"negativeSign\": \"-\",\n" +
                    "        \"positiveSign\": \"+\",\n" +
                    "        \"zeroDigit\": \"0\",\n" +
                    "        \"currencySymbol\": \"$\",\n" +
                    "        \"decimalSeparator\": \".\",\n" +
                    "        \"exponentSymbol\": \"e\",\n" +
                    "        \"groupSeparator\": \",\",\n" +
                    "        \"infinitySymbol\": \"∞\",\n" +
                    "        \"monetaryDecimalSeparator\": \".\",\n" +
                    "        \"nanSymbol\": \"NaN\",\n" +
                    "        \"percentSymbol\": \"%\",\n" +
                    "        \"permillSymbol\": \"‰\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 999.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellFormatterPatch() {
        final TestHttpServer server = this.createSpreadsheetAndSaveCell();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"a1\": {\n" +
                "      \"formatter\": \"text-format-pattern @\"\n" +
                "      }\n" +
                "    }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"999\",\n" +
                    "                        \"text\": \"999\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"999\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"999\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formatter\": \"text-format-pattern @\",\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"999\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLocalePatch() {
        final TestHttpServer server = this.createSpreadsheetAndSaveCell();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"a1\": {\n" +
                "      \"locale\": \"en-AU\"\n" +
                "      }\n" +
                "    }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"999\",\n" +
                    "                        \"text\": \"999\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"999\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"999\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"locale\": \"en-AU\",\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 999.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellParserPatch() {
        final TestHttpServer server = this.createSpreadsheetAndSaveCell();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"a1\": {\n" +
                "      \"parser\": \"number-parse-pattern ###\"\n" +
                "      }\n" +
                "    }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"spreadsheet-error\",\n" +
                    "          \"value\": {\n" +
                    "            \"kind\": \"ERROR\",\n" +
                    "            \"message\": \"Invalid character '=' at (1,1) expected \\\"###\\\"\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"parser\": \"number-parse-pattern ###\",\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"badge\",\n" +
                    "        \"value\": {\n" +
                    "          \"badgeText\": \"Invalid character '=' at (1,1) expected \\\"###\\\"\",\n" +
                    "          \"children\": [\n" +
                    "            {\n" +
                    "              \"type\": \"text\",\n" +
                    "              \"value\": \"#ERROR\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellStylePatch() {
        final TestHttpServer server = this.createSpreadsheetAndSaveCell();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"a1\": {\n" +
                "      \"style\": {\n" +
                "            \"color\": \"#123456\"\n" +
                "          }\n" +
                "      }\n" +
                "    }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"999\",\n" +
                    "                        \"text\": \"999\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"999\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"999\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"style\": {\n" +
                    "        \"color\": \"#123456\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text-style-node\",\n" +
                    "        \"value\": {\n" +
                    "          \"styles\": {\n" +
                    "            \"color\": \"#123456\"\n" +
                    "          },\n" +
                    "          \"children\": [\n" +
                    "            {\n" +
                    "              \"type\": \"text\",\n" +
                    "              \"value\": \"Number 999.000\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellValidatorPatch() {
        final TestHttpServer server = this.createSpreadsheetAndSaveCell();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"a1\": {\n" +
                "      \"validator\": \"Hello-validator\"\n" +
                "      }\n" +
                "    }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"999\",\n" +
                    "                        \"text\": \"999\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"999\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"spreadsheet-error\",\n" +
                    "          \"value\": {\n" +
                    "            \"kind\": \"VALUE\",\n" +
                    "            \"message\": \"Unknown validator Hello-validator\"\n" +
                    "          }\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"badge\",\n" +
                    "        \"value\": {\n" +
                    "          \"badgeText\": \"Unknown validator Hello-validator\",\n" +
                    "          \"children\": [\n" +
                    "            {\n" +
                    "              \"type\": \"text\",\n" +
                    "              \"value\": \"#VALUE!\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"validator\": \"Hello-validator\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    private TestHttpServer createSpreadsheetAndSaveCell() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // create cell with formula = 999
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("=999")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=999\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"999\",\n" +
                    "                        \"text\": \"999\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"999\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"999\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"999\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 999.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        return server;
    }

    @Test
    public void testCellPatchSelectionQueryParameter() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("=1+2")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1+2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"2\",\n" +
                    "                              \"text\": \"2\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1+2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1+2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"1\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"2\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"3\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 003.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/cell/A1?home=A1&width=1000&height=600&selectionType=cell&selection=B2&includeFrozenColumnsRows=false",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"cells\": {\n" +
                "     \"a1\": {\n" +
                "      \"formula\": {\n" +
                "            \"text\": \"'PATCHED\"\n" +
                "          }\n" +
                "      }\n" +
                "    }\n" +
                "}",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:1000.0:600.0\",\n" +
                    "    \"anchoredSelection\": {\n" +
                    "      \"selection\": {\n" +
                    "        \"type\": \"spreadsheet-cell-reference\",\n" +
                    "        \"value\": \"B2\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'PATCHED\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"PATCHED\",\n" +
                    "                  \"text\": \"PATCHED\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'PATCHED\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"PATCHED\"\n" +
                    "        },\n" +
                    "        \"value\": \"PATCHED\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text PATCHED\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:J12\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("=1+2")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1+2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"2\",\n" +
                    "                              \"text\": \"2\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1+2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1+2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"1\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"2\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"3\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 003.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA)
        );

        // create another cell in the first spreadsheet
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("B2")
                                .setFormula(
                                    formula("=4+A1")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=4+A1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"4\",\n" +
                    "                              \"text\": \"4\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"4\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"cell-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"column-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"A\",\n" +
                    "                              \"text\": \"A\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"row-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"A1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"4+A1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=4+A1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"4\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"reference-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"spreadsheet-cell-reference\",\n" +
                    "                \"value\": \"A1\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"7\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 007.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA)
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/*/force-recompute?home=A1&width=200&height=60&includeFrozenColumnsRows=false",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:200.0:100.0\"\n" +
                    "  },\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1+2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"2\",\n" +
                    "                              \"text\": \"2\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1+2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1+2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"1\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"2\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"3\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 003.000\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=4+A1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"4\",\n" +
                    "                              \"text\": \"4\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"4\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"cell-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"column-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"A\",\n" +
                    "                              \"text\": \"A\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"row-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1\",\n" +
                    "                              \"text\": \"1\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"A1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"4+A1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=4+A1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"4\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"reference-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"spreadsheet-cell-reference\",\n" +
                    "                \"value\": \"A1\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"7\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 007.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"references\": {\n" +
                    "    \"A1\": [\n" +
                    "      {\n" +
                    "        \"type\": \"spreadsheet-cell-reference\",\n" +
                    "        \"value\": \"B2\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B1,A2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportGetWithSelectionQueryParameters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("'Hello'")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/*/force-recompute?home=A1&width=200&height=60&selectionType=cell&selection=A1&includeFrozenColumnsRows=false",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:200.0:100.0\",\n" +
                    "    \"anchoredSelection\": {\n" +
                    "      \"selection\": {\n" +
                    "        \"type\": \"spreadsheet-cell-reference\",\n" +
                    "        \"value\": \"A1\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B1,A2,B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportGetWithSelectionQueryParametersWithAnchor() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("'Hello'")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/*/force-recompute?home=A1&width=200&height=60&selectionType=cell-range&selection=A1:B2&selectionAnchor=bottom-left&includeFrozenColumnsRows=false",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:200.0:100.0\",\n" +
                    "    \"anchoredSelection\": {\n" +
                    "      \"selection\": {\n" +
                    "        \"type\": \"spreadsheet-cell-range-reference\",\n" +
                    "        \"value\": \"A1:B2\"\n" +
                    "      },\n" +
                    "      \"anchor\": \"BOTTOM_LEFT\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B1,A2,B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportGetWithSelectionQueryParametersAnchorDefaulted() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("'Hello'")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/*/force-recompute?home=A1&width=200&height=60&selectionType=cell-range&selection=A1:B2&includeFrozenColumnsRows=false",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:200.0:100.0\",\n" +
                    "    \"anchoredSelection\": {\n" +
                    "      \"selection\": {\n" +
                    "        \"type\": \"spreadsheet-cell-range-reference\",\n" +
                    "        \"value\": \"A1:B2\"\n" +
                    "      },\n" +
                    "      \"anchor\": \"BOTTOM_RIGHT\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B1,A2,B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportGetWithSelectionAndNavigationQueryParameters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("'Hello'")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/*/force-recompute?home=A1&width=200&height=60&selectionType=cell&selection=A1&navigation=extend-right+column&includeFrozenColumnsRows=false",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:200.0:100.0\",\n" +
                    "    \"anchoredSelection\": {\n" +
                    "      \"selection\": {\n" +
                    "        \"type\": \"spreadsheet-cell-range-reference\",\n" +
                    "        \"value\": \"A1:B1\"\n" +
                    "      },\n" +
                    "      \"anchor\": \"TOP_LEFT\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B1,A2,B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );

        // verify metadata was updated and saved.
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"spreadsheetId\": \"1\",\n" +
                    "  \"auditInfo\": {\n" +
                    "    \"createdBy\": \"user@example.com\",\n" +
                    "    \"createdTimestamp\": \"1999-12-31T12:58\",\n" +
                    "    \"modifiedBy\": \"user@example.com\",\n" +
                    "    \"modifiedTimestamp\": \"1999-12-31T12:58\"\n" +
                    "  },\n" +
                    "  \"autoHideScrollbars\": false,\n" +
                    "  \"cellCharacterWidth\": 1,\n" +
                    "  \"color1\": \"black\",\n" +
                    "  \"color2\": \"white\",\n" +
                    "  \"colorBlack\": 1,\n" +
                    "  \"colorWhite\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, boolean, boolean-to-text, collection, collection-to-list, color, color-to-color, color-to-number, date-time, date-time-symbols, decimal-number-symbols, environment, error-throwing, error-to-error, error-to-number, expression, form-and-validation, format-pattern-to-string, has-formatter-selector, has-host-address, has-parser-selector, has-spreadsheet-selection, has-style, has-text-node, has-validator-selector, json, jsonTo, locale, locale-to-text, null-to-number, number, number-to-color, number-to-number, number-to-text, plugins, spreadsheet-cell-set, spreadsheet-metadata, spreadsheet-selection-to-spreadsheet-selection, spreadsheet-selection-to-text, spreadsheet-value, style, system, template, text, text-node, text-to-boolean-list, text-to-color, text-to-csv-string-list, text-to-date-list, text-to-date-time-list, text-to-email-address, text-to-environment-value-name, text-to-error, text-to-expression, text-to-form-name, text-to-has-host-address, text-to-json, text-to-locale, text-to-number-list, text-to-object, text-to-spreadsheet-color-name, text-to-spreadsheet-formatter-selector, text-to-spreadsheet-id, text-to-spreadsheet-metadata, text-to-spreadsheet-metadata-color, text-to-spreadsheet-metadata-property-name, text-to-spreadsheet-name, text-to-spreadsheet-selection, text-to-spreadsheet-text, text-to-string-list, text-to-template-value-name, text-to-text, text-to-text-node, text-to-text-style, text-to-text-style-property-name, text-to-time-list, text-to-url, text-to-validation-error, text-to-validator-selector, text-to-value-type, to-boolean, to-json, to-number, to-styleable, to-validation-error-list, url, url-to-hyperlink, url-to-image\",\n" +
                    "  \"dateFormatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"dateParser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"dateTimeFormatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeOffset\": \"-25569\",\n" +
                    "  \"dateTimeParser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeSymbols\": {\n" +
                    "    \"ampms\": [\n" +
                    "      \"am\",\n" +
                    "      \"pm\"\n" +
                    "    ],\n" +
                    "    \"monthNames\": [\n" +
                    "      \"January\",\n" +
                    "      \"February\",\n" +
                    "      \"March\",\n" +
                    "      \"April\",\n" +
                    "      \"May\",\n" +
                    "      \"June\",\n" +
                    "      \"July\",\n" +
                    "      \"August\",\n" +
                    "      \"September\",\n" +
                    "      \"October\",\n" +
                    "      \"November\",\n" +
                    "      \"December\"\n" +
                    "    ],\n" +
                    "    \"monthNameAbbreviations\": [\n" +
                    "      \"Jan.\",\n" +
                    "      \"Feb.\",\n" +
                    "      \"Mar.\",\n" +
                    "      \"Apr.\",\n" +
                    "      \"May\",\n" +
                    "      \"Jun.\",\n" +
                    "      \"Jul.\",\n" +
                    "      \"Aug.\",\n" +
                    "      \"Sep.\",\n" +
                    "      \"Oct.\",\n" +
                    "      \"Nov.\",\n" +
                    "      \"Dec.\"\n" +
                    "    ],\n" +
                    "    \"weekDayNames\": [\n" +
                    "      \"Sunday\",\n" +
                    "      \"Monday\",\n" +
                    "      \"Tuesday\",\n" +
                    "      \"Wednesday\",\n" +
                    "      \"Thursday\",\n" +
                    "      \"Friday\",\n" +
                    "      \"Saturday\"\n" +
                    "    ],\n" +
                    "    \"weekDayNameAbbreviations\": [\n" +
                    "      \"Sun.\",\n" +
                    "      \"Mon.\",\n" +
                    "      \"Tue.\",\n" +
                    "      \"Wed.\",\n" +
                    "      \"Thu.\",\n" +
                    "      \"Fri.\",\n" +
                    "      \"Sat.\"\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"decimalNumberSymbols\": {\n" +
                    "    \"negativeSign\": \"-\",\n" +
                    "    \"positiveSign\": \"+\",\n" +
                    "    \"zeroDigit\": \"0\",\n" +
                    "    \"currencySymbol\": \"$\",\n" +
                    "    \"decimalSeparator\": \".\",\n" +
                    "    \"exponentSymbol\": \"e\",\n" +
                    "    \"groupSeparator\": \",\",\n" +
                    "    \"infinitySymbol\": \"∞\",\n" +
                    "    \"monetaryDecimalSeparator\": \".\",\n" +
                    "    \"nanSymbol\": \"NaN\",\n" +
                    "    \"percentSymbol\": \"%\",\n" +
                    "    \"permillSymbol\": \"‰\"\n" +
                    "  },\n" +
                    "  \"defaultFormHandler\": \"basic\",\n" +
                    "  \"defaultYear\": 2000,\n" +
                    "  \"errorFormatter\": \"badge-error text-format-pattern @\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expressionNumberKind\": \"BIG_DECIMAL\",\n" +
                    "  \"findConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"findFunctions\": \"@\",\n" +
                    "  \"formHandlers\": \"\",\n" +
                    "  \"formatters\": \"automatic, badge-error, collection, date-format-pattern, date-time-format-pattern, default-text, expression, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formattingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, plugins, style, text-node, template, url)\",\n" +
                    "  \"formattingFunctions\": \"@\",\n" +
                    "  \"formulaConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, template, url)\",\n" +
                    "  \"formulaFunctions\": \"@ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"functions\": \"@ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"generalNumberFormatDigitCount\": 8,\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"numberFormatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"numberParser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"roundingMode\": \"HALF_UP\",\n" +
                    "  \"scriptingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, plugins, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"scriptingFunctions\": \"@\",\n" +
                    "  \"showFormulaEditor\": true,\n" +
                    "  \"showFormulas\": false,\n" +
                    "  \"showGridLines\": true,\n" +
                    "  \"showHeadings\": true,\n" +
                    "  \"sortComparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sortConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, locale)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"textFormatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"timeFormatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"timeParser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"twoDigitYear\": 50,\n" +
                    "  \"validationConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, environment, error-throwing, expression, form-and-validation, locale, plugins, template)\",\n" +
                    "  \"validationFunctions\": \"@\",\n" +
                    "  \"validationValidators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"validators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"valueSeparator\": \",\",\n" +
                    "  \"viewportHome\": \"A1\",\n" +
                    "  \"viewportSelection\": {\n" +
                    "    \"selection\": {\n" +
                    "      \"type\": \"spreadsheet-cell-range-reference\",\n" +
                    "      \"value\": \"A1:B1\"\n" +
                    "    },\n" +
                    "    \"anchor\": \"TOP_LEFT\"\n" +
                    "  }\n" +
                    "}",
                SpreadsheetMetadata.class.getSimpleName()
            )
        );
    }

    @Test
    public void testCellLoadViewportGetWithSelectionLabelAndNavigationQueryParameters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetLabelName label123 = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label123.setLabelMappingReference(a1);

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label/",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(mapping)
                )
            ),
            this.response(
                HttpStatusCode.CREATED.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            a1.setFormula(
                                formula("'Hello'")
                            )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label123\",\n" +
                    "      \"reference\": \"A1\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"references\": {\n" +
                    "    \"A1\": [\n" +
                    "      {\n" +
                    "        \"type\": \"spreadsheet-label-name\",\n" +
                    "        \"value\": \"Label123\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // load the cells that fill the viewport
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/*/force-recompute?home=A1&width=200&height=60&selectionType=label&selection=Label123&navigation=extend-right+column&includeFrozenColumnsRows=false",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:200.0:100.0\",\n" +
                    "    \"anchoredSelection\": {\n" +
                    "      \"selection\": {\n" +
                    "        \"type\": \"spreadsheet-cell-range-reference\",\n" +
                    "        \"value\": \"A1:B1\"\n" +
                    "      },\n" +
                    "      \"anchor\": \"TOP_LEFT\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello'\",\n" +
                    "                  \"text\": \"Hello'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label123\",\n" +
                    "      \"reference\": \"A1\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"references\": {\n" +
                    "    \"A1\": [\n" +
                    "      {\n" +
                    "        \"type\": \"spreadsheet-label-name\",\n" +
                    "        \"value\": \"Label123\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B1,A2,B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadGetWithFunctionMissingFromFormulaExpressions() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            a1.setFormula(
                                formula("=ExpressionFunction1()")
                            )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=ExpressionFunction1()\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"named-function-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-name-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"ExpressionFunction1\",\n" +
                    "                        \"text\": \"ExpressionFunction1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-parameters-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-open-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"(\",\n" +
                    "                              \"text\": \"(\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-close-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \")\",\n" +
                    "                              \"text\": \")\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"()\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"ExpressionFunction1()\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=ExpressionFunction1()\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"call-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"callable\": {\n" +
                    "              \"type\": \"named-function-expression\",\n" +
                    "              \"value\": \"ExpressionFunction1\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"123\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 123.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // PATCH remove "ExpressionFunction1" from "formula-functions"
        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS.patch(
                    SpreadsheetExpressionFunctions.parseAliasSet("ExpressionFunction2")
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"spreadsheetId\": \"1\",\n" +
                    "  \"auditInfo\": {\n" +
                    "    \"createdBy\": \"user@example.com\",\n" +
                    "    \"createdTimestamp\": \"1999-12-31T12:58\",\n" +
                    "    \"modifiedBy\": \"user@example.com\",\n" +
                    "    \"modifiedTimestamp\": \"1999-12-31T12:58\"\n" +
                    "  },\n" +
                    "  \"autoHideScrollbars\": false,\n" +
                    "  \"cellCharacterWidth\": 1,\n" +
                    "  \"color1\": \"black\",\n" +
                    "  \"color2\": \"white\",\n" +
                    "  \"colorBlack\": 1,\n" +
                    "  \"colorWhite\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, boolean, boolean-to-text, collection, collection-to-list, color, color-to-color, color-to-number, date-time, date-time-symbols, decimal-number-symbols, environment, error-throwing, error-to-error, error-to-number, expression, form-and-validation, format-pattern-to-string, has-formatter-selector, has-host-address, has-parser-selector, has-spreadsheet-selection, has-style, has-text-node, has-validator-selector, json, jsonTo, locale, locale-to-text, null-to-number, number, number-to-color, number-to-number, number-to-text, plugins, spreadsheet-cell-set, spreadsheet-metadata, spreadsheet-selection-to-spreadsheet-selection, spreadsheet-selection-to-text, spreadsheet-value, style, system, template, text, text-node, text-to-boolean-list, text-to-color, text-to-csv-string-list, text-to-date-list, text-to-date-time-list, text-to-email-address, text-to-environment-value-name, text-to-error, text-to-expression, text-to-form-name, text-to-has-host-address, text-to-json, text-to-locale, text-to-number-list, text-to-object, text-to-spreadsheet-color-name, text-to-spreadsheet-formatter-selector, text-to-spreadsheet-id, text-to-spreadsheet-metadata, text-to-spreadsheet-metadata-color, text-to-spreadsheet-metadata-property-name, text-to-spreadsheet-name, text-to-spreadsheet-selection, text-to-spreadsheet-text, text-to-string-list, text-to-template-value-name, text-to-text, text-to-text-node, text-to-text-style, text-to-text-style-property-name, text-to-time-list, text-to-url, text-to-validation-error, text-to-validator-selector, text-to-value-type, to-boolean, to-json, to-number, to-styleable, to-validation-error-list, url, url-to-hyperlink, url-to-image\",\n" +
                    "  \"dateFormatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"dateParser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"dateTimeFormatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeOffset\": \"-25569\",\n" +
                    "  \"dateTimeParser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeSymbols\": {\n" +
                    "    \"ampms\": [\n" +
                    "      \"am\",\n" +
                    "      \"pm\"\n" +
                    "    ],\n" +
                    "    \"monthNames\": [\n" +
                    "      \"January\",\n" +
                    "      \"February\",\n" +
                    "      \"March\",\n" +
                    "      \"April\",\n" +
                    "      \"May\",\n" +
                    "      \"June\",\n" +
                    "      \"July\",\n" +
                    "      \"August\",\n" +
                    "      \"September\",\n" +
                    "      \"October\",\n" +
                    "      \"November\",\n" +
                    "      \"December\"\n" +
                    "    ],\n" +
                    "    \"monthNameAbbreviations\": [\n" +
                    "      \"Jan.\",\n" +
                    "      \"Feb.\",\n" +
                    "      \"Mar.\",\n" +
                    "      \"Apr.\",\n" +
                    "      \"May\",\n" +
                    "      \"Jun.\",\n" +
                    "      \"Jul.\",\n" +
                    "      \"Aug.\",\n" +
                    "      \"Sep.\",\n" +
                    "      \"Oct.\",\n" +
                    "      \"Nov.\",\n" +
                    "      \"Dec.\"\n" +
                    "    ],\n" +
                    "    \"weekDayNames\": [\n" +
                    "      \"Sunday\",\n" +
                    "      \"Monday\",\n" +
                    "      \"Tuesday\",\n" +
                    "      \"Wednesday\",\n" +
                    "      \"Thursday\",\n" +
                    "      \"Friday\",\n" +
                    "      \"Saturday\"\n" +
                    "    ],\n" +
                    "    \"weekDayNameAbbreviations\": [\n" +
                    "      \"Sun.\",\n" +
                    "      \"Mon.\",\n" +
                    "      \"Tue.\",\n" +
                    "      \"Wed.\",\n" +
                    "      \"Thu.\",\n" +
                    "      \"Fri.\",\n" +
                    "      \"Sat.\"\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"decimalNumberSymbols\": {\n" +
                    "    \"negativeSign\": \"-\",\n" +
                    "    \"positiveSign\": \"+\",\n" +
                    "    \"zeroDigit\": \"0\",\n" +
                    "    \"currencySymbol\": \"$\",\n" +
                    "    \"decimalSeparator\": \".\",\n" +
                    "    \"exponentSymbol\": \"e\",\n" +
                    "    \"groupSeparator\": \",\",\n" +
                    "    \"infinitySymbol\": \"∞\",\n" +
                    "    \"monetaryDecimalSeparator\": \".\",\n" +
                    "    \"nanSymbol\": \"NaN\",\n" +
                    "    \"percentSymbol\": \"%\",\n" +
                    "    \"permillSymbol\": \"‰\"\n" +
                    "  },\n" +
                    "  \"defaultFormHandler\": \"basic\",\n" +
                    "  \"defaultYear\": 2000,\n" +
                    "  \"errorFormatter\": \"badge-error text-format-pattern @\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expressionNumberKind\": \"BIG_DECIMAL\",\n" +
                    "  \"findConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"findFunctions\": \"@\",\n" +
                    "  \"formHandlers\": \"\",\n" +
                    "  \"formatters\": \"automatic, badge-error, collection, date-format-pattern, date-time-format-pattern, default-text, expression, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formattingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, plugins, style, text-node, template, url)\",\n" +
                    "  \"formattingFunctions\": \"@\",\n" +
                    "  \"formulaConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, template, url)\",\n" +
                    "  \"formulaFunctions\": \"@ExpressionFunction2\",\n" +
                    "  \"functions\": \"@ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"generalNumberFormatDigitCount\": 8,\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"numberFormatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"numberParser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"roundingMode\": \"HALF_UP\",\n" +
                    "  \"scriptingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, plugins, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"scriptingFunctions\": \"@\",\n" +
                    "  \"showFormulaEditor\": true,\n" +
                    "  \"showFormulas\": false,\n" +
                    "  \"showGridLines\": true,\n" +
                    "  \"showHeadings\": true,\n" +
                    "  \"sortComparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sortConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, locale)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"textFormatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"timeFormatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"timeParser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"twoDigitYear\": 50,\n" +
                    "  \"validationConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, environment, error-throwing, expression, form-and-validation, locale, plugins, template)\",\n" +
                    "  \"validationFunctions\": \"@\",\n" +
                    "  \"validationValidators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"validators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"valueSeparator\": \",\"\n" +
                    "}",
                SpreadsheetMetadata.class.getSimpleName()
            )
        );

        // GET cell with function missing from "formula-functions" should give ERROR
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/force-recompute",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=ExpressionFunction1()\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"named-function-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-name-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"ExpressionFunction1\",\n" +
                    "                        \"text\": \"ExpressionFunction1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-parameters-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-open-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"(\",\n" +
                    "                              \"text\": \"(\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-close-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \")\",\n" +
                    "                              \"text\": \")\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"()\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"ExpressionFunction1()\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=ExpressionFunction1()\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"call-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"callable\": {\n" +
                    "              \"type\": \"named-function-expression\",\n" +
                    "              \"value\": \"ExpressionFunction1\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"spreadsheet-error\",\n" +
                    "          \"value\": {\n" +
                    "            \"kind\": \"NAME\",\n" +
                    "            \"message\": \"Function not found: \\\"ExpressionFunction1\\\"\",\n" +
                    "            \"value\": {\n" +
                    "              \"type\": \"expression-function-name\",\n" +
                    "              \"value\": \"@ExpressionFunction1\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"badge\",\n" +
                    "        \"value\": {\n" +
                    "          \"badgeText\": \"Function not found: \\\"ExpressionFunction1\\\"\",\n" +
                    "          \"children\": [\n" +
                    "            {\n" +
                    "              \"type\": \"text\",\n" +
                    "              \"value\": \"#NAME?\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // PATCH addd "ExpressionFunction1" back into "formula-functions"
        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS.patch(
                    SpreadsheetExpressionFunctions.parseAliasSet("ExpressionFunction1")
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"spreadsheetId\": \"1\",\n" +
                    "  \"auditInfo\": {\n" +
                    "    \"createdBy\": \"user@example.com\",\n" +
                    "    \"createdTimestamp\": \"1999-12-31T12:58\",\n" +
                    "    \"modifiedBy\": \"user@example.com\",\n" +
                    "    \"modifiedTimestamp\": \"1999-12-31T12:58\"\n" +
                    "  },\n" +
                    "  \"autoHideScrollbars\": false,\n" +
                    "  \"cellCharacterWidth\": 1,\n" +
                    "  \"color1\": \"black\",\n" +
                    "  \"color2\": \"white\",\n" +
                    "  \"colorBlack\": 1,\n" +
                    "  \"colorWhite\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, boolean, boolean-to-text, collection, collection-to-list, color, color-to-color, color-to-number, date-time, date-time-symbols, decimal-number-symbols, environment, error-throwing, error-to-error, error-to-number, expression, form-and-validation, format-pattern-to-string, has-formatter-selector, has-host-address, has-parser-selector, has-spreadsheet-selection, has-style, has-text-node, has-validator-selector, json, jsonTo, locale, locale-to-text, null-to-number, number, number-to-color, number-to-number, number-to-text, plugins, spreadsheet-cell-set, spreadsheet-metadata, spreadsheet-selection-to-spreadsheet-selection, spreadsheet-selection-to-text, spreadsheet-value, style, system, template, text, text-node, text-to-boolean-list, text-to-color, text-to-csv-string-list, text-to-date-list, text-to-date-time-list, text-to-email-address, text-to-environment-value-name, text-to-error, text-to-expression, text-to-form-name, text-to-has-host-address, text-to-json, text-to-locale, text-to-number-list, text-to-object, text-to-spreadsheet-color-name, text-to-spreadsheet-formatter-selector, text-to-spreadsheet-id, text-to-spreadsheet-metadata, text-to-spreadsheet-metadata-color, text-to-spreadsheet-metadata-property-name, text-to-spreadsheet-name, text-to-spreadsheet-selection, text-to-spreadsheet-text, text-to-string-list, text-to-template-value-name, text-to-text, text-to-text-node, text-to-text-style, text-to-text-style-property-name, text-to-time-list, text-to-url, text-to-validation-error, text-to-validator-selector, text-to-value-type, to-boolean, to-json, to-number, to-styleable, to-validation-error-list, url, url-to-hyperlink, url-to-image\",\n" +
                    "  \"dateFormatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"dateParser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"dateTimeFormatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeOffset\": \"-25569\",\n" +
                    "  \"dateTimeParser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"dateTimeSymbols\": {\n" +
                    "    \"ampms\": [\n" +
                    "      \"am\",\n" +
                    "      \"pm\"\n" +
                    "    ],\n" +
                    "    \"monthNames\": [\n" +
                    "      \"January\",\n" +
                    "      \"February\",\n" +
                    "      \"March\",\n" +
                    "      \"April\",\n" +
                    "      \"May\",\n" +
                    "      \"June\",\n" +
                    "      \"July\",\n" +
                    "      \"August\",\n" +
                    "      \"September\",\n" +
                    "      \"October\",\n" +
                    "      \"November\",\n" +
                    "      \"December\"\n" +
                    "    ],\n" +
                    "    \"monthNameAbbreviations\": [\n" +
                    "      \"Jan.\",\n" +
                    "      \"Feb.\",\n" +
                    "      \"Mar.\",\n" +
                    "      \"Apr.\",\n" +
                    "      \"May\",\n" +
                    "      \"Jun.\",\n" +
                    "      \"Jul.\",\n" +
                    "      \"Aug.\",\n" +
                    "      \"Sep.\",\n" +
                    "      \"Oct.\",\n" +
                    "      \"Nov.\",\n" +
                    "      \"Dec.\"\n" +
                    "    ],\n" +
                    "    \"weekDayNames\": [\n" +
                    "      \"Sunday\",\n" +
                    "      \"Monday\",\n" +
                    "      \"Tuesday\",\n" +
                    "      \"Wednesday\",\n" +
                    "      \"Thursday\",\n" +
                    "      \"Friday\",\n" +
                    "      \"Saturday\"\n" +
                    "    ],\n" +
                    "    \"weekDayNameAbbreviations\": [\n" +
                    "      \"Sun.\",\n" +
                    "      \"Mon.\",\n" +
                    "      \"Tue.\",\n" +
                    "      \"Wed.\",\n" +
                    "      \"Thu.\",\n" +
                    "      \"Fri.\",\n" +
                    "      \"Sat.\"\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"decimalNumberSymbols\": {\n" +
                    "    \"negativeSign\": \"-\",\n" +
                    "    \"positiveSign\": \"+\",\n" +
                    "    \"zeroDigit\": \"0\",\n" +
                    "    \"currencySymbol\": \"$\",\n" +
                    "    \"decimalSeparator\": \".\",\n" +
                    "    \"exponentSymbol\": \"e\",\n" +
                    "    \"groupSeparator\": \",\",\n" +
                    "    \"infinitySymbol\": \"∞\",\n" +
                    "    \"monetaryDecimalSeparator\": \".\",\n" +
                    "    \"nanSymbol\": \"NaN\",\n" +
                    "    \"percentSymbol\": \"%\",\n" +
                    "    \"permillSymbol\": \"‰\"\n" +
                    "  },\n" +
                    "  \"defaultFormHandler\": \"basic\",\n" +
                    "  \"defaultYear\": 2000,\n" +
                    "  \"errorFormatter\": \"badge-error text-format-pattern @\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expressionNumberKind\": \"BIG_DECIMAL\",\n" +
                    "  \"findConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"findFunctions\": \"@\",\n" +
                    "  \"formHandlers\": \"\",\n" +
                    "  \"formatters\": \"automatic, badge-error, collection, date-format-pattern, date-time-format-pattern, default-text, expression, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formattingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, locale, plugins, style, text-node, template, url)\",\n" +
                    "  \"formattingFunctions\": \"@\",\n" +
                    "  \"formulaConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, template, url)\",\n" +
                    "  \"formulaFunctions\": \"@ExpressionFunction1\",\n" +
                    "  \"functions\": \"@ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"generalNumberFormatDigitCount\": 8,\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"numberFormatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"numberParser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"roundingMode\": \"HALF_UP\",\n" +
                    "  \"scriptingConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, error-throwing, color, expression, environment, json, locale, plugins, spreadsheet-metadata, style, text-node, template, url)\",\n" +
                    "  \"scriptingFunctions\": \"@\",\n" +
                    "  \"showFormulaEditor\": true,\n" +
                    "  \"showFormulas\": false,\n" +
                    "  \"showGridLines\": true,\n" +
                    "  \"showHeadings\": true,\n" +
                    "  \"sortComparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sortConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, locale)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"textFormatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"timeFormatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"timeParser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"twoDigitYear\": 50,\n" +
                    "  \"validationConverter\": \"collection(text, number, date-time, basic, spreadsheet-value, boolean, environment, error-throwing, expression, form-and-validation, locale, plugins, template)\",\n" +
                    "  \"validationFunctions\": \"@\",\n" +
                    "  \"validationValidators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"validators\": \"absolute-url, collection, email-address, expression, non-null, text-length, text-mask, validation-choice-list-expression\",\n" +
                    "  \"valueSeparator\": \",\"\n" +
                    "}",
                SpreadsheetMetadata.class.getSimpleName()
            )
        );

        // GET cell with function missing from "formula-functions" should have no error
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/force-recompute",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=ExpressionFunction1()\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"named-function-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-name-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"ExpressionFunction1\",\n" +
                    "                        \"text\": \"ExpressionFunction1\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"function-parameters-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-open-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"(\",\n" +
                    "                              \"text\": \"(\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"parenthesis-close-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \")\",\n" +
                    "                              \"text\": \")\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"()\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"ExpressionFunction1()\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=ExpressionFunction1()\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"call-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"callable\": {\n" +
                    "              \"type\": \"named-function-expression\",\n" +
                    "              \"value\": \"ExpressionFunction1\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"123\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 123.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadGetInvalidWindowQueryParameterBadRequest() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // load the cells that fill the viewport
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/force-recompute?window=!INVALID",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.BAD_REQUEST.setMessage("Invalid window=\"!INVALID\""),
            IllegalArgumentException.class.getSimpleName()
        );
    }

    // save cell, save metadata, save cell..............................................................................

    @Test
    public void testCellSavePostUpdateMetadataLoadCell() {
        final TestHttpServer server = this.startServer();

        final SpreadsheetMetadata initial = this.createMetadata()
            .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SPREADSHEET_ID);

        // create a new spreadsheet.
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.CREATED.status(),
                initial
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.A1
                                .setFormula(
                                    formula("1.25")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"1.25\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"1\",\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"decimal-separator-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \".\",\n" +
                    "                  \"text\": \".\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"25\",\n" +
                    "                  \"text\": \"25\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1.25\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.250\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        // update metadata with a different decimal separator.
        final SpreadsheetMetadata updated = initial.set(
            SpreadsheetMetadataPropertyName.DECIMAL_NUMBER_SYMBOLS,
            DECIMAL_NUMBER_SYMBOLS.setGroupSeparator('*')
                .setDecimalSeparator(',')
                .setGroupSeparator('\u00a0')
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/" + initial.id().get(),
            NO_HEADERS_TRANSACTION_ID,
            updated.jsonNodeMarshallContext().marshall(updated).toString(),
            this.response(
                HttpStatusCode.OK.status(),
                updated
            )
        );

        this.checkEquals(1, this.metadataStore.count());

        // reload the saved cell
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/force-recompute",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"1.25\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"1\",\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"decimal-separator-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \".\",\n" +
                    "                  \"text\": \".\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"25\",\n" +
                    "                  \"text\": \"25\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1.25\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001,250\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellClearPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // clear A1:B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2/clear",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY),
            this.response(
                HttpStatusCode.BAD_REQUEST.setMessage("Unknown link relation \"clear\"")
            )
        );
    }

    @Test
    public void testCellClearPostRange() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // clear A1:B2
        server.handleAndCheck(HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:C3/clear",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY),
            this.response(
                HttpStatusCode.BAD_REQUEST.setMessage("Unknown link relation \"clear\"")
            )
        );
    }

    // fillCell........................................................................................................

    @Test
    public void testCellFillPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // fill A1:B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:B2/fill",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY
                .setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula("=1")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellFillPostRepeatsFromRange() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // fill A1:B2 from A1
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:B2/fill?from=A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula("=1")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"B1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"A2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellFillPostThatClears() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // fill A1:B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:B2/fill",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellFillPostWithSelectionQueryParameter() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2?home=A1&width=900&height=700&selectionType=cell&selection=C3&window=",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // fill A1:B2
        server.handleAndCheck(HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:B2/fill",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula("=1")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100,\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellsFindWithNonBooleanQueryGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2?home=A1&width=900&height=700&selectionType=cell&selection=C3&window=",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/B2/find?query=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.OK.status(),
            "{\n" +
                "  \"cells\": {\n" +
                "    \"B2\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"'Hello\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"'\",\n" +
                "                  \"text\": \"'\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"Hello\",\n" +
                "                  \"text\": \"Hello\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"'Hello\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": \"Hello\"\n" +
                "        },\n" +
                "        \"value\": \"Hello\"\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Text Hello\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"B\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"2\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 2,\n" +
                "  \"rowCount\": 2\n" +
                "}"
        );
    }

    // https://github.com/mP1/walkingkooka-spreadsheet-server/issues/1370
    // find Response missing references
    @Test
    public void testCellsFindNonBooleanQueryWithLabelsAndReferencesGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2?home=A1&width=900&height=700&selectionType=cell&selection=C3&window=",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("=1")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // save cell C3
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/C3?home=A1&width=900&height=700&selectionType=cell&selection=C3&window=",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("C3")
                            .setFormula(
                                formula("=B2+1000")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=B2+1000\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"addition-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"cell-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"column-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"B\",\n" +
                    "                              \"text\": \"B\"\n" +
                    "                            }\n" +
                    "                          },\n" +
                    "                          {\n" +
                    "                            \"type\": \"row-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"2\",\n" +
                    "                              \"text\": \"2\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"B2\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"plus-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"+\",\n" +
                    "                        \"text\": \"+\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": [\n" +
                    "                          {\n" +
                    "                            \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                            \"value\": {\n" +
                    "                              \"value\": \"1000\",\n" +
                    "                              \"text\": \"1000\"\n" +
                    "                            }\n" +
                    "                          }\n" +
                    "                        ],\n" +
                    "                        \"text\": \"1000\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"B2+1000\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=B2+1000\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"add-expression\",\n" +
                    "          \"value\": [\n" +
                    "            {\n" +
                    "              \"type\": \"reference-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"spreadsheet-cell-reference\",\n" +
                    "                \"value\": \"B2\"\n" +
                    "              }\n" +
                    "            },\n" +
                    "            {\n" +
                    "              \"type\": \"value-expression\",\n" +
                    "              \"value\": {\n" +
                    "                \"type\": \"expression-number\",\n" +
                    "                \"value\": \"1000\"\n" +
                    "              }\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1001\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 1001.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        // https://github.com/mP1/walkingkooka-spreadsheet-server/issues/1371
        // refereneces should be present but are missing
        //
        // save label
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(
                        SpreadsheetSelection.labelName("Label123")
                            .setLabelMappingReference(SpreadsheetSelection.parseCell("B2"))
                    )
                )
            ),
            this.response(
                HttpStatusCode.CREATED.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"1\",\n" +
                    "                        \"text\": \"1\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label123\",\n" +
                    "      \"reference\": \"B2\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        // https://github.com/mP1/walkingkooka-spreadsheet-server/issues/1370
        // Response missing references.
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/B2/find?query=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.OK.status(),
            "{\n" +
                "  \"cells\": {\n" +
                "    \"B2\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=1\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"1\",\n" +
                "                        \"text\": \"1\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"1\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=1\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"value-expression\",\n" +
                "          \"value\": {\n" +
                "            \"type\": \"expression-number\",\n" +
                "            \"value\": \"1\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"value\": {\n" +
                "          \"type\": \"expression-number\",\n" +
                "          \"value\": \"1\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Number 001.000\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"labels\": [\n" +
                "    {\n" +
                "      \"label\": \"Label123\",\n" +
                "      \"reference\": \"B2\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"references\": {\n" +
                "    \"B2\": [\n" +
                "      {\n" +
                "        \"type\": \"spreadsheet-cell-reference\",\n" +
                "        \"value\": \"C3\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"type\": \"spreadsheet-label-name\",\n" +
                "        \"value\": \"Label123\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"columnWidths\": {\n" +
                "    \"B\": 100\n" +
                "  },\n" +
                "  \"rowHeights\": {\n" +
                "    \"2\": 50\n" +
                "  },\n" +
                "  \"columnCount\": 3,\n" +
                "  \"rowCount\": 3\n" +
                "}"
        );
    }

    @Test
    public void testCellLabelsWithReferenceGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save label
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(
                        SpreadsheetSelection.labelName("Label123")
                            .setLabelMappingReference(SpreadsheetSelection.A1)
                    )
                )
            ),
            this.response(
                HttpStatusCode.CREATED.status(),
                "{\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label123\",\n" +
                    "      \"reference\": \"A1\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/labels",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.OK.status(),
            "{\n" +
                "  \"labels\": [\n" +
                "    {\n" +
                "      \"label\": \"Label123\",\n" +
                "      \"reference\": \"A1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        );
    }

    @Test
    public void testCellsWithReferencesGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:A2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula("=A2")
                            ),
                        SpreadsheetSelection.parseCell("A2")
                            .setFormula(
                                formula("'Zebra'")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=A2\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"cell-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"column-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"A\",\n" +
                    "                        \"text\": \"A\"\n" +
                    "                      }\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                      \"type\": \"row-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"2\",\n" +
                    "                        \"text\": \"2\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"A2\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=A2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"reference-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"spreadsheet-cell-reference\",\n" +
                    "            \"value\": \"A2\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": \"Zebra'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Zebra'\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    \"A2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Zebra'\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Zebra'\",\n" +
                    "                  \"text\": \"Zebra'\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Zebra'\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Zebra'\"\n" +
                    "        },\n" +
                    "        \"value\": \"Zebra'\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Zebra'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"references\": {\n" +
                    "    \"A2\": [\n" +
                    "      {\n" +
                    "        \"type\": \"spreadsheet-cell-reference\",\n" +
                    "        \"value\": \"A1\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50,\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A2/references",
            NO_HEADERS_TRANSACTION_ID,
            "",
            HttpStatusCode.OK.status(),
            "{\n" +
                "  \"cells\": {\n" +
                "    \"A1\": {\n" +
                "      \"formula\": {\n" +
                "        \"text\": \"=A2\",\n" +
                "        \"token\": {\n" +
                "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                "          \"value\": {\n" +
                "            \"value\": [\n" +
                "              {\n" +
                "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": \"=\",\n" +
                "                  \"text\": \"=\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"cell-spreadsheet-formula-parser-token\",\n" +
                "                \"value\": {\n" +
                "                  \"value\": [\n" +
                "                    {\n" +
                "                      \"type\": \"column-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"A\",\n" +
                "                        \"text\": \"A\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    {\n" +
                "                      \"type\": \"row-spreadsheet-formula-parser-token\",\n" +
                "                      \"value\": {\n" +
                "                        \"value\": \"2\",\n" +
                "                        \"text\": \"2\"\n" +
                "                      }\n" +
                "                    }\n" +
                "                  ],\n" +
                "                  \"text\": \"A2\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"text\": \"=A2\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"expression\": {\n" +
                "          \"type\": \"reference-expression\",\n" +
                "          \"value\": {\n" +
                "            \"type\": \"spreadsheet-cell-reference\",\n" +
                "            \"value\": \"A2\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"value\": \"Zebra'\"\n" +
                "      },\n" +
                "      \"formattedValue\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"value\": \"Text Zebra'\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        );
    }

    // column...........................................................................................................

    @Test
    public void testColumnClearPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY
                .setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // clear B
        server.handleAndCheck(HttpMethod.POST,
            "/api/spreadsheet/1/column/B/clear",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testColumnClearPostWithRange() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY
                .setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // clear A:C
        server.handleAndCheck(HttpMethod.POST,
            "/api/spreadsheet/1/column/A:C/clear",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testColumnInsertAfterPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save a cell at C3
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/C3",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("C3")
                                .setFormula(
                                    formula("=123")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=123\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"123\",\n" +
                    "                        \"text\": \"123\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"123\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"123\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 123.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        // save a cell at D4
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/D4",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("D4")
                                .setFormula(formula("=456")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"D4\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 4,\n" +
                    "  \"rowCount\": 4\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/column/C/insert-after?count=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"E4\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"D4\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100,\n" +
                    "    \"E\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 5,\n" +
                    "  \"rowCount\": 4\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testColumnInsertBeforePost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save a cell at C3
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/C3",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("C3")
                                .setFormula(
                                    formula("=123")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=123\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"123\",\n" +
                    "                        \"text\": \"123\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"123\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"123\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 123.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        // save a cell at D4
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/D4",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("D4")
                                .setFormula(
                                    formula("=456")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"D4\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 4,\n" +
                    "  \"rowCount\": 4\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/column/D/insert-before?count=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"E4\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"D4\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100,\n" +
                    "    \"E\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 5,\n" +
                    "  \"rowCount\": 4\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testColumnDeleteShiftsCell() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/C3",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("C3")
                                .setFormula(
                                    formula("1.25")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"1.25\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"1\",\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"decimal-separator-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \".\",\n" +
                    "                  \"text\": \".\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"25\",\n" +
                    "                  \"text\": \"25\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1.25\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.250\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/column/B",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"1.25\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"1\",\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"decimal-separator-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \".\",\n" +
                    "                  \"text\": \".\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"25\",\n" +
                    "                  \"text\": \"25\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1.25\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.250\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"C3\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100,\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testColumnPatch() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/column/A",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setColumns(
                        Sets.of(
                            SpreadsheetSelection.parseColumn("A")
                                .column()
                                .setHidden(true)
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"columns\": {\n" +
                    "    \"A\": {\n" +
                    "      \"hidden\": true\n" +
                    "    }\n" +
                    "  }\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testColumnPatchWithWindow() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/column/A",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setColumns(
                        Sets.of(
                            SpreadsheetSelection.parseColumn("A")
                                .column()
                                .setHidden(true)
                        )
                    ).setWindow(
                        SpreadsheetViewportWindows.parse("A1:B2")
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"columns\": {\n" +
                    "    \"A\": {\n" +
                    "      \"hidden\": true\n" +
                    "    }\n" +
                    "  }\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testColumnPatchAfterCellSavePost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell A1
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula("'Hello A1")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello A1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello A1\",\n" +
                    "                  \"text\": \"Hello A1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello A1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello A1\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello A1\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello A1\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/column/A?window=A1:B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setColumns(
                    Sets.of(
                        SpreadsheetSelection.parseColumn("A")
                            .column()
                            .setHidden(true)
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"columns\": {\n" +
                    "    \"A\": {\n" +
                    "      \"hidden\": true\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/column/A?window=A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setColumns(
                    Sets.of(
                        SpreadsheetSelection.parseColumn("A")
                            .column()
                            .setHidden(false)
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"columns\": {\n" +
                    "    \"A\": {\n" +
                    "      \"hidden\": false\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"window\": \"A1\"\n" +
                    "}",
                DELTA
            )
        );
    }

    // comparators......................................................................................................

    @Test
    public void testComparatorsGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/comparator",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date date\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/date-time date-time\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-month day-of-month\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-week day-of-week\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-am-pm hour-of-am-pm\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/hour-of-day hour-of-day\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/minute-of-hour minute-of-hour\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/month-of-year month-of-year\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/nano-of-second nano-of-second\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/number number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/seconds-of-minute seconds-of-minute\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/text-case-insensitive text-case-insensitive\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/time time\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/year year\"\n" +
                    "]",
                SpreadsheetComparatorInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testComparatorsWithNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/comparator/day-of-month",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetComparator/day-of-month day-of-month\"",
                SpreadsheetComparatorInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testComparatorsWithNameUnknownGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/comparator/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetComparatorInfo.class.getSimpleName()
            )
        );
    }

    // converters.......................................................................................................

    @Test
    public void testConvertersGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/converter",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/basic basic\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean boolean\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/boolean-to-text boolean-to-text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection-to-list collection-to-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/color color\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-color color-to-color\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/color-to-number color-to-number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time date-time\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/date-time-symbols date-time-symbols\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/decimal-number-symbols decimal-number-symbols\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/environment environment\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-throwing error-throwing\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-error error-to-error\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-number error-to-number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/expression expression\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/form-and-validation form-and-validation\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/format-pattern-to-string format-pattern-to-string\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-formatter-selector has-formatter-selector\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-host-address has-host-address\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-parser-selector has-parser-selector\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-spreadsheet-selection has-spreadsheet-selection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-style has-style\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-text-node has-text-node\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/has-validator-selector has-validator-selector\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/json json\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/jsonTo jsonTo\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale locale\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/locale-to-text locale-to-text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/null-to-number null-to-number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/number number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-color number-to-color\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-number number-to-number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/number-to-text number-to-text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/plugins plugins\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-cell-set spreadsheet-cell-set\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-metadata spreadsheet-metadata\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-spreadsheet-selection spreadsheet-selection-to-spreadsheet-selection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-selection-to-text spreadsheet-selection-to-text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-value spreadsheet-value\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/style style\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/system system\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/template template\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-node text-node\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-boolean-list text-to-boolean-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-color text-to-color\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-csv-string-list text-to-csv-string-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-list text-to-date-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-date-time-list text-to-date-time-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-email-address text-to-email-address\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-environment-value-name text-to-environment-value-name\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-error text-to-error\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-expression text-to-expression\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-form-name text-to-form-name\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-has-host-address text-to-has-host-address\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-json text-to-json\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-locale text-to-locale\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-number-list text-to-number-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-object text-to-object\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-color-name text-to-spreadsheet-color-name\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-formatter-selector text-to-spreadsheet-formatter-selector\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-id text-to-spreadsheet-id\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata text-to-spreadsheet-metadata\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-color text-to-spreadsheet-metadata-color\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-metadata-property-name text-to-spreadsheet-metadata-property-name\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-name text-to-spreadsheet-name\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-selection text-to-spreadsheet-selection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-spreadsheet-text text-to-spreadsheet-text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-string-list text-to-string-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-template-value-name text-to-template-value-name\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text text-to-text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-node text-to-text-node\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style text-to-text-style\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-text-style-property-name text-to-text-style-property-name\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-time-list text-to-time-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-url text-to-url\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validation-error text-to-validation-error\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-validator-selector text-to-validator-selector\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text-to-value-type text-to-value-type\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-boolean to-boolean\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-json to-json\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-number to-number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-styleable to-styleable\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/to-validation-error-list to-validation-error-list\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/url url\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-hyperlink url-to-hyperlink\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/url-to-image url-to-image\"\n" +
                    "]",
                ConverterInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testConvertersWithNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/converter/text",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-spreadsheet/Converter/text text\"",
                ConverterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testConvertersWithNameUnknownGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/converter/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                ConverterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testConverterVerifyPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/metadata/formulaConverter/verify",
            NO_HEADERS_TRANSACTION_ID,
            JSON_NODE_MARSHALL_CONTEXT.marshall(
                METADATA_EN_AU.getOrFail(
                    SpreadsheetMetadataPropertyName.FORMULA_CONVERTER
                )
            ).toString(),
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(
                        SpreadsheetServerMediaTypes.CONTENT_TYPE
                    ).setHeader(
                        HateosResourceMappings.X_CONTENT_TYPE_NAME,
                        Lists.of(
                            MissingConverterSet.class.getSimpleName()
                        )
                    ).setBodyText("[]")
                    .setContentLength()
            )
        );
    }

    // DateTimeSymbols..................................................................................................

    @Test
    public void testDateTimeSymbolsByLocaleGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/dateTimeSymbols/EN-AU",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"localeTag\": \"en-AU\",\n" +
                    "  \"text\": \"English (Australia)\",\n" +
                    "  \"dateTimeSymbols\": {\n" +
                    "    \"ampms\": [\n" +
                    "      \"am\",\n" +
                    "      \"pm\"\n" +
                    "    ],\n" +
                    "    \"monthNames\": [\n" +
                    "      \"January\",\n" +
                    "      \"February\",\n" +
                    "      \"March\",\n" +
                    "      \"April\",\n" +
                    "      \"May\",\n" +
                    "      \"June\",\n" +
                    "      \"July\",\n" +
                    "      \"August\",\n" +
                    "      \"September\",\n" +
                    "      \"October\",\n" +
                    "      \"November\",\n" +
                    "      \"December\"\n" +
                    "    ],\n" +
                    "    \"monthNameAbbreviations\": [\n" +
                    "      \"Jan.\",\n" +
                    "      \"Feb.\",\n" +
                    "      \"Mar.\",\n" +
                    "      \"Apr.\",\n" +
                    "      \"May\",\n" +
                    "      \"Jun.\",\n" +
                    "      \"Jul.\",\n" +
                    "      \"Aug.\",\n" +
                    "      \"Sep.\",\n" +
                    "      \"Oct.\",\n" +
                    "      \"Nov.\",\n" +
                    "      \"Dec.\"\n" +
                    "    ],\n" +
                    "    \"weekDayNames\": [\n" +
                    "      \"Sunday\",\n" +
                    "      \"Monday\",\n" +
                    "      \"Tuesday\",\n" +
                    "      \"Wednesday\",\n" +
                    "      \"Thursday\",\n" +
                    "      \"Friday\",\n" +
                    "      \"Saturday\"\n" +
                    "    ],\n" +
                    "    \"weekDayNameAbbreviations\": [\n" +
                    "      \"Sun.\",\n" +
                    "      \"Mon.\",\n" +
                    "      \"Tue.\",\n" +
                    "      \"Wed.\",\n" +
                    "      \"Thu.\",\n" +
                    "      \"Fri.\",\n" +
                    "      \"Sat.\"\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}",
                DateTimeSymbolsHateosResource.class.getSimpleName()
            )
        );
    }

    @Test
    public void testDateTimeSymbolsFindByLocaleStartsWith() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/dateTimeSymbols/*/localeStartsWith/English?offset=7&count=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en-AU\",\n" +
                    "    \"text\": \"English (Australia)\",\n" +
                    "    \"dateTimeSymbols\": {\n" +
                    "      \"ampms\": [\n" +
                    "        \"am\",\n" +
                    "        \"pm\"\n" +
                    "      ],\n" +
                    "      \"monthNames\": [\n" +
                    "        \"January\",\n" +
                    "        \"February\",\n" +
                    "        \"March\",\n" +
                    "        \"April\",\n" +
                    "        \"May\",\n" +
                    "        \"June\",\n" +
                    "        \"July\",\n" +
                    "        \"August\",\n" +
                    "        \"September\",\n" +
                    "        \"October\",\n" +
                    "        \"November\",\n" +
                    "        \"December\"\n" +
                    "      ],\n" +
                    "      \"monthNameAbbreviations\": [\n" +
                    "        \"Jan.\",\n" +
                    "        \"Feb.\",\n" +
                    "        \"Mar.\",\n" +
                    "        \"Apr.\",\n" +
                    "        \"May\",\n" +
                    "        \"Jun.\",\n" +
                    "        \"Jul.\",\n" +
                    "        \"Aug.\",\n" +
                    "        \"Sep.\",\n" +
                    "        \"Oct.\",\n" +
                    "        \"Nov.\",\n" +
                    "        \"Dec.\"\n" +
                    "      ],\n" +
                    "      \"weekDayNames\": [\n" +
                    "        \"Sunday\",\n" +
                    "        \"Monday\",\n" +
                    "        \"Tuesday\",\n" +
                    "        \"Wednesday\",\n" +
                    "        \"Thursday\",\n" +
                    "        \"Friday\",\n" +
                    "        \"Saturday\"\n" +
                    "      ],\n" +
                    "      \"weekDayNameAbbreviations\": [\n" +
                    "        \"Sun.\",\n" +
                    "        \"Mon.\",\n" +
                    "        \"Tue.\",\n" +
                    "        \"Wed.\",\n" +
                    "        \"Thu.\",\n" +
                    "        \"Fri.\",\n" +
                    "        \"Sat.\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  }\n" +
                    "]",
                DateTimeSymbolsHateosResourceSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testDateTimeSymbolsFindByLocaleStartsWithUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/dateTimeSymbols/*/localeStartsWith/ZZZ",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[]",
                DateTimeSymbolsHateosResourceSet.class.getSimpleName()
            )
        );
    }

    // DateTimeSymbols..................................................................................................

    @Test
    public void testDecimalNumberSymbolsByLocaleGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/decimalNumberSymbols/EN-AU",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"localeTag\": \"en-AU\",\n" +
                    "  \"text\": \"English (Australia)\",\n" +
                    "  \"decimalNumberSymbols\": {\n" +
                    "    \"negativeSign\": \"-\",\n" +
                    "    \"positiveSign\": \"+\",\n" +
                    "    \"zeroDigit\": \"0\",\n" +
                    "    \"currencySymbol\": \"$\",\n" +
                    "    \"decimalSeparator\": \".\",\n" +
                    "    \"exponentSymbol\": \"e\",\n" +
                    "    \"groupSeparator\": \",\",\n" +
                    "    \"infinitySymbol\": \"∞\",\n" +
                    "    \"monetaryDecimalSeparator\": \".\",\n" +
                    "    \"nanSymbol\": \"NaN\",\n" +
                    "    \"percentSymbol\": \"%\",\n" +
                    "    \"permillSymbol\": \"‰\"\n" +
                    "  }\n" +
                    "}",
                DecimalNumberSymbolsHateosResource.class.getSimpleName()
            )
        );
    }

    @Test
    public void testDecimalNumberSymbolsFindByLocaleStartsWith() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/decimalNumberSymbols/*/localeStartsWith/English?offset=7&count=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en-AU\",\n" +
                    "    \"text\": \"English (Australia)\",\n" +
                    "    \"decimalNumberSymbols\": {\n" +
                    "      \"negativeSign\": \"-\",\n" +
                    "      \"positiveSign\": \"+\",\n" +
                    "      \"zeroDigit\": \"0\",\n" +
                    "      \"currencySymbol\": \"$\",\n" +
                    "      \"decimalSeparator\": \".\",\n" +
                    "      \"exponentSymbol\": \"e\",\n" +
                    "      \"groupSeparator\": \",\",\n" +
                    "      \"infinitySymbol\": \"∞\",\n" +
                    "      \"monetaryDecimalSeparator\": \".\",\n" +
                    "      \"nanSymbol\": \"NaN\",\n" +
                    "      \"percentSymbol\": \"%\",\n" +
                    "      \"permillSymbol\": \"‰\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]",
                DecimalNumberSymbolsHateosResourceSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testDecimalNumberSymbolsFindByLocaleStartsWithUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/decimalNumberSymbols/*/localeStartsWith/ZZZ",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[]",
                DecimalNumberSymbolsHateosResourceSet.class.getSimpleName()
            )
        );
    }
    
    // exporters........................................................................................................

    @Test
    public void testExportersGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/exporter",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetExporter/collection collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetExporter/empty empty\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetExporter/json json\"\n" +
                    "]",
                SpreadsheetExporterInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testExportersWithNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/exporter/json",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetExporter/json json\"",
                SpreadsheetExporterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testExportersWithNameUnknownGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/exporter/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetExporterInfo.class.getSimpleName()
            )
        );
    }

    // expression-function.............................................................................................

    @Test
    public void testExpressionFunctionGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/function",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"@https://example.com/expression-function-1 ExpressionFunction1\",\n" +
                    "  \"@https://example.com/expression-function-2 ExpressionFunction2\"\n" +
                    "]",
                ExpressionFunctionInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testExpressionFunctionByNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/function/ExpressionFunction1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"@https://example.com/expression-function-1 ExpressionFunction1\"",
                ExpressionFunctionInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testExpressionFunctionByNameNotFoundGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/function/UnknownFunctionName",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                ExpressionFunctionInfo.class.getSimpleName()
            )
        );
    }

    // formatters.......................................................................................................

    @Test
    public void testFormattersGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/formatter",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/automatic automatic\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/badge-error badge-error\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/collection collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-format-pattern date-format-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-time-format-pattern date-time-format-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/default-text default-text\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/expression expression\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/general general\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/number-format-pattern number-format-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/spreadsheet-pattern-collection spreadsheet-pattern-collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/text-format-pattern text-format-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/time-format-pattern time-format-pattern\"\n" +
                    "]",
                SpreadsheetFormatterInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterByNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/formatter/date-format-pattern",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-format-pattern date-format-pattern\"",
                SpreadsheetFormatterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterByNameNotFoundGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/formatter/unknown-formatter-404",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetFormatterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterCellEditGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/formatter-edit/date-format-pattern%20yyyy/mm/ddd",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"selector\": \"date-format-pattern yyyy/mm/ddd\",\n" +
                    "  \"tokens\": [\n" +
                    "    {\n" +
                    "      \"label\": \"yyyy\",\n" +
                    "      \"text\": \"yyyy\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"yy\",\n" +
                    "          \"text\": \"yy\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mm\",\n" +
                    "      \"text\": \"mm\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"m\",\n" +
                    "          \"text\": \"m\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmm\",\n" +
                    "          \"text\": \"mmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmm\",\n" +
                    "          \"text\": \"mmmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmmm\",\n" +
                    "          \"text\": \"mmmmm\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"ddd\",\n" +
                    "      \"text\": \"ddd\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"d\",\n" +
                    "          \"text\": \"d\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"dd\",\n" +
                    "          \"text\": \"dd\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"dddd\",\n" +
                    "          \"text\": \"dddd\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"next\": {\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"m\",\n" +
                    "        \"text\": \"m\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mm\",\n" +
                    "        \"text\": \"mm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmm\",\n" +
                    "        \"text\": \"mmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmm\",\n" +
                    "        \"text\": \"mmmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmmm\",\n" +
                    "        \"text\": \"mmmmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"yy\",\n" +
                    "        \"text\": \"yy\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"yyyy\",\n" +
                    "        \"text\": \"yyyy\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"samples\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Short\",\n" +
                    "      \"selector\": \"date-format-pattern d/m/yy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31/12/99\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Medium\",\n" +
                    "      \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 Dec. 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Long\",\n" +
                    "      \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Full\",\n" +
                    "      \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Friday, 31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Sample\",\n" +
                    "      \"selector\": \"date-format-pattern yyyy/mm/ddd\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"1999/12/Fri.\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}",
                SpreadsheetFormatterSelectorEdit.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterMetadataEditGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/metadata/dateTimeFormatter/edit/date-format-pattern%20yyyy/mm/ddd",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"selector\": \"date-format-pattern yyyy/mm/ddd\",\n" +
                    "  \"tokens\": [\n" +
                    "    {\n" +
                    "      \"label\": \"yyyy\",\n" +
                    "      \"text\": \"yyyy\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"yy\",\n" +
                    "          \"text\": \"yy\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mm\",\n" +
                    "      \"text\": \"mm\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"m\",\n" +
                    "          \"text\": \"m\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmm\",\n" +
                    "          \"text\": \"mmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmm\",\n" +
                    "          \"text\": \"mmmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmmm\",\n" +
                    "          \"text\": \"mmmmm\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"ddd\",\n" +
                    "      \"text\": \"ddd\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"d\",\n" +
                    "          \"text\": \"d\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"dd\",\n" +
                    "          \"text\": \"dd\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"dddd\",\n" +
                    "          \"text\": \"dddd\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"next\": {\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"m\",\n" +
                    "        \"text\": \"m\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mm\",\n" +
                    "        \"text\": \"mm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmm\",\n" +
                    "        \"text\": \"mmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmm\",\n" +
                    "        \"text\": \"mmmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmmm\",\n" +
                    "        \"text\": \"mmmmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"yy\",\n" +
                    "        \"text\": \"yy\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"yyyy\",\n" +
                    "        \"text\": \"yyyy\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"samples\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Short\",\n" +
                    "      \"selector\": \"date-format-pattern d/m/yy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31/12/99\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Medium\",\n" +
                    "      \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 Dec. 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Long\",\n" +
                    "      \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Full\",\n" +
                    "      \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Friday, 31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Sample\",\n" +
                    "      \"selector\": \"date-format-pattern yyyy/mm/ddd\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"1999/12/Fri.\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}",
                SpreadsheetFormatterSelectorEdit.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterCellMenuGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/formatter-menu",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"label\": \"Short\",\n" +
                    "    \"selector\": \"date-format-pattern d/m/yy\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Medium\",\n" +
                    "    \"selector\": \"date-format-pattern d mmm yyyy\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Long\",\n" +
                    "    \"selector\": \"date-format-pattern d mmmm yyyy\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Full\",\n" +
                    "    \"selector\": \"date-format-pattern dddd, d mmmm yyyy\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Short\",\n" +
                    "    \"selector\": \"date-time-format-pattern d/m/yy, h:mm AM/PM\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Medium\",\n" +
                    "    \"selector\": \"date-time-format-pattern d mmm yyyy, h:mm:ss AM/PM\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Long\",\n" +
                    "    \"selector\": \"date-time-format-pattern d mmmm yyyy \\\\a\\\\t h:mm:ss AM/PM\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Full\",\n" +
                    "    \"selector\": \"date-time-format-pattern dddd, d mmmm yyyy \\\\a\\\\t h:mm:ss AM/PM\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Default\",\n" +
                    "    \"selector\": \"default-text @\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"General\",\n" +
                    "    \"selector\": \"general\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Number\",\n" +
                    "    \"selector\": \"number-format-pattern #,##0.###\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Integer\",\n" +
                    "    \"selector\": \"number-format-pattern #,##0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Percent\",\n" +
                    "    \"selector\": \"number-format-pattern #,##0%\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Currency\",\n" +
                    "    \"selector\": \"number-format-pattern $#,##0.00\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Default\",\n" +
                    "    \"selector\": \"text-format-pattern @\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Short\",\n" +
                    "    \"selector\": \"time-format-pattern h:mm AM/PM\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Long\",\n" +
                    "    \"selector\": \"time-format-pattern h:mm:ss AM/PM\"\n" +
                    "  }\n" +
                    "]",
                SpreadsheetFormatterMenuList.class.getSimpleName()
            )
        );
    }

    // formHandlers.....................................................................................................

    @Test
    public void testFormHandlersGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/formHandler",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/FormHandler/basic basic\"\n" +
                    "]",
                FormHandlerInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormHandlersWithNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/formHandler/basic",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-validation/FormHandler/basic basic\"",
                FormHandlerInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormHandlersWithNameUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/formHandler/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                FormHandlerInfo.class.getSimpleName()
            )
        );
    }

    // form.............................................................................................................

    @Test
    public void testFormLoadsWithOffsetAndCount() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final Form<SpreadsheetExpressionReference> form1 = SpreadsheetForms.form(FormName.with("Form1"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(form1)
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(form1)
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        final Form<SpreadsheetExpressionReference> form2 = SpreadsheetForms.form(FormName.with("Form2"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel2")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(form2)
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(form2)
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        final Form<SpreadsheetExpressionReference> form3 = SpreadsheetForms.form(FormName.with("Form3"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel3")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form3",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(form3)
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(form3)
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/form/*?offset=1&count=2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form2,
                            form3
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormSave() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final Form<SpreadsheetExpressionReference> form = SpreadsheetForms.form(FormName.with("Form1"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormUpdate() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final Form<SpreadsheetExpressionReference> form = SpreadsheetForms.form(FormName.with("Form1"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormLoad() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final Form<SpreadsheetExpressionReference> form = SpreadsheetForms.form(FormName.with("Form123"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormDelete() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final Form<SpreadsheetExpressionReference> form = SpreadsheetForms.form(FormName.with("Form123"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormPrepare() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final Form<SpreadsheetExpressionReference> form = SpreadsheetForms.form(FormName.with("Form123"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/A1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormSubmit() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final Form<SpreadsheetExpressionReference> form = SpreadsheetForms.form(FormName.with("Form123"))
            .setFields(
                Lists.of(
                    SpreadsheetForms.field(SpreadsheetSelection.A1.toExpressionReference())
                        .setLabel("FieldLabel1")
                )
            );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setForms(
                        Sets.of(
                            form
                        )
                    ).setDeletedCells(
                        Sets.of(SpreadsheetSelection.A1)
                    ).setColumnWidths(
                        Maps.of(
                            SpreadsheetSelection.parseColumn("A"),
                            100.0
                        )
                    ).setRowHeights(
                        Maps.of(
                            SpreadsheetSelection.parseRow("1"),
                            50.0
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1/form/Form123",
            NO_HEADERS_TRANSACTION_ID,
            this.toJson(
                SpreadsheetDelta.EMPTY.setForms(
                    Sets.of(
                        form
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {},\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text \"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    // importers........................................................................................................

    @Test
    public void testImportersGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/importer",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/collection collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/empty empty\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/json json\"\n" +
                    "]",
                SpreadsheetImporterInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testImportersWithNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/importer/json",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetImporter/json json\"",
                SpreadsheetImporterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testImportersWithNameUnknownGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/importer/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetImporterInfo.class.getSimpleName()
            )
        );
    }

    // Locale...........................................................................................................

    @Test
    public void testLocaleByLanguageTagGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/locale/EN-AU",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"localeTag\": \"en-AU\",\n" +
                    "  \"text\": \"English (Australia)\"\n" +
                    "}",
                LocaleHateosResource.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLocaleListGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/locale/*?offset=130&count=3",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"localeTag\": \"el-GR\",\n" +
                    "    \"text\": \"Greek (Greece)\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en\",\n" +
                    "    \"text\": \"English\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"localeTag\": \"en-001\",\n" +
                    "    \"text\": \"English (World)\"\n" +
                    "  }\n" +
                    "]",
                LocaleHateosResourceSet.class.getSimpleName()
            )
        );
    }

    // label............................................................................................................

    @Test
    public void testLabelSavePost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingReference(reference);

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label/",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(mapping)
                )
            ),
            this.response(
                HttpStatusCode.CREATED.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLabelSavePostAndLabelGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingReference(reference);

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label/",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(mapping)
                )
            ),
            this.response(
                HttpStatusCode.CREATED.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/label/" + label,
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLabelWithOffsetAndCountGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetLabelMapping mapping1 = SpreadsheetSelection.labelName("Label111")
            .setLabelMappingReference(SpreadsheetSelection.A1);
        final SpreadsheetLabelMapping mapping2 = SpreadsheetSelection.labelName("Label222")
            .setLabelMappingReference(SpreadsheetSelection.parseCell("B2"));
        final SpreadsheetLabelMapping mapping3 = SpreadsheetSelection.labelName("Label333")
            .setLabelMappingReference(SpreadsheetSelection.parseCell("C3"));
        final SpreadsheetLabelMapping mapping4 = SpreadsheetSelection.labelName("Label444")
            .setLabelMappingReference(SpreadsheetSelection.parseCell("D4"));

        for (final SpreadsheetLabelMapping mapping : Lists.of(mapping1, mapping2, mapping3, mapping4)) {
            server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    )
                ),
                this.response(
                    HttpStatusCode.CREATED.status(),
                    this.toJson(
                        SpreadsheetDelta.EMPTY.setLabels(
                            Sets.of(mapping)
                        ).setColumnCount(
                            OptionalInt.of(0)
                        ).setRowCount(
                            OptionalInt.of(0)
                        )
                    ),
                    SpreadsheetDelta.class.getSimpleName()
                )
            );
        }

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/label/*?offset=1&count=2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(
                            mapping2,
                            mapping3
                        )
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLabelLoadsWithOffsetAndCountWithCellReferences() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetLabelMapping mapping1 = SpreadsheetSelection.labelName("Label111")
            .setLabelMappingReference(SpreadsheetSelection.A1);
        final SpreadsheetLabelMapping mapping2 = SpreadsheetSelection.labelName("Label222")
            .setLabelMappingReference(SpreadsheetSelection.parseCell("B2"));
        final SpreadsheetLabelMapping mapping3 = SpreadsheetSelection.labelName("Label333")
            .setLabelMappingReference(SpreadsheetSelection.parseCell("C3"));
        final SpreadsheetLabelMapping mapping4 = SpreadsheetSelection.labelName("Label444")
            .setLabelMappingReference(SpreadsheetSelection.parseCell("D4"));

        for (final SpreadsheetLabelMapping mapping : Lists.of(mapping1, mapping2, mapping3, mapping4)) {
            server.handleAndCheck(
                HttpMethod.POST,
                "/api/spreadsheet/1/label/",
                NO_HEADERS_TRANSACTION_ID,
                toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    )
                ),
                this.response(
                    HttpStatusCode.CREATED.status(),
                    this.toJson(
                        SpreadsheetDelta.EMPTY.setLabels(
                            Sets.of(mapping)
                        ).setColumnCount(
                            OptionalInt.of(0)
                        ).setRowCount(
                            OptionalInt.of(0)
                        )
                    ),
                    SpreadsheetDelta.class.getSimpleName()
                )
            );
        }

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setCells(
                        Sets.of(
                            SpreadsheetSelection.parseCell("B2")
                                .setFormula(
                                    formula("=100")
                                )
                        )
                    )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=100\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"100\",\n" +
                    "                        \"text\": \"100\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"100\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=100\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"100\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"100\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 100.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label222\",\n" +
                    "      \"reference\": \"B2\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"references\": {\n" +
                    "    \"B2\": [\n" +
                    "      {\n" +
                    "        \"type\": \"spreadsheet-label-name\",\n" +
                    "        \"value\": \"Label222\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/label/*?offset=1&count=2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=100\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"100\",\n" +
                    "                        \"text\": \"100\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"100\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=100\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"100\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"100\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 100.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label222\",\n" +
                    "      \"reference\": \"B2\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Label333\",\n" +
                    "      \"reference\": \"C3\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLabelDelete() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingReference(reference);

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label/",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(mapping)
                )
            ),
            this.response(
                HttpStatusCode.CREATED.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/label/" + label,
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setDeletedLabels(
                        Sets.of(label)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLabelFindByName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingReference(reference);

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/label/",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setLabels(
                    Sets.of(mapping)
                )
            ),
            this.response(
                HttpStatusCode.CREATED.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    ).setColumnCount(
                        OptionalInt.of(0)
                    ).setRowCount(
                        OptionalInt.of(0)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/label/*/findByName/Label",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetDelta.EMPTY.setLabels(
                        Sets.of(mapping)
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    // parsers......................................................................................................

    @Test
    public void testParsersGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/parser",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date-parse-pattern date-parse-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date-time-parse-pattern date-time-parse-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/number-parse-pattern number-parse-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/time-parse-pattern time-parse-pattern\"\n" +
                    "]",
                SpreadsheetParserInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testParsersBySpreadsheetParserNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/parser/date-parse-pattern",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetParser/date-parse-pattern date-parse-pattern\"",
                SpreadsheetParserInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testParsersBySpreadsheetParserNameUnknownGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/parser/unknown-parser-204",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetParserInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testParserEditGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/parser/*/edit/date-parse-pattern%20yyyy/mm/ddd",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"selector\": \"date-parse-pattern yyyy/mm/ddd\",\n" +
                    "  \"message\": \"\",\n" +
                    "  \"tokens\": [\n" +
                    "    {\n" +
                    "      \"label\": \"yyyy\",\n" +
                    "      \"text\": \"yyyy\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"yy\",\n" +
                    "          \"text\": \"yy\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mm\",\n" +
                    "      \"text\": \"mm\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"m\",\n" +
                    "          \"text\": \"m\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmm\",\n" +
                    "          \"text\": \"mmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmm\",\n" +
                    "          \"text\": \"mmmm\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"mmmmm\",\n" +
                    "          \"text\": \"mmmmm\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"/\",\n" +
                    "      \"text\": \"/\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"ddd\",\n" +
                    "      \"text\": \"ddd\",\n" +
                    "      \"alternatives\": [\n" +
                    "        {\n" +
                    "          \"label\": \"d\",\n" +
                    "          \"text\": \"d\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"dd\",\n" +
                    "          \"text\": \"dd\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"label\": \"dddd\",\n" +
                    "          \"text\": \"dddd\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"next\": {\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"m\",\n" +
                    "        \"text\": \"m\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mm\",\n" +
                    "        \"text\": \"mm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmm\",\n" +
                    "        \"text\": \"mmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmm\",\n" +
                    "        \"text\": \"mmmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"mmmmm\",\n" +
                    "        \"text\": \"mmmmm\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"yy\",\n" +
                    "        \"text\": \"yy\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"yyyy\",\n" +
                    "        \"text\": \"yyyy\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"samples\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Short\",\n" +
                    "      \"selector\": \"date-format-pattern d/m/yy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31/12/99\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Medium\",\n" +
                    "      \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 Dec. 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Long\",\n" +
                    "      \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Full\",\n" +
                    "      \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Friday, 31 December 1999\"\n" +
                    "      }\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"Sample\",\n" +
                    "      \"selector\": \"date-format-pattern yyyy/mm/ddd\",\n" +
                    "      \"value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"1999/12/Fri.\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}",
                SpreadsheetParserSelectorEdit.class.getSimpleName()
            )
        );
    }

    // row...........................................................................................................

    @Test
    public void testRowClearPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // clear B
        server.handleAndCheck(HttpMethod.POST,
            "/api/spreadsheet/1/row/2/clear",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testRowClearPostRange() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("B2")
                            .setFormula(
                                formula("'Hello")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"B2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello\",\n" +
                    "                  \"text\": \"Hello\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 2,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                DELTA
            )
        );

        // clear A:C
        server.handleAndCheck(HttpMethod.POST,
            "/api/spreadsheet/1/row/1:3/clear",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );

        // GET deleted cell should return nothing.
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell/B2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"deletedCells\": \"B2\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"B\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testRowInsertAfterPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save a cell at C3
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/C3",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("C3")
                            .setFormula(
                                formula("=123")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=123\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"123\",\n" +
                    "                        \"text\": \"123\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"123\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"123\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 123.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        // save a cell at D4
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/D4",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("D4")
                            .setFormula(
                                formula("=456")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"D4\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 4,\n" +
                    "  \"rowCount\": 4\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/row/3/insert-after?count=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"D5\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"D4\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50,\n" +
                    "    \"5\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 4,\n" +
                    "  \"rowCount\": 5\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testRowInsertBeforePost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save a cell at C3
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/C3",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("C3")
                            .setFormula(
                                formula("=123")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=123\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"123\",\n" +
                    "                        \"text\": \"123\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"123\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"123\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"123\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 123.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        // save a cell at D4
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/D4",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("D4")
                            .setFormula(
                                formula("=456")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"D4\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 4,\n" +
                    "  \"rowCount\": 4\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/row/4/insert-before?count=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"D5\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"=456\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"expression-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"equals-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"=\",\n" +
                    "                  \"text\": \"=\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": [\n" +
                    "                    {\n" +
                    "                      \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                      \"value\": {\n" +
                    "                        \"value\": \"456\",\n" +
                    "                        \"text\": \"456\"\n" +
                    "                      }\n" +
                    "                    }\n" +
                    "                  ],\n" +
                    "                  \"text\": \"456\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"=456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"456\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"456\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 456.000\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"D4\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"D\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"4\": 50,\n" +
                    "    \"5\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 4,\n" +
                    "  \"rowCount\": 5\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testRowDeleteShiftsCellPost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/C3",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.parseCell("C3")
                            .setFormula(
                                formula("1.25")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C3\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"1.25\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"1\",\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"decimal-separator-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \".\",\n" +
                    "                  \"text\": \".\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"25\",\n" +
                    "                  \"text\": \"25\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1.25\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.250\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 3\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.DELETE,
            "/api/spreadsheet/1/row/2",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"C2\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"1.25\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"number-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"1\",\n" +
                    "                  \"text\": \"1\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"decimal-separator-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \".\",\n" +
                    "                  \"text\": \".\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"digits-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"25\",\n" +
                    "                  \"text\": \"25\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": {\n" +
                    "            \"type\": \"expression-number\",\n" +
                    "            \"value\": \"1.25\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"value\": {\n" +
                    "          \"type\": \"expression-number\",\n" +
                    "          \"value\": \"1.25\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 001.250\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"deletedCells\": \"C3\",\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"C\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"2\": 50,\n" +
                    "    \"3\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 3,\n" +
                    "  \"rowCount\": 2\n" +
                    "}",
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testRowPatch() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/row/1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setRows(
                    Sets.of(
                        SpreadsheetSelection.parseRow("1")
                            .row()
                            .setHidden(true)
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"rows\": {\n" +
                    "    \"1\": {\n" +
                    "      \"hidden\": true\n" +
                    "    }\n" +
                    "  }\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testRowPatchWithWindow() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/row/1?window=A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setRows(
                    Sets.of(
                        SpreadsheetSelection.parseRow("1")
                            .row()
                            .setHidden(true)
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"rows\": {\n" +
                    "    \"1\": {\n" +
                    "      \"hidden\": true\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"window\": \"A1\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testRowPatchAfterCellSavePost() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell A1
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setCells(
                    Sets.of(
                        SpreadsheetSelection.A1
                            .setFormula(
                                formula("'Hello A1")
                            )
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cells\": {\n" +
                    "    \"A1\": {\n" +
                    "      \"formula\": {\n" +
                    "        \"text\": \"'Hello A1\",\n" +
                    "        \"token\": {\n" +
                    "          \"type\": \"text-spreadsheet-formula-parser-token\",\n" +
                    "          \"value\": {\n" +
                    "            \"value\": [\n" +
                    "              {\n" +
                    "                \"type\": \"apostrophe-symbol-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"'\",\n" +
                    "                  \"text\": \"'\"\n" +
                    "                }\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\": \"text-literal-spreadsheet-formula-parser-token\",\n" +
                    "                \"value\": {\n" +
                    "                  \"value\": \"Hello A1\",\n" +
                    "                  \"text\": \"Hello A1\"\n" +
                    "                }\n" +
                    "              }\n" +
                    "            ],\n" +
                    "            \"text\": \"'Hello A1\"\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"expression\": {\n" +
                    "          \"type\": \"value-expression\",\n" +
                    "          \"value\": \"Hello A1\"\n" +
                    "        },\n" +
                    "        \"value\": \"Hello A1\"\n" +
                    "      },\n" +
                    "      \"formattedValue\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello A1\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
                    "  },\n" +
                    "  \"columnCount\": 1,\n" +
                    "  \"rowCount\": 1\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/row/1?window=A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setRows(
                    Sets.of(
                        SpreadsheetSelection.parseRow("1")
                            .row()
                            .setHidden(true)
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"rows\": {\n" +
                    "    \"1\": {\n" +
                    "      \"hidden\": true\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"window\": \"A1\"\n" +
                    "}",
                DELTA
            )
        );

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/row/1?window=A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY.setRows(
                    Sets.of(
                        SpreadsheetSelection.parseRow("1")
                            .row()
                            .setHidden(false)
                    )
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"rows\": {\n" +
                    "    \"1\": {\n" +
                    "      \"hidden\": false\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"window\": \"A1\"\n" +
                    "}",
                DELTA
            )
        );
    }

    // validators........................................................................................................

    @Test
    public void testValidatorsGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/validator",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/absolute-url absolute-url\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/collection collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/email-address email-address\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/expression expression\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/non-null non-null\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/text-length text-length\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/text-mask text-mask\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-validation/Validator/validation-choice-list-expression validation-choice-list-expression\"\n" +
                    "]",
                ValidatorInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testValidatorsWithNameGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/validator/non-null",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-validation/Validator/non-null non-null\"",
                ValidatorInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testValidatorsWithNameUnknownGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/validator/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                ValidatorInfo.class.getSimpleName()
            )
        );
    }

    // file server......................................................................................................

    @Test
    public void testFileFoundGet() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.POST,
            FILE.value(),
            Maps.of(
                HttpHeaderName.ACCEPT,
                Lists.of(MediaType.ALL.accept())
            ),
            "",
            this.response(
                HttpStatusCode.OK.status(),
                HttpEntity.EMPTY.setContentType(FILE_CONTENT_TYPE)
                    .addHeader(HttpHeaderName.CONTENT_LENGTH, 6L)
                    .addHeader(HttpHeaderName.LAST_MODIFIED, FILE_LAST_MODIFIED)
                    .setBody(FILE_BINARY)));
    }

    @Test
    public void testFileNotFoundGet() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.POST,
            "/file/not/found.txt",
            HttpRequest.NO_HEADERS,
            "",
            FILE_NOT_FOUND,
            ""
        );
    }

    // helpers..........................................................................................................

    private TestHttpServer startServer() {
        final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

        SpreadsheetHttpServer.with(
            MEDIA_TYPE_DETECTOR,
            this::fileServer,
            this::server,
            SpreadsheetServerContexts.basic(
                Url.parseAbsolute("https://example.com"),
                () -> SpreadsheetStoreRepositories.basic(
                    SpreadsheetCellStores.treeMap(),
                    SpreadsheetCellReferencesStores.treeMap(),
                    SpreadsheetColumnStores.treeMap(),
                    SpreadsheetFormStores.treeMap(),
                    SpreadsheetGroupStores.treeMap(),
                    SpreadsheetLabelStores.treeMap(),
                    SpreadsheetLabelReferencesStores.treeMap(),
                    metadataStore,
                    SpreadsheetCellRangeStores.treeMap(),
                    SpreadsheetCellRangeStores.treeMap(),
                    SpreadsheetRowStores.treeMap(),
                    Storages.tree(),
                    SpreadsheetUserStores.treeMap()
                ), // Suppler<SpreadsheetStoreRepository>
                SpreadsheetProviders.basic(
                    CONVERTER_PROVIDER,
                    EXPRESSION_FUNCTION_PROVIDER, // not SpreadsheetMetadataTesting see constant above
                    SPREADSHEET_COMPARATOR_PROVIDER,
                    SPREADSHEET_EXPORTER_PROVIDER,
                    SPREADSHEET_FORMATTER_PROVIDER,
                    FORM_HANDLER_PROVIDER,
                    SPREADSHEET_IMPORTER_PROVIDER,
                    SPREADSHEET_PARSER_PROVIDER,
                    VALIDATOR_PROVIDER
                ),
                EnvironmentContexts.readOnly(
                    EnvironmentContexts.map(ENVIRONMENT_CONTEXT)
                ), // EnvironmentContext
                LOCALE_CONTEXT,
                SpreadsheetMetadataContexts.basic(
                    (u, l) -> this.metadataStore.save(
                        this.createMetadata()
                    ),
                    metadataStore
                ),
                HateosResourceHandlerContexts.basic(
                    INDENTATION,
                    LINE_ENDING,
                    JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT
                ),
                ProviderContexts.basic(
                    ConverterContexts.fake(), // CanConvert
                    EnvironmentContexts.map(
                        EnvironmentContexts.empty(
                            LOCALE,
                            NOW,
                            Optional.of(USER)
                        )
                    ),
                    SpreadsheetHttpServerTest.this.httpServer.pluginStore
                )
            )
        );

        this.httpServer.start();
        return this.httpServer;
    }

    private TestHttpServer startServerAndCreateEmptySpreadsheet() {
        final TestHttpServer server = this.startServer();

        // create spreadsheet
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.CREATED.status(),
                this.createMetadata()
                    .set(
                        SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                        SPREADSHEET_ID
                    )
            )
        );
        return server;
    }

    private SpreadsheetMetadata createMetadata() {
        return this.createMetadata(LOCALE);
    }

    private SpreadsheetMetadata createMetadata(final Locale locale) {
        return SpreadsheetMetadataTesting.METADATA_EN_AU
            .set(SpreadsheetMetadataPropertyName.LOCALE, locale)
            .set(SpreadsheetMetadataPropertyName.DATE_FORMATTER, SpreadsheetPattern.parseDateFormatPattern("\"Date\" yyyy/mm/dd").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.DATE_PARSER, SpreadsheetPattern.parseDateParsePattern("yyyy/mm/dd").spreadsheetParserSelector())
            .set(SpreadsheetMetadataPropertyName.DATE_TIME_FORMATTER, SpreadsheetPattern.parseDateTimeFormatPattern("\"DateTime\" yyyy/mm/dd hh:mm").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.DATE_TIME_PARSER, SpreadsheetPattern.parseDateTimeParsePattern("yyyy/mm/dd hh:mm").spreadsheetParserSelector())
            .set(SpreadsheetMetadataPropertyName.NUMBER_FORMATTER, SpreadsheetPattern.parseNumberFormatPattern("\"Number\" 000.000").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.NUMBER_PARSER, SpreadsheetPattern.parseNumberParsePattern("000.000").spreadsheetParserSelector())
            .set(SpreadsheetMetadataPropertyName.TEXT_FORMATTER, SpreadsheetPattern.parseTextFormatPattern("\"Text\" @").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.TIME_FORMATTER, SpreadsheetPattern.parseTimeFormatPattern("\"Time\" hh:mm").spreadsheetFormatterSelector())
            .set(SpreadsheetMetadataPropertyName.TIME_PARSER, SpreadsheetPattern.parseTimeParsePattern("hh:mm").spreadsheetParserSelector())
            .set(
                SpreadsheetMetadataPropertyName.FUNCTIONS,
                SpreadsheetExpressionFunctions.parseAliasSet("ExpressionFunction1, ExpressionFunction2")
            ).set(
                SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS,
                SpreadsheetExpressionFunctions.parseAliasSet("ExpressionFunction1, ExpressionFunction2")
            );
    }

    private SpreadsheetProvider spreadsheetIdToSpreadsheetProvider(final SpreadsheetId id) {
        return this.metadataStore.loadOrFail(id)
            .spreadsheetProvider(
                SpreadsheetProviders.basic(
                    CONVERTER_PROVIDER,
                    EXPRESSION_FUNCTION_PROVIDER,
                    SPREADSHEET_COMPARATOR_PROVIDER,
                    SPREADSHEET_EXPORTER_PROVIDER,
                    SPREADSHEET_FORMATTER_PROVIDER,
                    FORM_HANDLER_PROVIDER,
                    SPREADSHEET_IMPORTER_PROVIDER,
                    SPREADSHEET_PARSER_PROVIDER,
                    VALIDATOR_PROVIDER
                )
            );
    }

    private final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap();

//    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository = spreadsheetIdToRepository(Maps.concurrent(),
//        storeRepositorySupplier(this.metadataStore));

    /**
     * Retrieves from the cache or lazily creates a {@link SpreadsheetStoreRepository} for the given {@link SpreadsheetId}.
     */
    static Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository(final Map<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                                         final Supplier<SpreadsheetStoreRepository> repositoryFactory) {
        return (id) -> {
            SpreadsheetStoreRepository repository = spreadsheetIdToRepository.get(id);
            if (null == repository) {
                repository = repositoryFactory.get();
                spreadsheetIdToRepository.put(id, repository); // TODO add locks etc.
            }
            return repository;
        };
    }

    private Either<WebFile, HttpStatus> fileServer(final UrlPath path) {
        return path.normalize().equals(FILE) ?
            Either.left(new WebFile() {
                @Override
                public LocalDateTime lastModified() {
                    return FILE_LAST_MODIFIED;
                }

                @Override
                public MediaType contentType() {
                    return FILE_CONTENT_TYPE;
                }

                @Override
                public long contentSize() {
                    return FILE_BINARY.size();
                }

                @Override
                public InputStream content() {
                    return FILE_BINARY.inputStream();
                }

                @Override
                public Optional<ETag> etag() {
                    return Optional.empty();
                }

                @Override
                public String toString() {
                    return path.toString();
                }
            }) :
            Either.right(FILE_NOT_FOUND);
    }

    private static SpreadsheetFormula formula(final String text) {
        return SpreadsheetFormula.EMPTY
            .setText(text);
    }

    /**
     * Initializes the test {@link HttpServer}.
     */
    private HttpServer server(final HttpHandler handler) {
        this.checkNotEquals(null, handler, "handler");
        this.httpServer.setHandler(handler);
        return this.httpServer;
    }

    private final TestHttpServer httpServer = new TestHttpServer();

    /**
     * A {@link HttpServer} that allows direct invocation of the main handler skipping the HTTP transport layer
     */
    private class TestHttpServer implements HttpServer {

        private TestHttpServer() {
            super();
            this.pluginStore = PluginStores.treeMap();
        }

        void setHandler(final HttpHandler handler) {
            this.handler = handler;
        }

        @Override
        public void start() {
            this.started = true;
        }

        @Override
        public void stop() {
            this.started = false;
        }

        void handleAndCheck(final HttpMethod method,
                            final String url,
                            final Map<HttpHeaderName<?>, List<?>> headers,
                            final String body,
                            final HttpStatus status,
                            final String bodyTextContains) {
            this.handleAndCheck(
                request(method, url, headers, body),
                status,
                bodyTextContains
            );
        }

        void handleAndCheck(final HttpRequest request,
                            final HttpStatus status,
                            final String bodyTextContains) {
            final HttpResponse response = this.handle(request);
            checkEquals(status, response.status().orElse(null), "status");

            final String body = response.entity()
                .bodyText();
            if (body.isEmpty()) {
                checkEquals(
                    bodyTextContains,
                    body,
                    () -> "REQUEST\n" + request + "\nRESPONE\n" + response
                );
            } else {
                checkNotEquals(
                    "",
                    bodyTextContains,
                    "bodyTextContains must not be empty"
                );

                checkEquals(
                    true,
                    body.contains(bodyTextContains),
                    () -> "REQUEST\n" + request + "\nRESPONE\n" + response
                );
            }
        }

        void handleAndCheck(final HttpMethod method,
                            final String url,
                            final Map<HttpHeaderName<?>, List<?>> headers,
                            final String body,
                            final HttpResponse expected) {
            this.handleAndCheck(request(method, url, headers, body), expected);
        }

        void handleAndCheck(final HttpRequest request,
                            final HttpResponse expected) {
            final HttpResponse response = this.handle(request);

            checkEquals(expected, response, () -> "" + request);
        }

        HttpResponse handle(final HttpRequest request) {
            if (!this.started) {
                Assertions.fail("Server not running");
            }
            final HttpResponse response = HttpResponses.recording();
            this.handler.handle(
                request,
                response
            );
            checkNotEquals(null, response.status(), "status not set");
            return response;
        }

        private boolean started;
        private HttpHandler handler;

        private final PluginStore pluginStore;

        @Override
        public String toString() {
            return this.handler.toString();
        }
    }

    private static Binary binary(final String body,
                                 final MediaType contentType) {
        return body.isEmpty() ?
            Binary.EMPTY :
            Binary.with(
                bytes(
                    body,
                    contentType
                )
            );
    }

    /**
     * Turns the text to bytes using the {@link MediaType content type}.
     */
    private static byte[] bytes(final String body,
                                final MediaType contentType) {
        final Charset charset = MediaTypeParameterName.CHARSET.parameterValue(contentType)
            .orElseThrow(() -> new IllegalStateException("Charset missing from " + contentType))
            .charset()
            .orElseThrow(() -> new IllegalStateException("Content type missing charset " + contentType));
        return body.getBytes(charset);
    }

    private static HttpRequest request(final HttpMethod method,
                                       final String url,
                                       final Map<HttpHeaderName<?>, List<?>> headers,
                                       final String body) {
        final Map<HttpHeaderName<?>, List<?>> headers2 = Maps.sorted();
        headers2.put(
            HttpHeaderName.ACCEPT,
            Lists.of(CONTENT_TYPE_UTF8.accept()
            )
        );
        headers2.put(
            HttpHeaderName.ACCEPT_CHARSET,
            Lists.of(
                AcceptCharset.parse(CHARSET.toHeaderText())
            )
        );
        headers2.put(
            HttpHeaderName.CONTENT_TYPE,
            Lists.of(CONTENT_TYPE_UTF8)
        );
        headers2.putAll(headers);

        final byte[] bodyBytes = bytes(body, CONTENT_TYPE_UTF8);
        if (null != bodyBytes) {
            headers2.put(
                HttpHeaderName.CONTENT_LENGTH,
                Lists.of(
                    Long.valueOf(bodyBytes.length)
                )
            );
        }
        return new HttpRequest() {
            @Override
            public HttpTransport transport() {
                return HttpTransport.SECURED;
            }

            @Override
            public HttpProtocolVersion protocolVersion() {
                return HttpProtocolVersion.VERSION_1_0;
            }

            @Override
            public HttpMethod method() {
                return method;
            }

            @Override
            public RelativeUrl url() {
                return Url.parseRelative(url);
            }

            @Override
            public Map<HttpHeaderName<?>, List<?>> headers() {
                return headers2;
            }

            @Override
            public byte[] body() {
                return bodyBytes;
            }

            @Override
            public Map<HttpRequestParameterName, List<String>> parameters() {
                final Map<HttpRequestParameterName, List<String>> parameters = Maps.ordered();

                this.url()
                    .query()
                    .parameters()
                    .forEach((key, value) -> parameters.put(HttpRequestParameterName.with(key.value()), value));

                return Maps.immutable(parameters);
            }

            @Override
            public List<String> parameterValues(final HttpRequestParameterName parameterName) {
                return Optional.ofNullable(this.parameters().get(parameterName)).orElse(Lists.empty());
            }

            @Override
            public String toString() {
                return method + " " + url + "\n" + headers.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue() + "\n").collect(Collectors.joining()) + "\n" + body;
            }
        };
    }

    private HttpResponse response(final HttpStatus status) {
        final HttpResponse response = HttpResponses.recording();
        response.setStatus(status);
        return response;
    }

    private HttpResponse response(final HttpStatus status,
                                  final String bodyTypeName) {
        final HttpResponse response = this.response(status);
        response.setEntity(
            HttpEntity.EMPTY.addHeader(
                HateosResourceMappings.X_CONTENT_TYPE_NAME,
                bodyTypeName
            )
        );
        return response;
    }

    private HttpResponse response(final HttpStatus status,
                                  final SpreadsheetMetadata body) {
        return this.response(status,
            NO_TRANSACTION_ID,
            toJson(body),
            SpreadsheetMetadata.class.getSimpleName());
    }

    private HttpResponse response(final HttpStatus status,
                                  final String body,
                                  final String bodyTypeName) {
        return this.response(
            status,
            NO_TRANSACTION_ID,
            binary(
                body,
                CONTENT_TYPE_UTF8
            ),
            bodyTypeName
        );
    }

    private HttpResponse response(final HttpStatus status,
                                  final Optional<String> transactionId,
                                  final String body,
                                  final String bodyTypeName) {
        return this.response(status,
            transactionId,
            binary(body, CONTENT_TYPE_UTF8),
            bodyTypeName);
    }

    private HttpResponse response(final HttpStatus status,
                                  final Optional<String> transactionId,
                                  final Binary body,
                                  final String bodyTypeName) {
        return this.response(status,
            HttpEntity.EMPTY
                .setHeader(SpreadsheetHttpServer.TRANSACTION_ID, transactionId.map(Lists::of).orElse(Lists.empty()))
                .setContentType(CONTENT_TYPE_UTF8)
                .addHeader(HateosResourceMappings.X_CONTENT_TYPE_NAME, bodyTypeName)
                .addHeader(HttpHeaderName.CONTENT_LENGTH, (long) body.value().length)
                .setBody(body));
    }

    private HttpResponse response(final HttpStatus status,
                                  final HttpEntity body) {
        final HttpResponse response = this.response(status);
        response.setEntity(body);
        return response;
    }

    private String toJson(final Object body) {
        return JSON_NODE_MARSHALL_CONTEXT.marshall(body)
            .toString();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetHttpServer> type() {
        return SpreadsheetHttpServer.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNamePrefix() {
        return "Spreadsheet";
    }

    @Override
    public String typeNameSuffix() {
        return "Server";
    }
}
