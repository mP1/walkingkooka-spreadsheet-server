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
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetGlobalContext;
import walkingkooka.spreadsheet.SpreadsheetGlobalContexts;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMetadataHttpHandlerTest implements HttpHandlerTesting<SpreadsheetMetadataHttpHandler> {

    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://example.com");

    private final static Function<SpreadsheetId, SpreadsheetProvider> SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static Function<SpreadsheetId, SpreadsheetStoreRepository> SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION = (id) -> {
        throw new UnsupportedOperationException();
    };

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.fake();

    private final static SpreadsheetGlobalContext SPREADSHEET_GLOBAL_CONTEXT = SpreadsheetGlobalContexts.fake();

    @Test
    public void testWithNullServerUrlFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                null,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_GLOBAL_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetIdToSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                null,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_GLOBAL_CONTEXT
            )
        );
    }


    @Test
    public void testWithNullSpreadsheetIdSpreadsheetStoreRepositoryFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_GLOBAL_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
                SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
                SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
                null,
                SPREADSHEET_GLOBAL_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetGlobalContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(
                SERVER_URL,
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
            SPREADSHEET_ID_SPREADSHEET_PROVIDER_FUNCTION,
            SPREADSHEET_ID_SPREADSHEET_STORE_REPOSITORY_FUNCTION,
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_GLOBAL_CONTEXT
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
