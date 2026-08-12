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

import org.junit.jupiter.api.Test;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.predicate.Predicates;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.storage.StorageContexts;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetProviderHateosHandlerContextTest implements SpreadsheetProviderHateosHandlerContextTesting<BasicSpreadsheetProviderHateosHandlerContext> {

    private final static SpreadsheetProvider SPREADSHEET_PROVIDER = SpreadsheetProviders.fake();

    private final static ProviderContext PROVIDER_CONTEXT = ProviderContexts.basic(
        PluginStores.treeMap(),
        StorageContexts.basic(
            CONVERTER_LIKE,
            MEDIA_TYPE_DETECTOR,
            STORAGE,
            EnvironmentContexts.readOnly(
                Predicates.always(), // all values read-only
                ENVIRONMENT_CONTEXT
            )
        )
    );

    @Test
    public void testWithNullSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetProviderHateosHandlerContext.with(
                null,
                PROVIDER_CONTEXT,
                HATEOS_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetProviderHateosHandlerContext.with(
                SPREADSHEET_PROVIDER,
                null,
                HATEOS_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetProviderHateosHandlerContext.with(
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                null
            )
        );
    }

    @Override
    public void testSetEnvironmentContextWithNullFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetEnvironmentContextWithEqualEnvironmentContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetIndentationWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetLineEndingWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetLocaleWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testSetUserWithDifferentAndWatcher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicSpreadsheetProviderHateosHandlerContext createContext() {
        return BasicSpreadsheetProviderHateosHandlerContext.with(
            SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT.cloneEnvironment(),
            HATEOS_HANDLER_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetProviderHateosHandlerContext> type() {
        return BasicSpreadsheetProviderHateosHandlerContext.class;
    }
}
