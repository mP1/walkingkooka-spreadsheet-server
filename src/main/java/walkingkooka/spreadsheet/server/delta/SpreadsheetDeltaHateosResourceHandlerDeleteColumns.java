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

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;

import java.util.Objects;

/**
 * A {@link HateosResourceHandler} for {@link SpreadsheetEngine#deleteColumns(SpreadsheetColumnReference, int, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerDeleteColumns extends SpreadsheetDeltaHateosResourceHandlerDelete<SpreadsheetColumnReference> {

    static SpreadsheetDeltaHateosResourceHandlerDeleteColumns with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerDeleteColumns(engine);
    }

    private SpreadsheetDeltaHateosResourceHandlerDeleteColumns(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    void checkReference(final SpreadsheetColumnReference column) {
        Objects.requireNonNull(column, "column");
    }

    @Override
    String rangeLabel() {
        return "columns";
    }

    @Override
    SpreadsheetDelta execute(final SpreadsheetColumnReference column,
                             final int count,
                             final SpreadsheetEngineContext context) {
        return this.engine.deleteColumns(
            column,
            count,
            context
        );
    }

    @Override
    String operation() {
        return "deleteColumns";
    }

    // FIXME https://github.com/mP1/walkingkooka-spreadsheet-server/issues/81
    private HttpRequestAttribute<?> dummy() {
        throw new UnsupportedOperationException();
    }
}
