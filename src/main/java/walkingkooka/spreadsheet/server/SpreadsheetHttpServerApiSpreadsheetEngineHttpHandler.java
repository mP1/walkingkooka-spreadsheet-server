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
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetGlobalContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.function.Function;

/**
 * A handler that routes all spreadsheet API calls.
 */
final class SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler implements HttpHandler {

    /**
     * Creates a new {@link SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler} handler.
     */
    static SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler with(final AbsoluteUrl serverUrl,
                                                                     final SpreadsheetProvider systemSpreadsheetProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                     final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                                     final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                                     final SpreadsheetGlobalContext spreadsheetGlobalContext) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler(
            serverUrl,
            systemSpreadsheetProvider,
            spreadsheetIdToSpreadsheetProvider,
            spreadsheetIdToStoreRepository,
            hateosResourceHandlerContext,
            spreadsheetGlobalContext
        );
    }

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler(final AbsoluteUrl serverUrl,
                                                                 final SpreadsheetProvider systemSpreadsheetProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                 final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository,
                                                                 final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                                 final SpreadsheetGlobalContext spreadsheetGlobalContext) {
        super();

        this.serverUrl = serverUrl;

        this.systemSpreadsheetProvider = systemSpreadsheetProvider;

        this.spreadsheetIdToSpreadsheetProvider = spreadsheetIdToSpreadsheetProvider;

        this.spreadsheetIdToStoreRepository = spreadsheetIdToStoreRepository;

        this.spreadsheetIdPathComponent = serverUrl.path()
            .namesList()
            .size();

        this.hateosResourceHandlerContext = hateosResourceHandlerContext;

        this.spreadsheetGlobalContext = spreadsheetGlobalContext;
    }

    // Router...........................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response) {
        SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest.with(
            request,
            response,
            this
        ).handle();
    }

    // shared with SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest
    final int spreadsheetIdPathComponent;

    /**
     * Creates a {@link Router} for engine apis with base url=<code>/api/spreadsheet/$spreadsheetId$/</code> for the given spreadsheet.
     */
    Router<HttpRequestAttribute<?>, HttpHandler> router(final SpreadsheetId id) {
        return SpreadsheetMetadataHateosResourceHandlerContexts.basic(
            this.serverUrl,
            this.spreadsheetIdToSpreadsheetProvider,
            this.spreadsheetIdToStoreRepository,
            this.hateosResourceHandlerContext,
            this.spreadsheetGlobalContext,
            this.systemSpreadsheetProvider
        ).httpRouter(id);
    }

    private final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider;

    /**
     * A {@link Function} that returns a {@link SpreadsheetStoreRepository} for a given {@link SpreadsheetId}.
     */
    private final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToStoreRepository;

    private final HateosResourceHandlerContext hateosResourceHandlerContext;

    private final SpreadsheetGlobalContext spreadsheetGlobalContext;

    private final SpreadsheetProvider systemSpreadsheetProvider;

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.serverUrl.toString();
    }

    private final AbsoluteUrl serverUrl;
}
