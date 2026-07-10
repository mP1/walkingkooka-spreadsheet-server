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

import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerContext;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosHandlerContexts;

import java.util.Objects;

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

        final SpreadsheetId spreadsheetId = SpreadsheetHttpServer.spreadsheetId(
            request,
            response,
            context
        ).orElse(null);
        if (null != spreadsheetId) {
            boolean notFound = true;

            final HttpHandler<HttpHandlerContext> spreadsheetIdHttpHandler = context.spreadsheetContextOrFail(spreadsheetId)
                .httpRouter()
                .route(request.routerParameters())
                .orElse(null);
            if (null != spreadsheetIdHttpHandler) {
                final SpreadsheetContext spreadsheetContext = context.spreadsheetContext(spreadsheetId)
                    .orElse(null);
                if (null != spreadsheetContext) {
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

            if (notFound) {
                SpreadsheetHttpServer.notFound(request, response, context);
            }
        }
    }

    // toString.........................................................................................................

    @Override
    public String toString() {
        return this.getClass()
            .getSimpleName();
    }
}
