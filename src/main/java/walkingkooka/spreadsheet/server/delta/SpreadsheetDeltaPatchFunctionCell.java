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

import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
final class SpreadsheetDeltaPatchFunctionCell extends SpreadsheetDeltaPatchFunction<SpreadsheetCellRangeReference> {

    static SpreadsheetDeltaPatchFunctionCell with(final HttpRequest request,
                                                  final SpreadsheetEngine engine,
                                                  final SpreadsheetEngineContext context) {
        return new SpreadsheetDeltaPatchFunctionCell(request, engine, context);
    }

    private SpreadsheetDeltaPatchFunctionCell(final HttpRequest request,
                                              final SpreadsheetEngine engine,
                                              final SpreadsheetEngineContext context) {
        super(request, engine, context);
    }

    @Override
    SpreadsheetCellRangeReference parseSelection(final String text) {
        return this.context.resolveIfLabel(
                SpreadsheetSelection.parseCellRangeOrLabel(text)
        ).toCellRange();
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetCellRangeReference cellRange) {
        return this.engine.loadCells(
                cellRange,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                CELL_AND_LABELS,
                this.context
        );
    }

    private final static Set<SpreadsheetDeltaProperties> CELL_AND_LABELS = Sets.of(
            SpreadsheetDeltaProperties.CELLS,
            SpreadsheetDeltaProperties.LABELS
    );

    @Override
    JsonNode preparePatch(final JsonNode delta) {
        return SpreadsheetDelta.resolveCellLabels(
                delta.objectOrFail(),
                (e) -> this.context.storeRepository()
                        .labels()
                        .cellReferenceOrRangeOrFail(e)
                        .toCell()
        );
    }

    @Override
    SpreadsheetDelta patch(final SpreadsheetCellRangeReference cellRange,
                           final SpreadsheetDelta loaded,
                           final JsonNode patch,
                           final JsonNodeUnmarshallContext context) {
        return loaded.patchCells(
                cellRange,
                patch,
                context
        );
    }

    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetCellRangeReference cellRange) {
        return this.engine.saveCells(
                patched.cells(),
                this.context
        );
    }

    @Override
    String toStringPrefix() {
        return "Patch cell: ";
    }
}
