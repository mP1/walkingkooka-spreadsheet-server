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

import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;

import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
final class SpreadsheetEnginePatchSpreadsheetCellFunction extends SpreadsheetEnginePatch<SpreadsheetCellReference> {

    static SpreadsheetEnginePatchSpreadsheetCellFunction with(final HttpRequest request,
                                                              final SpreadsheetEngine engine,
                                                              final SpreadsheetEngineContext context) {
        return new SpreadsheetEnginePatchSpreadsheetCellFunction(request, engine, context);
    }

    private SpreadsheetEnginePatchSpreadsheetCellFunction(final HttpRequest request,
                                                          final SpreadsheetEngine engine,
                                                          final SpreadsheetEngineContext context) {
        super(request, engine, context);
    }

    @Override
    SpreadsheetCellReference parseReference(final String text) {
        return SpreadsheetSelection.parseCellOrLabelResolvingLabels(
                text,
                l -> this.context.storeRepository()
                        .labels()
                        .cellReferenceOrFail(l)
        );
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetCellReference reference) {
        return this.engine.loadCell(
                reference,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                this.context
        );
    }

    @Override
    JsonObject preparePatch(final JsonNode delta) {
        return SpreadsheetDelta.resolveCellLabels(
                delta.objectOrFail(),
                this.context.storeRepository()
                        .labels()
                        ::cellReferenceOrFail
        );
    }

    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetCellReference reference) {
        return this.engine.saveCell(
                patched.cell(reference)
                        .orElseThrow(() -> new IllegalStateException("Missing patched cell " + reference)),
                this.context
        );
    }

    @Override
    String toStringPrefix() {
        return "Patch cell: ";
    }
}