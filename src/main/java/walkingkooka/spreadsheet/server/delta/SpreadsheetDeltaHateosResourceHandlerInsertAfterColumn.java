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
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;

import java.util.Objects;

final class SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn extends SpreadsheetDeltaHateosResourceHandlerInsert<SpreadsheetColumnReference> {

    static SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn(
                check(engine)
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerInsertAfterColumn(final SpreadsheetEngine engine) {
        super(engine);
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
                            final int count,
                            final SpreadsheetEngineContext context) {
        return this.insertAfter(
                column.addSaturated(1),
                count,
                context
        );
    }

    @Override
    SpreadsheetDelta insert(final Range<SpreadsheetColumnReference> column,
                            final int count,
                            final SpreadsheetEngineContext context) {
        return this.insertAfter(
                column.upperBound()
                        .value()
                        .get()
                        .addSaturated(1),
                count,
                context
        );
    }

    private SpreadsheetDelta insertAfter(final SpreadsheetColumnReference column,
                                         final int count,
                                         final SpreadsheetEngineContext context) {
        return this.engine.insertColumns(
                column,
                count,
                context
        );
    }

    @Override
    String operation() {
        return "columnInsertAfter";
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
