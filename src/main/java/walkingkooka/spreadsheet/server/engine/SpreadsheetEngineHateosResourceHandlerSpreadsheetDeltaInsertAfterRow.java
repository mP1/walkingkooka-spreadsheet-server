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
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;

import java.util.Objects;

final class SpreadsheetEngineHateosResourceHandlerSpreadsheetDeltaInsertAfterRow extends SpreadsheetEngineHateosResourceHandlerSpreadsheetDeltaInsert<SpreadsheetRowReference> {

    static SpreadsheetEngineHateosResourceHandlerSpreadsheetDeltaInsertAfterRow with(final SpreadsheetEngine engine,
                                                                                     final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosResourceHandlerSpreadsheetDeltaInsertAfterRow(engine, context);
    }

    private SpreadsheetEngineHateosResourceHandlerSpreadsheetDeltaInsertAfterRow(final SpreadsheetEngine engine,
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
        return this.insertAfter(
                row.addSaturated(1),
                count
        );
    }

    @Override
    SpreadsheetDelta insert(final Range<SpreadsheetRowReference> row,
                            final int count) {
        return this.insertAfter(
                row.upperBound()
                        .value()
                        .get()
                        .addSaturated(1),
                count
        );
    }

    private SpreadsheetDelta insertAfter(final SpreadsheetRowReference row,
                                         final int count) {
        return this.engine.insertRows(
                row,
                count,
                this.context
        );
    }

    @Override
    String operation() {
        return "rowRowAfter";
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
    private static void dummy(final HttpRequestAttribute<?> ignored){
    }
}
