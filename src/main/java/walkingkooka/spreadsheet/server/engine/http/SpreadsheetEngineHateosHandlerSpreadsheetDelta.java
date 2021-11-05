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

package walkingkooka.spreadsheet.server.engine.http;

import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosHandlerSpreadsheetDelta<I extends Comparable<I>>
        extends SpreadsheetEngineHateosHandler<I, SpreadsheetDelta, SpreadsheetDelta> {

    /**
     * Retrieves the window from any present {@link SpreadsheetDelta} and then tries the parameters.
     */
    private static Optional<SpreadsheetCellRange> window(final Optional<SpreadsheetDelta> input,
                                                         final Map<HttpRequestAttribute<?>, Object> parameters) {
        Optional<SpreadsheetCellRange> window = input.isPresent() ?
                input.get().window() :
                Optional.empty();
        if (!window.isPresent()) {
            window = window(parameters);
        }

        return window;
    }

    /**
     * Returns the window taken from the query parameters if present.
     */
    private static Optional<SpreadsheetCellRange> window(final Map<HttpRequestAttribute<?>, Object> parameters) {
        final SpreadsheetCellRange window;

        final Optional<String> maybeWindow = WINDOW.firstParameterValue(parameters);
        if (maybeWindow.isPresent()) {
            window = SpreadsheetCellRange.parseCellRange(maybeWindow.get());
        } else {
            window = null;
        }

        return Optional.ofNullable(window);
    }

    /**
     * Adds support for passing the window as a url query parameter.
     */
    // @VisibleForTesting
    final static UrlParameterName WINDOW = UrlParameterName.with("window");

    static void checkCell(final SpreadsheetCellReference cell) {
        Objects.requireNonNull(cell, "cell");
    }

    /**
     * Checks that the range bounds are not null and both are inclusive.
     * Complains if the resource is null.
     */
    static void checkRangeBounded(final Range<?> range, final String label) {
        Objects.requireNonNull(range, label);

        if (!range.lowerBound().isInclusive() || !range.upperBound().isInclusive()) {
            throw new IllegalArgumentException("Range with both " + label + " required=" + range);
        }
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetEngineHateosHandlerSpreadsheetDelta(final SpreadsheetEngine engine,
                                                   final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public final String toString() {
        return SpreadsheetEngine.class.getSimpleName() + "." + this.operation();
    }

    abstract String operation();

    /**
     * Applies the windo if any was present on the input {@link SpreadsheetDelta} and also adds the {@link SpreadsheetDelta#columnWidths()} and
     * {@link SpreadsheetDelta#rowHeights()}
     */
    final SpreadsheetDelta prepareResponse(final Optional<SpreadsheetDelta> in,
                                           final Map<HttpRequestAttribute<?>, Object> parameters,
                                           final SpreadsheetDelta out) {
        return this.setColumnWidthsRowHeights(
                out.setWindow(
                        window(in, parameters)
                )
        ).setSelection(SpreadsheetEngineHttps.viewportSelection(in, parameters));
    }

    /**
     * Computes the widths and heights for all the columns and rows covered by the cells.
     */
    private SpreadsheetDelta setColumnWidthsRowHeights(final SpreadsheetDelta delta) {

        final SpreadsheetEngine engine = this.engine;

        final Map<SpreadsheetColumnReference, Double> columns = Maps.sorted();
        final Map<SpreadsheetRowReference, Double> rows = Maps.sorted();

        for (final SpreadsheetCell cell : delta.cells()) {
            final SpreadsheetCellReference reference = cell.reference();

            final SpreadsheetColumnReference column = reference.column()
                    .setReferenceKind(SpreadsheetReferenceKind.RELATIVE);
            if (false == columns.containsKey(column)) {
                final double width = engine.columnWidth(column, this.context);
                if (width > 0) {
                    columns.put(column, width);
                }
            }

            final SpreadsheetRowReference row = reference.row()
                    .setReferenceKind(SpreadsheetReferenceKind.RELATIVE);
            if (false == rows.containsKey(row)) {
                final double height = engine.rowHeight(row, context);
                if (height > 0) {
                    rows.put(row, height);
                }
            }
        }

        return delta.setColumnWidths(columns)
                .setRowHeights(rows);
    }
}
