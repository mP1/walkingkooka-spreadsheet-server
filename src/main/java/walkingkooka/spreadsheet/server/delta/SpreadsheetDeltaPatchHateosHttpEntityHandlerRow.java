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
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class SpreadsheetDeltaPatchHateosHttpEntityHandlerRow extends SpreadsheetDeltaPatchHateosHttpEntityHandler<SpreadsheetRowReference, SpreadsheetRowRangeReference> {

    final static SpreadsheetDeltaPatchHateosHttpEntityHandlerRow INSTANCE = new SpreadsheetDeltaPatchHateosHttpEntityHandlerRow();

    private SpreadsheetDeltaPatchHateosHttpEntityHandlerRow() {
        super();
    }

    @Override
    SpreadsheetRowRangeReference toSelectionRange(final SpreadsheetRowReference selection) {
        return selection.toRowRange();
    }

    @Override
    SpreadsheetRowRangeReference toSelectionRange(final Range<SpreadsheetRowReference> selection) {
        return SpreadsheetSelection.rowRange(selection);
    }

    @Override
    JsonNode preparePatch(final JsonNode delta,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        return delta;
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetRowRangeReference range,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetEngine engine = context.spreadsheetEngine();

        final Set<SpreadsheetRow> loaded = SortedSets.tree(SpreadsheetRow.REFERENCE_COMPARATOR);

        for (final SpreadsheetRowReference row : range) {
            loaded.addAll(
                engine.loadRow(
                    row,
                    context
                ).rows()
            );
        }

        return SpreadsheetDelta.EMPTY.setRows(loaded);
    }

    @Override
    SpreadsheetDelta patch(final SpreadsheetDelta loaded,
                           final SpreadsheetRowRangeReference range,
                           final JsonNode patch,
                           final Map<HttpRequestAttribute<?>, Object> parameters,
                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetDelta patched = loaded.patchRows(
            patch,
            context
        );

        final SpreadsheetViewportWindows window = window(
            parameters,
            patched,
            context
        );

        // load all the cells for any unhidden rows....
        final Set<SpreadsheetCell> unhidden = SortedSets.tree();

        for (final SpreadsheetRow beforeRow : patched.rows()) {
            final SpreadsheetRowReference reference = beforeRow.reference();

            if (false == range.testRow(reference)) {
                throw new IllegalArgumentException("Patch row(s): " + range.toStringMaybeStar() + " includes invalid row " + reference);
            }

            if (beforeRow.hidden()) {
                final Optional<SpreadsheetRow> afterRow = patched.row(reference);
                if (!afterRow.isPresent() || !afterRow.get().hidden()) {
                    // row was hidden now shown, load all the cells within that window.
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
                          final SpreadsheetRowRangeReference range,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        final Set<SpreadsheetCell> cells = SortedSets.tree();

        final SpreadsheetEngine engine = context.spreadsheetEngine();

        for (final SpreadsheetRow row : patched.rows()) {
            cells.addAll(
                engine.saveRow(
                    row,
                    context
                ).cells()
            );
        }

        return patched.setCells(cells);
    }
}
