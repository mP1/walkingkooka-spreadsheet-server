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
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorProvider;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterProvider;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionProvider;
import walkingkooka.tree.json.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SpreadsheetContexts implements PublicStaticHelper {

    /**
     * {@see BasicSpreadsheetContext}
     */
    public static SpreadsheetContext basic(final AbsoluteUrl base,
                                           final HateosContentType contentType,
                                           final Indentation indentation,
                                           final LineEnding lineEnding,
                                           final Function<BigDecimal, Fraction> fractioner,
                                           final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                           final SpreadsheetMetadataStore metadataStore,
                                           final Function<SpreadsheetId, SpreadsheetComparatorProvider> spreadsheetIdToComparatorProvider,
                                           final Function<SpreadsheetId, SpreadsheetFormatterProvider> spreadsheetIdToFormatterProvider,
                                           final Function<SpreadsheetId, ExpressionFunctionProvider> spreadsheetIdToExpressionFunctionProvider,
                                           final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                           final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                           final BiFunction<SpreadsheetMetadata, SpreadsheetLabelStore, HateosContentType> contentTypeFactory,
                                           final Supplier<LocalDateTime> now) {
        return BasicSpreadsheetContext.with(
                base,
                contentType,
                indentation,
                lineEnding,
                fractioner,
                createMetadata,
                metadataStore,
                spreadsheetIdToComparatorProvider,
                spreadsheetIdToFormatterProvider,
                spreadsheetIdToExpressionFunctionProvider,
                spreadsheetIdToRepository,
                spreadsheetMetadataStamper,
                contentTypeFactory,
                now
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
     * Creates a JSON {@link HateosContentType}.
     */
    public static HateosContentType jsonHateosContentType(final SpreadsheetMetadata metadata,
                                                          final SpreadsheetLabelStore labelStore) {
        return HateosContentType.json(
                metadata.jsonNodeUnmarshallContext()
                        .setPreProcessor(
                                SpreadsheetContexts.spreadsheetDeltaJsonCellLabelResolver(labelStore)
                        ),
                metadata.jsonNodeMarshallContext()
        );
    }

    /**
     * Stop creation
     */
    private SpreadsheetContexts() {
        throw new UnsupportedOperationException();
    }
}
