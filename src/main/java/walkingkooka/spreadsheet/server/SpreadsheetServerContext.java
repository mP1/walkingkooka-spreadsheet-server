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

import walkingkooka.Context;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.locale.LocaleContext;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.HasProviderContext;
import walkingkooka.spreadsheet.HasSpreadsheetServerUrl;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.store.MissingStoreException;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
import java.util.Optional;

/**
 * A {@link Context} that holds available {@link SpreadsheetContext}.
 */
public interface SpreadsheetServerContext extends SpreadsheetMetadataContext,
    EnvironmentContext,
    HasSpreadsheetServerUrl,
    LocaleContext,
    HateosResourceHandlerContext,
    SpreadsheetProvider,
    HasProviderContext {

    @Override
    SpreadsheetServerContext setLocale(final Locale locale);

    /**
     * Creates a new {@link SpreadsheetContext} and spreadsheet.
     */
    SpreadsheetContext createSpreadsheetContext(final EmailAddress user,
                                                final Optional<Locale> locale);

    /**
     * Returns the {@link SpreadsheetContext} for the given {@link SpreadsheetId}.
     */
    Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId id);


    default SpreadsheetContext spreadsheetContextOrFail(final SpreadsheetId id) {
        return this.spreadsheetContext(id)
            .orElseThrow(() -> new MissingStoreException("Missing Spreadsheet " + id));
    }

    @Override
    SpreadsheetServerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor);

    @Override
    SpreadsheetServerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor);
}
