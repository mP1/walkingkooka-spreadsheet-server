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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponseHttpServerException;
import walkingkooka.net.http.server.hateos.HateosContentType;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Base class for a {@link HateosHttpEntityHandler} that supports PATCHING cells/columns/rows.
 */
abstract class SpreadsheetDeltaPatchHateosHttpEntityHandler<S extends SpreadsheetSelection & Comparable<S>,
        R extends SpreadsheetSelection & Comparable<R>> implements HateosHttpEntityHandler<S>,
        UnsupportedHateosHttpEntityHandlerHandleAll<S>,
        UnsupportedHateosHttpEntityHandlerHandleMany<S>,
        UnsupportedHateosHttpEntityHandlerHandleNone<S> {

    /**
     * {@see SpreadsheetDeltaPatchHateosHttpEntityHandlerCell}
     */
    static SpreadsheetDeltaPatchHateosHttpEntityHandlerCell cell(final SpreadsheetEngine engine,
                                                                 final HateosContentType hateosContentType,
                                                                 final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerCell.with(
                engine,
                hateosContentType,
                context
        );
    }

    /**
     * {@see SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn}
     */
    static SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn column(final SpreadsheetEngine engine,
                                                                     final HateosContentType hateosContentType,
                                                                     final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerColumn.with(
                engine,
                hateosContentType,
                context
        );
    }

    /**
     * {@see SpreadsheetDeltaPatchHateosHttpEntityHandlerRow}
     */
    static SpreadsheetDeltaPatchHateosHttpEntityHandlerRow row(final SpreadsheetEngine engine,
                                                               final HateosContentType hateosContentType,
                                                               final SpreadsheetEngineContext context) {
        return SpreadsheetDeltaPatchHateosHttpEntityHandlerRow.with(
                engine,
                hateosContentType,
                context
        );
    }

    SpreadsheetDeltaPatchHateosHttpEntityHandler(final SpreadsheetEngine engine,
                                                 final HateosContentType hateosContentType,
                                                 final SpreadsheetEngineContext context) {
        super();
        this.engine = Objects.requireNonNull(engine, "engine");
        this.hateosContentType = Objects.requireNonNull(hateosContentType, "hateosContentType");
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public final HttpEntity handleOne(final S selection,
                                      final HttpEntity entity,
                                      final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHttpEntityHandler.checkId(selection);

        return this.loadPatchReply(
                this.toSelectionRange(selection),
                entity,
                parameters
        );
    }

    abstract R toSelectionRange(final S selection);

    @Override
    public final HttpEntity handleRange(final Range<S> selection,
                                        final HttpEntity entity,
                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        return this.loadPatchReply(
                this.toSelectionRange(selection),
                entity,
                parameters
        );
    }

    /**
     * Turns the range into a {@link SpreadsheetSelection}.
     */
    abstract R toSelectionRange(final Range<S> selection);

    /**
     * Handles the request.
     * <ol>
     *     <li>Parse {@link HttpEntity} as JSON</li>
     *     <li>Read the {@link SpreadsheetDelta} with cells for the provided {@link SpreadsheetSelection}</li>
     *     <li>PATCH the {@link SpreadsheetDelta}</li>
     *     <li>Save the {@link SpreadsheetDelta#cells()}</li>
     *     <li>Serialize the PATCHED {@link SpreadsheetDelta} to the response</li>
     * </ol>
     */
    private HttpEntity loadPatchReply(final R selectionRange,
                                      final HttpEntity entity,
                                      final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHttpEntityHandler.checkHttpEntity(entity);
        HateosHttpEntityHandler.checkParameters(parameters);

        final SpreadsheetEngineContext context = this.context;
        final SpreadsheetMetadata metadata = context.spreadsheetMetadata();

        // parse HttpEntity as JSON giving the PATCH as JsonNode
        final JsonNode patch = this.preparePatch(
                this.unmarshallPatch(
                        entity,
                        metadata
                )
        );

        // load the SpreadsheetDelta with cells and patch
        final SpreadsheetDelta patched = this.patch(
                this.loadSpreadsheetDelta(selectionRange),
                selectionRange,
                patch,
                parameters,
                metadata.jsonNodeUnmarshallContext()
        );

        final SpreadsheetDelta saved =
                this.save(
                                patched,
                                selectionRange
                        ).setWindow(patched.window())
                        .setViewport(
                                this.viewport(
                                        parameters,
                                        patched.viewport()
                                )
                        );

        // honour any window or "query" url query parameters.
        final SpreadsheetDelta prepareResponse = SpreadsheetDeltaHttps.prepareResponse(
                Optional.empty(), // no input SpreadsheetDelta
                parameters,
                saved,
                this.engine,
                context
        );

        return marshallResponse(
                prepareResponse,
                metadata
        );
    }

    private JsonNode unmarshallPatch(final HttpEntity entity,
                                     final SpreadsheetMetadata metadata) {
        return metadata.jsonNodeUnmarshallContext()
                .unmarshall(
                        JsonNode.parse(
                                entity.bodyText()
                        ),
                        JsonNode.class
                );
    }

    abstract JsonNode preparePatch(final JsonNode delta);

    private SpreadsheetDelta loadSpreadsheetDelta(final R selectionRange) {
        try {
            return this.load(selectionRange);
        } catch (final MissingStoreException cause) {
            throw new HttpResponseHttpServerException(
                    HttpStatusCode.BAD_REQUEST
                            .setMessage(cause.getMessage()),
                    HttpResponseHttpServerException.NO_ENTITY
            );
        }
    }

    /**
     * Loads the {@link SpreadsheetCellRangeReference}, {@link walkingkooka.spreadsheet.reference.SpreadsheetColumnRangeReference} or
     * {@link walkingkooka.spreadsheet.reference.SpreadsheetRowRangeReference}.
     */
    abstract SpreadsheetDelta load(final R selectionRange);

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

    abstract SpreadsheetDelta patch(final SpreadsheetDelta loaded,
                                    final R selectionRange,
                                    final JsonNode patch,
                                    final Map<HttpRequestAttribute<?>, Object> parameters,
                                    final JsonNodeUnmarshallContext context);

    abstract SpreadsheetDelta save(final SpreadsheetDelta patched,
                                   final R selectionRange);

    /**
     * Tries to read the parameters from the request parameters otherwise returns the given {@link SpreadsheetViewport}.
     */
    private Optional<SpreadsheetViewport> viewport(final Map<HttpRequestAttribute<?>, Object> parameters,
                                                   final Optional<SpreadsheetViewport> viewport) {
        Optional<SpreadsheetViewport> result = SpreadsheetDeltaUrlQueryParameters.viewport(
                parameters,
                false // includeNavigation aka ignore navigation
        );
        if (false == result.isPresent()) {
            result = viewport;
        }
        return result;
    }

    /**
     * Returns the {@link SpreadsheetViewportWindows} from the parameters or the given {@link SpreadsheetDelta}.
     */
    final SpreadsheetViewportWindows window(final Map<HttpRequestAttribute<?>, Object> parameters,
                                            final SpreadsheetDelta delta) {
        return SpreadsheetDeltaUrlQueryParameters.window(
                parameters,
                Optional.of(delta),
                this.engine,
                this.context
        );
    }

    /**
     * Turn the {@link SpreadsheetDelta} into JSON inside a {@link HttpEntity}.
     */
    private HttpEntity marshallResponse(final SpreadsheetDelta response,
                                        final SpreadsheetMetadata metadata) {
        return HttpEntity.EMPTY
                .addHeader(HttpHeaderName.CONTENT_TYPE, hateosContentType.contentType())
                .setBodyText(
                        metadata.jsonNodeMarshallContext()
                                .marshall(
                                        response
                                ).toString()
                );
    }

    final SpreadsheetEngine engine;

    final HateosContentType hateosContentType;
    final SpreadsheetEngineContext context;
}
