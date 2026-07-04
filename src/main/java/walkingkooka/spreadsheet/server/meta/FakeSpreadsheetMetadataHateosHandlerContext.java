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

package walkingkooka.spreadsheet.server.meta;

import walkingkooka.environment.EnvironmentContext;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpHandlerContext;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.server.FakeSpreadsheetServerContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

public class FakeSpreadsheetMetadataHateosHandlerContext extends FakeSpreadsheetServerContext
    implements SpreadsheetMetadataHateosHandlerContext {

    // SpreadsheetMetadataHateosHandlerContext..................................................................

    @Override
    public Router<HttpRequestAttribute<?>, HttpHandler<HttpHandlerContext>> httpRouter(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    // HateosHandlerContext.....................................................................................

    @Override
    public MediaType contentType() {
        throw new UnsupportedOperationException();
    }

    // JsonNodeUnmarshallContext........................................................................................

    @Override
    public SpreadsheetMetadataHateosHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor) {
        throw new UnsupportedOperationException();
    }

    // EnvironmentContext...............................................................................................

    @Override
    public SpreadsheetMetadataHateosHandlerContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHateosHandlerContext setEnvironmentContext(final EnvironmentContext context) {
        throw new UnsupportedOperationException();
    }
}
