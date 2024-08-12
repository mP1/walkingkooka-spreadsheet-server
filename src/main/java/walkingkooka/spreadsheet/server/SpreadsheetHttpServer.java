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

import walkingkooka.Either;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.provider.ConverterProvider;
import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.HostAddress;
import walkingkooka.net.IpPort;
import walkingkooka.net.Url;
import walkingkooka.net.UrlFragment;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.UrlQueryString;
import walkingkooka.net.UrlScheme;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.parser.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A spreadsheet server that uses the given {@link HttpServer} and some other dependencies.
 */
public final class SpreadsheetHttpServer implements HttpServer {

    /**
     * This header contains the client transaction-id and is used to map responses with the original requests.
     */
    public final static HttpHeaderName<String> TRANSACTION_ID = HttpHeaderName.with("X-transaction-id")
            .stringValues();

    /**
     * Creates a new {@link SpreadsheetHttpServer} using the config and the functions to create the actual {@link HttpServer}.
     */
    public static SpreadsheetHttpServer with(final UrlScheme scheme,
                                             final HostAddress host,
                                             final IpPort port,
                                             final Indentation indentation,
                                             final LineEnding lineEnding,
                                             final Supplier<LocalDateTime> now,
                                             final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                             final SpreadsheetMetadataStore metadataStore,
                                             final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                             final Function<BigDecimal, Fraction> fractioner,
                                             final JsonNodeMarshallContext jsonNodeMarshallContext,
                                             final JsonNodeUnmarshallContext jsonNodeUnmarshallContext,
                                             final Function<SpreadsheetId, ConverterProvider> spreadsheetIdToConverterProvider,
                                             final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdToSpreadsheetComparatorProvider,
                                             final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdToSpreadsheetFormatterProvider,
                                             final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider,
                                             final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdToSpreadsheetParserProvider,
                                             final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                             final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                             final Function<HttpHandler, HttpServer> server) {
        return new SpreadsheetHttpServer(
                scheme,
                host,
                port,
                indentation,
                lineEnding,
                now,
                createMetadata,
                metadataStore,
                spreadsheetMetadataStamper,
                fractioner,
                jsonNodeMarshallContext,
                jsonNodeUnmarshallContext,
                spreadsheetIdToConverterProvider,
                spreadsheetIdToSpreadsheetComparatorProvider,
                spreadsheetIdToSpreadsheetFormatterProvider,
                spreadsheetIdToExpressionFunctionProvider,
                spreadsheetIdToSpreadsheetParserProvider,
                spreadsheetIdToStoreRepository,
                fileServer,
                server
        );
    }

    /**
     * Reports a resource was not found.
     */
    static void notFound(final HttpRequest request, final HttpResponse response) {
        response.setStatus(HttpStatusCode.NOT_FOUND.status());
        response.setEntity(HttpEntity.EMPTY);
    }

    /**
     * Private ctor use factory.
     */
    private SpreadsheetHttpServer(final UrlScheme scheme,
                                  final HostAddress host,
                                  final IpPort port,
                                  final Indentation indentation,
                                  final LineEnding lineEnding,
                                  final Supplier<LocalDateTime> now,
                                  final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                  final SpreadsheetMetadataStore metadataStore,
                                  final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                  final Function<BigDecimal, Fraction> fractioner,
                                  final JsonNodeMarshallContext jsonNodeMarshallContext,
                                  final JsonNodeUnmarshallContext jsonNodeUnmarshallContext,
                                  final Function<SpreadsheetId, ConverterProvider> spreadsheetIdToConverterProvider,
                                  final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdToSpreadsheetComparatorProvider,
                                  final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdToSpreadsheetFormatterProvider,
                                  final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider,
                                  final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdToSpreadsheetParserProvider,
                                  final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                  final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                  final Function<HttpHandler, HttpServer> server) {
        super();

        this.indentation = indentation;
        this.lineEnding = lineEnding;
        this.now = now;

        this.createMetadata = createMetadata;
        this.metadataStore = metadataStore;
        this.spreadsheetMetadataStamper = spreadsheetMetadataStamper;

        this.fractioner = fractioner;

        this.jsonNodeMarshallContext = jsonNodeMarshallContext;
        this.jsonNodeUnmarshallContext = jsonNodeUnmarshallContext;

        this.spreadsheetIdToConverterProvider = spreadsheetIdToConverterProvider;
        this.spreadsheetIdToSpreadsheetComparatorProvider = spreadsheetIdToSpreadsheetComparatorProvider;
        this.spreadsheetIdToSpreadsheetFormatterProvider = spreadsheetIdToSpreadsheetFormatterProvider;
        this.spreadsheetIdToExpressionFunctionProvider = spreadsheetIdToExpressionFunctionProvider;
        this.spreadsheetIdToSpreadsheetParserProvider = spreadsheetIdToSpreadsheetParserProvider;
        this.spreadsheetIdToStoreRepository = spreadsheetIdToStoreRepository;

        this.server = server.apply(
                HttpHandlers.stacktraceDumping(
                        HttpHandlers.headerCopy(
                                Sets.of(TRANSACTION_ID),
                                this::handler
                        ),
                        SpreadsheetThrowableTranslator.INSTANCE
                )
        );

        final AbsoluteUrl base = Url.absolute(scheme,
                AbsoluteUrl.NO_CREDENTIALS,
                host,
                Optional.of(port),
                UrlPath.ROOT,
                UrlQueryString.EMPTY,
                UrlFragment.EMPTY);
        final UrlPath api = UrlPath.parse(API);
        final UrlPath spreadsheet = UrlPath.parse(SPREADSHEET);

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
                .add(this.spreadsheetRouting(api).build(), this.spreadsheetHandler(base.setPath(api)))
                .add(this.spreadsheetEngineRouting(spreadsheet).build(), this.spreadsheetEngineHandler(base.setPath(spreadsheet)))
                .add(this.fileServerRouting().build(), this.fileServerHandler(UrlPath.ROOT, fileServer))
                .router();
    }

