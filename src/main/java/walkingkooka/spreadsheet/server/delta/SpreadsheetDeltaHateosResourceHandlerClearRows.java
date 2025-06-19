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
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that uses {@link SpreadsheetEngine#fillCells(Collection, SpreadsheetCellRangeReference, SpreadsheetCellRangeReference, SpreadsheetEngineContext)} to
 * clear a row or range of rows
 */
final class SpreadsheetDeltaHateosResourceHandlerClearRows extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetRowReference>
    implements UnsupportedHateosResourceHandlerHandleAll<SpreadsheetRowReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetDeltaHateosResourceHandlerClearRows with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerClearRows(engine);
    }

    private SpreadsheetDeltaHateosResourceHandlerClearRows(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetRowReference row,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final UrlPath path,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(row, "row");

        return this.clearCells(
            row.range(row),
            resource,
            parameters,
            path,
            context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetRowReference> rows,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final UrlPath path,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        Objects.requireNonNull(rows, "rows");

        return this.clearCells(
            rows,
            resource,
            parameters,
            path,
            context
        );
    }

    private Optional<SpreadsheetDelta> clearCells(final Range<SpreadsheetRowReference> rows,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final UrlPath path,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetCellReference lower = rows.lowerBound()
            .value()
            .get()
            .setColumn(SpreadsheetReferenceKind.RELATIVE.firstColumn());
        final SpreadsheetCellReference upper = rows.upperBound()
            .value()
            .get()
            .setColumn(SpreadsheetReferenceKind.RELATIVE.lastColumn());

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
        return "clearRows"; // SpreadsheetEngine#fillCells
    }
}
