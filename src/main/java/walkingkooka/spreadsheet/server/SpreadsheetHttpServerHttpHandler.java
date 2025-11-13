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
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.UrlPath;
import walkingkooka.net.UrlPathName;
import walkingkooka.net.header.MediaTypeDetector;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.server.comparator.SpreadsheetComparatorHateosResourceMappings;
import walkingkooka.spreadsheet.server.convert.ConverterHateosResourceMappings;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResourceMappings;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResourceMappings;
import walkingkooka.spreadsheet.server.export.SpreadsheetExporterHateosResourceMappings;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterHateosResourceMappings;
import walkingkooka.spreadsheet.server.formhandler.FormHandlerHateosResourceMappings;
import walkingkooka.spreadsheet.server.function.ExpressionFunctionHateosResourceMappings;
import walkingkooka.spreadsheet.server.importer.SpreadsheetImporterHateosResourceMappings;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceMappings;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHttpHandler;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserHateosResourceMappings;
import walkingkooka.spreadsheet.server.plugin.PluginHttpHandler;
import walkingkooka.spreadsheet.server.validation.ValidationHateosResourceMappings;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link HttpHandler} which routes requests to all the API end points and file server using the given {@link SpreadsheetServerContext}.
 */
final class SpreadsheetHttpServerHttpHandler implements HttpHandler {

    static SpreadsheetHttpServerHttpHandler with(final MediaTypeDetector mediaTypeDetector,
                                                 final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                                 final SpreadsheetServerContext context) {
        return new SpreadsheetHttpServerHttpHandler(
            mediaTypeDetector,
            fileServer,
            context
        );
    }

    private SpreadsheetHttpServerHttpHandler(final MediaTypeDetector mediaTypeDetector,
                                             final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                             final SpreadsheetServerContext context) {
        this.mediaTypeDetector = mediaTypeDetector;

        this.spreadsheetProviderHateosResourceHandlerContext = SpreadsheetProviderHateosResourceHandlerContexts.basic(
            context, // SpreadsheetProvider
            context.providerContext(),
            context // HateosResourceHandlerContext
        );

        this.localeHateosResourceHandlerContext = LocaleHateosResourceHandlerContexts.basic(
            context, // LocaleContext
            context //HateosResourceHandlerContext
        );

        this.context = context;

        final AbsoluteUrl serverUrl = context.serverUrl();

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
            .add(
                routing(SpreadsheetHttpServer.API_COMPARATOR),
                httpHandler(this.comparatorRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_CONVERTER),
                httpHandler(this.converterRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_DATE_TIME_SYMBOLS),
                httpHandler(
                    this.dateTimeSymbolsRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_DECIMAL_NUMBER_SYMBOLS),
                httpHandler(
                    this.decimalNumberSymbolsRouter()
                )
            ).add(
                routing(SpreadsheetHttpServer.API_EXPORTER),
                httpHandler(this.exporterRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_FORMATTER),
                httpHandler(this.formatterRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_FORM_HANDLER),
                httpHandler(this.formHandlerRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_FUNCTION),
                httpHandler(this.functionRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_IMPORTER),
                httpHandler(this.importerRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_LOCALE),
                httpHandler(this.localeRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_PARSER),
                httpHandler(this.parserRouter())
            ).add(
                routing(SpreadsheetHttpServer.API_PLUGIN),
                this.pluginHttpHandler(
                    serverUrl.setPath(SpreadsheetHttpServer.API)
                )
            ).add(
                routing(SpreadsheetHttpServer.API_VALIDATOR),
                httpHandler(this.validatorRouter())
            ).add(
                SPREADSHEET_ENGINE_ROUTING,
                this.spreadsheetEngineHttpHandler()
            ).add(
                routing(SpreadsheetHttpServer.API),
                SpreadsheetMetadataHttpHandler.with(context)
            ).add(
                FILE_SERVER_ROUTING,
                this.fileServerHttpHandler(
                    fileServer
                )
            ).router();
    }

