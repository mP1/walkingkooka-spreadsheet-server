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
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Set;

final class SpreadsheetDeltaPatchHateosHttpEntityHandlerCell extends SpreadsheetDeltaPatchHateosHttpEntityHandler<SpreadsheetCellReference, SpreadsheetCellRangeReference> {

    final static SpreadsheetDeltaPatchHateosHttpEntityHandlerCell INSTANCE = new SpreadsheetDeltaPatchHateosHttpEntityHandlerCell();

    private SpreadsheetDeltaPatchHateosHttpEntityHandlerCell() {
        super();
    }

    @Override
    SpreadsheetCellRangeReference toSelectionRange(final SpreadsheetCellReference selection) {
        return selection.toCellRange();
    }

    @Override
    SpreadsheetCellRangeReference toSelectionRange(final Range<SpreadsheetCellReference> selection) {
        return SpreadsheetSelection.cellRange(selection);
    }

    @Override
    JsonNode preparePatch(final JsonNode delta,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        return SpreadsheetDelta.resolveCellLabels(
            delta.objectOrFail(),
            (e) -> context.storeRepository()
                .labels()
                .resolveLabelOrFail(e)
                .toCell()
        );
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetCellRangeReference cellRange,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.spreadsheetEngine()
            .loadCells(
                cellRange,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                CELL_AND_LABELS,
                context
            );
    }

    private final static Set<SpreadsheetDeltaProperties> CELL_AND_LABELS = Sets.of(
        SpreadsheetDeltaProperties.CELLS,
        SpreadsheetDeltaProperties.LABELS
    );

    @Override
    SpreadsheetDelta patch(final SpreadsheetDelta loaded,
                           final SpreadsheetCellRangeReference cellRange,
                           final JsonNode patch,
                           final Map<HttpRequestAttribute<?>, Object> parameters,
                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        return loaded.patchCells(
            cellRange,
            patch,
            context
        );
    }

    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetCellRangeReference cellRange,
                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.spreadsheetEngine()
            .saveCells(
                patched.cells(),
                context
            );
    }
}
