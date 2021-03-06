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

import walkingkooka.ToStringBuilder;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;

import java.util.Map;
import java.util.stream.Collectors;

final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellBatchLoader {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellBatchLoader with(final SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell handler) {
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellBatchLoader(handler);
    }


    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCellBatchLoader(final SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell handler) {
        super();
        this.handler = handler;
    }

    SpreadsheetDelta batchLoad(final Range<SpreadsheetCellReference> cells) {
        SpreadsheetRange.with(cells).cellStream()
                .forEach(this::maybeLoadCell);
        //noinspection SimplifyStreamApiCallChains
        return this.referenceToCell.isEmpty() ?
                SpreadsheetDelta.with(SpreadsheetDelta.NO_CELLS) :
                SpreadsheetDelta.with(this.referenceToCell.values()
                        .stream()
                        .collect(Collectors.toSet()));
    }

    private void maybeLoadCell(final SpreadsheetCellReference reference) {
        if (false == this.referenceToCell.containsKey(reference)) {
            final SpreadsheetDelta loaded = this.handler.loadCell(reference);
            loaded.cells()
                    .forEach(this::add);
        }
    }

    private void add(final SpreadsheetCell cell) {
        this.referenceToCell.put(cell.reference(), cell);
    }

    /**
     * Tracks cells that have been loaded.
     */
    final Map<SpreadsheetCellReference, SpreadsheetCell> referenceToCell = Maps.sorted(SpreadsheetCellReference.COMPARATOR);

    /**
     * The handler is used to load individual cells
     */
    final SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell handler;

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .value(this.referenceToCell)
                .value(this.handler)
                .build();
    }
}
