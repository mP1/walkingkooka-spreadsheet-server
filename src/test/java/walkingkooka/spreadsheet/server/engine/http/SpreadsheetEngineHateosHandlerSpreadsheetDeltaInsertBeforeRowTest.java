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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;

import java.util.Optional;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRowTest extends
        SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow, SpreadsheetRowReference> {

    private final static Optional<SpreadsheetDelta> RESOURCE = Optional.empty();

    @Test
    public void testInsertBeforeRow() {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();
        final SpreadsheetEngineContext context = this.engineContext(
                engine
        );

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow.with(
                engine,
                context
        );

        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(
                a1.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'a1")
                )
        );
        cellStore.save(
                b2.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'b2")
                )
        );
        cellStore.save(
                c3.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'c3")
                )
        );
        cellStore.save(
                d4.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'d4")
                )
        );

        final int count = 2;

        final SpreadsheetCellReference c5 = c3.addRow(count);
        final SpreadsheetCellReference d6 = d4.addRow(count);

        this.handleOneAndCheck(
                handler,
                c3.row(),
                RESOURCE,
                Maps.of(SpreadsheetEngineHateosHandler.COUNT, Lists.of("" + count)),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                formattedCell(a1, "a1"),
                                                formattedCell(b2, "b2"),
                                                formattedCell(c5, "c3"),
                                                formattedCell(d6, "d4")
                                        )
                                ).setDeletedCells(
                                        Sets.of(c3, d4)
                                )
                                .setColumnWidths(
                                        columnWidths("a,b,c,d")
                                )
                                .setRowHeights(
                                        rowHeights("1,2,3,4,5,6")
                                )
                )
        );

        this.checkEquals(4, cellStore.count(), "cell count remains unchanged");

        this.checkNotEquals(Optional.empty(), cellStore.load(a1), "a1 was not moved");
        this.checkNotEquals(Optional.empty(), cellStore.load(b2), "b2 was not moved");
        this.checkNotEquals(Optional.empty(), cellStore.load(c5), "d3 moved");
        this.checkNotEquals(Optional.empty(), cellStore.load(d6), "e4 moved");
    }

    @Test
    public void testInsertBeforeRowRange() {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();
        final SpreadsheetEngineContext context = this.engineContext(
                engine
        );

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow.with(
                engine,
                context
        );

        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(
                a1.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'a1")
                )
        );
        cellStore.save(
                b2.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'b2")
                )
        );
        cellStore.save(
                c3.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'c3")
                )
        );
        cellStore.save(
                d4.setFormula(
                        SpreadsheetFormula.EMPTY.setText("'d4")
                )
        );


        final int count = 2;

        final SpreadsheetCellReference c5 = c3.addRow(count);
        final SpreadsheetCellReference d6 = d4.addRow(count);

        this.handleRangeAndCheck(
                handler,
                c3.row().range(d4.row()),
                RESOURCE,
                Maps.of(SpreadsheetEngineHateosHandler.COUNT, Lists.of("" + count)),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                formattedCell(a1, "a1"),
                                                formattedCell(b2, "b2"),
                                                formattedCell(c5, "c3"),
                                                formattedCell(d6, "d4")
                                        )
                                ).setDeletedCells(
                                        Sets.of(c3, d4)
                                )
                                .setColumnWidths(
                                        columnWidths("a,b,c,d")
                                )
                                .setRowHeights(
                                        rowHeights("1,2,3,4,5,6")
                                )
                )
        );

        this.checkEquals(4, cellStore.count(), "cell count remains unchanged");

        this.checkNotEquals(Optional.empty(), cellStore.load(a1), "a1 was not moved");
        this.checkNotEquals(Optional.empty(), cellStore.load(b2), "b2 was not moved");
        this.checkNotEquals(Optional.empty(), cellStore.load(c5), "d3 moved");
        this.checkNotEquals(Optional.empty(), cellStore.load(d6), "e4 moved");
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow createHandler(final SpreadsheetEngine engine,
                                                                                final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow.with(engine, context);
    }

    @Override
    public SpreadsheetRowReference id() {
        return SpreadsheetSelection.parseRow("1");
    }

    @Override
    public Range<SpreadsheetRowReference> range() {
        return SpreadsheetSelection.parseRowRange("2:3")
                .range();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow.class;
    }
}
