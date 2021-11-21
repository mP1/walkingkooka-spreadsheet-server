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

import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.ExpressionFunction;
import walkingkooka.tree.expression.function.ExpressionFunctionContext;
import walkingkooka.tree.json.JsonNode;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class SpreadsheetContexts implements PublicStaticHelper {

    /**
     * {@see BasicSpreadsheetContext}
     */
    public static SpreadsheetContext basic(final AbsoluteUrl base,
                                           final HateosContentType contentType,
                                           final Function<BigDecimal, Fraction> fractioner,
                                           final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                           final Function<SpreadsheetId, Function<FunctionExpressionName, ExpressionFunction<?, ExpressionFunctionContext>>> spreadsheetIdFunctions,
                                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                           final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper) {
        return BasicSpreadsheetContext.with(
                base,
                contentType,
                fractioner,
                createMetadata,
                spreadsheetIdFunctions,
                spreadsheetIdToRepository,
                spreadsheetMetadataStamper
        );
    }

    /**
     * {@see FakeSpreadsheetContext}
     */
    public static FakeSpreadsheetContext fake() {
        return new FakeSpreadsheetContext();
    }

    /**
     * {@see SpreadsheetDeltaJsonCellLabelResolverBiFunction}
     */
    public static BiFunction<JsonNode, Class<?>, JsonNode> spreadsheetDeltaJsonCellLabelResolver(final SpreadsheetLabelStore store) {
        return SpreadsheetDeltaJsonCellLabelResolverBiFunction.with(store);
    }

    /**
     * Stop creation
     */
    private SpreadsheetContexts() {
        throw new UnsupportedOperationException();
    }
}
