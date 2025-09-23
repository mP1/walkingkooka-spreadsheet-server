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
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetContext;
import walkingkooka.spreadsheet.meta.SpreadsheetContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMetadataHttpHandlerTest implements HttpHandlerTesting<SpreadsheetMetadataHttpHandler> {

    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://example.com");

    private final static LocaleContext LOCALE_CONTEXT = LocaleContexts.jre(Locale.ENGLISH);

    private final static SpreadsheetProvider SYSTEM_PROVIDER = SpreadsheetProviders.fake();

    private final static SpreadsheetMetadataStore METADATA_STORE = SpreadsheetMetadataStores.treeMap(
        SpreadsheetMetadata.EMPTY.set(SpreadsheetMetadataPropertyName.LOCALE, LOCALE_CONTEXT.locale())
            .loadFromLocale(LOCALE_CONTEXT),
        LocalDateTime::now
    );

    private final static Function<SpreadsheetId, SpreadsheetProvider> SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static Function<SpreadsheetId, SpreadsheetStoreRepository> SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.fake();

    private final static SpreadsheetContext SPREADSHEET_CONTEXT = SpreadsheetContexts.fake();

    @Test
    public void testWithNullServerUrlFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                null,
                SYSTEM_PROVIDER,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSystemProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                null,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullMetadataStoreFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                SYSTEM_PROVIDER,
                null,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                SYSTEM_PROVIDER,
                METADATA_STORE,
                null,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_CONTEXT
            )
        );
    }


    @Test
    public void testWithNullSpreadsheetIdSpreadsheetStoreRepositoryFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                SYSTEM_PROVIDER,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                SYSTEM_PROVIDER,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                null,
                SPREADSHEET_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                SYSTEM_PROVIDER,
                METADATA_STORE,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
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
            SYSTEM_PROVIDER,
            METADATA_STORE,
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_CONTEXT
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
