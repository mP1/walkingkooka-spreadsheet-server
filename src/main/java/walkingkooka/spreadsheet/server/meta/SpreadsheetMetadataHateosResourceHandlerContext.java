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

import walkingkooka.Context;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.function.Function;

/**
 * A {@link Context} for spreadsheets.
 */
public interface SpreadsheetMetadataHateosResourceHandlerContext extends HateosResourceHandlerContext,
    ProviderContext {

    @Override
    <T> SpreadsheetMetadataHateosResourceHandlerContext setEnvironmentValue(final EnvironmentValueName<T> name,
                                                                            final T value);

    @Override
    SpreadsheetMetadataHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor);

    /**
     * Saves the given {@link SpreadsheetMetadata}.
     */
    SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata);

    /**
     * Returns the {@link SpreadsheetMetadataStore}.
     */
    SpreadsheetMetadataStore metadataStore();

    /**
     * Returns a {@link Function} which knows the available {@link SpreadsheetProvider} for the given {@link SpreadsheetId}
     */
    SpreadsheetProvider spreadsheetProvider(final SpreadsheetId id);

    /**
     * A {@link Router} that can handle http requests for the given identified spreadsheet.
     */
    Router<HttpRequestAttribute<?>, HttpHandler> httpRouter(final SpreadsheetId id);

    /**
     * Factory that returns a {@link SpreadsheetStoreRepository} for a given {@link SpreadsheetId}
     */
    SpreadsheetStoreRepository storeRepository(final SpreadsheetId id);
}
