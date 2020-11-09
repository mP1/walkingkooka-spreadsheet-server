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

package walkingkooka.spreadsheet.server.engine.hateos;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#deleteCell(SpreadsheetCellReference, SpreadsheetEngineContext)}.
 * Deleting more than one cell is not supported.
 */
final class SpreadsheetEngineDeleteCellHateosHandler extends SpreadsheetEngineHateosHandler<SpreadsheetCellReference> {

    static SpreadsheetEngineDeleteCellHateosHandler with(final SpreadsheetEngine engine,
                                                         final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineDeleteCellHateosHandler(engine, context);
    }

    private SpreadsheetEngineDeleteCellHateosHandler(final SpreadsheetEngine engine,
                                                     final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetDelta> handle(final Optional<SpreadsheetCellReference> id,
                                             final Optional<SpreadsheetDelta> resource,
                                             final Map<HttpRequestAttribute<?>, Object> parameters) {
        this.checkIdNotNull(id);

        final SpreadsheetDelta delta = this.checkResourceNotEmpty(resource);
        final Set<SpreadsheetCell> cells = delta.cells();
        if (false == cells.isEmpty()) {
            throw new IllegalArgumentException("Expected no cells got " + cells.size());
        }
        this.checkParameters(parameters);

        return Optional.of(applyWindowAddMaxColumnWidthsMaxRowHeights(this.engine.deleteCell(cells.iterator().next().reference(), this.context),
                resource));
    }

    @Override
    public Optional<SpreadsheetDelta> handleCollection(final Range<SpreadsheetCellReference> ids,
                                                       final Optional<SpreadsheetDelta> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    String operation() {
        return "deleteCell";
    }
}
