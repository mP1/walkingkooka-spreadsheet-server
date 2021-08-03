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

package walkingkooka.spreadsheet.server.engine.hateos;

import walkingkooka.collect.Range;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReferenceOrLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosHandler} that resolves {@link String text} holding a cell-reference or label or range to a cell-reference.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities extends SpreadsheetEngineHateosHandler2<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> {

    static SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities with(final SpreadsheetEngine engine,
                                                                                         final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities(final SpreadsheetEngine engine,
                                                                                     final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetExpressionReferenceSimilarities> handleOne(final String text,
                                                                          final Optional<SpreadsheetExpressionReferenceSimilarities> resource,
                                                                          final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(text, "text");
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final SpreadsheetCellReferenceOrLabelName cellOrLabel = parseCellOrLabelOrNull(text);
        final Set<SpreadsheetLabelMapping> mappings = this.findLabelMappings(text, count(parameters));

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
    private static SpreadsheetCellReferenceOrLabelName parseCellOrLabelOrNull(final String text) {
        SpreadsheetCellReferenceOrLabelName cellOrLabel;
        try {
            cellOrLabel = SpreadsheetSelection.parseCellOrLabelName(text);
        } catch (final Exception invalid) {
            cellOrLabel = null;
        }
        return cellOrLabel;
    }

    /**
     * Returns the count parameter as an integer.
     */
    private int count(final Map<HttpRequestAttribute<?>, Object> parameters) {
        final List<String> counts = (List<String>) parameters.get(COUNT);
        if (null == counts) {
            throw new IllegalArgumentException("Missing count parameter");
        }
        switch (counts.size()) {
            case 0:
                throw new IllegalArgumentException("Missing count parameter");
            default:
                return Integer.parseInt(counts.get(0));
        }
    }

    // @VisibleForTesting
    final static UrlParameterName COUNT = UrlParameterName.with("count");

    /**
     * Finds the matching {@link SpreadsheetLabelMapping} for the given text and limit.
     */
    private Set<SpreadsheetLabelMapping> findLabelMappings(final String text,
                                                           final int count) {
        return this.context.storeRepository()
                .labels()
                .findSimilar(text, count);
    }

    @Override
    public Optional<SpreadsheetExpressionReferenceSimilarities> handleRange(final Range<String> range,
                                                                            final Optional<SpreadsheetExpressionReferenceSimilarities> resource,
                                                                            final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(range);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return SpreadsheetLabelStore.class.getSimpleName() + ".findSimilarities";
    }
}
