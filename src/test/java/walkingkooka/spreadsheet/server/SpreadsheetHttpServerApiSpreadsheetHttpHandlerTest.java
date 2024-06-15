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
import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProviders;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProviders;
import walkingkooka.spreadsheet.format.SpreadsheetParserProvider;
import walkingkooka.spreadsheet.format.SpreadsheetParserProviders;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public final class SpreadsheetHttpServerApiSpreadsheetHttpHandlerTest extends SpreadsheetHttpServerTestCase2<SpreadsheetHttpServerApiSpreadsheetHttpHandler> {

    private final static LocalDateTime MODIFIED_DATE_TIME = LocalDateTime.of(2021, 7, 15, 20, 34);

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.handler(), this.baseUrl().toString());
    }

    // helper...........................................................................................................

    private SpreadsheetHttpServerApiSpreadsheetHttpHandler handler() {
        return SpreadsheetHttpServerApiSpreadsheetHttpHandler.with(
                this.baseUrl(),
                HateosContentType.json(JsonNodeUnmarshallContexts.fake(), JsonNodeMarshallContexts.fake()),
                Indentation.SPACES2,
                LineEnding.NL,
                this::defaultMetadata,
                SpreadsheetMetadataStores.fake(),
                this::fractioner,
                this::spreadsheetIdToSpreadsheetComparatorProvider,
                this::spreadsheetIdToSpreadsheetFormatterProvider,
                this::spreadsheetIdToExpressionFunctionProvider,
                this::spreadsheetIdToSpreadsheetParserProvider,
                this::spreadsheetIdToStoreRepository,
                this::spreadsheetMetadataStamper,
                this::contentTypeFactory,
                LocalDateTime::now
        );
    }

    private AbsoluteUrl baseUrl() {
        return Url.parseAbsolute("https://example.com/api/api2");
    }

    private Fraction fractioner(final BigDecimal value) {
        throw new UnsupportedOperationException();
    }

    private SpreadsheetComparatorProvider spreadsheetIdToSpreadsheetComparatorProvider(final SpreadsheetId id) {
        return SpreadsheetComparatorProviders.builtIn();
    }

    private SpreadsheetFormatterProvider spreadsheetIdToSpreadsheetFormatterProvider(final SpreadsheetId id) {
        return SpreadsheetFormatterProviders.spreadsheetFormatPattern();
    }

    private ExpressionFunctionProvider spreadsheetIdToExpressionFunctionProvider(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    private SpreadsheetParserProvider spreadsheetIdToSpreadsheetParserProvider(final SpreadsheetId id) {
        return SpreadsheetParserProviders.spreadsheetParsePattern();
    }

    private SpreadsheetMetadata defaultMetadata(final Optional<Locale> locale) {
        throw new UnsupportedOperationException();
    }

    private SpreadsheetStoreRepository spreadsheetIdToStoreRepository(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    private SpreadsheetMetadata spreadsheetMetadataStamper(final SpreadsheetMetadata metadata) {
        return metadata.set(
                SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME,
                MODIFIED_DATE_TIME
        );
    }

    private HateosContentType contentTypeFactory(final SpreadsheetMetadata metadata,
                                                 final SpreadsheetLabelStore labelStore) {
        return SpreadsheetContexts.jsonHateosContentType(metadata, labelStore);
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetHttpServerApiSpreadsheetHttpHandler> type() {
        return SpreadsheetHttpServerApiSpreadsheetHttpHandler.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetHttpServer.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return HttpHandler.class.getSimpleName();
    }
}
