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

import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;

import java.util.Locale;
import java.util.Optional;

public class FakeSpreadsheetContext implements SpreadsheetContext {

    @Override
    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetMetadataStore metadataStore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetComparatorProvider comparatorProvider(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetFormatterProvider formatterProvider(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpressionFunctionProvider expressionFunctionProvider(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Router<HttpRequestAttribute<?>, HttpHandler> httpRouter(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetParserProvider parserProvider(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpreadsheetStoreRepository storeRepository(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }
}
