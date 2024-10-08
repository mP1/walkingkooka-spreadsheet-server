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

import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterContext;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

public final class SpreadsheetHateosResourceHandlerContexts implements PublicStaticHelper {

    /**
     * {@see BasicSpreadsheetHateosResourceHandlerContext}
     */
    public static SpreadsheetHateosResourceHandlerContext basic(final JsonNodeMarshallContext marshallContext,
                                                                final JsonNodeUnmarshallContext unmarshallContext,
                                                                final SpreadsheetEngineContext engineContext,
                                                                final SpreadsheetFormatterContext formatterContext,
                                                                final SpreadsheetProvider systemSpreadsheetProvider) {
        return BasicSpreadsheetHateosResourceHandlerContext.with(
                marshallContext,
                unmarshallContext,
                engineContext,
                formatterContext,
                systemSpreadsheetProvider
        );
    }

    /**
     * {see FakeSpreadsheetHateosResourceHandlerContext}
     */
    public static SpreadsheetHateosResourceHandlerContext fake() {
        return new FakeSpreadsheetHateosResourceHandlerContext();
    }

    /**
     * Stop creation
     */
    private SpreadsheetHateosResourceHandlerContexts() {
        throw new UnsupportedOperationException();
    }
}
