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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRectangle;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumnsTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns,
        SpreadsheetColumnReference> {

    @Test
    public void testInsertColumn() {
        final SpreadsheetColumnReference column = this.id();
        final Optional<SpreadsheetDelta> resource = this.resource();

        final Set<SpreadsheetCell> cells = Sets.of(this.cell());

        final double width = 50;
        final double height = 20;

        this.handleOneAndCheck(this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    @SuppressWarnings("OptionalGetWithoutIsPresent")
                    public SpreadsheetDelta insertColumns(final SpreadsheetColumnReference c,
                                                          final int count,
                                                          final SpreadsheetEngineContext context) {
                        assertEquals(column, c, "column");
                        assertEquals(1, count, "count");
                        return SpreadsheetDelta.with(cells);
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column) {
                        assertEquals(SpreadsheetColumnReference.parseColumn("A"), column, "column");
                        return width;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row) {
                        return height;
                    }
                }),
                column,
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(SpreadsheetDelta.with(cells)
                        .setMaxColumnWidths(Maps.of(SpreadsheetColumnReference.parseColumn("A"), width))
                        .setMaxRowHeights(Maps.of(SpreadsheetRowReference.parseRow("99"), height))));
    }

    @Test
    public void testInsertSeveralColumns() {
        final Optional<SpreadsheetDelta> resource = this.collectionResource();

        final Range<SpreadsheetColumnReference> range = SpreadsheetColumnOrRowReference.parseColumnRange("C:E");
        final Set<SpreadsheetCell> cells = this.cells();

        final SpreadsheetDelta delta = SpreadsheetDelta.with(cells);

        this.handleRangeAndCheck(this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    public SpreadsheetDelta insertColumns(final SpreadsheetColumnReference c,
                                                          final int count,
                                                          final SpreadsheetEngineContext context) {
                        assertEquals(SpreadsheetColumnOrRowReference.parseColumn("C"), c, "column");
                        assertEquals(3, count, "count"); // C, D & E
                        return delta;
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column) {
                        switch(column.toString()) {
                            case "A":
                            case "Z":
                                break;
                            default:
                                throw new UnsupportedOperationException("Unknown column " + column);
                        }

                        return 0;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row) {
                        return 0;
                    }
                }),
                range, // 2 inclusive
                resource,
                HateosHandler.NO_PARAMETERS,
                Optional.of(delta));
    }

    @Test
    public void testInsertColumnFiltered() {
        final SpreadsheetColumnReference column = this.id();

        final Set<SpreadsheetCell> cells = this.cells();
        final List<SpreadsheetRectangle<?>> window = this.window();

        this.handleOneAndCheck(this.createHandler(
                new FakeSpreadsheetEngine() {

                    @Override
                    @SuppressWarnings("OptionalGetWithoutIsPresent")
                    public SpreadsheetDelta insertColumns(final SpreadsheetColumnReference c,
                                                          final int count,
                                                          final SpreadsheetEngineContext context) {
                        assertEquals(column, c, "column");
                        assertEquals(1, count, "count");
                        return SpreadsheetDelta.with(cells);
                    }

                    @Override
                    public double columnWidth(final SpreadsheetColumnReference column) {
                        assertEquals(SpreadsheetColumnReference.parseColumn("A"), column, "column");
                        return 0;
                    }

                    @Override
                    public double rowHeight(final SpreadsheetRowReference row) {
                        return 0;
                    }
                }),
                column,
                Optional.of(SpreadsheetDelta.with(SpreadsheetDelta.NO_CELLS).setWindow(window)),
                HateosHandler.NO_PARAMETERS,
                Optional.of(SpreadsheetDelta.with(this.cellsWithinWindow())));
    }

    @Test
    public void testInsertAllColumnsFails() {
        this.handleRangeFails2(Range.all());
    }

    @Test
    public void testInsertOpenRangeBeginFails() {
        this.handleRangeFails2(Range.lessThanEquals(SpreadsheetColumnOrRowReference.parseColumn("A")));
    }

    @Test
    public void testInsertOpenRangeEndFails() {
        this.handleRangeFails2(Range.greaterThanEquals(SpreadsheetColumnOrRowReference.parseColumn("A")));
    }

    private void handleRangeFails2(final Range<SpreadsheetColumnReference> columns) {
        assertEquals("Range with both columns required=" + columns,
                this.handleRangeFails(columns,
                        this.collectionResource(),
                        HateosHandler.NO_PARAMETERS,
                        IllegalArgumentException.class).getMessage(),
                "message");
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler().toString(), "SpreadsheetEngine.insertColumns");
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns createHandler(final SpreadsheetEngine engine) {
        return this.createHandler(engine, this.engineContext());
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns createHandler(final SpreadsheetEngine engine,
                                                                                            final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns.with(engine, context);
    }

    @Override
    public SpreadsheetColumnReference id() {
        return SpreadsheetColumnOrRowReference.parseColumn("C");
    }

    @Override
    public Range<SpreadsheetColumnReference> range() {
        return SpreadsheetColumnOrRowReference.parseColumnRange("C:E");
    }

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosHandler.NO_PARAMETERS;
    }

    @Override
    SpreadsheetEngine engine() {
        return new FakeSpreadsheetEngine();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteOrInsertInsertColumns.class;
    }
}
