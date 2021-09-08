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
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.Objects;

final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsert<SpreadsheetRowReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow with(final SpreadsheetEngine engine,
                                                                              final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeRow(final SpreadsheetEngine engine,
                                                                          final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    void checkReference(final SpreadsheetRowReference row) {
        Objects.requireNonNull(row, "row");
    }

    @Override
    String rangeLabel() {
        return "row range";
    }

    @Override
    SpreadsheetDelta insert(final SpreadsheetRowReference row,
                            final int count) {
        return this.engine.insertRows(
                row.addSaturated(-count),
                count,
                this.context
        );
    }

    @Override
    SpreadsheetDelta insert(final Range<SpreadsheetRowReference> row,
                            final int count) {
        return this.engine.insertRows(
                row.lowerBound().value().get().addSaturated(-count),
                count,
                this.context
        );
    }

    @Override
    String operation() {
        return "rowInsertBefore";
    }
}
