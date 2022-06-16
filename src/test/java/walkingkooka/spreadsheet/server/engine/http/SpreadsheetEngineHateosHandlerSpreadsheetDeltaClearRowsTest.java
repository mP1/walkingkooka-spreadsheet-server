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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRowsTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows,
        SpreadsheetRowReference> {

    private final static Optional<SpreadsheetDelta> RESOURCE = Optional.of(SpreadsheetDelta.EMPTY);

    @Test
    public void testClearRow() {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();
        final SpreadsheetEngineContext context = this.engineContext(
                engine
        );

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.with(
                engine,
                context
        );

        final SpreadsheetRowReference row1 = SpreadsheetSelection.parseRow("1");

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference row1ColMax = row1.setColumn(SpreadsheetColumnReference.MAX);
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("b2");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(
                a1.setFormula(SpreadsheetFormula.EMPTY)
        );
        cellStore.save(
                row1ColMax.setFormula(SpreadsheetFormula.EMPTY)
        );
        cellStore.save(
                b2.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
                handler,
                row1,
                RESOURCE,
                HateosHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetDelta.EMPTY.setDeletedCells(
                                Sets.of(a1, row1ColMax)
                        ).setColumnWidths(
                                columnWidths("A,XFD")
                        ).setRowHeights(
                                rowHeights("1")
                        )
                )
        );

        this.checkEquals(Optional.empty(), cellStore.load(a1), "a1 should have been deleted");
        this.checkEquals(Optional.empty(), cellStore.load(row1ColMax), "A1048576 should have been deleted");
        this.checkNotEquals(Optional.empty(), cellStore.load(b2), "b2 should NOT have been deleted");
    }

    @Test
    public void testClearRowRange() {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();
        final SpreadsheetEngineContext context = this.engineContext(
                engine
        );

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(
                a1.setFormula(SpreadsheetFormula.EMPTY)
        );
        cellStore.save(
                b2.setFormula(SpreadsheetFormula.EMPTY)
        );
        cellStore.save(
                c3.setFormula(SpreadsheetFormula.EMPTY)
        );

        cellStore.save(
                d4.setFormula(SpreadsheetFormula.EMPTY)
        );

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.with(
                engine,
                context
        );

        this.handleRangeAndCheck(
                handler,
                b2.row()
                        .rowRange(c3.row())
                        .range(),
                RESOURCE,
                HateosHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetDelta.EMPTY.setDeletedCells(
                                Sets.of(b2, c3)
                        ).setColumnWidths(
                                columnWidths("B,C")
                        ).setRowHeights(
                                rowHeights("2,3")
                        )
                )
        );

        this.checkNotEquals(Optional.empty(), cellStore.load(a1), "a1 should NOT have been deleted");
        this.checkEquals(Optional.empty(), cellStore.load(b2), "b2 should have been deleted");
        this.checkEquals(Optional.empty(), cellStore.load(c3), "c3 should have been deleted");
        this.checkNotEquals(Optional.empty(), cellStore.load(d4), "d4 should have been deleted");
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(), SpreadsheetEngine.class.getSimpleName() + ".clearRows"
        );
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows createHandler(final SpreadsheetEngine engine,
                                                                          final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.with(engine, context);
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    SpreadsheetEngineContext engineContext() {
        return SpreadsheetEngineContexts.fake();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetRowReference id() {
        return SpreadsheetSelection.parseRow("3");
    }

    @Override
    public Range<SpreadsheetRowReference> range() {
        return SpreadsheetRowReference.parseRowRange(RANGE)
                .range();
    }

    private final static String RANGE = "1:3";

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return RESOURCE;
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return RESOURCE;
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows.class;
    }
}
