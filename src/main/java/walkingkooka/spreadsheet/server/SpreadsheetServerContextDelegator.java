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

import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContextDelegator;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.locale.LocaleContext;
import walkingkooka.locale.LocaleContextDelegator;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContextDelegator;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContextDelegator;

import java.util.Locale;
import java.util.Optional;

public interface SpreadsheetServerContextDelegator extends SpreadsheetServerContext,
    EnvironmentContextDelegator,
    LocaleContextDelegator,
    SpreadsheetMetadataContextDelegator,
    HateosResourceHandlerContextDelegator {

    // SpreadsheetServerContext.........................................................................................

    @Override
    default AbsoluteUrl serverUrl() {
        return this.spreadsheetServerContext()
            .serverUrl();
    }

    // EnvironmentContextDelegator......................................................................................

    @Override
    default Locale locale() {
        return this.environmentContext()
            .locale();
    }

    @Override
    default SpreadsheetServerContext setLocale(final Locale locale) {
        this.environmentContext()
            .setLocale(locale);
        return this;
    }

    @Override
    default <T> SpreadsheetServerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                             final T value) {
        this.environmentContext()
            .setEnvironmentValue(
                name,
                value
            );
        return this;
    }

    @Override
    default SpreadsheetServerContext removeEnvironmentValue(final EnvironmentValueName<?> name) {
        this.environmentContext()
            .removeEnvironmentValue(name);
        return this;
    }

    @Override
    default EnvironmentContext environmentContext() {
        return this.spreadsheetServerContext();
    }

    // LocaleContextDelegator...........................................................................................

    @Override
    default LocaleContext localeContext() {
        return this.spreadsheetServerContext();
    }

    // SpreadsheetMetadataContextDelegator..............................................................................

    @Override
    default SpreadsheetMetadataContext spreadsheetMetadataContext() {
        return this.spreadsheetServerContext();
    }

    @Override
    default SpreadsheetContext createSpreadsheetContext(final EmailAddress user,
                                                        final Optional<Locale> locale) {
        return this.spreadsheetServerContext()
            .createSpreadsheetContext(
                user,
                locale
            );
    }

    @Override
    default Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id) {
        return this.spreadsheetServerContext()
            .spreadsheetContext(id);
    }

    @Override
    default ProviderContext providerContext() {
        return this.spreadsheetServerContext()
            .providerContext();
    }

    SpreadsheetServerContext spreadsheetServerContext();

    // HateosResourceHandlerContextDelegator............................................................................

    @Override
    default HateosResourceHandlerContext hateosResourceHandlerContext() {
        return this.spreadsheetServerContext();
    }
}
