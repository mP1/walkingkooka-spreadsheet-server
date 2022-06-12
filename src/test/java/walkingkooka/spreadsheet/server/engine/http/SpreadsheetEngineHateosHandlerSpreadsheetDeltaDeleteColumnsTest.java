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

import java.util.Optional;
import java.util.Set;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumnsTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns,
        SpreadsheetColumnReference> {

    @Test
    public void testDeleteColumn() {
        final SpreadsheetColumnReference column = this.id();
        final Optional<SpreadsheetDelta> resource = this.resource();

        final Set<SpreadsheetCell> cells = this.cells();

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            @SuppressWarnings("OptionalGetWithoutIsPresent")
                            public SpreadsheetDelta deleteColumns(final SpreadsheetColumnReference c,
                                                                  final int count,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(column, c, "column");
                                checkEquals(1, count, "count");
                                return SpreadsheetDelta.EMPTY.setCells(cells);
                            }
                }),
                column,
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(cells)
                )
        );
    }

    @Test
    public void testDeleteSeveralColumns() {
        final Optional<SpreadsheetDelta> resource = this.collectionResource();

        final Range<SpreadsheetColumnReference> range = SpreadsheetColumnOrRowReference.parseColumnRange("C:E")
                .range();
        final Set<SpreadsheetCell> cells = this.cells();

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(cells);

        this.handleRangeAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta deleteColumns(final SpreadsheetColumnReference c,
                                                                  final int count,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(SpreadsheetColumnOrRowReference.parseColumn("C"), c, "column");
                                checkEquals(3, count, "count"); // C, D & E
                                return delta;
                            }
                }),
                range, // 2 inclusive
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(delta));
    }

    @Test
    public void testDeleteColumnFiltered() {
        final SpreadsheetColumnReference column = this.id();

        final Set<SpreadsheetCell> cells = this.cells();
        final Set<SpreadsheetCellRange> window = this.window();

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {

                            @Override
                            @SuppressWarnings("OptionalGetWithoutIsPresent")
                            public SpreadsheetDelta deleteColumns(final SpreadsheetColumnReference c,
                                                                  final int count,
                                                                  final SpreadsheetEngineContext context) {
                                checkEquals(column, c, "column");
                                checkEquals(1, count, "count");
                                return SpreadsheetDelta.EMPTY.setCells(cells)
                                        .setWindow(window);
                            }
                        }
                ),
                column,
                Optional.empty(),
                Maps.of(
                        SpreadsheetEngineHttps.WINDOW,
                        Lists.of(
                                window.iterator().next().toString()
                        )
                ),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(this.cellsWithinWindow())
                                .setWindow(window)
                )
        );
    }

    @Test
    public void testDeleteAllColumnsFails() {
        this.handleRangeFails2(Range.all());
    }

    @Test
    public void testDeleteOpenRangeBeginFails() {
        this.handleRangeFails2(Range.lessThanEquals(SpreadsheetColumnOrRowReference.parseColumn("A")));
    }

    @Test
    public void testDeleteOpenRangeEndFails() {
        this.handleRangeFails2(Range.greaterThanEquals(SpreadsheetColumnOrRowReference.parseColumn("A")));
    }

    private void handleRangeFails2(final Range<SpreadsheetColumnReference> columns) {
        this.checkEquals("Range with both columns required=" + columns,
                this.handleRangeFails(columns,
                        this.collectionResource(),
                        HateosHandler.NO_PARAMETERS,
                        IllegalArgumentException.class).getMessage(),
                "message");
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.deleteColumns");
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns createHandler(final SpreadsheetEngine engine) {
        return this.createHandler(engine, this.engineContext());
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns createHandler(final SpreadsheetEngine engine,
                                                                              final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns.with(engine, context);
    }

    @Override
    public SpreadsheetColumnReference id() {
        return SpreadsheetColumnOrRowReference.parseColumn("C");
    }

    @Override
    public Range<SpreadsheetColumnReference> range() {
        return SpreadsheetColumnOrRowReference.parseColumnRange("C:E")
                .range();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteColumns.class;
    }
}
