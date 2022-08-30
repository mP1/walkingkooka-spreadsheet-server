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
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.store.LoadStoreException;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
abstract class SpreadsheetEnginePatch<R extends SpreadsheetSelection> implements UnaryOperator<JsonNode> {

    SpreadsheetEnginePatch(final HttpRequest request,
                           final SpreadsheetEngine engine,
                           final SpreadsheetEngineContext context) {
        super();
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(context, "context");

        this.request = request;
        this.engine = engine;
        this.context = context;
    }

    @Override
    public JsonNode apply(final JsonNode json) {
        final R reference = this.parseReference();

        final SpreadsheetDelta loaded = this.loadSpreadsheetDelta(reference);
        final JsonNode patch = this.preparePatch(json);

        final SpreadsheetMetadata metadata = this.context.metadata();

        final SpreadsheetDelta patched = this.patch(
                reference,
                loaded,
                patch,
                metadata.jsonNodeUnmarshallContext()
        );

        final SpreadsheetDelta saved =
                this.save(patched, reference)
                        .setWindow(patched.window())
                        .setViewportSelection(
                                this.viewportSelection(
                                        patched.viewportSelection()
                                )
                        );

        return metadata.jsonNodeMarshallContext()
                .marshall(saved);
    }

    private R parseReference() {
        return this.parseReference(
                this.request.url()
                        .path()
                        .name()
                        .value()
        );
    }

    /**
     * Parses the last part of the path into the {@link SpreadsheetSelection}.
     */
    abstract R parseReference(final String text);

    private SpreadsheetDelta loadSpreadsheetDelta(final R reference) {
        try {
            return this.load(reference);
        } catch (final LoadStoreException cause) {
            throw new HttpResponseHttpServerException(
                    HttpStatusCode.BAD_REQUEST
                            .setMessage(cause.getMessage()),
                    HttpResponseHttpServerException.NO_ENTITY
            );
        }
    }

    /**
     * Loads the cell, column or row
     */
    abstract SpreadsheetDelta load(final R reference);

    abstract JsonNode preparePatch(final JsonNode delta);

    abstract SpreadsheetDelta patch(final R reference,
                                    final SpreadsheetDelta loaded,
                                    final JsonNode patch,
                                    final JsonNodeUnmarshallContext context);

    abstract SpreadsheetDelta save(final SpreadsheetDelta patched,
                                   final R reference);

    final HttpRequest request;
    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    private Optional<SpreadsheetViewportSelection> viewportSelection(final Optional<SpreadsheetViewportSelection> viewportSelection) {
        return SpreadsheetEngineHttps.viewportSelection(
                Optional.of(
                        SpreadsheetDelta.EMPTY.setViewportSelection(viewportSelection)
                ),
                this.request.routerParameters(),
                this.engine,
                this.context
        );
    }

    final Set<SpreadsheetCellRange> window(final SpreadsheetDelta delta) {
        return SpreadsheetEngineHttps.window(
                Optional.of(delta),
                this.request.routerParameters()
        );
    }

    /**
     * Used to load all the cells within an unhidden column or row.
     */
    final Set<SpreadsheetCell> loadCells(final Set<SpreadsheetCellRange> window) {
        return this.engine.loadCells(
                window,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                SpreadsheetDeltaProperties.ALL,
                this.context
        ).cells();
    }

    @Override
    public final String toString() {
        return this.toStringPrefix() + this.request + " " + this.engine + " " + this.context;
    }

    abstract String toStringPrefix();
}
