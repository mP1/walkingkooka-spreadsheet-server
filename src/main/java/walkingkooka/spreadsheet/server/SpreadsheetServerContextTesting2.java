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
import walkingkooka.spreadsheet.SpreadsheetContextSupplierTesting;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContextTesting2;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContextTesting2;

import static org.junit.jupiter.api.Assertions.assertThrows;

public interface SpreadsheetServerContextTesting2<C extends SpreadsheetServerContext> extends SpreadsheetServerContextTesting,
    SpreadsheetContextSupplierTesting<C>,
    SpreadsheetMetadataContextTesting2<C>,
    SpreadsheetEnvironmentContextTesting2<C> {

    // createEmptySpreadsheet...........................................................................................

    @Test
    default void testCreateEmptySpreadsheetWithNullLocaleFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createContext()
                .createEmptySpreadsheet(
                    null // locale
                )
        );
    }

    // SpreadsheetContextSupplierTesting................................................................................

    @Override
    default C createSpreadsheetContextSupplier() {
        return null;
    }

    // class............................................................................................................

    @Override
    default String typeNameSuffix() {
        return SpreadsheetServerContext.class.getSimpleName();
    }
}
