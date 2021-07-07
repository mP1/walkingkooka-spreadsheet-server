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
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;

import java.util.Map;
import java.util.Optional;

/**
 * A template that also filters using any requested {@link SpreadsheetDelta#window()}} if present.
 */
abstract class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsert<R extends SpreadsheetColumnOrRowReference & Comparable<R>> extends SpreadsheetEngineHateosHandlerSpreadsheetDelta2<R> {

    SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsert(final SpreadsheetEngine engine,
                                                                 final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public final Optional<SpreadsheetDelta> handleOne(final R columnOrRow,
                                                      final Optional<SpreadsheetDelta> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkReference(columnOrRow);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(this.executeAndWindowFilter(columnOrRow,
                1,
                resource));
    }

    abstract void checkReference(R columnOrRow);

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public final Optional<SpreadsheetDelta> handleRange(final Range<R> columnOrRow,
                                                        final Optional<SpreadsheetDelta> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkRangeBounded(columnOrRow, this.rangeLabel());
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        final R lower = columnOrRow.lowerBound().value().get();
        final R upper = columnOrRow.upperBound().value().get();

        return Optional.of(this.executeAndWindowFilter(lower,
                upper.value() - lower.value() + 1,
                resource));
    }

    abstract String rangeLabel();

    private SpreadsheetDelta executeAndWindowFilter(final R lower,
                                                    final int count,
                                                    final Optional<SpreadsheetDelta> in) {
        checkWithoutCells(in);

        return filterWindowAndSetMaxColumnWidthsMaxRowHeights(this.execute(lower, count), in);
    }

    /**
     * Sub classes must perform the delete or insert option.
     */
    abstract SpreadsheetDelta execute(final R lower, final int count);
}
