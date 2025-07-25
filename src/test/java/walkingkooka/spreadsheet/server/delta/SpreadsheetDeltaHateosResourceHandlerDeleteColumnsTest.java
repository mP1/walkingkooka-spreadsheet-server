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
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaHateosResourceHandlerDeleteColumnsTest extends SpreadsheetDeltaHateosResourceHandlerDeleteTestCase<SpreadsheetDeltaHateosResourceHandlerDeleteColumns,
    SpreadsheetColumnReference> {

    @Test
    public void testDeleteColumn() {
        final SpreadsheetColumnReference column = this.id();
        final Optional<SpreadsheetDelta> resource = this.resource();

        final Set<SpreadsheetCell> cells = this.cells();

        this.handleOneAndCheck(
            column,
            resource,
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta deleteColumns(final SpreadsheetColumnReference c,
                                                          final int count,
                                                          final SpreadsheetEngineContext context) {
                        checkEquals(column, c, "column");
                        checkEquals(1, count, "count");
                        return SpreadsheetDelta.EMPTY.setCells(cells);
                    }
                }
            ),
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setCells(cells)
            )
        );
    }

    @Test
    public void testHandleRangeDeleteSeveralColumns() {
        final Optional<SpreadsheetDelta> resource = this.collectionResource();

        final Range<SpreadsheetColumnReference> range = SpreadsheetSelection.parseColumnRange("C:E")
            .range();
        final Set<SpreadsheetCell> cells = this.cells();

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(cells);

        this.handleRangeAndCheck(
            range, // 2 inclusive
            resource,
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta deleteColumns(final SpreadsheetColumnReference c,
                                                          final int count,
                                                          final SpreadsheetEngineContext context) {
                        checkEquals(SpreadsheetSelection.parseColumn("C"), c, "column");
                        checkEquals(3, count, "count"); // C, D & E
                        return delta;
                    }
                }
            ),
            Optional.of(delta)
        );
    }

    @Test
    public void testHandleOneDeleteColumnFiltered() {
        final SpreadsheetColumnReference column = this.id();

        final Set<SpreadsheetCell> cells = this.cells();
        final SpreadsheetViewportWindows window = this.window();

        this.handleOneAndCheck(
            column,
            Optional.empty(),
            Maps.of(
                SpreadsheetDeltaUrlQueryParameters.WINDOW,
                Lists.of(
                    window.toString()
                )
            ),
            UrlPath.EMPTY,
            this.context(
                new FakeSpreadsheetEngine() {

                    @Override
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
        this.handleRangeFails2(Range.lessThanEquals(SpreadsheetSelection.parseColumn("A")));
    }

    @Test
    public void testDeleteOpenRangeEndFails() {
        this.handleRangeFails2(Range.greaterThanEquals(SpreadsheetSelection.parseColumn("A")));
    }

    private void handleRangeFails2(final Range<SpreadsheetColumnReference> columns) {
        this.checkEquals(
            "Range with both columns required=" + columns,
            this.handleRangeFails(
                columns,
                this.collectionResource(),
                HateosResourceHandler.NO_PARAMETERS,
                UrlPath.EMPTY,
                this.context(),
                IllegalArgumentException.class
            ).getMessage(),
            "message"
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.deleteColumns");
    }

    @Override
    public SpreadsheetDeltaHateosResourceHandlerDeleteColumns createHandler() {
        return SpreadsheetDeltaHateosResourceHandlerDeleteColumns.INSTANCE;
    }

    @Override
    public SpreadsheetColumnReference id() {
        return SpreadsheetSelection.parseColumn("C");
    }

    @Override
    public Range<SpreadsheetColumnReference> range() {
        return SpreadsheetSelection.parseColumnRange("C:E")
            .range();
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerDeleteColumns> type() {
        return SpreadsheetDeltaHateosResourceHandlerDeleteColumns.class;
    }
}
