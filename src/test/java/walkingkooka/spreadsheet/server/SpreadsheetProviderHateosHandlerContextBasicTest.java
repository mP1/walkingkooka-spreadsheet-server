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
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.predicate.Predicates;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.storage.StorageContexts;
import walkingkooka.storage.StorageEnvironmentContexts;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetProviderHateosHandlerContextBasicTest implements SpreadsheetProviderHateosHandlerContextTesting<SpreadsheetProviderHateosHandlerContextBasic> {

    private final static SpreadsheetProvider SPREADSHEET_PROVIDER = SpreadsheetProviders.fake();

    private final static ProviderContext PROVIDER_CONTEXT = ProviderContexts.basic(
        StorageContexts.basic(
            CONVERTER_LIKE,
            MEDIA_TYPE_DETECTOR,
            STORAGE,
            StorageEnvironmentContexts.readOnly(
                Predicates.always(), // all values read-only
                STORAGE_ENVIRONMENT_CONTEXT.cloneEnvironment()
            )
        )
    );

    @Test
    public void testWithNullSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetProviderHateosHandlerContextBasic.with(
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
            () -> SpreadsheetProviderHateosHandlerContextBasic.with(
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
            () -> SpreadsheetProviderHateosHandlerContextBasic.with(
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
    public SpreadsheetProviderHateosHandlerContextBasic createContext() {
        return SpreadsheetProviderHateosHandlerContextBasic.with(
            SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT.cloneEnvironment(),
            HATEOS_HANDLER_CONTEXT
        );
    }

    // HasEnvironmentContext............................................................................................

    @Test
    @Override
    public void testEnvironmentContext() {
        final ProviderContext providerContext = ProviderContexts.fake();

        this.environmentContextAndCheck(
            SpreadsheetProviderHateosHandlerContextBasic.with(
                SPREADSHEET_PROVIDER,
                providerContext,
                HATEOS_HANDLER_CONTEXT
            ),
            providerContext
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetProviderHateosHandlerContextBasic> type() {
        return SpreadsheetProviderHateosHandlerContextBasic.class;
    }

    @Override
    public void testTypeNaming() {
        throw new UnsupportedOperationException();
    }
}
