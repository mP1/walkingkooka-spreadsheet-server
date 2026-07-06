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
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerContext;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.server.comparator.SpreadsheetComparatorHateosResourceMappings;
import walkingkooka.spreadsheet.server.convert.ConverterHateosResourceMappings;
import walkingkooka.spreadsheet.server.currency.CurrencyHateosHandlerContext;
import walkingkooka.spreadsheet.server.currency.CurrencyHateosHandlerContexts;
import walkingkooka.spreadsheet.server.currency.CurrencyHateosResourceMappings;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResourceMappings;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResourceMappings;
import walkingkooka.spreadsheet.server.export.SpreadsheetExporterHateosResourceMappings;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterHateosResourceMappings;
import walkingkooka.spreadsheet.server.formhandler.FormHandlerHateosResourceMappings;
import walkingkooka.spreadsheet.server.function.ExpressionFunctionHateosResourceMappings;
import walkingkooka.spreadsheet.server.importer.SpreadsheetImporterHateosResourceMappings;
import walkingkooka.spreadsheet.server.locale.LocaleHateosHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleHateosHandlerContexts;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceMappings;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHttpHandler;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserHateosResourceMappings;
import walkingkooka.spreadsheet.server.validation.ValidationHateosResourceMappings;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link HttpHandler} which routes requests to all the API end points and file server using the given {@link SpreadsheetServerContext}.
 */
final class SpreadsheetHttpServerHttpHandler implements HttpHandler<SpreadsheetServerContext> {

    static SpreadsheetHttpServerHttpHandler with(final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                                 final SpreadsheetServerContext context) {
        return new SpreadsheetHttpServerHttpHandler(
            fileServer,
            context
        );
    }

