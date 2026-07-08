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

package walkingkooka.spreadsheet.server.meta;

import walkingkooka.Cast;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.json.JsonHttpHandlers;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.SpreadsheetHttpServer;
import walkingkooka.spreadsheet.server.SpreadsheetServerContext;
import walkingkooka.spreadsheet.server.net.SpreadsheetServerMediaTypes;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A handler that routes all spreadsheet API calls.
 */
public final class SpreadsheetMetadataHttpHandler implements HttpHandler<SpreadsheetServerContext> {

    /**
     * Singleton
     */
    public final static SpreadsheetMetadataHttpHandler INSTANCE = new SpreadsheetMetadataHttpHandler();

    /**
     * Private ctor
     */
    private SpreadsheetMetadataHttpHandler() {
        super();
    }

    private static Map<HttpRequestAttribute<?>, Predicate<?>> metadataPatchRouterPredicate() {
        return HttpRequestAttributeRouting.empty()
            .method(HttpMethod.PATCH)
            .path(API_SPREADSHEET_STAR)
            .build();
    }

    private final static UrlPath API_SPREADSHEET_STAR = SpreadsheetHttpServer.API_SPREADSHEET.append(
        UrlPathName.WILDCARD
    );

    private void metadataPatchHttpHandler(final HttpRequest request,
                                          final HttpResponse response,
                                          final SpreadsheetServerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");

        // PATCH
        // content type = JSON
        HttpHandlers.methodNotAllowed(
            HttpMethod.PATCH,
            HttpHandlers.contentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE,
                JsonHttpHandlers.json(
                    (final JsonNode json, final SpreadsheetServerContext c) -> SpreadsheetMetadataPatchFunction.with(
                        SpreadsheetId.parse(
                            request.url()
                                .path()
                                .name()
                                .value()
                        )
                    ).apply(
                        json,
                        context
                    ),
                    SpreadsheetMetadataHttpHandler::patchPost
                )
            )
        ).handle(
            request,
            response,
            context
        );
    }

    private static HttpEntity patchPost(final HttpEntity response,
                                        final SpreadsheetServerContext context) {
        return response.addHeader(
            HateosResourceMappings.X_CONTENT_TYPE_NAME,
            SpreadsheetMetadata.class.getSimpleName()
        );
    }

    // HttpHandler......................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetServerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");

        final SpreadsheetMetadataHateosHandlerContext spreadsheetMetadataHateosHandlerContext = SpreadsheetMetadataHateosHandlerContexts.basic(context);

        final Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetServerContext>> router  = RouteMappings.<HttpRequestAttribute<?>, HttpHandler<SpreadsheetServerContext>>empty()
            .add(
                metadataPatchRouterPredicate(),
                this::metadataPatchHttpHandler
            ).router()
            .then(
                Cast.to(
                    SpreadsheetMetadataHateosResourceHandlersRouter.with(
                        spreadsheetMetadataHateosHandlerContext
                    )
                )
            );

        router.route(request.routerParameters())
            .orElse(SpreadsheetHttpServer::notFound)
            .handle(
                request,
                response,
                spreadsheetMetadataHateosHandlerContext
            );
    }

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.getClass()
            .getSimpleName();
    }
}
