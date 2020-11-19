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

import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRectangle;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosHandlerSpreadsheetDelta<I extends Comparable<I>>
        extends SpreadsheetEngineHateosHandler<I, SpreadsheetDelta, SpreadsheetDelta> {

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
     * Applies the windo if any was present on the input {@link SpreadsheetDelta} and also adds the {@link SpreadsheetDelta#maxColumnWidths()} and
     * {@link SpreadsheetDelta#maxRowHeights()}
     */
    final SpreadsheetDelta filterWindowAndSetMaxColumnWidthsMaxRowHeights(final SpreadsheetDelta out,
                                                                          final Optional<SpreadsheetDelta> in) {
        // if $in is present apply its window to filter the result cells
        return in.isPresent() ?
                this.filterWindowAndSetMaxColumnWidthsMaxRowHeights0(in.get().window(), out) :
                this.setMaxColumnWidthsMaxRowHeights(out);
    }

    /**
     * Filter the cells with the window and then gather the column widths and row heights.
     */
    private SpreadsheetDelta filterWindowAndSetMaxColumnWidthsMaxRowHeights0(final List<SpreadsheetRectangle<?>> window,
                                                                             final SpreadsheetDelta delta) {
        final List<SpreadsheetRectangle<?>> ranges = SpreadsheetEngineHateosHandlerSpreadsheetDeltaSpreadsheetExpressionReferenceVisitor.transform(window, this.engine);
        return this.setMaxColumnWidthsMaxRowHeights(delta.setWindow(ranges)
                .setWindow(SpreadsheetDelta.NO_WINDOW));
    }

    /**
     * Computes the widths and heights for all the columns and rows covered by the cells.
     */
    private SpreadsheetDelta setMaxColumnWidthsMaxRowHeights(final SpreadsheetDelta delta) {

        final SpreadsheetEngine engine = this.engine;

        final Map<SpreadsheetColumnReference, Double> columns = Maps.sorted();
        final Map<SpreadsheetRowReference, Double> rows = Maps.sorted();

        for (final SpreadsheetCell cell : delta.cells()) {
            final SpreadsheetCellReference reference = cell.reference();

            final SpreadsheetColumnReference column = reference.column().setReferenceKind(SpreadsheetReferenceKind.RELATIVE);
            if (false == columns.containsKey(column)) {
                final double width = engine.columnWidth(column);
                if (width > 0) {
                    columns.put(column, width);
                }
            }

            final SpreadsheetRowReference row = reference.row().setReferenceKind(SpreadsheetReferenceKind.RELATIVE);
            if (false == rows.containsKey(row)) {
                final double height = engine.rowHeight(row);
                if (height > 0) {
                    rows.put(row, height);
                }
            }
        }

        return delta.setMaxColumnWidths(columns)
                .setMaxRowHeights(rows);
    }
}
