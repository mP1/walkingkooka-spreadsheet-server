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

import walkingkooka.environment.EnvironmentContext;
import walkingkooka.locale.LocaleContext;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.function.Function;
import java.util.function.Supplier;

public final class SpreadsheetServerContexts implements PublicStaticHelper {

    /**
     * {@see BasicSpreadsheetServerContext}
     */
    public static SpreadsheetServerContext basic(final AbsoluteUrl serverUrl,
                                                 final Supplier<SpreadsheetStoreRepository> spreadsheetStoreRepository,
                                                 final SpreadsheetProvider spreadsheetProvider,
                                                 final Function<SpreadsheetContext, SpreadsheetEngineContext> spreadsheetEngineContextFunction,
                                                 final EnvironmentContext environmentContext,
                                                 final LocaleContext localeContext,
                                                 final SpreadsheetMetadataContext spreadsheetMetadataContext,
                                                 final HateosResourceHandlerContext hateosResourceHandlerContext,
                                                 final ProviderContext providerContext) {
        return BasicSpreadsheetServerContext.with(
            serverUrl,
            spreadsheetStoreRepository,
            spreadsheetProvider,
            spreadsheetEngineContextFunction,
            environmentContext,
            localeContext,
            spreadsheetMetadataContext,
            hateosResourceHandlerContext,
            providerContext
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
