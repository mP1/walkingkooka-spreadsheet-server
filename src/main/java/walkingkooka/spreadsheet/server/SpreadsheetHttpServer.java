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
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlFragment;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.UrlQueryString;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaTypeDetector;
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
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHttpHandler;
import walkingkooka.spreadsheet.server.plugin.PluginHttpHandler;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.CharSequences;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

import java.util.Objects;
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

    public final static UrlPath API = walkingkooka.net.UrlPath.parse("/api");

    public final static UrlPath API_SPREADSHEET = API.append(
            SpreadsheetMetadata.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_PLUGIN = API.append(
            Plugin.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    /**
     * Creates a new {@link SpreadsheetHttpServer} using the config and the functions to create the actual {@link HttpServer}.
     */
    public static SpreadsheetHttpServer with(final AbsoluteUrl serverUrl,
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
        return new SpreadsheetHttpServer(
                checkServerUrl(serverUrl),
                Objects.requireNonNull(indentation, "indentation"),
                Objects.requireNonNull(lineEnding, "lineEnding"),
                Objects.requireNonNull(mediaTypeDetector, "mediaTypeDetector"),
                Objects.requireNonNull(systemSpreadsheetProvider, "systemSpreadsheetProvider"),
                Objects.requireNonNull(providerContext, "providerContext"),
                Objects.requireNonNull(metadataStore, "metadataStore"),
                Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
                Objects.requireNonNull(spreadsheetIdToSpreadsheetProvider, "spreadsheetIdToSpreadsheetProvider"),
                Objects.requireNonNull(spreadsheetIdToStoreRepository, "spreadsheetIdToStoreRepository"),
                Objects.requireNonNull(fileServer, "fileServer"),
                Objects.requireNonNull(server, "server")
        );
    }

    private static AbsoluteUrl checkServerUrl(final AbsoluteUrl serverUrl) {
        Objects.requireNonNull(serverUrl, "serverUrl");

        if (false == serverUrl.path().equals(walkingkooka.net.UrlPath.EMPTY)) {
            throw checkServerUrlFail("path", serverUrl);
        }
        if (false == serverUrl.query().equals(UrlQueryString.EMPTY)) {
            throw checkServerUrlFail("query string", serverUrl);
        }
        if (false == serverUrl.urlFragment().equals(UrlFragment.EMPTY)) {
            throw checkServerUrlFail("fragment", serverUrl);
        }
        return serverUrl;
    }

    private static IllegalArgumentException checkServerUrlFail(final String property,
                                                               final AbsoluteUrl serverUrl) {
        return new IllegalArgumentException(
                "Url must not have " +
                        property +
                        " got " +
                        CharSequences.quoteAndEscape(
                                serverUrl.toString()
                        )
        );
    }

    /**
     * Reports a resource was not found.
     */
    public static void notFound(final HttpRequest request, final HttpResponse response) {
        response.setStatus(HttpStatusCode.NOT_FOUND.status());
        response.setEntity(HttpEntity.EMPTY);
    }

    /**
     * Private ctor use factory.
     */
    private SpreadsheetHttpServer(final AbsoluteUrl serverUrl,
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
        super();

        this.indentation = indentation;
        this.lineEnding = lineEnding;

        this.mediaTypeDetector = mediaTypeDetector;

        this.systemSpreadsheetProvider = systemSpreadsheetProvider;

        this.providerContext = providerContext;

        this.metadataStore = metadataStore;

        this.hateosResourceHandlerContext = hateosResourceHandlerContext;

        this.spreadsheetIdToSpreadsheetProvider = spreadsheetIdToSpreadsheetProvider;
 
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

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
                .add(
                        this.routing(API_PLUGIN)
                                .build(),
                        this.pluginHttpHandler(
                                serverUrl.setPath(API)
                        )
                ).add(
                        this.routing(API)
                                .build(),
                        this.spreadsheetMetadataHttpHandler(
                                serverUrl.setPath(API)
                        )
                ).add(
                        this.spreadsheetEngineRouting(API_SPREADSHEET)
                                .build(),
                        this.spreadsheetEngineHttpHandler(
                                serverUrl.setPath(API_SPREADSHEET)
                        )
                ).add(
                        this.fileServerRouting()
                                .build(),
                        this.fileServerHttpHandler(
                                UrlPath.ROOT,
                                fileServer
                        )
                ).router();
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

    private HttpRequestAttributeRouting routing(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
                .path(path);
    }

    private HttpHandler spreadsheetMetadataHttpHandler(final AbsoluteUrl api) {
        return SpreadsheetMetadataHttpHandler.with(
                api,
                this.indentation,
                this.lineEnding,
                this.systemSpreadsheetProvider,
                this.providerContext,
                this.metadataStore,
                this.spreadsheetIdToSpreadsheetProvider,
                this.spreadsheetIdToStoreRepository,
                this.hateosResourceHandlerContext
        );
    }

    /**
     * Require base url plus two more components to hold the service and its identifier, eg:
     * <pre>http://example.com/api-base/spreadsheet/spreadsheet-id-1234/cells/A1</pre>
     */
    private HttpRequestAttributeRouting spreadsheetEngineRouting(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
                .path(
                        path.append(UrlPathName.WILDCARD)
                                .append(UrlPathName.WILDCARD));
    }

    private HttpHandler spreadsheetEngineHttpHandler(final AbsoluteUrl url) {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.with(
                url,
                this.indentation,
                this.lineEnding,
                this.systemSpreadsheetProvider,
                this.providerContext,
                this.metadataStore,
                this.spreadsheetIdToSpreadsheetProvider,
                this.spreadsheetIdToStoreRepository,
                this.hateosResourceHandlerContext
        );
    }

    private HttpHandler pluginHttpHandler(final AbsoluteUrl apiPlugin) {
        return PluginHttpHandler.with(
                apiPlugin,
                this.indentation,
                this.lineEnding,
                this.hateosResourceHandlerContext,
                this.providerContext,
                this.mediaTypeDetector
        );
    }

    private final Indentation indentation;

    private final LineEnding lineEnding;

    private final MediaTypeDetector mediaTypeDetector;

    private final SpreadsheetProvider systemSpreadsheetProvider;

    private final ProviderContext providerContext;

    private final SpreadsheetMetadataStore metadataStore;

    private final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider;

    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository;

    private final Router<HttpRequestAttribute<?>, HttpHandler> router;

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    // files............................................................................................................

    /**
     * This routing must be last as it matches everything and tries to find a file.
     */
    private HttpRequestAttributeRouting fileServerRouting() {
        return HttpRequestAttributeRouting.empty()
                .path(UrlPath.parse("/*"));
    }

    private HttpHandler fileServerHttpHandler(final UrlPath baseUrlPath,
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
