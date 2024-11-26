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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

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

    static SpreadsheetDeltaHateosResourceHandlerSortCells with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerSortCells(
                check(engine)
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerSortCells(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.handleRange(
                SpreadsheetSelection.ALL_CELLS.range(),
                resource,
                parameters,
                context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkCell(cell);

        return this.handleRange(
                cell.range(cell),
                resource,
                parameters,
                context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> cells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkIdRange(cells);

        return this.sortCells(
                SpreadsheetSelection.cellRange(cells),
                resource,
                parameters,
                context
        );
    }

    private Optional<SpreadsheetDelta> sortCells(final SpreadsheetCellRangeReference cells,
                                                 final Optional<SpreadsheetDelta> resource,
                                                 final Map<HttpRequestAttribute<?>, Object> parameters,
                                                 final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.ofNullable(
                this.engine.sortCells(
                        cells, // cells
                        SpreadsheetDeltaUrlQueryParameters.comparators(parameters),
                        SpreadsheetDeltaProperties.extract(parameters),
                        context
                )
        );
    }

    @Override
    String operation() {
        return "sortCells";
    }
}
