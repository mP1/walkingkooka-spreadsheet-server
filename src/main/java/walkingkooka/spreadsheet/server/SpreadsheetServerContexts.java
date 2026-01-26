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

import walkingkooka.locale.LocaleContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContextSupplier;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.terminal.server.TerminalServerContext;

import java.util.function.Function;

public final class SpreadsheetServerContexts implements PublicStaticHelper {

    /**
     * {@see BasicSpreadsheetServerContext}
     */
    public static SpreadsheetServerContext basic(final SpreadsheetEngine spreadsheetEngine,
                                                 final SpreadsheetContextSupplier spreadsheetContextSupplier,
                                                 final SpreadsheetProvider spreadsheetProvider,
                                                 final Function<SpreadsheetContext, SpreadsheetEngineContext> spreadsheetEngineContextFactory,
                                                 final SpreadsheetEnvironmentContext spreadsheetEnvironmentContext,
                                                 final LocaleContext localeContext,
                                                 final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                                 final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                 final ProviderContext providerContext,
                                                 final TerminalServerContext terminalServerContext) {
        return BasicSpreadsheetServerContext.with(
            spreadsheetEngine,
            spreadsheetContextSupplier,
            spreadsheetProvider,
            spreadsheetEngineContextFactory,
            spreadsheetEnvironmentContext,
            localeContext,
            spreadsheetMetadataContext,
            hateosResourceHandlerContext,
            providerContext,
            terminalServerContext
        );
    }
    
    /**
     * {@see FakeSpreadsheetServerContext}
     */
    public static SpreadsheetServerContext fake() {
        return new FakeSpreadsheetServerContext();
    }

    /**
     * Stop creation
     */
    private SpreadsheetServerContexts() {
        throw new UnsupportedOperationException();
    }
}
