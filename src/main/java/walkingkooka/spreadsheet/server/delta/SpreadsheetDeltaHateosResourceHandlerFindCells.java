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
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetValueType;
import walkingkooka.spreadsheet.engine.SpreadsheetCellFindQuery;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReferencePath;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.expression.Expression;
import walkingkooka.validation.ValidationValueTypeName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#findCells(SpreadsheetCellRangeReference, SpreadsheetCellRangeReferencePath, int, int, ValidationValueTypeName, Expression, Set, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerFindCells extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference> {

    /**
     * Singleton
     */
    final static SpreadsheetDeltaHateosResourceHandlerFindCells INSTANCE = new SpreadsheetDeltaHateosResourceHandlerFindCells();

    private SpreadsheetDeltaHateosResourceHandlerFindCells() {
        super();
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.handleRange(
            SpreadsheetSelection.ALL_CELLS.range(),
            resource,
            parameters,
            path,
            context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.handleRange(
            Objects.requireNonNull(cell, "cell")
                .range(cell),
            resource,
            parameters,
            path,
            context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> cells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final UrlPath path,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkIdRange(cells);

        return this.findCells(
            SpreadsheetSelection.cellRange(cells),
            resource,
            parameters,
            path,
            context
        );
    }

    private Optional<SpreadsheetDelta> findCells(final SpreadsheetCellRangeReference cells,
                                                 final Optional<SpreadsheetDelta> resource,
                                                 final Map<HttpRequestAttribute<?>, Object> parameters,
                                                 final UrlPath path,
                                                 final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetCellFindQuery find = SpreadsheetCellFindQuery.extract(parameters);

        return Optional.ofNullable(
            context.spreadsheetEngine()
                .findCells(
                    cells, // cells
                    find.path().orElse(DEFAULT_CELL_RANGE_PATH), // path
                    find.offset().orElse(DEFAULT_OFFSET), // offset
                    find.count().orElse(DEFAULT_COUNT), // count
                    find.valueType().orElse(DEFAULT_VALUE_TYPE), // valueType
                    find.query()
                        .map(q -> context.toExpression(
                                q.parserToken()
                            ).orElse(DEFAULT_QUERY)
                        ).orElse(DEFAULT_QUERY), // query
                    SpreadsheetDeltaProperties.extract(parameters),
                    context
                )
        );
    }

    final static SpreadsheetCellRangeReferencePath DEFAULT_CELL_RANGE_PATH = SpreadsheetCellRangeReferencePath.LRTD;

    final static Integer DEFAULT_OFFSET = 0;

    final static ValidationValueTypeName DEFAULT_VALUE_TYPE = SpreadsheetValueType.ANY;

    final static int DEFAULT_COUNT = 50;

    final static Expression DEFAULT_QUERY = Expression.value(true);

    @Override
    String operation() {
        return "findCells";
    }
}
