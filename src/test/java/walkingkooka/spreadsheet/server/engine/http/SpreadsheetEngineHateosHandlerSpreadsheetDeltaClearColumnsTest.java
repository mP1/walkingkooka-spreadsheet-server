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
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetCellRangeStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetExpressionReferenceStores;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.SpreadsheetCellStore;
import walkingkooka.spreadsheet.store.SpreadsheetCellStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumnsTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns,
        SpreadsheetColumnReference> {

    private final static Optional<SpreadsheetDelta> RESOURCE = Optional.of(SpreadsheetDelta.EMPTY);

    @Test
    public void testClearColumn() {
        final SpreadsheetMetadata metadata = this.metadata();
        final SpreadsheetEngine engine = SpreadsheetEngines.basic(metadata);
        final SpreadsheetEngineContext context = this.engineContext(engine, metadata);

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns.with(
                engine,
                context
        );

        final SpreadsheetColumnReference a = SpreadsheetSelection.parseColumn("A");

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference a1048576 = SpreadsheetSelection.parseCell("A1048576");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("b2");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(SpreadsheetCell.with(a1, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(a1048576, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(b2, SpreadsheetFormula.EMPTY));

        this.handleOneAndCheck(
                handler,
                a,
                RESOURCE,
                HateosHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setDeletedCells(
                                        Sets.of(a1, a1048576)
                                )
                )
        );

        assertEquals(Optional.empty(), cellStore.load(a1), "a1 should have been deleted");
        assertEquals(Optional.empty(), cellStore.load(a1048576), "A1048576 should have been deleted");
        assertNotEquals(Optional.empty(), cellStore.load(b2), "b2 should NOT have been deleted");
    }

    @Test
    public void testClearColumnRange() {
        final SpreadsheetMetadata metadata = this.metadata();
        final SpreadsheetEngine engine = SpreadsheetEngines.basic(metadata);
        final SpreadsheetEngineContext context = this.engineContext(engine, metadata);

        final SpreadsheetCellReference a1 = SpreadsheetSelection.parseCell("A1");
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final SpreadsheetCellReference d4 = SpreadsheetSelection.parseCell("D4");

        final SpreadsheetCellStore cellStore = context.storeRepository()
                .cells();

        cellStore.save(SpreadsheetCell.with(a1, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(b2, SpreadsheetFormula.EMPTY));
        cellStore.save(SpreadsheetCell.with(c3, SpreadsheetFormula.EMPTY));

        cellStore.save(SpreadsheetCell.with(d4, SpreadsheetFormula.EMPTY));

        final SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns handler = SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns.with(
                engine,
                context
        );

        this.handleRangeAndCheck(
                handler,
                b2.column().spreadsheetColumnRange(c3.column()).range(),
                RESOURCE,
                HateosHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setDeletedCells(
                                        Sets.of(b2, c3)
                                )
                )
        );

        assertNotEquals(Optional.empty(), cellStore.load(a1), "a1 should NOT have been deleted");
        assertEquals(Optional.empty(), cellStore.load(b2), "b2 should have been deleted");
        assertEquals(Optional.empty(), cellStore.load(c3), "c3 should have been deleted");
        assertNotEquals(Optional.empty(), cellStore.load(d4), "d4 should have been deleted");
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(), SpreadsheetEngine.class.getSimpleName() + ".clearColumns"
        );
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns createHandler(final SpreadsheetEngine engine,
                                                                             final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns.with(engine, context);
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
        return SpreadsheetColumnReference.parseColumnRange(RANGE)
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
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns.class;
    }
}
