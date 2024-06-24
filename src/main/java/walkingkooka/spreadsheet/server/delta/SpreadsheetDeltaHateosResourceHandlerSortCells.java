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

import walkingkooka.collect.Range;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.compare.SpreadsheetColumnOrRowSpreadsheetComparatorNames;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#sortCells(SpreadsheetCellRangeReference, List, Set, SpreadsheetEngineContext)}.
 * <pre>
 * /api/spreadsheet/1/cells/A1:B2?comparators=A=day-of-month,month-of-year,year
 * </pre>
 */
final class SpreadsheetDeltaHateosResourceHandlerSortCells extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference> {

    static SpreadsheetDeltaHateosResourceHandlerSortCells with(final SpreadsheetEngine engine,
                                                               final SpreadsheetEngineContext context) {
        check(
                engine,
                context
        );

        return new SpreadsheetDeltaHateosResourceHandlerSortCells(
                engine,
                context
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerSortCells(final SpreadsheetEngine engine,
                                                           final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        return this.handleRange(
                SpreadsheetSelection.ALL_CELLS.range(),
                resource,
                parameters
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkCell(cell);

        return this.handleRange(
                cell.range(cell),
                resource,
                parameters
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> cells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkIdRange(cells);

        return this.sortCells(
                SpreadsheetSelection.cellRange(cells),
                resource,
                parameters
        );
    }

    private Optional<SpreadsheetDelta> sortCells(final SpreadsheetCellRangeReference cells,
                                                 final Optional<SpreadsheetDelta> resource,
                                                 final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);

        return Optional.ofNullable(
                this.engine.sortCells(
                        cells, // cells
                        comparators(parameters),
                        SpreadsheetDeltaUrlQueryParameters.deltaProperties(parameters),
                        this.context
                )
        );
    }

    private static List<SpreadsheetColumnOrRowSpreadsheetComparatorNames> comparators(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return COMPARATORS.firstParameterValue(parameters)
                .map(SpreadsheetColumnOrRowSpreadsheetComparatorNames::parseList)
                .orElseThrow(() -> new IllegalArgumentException("Missing required " + COMPARATORS));
    }

    /**
     * Required parameter holding the columns/rows to be sorted.
     */
    final static UrlParameterName COMPARATORS = UrlParameterName.with("comparators");

    @Override
    String operation() {
        return "sortCells";
    }
}
