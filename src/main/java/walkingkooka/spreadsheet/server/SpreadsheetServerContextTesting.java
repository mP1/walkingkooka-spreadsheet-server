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

import walkingkooka.plugin.HasProviderContextTesting;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContextSupplierTesting;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContextTesting;
import walkingkooka.spreadsheet.meta.HasSpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContextTesting;

import java.util.Locale;
import java.util.Optional;

public interface SpreadsheetServerContextTesting extends SpreadsheetContextSupplierTesting,
    SpreadsheetEnvironmentContextTesting,
    SpreadsheetMetadataContextTesting,
    HasProviderContextTesting,
    HasSpreadsheetMetadataTesting {

    default void createEmptySpreadsheetAndCheck(final SpreadsheetServerContext context,
                                                final Optional<Locale> locale,
                                                final SpreadsheetMetadata expected) {
        this.checkEquals(
            expected,
            context.createEmptySpreadsheet(
                locale
            ),
            "createEmptySpreadsheet " + locale.orElse(null)
        );
    }

    default void createEmptySpreadsheetAndCheck(final SpreadsheetServerContext context,
                                                final Optional<Locale> locale,
                                                final SpreadsheetContext expected) {
        this.checkEquals(
            expected,
            context.createEmptySpreadsheet(locale)
        );
    }
}
