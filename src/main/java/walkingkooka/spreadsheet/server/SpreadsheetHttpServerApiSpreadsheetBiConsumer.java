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

import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.json.JsonHttpRequestHttpResponseBiConsumers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpRequestHttpResponseBiConsumers;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.server.context.hateos.SpreadsheetContextHttps;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A handler that routes all spreadsheet API calls, outside {@link SpreadsheetHttpServerApiSpreadsheetEngineBiConsumer}.
 */
final class SpreadsheetHttpServerApiSpreadsheetBiConsumer implements BiConsumer<HttpRequest, HttpResponse> {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiSpreadsheetBiConsumer} handler.
     */
    static SpreadsheetHttpServerApiSpreadsheetBiConsumer with(final AbsoluteUrl baseUrl,
                                                              final HateosContentType contentType,
                                                              final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                              final Function<BigDecimal, Fraction> fractioner,
                                                              final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> functions,
                                                              final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                                              final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        return new SpreadsheetHttpServerApiSpreadsheetBiConsumer(
                baseUrl,
                contentType,
                createMetadata,
                fractioner,
                functions,
                idToStoreRepository,
                spreadsheetMetadataStamper
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetBiConsumer(final AbsoluteUrl baseUrl,
                                                          final HateosContentType contentType,
                                                          final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                          final Function<BigDecimal, Fraction> fractioner,
                                                          final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> functions,
                                                          final Function<SpreadsheetId, SpreadsheetStoreRepository> idToStoreRepository,
                                                          final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        super();

        this.baseUrl = baseUrl;
        this.contentType = contentType;

        final SpreadsheetContext context = SpreadsheetContexts.basic(
                baseUrl,
                contentType,
                fractioner,
                createMetadata,
                functions,
                idToStoreRepository,
                spreadsheetMetadataStamper
        );

        this.context = context;

        this.hateosRouter = SpreadsheetContextHttps.router(
                baseUrl,
                contentType,
                SpreadsheetContextHttps.createAndSaveMetadata(context),
                SpreadsheetContextHttps.loadMetadata(context)
        );
    }

    // BiConsumer.......................................................................................................

    @Override
    public void accept(final HttpRequest request,
                       final HttpResponse response) {
        RouteMappings.<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>>empty()
                .add(
                        patchRouterPredicates(),
                        this::patchRequestResponseBiConsumer
                )
                .router()
                .then(this.hateosRouter)
                .route(request.routerParameters())
                .orElse(SpreadsheetHttpServer::notFound)
                .accept(request, response);
    }

    private static Map<HttpRequestAttribute<?>, Predicate<?>> patchRouterPredicates() {
        return HttpRequestAttributeRouting.empty()
                .method(HttpMethod.PATCH)
                .path(UrlPath.parse("/api/spreadsheet/*"))
                .build();
    }

    private void patchRequestResponseBiConsumer(final HttpRequest request,
                                                final HttpResponse response) {
        // PATCH
        // content type = JSON
        // Function<JsonNode, JsonNode> handler, Function<HttpEntity, HttpEntity> pos
        HttpRequestHttpResponseBiConsumers.methodNotAllowed(
                HttpMethod.PATCH,
                HttpRequestHttpResponseBiConsumers.contentType(
                        this.contentType.contentType(),
                        JsonHttpRequestHttpResponseBiConsumers.json(
                                (json) -> SpreadsheetContextHttps.patch(
                                        SpreadsheetId.parse(request.url().path().name().value()),
                                        context
                                ).apply(json),
                                SpreadsheetHttpServerApiSpreadsheetBiConsumer::patchPost
                        )
                )
        ).accept(request, response);
    }

    private final HateosContentType contentType;

    private static HttpEntity patchPost(final HttpEntity response) {
        return response.addHeader(
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
                SpreadsheetMetadata.class.getSimpleName()
        );
    }

    private final SpreadsheetContext context;

    /**
     * A {@link Function} that creates a default metadata with the given {@link Locale}.
     */
    private final Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> hateosRouter;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.baseUrl.toString();
    }

    private final AbsoluteUrl baseUrl;
}
