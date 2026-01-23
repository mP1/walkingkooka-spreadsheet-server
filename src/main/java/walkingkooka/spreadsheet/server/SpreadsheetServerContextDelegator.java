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

import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContextDelegator;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContextDelegator;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviderDelegator;
import walkingkooka.text.LineEnding;

import java.util.Locale;
import java.util.Optional;

public interface SpreadsheetServerContextDelegator extends SpreadsheetServerContext,
    SpreadsheetProviderDelegator,
    SpreadsheetEnvironmentContextDelegator,
    LocaleContextDelegator,
    SpreadsheetMetadataContextDelegator,
    HateosResourceHandlerContextDelegator {
    
    // EnvironmentContextDelegator......................................................................................

    @Override
    default LineEnding lineEnding() {
        return this.spreadsheetEnvironmentContext()
            .lineEnding();
    }

    @Override
    default void setLineEnding(final LineEnding lineEnding) {
        this.spreadsheetEnvironmentContext()
            .setLineEnding(lineEnding);
    }

    @Override
    default Locale locale() {
        return this.spreadsheetEnvironmentContext()
            .locale();
    }

    @Override
    default void setLocale(final Locale locale) {
        this.spreadsheetEnvironmentContext()
            .setLocale(locale);
    }

    @Override
    default void setUser(final Optional<EmailAddress> user) {
        this.spreadsheetEnvironmentContext()
            .setUser(user);
    }

    @Override
    default SpreadsheetEnvironmentContext spreadsheetEnvironmentContext() {
        return this.spreadsheetServerContext();
    }

    // LocaleContextDelegator...........................................................................................

    @Override
    default LocaleContext localeContext() {
        return this.spreadsheetServerContext();
    }

    // HasProviderContext...............................................................................................

    @Override
    default ProviderContext providerContext() {
        return this.spreadsheetServerContext()
            .providerContext();
    }

    // HateosResourceHandlerContextDelegator............................................................................

    @Override
    default HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.spreadsheetServerContext();
    }

    // SpreadsheetMetadataContextDelegator..............................................................................

    @Override
    default SpreadsheetMetadataContext spreadsheetMetadataContext() {
        return this.spreadsheetServerContext();
    }

    // SpreadsheetProviderDelegator.....................................................................................

    @Override
    default SpreadsheetProvider spreadsheetProvider() {
        return this.spreadsheetServerContext();
    }

    // SpreadsheetServerContext.........................................................................................

    @Override
    default SpreadsheetContext createEmptySpreadsheet(final Optional<Locale> locale) {
        return this.spreadsheetServerContext()
            .createEmptySpreadsheet(locale);
    }

    @Override
    default AbsoluteUrl serverUrl() {
        return this.spreadsheetServerContext()
            .serverUrl();
    }

    @Override
    default Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
        return this.spreadsheetServerContext()
            .spreadsheetContext(id);
    }

    SpreadsheetServerContext spreadsheetServerContext();
}
