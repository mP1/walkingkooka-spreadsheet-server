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

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.Objects;

/**
 * A {@link HateosHandler} for {@link SpreadsheetEngine#deleteRows(SpreadsheetRowReference, int, SpreadsheetEngineContext)}.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteRows extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaDelete<SpreadsheetRowReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteRows with(final SpreadsheetEngine engine,
                                                                         final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteRows(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaDeleteRows(final SpreadsheetEngine engine,
                                                                     final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    void checkReference(final SpreadsheetRowReference row) {
        Objects.requireNonNull(row, "row");
    }

    @Override
    String rangeLabel() {
        return "rows";
    }

    @Override
    SpreadsheetDelta execute(final SpreadsheetRowReference row, final int count) {
        return this.engine.deleteRows(
                row,
                count,
                this.context
        );
    }

    @Override
    String operation() {
        return "deleteRows";
    }

    // FIXME https://github.com/mP1/walkingkooka-spreadsheet-server/issues/81
    private HttpRequestAttribute<?> dummy() {
        throw new UnsupportedOperationException();
    }
}
