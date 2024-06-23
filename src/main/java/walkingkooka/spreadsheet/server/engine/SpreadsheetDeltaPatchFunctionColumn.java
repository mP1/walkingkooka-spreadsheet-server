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

package walkingkooka.spreadsheet.server.engine;

import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
final class SpreadsheetDeltaPatchFunctionColumn extends SpreadsheetDeltaPatchFunction<SpreadsheetColumnRangeReference> {

    static SpreadsheetDeltaPatchFunctionColumn with(final HttpRequest request,
                                                    final SpreadsheetEngine engine,
                                                    final SpreadsheetEngineContext context) {
        return new SpreadsheetDeltaPatchFunctionColumn(request, engine, context);
    }

    private SpreadsheetDeltaPatchFunctionColumn(final HttpRequest request,
                                                final SpreadsheetEngine engine,
                                                final SpreadsheetEngineContext context) {
        super(request, engine, context);
    }

    @Override
    SpreadsheetColumnRangeReference parseSelection(final String text) {
        return SpreadsheetSelection.parseColumnRange(text);
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetColumnRangeReference range) {
        final SpreadsheetEngine engine = this.engine;
        final SpreadsheetEngineContext context = this.context;

        final Set<SpreadsheetColumn> loaded = Sets.sorted();

        for (final SpreadsheetColumnReference column : range) {
            loaded.addAll(
                    engine.loadColumn(
                            column,
                            context
                    ).columns()
            );
        }

        return SpreadsheetDelta.EMPTY.setColumns(loaded);
    }

    @Override
    JsonNode preparePatch(final JsonNode delta) {
        return delta;
    }

    @Override
    SpreadsheetDelta patch(final SpreadsheetColumnRangeReference range,
                           final SpreadsheetDelta loaded,
                           final JsonNode patch,
                           final JsonNodeUnmarshallContext context) {
        final SpreadsheetDelta patched = loaded.patchColumns(
                patch,
                context
        );

        final SpreadsheetViewportWindows window = window(
                patched
        );

        // load all the cells for any unhidden columns....
        final Set<SpreadsheetCell> unhidden = Sets.sorted();

        for (final SpreadsheetColumn beforeColumn : patched.columns()) {
            final SpreadsheetColumnReference reference = beforeColumn.reference();

            if (false == range.testColumn(reference)) {
                throw new IllegalArgumentException("Patch columns: " + range.toStringMaybeStar() + " includes invalid column " + reference);
            }

            if (beforeColumn.hidden()) {
                final Optional<SpreadsheetColumn> afterColumn = patched.column(reference);
                if (!afterColumn.isPresent() || !afterColumn.get().hidden()) {
                    // column was hidden now shown, load all the cells within that window.
                    unhidden.addAll(
                            this.loadCells(window.cellRanges())
                    );
                }
            }
        }

        return patched.setCells(unhidden);
    }

    /**
     * Saves all the {@link SpreadsheetColumn columns} in the patched {@link SpreadsheetDelta}.
     */
    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetColumnRangeReference range) {
        final SpreadsheetEngine engine = this.engine;
        final SpreadsheetEngineContext context = this.context;

        final Set<SpreadsheetCell> cells = Sets.sorted();

        for (final SpreadsheetColumn column : patched.columns()) {
            cells.addAll(
                    engine.saveColumn(
                            column,
                            context
                    ).cells()
            );
        }

        return patched.setCells(cells);
    }

    @Override
    String toStringPrefix() {
        return "Patch column: ";
    }
}