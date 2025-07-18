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
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Objects;

final class SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow extends SpreadsheetDeltaHateosResourceHandlerInsert<SpreadsheetRowReference> {

    final static SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow INSTANCE = new SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow();

    private SpreadsheetDeltaHateosResourceHandlerInsertBeforeRow() {
        super();
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
    SpreadsheetDelta insert(final SpreadsheetRowReference column,
                            final int count,
                            final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.insertBefore(
            column,
            count,
            context
        );
    }

    @Override
    SpreadsheetDelta insert(final Range<SpreadsheetRowReference> column,
                            final int count,
                            final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.insertBefore(
            column.lowerBound()
                .value()
                .get(),
            count,
            context
        );
    }

    private SpreadsheetDelta insertBefore(final SpreadsheetRowReference column,
                                          final int count,
                                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.spreadsheetEngine()
            .insertRows(
                column,
                count,
                context
            );
    }

    @Override
    String operation() {
        return "rowInsertBefore";
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
    private static void dummy(final HttpRequestAttribute<?> ignored) {
    }
}
