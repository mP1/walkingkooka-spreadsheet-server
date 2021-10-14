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
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReferenceRange;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRowTest extends
        SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRow, SpreadsheetRowReference> {

    @Test
    public void testRow() {
        final SpreadsheetRowReference row = SpreadsheetSelection.parseRow("3");

        final SpreadsheetCell cell = SpreadsheetCell.with(
                SpreadsheetSelection.parseCell("C3"),
                SpreadsheetFormula.with("=99")
        );
        final SpreadsheetDelta returned = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(cell)
                );

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta insertRows(final SpreadsheetRowReference row,
                                                               final int count,
                                                               final SpreadsheetEngineContext context) {
                                assertEquals(SpreadsheetSelection.parseRow("4"), row, "row");
                                assertEquals(COUNT, count, "count");
                                return returned;
                            }

                            @Override
                            public double columnWidth(final SpreadsheetColumnReference c,
                                                      final SpreadsheetEngineContext context) {
                                return COLUMN_WIDTH;
                            }

                            @Override
                            public double rowHeight(final SpreadsheetRowReference r,
                                                    final SpreadsheetEngineContext context) {
                                return ROW_HEIGHT;
                            }
                        },
                        this.engineContext()
                ),
                row,
                this.resource(),
                this.parameters(),
                Optional.of(
                        returned.setColumnWidths(
                                        Maps.of(
                                                cell.reference().column(), COLUMN_WIDTH
                                        )
                                )
                                .setRowHeights(
                                        Maps.of(
                                                cell.reference().row(), ROW_HEIGHT
                                        )
                                )
                )
        );
    }

    @Test
    public void testRowRange() {
        final SpreadsheetRowReferenceRange range = SpreadsheetSelection.parseRowRange("3:5");

        final SpreadsheetCell cell = SpreadsheetCell.with(
                SpreadsheetSelection.parseCell("C3"),
                SpreadsheetFormula.with("=99")
        );
        final SpreadsheetDelta returned = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(cell)
                );

        this.handleRangeAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta insertRows(final SpreadsheetRowReference row,
                                                               final int count,
                                                               final SpreadsheetEngineContext context) {
                                assertEquals(SpreadsheetSelection.parseRow("6"), row, "row");
                                assertEquals(COUNT, count, "count");
                                return returned;
                            }

                            @Override
                            public double columnWidth(final SpreadsheetColumnReference c,
                                                      final SpreadsheetEngineContext context) {
                                return COLUMN_WIDTH;
                            }

                            @Override
                            public double rowHeight(final SpreadsheetRowReference r,
                                                    final SpreadsheetEngineContext context) {
                                return ROW_HEIGHT;
                            }
                        },
                        this.engineContext()
                ),
                range.range(),
                this.resource(),
                this.parameters(),
                Optional.of(
                        returned.setColumnWidths(
                                        Maps.of(
                                                cell.reference().column(), COLUMN_WIDTH
                                        )
                                )
                                .setRowHeights(
                                        Maps.of(
                                                cell.reference().row(), ROW_HEIGHT
                                        )
                                )
                )
        );
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRow createHandler(final SpreadsheetEngine engine,
                                                                               final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRow.with(engine, context);
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
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRow> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertAfterRow.class;
    }
}
