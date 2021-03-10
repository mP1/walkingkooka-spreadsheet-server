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
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpRequestHttpResponseBiConsumers;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.ExpressionNumberContexts;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
                                             final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                             final Function<BigDecimal, Fraction> fractioner,
                                             final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions,
                                             final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                             final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                             final Function<BiConsumer<HttpRequest, HttpResponse>, HttpServer> server) {
        return new SpreadsheetHttpServer(scheme,
                host,
                port,
                createMetadata,
                fractioner,
                idToFunctions,
                idToStoreRepository,
                fileServer,
                server);
    }

    /**
     * Reports a resource was not found.
     */
    static void notFound(final HttpRequest request, final HttpResponse response) {
        response.setStatus(HttpStatusCode.NOT_FOUND.status());
        response.addEntity(HttpEntity.EMPTY);
    }

    /**
     * Private ctor use factory.
     */
    private SpreadsheetHttpServer(final UrlScheme scheme,
                                  final HostAddress host,
                                  final IpPort port,
                                  final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                  final Function<BigDecimal, Fraction> fractioner,
                                  final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions,
                                  final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                  final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                  final Function<BiConsumer<HttpRequest, HttpResponse>, HttpServer> server) {
        super();

        this.contentTypeJson = HateosContentType.json(
                JsonNodeUnmarshallContexts.basic(ExpressionNumberContexts.basic(ExpressionNumberKind.DEFAULT, MathContext.DECIMAL32)),
                JsonNodeMarshallContexts.basic()); // TODO https://github.com/mP1/walkingkooka-spreadsheet-server/issues/42
        this.createMetadata = createMetadata;
        this.fractioner = fractioner;
        this.idToFunctions = idToFunctions;
        this.idToStoreRepository = idToStoreRepository;
        this.server = server.apply(
                HttpRequestHttpResponseBiConsumers.stacktraceDumping(
                        HttpRequestHttpResponseBiConsumers.headerCopy(
                                Sets.of(TRANSACTION_ID),
                                this::handler
                        )
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

        this.router = RouteMappings.<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>>empty()
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
        this.router.route(request.routerParameters())
                .orElse(SpreadsheetHttpServer::notFound)
                .accept(request, response);
    }

    // mappings.........................................................................................................

    private final static String API = "/api";
    private final static String SPREADSHEET = API + "/spreadsheet";
    private final static UrlPathName WILDCARD = UrlPathName.with("*");


    private HttpRequestAttributeRouting spreadsheetRouting(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
                .path(path);
    }

    private BiConsumer<HttpRequest, HttpResponse> spreadsheetHandler(final AbsoluteUrl api) {
        return SpreadsheetHttpServerApiSpreadsheetBiConsumer.with(api,
                this.contentTypeJson,
                this.createMetadata,
                this.fractioner,
                this.idToFunctions,
                this.idToStoreRepository);
    }

    /**
     * Require base url plus two more components to hold the service and its identifier, eg:
     * <pre>http://example.com/api-base/spreadsheet/spreadsheet-id-1234/cells/A1</pre>
     */
    private HttpRequestAttributeRouting spreadsheetEngineRouting(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
                .path(path.append(WILDCARD).append(WILDCARD));
    }

    private BiConsumer<HttpRequest, HttpResponse> spreadsheetEngineHandler(final AbsoluteUrl url) {
        return SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer.with(url,
                this.contentTypeJson,
                this.fractioner,
                this.idToFunctions,
                this.idToStoreRepository);
    }

    private final HateosContentType contentTypeJson;
    private final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata;
    private final Function<BigDecimal, Fraction> fractioner;
    private final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> idToFunctions;
    private final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository;

    private final Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> router;

    // files............................................................................................................

    /**
     * This routing must be last as it matches everything and tries to find a file.
     */
    private HttpRequestAttributeRouting fileServerRouting() {
        return HttpRequestAttributeRouting.empty()
                .path(UrlPath.parse("/*"));
    }

    private BiConsumer<HttpRequest, HttpResponse> fileServerHandler(final UrlPath baseUrlPath,
                                                                    final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer) {
        return HttpRequestHttpResponseBiConsumers.webFile(baseUrlPath.normalize(),
                fileServer);
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
