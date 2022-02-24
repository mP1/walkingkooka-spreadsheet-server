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
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
final class SpreadsheetEnginePatchSpreadsheetColumnFunction extends SpreadsheetEnginePatch<SpreadsheetColumnReference> {

    static SpreadsheetEnginePatchSpreadsheetColumnFunction with(final HttpRequest request,
                                                                final SpreadsheetEngine engine,
                                                                final SpreadsheetEngineContext context) {
        return new SpreadsheetEnginePatchSpreadsheetColumnFunction(request, engine, context);
    }

    private SpreadsheetEnginePatchSpreadsheetColumnFunction(final HttpRequest request,
                                                            final SpreadsheetEngine engine,
                                                            final SpreadsheetEngineContext context) {
        super(request, engine, context);
    }

    @Override
    SpreadsheetColumnReference parseReference(final String text) {
        return SpreadsheetSelection.parseColumn(text);
    }

    @Override
    SpreadsheetDelta load(final SpreadsheetColumnReference reference) {
        return this.engine.loadColumn(
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
        return delta.patchColumns(
                patch,
                context
        );
    }

    @Override
    SpreadsheetDelta save(final SpreadsheetDelta patched,
                          final SpreadsheetColumnReference reference) {
        final Optional<SpreadsheetColumn> column = patched.column(reference);

        if (!column.isPresent()) {
            throw new IllegalArgumentException("Missing column " + reference);
        }

        return this.engine.saveColumn(
                column.get(),
                this.context
        );
    }

    @Override
    String toStringPrefix() {
        return "Patch column: ";
    }
}
