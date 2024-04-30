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

package walkingkooka.spreadsheet.server.context;

import walkingkooka.Context;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponse;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparator;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@link Context} for spreadsheets.
 */
public interface SpreadsheetContext extends Context {

    static void checkId(final SpreadsheetId id) {
        Objects.requireNonNull(id, "id");
    }

    /**
     * Returns a {@link SpreadsheetMetadata} with necessary defaults allocating a new {@link SpreadsheetId}.
     */
    SpreadsheetMetadata createMetadata(final Optional<Locale> locale);

    /**
     * Saves the given {@link SpreadsheetMetadata}.
     */
    SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata);

    /**
     * Returns the {@link SpreadsheetMetadataStore}.
     */
    SpreadsheetMetadataStore metadataStore();

    /**
     * Returns a {@link Function} which knows the available {@link SpreadsheetComparator} by {@link SpreadsheetComparatorName}.
     */
    SpreadsheetComparatorProvider comparatorProvider(final SpreadsheetId id);

    /**
     * Returns a {@link ExpressionFunctionProvider} for the given {@link SpreadsheetId}.
     */
    ExpressionFunctionProvider expressionFunctionProvider(final SpreadsheetId id);

    /**
     * A {@link Router} that can handle http requests for the given identified spreadsheet.
     */
    Router<HttpRequestAttribute<?>, BiConsumer<HttpRequest, HttpResponse>> httpRouter(final SpreadsheetId id);

    /**
     * Factory that returns a {@link SpreadsheetStoreRepository} for a given {@link SpreadsheetId}
     */
    SpreadsheetStoreRepository storeRepository(final SpreadsheetId id);
}
