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

import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponseHttpServerException;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.store.LoadStoreException;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonObject;

import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
final class SpreadsheetEnginePatchSpreadsheetCellFunction implements UnaryOperator<JsonNode> {

    static SpreadsheetEnginePatchSpreadsheetCellFunction with(final HttpRequest request,
                                                              final SpreadsheetEngine engine,
                                                              final SpreadsheetEngineContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(context, "context");

        return new SpreadsheetEnginePatchSpreadsheetCellFunction(request, engine, context);
    }

    private SpreadsheetEnginePatchSpreadsheetCellFunction(final HttpRequest request,
                                                          final SpreadsheetEngine engine,
                                                          final SpreadsheetEngineContext context) {
        super();
        this.request = request;
        this.engine = engine;
        this.context = context;
    }

    @Override
    public JsonNode apply(final JsonNode json) {
        final SpreadsheetCellReference reference = this.patchCellCellReference();
        final SpreadsheetEngine engine = this.engine;
        final SpreadsheetEngineContext context = this.context;

        final SpreadsheetDelta delta;
        try {
            delta = engine.loadCell(
                    reference,
                    SpreadsheetEngineEvaluation.SKIP_EVALUATE,
                    context
            );
        } catch (final LoadStoreException cause) {
            throw new HttpResponseHttpServerException(
                    HttpStatusCode.BAD_REQUEST
                            .setMessage(cause.getMessage()),
                    HttpResponseHttpServerException.NO_ENTITY
            );
        }

        final JsonObject resolved = SpreadsheetDelta.resolveCellLabels(
                json.objectOrFail(),
                context.storeRepository()
                        .labels()
                        ::cellReferenceOrFail
        );

        final SpreadsheetMetadata metadata = context.metadata();

        final SpreadsheetDelta patched = delta.patch(
                resolved,
                metadata.jsonNodeUnmarshallContext()
        );

        final SpreadsheetDelta saved = engine.saveCell(
                        patched.cell(reference)
                                .orElseThrow(() -> new IllegalStateException("Missing cell " + reference)),
                        context
                ).setWindow(patched.window())
                .setSelection(this.selection(patched.selection()));

        return metadata.jsonNodeMarshallContext()
                .marshall(saved);
    }


    private SpreadsheetCellReference patchCellCellReference() {
        return SpreadsheetSelection.parseCellOrLabelResolvingLabels(
                this.request.url()
                        .path()
                        .name()
                        .value(),
                l -> this.context.storeRepository()
                        .labels()
                        .cellReferenceOrFail(l)
        );
    }

    private final HttpRequest request;
    private final SpreadsheetEngine engine;
    private final SpreadsheetEngineContext context;

    private Optional<SpreadsheetViewportSelection> selection(final Optional<SpreadsheetViewportSelection> selection) {
        return selection.isPresent() ?
                selection :
                SpreadsheetEngineHttps.viewportSelection(this.request.routerParameters());
    }

    @Override
    public String toString() {
        return this.request + " " + this.engine + " " + this.context;
    }
}
