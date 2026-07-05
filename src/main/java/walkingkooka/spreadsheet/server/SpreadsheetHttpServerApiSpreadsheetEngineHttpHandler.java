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

import walkingkooka.net.UrlPathName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerContext;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributes;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosHandlerContexts;

import java.util.Objects;
import java.util.Optional;

/**
 * A handler that routes all spreadsheet API calls.
 */
final class SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler implements HttpHandler<SpreadsheetServerContext> {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler} handler.
     */
    static SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler with(final SpreadsheetServerContext context) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler(
            Objects.requireNonNull(context, "context")
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler(final SpreadsheetServerContext context) {
        super();

        this.context = context;
    }

    // Router...........................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetServerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");

        final Optional<UrlPathName> path = HttpRequestAttributes.pathComponent(SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.SPREADSHEET_ID_PATH_COMPONENT + 1)
            .parameterValue(request);
        if (path.isPresent()) {
            final Optional<UrlPathName> path1 = HttpRequestAttributes.pathComponent(SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.SPREADSHEET_ID_PATH_COMPONENT)
                .parameterValue(request);
            if (path1.isPresent()) {
                SpreadsheetId id;
                try {
                    id = SpreadsheetId.parse(path1.get().value());
                } catch (final RuntimeException cause) {
                    id = null;

                    response.setVersion(request.protocolVersion());
                    response.setStatus(
                        HttpStatusCode.BAD_REQUEST.setMessage(
                            "Invalid " + SpreadsheetId.class.getSimpleName())
                    );
                    response.setEntity(HttpEntity.EMPTY);
                }
                if (null != id) {
                    this.router(id)
                        .route(request.routerParameters())
                        .orElse(SpreadsheetHttpServer::notFound)
                        .handle(
                            request,
                            response,
                            context
                        );
                }
            } else {
                response.setVersion(request.protocolVersion());
                response.setStatus(
                    HttpStatusCode.BAD_REQUEST.setMessage(
                        "Missing " + SpreadsheetId.class.getSimpleName())
                );
                response.setEntity(HttpEntity.EMPTY);
            }
        } else {
            SpreadsheetHttpServer.notFound(
                request,
                response,
                context
            );
        }
    }

    // shared with SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest
    final static int SPREADSHEET_ID_PATH_COMPONENT = SpreadsheetHttpServer.API_SPREADSHEET
        .namesList()
        .size();

    /**
     * Creates a {@link Router} for engine apis with base url=<code>/api/spreadsheet/$spreadsheetId$/</code> for the given spreadsheet.
     */
    Router<HttpRequestAttribute<?>, HttpHandler<HttpHandlerContext>> router(final SpreadsheetId id) {
        return SpreadsheetMetadataHateosHandlerContexts.basic(
            this.context
        ).httpRouter(id);
    }

    // @VisibleForTesting
    final SpreadsheetServerContext context;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.context.toString();
    }
}
