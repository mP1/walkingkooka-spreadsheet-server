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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosContentType;
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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn extends SpreadsheetDeltaPatchHateosHttpEntityHandler<SpreadsheetColumnReference, SpreadsheetColumnRangeReference> {

    static SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn with(final SpreadsheetEngine engine,
                                                                   final HateosContentType hateosContentType,
                                                                   final SpreadsheetEngineContext context) {
        return new SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn(
                engine,
                hateosContentType,
                context
        );
    }

    private SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn(final SpreadsheetEngine engine,
                                                               final HateosContentType hateosContentType,
                                                               final SpreadsheetEngineContext context) {
        super(engine, hateosContentType, context);
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
    JsonNode preparePatch(final JsonNode delta) {
        return delta;
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
    SpreadsheetDelta patch(final SpreadsheetDelta loaded,
                           final SpreadsheetColumnRangeReference range,
                           final JsonNode patch,
                           final Map<HttpRequestAttribute<?>, Object> parameters,
                           final JsonNodeUnmarshallContext context) {
        final SpreadsheetDelta patched = loaded.patchColumns(
                patch,
                context
        );

        final SpreadsheetViewportWindows window = window(
                parameters,
                patched
        );

        // load all the cells for any unhidden columns....
        final Set<SpreadsheetCell> unhidden = Sets.sorted();

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
                            this.loadCells(window.cellRanges())
                    );
                }
            }
        }

        return patched.setCells(unhidden);
    }

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
}