    private SpreadsheetHttpServerHttpHandler(final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                             final SpreadsheetServerContext context) {
        this.spreadsheetProviderHateosHandlerContext = SpreadsheetProviderHateosHandlerContexts.basic(
            context, // SpreadsheetProvider
            context.providerContext(),
            context // HateosHandlerContext
        );

        this.currencyHateosHandlerContext = CurrencyHateosHandlerContexts.basic(
            context, // LocaleContext
            context //HateosHandlerContext
        );

        this.localeHateosHandlerContext = LocaleHateosHandlerContexts.basic(
            context, // LocaleContext
            context //HateosHandlerContext
        );

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler<SpreadsheetServerContext>>empty()
            .add(
                routing(SpreadsheetHttpServer.API_COMPARATOR),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.comparatorRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_CONVERTER),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.converterRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_CURRENCY),
                currencyHateosHandlerContextHttpHandler(
                    this.currencyRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_DATE_TIME_SYMBOLS),
                localeHateosHandlerContextHttpHandler(
                    this.dateTimeSymbolsRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_DECIMAL_NUMBER_SYMBOLS),
                localeHateosHandlerContextHttpHandler(
                    this.decimalNumberSymbolsRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_EXPORTER),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.exporterRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_FORMATTER),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.formatterRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_FORM_HANDLER),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.formHandlerRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_FUNCTION),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.functionRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_IMPORTER),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.importerRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_LOCALE),
                localeHateosHandlerContextHttpHandler(
                    this.localeRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_PARSER),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.parserRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_VALIDATOR),
                spreadsheetProviderHateosHandlerContextHttpHandler(
                    this.validatorRouter()
                )
            ).add(
                SPREADSHEET_ENGINE_ROUTING,
                SpreadsheetHttpServerSpreadsheetHttpHandler.INSTANCE
            ).add(
                routing(SpreadsheetHttpServer.API),
                SpreadsheetMetadataHttpHandler.INSTANCE
            ).add(
                FILE_SERVER_ROUTING,
                this.fileServerHttpHandler(
                    fileServer
                )
            ).router();
    }

    private static HttpHandler<SpreadsheetServerContext> currencyHateosHandlerContextHttpHandler(final Router<HttpRequestAttribute<?>, HttpHandler<CurrencyHateosHandlerContext>> router) {
        return SpreadsheetHttpServerHttpHandlerCurrencyHateosHandlerContext.with(
            httpHandler(router)
        );
    }

    private static HttpHandler<SpreadsheetServerContext> localeHateosHandlerContextHttpHandler(final Router<HttpRequestAttribute<?>, HttpHandler<LocaleHateosHandlerContext>> router) {
        return SpreadsheetHttpServerHttpHandlerLocaleHateosHandlerContext.with(
            httpHandler(router)
        );
    }

    private static HttpHandler<SpreadsheetServerContext> spreadsheetProviderHateosHandlerContextHttpHandler(final Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> router) {
        return SpreadsheetHttpServerHttpHandlerSpreadsheetProviderHateosHandlerContext.with(
            httpHandler(router)
        );
    }

    private static <C extends HttpHandlerContext> HttpHandler<C> httpHandler(final Router<HttpRequestAttribute<?>, HttpHandler<C>> router) {
        return (final HttpRequest request,
                final HttpResponse response,
                final C context) ->
            router.route(request.routerParameters())
                .orElse(SpreadsheetHttpServer::notFound)
                .handle(
                    request,
                    response,
                    context
                );
    }

    // mappings.........................................................................................................

    private static Map<HttpRequestAttribute<?>, Predicate<?>> routing(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
            .path(path)
            .build();
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> comparatorRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            SpreadsheetComparatorHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> converterRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            ConverterHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<CurrencyHateosHandlerContext>> currencyRouter() {
        return this.currencyHateosHandlerContextRouter(
            CurrencyHateosResourceMappings.currencyHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<LocaleHateosHandlerContext>> dateTimeSymbolsRouter() {
        return this.localeHateosHandlerContextRouter(
            DateTimeSymbolsHateosResourceMappings.localeHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<LocaleHateosHandlerContext>> decimalNumberSymbolsRouter() {
        return this.localeHateosHandlerContextRouter(
            DecimalNumberSymbolsHateosResourceMappings.localeHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> exporterRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            SpreadsheetExporterHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> formatterRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            SpreadsheetFormatterHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> formHandlerRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            FormHandlerHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> functionRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            ExpressionFunctionHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> importerRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            SpreadsheetImporterHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<LocaleHateosHandlerContext>> localeRouter() {
        return this.localeHateosHandlerContextRouter(
            LocaleHateosResourceMappings.localeHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> parserRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            SpreadsheetParserHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> validatorRouter() {
        return this.spreadsheetProviderHateosHandlerContextRouter(
            ValidationHateosResourceMappings.spreadsheetProviderHateosHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler<CurrencyHateosHandlerContext>> currencyHateosHandlerContextRouter(final HateosResourceMappings<?, ?, ?, ?, CurrencyHateosHandlerContext> mappings) {
        return this.hateosResourceMappingsRouter(
            mappings,
            this.currencyHateosHandlerContext
        );
    }

    private final CurrencyHateosHandlerContext currencyHateosHandlerContext;

    private Router<HttpRequestAttribute<?>, HttpHandler<LocaleHateosHandlerContext>> localeHateosHandlerContextRouter(final HateosResourceMappings<?, ?, ?, ?, LocaleHateosHandlerContext> mappings) {
        return this.hateosResourceMappingsRouter(
            mappings,
            this.localeHateosHandlerContext
        );
    }

    private final LocaleHateosHandlerContext localeHateosHandlerContext;

    private Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetProviderHateosHandlerContext>> spreadsheetProviderHateosHandlerContextRouter(final HateosResourceMappings<?, ?, ?, ?, SpreadsheetProviderHateosHandlerContext> mappings) {
        return this.hateosResourceMappingsRouter(
                mappings,
                this.spreadsheetProviderHateosHandlerContext
            );
    }

    private final SpreadsheetProviderHateosHandlerContext spreadsheetProviderHateosHandlerContext;

    private <X extends HateosHandlerContext> Router<HttpRequestAttribute<?>, HttpHandler<X>> hateosResourceMappingsRouter(final HateosResourceMappings<?, ?, ?, ?, X> mappings,
                                                                                                                          final X context) {
        return HateosResourceMappings.router(
            SpreadsheetHttpServer.API,
            Sets.of(mappings),
            context
        );
    }

    // /api/spreadsheet/*/*
    private final static Map<HttpRequestAttribute<?>, Predicate<?>> SPREADSHEET_ENGINE_ROUTING = HttpRequestAttributeRouting.empty()
        .path(
            SpreadsheetHttpServer.API_SPREADSHEET.append(UrlPathName.WILDCARD)
                .append(UrlPathName.WILDCARD)
        ).build();

    private final Router<HttpRequestAttribute<?>, HttpHandler<SpreadsheetServerContext>> router;

    // files............................................................................................................

    private final static Map<HttpRequestAttribute<?>, Predicate<?>> FILE_SERVER_ROUTING = routing(UrlPath.parse("/*"));

    private HttpHandler<SpreadsheetServerContext> fileServerHttpHandler(final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer) {
        return HttpHandlers.webFile(
            UrlPath.ROOT,
            fileServer
        );
    }

    // HttpHandler......................................................................................................

    /**
     * Asks the router for a target default to {@link SpreadsheetHttpServer#notFound(HttpRequest, HttpResponse, HttpHandlerContext)} and dispatches the
     * given request/response.
     */
    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetServerContext context) {
        this.router.route(
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
        return this.router.toString();
    }
}
