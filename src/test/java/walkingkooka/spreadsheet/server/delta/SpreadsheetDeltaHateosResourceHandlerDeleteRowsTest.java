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
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaHateosResourceHandlerDeleteRowsTest extends SpreadsheetDeltaHateosResourceHandlerDeleteTestCase<SpreadsheetDeltaHateosResourceHandlerDeleteRows,
    SpreadsheetRowReference> {

    @Test
    public void testHandleOneDeleteRow() {
        final SpreadsheetRowReference row = this.id();
        final Optional<SpreadsheetDelta> resource = this.resource();

        final Set<SpreadsheetCell> cells = Sets.of(this.cell());

        this.handleOneAndCheck(
            this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta deleteRows(final SpreadsheetRowReference r,
                                                       final int count,
                                                       final SpreadsheetEngineContext context) {
                        checkEquals(row, r, "row");
                        checkEquals(1, count, "count");
                        return SpreadsheetDelta.EMPTY.setCells(cells);
                    }
                }),
            row,
            resource,
            HateosResourceHandler.NO_PARAMETERS,
            this.context(),
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setCells(cells)
            )
        );
    }

    @Test
    public void testHandleRangeDeleteSeveralRows() {
        final Optional<SpreadsheetDelta> resource = this.collectionResource();

        final Range<SpreadsheetRowReference> range = SpreadsheetSelection.parseRowRange("2:4")
            .range();
        final Set<SpreadsheetCell> cells = this.cells();

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(cells);

        this.handleRangeAndCheck(
            this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta deleteRows(final SpreadsheetRowReference r,
                                                       final int count,
                                                       final SpreadsheetEngineContext context) {
                        checkEquals(SpreadsheetSelection.parseRow("2"), r, "row");
                        checkEquals(3, count, "count"); // 2, 3 & 4
                        return delta;
                    }
                }),
            range, // 3 inclusive
            resource,
            HateosResourceHandler.NO_PARAMETERS,
            this.context(),
            Optional.of(delta)
        );
    }

    @Test
    public void testHandleOneDeleteRowFiltered() {
        final SpreadsheetRowReference row = this.id();

        final Set<SpreadsheetCell> cells = this.cells();
        final SpreadsheetViewportWindows window = this.window();

        this.handleOneAndCheck(
            this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta deleteRows(final SpreadsheetRowReference c,
                                                       final int count,
                                                       final SpreadsheetEngineContext context) {
                        checkEquals(row, c, "row");
                        checkEquals(1, count, "count");
                        return SpreadsheetDelta.EMPTY.setCells(cells)
                            .setWindow(window);
                    }
                }),
            row,
            Optional.empty(),
            Maps.of(
                SpreadsheetDeltaUrlQueryParameters.WINDOW,
                Lists.of(
                    window.toString()
                )
            ),
            this.context(),
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setCells(this.cellsWithinWindow())
                    .setWindow(window)
            )
        );
    }

    @Test
    public void testDeleteAllRowsFails() {
        this.handleRangeFails2(Range.all());
    }

    @Test
    public void testDeleteOpenRangeBeginFails() {
        this.handleRangeFails2(Range.lessThanEquals(SpreadsheetSelection.parseRow("2")));
    }

    @Test
    public void testDeleteOpenRangeEndFails() {
        this.handleRangeFails2(Range.greaterThanEquals(SpreadsheetSelection.parseRow("3")));
    }

    private void handleRangeFails2(final Range<SpreadsheetRowReference> rows) {
        this.checkEquals("Range with both rows required=" + rows,
            this.handleRangeFails(
                rows,
                this.collectionResource(),
                HateosResourceHandler.NO_PARAMETERS,
                this.context(),
                IllegalArgumentException.class).getMessage(),
            "message"
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.deleteRows");
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerDeleteRows createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerDeleteRows.with(engine);
    }

    @Override
    public SpreadsheetRowReference id() {
        return SpreadsheetSelection.parseRow("2");
    }

    @Override
    public Range<SpreadsheetRowReference> range() {
        return SpreadsheetSelection.parseRowRange("2:4")
            .range();
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerDeleteRows> type() {
        return SpreadsheetDeltaHateosResourceHandlerDeleteRows.class;
    }
}
