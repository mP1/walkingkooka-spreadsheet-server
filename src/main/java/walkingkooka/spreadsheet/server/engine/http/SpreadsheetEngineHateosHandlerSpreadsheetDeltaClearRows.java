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
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosHandler} that uses {@link SpreadsheetEngine#fillCells(Collection, SpreadsheetCellRange, SpreadsheetCellRange, SpreadsheetEngineContext)} to
 * clear a row or range of rows
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows extends SpreadsheetEngineHateosHandlerSpreadsheetDelta2<SpreadsheetRowReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows with(final SpreadsheetEngine engine,
                                                                        final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaClearRows(final SpreadsheetEngine engine,
                                                                    final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetRowReference row,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(row, "row");

        return this.clearCells(
                row.range(row),
                resource,
                parameters
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetRowReference> rows,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(rows, "rows");

        return this.clearCells(
                rows,
                resource,
                parameters
        );
    }

    private Optional<SpreadsheetDelta> clearCells(final Range<SpreadsheetRowReference> rows,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(rows, "rows");
        HateosHandler.checkResourceNotEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final SpreadsheetCellReference lower = rows.lowerBound()
                .value()
                .get()
                .setColumn(SpreadsheetReferenceKind.RELATIVE.firstColumn());
        final SpreadsheetCellReference upper = rows.upperBound()
                .value()
                .get()
                .setColumn(SpreadsheetReferenceKind.RELATIVE.lastColumn());

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
        return "clearRows"; // SpreadsheetEngine#fillCells
    }
}
