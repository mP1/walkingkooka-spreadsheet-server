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

import walkingkooka.locale.LocaleContext;
import walkingkooka.net.AbsoluteUrl;
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
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.server.SpreadsheetHttpServer;
import walkingkooka.spreadsheet.server.SpreadsheetServerMediaTypes;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A handler that routes all spreadsheet API calls.
 */
public final class SpreadsheetMetadataHttpHandler implements HttpHandler {

    /**
     * Creates a new {@link SpreadsheetMetadataHttpHandler} handler.
     */
    public static SpreadsheetMetadataHttpHandler with(final AbsoluteUrl serverUrl,
                                                      final LocaleContext localeContext,
                                                      final SpreadsheetProvider systemSpreadsheetProvider,
                                                      final ProviderContext providerContext,
                                                      final SpreadsheetMetadataStore metadataStore,
                                                      final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                      final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                      final HateosResourceHandlerContext hateosResourceHandlerContext) {
        return new SpreadsheetMetadataHttpHandler(
            Objects.requireNonNull(serverUrl, "serverUrl"),
            Objects.requireNonNull(localeContext, "localeContext"),
            Objects.requireNonNull(systemSpreadsheetProvider, "systemSpreadsheetProvider"),
            Objects.requireNonNull(providerContext, "providerContext"),
            Objects.requireNonNull(metadataStore, "metadataStore"),
            Objects.requireNonNull(spreadsheetIdToSpreadsheetProvider, "spreadsheetIdToSpreadsheetProvider"),
            Objects.requireNonNull(spreadsheetIdToStoreRepository, "spreadsheetIdToStoreRepository"),
            Objects.requireNonNull(hateosResourceHandlerContext, "hateosResourceHandlerContext")
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetMetadataHttpHandler(final AbsoluteUrl serverUrl,
                                           final LocaleContext localeContext,
                                           final SpreadsheetProvider systemSpreadsheetProvider,
                                           final ProviderContext providerContext,
                                           final SpreadsheetMetadataStore metadataStore,
                                           final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                           final HateosResourceHandlerContext hateosResourceHandlerContext) {
        super();

        this.serverUrl = serverUrl;

        final SpreadsheetMetadataHateosResourceHandlerContext context = SpreadsheetMetadataHateosResourceHandlerContexts.basic(
            serverUrl,
            localeContext,
            systemSpreadsheetProvider,
            providerContext,
            metadataStore,
            spreadsheetIdToSpreadsheetProvider,
            spreadsheetIdToStoreRepository,
            hateosResourceHandlerContext
        );

        this.context = context;

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
            .add(
                metadataPatchRouterPredicate(),
                this::metadataPatchHttpHandler
            ).router()
            .then(
                SpreadsheetMetadataHateosResourceHandlersRouter.with(
                    serverUrl.path(),
                    context
                )
            );
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
                                          final HttpResponse response) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");

        // PATCH
        // content type = JSON
        HttpHandlers.methodNotAllowed(
            HttpMethod.PATCH,
            HttpHandlers.contentType(
                SpreadsheetServerMediaTypes.CONTENT_TYPE,
                JsonHttpHandlers.json(
                    (json) -> SpreadsheetMetadataPatchFunction.with(
                        SpreadsheetId.parse(
                            request.url()
                                .path()
                                .name()
                                .value()
                        ),
                        this.context
                    ).apply(json),
                    SpreadsheetMetadataHttpHandler::patchPost
                )
            )
        ).handle(
            request,
            response
        );
    }

    private static HttpEntity patchPost(final HttpEntity response) {
        return response.addHeader(
            HateosResourceMappings.X_CONTENT_TYPE_NAME,
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

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.serverUrl.toString();
    }

    private final AbsoluteUrl serverUrl;
}
