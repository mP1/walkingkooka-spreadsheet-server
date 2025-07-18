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
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.compare.SpreadsheetColumnOrRowSpreadsheetComparatorNames;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.formula.SpreadsheetFormula;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContexts;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetDeltaHateosResourceHandlerSortCellsTest extends SpreadsheetDeltaHateosResourceHandlerTestCase2<SpreadsheetDeltaHateosResourceHandlerSortCells,
    SpreadsheetCellReference> {

    @Test
    public void testHandleOneMissingComparatorsParameter() {
        final SpreadsheetCellReference cell = SpreadsheetSelection.A1;
        final String comparators = "A=day-of-month";

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(
            Sets.of(
                cell.setFormula(SpreadsheetFormula.EMPTY.setText("=1"))
            )
        );

        this.handleOneFails(
            cell, // reference
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(
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
                }
            ),
            IllegalArgumentException.class
        );
    }

    @Test
    public void testHandleOne() {
        final SpreadsheetCellReference cell = SpreadsheetSelection.A1;
        final String comparators = "A=day-of-month";

        final SpreadsheetDelta delta = SpreadsheetDelta.EMPTY.setCells(
            Sets.of(
                cell.setFormula(SpreadsheetFormula.EMPTY.setText("=1"))
            )
        );

        this.handleOneAndCheck(
            cell, // reference
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetDeltaUrlQueryParameters.COMPARATORS, Lists.of(comparators)
            ), // parameters
            UrlPath.EMPTY,
            this.context(
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
                }
            ),
            Optional.of(
                delta
            )
        );
    }

    @Test
    public void testHandleRange() {
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
            cellRange.range(), // reference
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetDeltaUrlQueryParameters.COMPARATORS,
                Lists.of(comparators)
            ), // parameters
            UrlPath.EMPTY,
            this.context(
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
                }
            ),
            Optional.of(
                delta
            )
        );
    }

    @Test
    public void testHandleAll() {
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
            Optional.empty(), // resource
            Maps.of(
                SpreadsheetDeltaUrlQueryParameters.COMPARATORS,
                Lists.of(comparators)
            ), // parameters
            UrlPath.EMPTY,
            this.context(
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
                }
            ),
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
    public SpreadsheetDeltaHateosResourceHandlerSortCells createHandler() {
        return SpreadsheetDeltaHateosResourceHandlerSortCells.INSTANCE;
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

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // ClassTesting.....................................................................................................
    @Override
    public Class<SpreadsheetDeltaHateosResourceHandlerSortCells> type() {
        return SpreadsheetDeltaHateosResourceHandlerSortCells.class;
    }
}
