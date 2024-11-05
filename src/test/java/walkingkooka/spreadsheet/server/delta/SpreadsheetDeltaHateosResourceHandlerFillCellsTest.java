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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.tree.text.TextNode;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class SpreadsheetDeltaHateosResourceHandlerFillCellsTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerFillCells,
        SpreadsheetCellReference> {

    @Test
    public void testFillFromParameterMissing() {
        this.handleRangeAndCheck2(this.parameters(), this.toSpreadsheetCellRangeReference());
    }

    @Test
    public void testFillFromParameterEmptyFails() {
        this.handleRangeFails(
                this.toSpreadsheetCellRangeReference().range(),
                this.collectionResource(),
                Maps.of(
                        SpreadsheetDeltaHateosResourceHandlerFillCells.FROM, Lists.empty()
                ),
                this.context(),
                IllegalArgumentException.class);
    }

    @Test
    public void testFillFromParameterInvalidFails() {
        this.handleRangeFails(
                this.toSpreadsheetCellRangeReference().range(),
                this.collectionResource(),
                Maps.of(
                        SpreadsheetDeltaHateosResourceHandlerFillCells.FROM, Lists.of("!INVALID")
                ),
                this.context(),
                IllegalArgumentException.class
        );
    }

    @Test
    public void testFillFromParameterPresent() {
        this.handleRangeAndCheck2(
                Maps.of(
                        SpreadsheetDeltaHateosResourceHandlerFillCells.FROM, Lists.of(TO)
                ),
                this.toSpreadsheetCellRangeReference()
        );
    }

    @Test
    public void testFillFromParameterPresent2() {
        this.handleRangeAndCheck2(
                Maps.of(
                        SpreadsheetDeltaHateosResourceHandlerFillCells.FROM, Lists.of(TO, FROM)
                ),
                this.toSpreadsheetCellRangeReference()
        );
    }

    private void handleRangeAndCheck2(final Map<HttpRequestAttribute<?>, Object> parameters,
                                      final SpreadsheetCellRangeReference from) {
        this.handleRangeAndCheck(
                SpreadsheetDeltaHateosResourceHandlerFillCells.with(
                        new FakeSpreadsheetEngine() {

                            @Override
                            @SuppressWarnings("OptionalGetWithoutIsPresent")
                            public SpreadsheetDelta fillCells(final Collection<SpreadsheetCell> cells,
                                                              final SpreadsheetCellRangeReference f,
                                                              final SpreadsheetCellRangeReference t,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(collectionResource().get().cells(), cells, "cells");
                                checkEquals(from, f, "from");
                                checkEquals(toSpreadsheetCellRangeReference(), t, "to");
                                return deltaWithCell();
                            }
                        }
                ),
                this.range(),
                this.collectionResource(),
                parameters,
                this.context(),
                Optional.of(
                        this.deltaWithCell()
                )
        );
    }

    @Test
    public void testFillFiltered() {
        final SpreadsheetCell unsaved1 = this.cell();
        final SpreadsheetCell saved1 = unsaved1.setFormattedValue(
                Optional.of(
                        TextNode.text("FORMATTED 1")
                )
        );

        final Range<SpreadsheetCellReference> range = this.range();
        final SpreadsheetCellRangeReference spreadsheetCellRangeReference = SpreadsheetSelection.cellRange(range);

        final SpreadsheetDelta resource = SpreadsheetDelta.EMPTY
                .setCells(Sets.of(unsaved1));

        final SpreadsheetViewportWindows window = this.window();

        this.handleRangeAndCheck(
                SpreadsheetDeltaHateosResourceHandlerFillCells.with(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta fillCells(final Collection<SpreadsheetCell> cells,
                                                              final SpreadsheetCellRangeReference from,
                                                              final SpreadsheetCellRangeReference to,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(resource.cells(), cells, "cells");
                                checkEquals(spreadsheetCellRangeReference, from, "from");
                                checkEquals(spreadsheetCellRangeReference, to, "to");
                                return SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(saved1,
                                                        cellOutsideWindow()
                                                                .setFormattedValue(
                                                                        Optional.of(
                                                                                TextNode.text("FORMATTED 2")
                                                                        )
                                                                )
                                                )
                                        );
                            }
                        }
                ),
                range,
                Optional.of(resource.setWindow(window)),
                this.parameters(),
                this.context(),
                Optional.of(
                        SpreadsheetDelta.EMPTY
                                .setCells(Sets.of(saved1))
                                .setWindow(window)
                )
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createHandler(), SpreadsheetEngine.class.getSimpleName() + ".fillCells");
    }

    @Override
    SpreadsheetDeltaHateosResourceHandlerFillCells createHandler(final SpreadsheetEngine engine) {
        return SpreadsheetDeltaHateosResourceHandlerFillCells.with(engine);
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
    public SpreadsheetCellReference id() {
        return SpreadsheetSelection.A1;
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetSelection.parseCellRange(TO)
                .range(); // url has TO
    }

    private SpreadsheetCellRangeReference toSpreadsheetCellRangeReference() {
        return SpreadsheetExpressionReference.parseCellRange(TO);
    }

    private final static String TO = "B1:C2";

    private final static String FROM = "E1:F2";

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.of(
                this.deltaWithCell()
        );
    }

    private SpreadsheetDelta deltaWithCell() {
        return SpreadsheetDelta.EMPTY
                .setCells(Sets.of(this.cell()));
    }

    @Override
    public SpreadsheetHateosResourceHandlerContext context() {
        return CONTEXT;
    }

    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerFillCells> type() {
        return SpreadsheetDeltaHateosResourceHandlerFillCells.class;
    }
}
