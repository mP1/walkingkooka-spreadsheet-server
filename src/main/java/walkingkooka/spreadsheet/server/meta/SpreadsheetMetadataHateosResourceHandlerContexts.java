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

import walkingkooka.math.Fraction;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SpreadsheetMetadataHateosResourceHandlerContexts implements PublicStaticHelper {

    /**
     * {@see BasicSpreadsheetMetadataHateosResourceHandlerContext}
     */
    public static SpreadsheetMetadataHateosResourceHandlerContext basic(final AbsoluteUrl base,
                                                                        final Indentation indentation,
                                                                        final LineEnding lineEnding,
                                                                        final Function<BigDecimal, Fraction> fractioner,
                                                                        final Function<Optional<Locale>, SpreadsheetMetadata> createMetadata,
                                                                        final SpreadsheetMetadataStore metadataStore,
                                                                        final Function<SpreadsheetId, SpreadsheetProvider> spreadsheetIdToSpreadsheetProvider,
                                                                        final Function<SpreadsheetId, SpreadsheetStoreRepository> spreadsheetIdToRepository,
                                                                        final Function<SpreadsheetMetadata, SpreadsheetMetadata> spreadsheetMetadataStamper,
                                                                        final JsonNodeMarshallContext marshallContext,
                                                                        final JsonNodeUnmarshallContext unmarshallContext,
                                                                        final Supplier<LocalDateTime> now) {
        return BasicSpreadsheetMetadataHateosResourceHandlerContext.with(
                base,
                indentation,
                lineEnding,
                fractioner,
                createMetadata,
                metadataStore,
                spreadsheetIdToSpreadsheetProvider,
                spreadsheetIdToRepository,
                spreadsheetMetadataStamper,
                marshallContext,
                unmarshallContext,
                now
        );
    }

    /**
     * {@see FakeSpreadsheetMetadataHateosResourceHandlerContext}
     */
    public static FakeSpreadsheetMetadataHateosResourceHandlerContext fake() {
        return new FakeSpreadsheetMetadataHateosResourceHandlerContext();
    }

    /**
     * {@see SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessor}
     */
    public static JsonNodeUnmarshallContextPreProcessor spreadsheetDeltaJsonCellLabelResolver(final SpreadsheetLabelStore store) {
        return SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessor.with(store);
    }

    /**
     * Stop creation
     */
    private SpreadsheetMetadataHateosResourceHandlerContexts() {
        throw new UnsupportedOperationException();
    }
}
