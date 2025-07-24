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

import org.junit.jupiter.api.Test;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMetadataHttpHandlerTest implements HttpHandlerTesting<SpreadsheetMetadataHttpHandler> {

    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://example.com");

    private final static LocaleContext LOCALE_CONTEXT = LocaleContexts.fake();

    private final static SpreadsheetProvider SYSTEM_PROVIDER_CONTEXT = SpreadsheetProviders.fake();

    private final static ProviderContext PROVIDER_CONTEXT = ProviderContexts.fake();

    private final static SpreadsheetMetadataStore METADATA_STORE = SpreadsheetMetadataStores.fake();

    private final static Function<SpreadsheetId, SpreadsheetProvider> SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static Function<SpreadsheetId, SpreadsheetStoreRepository> SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.fake();

    @Test
    public void testWithNullServerUrlFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                null,
                LOCALE_CONTEXT,
                SYSTEM_PROVIDER_CONTEXT,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullLocaleContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                null,
                SYSTEM_PROVIDER_CONTEXT,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSystemProviderContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                LOCALE_CONTEXT,
                null,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                LOCALE_CONTEXT,
                SYSTEM_PROVIDER_CONTEXT,
                null,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullMetadataStoreFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                LOCALE_CONTEXT,
                SYSTEM_PROVIDER_CONTEXT,
                PROVIDER_CONTEXT,
                null,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                LOCALE_CONTEXT,
                SYSTEM_PROVIDER_CONTEXT,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                null,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }


    @Test
    public void testWithNullSpreadsheetIdSpreadsheetStoreRepositoryFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                LOCALE_CONTEXT,
                SYSTEM_PROVIDER_CONTEXT,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                LOCALE_CONTEXT,
                SYSTEM_PROVIDER_CONTEXT,
                PROVIDER_CONTEXT,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                null
            )
        );
    }

    // HttpHandler......................................................................................................

    @Override
    public void testHandleWithNullResponseFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataHttpHandler createHttpHandler() {
        return SpreadsheetMetadataHttpHandler.with(
            SERVER_URL,
            LOCALE_CONTEXT,
            SYSTEM_PROVIDER_CONTEXT,
            PROVIDER_CONTEXT,
            METADATA_STORE,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            HATEOS_RESOURCE_HANDLER_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetMetadataHttpHandler> type() {
        return SpreadsheetMetadataHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
