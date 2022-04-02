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

import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReferenceRange;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
final class SpreadsheetEnginePatchSpreadsheetRowFunction extends SpreadsheetEnginePatch<SpreadsheetRowReferenceRange> {

    static SpreadsheetEnginePatchSpreadsheetRowFunction with(final HttpRequest request,
                                                             final SpreadsheetEngine engine,
                                                             final SpreadsheetEngineContext context) {
        return new SpreadsheetEnginePatchSpreadsheetRowFunction(request, engine, context);
    }

    private SpreadsheetEnginePatchSpreadsheetRowFunction(final HttpRequest request,
                                                         final SpreadsheetEngine engine,
                                                         final SpreadsheetEngineContext context) {
        super(request, engine, context);
    }

    @Override
    SpreadsheetRowReferenceRange parseReference(final String text) {
        return SpreadsheetSelection.parseRowRange(text);
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetRowReferenceRange range) {
        final SpreadsheetEngine engine = this.engine;
        final SpreadsheetEngineContext context = this.context;

        final Set<SpreadsheetRow> loaded = Sets.sorted();

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
    JsonNode preparePatch(final JsonNode delta) {
        return delta;
    }

    @Override
    SpreadsheetDelta patch(final SpreadsheetRowReferenceRange range,
                           final SpreadsheetDelta loaded,
                           final JsonNode patch,
                           final JsonNodeUnmarshallContext context) {
        final SpreadsheetDelta patched = loaded.patchRows(
                patch,
                context
        );

        final Set<SpreadsheetCellRange> window = window(
                patched
        );

        // load all the cells for any unhidden rows....
        final Set<SpreadsheetCell> unhidden = Sets.sorted();

        for (final SpreadsheetRow beforeRow : patched.rows()) {
            final SpreadsheetRowReference reference = beforeRow.reference();

            if (!range.testRow(reference)) {
                throw new IllegalArgumentException("Patch rows: " + range + " includes invalid row " + reference);
            }

            if (beforeRow.hidden()) {
                final Optional<SpreadsheetRow> afterRow = patched.row(reference);
                if (!afterRow.isPresent() || !afterRow.get().hidden()) {
                    // row was hidden now shown, load all the cells within that window.
                    unhidden.addAll(
                            this.loadCells(window)
                    );
                }
            }
        }

        return patched.setCells(unhidden);
    }

    /**
     * Saves all the {@link SpreadsheetRow rows} in the patched {@link SpreadsheetDelta}.
     */
    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetRowReferenceRange reference) {
        final SpreadsheetEngine engine = this.engine;
        final SpreadsheetEngineContext context = this.context;

        final Set<SpreadsheetCell> cells = Sets.sorted();

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

    @Override
    String toStringPrefix() {
        return "Patch row: ";
    }
}