    private static HttpHandler httpHandler(final Router<HttpRequestAttribute<?>, HttpHandler> router) {
        return (HttpRequest request, HttpResponse response) ->
            router.route(request.routerParameters())
                .orElse(SpreadsheetHttpServer::notFound)
                .handle(
                    request,
                    response
                );
    }

    // mappings.........................................................................................................

    private static Map<HttpRequestAttribute<?>, Predicate<?>> routing(final UrlPath path) {
        return HttpRequestAttributeRouting.empty()
            .path(path)
            .build();
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> comparatorRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            SpreadsheetComparatorHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> converterRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            ConverterHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> dateTimeSymbolsRouter() {
        return this.localeHateosResourceHandlerContextRouter(
            DateTimeSymbolsHateosResourceMappings.localeHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> decimalNumberSymbolsRouter() {
        return this.localeHateosResourceHandlerContextRouter(
            DecimalNumberSymbolsHateosResourceMappings.localeHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> exporterRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            SpreadsheetExporterHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> formatterRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            SpreadsheetFormatterHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> formHandlerRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            FormHandlerHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> functionRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            ExpressionFunctionHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> importerRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            SpreadsheetImporterHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> localeRouter() {
        return this.localeHateosResourceHandlerContextRouter(
            LocaleHateosResourceMappings.localeHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> parserRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            SpreadsheetParserHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> validatorRouter() {
        return this.spreadsheetProviderHateosResourceHandlerContextRouter(
            ValidationHateosResourceMappings.spreadsheetProviderHateosResourceHandlerContext()
        );
    }

    private Router<HttpRequestAttribute<?>, HttpHandler> localeHateosResourceHandlerContextRouter(final HateosResourceMappings<?, ?, ?, ?, LocaleHateosResourceHandlerContext> mappings) {
        return this.hateosResourceMappingsRouter(
            mappings,
            this.localeHateosResourceHandlerContext
        );
    }

    private final LocaleHateosResourceHandlerContext localeHateosResourceHandlerContext;

    private Router<HttpRequestAttribute<?>, HttpHandler> spreadsheetProviderHateosResourceHandlerContextRouter(final HateosResourceMappings<?, ?, ?, ?, SpreadsheetProviderHateosResourceHandlerContext> mappings) {
        return this.hateosResourceMappingsRouter(
            mappings,
            this.spreadsheetProviderHateosResourceHandlerContext
        );
    }

    private final SpreadsheetProviderHateosResourceHandlerContext spreadsheetProviderHateosResourceHandlerContext;

    private <X extends HateosResourceHandlerContext> Router<HttpRequestAttribute<?>, HttpHandler> hateosResourceMappingsRouter(final HateosResourceMappings<?, ?, ?, ?, X> mappings,
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

    private HttpHandler spreadsheetEngineHttpHandler() {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.with(this.context);
    }

    private HttpHandler pluginHttpHandler(final AbsoluteUrl apiPlugin) {
        final SpreadsheetServerContext context = this.context;

        return PluginHttpHandler.with(
            apiPlugin,
            context, // HateosResourceHandlerContext
            context.providerContext(),
            this.mediaTypeDetector
        );
    }

    private final MediaTypeDetector mediaTypeDetector;

    private final Router<HttpRequestAttribute<?>, HttpHandler> router;

    private final SpreadsheetServerContext context;

    // files............................................................................................................

    private final static Map<HttpRequestAttribute<?>, Predicate<?>> FILE_SERVER_ROUTING = routing(UrlPath.parse("/*"));

    private HttpHandler fileServerHttpHandler(final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer) {
        return HttpHandlers.webFile(
            UrlPath.ROOT,
            fileServer
        );
    }

    // HttpHandler......................................................................................................

    /**
     * Asks the router for a target default to {@link SpreadsheetHttpServer#notFound(HttpRequest, HttpResponse)} and dispatches the
     * given request/response.
     */
    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
        this.router.route(
                request.routerParameters()
            ).orElse(SpreadsheetHttpServer::notFound)
            .handle(
                request,
                response
            );
    }

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.router.toString();
    }
}
