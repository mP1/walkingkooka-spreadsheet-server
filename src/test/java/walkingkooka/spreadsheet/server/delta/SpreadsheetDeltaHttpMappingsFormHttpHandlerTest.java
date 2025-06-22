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

import walkingkooka.net.http.server.HttpHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

public final class SpreadsheetDeltaHttpMappingsFormHttpHandlerTest implements HttpHandlerTesting<SpreadsheetDeltaHttpMappingsFormHttpHandler> {

    @Override
    public SpreadsheetDeltaHttpMappingsFormHttpHandler createHttpHandler() {
        return SpreadsheetDeltaHttpMappingsFormHttpHandler.with(
            SpreadsheetEngines.fake(),
            Indentation.SPACES2,
            LineEnding.NL,
            SpreadsheetEngineHateosResourceHandlerContexts.fake()
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetDeltaHttpMappingsFormHttpHandler> type() {
        return SpreadsheetDeltaHttpMappingsFormHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
