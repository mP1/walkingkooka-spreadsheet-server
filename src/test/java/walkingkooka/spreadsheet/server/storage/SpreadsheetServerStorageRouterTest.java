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

package walkingkooka.spreadsheet.server.storage;

import org.junit.jupiter.api.Test;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.route.RouterTesting2;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosHandlerContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetServerStorageRouterTest implements RouterTesting2<SpreadsheetServerStorageRouter, HttpRequestAttribute<?>, HttpHandler<SpreadsheetEngineHateosHandlerContext>> {

    private final static String BASE_URL = "/api/spreadsheet/123";

    @Test
    public void testWithNullBasePathFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetServerStorageRouter.with(null)
        );
    }

    @Override
    public SpreadsheetServerStorageRouter createRouter() {
        return SpreadsheetServerStorageRouter.with(
            UrlPath.parse(BASE_URL)
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createRouter(),
            BASE_URL
        );
    }

    // class............................................................................................................

    @Override
    public Class<SpreadsheetServerStorageRouter> type() {
        return SpreadsheetServerStorageRouter.class;
    }
}
