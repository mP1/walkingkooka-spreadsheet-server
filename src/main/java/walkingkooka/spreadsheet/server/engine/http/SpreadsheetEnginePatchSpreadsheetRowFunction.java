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
import walkingkooka.spreadsheet.SpreadsheetRow;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetRowReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
final class SpreadsheetEnginePatchSpreadsheetRowFunction extends SpreadsheetEnginePatch<SpreadsheetRowReference> {

    static SpreadsheetEnginePatchSpreadsheetRowFunction with(final HttpRequest request,
                                                             final SpreadsheetEngine engine,
                                                             final SpreadsheetEngineContext context) {
        return new SpreadsheetEnginePatchSpreadsheetRowFunction(request, engine, context);
    }

    private SpreadsheetEnginePatchSpreadsheetRowFunction(final HttpRequest request,
                                                         final SpreadsheetEngine engine,
                                                         final SpreadsheetEngineContext context) {
        super(request, engine, context);
    }

    @Override
    SpreadsheetRowReference parseReference(final String text) {
        return SpreadsheetSelection.parseRow(text);
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetRowReference reference) {
        return this.engine.loadRow(
                reference,
                this.context
        );
    }

    @Override
    JsonNode preparePatch(final JsonNode delta) {
        return delta;
    }

    @Override
    SpreadsheetDelta patch(final SpreadsheetDelta delta,
                           final JsonNode patch,
                           final JsonNodeUnmarshallContext context) {
        return delta.patchRows(
                patch,
                context
        );
    }

    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetRowReference reference) {
        final Optional<SpreadsheetRow> row = patched.row(reference);

        if (!row.isPresent()) {
            throw new IllegalArgumentException("Missing row " + reference);
        }

        return this.engine.saveRow(
                row.get(),
                this.context
        );
    }

    @Override
    String toStringPrefix() {
        return "Patch row: ";
    }
}
