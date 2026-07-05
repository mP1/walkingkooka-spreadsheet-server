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
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataHateosHandlerContexts;

final class SpreadsheetHttpServerHttpHandlerSpreadsheetMetadataHateosHandlerContext implements HttpHandler<SpreadsheetServerContext> {

    static SpreadsheetHttpServerHttpHandlerSpreadsheetMetadataHateosHandlerContext with(final HttpHandler<HttpHandlerContext> handler) {
        return new SpreadsheetHttpServerHttpHandlerSpreadsheetMetadataHateosHandlerContext(handler);
    }


    private SpreadsheetHttpServerHttpHandlerSpreadsheetMetadataHateosHandlerContext(final HttpHandler<HttpHandlerContext> handler) {
        super();

        this.handler = handler;
    }

    @Override
    public void handle(final HttpRequest request,
                       final HttpResponse response,
                       final SpreadsheetServerContext context) {
        this.handler.handle(
            request,
            response,
            SpreadsheetMetadataHateosHandlerContexts.basic(context)
        );
    }

    private final HttpHandler<HttpHandlerContext> handler;
}
