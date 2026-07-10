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
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerContext;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttributes;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosHandlerContexts;

import java.util.Objects;
import java.util.Optional;

/**
 * A handler that routes all spreadsheet API calls.
 */
public final class SpreadsheetHttpServerSpreadsheetHttpHandler implements HttpHandler<SpreadsheetServerContext> {

    public final static SpreadsheetHttpServerSpreadsheetHttpHandler INSTANCE = new SpreadsheetHttpServerSpreadsheetHttpHandler();

    /**
     * Private ctor
     */
    private SpreadsheetHttpServerSpreadsheetHttpHandler() {
        super();
    }

    // Router...........................................................................................................

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetServerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(context, "context");

        final Optional<UrlPathName> spreadsheetPath = HttpRequestAttributes.pathComponent(SpreadsheetHttpServerSpreadsheetHttpHandler.SPREADSHEET_ID_PATH_COMPONENT + 1)
            .parameterValue(request);
        if (spreadsheetPath.isPresent()) {
            final Optional<UrlPathName> spreadsheetIdPath = HttpRequestAttributes.pathComponent(SpreadsheetHttpServerSpreadsheetHttpHandler.SPREADSHEET_ID_PATH_COMPONENT)
                .parameterValue(request);
            if (spreadsheetIdPath.isPresent()) {
                SpreadsheetId spreadsheetId;
                try {
                    spreadsheetId = SpreadsheetId.parse(
                        spreadsheetIdPath.get()
                            .value()
                    );
                } catch (final RuntimeException cause) {
                    spreadsheetId = null;

                    response.setVersion(request.protocolVersion());
                    response.setStatus(
                        HttpStatusCode.BAD_REQUEST.setMessage(
                            "Invalid " + SpreadsheetId.class.getSimpleName())
                    );
                    response.setEntity(HttpEntity.EMPTY);
                }
                if (null != spreadsheetId) {
                    boolean notFound = true;

                    final HttpHandler<HttpHandlerContext> spreadsheetIdHttpHandler = context.spreadsheetContextOrFail(spreadsheetId)
                        .httpRouter()
                        .route(request.routerParameters())
                        .orElse(null);
                    if(null != spreadsheetIdHttpHandler) {
                        final SpreadsheetContext spreadsheetContext = context.spreadsheetContext(spreadsheetId)
                                .orElse(null);
                        if(null != spreadsheetContext) {
                            notFound = false;

                            final SpreadsheetEngineContext spreadsheetEngineContext = spreadsheetContext.spreadsheetEngineContext();

                            final SpreadsheetEngine engine = SpreadsheetEngines.stamper(
                                SpreadsheetEngines.basic(),
                                metadata -> metadata.set(
                                    SpreadsheetMetadataPropertyName.AUDIT_INFO,
                                    spreadsheetEngineContext.refreshModifiedAuditInfo(
                                        metadata.getOrFail(SpreadsheetMetadataPropertyName.AUDIT_INFO)
                                    )
                                )
                            );

                            final SpreadsheetEngineHateosHandlerContext spreadsheetEngineContext2 = SpreadsheetEngineHateosHandlerContexts.basic(
                                engine,
                                context, // HateosHandlerContext,
                                spreadsheetEngineContext
                            ).setPreProcessor(
                                SpreadsheetMetadataHateosHandlerContexts.spreadsheetDeltaJsonCellLabelResolver(
                                    spreadsheetEngineContext.storeRepository()
                                        .labels()
                                )
                            );

                            spreadsheetIdHttpHandler.handle(
                                request,
                                response,
                                spreadsheetEngineContext2
                            );
                        }
                    }

                    if(notFound) {
                        SpreadsheetHttpServer.notFound(request, response, context);
                    }
                }
            } else {
                response.setVersion(request.protocolVersion());
                response.setStatus(
                    HttpStatusCode.BAD_REQUEST.setMessage(
                        "Missing " + SpreadsheetId.class.getSimpleName())
                );
                response.setEntity(HttpEntity.EMPTY);
            }
        } else {
            SpreadsheetHttpServer.notFound(
                request,
                response,
                context
            );
        }
    }

    private final static int SPREADSHEET_ID_PATH_COMPONENT = SpreadsheetHttpServer.API_SPREADSHEET
        .namesList()
        .size();

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.getClass()
            .getSimpleName();
    }
}
