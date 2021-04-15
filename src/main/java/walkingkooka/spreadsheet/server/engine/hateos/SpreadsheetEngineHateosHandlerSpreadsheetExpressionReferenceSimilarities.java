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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpRequestParameterName;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosHandler} that resolves {@link String text} holding a cell-reference or label or range to a cell-reference.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceSimilarities extends SpreadsheetEngineHateosHandler<String, SpreadsheetExpressionReferenceSimilarities, SpreadsheetExpressionReferenceSimilarities> {

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

        return Optional.of(
                SpreadsheetExpressionReferenceSimilarities.with(
                        this.parseCellReference(text),
                        this.findLabels(text, count(parameters))
                )
        );
    }

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
    final static HttpRequestParameterName COUNT = HttpRequestParameterName.with("count");

    private Optional<SpreadsheetCellReference> parseCellReference(final String text) {
        SpreadsheetCellReference cellReference;
        try{
            cellReference = SpreadsheetExpressionReference.parseCellReference(text);
        } catch (final Exception invalid) {
            cellReference = null;
        }

        return Optional.ofNullable(cellReference);
    }

    private Set<SpreadsheetLabelMapping> findLabels(final String text,
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
