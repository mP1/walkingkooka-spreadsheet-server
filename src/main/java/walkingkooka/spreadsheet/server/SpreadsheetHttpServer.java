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
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.net.UrlPath;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaTypeDetector;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatus;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlers;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.HttpServer;
import walkingkooka.net.http.server.WebFile;
import walkingkooka.plugin.store.Plugin;
import walkingkooka.spreadsheet.compare.provider.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.export.provider.SpreadsheetExporterName;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.importer.provider.SpreadsheetImporterName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserName;
import walkingkooka.spreadsheet.server.convert.ConverterHateosResourceMappings;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResource;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResource;
import walkingkooka.spreadsheet.server.function.ExpressionFunctionHateosResourceMappings;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResource;
import walkingkooka.validation.form.provider.FormHandlerName;
import walkingkooka.validation.provider.ValidatorName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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
     * This request parameter will hold the {@link EmailAddress} of the user making the request.
     */
    public final static HttpRequestParameterName CURRENT_USER = HttpRequestParameterName.with("currentUser");

    /**
     * Creates a new {@link SpreadsheetHttpServer} using the config and the functions to create the actual {@link HttpServer}.
     */
    public static SpreadsheetHttpServer with(final MediaTypeDetector mediaTypeDetector,
                                             final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer,
                                             final Function<HttpHandler, HttpServer> server,
                                             final Function<Optional<EmailAddress>, SpreadsheetServerContext> spreadsheetServerContextFactory,
                                             final Function<HttpRequest, Optional<EmailAddress>> httpRequestUserExtractor) {
        return new SpreadsheetHttpServer(
            Objects.requireNonNull(mediaTypeDetector, "mediaTypeDetector"),
            Objects.requireNonNull(fileServer, "fileServer"),
            Objects.requireNonNull(server, "server"),
            Objects.requireNonNull(spreadsheetServerContextFactory, "spreadsheetServerContextFactory"),
            Objects.requireNonNull(httpRequestUserExtractor, "httpRequestUserExtractor")
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
                                  final Function<Optional<EmailAddress>, SpreadsheetServerContext> spreadsheetServerContextFactory,
                                  final Function<HttpRequest, Optional<EmailAddress>> httpRequestUserExtractor) {
        super();

        this.mediaTypeDetector = mediaTypeDetector;
        this.fileServer = fileServer;
        this.spreadsheetServerContextFactory = spreadsheetServerContextFactory;

        this.anonymousHttpHandler = SpreadsheetHttpServerHttpHandler.with(
            mediaTypeDetector,
            fileServer,
            spreadsheetServerContextFactory.apply(EnvironmentContext.ANONYMOUS)
        );

        this.server = server.apply(
            HttpHandlers.stacktraceDumping(
                HttpHandlers.headerCopy(
                    Sets.of(TRANSACTION_ID),
                    this::handle
                ),
                HttpHandlers.throwableTranslator()
            )
        );

        this.httpRequestUserExtractor = httpRequestUserExtractor;
    }

    private void handle(final HttpRequest request,
                        final HttpResponse response) {
        HttpHandler httpHandler;

        final Optional<EmailAddress> userOrAnonymous = this.httpRequestUserExtractor.apply(request);
        final EmailAddress user = userOrAnonymous.orElse(null);
        if (null == user) {
            httpHandler = this.anonymousHttpHandler;
        } else {
            httpHandler = this.userToHttpHandler.get(user);
        }

        if (null == httpHandler) {
            httpHandler = this.userToHttpHandler.get(user);
            if (null == httpHandler) {
                httpHandler = SpreadsheetHttpServerHttpHandler.with(
                    this.mediaTypeDetector,
                    this.fileServer,
                    this.spreadsheetServerContextFactory.apply(userOrAnonymous)
                );

                this.userToHttpHandler.put(
                    user,
                    httpHandler
                );
            }
        }

        httpHandler.handle(
            request,
            response
        );
    }

    private final MediaTypeDetector mediaTypeDetector;

    private final Function<UrlPath, Either<WebFile, HttpStatus>> fileServer;

    private final Function<Optional<EmailAddress>, SpreadsheetServerContext> spreadsheetServerContextFactory;

    /**
     * Extracts from a header or maps a cookie to a user {@link EmailAddress}.
     */
    private final Function<HttpRequest, Optional<EmailAddress>> httpRequestUserExtractor;

    /**
     * A {@link HttpHandler} that has a {@link SpreadsheetServerContext} with an anonymous user.
     */
    private final HttpHandler anonymousHttpHandler;

    /**
     * Maps authenticated users to a {@link HttpHandler} with a {@link SpreadsheetServerContext} with the environment
     * set to the user.
     */
    private final Map<EmailAddress, HttpHandler> userToHttpHandler = Maps.concurrent();

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
