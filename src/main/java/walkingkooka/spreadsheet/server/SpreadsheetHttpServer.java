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
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaTypeDetector;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestAttributeRouting;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.route.RouteMappings;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterName;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserName;
import walkingkooka.spreadsheet.server.comparator.SpreadsheetComparatorHateosResourceMappings;
import walkingkooka.spreadsheet.server.convert.ConverterHateosResourceMappings;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResource;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResourceMappings;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResource;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResourceMappings;
import walkingkooka.spreadsheet.server.export.SpreadsheetExporterHateosResourceMappings;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterHateosResourceMappings;
import walkingkooka.spreadsheet.server.formhandler.FormHandlerHateosResourceMappings;
import walkingkooka.spreadsheet.server.function.ExpressionFunctionHateosResourceMappings;
import walkingkooka.spreadsheet.server.importer.SpreadsheetImporterHateosResourceMappings;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResource;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceMappings;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHttpHandler;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserHateosResourceMappings;
import walkingkooka.spreadsheet.server.plugin.PluginHttpHandler;
import walkingkooka.spreadsheet.server.validation.ValidationHateosResourceMappings;
import walkingkooka.validation.form.provider.FormHandlerName;
import walkingkooka.validation.provider.ValidatorName;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A spreadsheet server that uses the given {@link HttpServer} and some other dependencies.
 */
public final class SpreadsheetHttpServer implements HttpServer {

    /**
     * This header contains the client transaction-id and is used to map responses with the original requests.
     */
    public final static HttpHeaderName<String> TRANSACTION_ID = HttpHeaderName.with("X-transaction-id")
        .stringValues();

    public final static UrlPath API = UrlPath.parse("/api");

    public final static UrlPath API_COMPARATOR = API.append(
        SpreadsheetComparatorName.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_CONVERTER = API.append(
        ConverterHateosResourceMappings.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_DATE_TIME_SYMBOLS = API.append(
        DateTimeSymbolsHateosResource.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_DECIMAL_NUMBER_SYMBOLS = API.append(
        DecimalNumberSymbolsHateosResource.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_EXPORTER = API.append(
        SpreadsheetExporterName.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_FORM_HANDLER = API.append(
        FormHandlerName.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_FORMATTER = API.append(
        SpreadsheetFormatterName.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_FUNCTION = API.append(
        ExpressionFunctionHateosResourceMappings.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_IMPORTER = API.append(
        SpreadsheetImporterName.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_LOCALE = API.append(
        LocaleHateosResource.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_PLUGIN = API.append(
        Plugin.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_PARSER = API.append(
        SpreadsheetParserName.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_SPREADSHEET = API.append(
        SpreadsheetMetadata.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    public final static UrlPath API_VALIDATOR = API.append(
        ValidatorName.HATEOS_RESOURCE_NAME.toUrlPathName()
    );

    /**
     * Creates a new {@link SpreadsheetHttpServer} using the config and the functions to create the actual {@link HttpServer}.
     */
    public static SpreadsheetHttpServer with(final MediaTypeDetector mediaTypeDetector,
                                             final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                             final Function<HttpHandler, HttpServer> server,
                                             final SpreadsheetServerContext context) {
        return new SpreadsheetHttpServer(
            Objects.requireNonNull(mediaTypeDetector, "mediaTypeDetector"),
            Objects.requireNonNull(fileServer, "fileServer"),
            Objects.requireNonNull(server, "server"),
            Objects.requireNonNull(context, "context")
        );
    }

    /**
     * Reports a resource was not found as an empty response body and {@link HttpStatusCode#NO_CONTENT}.
     * This hopefully leaves 404 responses for only invalid resource urls.
     */
    public static void notFound(final HttpRequest request,
                                final HttpResponse response) {
        response.setStatus(HttpStatusCode.NO_CONTENT.status());
        response.setEntity(HttpEntity.EMPTY);
    }

    /**
     * Private ctor use factory.
     */
    private SpreadsheetHttpServer(final MediaTypeDetector mediaTypeDetector,
                                  final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                  final Function<HttpHandler, HttpServer> server,
                                  final SpreadsheetServerContext context) {
        super();

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

        this.server = server.apply(
            HttpHandlers.stacktraceDumping(
                HttpHandlers.headerCopy(
                    Sets.of(TRANSACTION_ID),
                    this::handler
                ),
                HttpHandlers.throwableTranslator()
            )
        );

        this.context = context;

        final AbsoluteUrl serverUrl = context.serverUrl();

        this.router = RouteMappings.<HttpRequestAttribute<?>, HttpHandler>empty()
            .add(
                this.routing(API_COMPARATOR),
                httpHandler(this.comparatorRouter())
            ).add(
                this.routing(API_CONVERTER),
                httpHandler(this.converterRouter())
            ).add(
                this.routing(API_DATE_TIME_SYMBOLS),
                httpHandler(
                    this.dateTimeSymbolsRouter()
                )
            ).add(
                this.routing(API_DECIMAL_NUMBER_SYMBOLS),
                httpHandler(
                    this.decimalNumberSymbolsRouter()
                )
            ).add(
                this.routing(API_EXPORTER),
                httpHandler(this.exporterRouter())
            ).add(
                this.routing(API_FORMATTER),
                httpHandler(this.formatterRouter())
            ).add(
                this.routing(API_FORM_HANDLER),
                httpHandler(this.formHandlerRouter())
            ).add(
                this.routing(API_FUNCTION),
                httpHandler(this.functionRouter())
            ).add(
                this.routing(API_IMPORTER),
                httpHandler(this.importerRouter())
            ).add(
                this.routing(API_LOCALE),
                httpHandler(this.localeRouter())
            ).add(
                this.routing(API_PARSER),
                httpHandler(this.parserRouter())
            ).add(
                this.routing(API_PLUGIN),
                this.pluginHttpHandler(
                    serverUrl.setPath(API)
                )
            ).add(
                this.routing(API_VALIDATOR),
                httpHandler(this.validatorRouter())
            ).add(
                SPREADSHEET_ENGINE_ROUTING,
                this.spreadsheetEngineHttpHandler()
            ).add(
                this.routing(API),
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

    /**
     * Asks the router for a target default to {@link #notFound(HttpRequest, HttpResponse)} and dispatches the
     * given request/response.
     */
    private void handler(final HttpRequest request,
                         final HttpResponse response) {
        this.router.route(
                request.routerParameters()
            ).orElse(SpreadsheetHttpServer::notFound)
            .handle(
                request,
                response
            );
    }

    // mappings.........................................................................................................

    private Map<HttpRequestAttribute<?>, Predicate<?>> routing(final UrlPath path) {
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
            API,
            Sets.of(mappings),
            context
        );
    }

    // /api/spreadsheet/*/*
    private final static Map<HttpRequestAttribute<?>, Predicate<?>> SPREADSHEET_ENGINE_ROUTING = HttpRequestAttributeRouting.empty()
        .path(
            API_SPREADSHEET.append(UrlPathName.WILDCARD)
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

    private final static Map<HttpRequestAttribute<?>, Predicate<?>> FILE_SERVER_ROUTING = HttpRequestAttributeRouting.empty()
        .path(UrlPath.parse("/*"))
        .build();

    private HttpHandler fileServerHttpHandler(final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer) {
        return HttpHandlers.webFile(
            UrlPath.ROOT,
            fileServer
        );
    }

    // HttpServer.......................................................................................................

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void stop() {
        this.server.stop();
    }

    private final HttpServer server;

    // Object...........................................................................................................

    @Override
    public String toString() {
        return this.server.toString();
    }
}