    /**
     * Asks the router for a target default to {@link #notFound(HttpRequest, HttpResponse)} and dispatches the
     * given request/response.
     */
    private void handler(final HttpRequest request, final HttpResponse response) {
        this.router.route(
                        request.routerParameters()
                ).orElse(SpreadsheetHttpServer::notFound)
                .handle(
                        request,
                        response
                );
    }

    // mappings.........................................................................................................

    private final static String API = "/api";
    private final static String SPREADSHEET = API + "/spreadsheet";
    private final static UrlPathName WILDCARD = UrlPathName.with("*");


    private HttpRequestAttributeRouting spreadsheetRouting(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
                .path(path);
    }

    private HttpHandler spreadsheetHandler(final AbsoluteUrl api) {
        return SpreadsheetHttpServerApiSpreadsheetHttpHandler.with(
                api,
                this.indentation,
                this.lineEnding,
                this.createMetadata,
                this.metadataStore,
                this.fractioner,
                this.spreadsheetIdToConverterProvider,
                this.spreadsheetIdToSpreadsheetComparatorProvider,
                this.spreadsheetIdToSpreadsheetFormatterProvider,
                this.spreadsheetIdToExpressionFunctionProvider,
                this.spreadsheetIdToSpreadsheetParserProvider,
                this.spreadsheetIdToStoreRepository,
                this.spreadsheetMetadataStamper,
                this.jsonNodeMarshallContext,
                this.jsonNodeUnmarshallContext,
                this.now
        );
    }

    /**
     * Require base url plus two more components to hold the service and its identifier, eg:
     * <pre>http://example.com/api-base/spreadsheet/spreadsheet-id-1234/cells/A1</pre>
     */
    private HttpRequestAttributeRouting spreadsheetEngineRouting(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
                .path(path.append(WILDCARD).append(WILDCARD));
    }

    private HttpHandler spreadsheetEngineHandler(final AbsoluteUrl url) {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.with(
                url,
                this.indentation,
                this.lineEnding,
                this.fractioner,
                this.createMetadata,
                this.metadataStore,
                this.spreadsheetIdToConverterProvider,
                this.spreadsheetIdToSpreadsheetComparatorProvider,
                this.spreadsheetIdToSpreadsheetFormatterProvider,
                this.spreadsheetIdToExpressionFunctionProvider,
                this.spreadsheetIdToSpreadsheetParserProvider,
                this.spreadsheetIdToStoreRepository,
                this.spreadsheetMetadataStamper,
                this.jsonNodeMarshallContext,
                this.jsonNodeUnmarshallContext,
                this.now
        );
    }

    private final Indentation indentation;

    private final LineEnding lineEnding;

    private final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata;

    private final SpreadsheetMetadataStore metadataStore;

    private final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper;

    private final Function<BigDecimal, Fraction> fractioner;

    private final Function<SpreadsheetId, ConverterProvider> spreadsheetIdToConverterProvider;

    private final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdToSpreadsheetComparatorProvider;

    private final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdToSpreadsheetFormatterProvider;

    private final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider;

    private final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdToSpreadsheetParserProvider;

    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository;

    private final Router<HttpRequestAttribute<?>, HttpHandler> router;

    private final JsonNodeMarshallContext jsonNodeMarshallContext;

    private final JsonNodeUnmarshallContext jsonNodeUnmarshallContext;

    private final Supplier<LocalDateTime> now;

    // files............................................................................................................

    /**
     * This routing must be last as it matches everything and tries to find a file.
     */
    private HttpRequestAttributeRouting fileServerRouting() {
        return HttpRequestAttributeRouting.empty()
                .path(UrlPath.parse("/*"));
    }

    private HttpHandler fileServerHandler(final UrlPath baseUrlPath,
                                          final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer) {
        return HttpHandlers.webFile(
                baseUrlPath.normalize(),
                fileServer
        );
    }

    // HttpServer.......................................................................................................

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void stop() {
        this.server.stop();
    }

    private final HttpServer server;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.server.toString();
    }
}
