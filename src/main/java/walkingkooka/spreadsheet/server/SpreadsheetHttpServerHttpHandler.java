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

import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerContext;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;

/**
 * A {@link HttpHandler} which routes requests to all the API end points and file server using the given {@link SpreadsheetServerContext}.
 */
final class SpreadsheetHttpServerHttpHandler implements HttpHandler<SpreadsheetServerContext> {

    static SpreadsheetHttpServerHttpHandler with(final HttpHandler<SpreadsheetServerContext> publicHttpHandler) {
        return new SpreadsheetHttpServerHttpHandler(publicHttpHandler);
    }

    private SpreadsheetHttpServerHttpHandler(final HttpHandler<SpreadsheetServerContext> publicHttpHandler) {
        super();
        this.publicHttpHandler = publicHttpHandler;
    }

    private final HttpHandler<SpreadsheetServerContext> publicHttpHandler;

    // HttpHandler......................................................................................................

    /**
     * Asks the router for a target default to {@link SpreadsheetHttpServer#notFound(HttpRequest, HttpResponse, HttpHandlerContext)} and dispatches the
     * given request/response.
     */
    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetServerContext context) {
        SpreadsheetHttpServerHttpHandlerRouterFactory.with(
                this.publicHttpHandler
            ).router
            .route(
                request.routerParameters()
            ).orElse(SpreadsheetHttpServer::notFound)
            .handle(
                request,
                response,
                context
            );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.getClass()
            .getSimpleName();
    }
}
