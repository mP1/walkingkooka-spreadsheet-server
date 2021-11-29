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
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumnTest extends
        SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn, SpreadsheetColumnReference> {

    private final static Optional<SpreadsheetDelta> RESOURCE = Optional.empty();

    @Test
    public void testInsertBeforeColumn() {
        final SpreadsheetMetadata metadata = this.metadata();
        final SpreadsheetEngine engine = SpreadsheetEngines.basic(metadata);
        final SpreadsheetEngineContext context = this.engineContext(engine, metadata);

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn.with(
                engine,
                context
        );

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(SpreadsheetCell.with(a1, SpreadsheetFormula.EMPTY.setText("'a1")));
        cellStore.save(SpreadsheetCell.with(b2, SpreadsheetFormula.EMPTY.setText("'b2")));
        cellStore.save(SpreadsheetCell.with(c3, SpreadsheetFormula.EMPTY.setText("'c3")));
        cellStore.save(SpreadsheetCell.with(d4, SpreadsheetFormula.EMPTY.setText("'d4")));

        final double width = COLUMN_WIDTH.pixelValue();
        final double height = ROW_HEIGHT.pixelValue();

        final int count = 2;

        final SpreadsheetCellReference d3 = c3.addColumn(count);
        final SpreadsheetCellReference e4 = d4.addColumn(count);

        this.handleOneAndCheck(
                handler,
                c3.column(),
                RESOURCE,
                Maps.of(SpreadsheetEngineHateosHandler.COUNT, Lists.of("" + count)),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                formattedCell(a1, "a1"),
                                                formattedCell(b2, "b2"),
                                                formattedCell(d3, "c3"),
                                                formattedCell(e4, "d4")
                                        )
                                ).setDeletedCells(
                                        Sets.of(c3, d4)
                                )
                                .setColumnWidths(
                                        Maps.of(a1.column(), width, b2.column(), width, d3.column(), width, e4.column(), width)
                                )
                                .setRowHeights(
                                        Maps.of(a1.row(), height, b2.row(), height, d3.row(), height, e4.row(), height)
                                )
                )
        );

        assertEquals(4, cellStore.count(), "cell count remains unchanged");

        assertNotEquals(Optional.empty(), cellStore.load(a1), "a1 was not moved");
        assertNotEquals(Optional.empty(), cellStore.load(b2), "b2 was not moved");
        assertNotEquals(Optional.empty(), cellStore.load(d3), "d3 moved");
        assertNotEquals(Optional.empty(), cellStore.load(e4), "e4 moved");
    }

    @Test
    public void testInsertBeforeColumnRange() {
        final SpreadsheetMetadata metadata = this.metadata();
        final SpreadsheetEngine engine = SpreadsheetEngines.basic(metadata);
        final SpreadsheetEngineContext context = this.engineContext(engine, metadata);

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn.with(
                engine,
                context
        );

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(SpreadsheetCell.with(a1, SpreadsheetFormula.EMPTY.setText("'a1")));
        cellStore.save(SpreadsheetCell.with(b2, SpreadsheetFormula.EMPTY.setText("'b2")));
        cellStore.save(SpreadsheetCell.with(c3, SpreadsheetFormula.EMPTY.setText("'c3")));
        cellStore.save(SpreadsheetCell.with(d4, SpreadsheetFormula.EMPTY.setText("'d4")));

        final double width = COLUMN_WIDTH.pixelValue();
        final double height = ROW_HEIGHT.pixelValue();

        final int count = 2;

        final SpreadsheetCellReference d3 = c3.addColumn(count);
        final SpreadsheetCellReference e4 = d4.addColumn(count);

        this.handleRangeAndCheck(
                handler,
                c3.column().range(d4.column()),
                RESOURCE,
                Maps.of(SpreadsheetEngineHateosHandler.COUNT, Lists.of("" + count)),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(
                                        Sets.of(
                                                formattedCell(a1, "a1"),
                                                formattedCell(b2, "b2"),
                                                formattedCell(d3, "c3"),
                                                formattedCell(e4, "d4")
                                        )
                                ).setDeletedCells(
                                        Sets.of(c3, d4)
                                )
                                .setColumnWidths(
                                        Maps.of(a1.column(), width, b2.column(), width, d3.column(), width, e4.column(), width)
                                )
                                .setRowHeights(
                                        Maps.of(a1.row(), height, b2.row(), height, d3.row(), height, e4.row(), height)
                                )
                )
        );

        assertEquals(4, cellStore.count(), "cell count remains unchanged");

        assertNotEquals(Optional.empty(), cellStore.load(a1), "a1 was not moved");
        assertNotEquals(Optional.empty(), cellStore.load(b2), "b2 was not moved");
        assertNotEquals(Optional.empty(), cellStore.load(d3), "d3 moved");
        assertNotEquals(Optional.empty(), cellStore.load(e4), "e4 moved");
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn createHandler(final SpreadsheetEngine engine,
                                                                                   final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn.with(engine, context);
    }

    @Override
    public SpreadsheetColumnReference id() {
        return SpreadsheetSelection.parseColumn("E");
    }

    @Override
    public Range<SpreadsheetColumnReference> range() {
        return SpreadsheetSelection.parseColumnRange("E:G")
                .range();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn.class;
    }
}
