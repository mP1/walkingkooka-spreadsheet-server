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
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public final class SpreadsheetDeltaHateosResourceHandlerClearColumnsTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerClearColumns,
    SpreadsheetColumnReference> {

    private final static Optional<SpreadsheetDelta> RESOURCE = Optional.empty();

    @Test
    public void testClearColumn() {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();

        final SpreadsheetDeltaHateosResourceHandlerClearColumns handler = SpreadsheetDeltaHateosResourceHandlerClearColumns.with(
            engine
        );

        final SpreadsheetColumnReference a = SpreadsheetSelection.parseColumn("A");

        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetCellReference a1048576 = SpreadsheetSelection.parseCell("A1048576");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("b2");

        final SpreadsheetCellStore cellStore = SpreadsheetCellStores.treeMap();

        cellStore.save(
            a1.setFormula(SpreadsheetFormula.EMPTY)
        );
        cellStore.save(
            a1048576.setFormula(SpreadsheetFormula.EMPTY)
        );
        cellStore.save(
            b2.setFormula(SpreadsheetFormula.EMPTY)
        );

        this.handleOneAndCheck(
            handler,
            a,
            RESOURCE,
            HateosResourceHandler.NO_PARAMETERS,
            this.context(cellStore),
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setDeletedCells(
                        Sets.of(a1, a1048576)
                    ).setColumnWidths(
                        columnWidths("A")
                    ).setRowHeights(
                        rowHeights("1,1048576")
                    ).setColumnCount(
                        OptionalInt.of(2)
                    ).setRowCount(
                        OptionalInt.of(2)
                    )
            )
        );

        this.checkEquals(Optional.empty(), cellStore.load(a1), "a1 should have been deleted");
        this.checkEquals(Optional.empty(), cellStore.load(a1048576), "A1048576 should have been deleted");
        this.checkNotEquals(Optional.empty(), cellStore.load(b2), "b2 should NOT have been deleted");
    }

    @Test
    public void testClearColumnRange() {
        final SpreadsheetEngine engine = SpreadsheetEngines.basic();

        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetCellStore cellStore = SpreadsheetCellStores.treeMap();

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

        final SpreadsheetDeltaHateosResourceHandlerClearColumns handler = SpreadsheetDeltaHateosResourceHandlerClearColumns.with(
            engine
        );

        this.handleRangeAndCheck(
            handler,
            b2.column().columnRange(c3.column()).range(),
            RESOURCE,
            HateosResourceHandler.NO_PARAMETERS,
            this.context(cellStore),
            Optional.of(
                SpreadsheetDelta.EMPTY
                    .setDeletedCells(
                        Sets.of(b2, c3)
                    ).setColumnWidths(
                        columnWidths("B,C")
                    ).setRowHeights(
                        rowHeights("2,3")
                    ).setColumnCount(
                        OptionalInt.of(4)
                    ).setRowCount(
                        OptionalInt.of(4)
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
            this.createHandler(), SpreadsheetEngine.class.getSimpleName() + ".clearColumns"
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerClearColumns createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerClearColumns.with(engine);
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetColumnReference id() {
        return SpreadsheetSelection.parseColumn("C");
    }

    @Override
    public Range<SpreadsheetColumnReference> range() {
        return SpreadsheetSelection.parseColumnRange(RANGE)
            .range();
    }

    private final static String RANGE = "A:C";

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return RESOURCE;
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return RESOURCE;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerClearColumns> type() {
        return SpreadsheetDeltaHateosResourceHandlerClearColumns.class;
    }
}
