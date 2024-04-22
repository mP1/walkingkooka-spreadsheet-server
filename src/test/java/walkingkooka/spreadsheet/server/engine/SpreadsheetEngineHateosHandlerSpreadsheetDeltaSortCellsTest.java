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

package walkingkooka.spreadsheet.server.engine;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.compare.SpreadsheetColumnOrRowSpreadsheetComparatorNames;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCellsTest extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaTestCase<SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells,
        SpreadsheetCellReference> {

    @Test
    public void testOneMissingComparatorsParameter() {
        final SpreadsheetCellReference cell = SpreadsheetSelection.A1;
        final String comparators = "A=day-of-month";

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(
                Sets.of(
                        cell.setFormula(SpreadsheetFormula.EMPTY.setText("=1"))
                )
        );

        this.handleOneFails(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.with(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta sortCells(final SpreadsheetCellRangeReference cellRange,
                                                              final List<SpreadsheetColumnOrRowSpreadsheetComparatorNames> c,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(cell.toCellRange(), cellRange, "cellRange");
                                checkEquals(SpreadsheetColumnOrRowSpreadsheetComparatorNames.parseList(comparators), c);
                                return delta;
                            }
                        },
                        SpreadsheetEngineContexts.fake()
                ),
                cell, // reference
                Optional.empty(), // resource
                Maps.empty(), // missing COMPARATORS parameters
                IllegalArgumentException.class
        );
    }

    @Test
    public void testOne() {
        final SpreadsheetCellReference cell = SpreadsheetSelection.A1;
        final String comparators = "A=day-of-month";

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(
                Sets.of(
                        cell.setFormula(SpreadsheetFormula.EMPTY.setText("=1"))
                )
        );

        this.handleOneAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.with(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta sortCells(final SpreadsheetCellRangeReference cellRange,
                                                              final List<SpreadsheetColumnOrRowSpreadsheetComparatorNames> c,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(cell.toCellRange(), cellRange, "cellRange");
                                checkEquals(SpreadsheetColumnOrRowSpreadsheetComparatorNames.parseList(comparators), c);
                                return delta;
                            }
                        },
                        SpreadsheetEngineContexts.fake()
                ),
                cell, // reference
                Optional.empty(), // resource
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.COMPARATORS, Lists.of(comparators)
                ), // parameters
                Optional.of(
                        delta
                )
        );
    }

    @Test
    public void testRange() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetCell a1Cell = a1.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell b2Cell = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("=200")
        );

        final SpreadsheetCellRangeReference cellRange = SpreadsheetSelection.parseCellRange("A1:B2");
        final String comparators = "A=month-of-year";

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(
                Sets.of(
                        a1Cell,
                        b2Cell
                )
        );

        this.handleRangeAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.with(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta sortCells(final SpreadsheetCellRangeReference cellRange,
                                                              final List<SpreadsheetColumnOrRowSpreadsheetComparatorNames> c,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(cellRange.toCellRange(), cellRange, "cellRange");
                                checkEquals(SpreadsheetColumnOrRowSpreadsheetComparatorNames.parseList(comparators), c);
                                return delta;
                            }
                        },
                        SpreadsheetEngineContexts.fake()
                ),
                cellRange.range(), // reference
                Optional.empty(), // resource
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.COMPARATORS, Lists.of(comparators)
                ), // parameters
                Optional.of(
                        delta
                )
        );
    }

    @Test
    public void testAll() {
        final SpreadsheetCellReference a1 = SpreadsheetSelection.A1;
        final SpreadsheetCell a1Cell = a1.setFormula(
                SpreadsheetFormula.EMPTY.setText("=100")
        );

        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell b2Cell = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("=200")
        );

        final String comparators = "A=month-of-year";

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(
                Sets.of(
                        a1Cell,
                        b2Cell
                )
        );

        this.handleAllAndCheck(
                SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.with(
                        new FakeSpreadsheetEngine() {

                            @Override
                            public SpreadsheetDelta sortCells(final SpreadsheetCellRangeReference cellRange,
                                                              final List<SpreadsheetColumnOrRowSpreadsheetComparatorNames> c,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(cellRange.toCellRange(), cellRange, "cellRange");
                                checkEquals(SpreadsheetColumnOrRowSpreadsheetComparatorNames.parseList(comparators), c);
                                return delta;
                            }
                        },
                        SpreadsheetEngineContexts.fake()
                ),
                Optional.empty(), // resource
                Maps.of(
                        SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.COMPARATORS, Lists.of(comparators)
                ), // parameters
                Optional.of(
                        delta
                )
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                SpreadsheetEngine.class.getSimpleName() + ".sortCells"
        );
    }

    @Override
    SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells createHandler(final SpreadsheetEngine engine,
                                                                          final SpreadsheetEngineContext context) {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.with(
                engine,
                context
        );
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
    public SpreadsheetCellReference id() {
        return SpreadsheetSelection.A1;
    }

    @Override
    public Range<SpreadsheetCellReference> range() {
        return SpreadsheetSelection.parseCellRange("A1:B2")
                .range(); // url has TO
    }

    @Override
    public Optional<SpreadsheetDelta> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetDelta> collectionResource() {
        return Optional.empty();
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells> type() {
        return SpreadsheetEngineHateosHandlerSpreadsheetDeltaSortCells.class;
    }
}
