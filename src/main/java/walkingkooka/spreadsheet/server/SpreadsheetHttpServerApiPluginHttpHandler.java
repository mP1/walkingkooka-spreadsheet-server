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

import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.server.plugin.PluginHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.plugin.PluginHttpMappings;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

/**
 * A handler that routes all plugin API calls.
 */
final class SpreadsheetHttpServerApiPluginHttpHandler implements HttpHandler {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiPluginHttpHandler} handler.
     */
    static SpreadsheetHttpServerApiPluginHttpHandler with(final AbsoluteUrl serverUrl,
                                                          final Indentation indentation,
                                                          final LineEnding lineEnding,
                                                          final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                          final ProviderContext providerContext) {
        return new SpreadsheetHttpServerApiPluginHttpHandler(
                serverUrl,
                indentation,
                lineEnding,
                hateosResourceHandlerContext,
                providerContext
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiPluginHttpHandler(final AbsoluteUrl serverUrl,
                                                      final Indentation indentation,
                                                      final LineEnding lineEnding,
                                                      final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                      final ProviderContext providerContext) {
        super();

        this.serverUrl = serverUrl;

        this.router = PluginHttpMappings.router(
                serverUrl,
                indentation,
                lineEnding,
                PluginHateosResourceHandlerContexts.basic(
                        hateosResourceHandlerContext,
                        providerContext
                )
        );
    }

    // HttpHandler......................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
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
