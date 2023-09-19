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

package walkingkooka.spreadsheet.server.engine.http;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosHandler} that uses {@link SpreadsheetEngine#fillCells(Collection, SpreadsheetCellRange, SpreadsheetCellRange, SpreadsheetEngineContext)} to
 * clear a column or range of columns
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns extends SpreadsheetEngineHateosHandlerSpreadsheetDelta2<SpreadsheetColumnReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns with(final SpreadsheetEngine engine,
                                                                           final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearColumns(final SpreadsheetEngine engine,
                                                                       final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetColumnReference column,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(column, "column");

        return this.clearCells(
                column.range(column),
                resource,
                parameters
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetColumnReference> columns,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(columns, "columns");

        return this.clearCells(
                columns,
                resource,
                parameters
        );
    }

    private Optional<SpreadsheetDelta> clearCells(final Range<SpreadsheetColumnReference> columns,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(columns, "columns");
        HateosHandler.checkResourceNotEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final SpreadsheetCellReference lower = columns.lowerBound()
                .value()
                .get()
                .setRow(SpreadsheetReferenceKind.RELATIVE.firstRow());
        final SpreadsheetCellReference upper = columns.upperBound()
                .value()
                .get()
                .setRow(SpreadsheetReferenceKind.RELATIVE.lastRow());

        final SpreadsheetCellRange cellRange = lower.cellRange(upper);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.engine.fillCells(
                                SpreadsheetDelta.NO_CELLS,
                                cellRange, // from is ignored because cells is empty.
                                cellRange,
                                this.context
                        )
                )
        );
    }

    @Override
    String operation() {
        return "clearColumns"; // SpreadsheetEngine#fillCells
    }
}
