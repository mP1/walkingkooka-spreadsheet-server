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
import walkingkooka.net.http.json.JsonHttpHandlers;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;
import walkingkooka.spreadsheet.server.context.SpreadsheetContextHttps;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
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
    static SpreadsheetHttpServerApiSpreadsheetHttpHandler with(final AbsoluteUrl baseUrl,
                                                               final HateosContentType contentType,
                                                               final Indentation indentation,
                                                               final LineEnding lineEnding,
                                                               final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                               final SpreadsheetMetadataStore metadataStore,
                                                               final Function<BigDecimal, Fraction> fractioner,
                                                               final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdSpreadsheetComparatorProvider,
                                                               final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdSpreadsheetFormatterProvider,
                                                               final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider,
                                                               final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdSpreadsheetParserProvider,
                                                               final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                               final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                               final BiFunction<SpreadsheetMetadata, SpreadsheetLabelStore, HateosContentType> contentTypeFactory,
                                                               final Supplier<LocalDateTime> now) {
        return new SpreadsheetHttpServerApiSpreadsheetHttpHandler(
                baseUrl,
                contentType,
                indentation,
                lineEnding,
                createMetadata,
                metadataStore,
                fractioner,
                spreadsheetIdSpreadsheetComparatorProvider,
                spreadsheetIdSpreadsheetFormatterProvider,
                spreadsheetIdToExpressionFunctionProvider,
                spreadsheetIdSpreadsheetParserProvider,
                spreadsheetIdToStoreRepository,
                spreadsheetMetadataStamper,
                contentTypeFactory,
                now
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetHttpHandler(final AbsoluteUrl baseUrl,
                                                           final HateosContentType contentType,
                                                           final Indentation indentation,
                                                           final LineEnding lineEnding,
                                                           final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                           final SpreadsheetMetadataStore metadataStore,
                                                           final Function<BigDecimal, Fraction> fractioner,
                                                           final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdSpreadsheetComparatorProvider,
                                                           final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdSpreadsheetFormatterProvider,
                                                           final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider,
                                                           final Function<SpreadsheetId, SpreadsheetParserProvider> spreadsheetIdSpreadsheetParserProvider,
                                                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                           final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                           final BiFunction<SpreadsheetMetadata, SpreadsheetLabelStore, HateosContentType> contentTypeFactory,
                                                           final Supplier<LocalDateTime> now) {
        super();

        this.baseUrl = baseUrl;
        this.contentType = contentType;

        final SpreadsheetContext context = SpreadsheetContexts.basic(
                baseUrl,
                contentType,
                indentation,
                lineEnding,
                fractioner,
                createMetadata,
                metadataStore,
                spreadsheetIdSpreadsheetComparatorProvider,
                spreadsheetIdSpreadsheetFormatterProvider,
                spreadsheetIdToExpressionFunctionProvider,
                spreadsheetIdSpreadsheetParserProvider,
                spreadsheetIdToStoreRepository,
                spreadsheetMetadataStamper,
                contentTypeFactory,
                now
        );

        this.context = context;

        this.hateosRouter = SpreadsheetContextHttps.router(
                baseUrl,
                contentType,
                indentation,
                lineEnding,
                SpreadsheetContextHttps.saveOrUpdateMetadata(context),
                SpreadsheetContextHttps.deleteMetadata(context),
                SpreadsheetContextHttps.loadMetadata(context)
        );
    }

    // HttpHandler......................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
        RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
                .add(
                        patchRouterPredicates(),
                        this::patchRequestResponseHttpHandler
                )
                .router()
                .then(this.hateosRouter)
                .route(request.routerParameters())
                .orElse(SpreadsheetHttpServer::notFound)
                .handle(
                        request,
                        response
                );
    }

    // patch

    private static Map<HttpRequestAttribute<?>, Predicate<?>> patchRouterPredicates() {
        return HttpRequestAttributeRouting.empty()
                .method(HttpMethod.PATCH)
                .path(UrlPath.parse("/api/spreadsheet/*"))
                .build();
    }

    private void patchRequestResponseHttpHandler(final HttpRequest request,
                                                 final HttpResponse response) {
        // PATCH
        // content type = JSON
        HttpHandlers.methodNotAllowed(
                HttpMethod.PATCH,
                HttpHandlers.contentType(
                        this.contentType.contentType(),
                        JsonHttpHandlers.json(
                                (json) -> SpreadsheetContextHttps.patch(
                                        SpreadsheetId.parse(request.url().path().name().value()),
                                        context
                                ).apply(json),
                                SpreadsheetHttpServerApiSpreadsheetHttpHandler::patchPost
                        )
                )
        ).handle(
                request,
                response
        );
    }

    private final HateosContentType contentType;

    private static HttpEntity patchPost(final HttpEntity response) {
        return response.addHeader(
                HateosResourceMapping.X_CONTENT_TYPE_NAME,
                SpreadsheetMetadata.class.getSimpleName()
        );
    }

    private final SpreadsheetContext context;

    private final Router<HttpRequestAttribute<?>, HttpHandler> hateosRouter;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.baseUrl.toString();
    }

    private final AbsoluteUrl baseUrl;
}
