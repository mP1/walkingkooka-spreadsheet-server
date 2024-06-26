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
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContexts;

import java.util.Map;
import java.util.Optional;

public final class SpreadsheetDeltaHateosResourceHandlerDeleteCellTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerDeleteCell,
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
                        }
                ),
                this.id(),
                this.resource(),
                this.parameters(),
                this.context(),
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
                                checkEquals(SpreadsheetCellRangeReference.cellRange(range), s, "selection");
                                return SpreadsheetDelta.EMPTY
                                        .setDeletedCells(
                                                Sets.of(cell)
                                        );
                            }
                        }
                ),
                range,
                this.resource(),
                this.parameters(),
                this.context(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setDeletedCells(
                                        Sets.of(cell)
                                )
                )
        );
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerDeleteCell createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerDeleteCell.with(engine);
    }

    @Override
    SpreadsheetEngine engine() {
        return SpreadsheetEngines.fake();
    }

    @Override
    public SpreadsheetCellReference id() {
        return SpreadsheetSelection.A1;
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetExpressionReference.parseCellRange("A1:B2").range();
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
        return Maps.empty();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerDeleteCell> type() {
        return SpreadsheetDeltaHateosResourceHandlerDeleteCell.class;
    }
}
