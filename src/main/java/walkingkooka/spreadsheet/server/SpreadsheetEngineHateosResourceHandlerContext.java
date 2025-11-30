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
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.terminal.TerminalId;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionNumber;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
import java.util.Optional;

/**
 * A {@link HateosResourceHandlerContext} that includes {@link SpreadsheetEngineContext}.
 */
public interface SpreadsheetEngineHateosResourceHandlerContext extends HateosResourceHandlerContext,
    SpreadsheetEngineContext {

    @Override
    SpreadsheetEngineHateosResourceHandlerContext setSpreadsheetId(final SpreadsheetId spreadsheetId);

    @Override
    SpreadsheetEngineHateosResourceHandlerContext cloneEnvironment();

    @Override
    <T> SpreadsheetEngineHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                          final T value);

    @Override
    SpreadsheetEngineHateosResourceHandlerContext removeEnvironmentValue(final EnvironmentValueName<?> name);

    /**
     * Getter that returns a {@link SpreadsheetEngine}.
     */
    SpreadsheetEngine spreadsheetEngine();

    @Override
    SpreadsheetEngineHateosResourceHandlerContext setLineEnding(final LineEnding lineEnding);

    @Override
    SpreadsheetEngineHateosResourceHandlerContext setLocale(final Locale locale);

    @Override
    SpreadsheetEngineHateosResourceHandlerContext setUser(final Optional<EmailAddress> user);

    @Override
    SpreadsheetEngineHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor);

    @Override
    SpreadsheetEngineHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor);

    // HasMissingCellNumberValue........................................................................................

    @Override
    default ExpressionNumber missingCellNumberValue() {
        return this.spreadsheetMetadata()
            .missingCellNumberValue();
    }

    // TerminalServerContext............................................................................................

    @Override
    SpreadsheetEngineHateosResourceHandlerContext removeTerminalContext(final TerminalId id);
}
