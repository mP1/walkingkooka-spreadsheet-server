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

import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpResponseHttpServerException;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.store.MissingStoreException;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * A {@link UnaryOperator} that accepts the PATCH json and returns the {@link SpreadsheetDelta} JSON response.
 */
abstract class SpreadsheetDeltaPatchFunction<S extends SpreadsheetSelection> implements UnaryOperator<JsonNode> {

    static SpreadsheetDeltaPatchFunctionCell cell(final HttpRequest request,
                                                  final SpreadsheetEngine engine,
                                                  final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchFunctionCell.with(request, engine, context);
    }

    static SpreadsheetDeltaPatchFunctionColumn column(final HttpRequest request,
                                                      final SpreadsheetEngine engine,
                                                      final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchFunctionColumn.with(request, engine, context);
    }

    static SpreadsheetDeltaPatchFunctionRow row(final HttpRequest request,
                                                final SpreadsheetEngine engine,
                                                final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchFunctionRow.with(request, engine, context);
    }

    SpreadsheetDeltaPatchFunction(final HttpRequest request,
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
        final S selection = this.parseSelection();

        final SpreadsheetDelta loaded = this.loadSpreadsheetDelta(selection);
        final JsonNode patch = this.preparePatch(json);

        final SpreadsheetEngineContext context = this.context;
        final SpreadsheetMetadata metadata = context.spreadsheetMetadata();

        final SpreadsheetDelta patched = this.patch(
                selection,
                loaded,
                patch,
                metadata.jsonNodeUnmarshallContext()
        );

        final SpreadsheetDelta saved =
                this.save(patched, selection)
                        .setWindow(patched.window())
                        .setViewport(
                                this.viewport(
                                        patched.viewport()
                                )
                        );

        // honour any window or "query" url query parameters.
        final SpreadsheetDelta prepareResponse = SpreadsheetDeltaHateosResourceMappings.prepareResponse(
                Optional.empty(), // no input SpreadsheetDelta
                this.request.routerParameters(),
                saved,
                this.engine,
                context
        );

        return metadata.jsonNodeMarshallContext()
                .marshall(prepareResponse);
    }

    private S parseSelection() {
        return this.parseSelection(
                this.request.url()
                        .path()
                        .name()
                        .value()
        );
    }

    /**
     * Parses the last part of the path into the {@link SpreadsheetSelection}.
     */
    abstract S parseSelection(final String text);

    private SpreadsheetDelta loadSpreadsheetDelta(final S selectin) {
        try {
            return this.load(selectin);
        } catch (final MissingStoreException cause) {
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
    abstract SpreadsheetDelta load(final S reference);

    abstract JsonNode preparePatch(final JsonNode delta);

    abstract SpreadsheetDelta patch(final S selection,
                                    final SpreadsheetDelta loaded,
                                    final JsonNode patch,
                                    final JsonNodeUnmarshallContext context);

    abstract SpreadsheetDelta save(final SpreadsheetDelta patched,
                                   final S reference);

    final HttpRequest request;
    final SpreadsheetEngine engine;
    final SpreadsheetEngineContext context;

    private Optional<SpreadsheetViewport> viewport(final Optional<SpreadsheetViewport> viewport) {
        Optional<SpreadsheetViewport> result = SpreadsheetDeltaUrlQueryParameters.viewport(
                this.request.routerParameters(),
                false // includeNavigation
        );
        if (false == result.isPresent()) {
            result = viewport;
        }
        return result;
    }

    final SpreadsheetViewportWindows window(final SpreadsheetDelta delta) {
        return SpreadsheetDeltaUrlQueryParameters.window(
                this.request.routerParameters(),
                Optional.of(delta),
                this.engine,
                this.context
        );
    }

    /**
     * Used to load all the cells within an unhidden column or row.
     */
    final Set<SpreadsheetCell> loadCells(final Set<SpreadsheetCellRangeReference> window) {
        return this.engine.loadCells(
                window,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                CELLS,
                this.context
        ).cells();
    }

    private final static Set<SpreadsheetDeltaProperties> CELLS = Sets.of(
            SpreadsheetDeltaProperties.CELLS
    );

    @Override
    public final String toString() {
        return this.toStringPrefix() + this.request + " " + this.engine + " " + this.context;
    }

    abstract String toStringPrefix();
}
