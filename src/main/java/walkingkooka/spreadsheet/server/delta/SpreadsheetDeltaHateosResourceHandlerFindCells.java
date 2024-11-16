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
import walkingkooka.spreadsheet.SpreadsheetValueType;
import walkingkooka.spreadsheet.engine.SpreadsheetCellFindQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReferencePath;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.tree.expression.Expression;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#findCells(SpreadsheetCellRangeReference, SpreadsheetCellRangeReferencePath, int, int, String, Expression, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerFindCells extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference> {

    static SpreadsheetDeltaHateosResourceHandlerFindCells with(final int defaultMax,
                                                               final SpreadsheetEngine engine) {
        if (defaultMax < 0) {
            throw new IllegalArgumentException("Invalid default max " + defaultMax + " < 0");
        }

        return new SpreadsheetDeltaHateosResourceHandlerFindCells(
                defaultMax,
                check(engine)
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerFindCells(final int defaultMax,
                                                           final SpreadsheetEngine engine) {
        super(engine);
        this.defaultMax = defaultMax;
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetHateosResourceHandlerContext context) {
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
                                                final SpreadsheetHateosResourceHandlerContext context) {
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
                                                  final SpreadsheetHateosResourceHandlerContext context) {
        HateosResourceHandler.checkIdRange(cells);

        return this.findCells(
                SpreadsheetSelection.cellRange(cells),
                resource,
                parameters,
                context
        );
    }

    private Optional<SpreadsheetDelta> findCells(final SpreadsheetCellRangeReference cells,
                                                 final Optional<SpreadsheetDelta> resource,
                                                 final Map<HttpRequestAttribute<?>, Object> parameters,
                                                 final SpreadsheetHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetCellFindQuery find = SpreadsheetCellFindQuery.extract(parameters);

        return Optional.ofNullable(
                SpreadsheetDelta.EMPTY.setCells(
                        this.engine.findCells(
                                cells, // cells
                                find.path().orElse(DEFAULT_CELL_RANGE_PATH), // path
                                find.offset().orElse(DEFAULT_OFFSET), // offset
                                find.max().orElse(this.defaultMax), // max
                                find.valueType().orElse(DEFAULT_VALUE_TYPE), // valueType
                                find.query()
                                        .map(q -> context.toExpression(
                                                q.parserToken()
                                                ).orElse(DEFAULT_QUERY)
                                        ).orElse(DEFAULT_QUERY), // query
                                context
                        )
                )
        );
    }

    final static SpreadsheetCellRangeReferencePath DEFAULT_CELL_RANGE_PATH = SpreadsheetCellRangeReferencePath.LRTD;

    final static Integer DEFAULT_OFFSET = 0;

    final static String DEFAULT_VALUE_TYPE = SpreadsheetValueType.ANY;

    private final int defaultMax;

    final static Expression DEFAULT_QUERY = Expression.value(true);

    @Override
    String operation() {
        return "findCells";
    }
}
