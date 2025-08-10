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

import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

/**
 * A {@link HateosResourceHandlerContext} that includes {@link SpreadsheetEngineContext}.
 */
public interface SpreadsheetEngineHateosResourceHandlerContext extends HateosResourceHandlerContext,
    SpreadsheetEngineContext {

    @Override
    <T> SpreadsheetEngineHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                          final T value);

    @Override
    SpreadsheetEngineHateosResourceHandlerContext removeEnvironmentValue(final EnvironmentValueName<?> name);

    /**
     * Getter that returns a {@link SpreadsheetEngine}.
     */
    SpreadsheetEngine spreadsheetEngine();

    /**
     * Returns a {@link SpreadsheetProvider} which may be queried to discover all available {@link walkingkooka.plugin.PluginInfoSetLike}.
     */
    SpreadsheetProvider systemSpreadsheetProvider();

    @Override
    SpreadsheetEngineHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor);

    // HasMissingCellNumberValue........................................................................................

    @Override
    default ExpressionNumber missingCellNumberValue() {
        return this.spreadsheetMetadata()
            .missingCellNumberValue();
    }
}
