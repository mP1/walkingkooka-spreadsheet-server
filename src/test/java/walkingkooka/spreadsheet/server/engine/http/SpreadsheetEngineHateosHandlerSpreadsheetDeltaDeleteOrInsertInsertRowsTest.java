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
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRowsTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows,
        SpreadsheetRowReference> {

    @Test
    public void testInsertRow() {
        final SpreadsheetRowReference row = this.id();
        final Optional<SpreadsheetDelta> resource = this.resource();

        final Set<SpreadsheetCell> cells = this.cells();

        final double width = 50;
        final double height = 20;

        this.handleOneAndCheck(this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    @SuppressWarnings("OptionalGetWithoutIsPresent")
                    public SpreadsheetDelta insertRows(final SpreadsheetRowReference r,
                                                       final int count,
                                                       final SpreadsheetEngineContext context) {
                        assertEquals(row, r, "row");
                        assertEquals(1, count, "count");
                        return SpreadsheetDelta.EMPTY.setCells(cells);
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return width;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        assertEquals(SpreadsheetRowReference.parseRow("99"), row, "row");
                        return height;
                    }
                }),
                row,
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(SpreadsheetDelta.EMPTY.setCells(cells)
                        .setColumnWidths(Maps.of(SpreadsheetColumnReference.parseColumn("A"), width, SpreadsheetColumnReference.parseColumn("Z"), width))
                        .setRowHeights(Maps.of(SpreadsheetRowReference.parseRow("99"), height))));
    }

    @Test
    public void testInsertSeveralRows() {
        final Optional<SpreadsheetDelta> resource = this.collectionResource();

        final Range<SpreadsheetRowReference> range = SpreadsheetColumnOrRowReference.parseRowRange("2:4")
                .range();
        final Set<SpreadsheetCell> cells = this.cells();

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(cells);

        this.handleRangeAndCheck(this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta insertRows(final SpreadsheetRowReference r,
                                                       final int count,
                                                       final SpreadsheetEngineContext context) {
                        assertEquals(SpreadsheetColumnOrRowReference.parseRow("2"), r, "row");
                        assertEquals(3, count, "count"); // 2, 3 & 4
                        return delta;
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column,
                                              final SpreadsheetEngineContext context) {
                        return 0;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row,
                                            final SpreadsheetEngineContext context) {
                        assertEquals(SpreadsheetRowReference.parseRow("99"), row, "row");
                        return 0;
                    }
                }),
                range, // 3 inclusive
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(delta));
    }

    @Test
    public void testInsertRowFiltered() {
        final SpreadsheetRowReference row = this.id();

        final Set<SpreadsheetCell> cells = this.cells();
        final Optional<SpreadsheetCellRange> window = this.window();

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            @SuppressWarnings("OptionalGetWithoutIsPresent")
                            public SpreadsheetDelta insertRows(final SpreadsheetRowReference r,
                                                               final int count,
                                                               final SpreadsheetEngineContext context) {
                                assertEquals(row, r, "row");
                                assertEquals(1, count, "count");
                                return SpreadsheetDelta.EMPTY.setCells(cells);
                            }

                            @Override
                            public double columnWidth(final SpreadsheetColumnReference column,
                                                      final SpreadsheetEngineContext context) {
                                return 0;
                            }

                            @Override
                            public double rowHeight(final SpreadsheetRowReference row,
                                                    final SpreadsheetEngineContext context) {
                                assertEquals(SpreadsheetRowReference.parseRow("99"), row, "row");
                                return 0;
                            }
                        }),
                row,
                Optional.empty(),
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDelta.WINDOW, Lists.of(window.get().toString())
                ),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(this.cellsWithinWindow())
                                .setWindow(window)
                )
        );
    }

    @Test
    public void testInsertAllRowsFails() {
        this.handleRangeFails2(Range.all());
    }

    @Test
    public void testInsertOpenRangeBeginFails() {
        this.handleRangeFails2(Range.lessThanEquals(SpreadsheetColumnOrRowReference.parseRow("2")));
    }

    @Test
    public void testInsertOpenRangeEndFails() {
        this.handleRangeFails2(Range.greaterThanEquals(SpreadsheetColumnOrRowReference.parseRow("3")));
    }

    private void handleRangeFails2(final Range<SpreadsheetRowReference> rows) {
        assertEquals("Range with both rows required=" + rows,
                this.handleRangeFails(rows,
                        this.collectionResource(),
                        HateosHandler.NO_PARAMETERS,
                        IllegalArgumentException.class).getMessage(),
                "message");
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.insertRows");
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows createHandler(final SpreadsheetEngine engine) {
        return this.createHandler(engine, this.engineContext());
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows createHandler(final SpreadsheetEngine engine,
                                                                                         final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows.with(engine, context);
    }

    @Override
    public SpreadsheetRowReference id() {
        return SpreadsheetColumnOrRowReference.parseRow("2");
    }

    @Override
    public Range<SpreadsheetRowReference> range() {
        return SpreadsheetColumnOrRowReference.parseRowRange("2:4")
                .range();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertRows.class;
    }
}
