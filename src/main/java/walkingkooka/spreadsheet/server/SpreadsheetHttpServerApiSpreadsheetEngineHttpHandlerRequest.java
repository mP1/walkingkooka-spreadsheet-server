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

import walkingkooka.net.UrlPathName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttributes;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.spreadsheet.meta.SpreadsheetId;

import java.util.Optional;

/**
 * Handles dispatching a single request, extracting the spreadsheet id and then invoking the http service.
 */
final class SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest {

    static SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest with(final HttpRequest request,
                                                                            final HttpResponse response,
                                                                            final SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler handler) {
        return new SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest(
            request,
            response,
            handler
        );
    }

    private SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest(final HttpRequest request,
                                                                        final HttpResponse response,
                                                                        final SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler handler) {
        super();

        this.request = request;
        this.response = response;
        this.handler = handler;
    }

    void handle() {
        // verify spreadsheetId is present...
        final Optional<UrlPathName> path = this.path(SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.SPREADSHEET_ID_PATH_COMPONENT + 1);
        if (path.isPresent()) {
            this.handleSpreadsheetIdOrMissing();
        } else {
            SpreadsheetHttpServer.notFound(
                request,
                response,
                handler.context
            );
        }
    }

    private void handleSpreadsheetIdOrMissing() {
        final Optional<UrlPathName> path = path(SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.SPREADSHEET_ID_PATH_COMPONENT);
        if (path.isPresent()) {
            this.handleSpreadsheetId(path.get());
        } else {
            this.spreadsheetIdMissing();
        }
    }

    private Optional<UrlPathName> path(final int index) {
        return HttpRequestAttributes.pathComponent(index)
            .parameterValue(this.request);
    }

    private void handleSpreadsheetId(final UrlPathName pathName) {
        SpreadsheetId id;
        try {
            id = SpreadsheetId.parse(pathName.value());
        } catch (final RuntimeException cause) {
            id = null;
            this.setStatus(HttpStatusCode.BAD_REQUEST,
                "Invalid " + SpreadsheetId.class.getSimpleName());
        }
        if (null != id) {
            this.handler.router(id)
                .route(this.request.routerParameters())
                .orElse(SpreadsheetHttpServer::notFound)
                .handle(
                    this.request,
                    this.response,
                    this.handler.context
                );
        }
    }

    /**
     * Updates the response with a bad request that the spreadsheet id is missing.
     */
    private void spreadsheetIdMissing() {
        this.setStatus(HttpStatusCode.BAD_REQUEST,
            "Missing " + SpreadsheetId.class.getSimpleName());
    }

    private void setStatus(final HttpStatusCode status,
                           final String message) {
        final HttpResponse response = this.response;

        response.setVersion(this.request.protocolVersion());
        response.setStatus(status.setMessage(message));
        response.setEntity(HttpEntity.EMPTY);
    }

    private final HttpRequest request;
    private final HttpResponse response;
    private final SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler handler;

    // String...........................................................................................................

    @Override
    public String toString() {
        return this.request.toString();
    }
}
