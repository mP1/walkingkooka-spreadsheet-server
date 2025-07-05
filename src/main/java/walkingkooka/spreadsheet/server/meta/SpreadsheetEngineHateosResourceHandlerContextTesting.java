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
import walkingkooka.environment.EnvironmentContextTesting2;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextTesting;
import walkingkooka.reflect.TypeNameTesting;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;

import static org.junit.jupiter.api.Assertions.assertThrows;

public interface SpreadsheetEngineHateosResourceHandlerContextTesting<C extends SpreadsheetMetadataHateosResourceHandlerContext> extends EnvironmentContextTesting2<C>,
    HateosResourceHandlerContextTesting<C>,
    TypeNameTesting<C> {

    @Test
    default void testSaveMetadataNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createContext().saveMetadata(null)
        );
    }

    @Test
    default void testSaveMetadataMissingSpreadsheetIdFails() {
        assertThrows(
            IllegalArgumentException.class,
            () -> this.createContext()
                .saveMetadata(
                    SpreadsheetMetadata.EMPTY
                )
        );
    }

    default void saveMetadataAndCheck(final SpreadsheetMetadataHateosResourceHandlerContext context,
                                      final SpreadsheetMetadata metadata,
                                      final SpreadsheetMetadata expected) {
        this.checkEquals(
            expected,
            context.saveMetadata(metadata),
            () -> "saveMetadata " + metadata
        );
    }

    @Test
    default void testHttpRouterNullSpreadsheetIdFails() {
        assertThrows(NullPointerException.class, () -> this.createContext().httpRouter(null));
    }

    @Test
    default void testStoreRepositoryNullSpreadsheetIdFails() {
        assertThrows(NullPointerException.class, () -> this.createContext().storeRepository(null));
    }

    // TypeNameTesting..................................................................................................

    @Override
    default String typeNameSuffix() {
        return SpreadsheetMetadataHateosResourceHandlerContext.class.getSimpleName();
    }
}
