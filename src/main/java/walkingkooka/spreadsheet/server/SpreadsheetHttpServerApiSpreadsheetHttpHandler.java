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
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.json.JsonHttpHandlers;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosResourceMappings;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContext;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A handler that routes all spreadsheet API calls, outside {@link SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler}.
 */
final class SpreadsheetHttpServerApiSpreadsheetHttpHandler implements HttpHandler {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiSpreadsheetHttpHandler} handler.
     */
    static SpreadsheetHttpServerApiSpreadsheetHttpHandler with(final AbsoluteUrl serverUrl,
                                                               final Indentation indentation,
                                                               final LineEnding lineEnding,
                                                               final SpreadsheetMetadataStore metadataStore,
                                                               final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                               final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                               final JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext,
                                                               final Supplier<LocalDateTime> now,
                                                               final SpreadsheetProvider systemSpreadsheetProvider) {
        return new SpreadsheetHttpServerApiSpreadsheetHttpHandler(
                serverUrl,
                indentation,
                lineEnding,
                metadataStore,
                spreadsheetIdToSpreadsheetProvider,
                spreadsheetIdToStoreRepository,
                jsonNodeMarshallUnmarshallContext,
                now,
                systemSpreadsheetProvider
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetHttpHandler(final AbsoluteUrl serverUrl,
                                                           final Indentation indentation,
                                                           final LineEnding lineEnding,
                                                           final SpreadsheetMetadataStore metadataStore,
                                                           final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                           final JsonNodeMarshallUnmarshallContext jsonNodeMarshallUnmarshallContext,
                                                           final Supplier<LocalDateTime> now,
                                                           final SpreadsheetProvider systemSpreadsheetProvider) {
        super();

        this.serverUrl = serverUrl;

        final SpreadsheetMetadataHateosResourceHandlerContext context = SpreadsheetMetadataHateosResourceHandlerContexts.basic(
                serverUrl,
                indentation,
                lineEnding,
                metadataStore,
                spreadsheetIdToSpreadsheetProvider,
                spreadsheetIdToStoreRepository,
                jsonNodeMarshallUnmarshallContext,
                now,
                systemSpreadsheetProvider
        );

        this.context = context;

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
                .add(
                        patchRouterPredicates(),
                        this::patchRequestResponseHttpHandler
                ).router()
                .then(
                        SpreadsheetMetadataHateosResourceMappings.router(
                                serverUrl,
                                indentation,
                                lineEnding,
                                context
                        )
                );
    }

    private void patchRequestResponseHttpHandler(final HttpRequest request,
                                                 final HttpResponse response) {
        // PATCH
        // content type = JSON
        HttpHandlers.methodNotAllowed(
                HttpMethod.PATCH,
                HttpHandlers.contentType(
                        MediaType.APPLICATION_JSON,
                        JsonHttpHandlers.json(
                                (json) -> SpreadsheetMetadataHateosResourceMappings.patch(
                                        SpreadsheetId.parse(request.url().path().name().value()),
                                        this.context
                                ).apply(json),
                                SpreadsheetHttpServerApiSpreadsheetHttpHandler::patchPost
                        )
                )
        ).handle(
                request,
                response
        );
    }

    private static HttpEntity patchPost(final HttpEntity response) {
        return response.addHeader(
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
                SpreadsheetMetadata.class.getSimpleName()
        );
    }

    private final SpreadsheetMetadataHateosResourceHandlerContext context;

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

    // patch

    private static Map<HttpRequestAttribute<?>, Predicate<?>> patchRouterPredicates() {
        return HttpRequestAttributeRouting.empty()
                .method(HttpMethod.PATCH)
                .path(UrlPath.parse("/api/spreadsheet/*"))
                .build();
    }

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.serverUrl.toString();
    }

    private final AbsoluteUrl serverUrl;
}
