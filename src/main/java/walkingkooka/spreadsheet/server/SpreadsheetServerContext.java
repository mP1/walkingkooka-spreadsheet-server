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
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContextSupplier;
import walkingkooka.spreadsheet.environment.SpreadsheetEnvironmentContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataContext;
import walkingkooka.spreadsheet.net.HasSpreadsheetServerUrl;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;
import java.util.Optional;

/**
 * A {@link Context} that holds ALL available {@link SpreadsheetContext} for a single user.
 */
public interface SpreadsheetServerContext extends SpreadsheetContextSupplier,
    SpreadsheetMetadataContext,
    SpreadsheetEnvironmentContext,
    HasSpreadsheetServerUrl,
    LocaleContext,
    HateosResourceHandlerContext,
    SpreadsheetProvider,
    HasProviderContext {

    /**
     * Creates a new empty spreadsheet {@link SpreadsheetContext} for this given {@link EmailAddress user}.
     * The environment will have a new generated/unique {@link walkingkooka.spreadsheet.meta.SpreadsheetId}.
     */
    SpreadsheetContext createEmptySpreadsheet(final Optional<Locale> locale);

    /**
     * Creates a new {@link SpreadsheetContext}, cloning environment values. Mostly useful for new terminal shells.
     */
    SpreadsheetContext createSpreadsheetContext();

    // EnvironmentContext...............................................................................................

    /**
     * Only the {@link SpreadsheetEnvironmentContext} should be cloned not the entire {@link SpreadsheetServerContext}.
     */
    @Override
    SpreadsheetEnvironmentContext cloneEnvironment();

    @Override
    SpreadsheetServerContext setEnvironmentContext(final EnvironmentContext environmentContext);

    // JsonNodeMarshallUnmarshallContext................................................................................

    @Override
    SpreadsheetServerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor);

    @Override
    SpreadsheetServerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor);
}
