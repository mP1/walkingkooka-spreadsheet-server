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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContexts;

import java.util.Optional;

public final class SpreadsheetDeltaHateosResourceHandlerInsertAfterRowTest extends
        SpreadsheetDeltaHateosResourceHandlerInsertTestCase<SpreadsheetDeltaHateosResourceHandlerInsertAfterRow, SpreadsheetRowReference> {

    @Test
    public void testRow() {
        final SpreadsheetRowReference row = SpreadsheetSelection.parseRow("3");

        final SpreadsheetCell cell = SpreadsheetSelection.parseCell("C3")
                .setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText("=99")
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
                                checkEquals(SpreadsheetSelection.parseRow("4"), row, "row");
                                checkEquals(COUNT, count, "count");
                                return returned;
                            }
                        }
                ),
                row,
                this.resource(),
                this.parameters(),
                this.context(),
                Optional.of(
                        returned
                )
        );
    }

    @Test
    public void testRowRange() {
        final SpreadsheetRowRangeReference range = SpreadsheetSelection.parseRowRange("3:5");

        final SpreadsheetCell cell = SpreadsheetSelection.parseCell("C3")
                .setFormula(
                        SpreadsheetFormula.EMPTY
                                .setText("=99")
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
                                checkEquals(SpreadsheetSelection.parseRow("6"), row, "row");
                                checkEquals(COUNT, count, "count");
                                return returned;
                            }
                        }
                ),
                range.range(),
                this.resource(),
                this.parameters(),
                this.context(),
                Optional.of(
                        returned
                )
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerInsertAfterRow createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerInsertAfterRow.with(engine);
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
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerInsertAfterRow> type() {
        return SpreadsheetDeltaHateosResourceHandlerInsertAfterRow.class;
    }
}
