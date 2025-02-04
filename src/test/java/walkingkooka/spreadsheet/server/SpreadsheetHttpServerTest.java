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
import walkingkooka.convert.provider.ConverterInfo;
import walkingkooka.convert.provider.ConverterInfoSet;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.HostAddress;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlQueryString;
import walkingkooka.net.UrlScheme;
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
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.plugin.JarFileTesting;
import walkingkooka.plugin.PluginName;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.plugin.store.PluginSet;
import walkingkooka.plugin.store.PluginStore;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetExpressionFunctionNames;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.export.SpreadsheetExporterInfo;
import walkingkooka.spreadsheet.export.SpreadsheetExporterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSampleList;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorToken;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelectorTokenList;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterInfo;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterInfoSet;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorToken;
import walkingkooka.spreadsheet.parser.SpreadsheetParserSelectorTokenList;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.security.store.SpreadsheetGroupStores;
import walkingkooka.spreadsheet.security.store.SpreadsheetUserStores;
import walkingkooka.spreadsheet.server.delta.SpreadsheetExpressionReferenceSimilarities;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterSelectorEdit;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterSelectorMenuList;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserSelectorEdit;
import walkingkooka.spreadsheet.server.plugin.JarEntryInfoList;
import walkingkooka.spreadsheet.server.plugin.JarEntryInfoName;
import walkingkooka.spreadsheet.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.SpreadsheetColumnStores;
import walkingkooka.spreadsheet.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetRowStores;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionParameter;
import walkingkooka.tree.expression.function.FakeExpressionFunction;
import walkingkooka.tree.expression.function.UnknownExpressionFunctionException;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionAliasSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.expression.function.provider.FakeExpressionFunctionProvider;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;
import walkingkooka.tree.text.TextNodeList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
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

    private final static LocalDateTime MODIFIED_DATE_TIME = LocalDateTime.of(2021, 7, 15, 20, 33);
    private static final SpreadsheetId SPREADSHEET_ID = SpreadsheetId.with(1L);

    private static final ExpressionFunctionProvider EXPRESSION_FUNCTION_PROVIDER = new FakeExpressionFunctionProvider() {

        @Override
        public ExpressionFunction<?, ExpressionEvaluationContext> expressionFunction(final ExpressionFunctionName name,
                                                                                     final List<?> values,
                                                                                     final ProviderContext context) {
            switch (name.value().toLowerCase()) {
                case "expressionfunction1":
                    return Cast.to(
                        new TestFunction(
                            ExpressionFunctionName.with("ExpressionFunction1")
                                .setCaseSensitivity(SpreadsheetExpressionFunctionNames.CASE_SENSITIVITY)
                        )
                    );
                default:
                    throw new UnknownExpressionFunctionException(name);
            }
        }

        @Override
        public ExpressionFunctionInfoSet expressionFunctionInfos() {
            return ExpressionFunctionInfoSet.with(
                Sets.of(
                    ExpressionFunctionInfo.with(
                        Url.parseAbsolute("https://example.com/expression-function-1"),
                        ExpressionFunctionName.with("ExpressionFunction1")
                            .setCaseSensitivity(SpreadsheetExpressionFunctionNames.CASE_SENSITIVITY)
                    ),
                    ExpressionFunctionInfo.with(
                        Url.parseAbsolute("https://example.com/expression-function-2"),
                        ExpressionFunctionName.with("ExpressionFunction2")
                            .setCaseSensitivity(SpreadsheetExpressionFunctionNames.CASE_SENSITIVITY)
                    )
                )
            );
        }

        @Override
        public CaseSensitivity expressionFunctionNameCaseSensitivity() {
            return SpreadsheetExpressionFunctionNames.CASE_SENSITIVITY;
        }
    };

    static class TestFunction extends FakeExpressionFunction<Object, ExpressionEvaluationContext> {

        TestFunction(final ExpressionFunctionName name) {
            this.name = name;
        }

        @Override
        public Object apply(final List<Object> parameters,
                            final ExpressionEvaluationContext context) {
            return context.expressionNumberKind().create(123);
        }

        @Override
        public Optional<ExpressionFunctionName> name() {
            return Optional.of(this.name);
        }

        private final ExpressionFunctionName name;

        @Override
        public ExpressionFunction<Object, ExpressionEvaluationContext> setName(final Optional<ExpressionFunctionName> name) {
            return new TestFunction(name.get());
        }

        @Override
        public List<ExpressionFunctionParameter<?>> parameters(final int count) {
            return Lists.empty();
        }
    }

    // with.............................................................................................................

    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://example.com");

    private final static Indentation INDENTATION = Indentation.SPACES2;

    private final static LineEnding LINE_ENDING = LineEnding.NL;

    private final static MediaTypeDetector MEDIA_TYPE_DETECTOR = (filename, binary) ->
        filename.endsWith(".java") ?
            MediaType.parse("text/java") :
            MediaType.BINARY;

    private final static SpreadsheetProvider SYSTEM_SPREADSHEET_PROVIDER = SpreadsheetProviders.fake();

    private final static SpreadsheetMetadataStore METADATA_STORE = SpreadsheetMetadataStores.fake();

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.fake();

    private final static Function<SpreadsheetId, SpreadsheetProvider> SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static Function<SpreadsheetId, SpreadsheetStoreRepository> SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static Function<UrlPath, Either<WebFile, HttpStatus>> FILE_SERVER = (p) -> {
        throw new UnsupportedOperationException();
    };

    private final static Function<HttpHandler, HttpServer> SERVER = (h) -> {
        throw new UnsupportedOperationException();
    };

    @Test
    public void testWithNullServerUrlFails() {
        this.withFails(
            null,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithServerUrlNonEmptyPathFails() {
        this.withFails(
            "http://example.com/path123",
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER,
            "Url must not have path got \"http://example.com/path123\""
        );
    }

    @Test
    public void testWithServerUrlNonQueryStringFails() {
        this.withFails(
            "http://example.com?path123",
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER,
            "Url must not have query string got \"http://example.com?path123\""
        );
    }

    @Test
    public void testWithServerUrlNonFragmentFails() {
        this.withFails(
            "http://example.com#fragment456",
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER,
            "Url must not have fragment got \"http://example.com#fragment456\""
        );
    }

    private void withFails(final String serverUrl,
                           final Indentation indentation,
                           final LineEnding lineEnding,
                           final MediaTypeDetector mediaTypeDetector,
                           final SpreadsheetProvider systemSpreadsheetProvider,
                           final ProviderContext providerContext,
                           final SpreadsheetMetadataStore metadataStore,
                           final HateosResourceHandlerContext hateosResourceHandlerContext,
                           final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                           final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                           final Function<HttpHandler, HttpServer> server,
                           final String message) {
        final IllegalArgumentException thrown = assertThrows(
            IllegalArgumentException.class,
            () -> SpreadsheetHttpServer.with(
                Url.parseAbsolute(serverUrl),
                indentation,
                lineEnding,
                mediaTypeDetector,
                systemSpreadsheetProvider,
                providerContext,
                metadataStore,
                hateosResourceHandlerContext,
                spreadsheetIdToSpreadsheetProvider,
                spreadsheetIdToStoreRepository,
                fileServer,
                server
            )
        );

        this.checkEquals(
            message,
            thrown.getMessage(),
            "message"
        );
    }

    @Test
    public void testWithNullIndentationFails() {
        this.withFails(
            SERVER_URL,
            null,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullLineEndingFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            null,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullMediaTypeDetectorFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            null,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullSystemSpreadsheetProviderFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            null,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            null,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullMetadataStoreFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            null,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullMarshallUnmarshallContextFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            null,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetProviderFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            null,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetStoreRepositoryFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            null,
            FILE_SERVER,
            SERVER
        );
    }

    @Test
    public void testWithNullFileServerFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            null,
            SERVER
        );
    }

    @Test
    public void testWithNullServerFails() {
        this.withFails(
            SERVER_URL,
            INDENTATION,
            LINE_ENDING,
            MEDIA_TYPE_DETECTOR,
            SYSTEM_SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            FILE_SERVER,
            null
        );
    }

    private void withFails(final AbsoluteUrl serverUrl,
                           final Indentation indentation,
                           final LineEnding lineEnding,
                           final MediaTypeDetector mediaTypeDetector,
                           final SpreadsheetProvider systemSpreadsheetProvider,
                           final ProviderContext providerContext,
                           final SpreadsheetMetadataStore metadataStore,
                           final HateosResourceHandlerContext hateosResourceHandlerContext,
                           final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                           final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                           final Function<HttpHandler, HttpServer> server) {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetHttpServer.with(
                serverUrl,
                indentation,
                lineEnding,
                mediaTypeDetector,
                systemSpreadsheetProvider,
                providerContext,
                metadataStore,
                hateosResourceHandlerContext,
                spreadsheetIdToSpreadsheetProvider,
                spreadsheetIdToStoreRepository,
                fileServer,
                server
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
    public void testPluginPostMultipartUpload() throws Exception {
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
    public void testPluginPostBinaryUpload() throws Exception {
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
            HttpStatusCode.BAD_REQUEST.setMessage("Invalid character '!' at 0 in \"!invalid-plugin-name\""),
            "walkingkooka.InvalidCharacterException: Invalid character '!' at 0 in \"!invalid-plugin-name\""
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
    public void testPluginDownloadAbsent() {
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
    public void testPluginDownloadWithAcceptAll() throws IOException {
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
    public void testPluginDownloadWithAcceptBinary() throws IOException {
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
    public void testPluginListAbsent() {
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
    public void testPluginList() throws IOException {
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
                    .addHeader(HateosResourceMapping.X_CONTENT_TYPE_NAME, JarEntryInfoList.class.getSimpleName())
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
    public void testPluginFileDownloadAbsent() {
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
    public void testPluginFileDownload() throws IOException {
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
    public void testPluginFilter() throws IOException {
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

    // metadata.........................................................................................................

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
            HttpStatusCode.NOT_FOUND.status()
                .setMessage("Unable to load spreadsheet 99"),
            "Unable to load spreadsheet 99"
        );
    }

    @Test
    public void testMetadataCreate() {
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
    public void testMetadataCreateWithTransactionIdHeader() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        this.checkNotEquals(
            null,
            this.metadataStore.load(SPREADSHEET_ID),
            () -> "spreadsheet metadata not created and saved: " + this.metadataStore
        );
    }

    @Test
    public void testMetadataCreateThenLoad() {
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
                    "  \"spreadsheet-id\": \"1\",\n" +
                    "  \"cell-character-width\": 1,\n" +
                    "  \"color-1\": \"#000000\",\n" +
                    "  \"color-2\": \"#ffffff\",\n" +
                    "  \"color-Black\": 1,\n" +
                    "  \"color-White\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, collection, error-throwing, error-to-number, error-to-string, general, plugin-selector-like-to-string, selection-to-selection, selection-to-string, spreadsheet-cell-to, string-to-selection\",\n" +
                    "  \"create-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"creator\": \"user@example.com\",\n" +
                    "  \"currency-symbol\": \"$\",\n" +
                    "  \"date-formatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"date-parser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"date-time-formatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"date-time-offset\": \"-25569\",\n" +
                    "  \"date-time-parser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"decimal-separator\": \".\",\n" +
                    "  \"default-year\": 2000,\n" +
                    "  \"exponent-symbol\": \"e\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expression-number-kind\": \"BIG_DECIMAL\",\n" +
                    "  \"find-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"find-functions\": \"\",\n" +
                    "  \"format-converter\": \"collection(error-to-number, error-to-string, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formatters\": \"automatic, collection, date-format-pattern, date-time-format-pattern, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formula-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formula-functions\": \"ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"functions\": \"ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"general-number-format-digit-count\": 8,\n" +
                    "  \"group-separator\": \",\",\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"modified-by\": \"user@example.com\",\n" +
                    "  \"modified-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"negative-sign\": \"-\",\n" +
                    "  \"number-formatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"number-parser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"percentage-symbol\": \"%\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"positive-sign\": \"+\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"rounding-mode\": \"HALF_UP\",\n" +
                    "  \"sort-comparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sort-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"text-formatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"time-formatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"time-parser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"two-digit-year\": 50,\n" +
                    "  \"value-separator\": \",\"\n" +
                    "}",
                SpreadsheetMetadata.class.getSimpleName()
            )
        );
    }

    @Test
    public void testCellSaveApostropheString() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSaveDate() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSaveDateTime() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSaveNumber() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSaveTime() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSaveExpressionString() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSaveExpressionNumber() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSaveSelectionQueryParameter() {
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
                "      \"formatted-value\": {\n" +
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
    public void testCellSort() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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

    // patch............................................................................................................

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
        final String currency = "NSWD";

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            JsonNode.object()
                .set(
                    JsonPropertyName.with(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL.value()),
                    JsonNode.string(currency)
                )
                .toString(),
            this.response(
                HttpStatusCode.OK.status(),
                loaded.set(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL, currency)
            )
        );
    }

    @Test
    public void testPatchFails() {
        final TestHttpServer server = this.startServer();

        // patch metadata will fail with 404
        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1",
            NO_HEADERS_TRANSACTION_ID,
            JsonNode.object()
                .set(
                    JsonPropertyName.with(SpreadsheetMetadataPropertyName.CURRENCY_SYMBOL.value()),
                    JsonNode.string("NSWD")
                )
                .toString(),
            HttpStatusCode.NOT_FOUND.setMessage("Unable to load spreadsheet with id=1"),
            "walkingkooka.store.MissingStoreException: Unable to load spreadsheet with id=1"
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
    public void testCellSaveThenSaveAnotherCellReferencingFirst() {
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
                    "      \"formatted-value\": {\n" +
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
                    "                            \"type\": \"row-reference-spreadsheet-formula-parser-token\",\n" +
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellSaveTwice() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "                            \"type\": \"row-reference-spreadsheet-formula-parser-token\",\n" +
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellSaveFormulaWithMixedCaseFunction() {
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
                    "      \"formatted-value\": {\n" +
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
            "/api/spreadsheet/1/cell/!",
            NO_HEADERS_TRANSACTION_ID,
            "{\n" +
                "  \"formulaXXX\": {\n" +
                "    \"text\": \"=2\"\n" +
                "  }\n" +
                "}",
            HttpStatusCode.BAD_REQUEST
                .setMessage("Invalid character '!' at 0 in \"!\""),
            "Invalid character '!' at 0 in \"!\""
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellPatchStyle() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
                .setMessage("Label not found: UnknownLabel123"),
            "Label not found: UnknownLabel123"
        );
    }

    @Test
    public void testCellSavePatchCellLabel() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("ZZZ");
        final SpreadsheetCellReference cellReference = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingTarget(cellReference);

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
                    "      \"formatted-value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"ZZZ\",\n" +
                    "      \"target\": \"B2\"\n" +
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
                    "      \"formatted-value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text PatchedText123\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"ZZZ\",\n" +
                    "      \"target\": \"B2\"\n" +
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
                DELTA
            )
        );
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellLoadViewport() {
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
                    "      \"formatted-value\": {\n" +
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
                    "                            \"type\": \"row-reference-spreadsheet-formula-parser-token\",\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "    \"rectangle\": \"A1:200.0:60.0\"\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "                            \"type\": \"row-reference-spreadsheet-formula-parser-token\",\n" +
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
                    "      \"formatted-value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Number 007.000\"\n" +
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
                    "  \"rowCount\": 2,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportWithSelectionQueryParameters() {
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
                    "      \"formatted-value\": {\n" +
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
                    "    \"rectangle\": \"A1:200.0:60.0\",\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportWithSelectionQueryParametersWithAnchor() {
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
                    "      \"formatted-value\": {\n" +
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
                    "    \"rectangle\": \"A1:200.0:60.0\",\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportWithSelectionQueryParametersAnchorDefaulted() {
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
                    "      \"formatted-value\": {\n" +
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
                    "    \"rectangle\": \"A1:200.0:60.0\",\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"rowCount\": 1,\n" +
                    "  \"window\": \"A1:B2\"\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testCellLoadViewportWithSelectionAndNavigationQueryParameters() {
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
                    "      \"formatted-value\": {\n" +
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
                    "    \"rectangle\": \"A1:200.0:60.0\",\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"spreadsheet-id\": \"1\",\n" +
                    "  \"cell-character-width\": 1,\n" +
                    "  \"color-1\": \"#000000\",\n" +
                    "  \"color-2\": \"#ffffff\",\n" +
                    "  \"color-Black\": 1,\n" +
                    "  \"color-White\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, collection, error-throwing, error-to-number, error-to-string, general, plugin-selector-like-to-string, selection-to-selection, selection-to-string, spreadsheet-cell-to, string-to-selection\",\n" +
                    "  \"create-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"creator\": \"user@example.com\",\n" +
                    "  \"currency-symbol\": \"$\",\n" +
                    "  \"date-formatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"date-parser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"date-time-formatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"date-time-offset\": \"-25569\",\n" +
                    "  \"date-time-parser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"decimal-separator\": \".\",\n" +
                    "  \"default-year\": 2000,\n" +
                    "  \"exponent-symbol\": \"e\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expression-number-kind\": \"BIG_DECIMAL\",\n" +
                    "  \"find-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"find-functions\": \"\",\n" +
                    "  \"format-converter\": \"collection(error-to-number, error-to-string, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formatters\": \"automatic, collection, date-format-pattern, date-time-format-pattern, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formula-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formula-functions\": \"ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"functions\": \"ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"general-number-format-digit-count\": 8,\n" +
                    "  \"group-separator\": \",\",\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"modified-by\": \"user@example.com\",\n" +
                    "  \"modified-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"negative-sign\": \"-\",\n" +
                    "  \"number-formatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"number-parser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"percentage-symbol\": \"%\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"positive-sign\": \"+\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"rounding-mode\": \"HALF_UP\",\n" +
                    "  \"sort-comparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sort-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"text-formatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"time-formatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"time-parser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"two-digit-year\": 50,\n" +
                    "  \"value-separator\": \",\",\n" +
                    "  \"viewport\": {\n" +
                    "    \"rectangle\": \"A1:200.0:60.0\",\n" +
                    "    \"anchoredSelection\": {\n" +
                    "      \"selection\": {\n" +
                    "        \"type\": \"spreadsheet-cell-range-reference\",\n" +
                    "        \"value\": \"A1:B1\"\n" +
                    "      },\n" +
                    "      \"anchor\": \"TOP_LEFT\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}",
                SpreadsheetMetadata.class.getSimpleName()
            )
        );
    }

    @Test
    public void testCellLoadViewportWithSelectionLabelAndNavigationQueryParameters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetLabelName label123 = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label123.setLabelMappingTarget(a1);

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
                    "      \"formatted-value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label123\",\n" +
                    "      \"target\": \"A1\"\n" +
                    "    }\n" +
                    "  ],\n" +
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
                    "    \"rectangle\": \"A1:200.0:60.0\",\n" +
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
                    "      \"formatted-value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text Hello'\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"labels\": [\n" +
                    "    {\n" +
                    "      \"label\": \"Label123\",\n" +
                    "      \"target\": \"A1\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"columnWidths\": {\n" +
                    "    \"A\": 100\n" +
                    "  },\n" +
                    "  \"rowHeights\": {\n" +
                    "    \"1\": 50\n" +
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
    public void testCellLoadWithFunctionMissingFromFormulaExpressions() {
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
                    "      \"formatted-value\": {\n" +
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
                    ExpressionFunctionAliasSet.parse("ExpressionFunction2")
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"spreadsheet-id\": \"1\",\n" +
                    "  \"cell-character-width\": 1,\n" +
                    "  \"color-1\": \"#000000\",\n" +
                    "  \"color-2\": \"#ffffff\",\n" +
                    "  \"color-Black\": 1,\n" +
                    "  \"color-White\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, collection, error-throwing, error-to-number, error-to-string, general, plugin-selector-like-to-string, selection-to-selection, selection-to-string, spreadsheet-cell-to, string-to-selection\",\n" +
                    "  \"create-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"creator\": \"user@example.com\",\n" +
                    "  \"currency-symbol\": \"$\",\n" +
                    "  \"date-formatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"date-parser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"date-time-formatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"date-time-offset\": \"-25569\",\n" +
                    "  \"date-time-parser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"decimal-separator\": \".\",\n" +
                    "  \"default-year\": 2000,\n" +
                    "  \"exponent-symbol\": \"e\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expression-number-kind\": \"BIG_DECIMAL\",\n" +
                    "  \"find-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"find-functions\": \"\",\n" +
                    "  \"format-converter\": \"collection(error-to-number, error-to-string, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formatters\": \"automatic, collection, date-format-pattern, date-time-format-pattern, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formula-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formula-functions\": \"ExpressionFunction2\",\n" +
                    "  \"functions\": \"ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"general-number-format-digit-count\": 8,\n" +
                    "  \"group-separator\": \",\",\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"modified-by\": \"user@example.com\",\n" +
                    "  \"modified-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"negative-sign\": \"-\",\n" +
                    "  \"number-formatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"number-parser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"percentage-symbol\": \"%\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"positive-sign\": \"+\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"rounding-mode\": \"HALF_UP\",\n" +
                    "  \"sort-comparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sort-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"text-formatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"time-formatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"time-parser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"two-digit-year\": 50,\n" +
                    "  \"value-separator\": \",\"\n" +
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
                    "              \"value\": \"ExpressionFunction1\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      },\n" +
                    "      \"formatted-value\": {\n" +
                    "        \"type\": \"text\",\n" +
                    "        \"value\": \"Text #NAME?\"\n" +
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
                    ExpressionFunctionAliasSet.parse("ExpressionFunction1")
                )
            ),
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"spreadsheet-id\": \"1\",\n" +
                    "  \"cell-character-width\": 1,\n" +
                    "  \"color-1\": \"#000000\",\n" +
                    "  \"color-2\": \"#ffffff\",\n" +
                    "  \"color-Black\": 1,\n" +
                    "  \"color-White\": 2,\n" +
                    "  \"comparators\": \"date, date-time, day-of-month, day-of-week, hour-of-am-pm, hour-of-day, minute-of-hour, month-of-year, nano-of-second, number, seconds-of-minute, text, text-case-insensitive, time, year\",\n" +
                    "  \"converters\": \"basic, collection, error-throwing, error-to-number, error-to-string, general, plugin-selector-like-to-string, selection-to-selection, selection-to-string, spreadsheet-cell-to, string-to-selection\",\n" +
                    "  \"create-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"creator\": \"user@example.com\",\n" +
                    "  \"currency-symbol\": \"$\",\n" +
                    "  \"date-formatter\": \"date-format-pattern \\\"Date\\\" yyyy/mm/dd\",\n" +
                    "  \"date-parser\": \"date-parse-pattern yyyy/mm/dd\",\n" +
                    "  \"date-time-formatter\": \"date-time-format-pattern \\\"DateTime\\\" yyyy/mm/dd hh:mm\",\n" +
                    "  \"date-time-offset\": \"-25569\",\n" +
                    "  \"date-time-parser\": \"date-time-parse-pattern yyyy/mm/dd hh:mm\",\n" +
                    "  \"decimal-separator\": \".\",\n" +
                    "  \"default-year\": 2000,\n" +
                    "  \"exponent-symbol\": \"e\",\n" +
                    "  \"exporters\": \"collection, empty, json\",\n" +
                    "  \"expression-number-kind\": \"BIG_DECIMAL\",\n" +
                    "  \"find-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"find-functions\": \"\",\n" +
                    "  \"format-converter\": \"collection(error-to-number, error-to-string, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formatters\": \"automatic, collection, date-format-pattern, date-time-format-pattern, general, number-format-pattern, spreadsheet-pattern-collection, text-format-pattern, time-format-pattern\",\n" +
                    "  \"formula-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"formula-functions\": \"ExpressionFunction1\",\n" +
                    "  \"functions\": \"ExpressionFunction1, ExpressionFunction2\",\n" +
                    "  \"general-number-format-digit-count\": 8,\n" +
                    "  \"group-separator\": \",\",\n" +
                    "  \"importers\": \"collection, empty, json\",\n" +
                    "  \"locale\": \"en-AU\",\n" +
                    "  \"modified-by\": \"user@example.com\",\n" +
                    "  \"modified-date-time\": \"1999-12-31T12:58\",\n" +
                    "  \"negative-sign\": \"-\",\n" +
                    "  \"number-formatter\": \"number-format-pattern \\\"Number\\\" 000.000\",\n" +
                    "  \"number-parser\": \"number-parse-pattern 000.000\",\n" +
                    "  \"parsers\": \"date-parse-pattern, date-time-parse-pattern, number-parse-pattern, time-parse-pattern\",\n" +
                    "  \"percentage-symbol\": \"%\",\n" +
                    "  \"plugins\": \"\",\n" +
                    "  \"positive-sign\": \"+\",\n" +
                    "  \"precision\": 7,\n" +
                    "  \"rounding-mode\": \"HALF_UP\",\n" +
                    "  \"sort-comparators\": \"date,datetime,day-of-month,day-of-year,hour-of-ampm,hour-of-day,minute-of-hour,month-of-year,nano-of-second,number,seconds-of-minute,text,text-case-insensitive,time,year\",\n" +
                    "  \"sort-converter\": \"collection(error-to-number, error-throwing, string-to-selection, selection-to-selection, selection-to-string, general)\",\n" +
                    "  \"style\": {\n" +
                    "    \"height\": \"50px\",\n" +
                    "    \"width\": \"100px\"\n" +
                    "  },\n" +
                    "  \"text-formatter\": \"text-format-pattern \\\"Text\\\" @\",\n" +
                    "  \"time-formatter\": \"time-format-pattern \\\"Time\\\" hh:mm\",\n" +
                    "  \"time-parser\": \"time-parse-pattern hh:mm\",\n" +
                    "  \"two-digit-year\": 50,\n" +
                    "  \"value-separator\": \",\"\n" +
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
                    "      \"formatted-value\": {\n" +
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
    public void testLoadCellInvalidWindowQueryParameterBadRequest() {
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
    public void testCellSaveUpdateMetadataLoadCell() {
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
                    "      \"formatted-value\": {\n" +
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
        final SpreadsheetMetadata updated = initial.set(SpreadsheetMetadataPropertyName.DECIMAL_SEPARATOR, ',');

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
                    "      \"formatted-value\": {\n" +
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

    // save cell, save metadata, save cell..............................................................................

    @Test
    public void testCellSaveThenSaveLabel() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingTarget(
            SpreadsheetSelection.parseCell("A99")
        );

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
    }

    // cell-reference...................................................................................................

    @Test
    public void testCellReferenceGet() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/cell-reference/" + reference + "?count=100",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"cell-reference\": \"A99\"\n" +
                    "}",
                SpreadsheetExpressionReferenceSimilarities.class.getSimpleName()
            )
        );
    }

    // column...........................................................................................................

    @Test
    public void testColumnClear() {
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testColumnClearRange() {
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testColumnInsertAfter() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
            "/api/spreadsheet/1/column/C/after?count=1",
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
                    "      \"formatted-value\": {\n" +
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
    public void testColumnInsertBefore() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
            "/api/spreadsheet/1/column/D/before?count=1",
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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

    // label............................................................................................................

    @Test
    public void testLabelSave() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingTarget(reference);

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
    public void testLabelSaveAndLoad() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingTarget(reference);

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
                    )
                ),
                SpreadsheetDelta.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLabelSaveAndResolveCellReference() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingTarget(reference);

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
            "/api/spreadsheet/1/cell-reference/" + label + "?count=1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                this.toJson(
                    SpreadsheetExpressionReferenceSimilarities.with(
                        SpreadsheetExpressionReferenceSimilarities.NO_CELL_REFERENCE,
                        SpreadsheetExpressionReferenceSimilarities.NO_LABEL,
                        Sets.of(mapping)
                    )
                ),
                SpreadsheetExpressionReferenceSimilarities.class.getSimpleName()
            )
        );
    }

    @Test
    public void testLabelDelete() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        final SpreadsheetCellReference reference = SpreadsheetSelection.parseCell("A99");
        final SpreadsheetLabelName label = SpreadsheetSelection.labelName("Label123");
        final SpreadsheetLabelMapping mapping = label.setLabelMappingTarget(reference);

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

    // comparators......................................................................................................

    @Test
    public void testComparators() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/comparator",
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
    public void testComparatorsWithName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/comparator/day-of-month",
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
    public void testComparatorsWithNameUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/comparator/unknown",
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
    public void testConverters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/converter",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/basic basic\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/collection collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-throwing error-throwing\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-number error-to-number\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/error-to-string error-to-string\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/general general\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/plugin-selector-like-to-string plugin-selector-like-to-string\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/selection-to-selection selection-to-selection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/selection-to-string selection-to-string\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/spreadsheet-cell-to spreadsheet-cell-to\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/Converter/string-to-selection string-to-selection\"\n" +
                    "]",
                ConverterInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testConvertersWithName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/converter/general",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://github.com/mP1/walkingkooka-spreadsheet/Converter/general general\"",
                ConverterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testConvertersWithNameUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/converter/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                ConverterInfo.class.getSimpleName()
            )
        );
    }

    // exporters........................................................................................................

    @Test
    public void testExporters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/exporter",
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
    public void testExportersWithName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/exporter/json",
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
    public void testExportersWithNameUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/exporter/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetExporterInfo.class.getSimpleName()
            )
        );
    }

    // formatters......................................................................................................

    @Test
    public void testFormatters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/formatter",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/automatic automatic\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/collection collection\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-format-pattern date-format-pattern\",\n" +
                    "  \"https://github.com/mP1/walkingkooka-spreadsheet/SpreadsheetFormatter/date-time-format-pattern date-time-format-pattern\",\n" +
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
    public void testFormatterByName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/formatter/date-format-pattern",
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
    public void testFormatterByNameNotFound() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/formatter/unknown-formatter-404",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetFormatterInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterEdit() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/formatter/*/edit",
            NO_HEADERS_TRANSACTION_ID,
            "\"date-format-pattern yyyy/mm/ddd\"",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"selector\": \"date-format-pattern yyyy/mm/ddd\",\n" +
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
                    "    }\n" +
                    "  ]\n" +
                    "}",
                SpreadsheetFormatterSelectorEdit.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterBatchFormat() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/formatter/*/format",
            NO_HEADERS_TRANSACTION_ID,
            "[\n" +
                "  {\n" +
                "    \"selector\": \"date-format-pattern yyyy/mmmm/dddd d\",\n" +
                "    \"value\": {\n" +
                "      \"type\": \"local-date\",\n" +
                "      \"value\": \"1999-12-31\"\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"selector\": \"text-format-pattern @@\",\n" +
                "    \"value\": \"Text123\"\n" +
                "  }\n" +
                "]",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"type\": \"text\",\n" +
                    "    \"value\": \"1999/December/Friday 31\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"type\": \"text\",\n" +
                    "    \"value\": \"Text123Text123\"\n" +
                    "  }\n" +
                    "]",
                TextNodeList.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterMenu() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/formatter/*/menu",
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
                SpreadsheetFormatterSelectorMenuList.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterTokens() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/formatter/date-format-pattern/tokens",
            NO_HEADERS_TRANSACTION_ID,
            "\"yyyy/mm/ddd\"",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"label\": \"yyyy\",\n" +
                    "    \"text\": \"yyyy\",\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"yy\",\n" +
                    "        \"text\": \"yy\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"/\",\n" +
                    "    \"text\": \"/\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"mm\",\n" +
                    "    \"text\": \"mm\",\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"m\",\n" +
                    "        \"text\": \"m\"\n" +
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
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"/\",\n" +
                    "    \"text\": \"/\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"ddd\",\n" +
                    "    \"text\": \"ddd\",\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"d\",\n" +
                    "        \"text\": \"d\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"dd\",\n" +
                    "        \"text\": \"dd\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"dddd\",\n" +
                    "        \"text\": \"dddd\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]",
                SpreadsheetFormatterSelectorTokenList.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterNextToken() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/formatter/date-format-pattern/next-token",
            NO_HEADERS_TRANSACTION_ID,
            "\"yyyy/mm/ddd\"",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"alternatives\": [\n" +
                    "    {\n" +
                    "      \"label\": \"m\",\n" +
                    "      \"text\": \"m\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mm\",\n" +
                    "      \"text\": \"mm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mmm\",\n" +
                    "      \"text\": \"mmm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mmmm\",\n" +
                    "      \"text\": \"mmmm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mmmmm\",\n" +
                    "      \"text\": \"mmmmm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"yy\",\n" +
                    "      \"text\": \"yy\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"yyyy\",\n" +
                    "      \"text\": \"yyyy\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}",
                SpreadsheetFormatterSelectorToken.class.getSimpleName()
            )
        );
    }

    @Test
    public void testFormatterSamples() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/formatter/date-format-pattern/samples",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"label\": \"Short\",\n" +
                    "    \"selector\": \"date-format-pattern d/m/yy\",\n" +
                    "    \"value\": {\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"31/12/99\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Medium\",\n" +
                    "    \"selector\": \"date-format-pattern d mmm yyyy\",\n" +
                    "    \"value\": {\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"31 Dec. 1999\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Long\",\n" +
                    "    \"selector\": \"date-format-pattern d mmmm yyyy\",\n" +
                    "    \"value\": {\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"31 December 1999\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"Full\",\n" +
                    "    \"selector\": \"date-format-pattern dddd, d mmmm yyyy\",\n" +
                    "    \"value\": {\n" +
                    "      \"type\": \"text\",\n" +
                    "      \"value\": \"Friday, 31 December 1999\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]",
                SpreadsheetFormatterSampleList.class.getSimpleName()
            )
        );
    }

    // expression-function.............................................................................................

    @Test
    public void testExpressionFunction() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/function",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  \"https://example.com/expression-function-1 ExpressionFunction1\",\n" +
                    "  \"https://example.com/expression-function-2 ExpressionFunction2\"\n" +
                    "]",
                ExpressionFunctionInfoSet.class.getSimpleName()
            )
        );
    }

    @Test
    public void testExpressionFunctionByName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/function/ExpressionFunction1",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.OK.status(),
                "\"https://example.com/expression-function-1 ExpressionFunction1\"",
                ExpressionFunctionInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testExpressionFunctionByNameNotFound() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/function/UnknownFunctionName",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                ExpressionFunctionInfo.class.getSimpleName()
            )
        );
    }

    // importers........................................................................................................

    @Test
    public void testImporters() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/importer",
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
    public void testImportersWithName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/importer/json",
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
    public void testImportersWithNameUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/importer/unknown",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetImporterInfo.class.getSimpleName()
            )
        );
    }

    // parsers......................................................................................................

    @Test
    public void testParsers() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/parser",
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
    public void testParsersBySpreadsheetParserName() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/parser/date-parse-pattern",
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
    public void testParsersBySpreadsheetParserNameUnknown() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.GET,
            "/api/spreadsheet/1/parser/unknown-parser-204",
            NO_HEADERS_TRANSACTION_ID,
            "",
            this.response(
                HttpStatusCode.NO_CONTENT.status(),
                SpreadsheetParserInfo.class.getSimpleName()
            )
        );
    }

    @Test
    public void testParserEdit() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/parser/*/edit",
            NO_HEADERS_TRANSACTION_ID,
            "\"date-parse-pattern yyyy/mm/ddd\"",
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
                    "    }\n" +
                    "  ]\n" +
                    "}",
                SpreadsheetParserSelectorEdit.class.getSimpleName()
            )
        );
    }

    @Test
    public void testParserNextToken() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/parser/date-parse-pattern/next-token",
            NO_HEADERS_TRANSACTION_ID,
            "\"yyyy/mm/ddd\"",
            this.response(
                HttpStatusCode.OK.status(),
                "{\n" +
                    "  \"alternatives\": [\n" +
                    "    {\n" +
                    "      \"label\": \"m\",\n" +
                    "      \"text\": \"m\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mm\",\n" +
                    "      \"text\": \"mm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mmm\",\n" +
                    "      \"text\": \"mmm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mmmm\",\n" +
                    "      \"text\": \"mmmm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"mmmmm\",\n" +
                    "      \"text\": \"mmmmm\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"yy\",\n" +
                    "      \"text\": \"yy\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"label\": \"yyyy\",\n" +
                    "      \"text\": \"yyyy\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}",
                SpreadsheetParserSelectorToken.class.getSimpleName()
            )
        );
    }

    @Test
    public void testParserTokens() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/parser/date-parse-pattern/tokens",
            NO_HEADERS_TRANSACTION_ID,
            "\"yyyy/mm/ddd\"",
            this.response(
                HttpStatusCode.OK.status(),
                "[\n" +
                    "  {\n" +
                    "    \"label\": \"yyyy\",\n" +
                    "    \"text\": \"yyyy\",\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"yy\",\n" +
                    "        \"text\": \"yy\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"/\",\n" +
                    "    \"text\": \"/\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"mm\",\n" +
                    "    \"text\": \"mm\",\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"m\",\n" +
                    "        \"text\": \"m\"\n" +
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
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"/\",\n" +
                    "    \"text\": \"/\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"label\": \"ddd\",\n" +
                    "    \"text\": \"ddd\",\n" +
                    "    \"alternatives\": [\n" +
                    "      {\n" +
                    "        \"label\": \"d\",\n" +
                    "        \"text\": \"d\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"dd\",\n" +
                    "        \"text\": \"dd\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"label\": \"dddd\",\n" +
                    "        \"text\": \"dddd\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]",
                SpreadsheetParserSelectorTokenList.class.getSimpleName()
            )
        );
    }

    // row...........................................................................................................

    @Test
    public void testRowClear() {
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testRowClearRange() {
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
                    "      \"formatted-value\": {\n" +
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
                    "  \"columnCount\": 0,\n" +
                    "  \"rowCount\": 0\n" +
                    "}",
                DELTA
            )
        );
    }

    @Test
    public void testRowInsertAfter() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
            "/api/spreadsheet/1/row/3/after?count=1",
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
                    "      \"formatted-value\": {\n" +
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
    public void testRowInsertBefore() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
            "/api/spreadsheet/1/row/4/before?count=1",
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
                    "      \"formatted-value\": {\n" +
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
    public void testRowDeleteShiftsCell() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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

    // clear...........................................................................................................


    @Test
    public void testCellClear() {
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
                    "      \"formatted-value\": {\n" +
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
            "/api/spreadsheet/1/cell/B2/clear",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY),
            this.response(
                HttpStatusCode.BAD_REQUEST.setMessage("Unknown link relation \"clear\"")
            )
        );
    }

    @Test
    public void testCellClearRange() {
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellFill() {
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellFillRepeatsFromRange() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // fill A1:B2 from A1
        server.handleAndCheck(HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1:B2/fill?from=A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellFillThatClears() {
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
                    "      \"formatted-value\": {\n" +
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
    public void testCellFillWithSelectionQueryParameter() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2?home=A1&width=900&height=700&selectionType=cell&selection=C3&window=",
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
                    "      \"formatted-value\": {\n" +
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
                    "      \"formatted-value\": {\n" +
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

    // find cells.......................................................................................................

    @Test
    public void testFindCellsNonBooleanQuery() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell B2
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/B2?home=A1&width=900&height=700&selectionType=cell&selection=C3&window=",
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
                    "      \"formatted-value\": {\n" +
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
                "      \"formatted-value\": {\n" +
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

    // column...........................................................................................................

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
    public void testCellSavePatchColumn() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell A1
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY
                .setCells(
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
                    "      \"formatted-value\": {\n" +
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
                SpreadsheetDelta.EMPTY
                    .setColumns(
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

    // row...........................................................................................................

    @Test
    public void testRowPatch() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        server.handleAndCheck(
            HttpMethod.PATCH,
            "/api/spreadsheet/1/row/1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(
                SpreadsheetDelta.EMPTY
                    .setRows(
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
                SpreadsheetDelta.EMPTY
                    .setRows(
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
    public void testCellSavePatchRow() {
        final TestHttpServer server = this.startServerAndCreateEmptySpreadsheet();

        // save cell A1
        server.handleAndCheck(
            HttpMethod.POST,
            "/api/spreadsheet/1/cell/A1",
            NO_HEADERS_TRANSACTION_ID,
            toJson(SpreadsheetDelta.EMPTY
                .setCells(
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
                    "      \"formatted-value\": {\n" +
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
                SpreadsheetDelta.EMPTY
                    .setRows(
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
                SpreadsheetDelta.EMPTY
                    .setRows(
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

    // file server......................................................................................................

    @Test
    public void testFileFound() {
        final TestHttpServer server = this.startServer();

        server.handleAndCheck(
            HttpMethod.POST,
            FILE.value(),
            Maps.of(
                HttpHeaderName.ACCEPT,
                Lists.of(MediaType.ALL.accept())
            ),
            "",
            this.response(HttpStatusCode.OK.status(),
                HttpEntity.EMPTY
                    .setContentType(FILE_CONTENT_TYPE)
                    .addHeader(HttpHeaderName.CONTENT_LENGTH, 6L)
                    .addHeader(HttpHeaderName.LAST_MODIFIED, FILE_LAST_MODIFIED)
                    .setBody(FILE_BINARY)));
    }

    @Test
    public void testFileNotFound() {
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
        SpreadsheetHttpServer.with(
            UrlScheme.HTTP.andHost(
                HostAddress.with("example.com")
            ),
            Indentation.SPACES2,
            LineEnding.NL,
            MEDIA_TYPE_DETECTOR,
            SpreadsheetProviders.basic(
                CONVERTER_PROVIDER,
                EXPRESSION_FUNCTION_PROVIDER, // not SpreadsheetMetadataTesting see constant above
                SPREADSHEET_COMPARATOR_PROVIDER,
                SPREADSHEET_EXPORTER_PROVIDER,
                SPREADSHEET_FORMATTER_PROVIDER,
                SPREADSHEET_IMPORTER_PROVIDER,
                SPREADSHEET_PARSER_PROVIDER
            ),
            ProviderContexts.basic(
                EnvironmentContexts.empty(
                    NOW,
                    Optional.of(USER)
                ),
                this.httpServer.pluginStore
            ),
            this.metadataStore,
            HateosResourceHandlerContexts.basic(
                JsonNodeMarshallUnmarshallContexts.basic(
                    JSON_NODE_MARSHALL_CONTEXT,
                    JSON_NODE_UNMARSHALL_CONTEXT
                )
            ),
            this::spreadsheetIdToSpreadsheetProvider,
            this.spreadsheetIdToRepository,
            this::fileServer,
            this::server
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
        return SpreadsheetMetadataTesting.METADATA_EN_AU
            .set(SpreadsheetMetadataPropertyName.LOCALE, LOCALE)
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
                ExpressionFunctionAliasSet.parse("ExpressionFunction1, ExpressionFunction2")
            ).set(
                SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS,
                ExpressionFunctionAliasSet.parse("ExpressionFunction1, ExpressionFunction2")
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
                    SPREADSHEET_IMPORTER_PROVIDER,
                    SPREADSHEET_PARSER_PROVIDER
                )
            );
    }

    private final SpreadsheetMetadataStore metadataStore = SpreadsheetMetadataStores.treeMap(
        createMetadata(),
        NOW
    );

    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository = spreadsheetIdToRepository(Maps.concurrent(),
        storeRepositorySupplier(this.metadataStore));

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

    /**
     * Creates a new {@link SpreadsheetStoreRepository} on demand
     */
    private static Supplier<SpreadsheetStoreRepository> storeRepositorySupplier(final SpreadsheetMetadataStore metadataStore) {
        return () -> SpreadsheetStoreRepositories.basic(
            SpreadsheetCellStores.treeMap(),
            SpreadsheetExpressionReferenceStores.treeMap(),
            SpreadsheetColumnStores.treeMap(),
            SpreadsheetGroupStores.treeMap(),
            SpreadsheetLabelStores.treeMap(),
            SpreadsheetExpressionReferenceStores.treeMap(),
            metadataStore,
            SpreadsheetCellRangeStores.treeMap(),
            SpreadsheetCellRangeStores.treeMap(),
            SpreadsheetRowStores.treeMap(),
            SpreadsheetUserStores.treeMap()
        );
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

    private SpreadsheetMetadata spreadsheetMetadataStamper(final SpreadsheetMetadata metadata) {
        return metadata.set(
            SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME,
            MODIFIED_DATE_TIME
        );
    }

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
        headers2.put(HttpHeaderName.ACCEPT, list(CONTENT_TYPE_UTF8.accept()));
        headers2.put(HttpHeaderName.ACCEPT_CHARSET, list(AcceptCharset.parse(CHARSET.toHeaderText())));
        headers2.put(HttpHeaderName.CONTENT_TYPE, list(CONTENT_TYPE_UTF8));
        headers2.putAll(headers);

        final byte[] bodyBytes = bytes(body, CONTENT_TYPE_UTF8);
        if (null != bodyBytes) {
            headers2.put(HttpHeaderName.CONTENT_LENGTH, list((long) bodyBytes.length));
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

    private static <T> List<T> list(final T... values) {
        return Lists.of(values);
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
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
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
                .addHeader(HateosResourceMapping.X_CONTENT_TYPE_NAME, bodyTypeName)
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
