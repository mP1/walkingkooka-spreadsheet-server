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
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that finds {@link SpreadsheetCellReference} for a given {@link SpreadsheetExpressionReference}.
 */
final class SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference> {

    static SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences with(final int defaultCount,
                                                                             final SpreadsheetEngine engine) {
        if (defaultCount < 0) {
            throw new IllegalArgumentException("Invalid default count " + defaultCount + " < 0");
        }

        return new SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences(
            defaultCount,
            engine
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerFindCellsWithReferences(final int defaultCount,
                                                                         final SpreadsheetEngine engine) {
        super(engine);
        this.defaultCount = defaultCount;
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.findCellsWithReferences(
            SpreadsheetSelection.ALL_CELLS,
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

        return this.findCellsWithReferences(
            cell,
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

        return this.findCellsWithReferences(
            SpreadsheetSelection.cellRange(cells),
            resource,
            parameters,
            context
        );
    }

    private Optional<SpreadsheetDelta> findCellsWithReferences(final SpreadsheetExpressionReference reference,
                                                               final Optional<SpreadsheetDelta> resource,
                                                               final Map<HttpRequestAttribute<?>, Object> parameters,
                                                               final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            this.engine.findCellsWithReference(
                reference,
                SpreadsheetUrlQueryParameters.offset(parameters)
                    .orElse(0),
                SpreadsheetUrlQueryParameters.count(parameters)
                    .orElse(this.defaultCount),
                context
            )
        );
    }

    private final int defaultCount;

    @Override
    String operation() {
        return "findCellsWithReferences";
    }
}
