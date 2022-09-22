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
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#deleteCells(SpreadsheetSelection, SpreadsheetEngineContext)}.
 * Deleting more than one cell is not supported.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell extends SpreadsheetEngineHateosHandlerSpreadsheetDelta2<SpreadsheetCellReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell with(final SpreadsheetEngine engine,
                                                                         final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteCell(final SpreadsheetEngine engine,
                                                                     final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkCell(cell);

        return deleteCells(
                resource,
                parameters,
                cell
        );
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> rangeOfCells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        return deleteCells(
                resource,
                parameters,
                SpreadsheetSelection.cellRange(rangeOfCells)
        );
    }

    private Optional<SpreadsheetDelta> deleteCells(final Optional<SpreadsheetDelta> resource,
                                                   final Map<HttpRequestAttribute<?>, Object> parameters,
                                                   final SpreadsheetSelection cells) {
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.engine.deleteCells(
                                cells,
                                this.context
                        )
                )
        );
    }

    @Override
    String operation() {
        return "deleteCell";
    }
}
