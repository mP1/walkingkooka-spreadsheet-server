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
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that uses {@link SpreadsheetEngine#fillCells(Collection, SpreadsheetCellRangeReference, SpreadsheetCellRangeReference, SpreadsheetEngineContext)} to
 * clear a column or range of columns
 */
final class SpreadsheetDeltaHateosResourceHandlerClearColumns extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetColumnReference>
        implements UnsupportedHateosResourceHandlerHandleAll<SpreadsheetColumnReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetDeltaHateosResourceHandlerClearColumns with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerClearColumns(
                check(engine)
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerClearColumns(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetColumnReference column,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(column, "column");

        return this.clearCells(
                column.range(column),
                resource,
                parameters,
                context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetColumnReference> columns,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(columns, "columns");

        return this.clearCells(
                columns,
                resource,
                parameters,
                context
        );
    }

    private Optional<SpreadsheetDelta> clearCells(final Range<SpreadsheetColumnReference> columns,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceNotEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetCellReference lower = columns.lowerBound()
                .value()
                .get()
                .setRow(SpreadsheetReferenceKind.RELATIVE.firstRow());
        final SpreadsheetCellReference upper = columns.upperBound()
                .value()
                .get()
                .setRow(SpreadsheetReferenceKind.RELATIVE.lastRow());

        final SpreadsheetCellRangeReference cellRange = lower.cellRange(upper);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        context,
                        this.engine.fillCells(
                                SpreadsheetDelta.NO_CELLS,
                                cellRange, // from is ignored because cells is empty.
                                cellRange,
                                context
                        )
                )
        );
    }

    @Override
    String operation() {
        return "clearColumns"; // SpreadsheetEngine#fillCells
    }
}
