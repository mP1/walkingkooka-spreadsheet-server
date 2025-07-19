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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.net.http.server.hateos.HateosHttpHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

public final class SpreadsheetDeltaHttpMappingsFormHateosHttpHandlerTest implements HateosHttpHandlerTesting<SpreadsheetDeltaHttpMappingsFormHateosHttpHandler,
    SpreadsheetEngineHateosResourceHandlerContext> {

    @Override
    public SpreadsheetDeltaHttpMappingsFormHateosHttpHandler createHandler() {
        return SpreadsheetDeltaHttpMappingsFormHateosHttpHandler.with(
            Indentation.SPACES2,
            LineEnding.NL
        );
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetDeltaHttpMappingsFormHateosHttpHandler> type() {
        return SpreadsheetDeltaHttpMappingsFormHateosHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
