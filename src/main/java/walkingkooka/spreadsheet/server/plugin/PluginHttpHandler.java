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

import walkingkooka.Binary;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.server.SpreadsheetHttpServer;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A handler that routes all plugin API calls.
 */
public final class PluginHttpHandler implements HttpHandler {

    /**
     * Creates a new {@link PluginHttpHandler} handler.
     */
    public static PluginHttpHandler with(final AbsoluteUrl serverUrl,
                                         final Indentation indentation,
                                         final LineEnding lineEnding,
                                         final HateosResourceHandlerContext hateosResourceHandlerContext,
                                         final ProviderContext providerContext,
                                         final BiFunction<String, Binary, MediaType> contentTypeDetector) {
        return new PluginHttpHandler(
                Objects.requireNonNull(serverUrl, "serverUrl"),
                Objects.requireNonNull(indentation, "indentation"),
                Objects.requireNonNull(lineEnding, "lineEnding"),
                Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext"),
                Objects.requireNonNull(providerContext, "providerContext"),
                Objects.requireNonNull(contentTypeDetector, "contentTypeDetector")
        );
    }

    /**
     * Private ctor
     */
    private PluginHttpHandler(final AbsoluteUrl serverUrl,
                              final Indentation indentation,
                              final LineEnding lineEnding,
                              final HateosResourceHandlerContext hateosResourceHandlerContext,
                              final ProviderContext providerContext,
                              final BiFunction<String, Binary, MediaType> contentTypeDetector) {
        super();

        this.serverUrl = serverUrl;

        final Map<HttpRequestAttribute<?>, Predicate<?>> fileDownloadPredicate = HttpRequestAttributeRouting.empty()
                .method(HttpMethod.GET)
                .path(
                        UrlPath.parse(
                                serverUrl.path() + "/plugin/*/download"
                        )
                ).build();

        final HttpHandler fileDownloadHttpHandler = PluginFileDownloadHttpHandler.with(
                serverUrl.appendPathName(
                        Plugin.HATEOS_RESOURCE_NAME.toUrlPathName()
                ),
                providerContext.pluginStore(),
                contentTypeDetector
        );

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
                .add(
                        fileDownloadPredicate,
                        fileDownloadHttpHandler
                ).router()
                .then(
                        PluginHttpMappings.router(
                                serverUrl,
                                indentation,
                                lineEnding,
                                PluginHateosResourceHandlerContexts.basic(
                                        hateosResourceHandlerContext,
                                        providerContext
                                )
                        )
                );
    }

    // HttpHandler......................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");

        this.router.route(request.routerParameters())
                .orElse(SpreadsheetHttpServer::notFound)
                .handle(
                        request,
                        response
                );
    }

    private final Router<HttpRequestAttribute<?>, HttpHandler> router;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.serverUrl.toString();
    }

    private final AbsoluteUrl serverUrl;
}
