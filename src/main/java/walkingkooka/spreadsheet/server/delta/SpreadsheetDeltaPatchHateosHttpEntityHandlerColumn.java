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

import walkingkooka.collect.Range;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn extends SpreadsheetDeltaPatchHateosHttpEntityHandler<SpreadsheetColumnReference, SpreadsheetColumnRangeReference> {

    final static SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn INSTANCE = new SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn();

    private SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn() {
        super();
    }

    @Override
    SpreadsheetColumnRangeReference toSelectionRange(final SpreadsheetColumnReference selection) {
        return selection.toColumnRange();
    }

    @Override
    SpreadsheetColumnRangeReference toSelectionRange(final Range<SpreadsheetColumnReference> selection) {
        return SpreadsheetSelection.columnRange(selection);
    }

    @Override
    JsonNode preparePatch(final JsonNode delta,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        return delta;
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetColumnRangeReference range,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetEngine engine = context.spreadsheetEngine();

        final Set<SpreadsheetColumn> loaded = SortedSets.tree(SpreadsheetColumn.REFERENCE_COMPARATOR);

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
    SpreadsheetDelta patch(final SpreadsheetDelta loaded,
                           final SpreadsheetColumnRangeReference range,
                           final JsonNode patch,
                           final Map<HttpRequestAttribute<?>, Object> parameters,
                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetDelta patched = loaded.patchColumns(
            patch,
            context
        );

        final SpreadsheetViewportWindows window = window(
            parameters,
            patched,
            context
        );

        // load all the cells for any unhidden columns....
        final Set<SpreadsheetCell> unhidden = SortedSets.tree();

        for (final SpreadsheetColumn beforeColumn : patched.columns()) {
            final SpreadsheetColumnReference reference = beforeColumn.reference();

            if (false == range.testColumn(reference)) {
                throw new IllegalArgumentException("Patch column(s): " + range.toStringMaybeStar() + " includes invalid column " + reference);
            }

            if (beforeColumn.hidden()) {
                final Optional<SpreadsheetColumn> afterColumn = patched.column(reference);
                if (!afterColumn.isPresent() || !afterColumn.get().hidden()) {
                    // column was hidden now shown, load all the cells within that window.
                    unhidden.addAll(
                        this.loadMultipleCellRanges(
                            window.cellRanges(),
                            context
                        )
                    );
                }
            }
        }

        return patched.setCells(unhidden);
    }

    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetColumnRangeReference range,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetEngine engine = context.spreadsheetEngine();

        final Set<SpreadsheetCell> cells = SortedSets.tree();

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
}
