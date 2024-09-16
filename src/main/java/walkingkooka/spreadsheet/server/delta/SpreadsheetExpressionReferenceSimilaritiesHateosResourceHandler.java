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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that resolves {@link String text} holding a cell-reference or label or range to a cell-reference.
 */
final class SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler implements HateosResourceHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleAll<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleMany<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleNone<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleRange<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities, SpreadsheetHateosResourceHandlerContext> {

    static SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler with(final SpreadsheetEngineContext context) {
        return new SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler(
                Objects.requireNonNull(context, "context")
        );
    }

    private SpreadsheetExpressionReferenceSimilaritiesHateosResourceHandler(final SpreadsheetEngineContext context) {
        super();
        this.context = context;
    }

    @Override
    public Optional<SpreadsheetExpressionReferenceSimilarities> handleOne(final String text,
                                                                          final Optional<SpreadsheetExpressionReferenceSimilarities> resource,
                                                                          final Map<HttpRequestAttribute<?>, Object> parameters,
                                                                          final SpreadsheetHateosResourceHandlerContext context) {
        Objects.requireNonNull(text, "text");
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetExpressionReference cellOrLabel = parseCellOrLabelOrNull(text);
        final Set<SpreadsheetLabelMapping> mappings = this.findLabelMappings(
                text,
                SpreadsheetUrlQueryParameters.count(parameters)
        );

        return Optional.of(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.ofNullable(cellOrLabel instanceof SpreadsheetCellReference ? (SpreadsheetCellReference) cellOrLabel : null),
                        Optional.ofNullable(cellOrLabel instanceof SpreadsheetLabelName && mappings.isEmpty() ? (SpreadsheetLabelName) cellOrLabel : null),
                        mappings
                )
        );
    }

    /**
     * Attempts to parse the text into a {@link SpreadsheetCellReference} or {@link SpreadsheetLabelName} returning
     * null if that fails.
     */
    private static SpreadsheetExpressionReference parseCellOrLabelOrNull(final String text) {
        SpreadsheetExpressionReference cellOrLabel;
        try {
            cellOrLabel = SpreadsheetSelection.parseCellOrLabel(text);
        } catch (final Exception invalid) {
            cellOrLabel = null;
        }
        return cellOrLabel;
    }

    /**
     * Finds the matching {@link SpreadsheetLabelMapping} for the given text and limit.
     */
    private Set<SpreadsheetLabelMapping> findLabelMappings(final String text,
                                                           final int count) {
        return this.context.storeRepository()
                .labels()
                .findSimilar(text, count);
    }

    private final SpreadsheetEngineContext context;

    @Override
    public String toString() {
        return SpreadsheetLabelStore.class.getSimpleName() + ".findSimilarities";
    }
}
