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

package walkingkooka.spreadsheet.server.engine;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;

import java.util.Objects;

final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn extends SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsert<SpreadsheetColumnReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn with(final SpreadsheetEngine engine,
                                                                                 final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsertBeforeColumn(final SpreadsheetEngine engine,
                                                                             final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    void checkReference(final SpreadsheetColumnReference column) {
        Objects.requireNonNull(column, "column");
    }

    @Override
    String rangeLabel() {
        return "column range";
    }

    @Override
    SpreadsheetDelta insert(final SpreadsheetColumnReference column,
                            final int count) {
        return this.insertBefore(
                column,
                count
        );
    }

    @Override
    SpreadsheetDelta insert(final Range<SpreadsheetColumnReference> column,
                            final int count) {
        return this.insertBefore(
                column.lowerBound()
                        .value()
                        .get(),
                count
        );
    }

    private SpreadsheetDelta insertBefore(final SpreadsheetColumnReference column,
                                          final int count) {
        return this.engine.insertColumns(
                column,
                count,
                this.context
        );
    }

    @Override
    String operation() {
        return "columnInsertBefore";
    }

    /**
     * <pre>
     * INFO] Caused by: java.lang.IllegalStateException: An alias was needed for walkingkooka.net.http.server.HttpRequestAttribute<?Lwalkingkooka/net/http/server/HttpRequestAttribute;:TT;> but no alias was found.
     * [INFO]  at com.google.common.base.Preconditions.checkState(Preconditions.java:585)
     * [INFO] [WARNING] Killing all running tasks
     * [INFO]  at com.google.j2cl.generator.GenerationEnvironment.aliasForType(GenerationEnvironment.java:65)
     * [INFO]  at com.google.j2cl.generator.ClosureTypesGenerator.getClosureTypeForDeclaration(ClosureTypesGenerator.java:298)
     * [INFO]  at com.google.j2cl.generator.ClosureTypesGenerator.getClosureType(ClosureTypesGenerator.java:115)
     * </pre>
     * Needed to avoid the above failure. Note this method is pruned from output.
     */
    private void dummy(final HttpRequestAttribute<?> ignored){
    }
}
