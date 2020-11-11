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
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.HttpRequestAttribute;
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
import java.util.Objects;
import java.util.Optional;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosHandler<I extends Comparable<I>>
        implements HateosHandler<I, SpreadsheetDelta, SpreadsheetDelta> {

    /**
     * Checks required factory method parameters are not null.
     */
    static void check(final SpreadsheetEngine engine,
                      final SpreadsheetEngineContext context) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(context, "context");
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetEngineHateosHandler(final SpreadsheetEngine engine,
                                   final SpreadsheetEngineContext context) {
        super();
        this.engine = engine;
        this.context = context;
    }

    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    @Override
    public final String toString() {
        return SpreadsheetEngine.class.getSimpleName() + "." + this.operation();
    }

    abstract String operation();

    // Optional<I>......................................................................................................

    final I checkIdRequired(final Optional<I> id) {
        checkIdNotNull(id);

        return id.orElseThrow(() -> new IllegalArgumentException("Id missing"));
    }

    final void checkIdNotNull(final Optional<I> id) {
        Objects.requireNonNull(id, "id");
    }

    // Range<I>.........................................................................................................

    /**
     * Checks that the range bounds are not null and both are inclusive.
     */
    final void checkRangeBounded(final Range<?> range, final String label) {
        Objects.requireNonNull(range, label);

        if (!range.lowerBound().isInclusive() || !range.upperBound().isInclusive()) {
            throw new IllegalArgumentException("Range with both " + label + " required=" + range);
        }
    }

    final void checkRangeNotNull(final Range<I> ids) {
        Objects.requireNonNull(ids, "ids");
    }

    // Optional<RESOURCE>...............................................................................................

    /**
     * Complains if the resource is null.
     */
    final void checkResource(final Optional<?> resource) {
        Objects.requireNonNull(resource, "resource");
    }

    /**
     * Complains if the resource is null or present.
     */
    final void checkResourceEmpty(final Optional<?> resource) {
        checkResource(resource);
        resource.ifPresent((r) -> {
            throw new IllegalArgumentException("Resource not allowed=" + r);
        });
    }

    /**
     * Complains if the resource is absent.
     */
    final <T> T checkResourceNotEmpty(final Optional<T> resource) {
        checkResource(resource);
        return resource.orElseThrow(() -> new IllegalArgumentException("Required resource missing"));
    }

    static void checkWithoutCells(final Optional<SpreadsheetDelta> delta) {
        delta.ifPresent(SpreadsheetEngineHateosHandler::checkWithoutCells0);
    }

    private static void checkWithoutCells0(final SpreadsheetDelta delta) {
        if (!delta.cells().isEmpty()) {
            throw new IllegalArgumentException("Expected delta without cells: " + delta);
        }
    }

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
    private SpreadsheetDelta filterWindowAndSetMaxColumnWidthsMaxRowHeights0(final List<SpreadsheetRectangle> window,
                                                                             final SpreadsheetDelta delta) {
        final List<SpreadsheetRectangle> ranges = SpreadsheetEngineHateosHandlerSpreadsheetExpressionReferenceVisitor.transform(window, this.engine);
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

    // parameters.......................................................................................................

    /**
     * Checks parameters are present.
     */
    final void checkParameters(final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(parameters, "parameters");
    }
}
