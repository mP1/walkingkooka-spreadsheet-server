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

package walkingkooka.spreadsheet.server.engine;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Optional;

public final class SpreadsheetDeltaHateosResourceHandlerInsertAfterColumnTest extends
        SpreadsheetDeltaHateosResourceHandlerInsertTestCase<SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn, SpreadsheetColumnReference> {

    @Test
    public void testColumn() {
        final SpreadsheetColumnReference column = SpreadsheetSelection.parseColumn("C");

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
                            public SpreadsheetDelta insertColumns(final SpreadsheetColumnReference column,
                                                                  final int count,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(SpreadsheetSelection.parseColumn("D"), column, "column");
                                checkEquals(COUNT, count, "count");

                                return returned;
                            }
                        },
                        this.engineContext()
                ),
                column,
                this.resource(),
                this.parameters(),
                Optional.of(
                        returned
                )
        );
    }

    @Test
    public void testColumnRange() {
        final SpreadsheetColumnRangeReference range = SpreadsheetSelection.parseColumnRange("C:E");

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
                            public SpreadsheetDelta insertColumns(final SpreadsheetColumnReference column,
                                                                  final int count,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(SpreadsheetSelection.parseColumn("F"), column, "column");
                                checkEquals(COUNT, count, "count");

                                return returned;
                            }
                        },
                        this.engineContext()
                ),
                range.range(),
                this.resource(),
                this.parameters(),
                Optional.of(
                        returned
                )
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn createHandler(final SpreadsheetEngine engine,
                                                                         final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn.with(engine, context);
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
    public Class<SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn> type() {
        return SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn.class;
    }
}