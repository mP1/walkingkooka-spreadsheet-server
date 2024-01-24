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

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetValueType;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangePath;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.expression.Expression;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#findCells(SpreadsheetCellRange, SpreadsheetCellRangePath, int, int, String, Expression, SpreadsheetEngineContext)}.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaFindCells extends SpreadsheetEngineHateosHandlerSpreadsheetDelta<SpreadsheetCellReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaFindCells with(final int defaultMax,
                                                                        final SpreadsheetEngine engine,
                                                                        final SpreadsheetEngineContext context) {
        if (defaultMax < 0) {
            throw new IllegalArgumentException("Invalid default max " + defaultMax + " < 0");
        }
        check(
                engine,
                context
        );

        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaFindCells(
                defaultMax,
                engine,
                context
        );
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaFindCells(final int defaultMax,
                                                                    final SpreadsheetEngine engine,
                                                                    final SpreadsheetEngineContext context) {
        super(engine, context);
        this.defaultMax = defaultMax;
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
        HateosHandler.checkRange(cells);

        return this.findCells(
                SpreadsheetSelection.cellRange(cells),
                resource,
                parameters
        );
    }

    private Optional<SpreadsheetDelta> findCells(final SpreadsheetCellRange cells,
                                                 final Optional<SpreadsheetDelta> resource,
                                                 final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final SpreadsheetEngineContext context = this.context;

        final Optional<SpreadsheetCellRangePath> path = SpreadsheetEngineHttps.cellRangePath(parameters);
        final Optional<Integer> offset = SpreadsheetEngineHttps.offset(parameters);
        final Optional<Integer> max = SpreadsheetEngineHttps.max(parameters);
        final Optional<String> valueType = SpreadsheetEngineHttps.valueType(parameters, context);
        final Optional<Expression> query = SpreadsheetEngineHttps.query(parameters, context);

        return Optional.ofNullable(
                SpreadsheetDelta.EMPTY.setCells(
                        this.engine.findCells(
                                cells, // cells
                                path.orElse(DEFAULT_CELL_RANGE_PATH), // path
                                offset.orElse(DEFAULT_OFFSET), // offset
                                max.orElse(this.defaultMax), // max
                                valueType.orElse(DEFAULT_VALUE_TYPE), // valueType
                                query.orElse(DEFAULT_QUERY), // query
                                context
                        )
                )
        );
    }

    final static SpreadsheetCellRangePath DEFAULT_CELL_RANGE_PATH = SpreadsheetCellRangePath.LRTD;

    final static Integer DEFAULT_OFFSET = 0;

    final static String DEFAULT_VALUE_TYPE = SpreadsheetValueType.ANY;

    private final int defaultMax;

    final static Expression DEFAULT_QUERY = Expression.value(true);

    @Override
    String operation() {
        return "findCells";
    }
}
