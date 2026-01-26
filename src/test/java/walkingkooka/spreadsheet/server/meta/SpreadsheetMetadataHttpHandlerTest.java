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
import walkingkooka.net.http.server.HttpHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.server.SpreadsheetServerContexts;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepositories;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetMetadataHttpHandlerTest implements HttpHandlerTesting<SpreadsheetMetadataHttpHandler>,
    SpreadsheetMetadataTesting {

    @Test
    public void testWithNullContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetMetadataHttpHandler.with(null)
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
            SpreadsheetServerContexts.basic(
                SpreadsheetEngines.fake(),
                (id) -> SpreadsheetStoreRepositories.fake(),
                SPREADSHEET_PROVIDER,
                SPREADSHEET_ENVIRONMENT_CONTEXT,
                LOCALE_CONTEXT,
                SpreadsheetMetadataContexts.fake(),
                HateosResourceHandlerContexts.fake(),
                PROVIDER_CONTEXT,
                TERMINAL_SERVER_CONTEXT
            )
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
