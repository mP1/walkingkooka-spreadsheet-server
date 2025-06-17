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
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#deleteCells(SpreadsheetSelection, SpreadsheetEngineContext)}.
 * Deleting more than one cell is not supported.
 */
final class SpreadsheetDeltaHateosResourceHandlerDeleteCell extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference>
    implements UnsupportedHateosResourceHandlerHandleAll<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetDeltaHateosResourceHandlerDeleteCell with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerDeleteCell(engine);
    }

    private SpreadsheetDeltaHateosResourceHandlerDeleteCell(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        return deleteCells(
            resource,
            parameters,
            checkCell(cell),
            context
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> rangeOfCells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        return deleteCells(
            resource,
            parameters,
            SpreadsheetSelection.cellRange(rangeOfCells),
            context
        );
    }

    private Optional<SpreadsheetDelta> deleteCells(final Optional<SpreadsheetDelta> resource,
                                                   final Map<HttpRequestAttribute<?>, Object> parameters,
                                                   final SpreadsheetSelection cells,
                                                   final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            this.prepareResponse(
                resource,
                parameters,
                context,
                this.engine.deleteCells(
                    cells,
                    context
                )
            )
        );
    }

    @Override
    String operation() {
        return "deleteCell";
    }
}
