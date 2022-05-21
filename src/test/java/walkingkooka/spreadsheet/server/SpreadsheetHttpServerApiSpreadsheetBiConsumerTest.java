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
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.ExpressionEvaluationContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SpreadsheetHttpServerApiSpreadsheetBiConsumerTest extends SpreadsheetHttpServerTestCase2<SpreadsheetHttpServerApiSpreadsheetBiConsumer> {

    private final static LocalDateTime MODIFIED_DATE_TIME = LocalDateTime.of(2021, 7, 15, 20, 34);

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.handler(), this.baseUrl().toString());
    }

    // helper...........................................................................................................

    private SpreadsheetHttpServerApiSpreadsheetBiConsumer handler() {
        return SpreadsheetHttpServerApiSpreadsheetBiConsumer.with(
                this.baseUrl(),
                HateosContentType.json(JsonNodeUnmarshallContexts.fake(), JsonNodeMarshallContexts.fake()),
                Indentation.with("  "),
                LineEnding.NL,
                this::defaultMetadata,
                this::fractioner,
                this::idToFunctions,
                this::idToStoreRepository,
                this::spreadsheetMetadataStamper,
                this::contentTypeFactory
        );
    }

    private AbsoluteUrl baseUrl() {
        return Url.parseAbsolute("http://example.com/api/api2");
    }

    private Fraction fractioner(final BigDecimal value) {
        throw new UnsupportedOperationException();
    }

    private Function<FunctionExpressionName, ExpressionFunction<?, ExpressionEvaluationContext>> idToFunctions(final SpreadsheetId id) {
        throw new UnsupportedOperationException();
    }

    private SpreadsheetMetadata defaultMetadata(final Optional<Locale> locale) {
        throw new UnsupportedOperationException();
    }

    private SpreadsheetStoreRepository idToStoreRepository(final SpreadsheetId id) {
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
    public Class<SpreadsheetHttpServerApiSpreadsheetBiConsumer> type() {
        return SpreadsheetHttpServerApiSpreadsheetBiConsumer.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetHttpServer.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return BiConsumer.class.getSimpleName();
    }
}
