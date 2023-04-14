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
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCellTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<
        SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell,
        SpreadsheetCellReference> {

    @Test
    public void testDeleteCell() {
        final SpreadsheetCellReference cell = this.id();

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta deleteCells(final SpreadsheetSelection s,
                                                                final SpreadsheetEngineContext context) {
                                checkEquals(cell, s, "selection");

                                return SpreadsheetDelta.EMPTY
                                        .setDeletedCells(
                                                Sets.of(
                                                        (SpreadsheetCellReference) s
                                                )
                                        );
                            }
                        },
                        this.engineContext()
                ),
                this.id(),
                this.resource(),
                this.parameters(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setDeletedCells(
                                        Sets.of(cell)
                                )
                )
        );
    }

    @Test
    public void testDeleteCellRange() {
        final Range<SpreadsheetCellReference> range = this.range();
        final SpreadsheetCellReference cell = this.id();

        this.handleRangeAndCheck(
                this.createHandler(
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta deleteCells(final SpreadsheetSelection s,
                                                                final SpreadsheetEngineContext context) {
                                checkEquals(SpreadsheetCellRange.cellRange(range), s, "selection");
                                return SpreadsheetDelta.EMPTY
                                        .setDeletedCells(
                                                Sets.of(cell)
                                        );
                            }
                        },
                        this.engineContext()
                ),
                range,
                this.resource(),
                this.parameters(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setDeletedCells(
                                        Sets.of(cell)
                                )
                )
        );
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell createHandler(final SpreadsheetEngine engine,
                                                                           final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell.with(engine, context);
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
    public SpreadsheetCellReference id() {
        return SpreadsheetSelection.A1;
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetExpressionReference.parseCellRange("A1:B2").range();
    }

    @Override public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell.class;
    }
}
