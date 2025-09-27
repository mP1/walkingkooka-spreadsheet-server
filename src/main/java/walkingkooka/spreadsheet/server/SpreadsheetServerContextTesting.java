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
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.plugin.HasProviderContextTesting;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContextTesting;
import walkingkooka.text.printer.TreePrintableTesting;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public interface SpreadsheetServerContextTesting<C extends SpreadsheetServerContext> extends SpreadsheetMetadataContextTesting<C>,
    HasProviderContextTesting,
    TreePrintableTesting {

    // serverUrl........................................................................................................

    default void serverUrlAndCheck(final C context,
                                   final AbsoluteUrl expected) {
        this.checkEquals(
            expected,
            context.serverUrl(),
            context::toString
        );
    }

    // createSpreadsheetContext.........................................................................................

    @Test
    default void testCreateSpreadsheetContextWithNullUserFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createContext()
                .createSpreadsheetContext(
                    null,
                    Optional.empty()
                )
        );
    }

    @Test
    default void testCreateSpreadsheetContextWithNullLocaleFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createContext()
                .createSpreadsheetContext(
                    EmailAddress.parse("user@example.com"),
                    null // locale
                )
        );
    }

    default void createSpreadsheetContextAndCheck(final C context,
                                                  final EmailAddress user,
                                                  final Optional<Locale> locale,
                                                  final SpreadsheetMetadata expected) {
        this.checkEquals(
            expected,
            context.createSpreadsheetContext(
                user,
                locale
            ),
            "createSpreadsheetContext " + user + " " + locale.orElse(null)
        );
    }

    default void createSpreadsheetContextAndCheck(final C context,
                                                  final EmailAddress user,
                                                  final Optional<Locale> locale,
                                                  final SpreadsheetContext expected) {
        this.checkEquals(
            expected,
            context.createSpreadsheetContext(
                user,
                locale
            )
        );
    }
    
    // spreadsheetContext...............................................................................................
    
    @Test
    default void testSpreadsheetContextWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> this.createContext()
                .spreadsheetContext(null)
        );
    }

    default void spreadsheetContextAndCheck(final C context,
                                            final SpreadsheetId id,
                                            final SpreadsheetContext expected) {
        this.checkEquals(
            expected,
            context.spreadsheetContext(id),
            () -> context + " spreadsheetContext " + id
        );
    }

    // class............................................................................................................

    @Override
    default String typeNameSuffix() {
        return SpreadsheetServerContext.class.getSimpleName();
    }
}
