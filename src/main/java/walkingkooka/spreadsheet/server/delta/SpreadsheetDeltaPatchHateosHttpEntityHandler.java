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
import walkingkooka.net.UrlPath;
import walkingkooka.net.header.CharsetName;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;
import walkingkooka.net.http.HttpStatusCode;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.HttpResponseHttpServerException;
import walkingkooka.net.http.server.hateos.HateosHttpEntityHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosHttpEntityHandlerHandleNone;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewport;
import walkingkooka.spreadsheet.viewport.SpreadsheetViewportWindows;
import walkingkooka.store.MissingStoreException;
import walkingkooka.tree.json.JsonNode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Base class for a {@link HateosHttpEntityHandler} that supports PATCHING cells/columns/rows.
 */
abstract class SpreadsheetDeltaPatchHateosHttpEntityHandler<S extends SpreadsheetSelection & Comparable<S>,
    R extends SpreadsheetSelection & Comparable<R>> implements HateosHttpEntityHandler<S, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleAll<S, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleMany<S, SpreadsheetEngineHateosResourceHandlerContext>,
    UnsupportedHateosHttpEntityHandlerHandleNone<S, SpreadsheetEngineHateosResourceHandlerContext> {

    SpreadsheetDeltaPatchHateosHttpEntityHandler() {
        super();
    }

    @Override
    public final HttpEntity handleOne(final S selection,
                                      final HttpEntity entity,
                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                      final UrlPath path,
                                      final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkId(selection);

        return this.loadPatchReply(
            this.toSelectionRange(selection),
            entity,
            parameters,
            path,
            context
        );
    }

    abstract R toSelectionRange(final S selection);

    @Override
    public final HttpEntity handleRange(final Range<S> selection,
                                        final HttpEntity entity,
                                        final Map<HttpRequestAttribute<?>, Object> parameters,
                                        final UrlPath path,
                                        final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.loadPatchReply(
            this.toSelectionRange(selection),
            entity,
            parameters,
            path,
            context
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
                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                      final UrlPath path,
                                      final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosHttpEntityHandler.checkHttpEntity(entity);
        HateosHttpEntityHandler.checkParameters(parameters);
        HateosHttpEntityHandler.checkPathEmpty(path);
        HateosHttpEntityHandler.checkContext(context);

        final MediaType requiredContentType = context.contentType();
        requiredContentType.requireContentType(
            HttpHeaderName.CONTENT_TYPE.header(entity)
                .orElse(null)
        );
        HttpHeaderName.ACCEPT.headerOrFail(entity)
            .testOrFail(requiredContentType);

        // parse HttpEntity as JSON giving the PATCH as JsonNode
        final JsonNode patch = this.preparePatch(
            this.unmarshallPatch(
                entity,
                context
            ),
            context
        );

        // load the SpreadsheetDelta with cells and patch
        final SpreadsheetDelta patched = this.patch(
            this.loadSpreadsheetDelta(
                selectionRange,
                context
            ),
            selectionRange,
            patch,
            parameters,
            context
        );

        final SpreadsheetDelta saved =
            this.save(
                    patched,
                    selectionRange,
                    context
                ).setWindow(patched.window())
                .setViewport(
                    this.viewport(
                        parameters,
                        patched.viewport()
                    )
                );

        // honour any window or "query" url query parameters.
        final SpreadsheetDelta prepareResponse = SpreadsheetDeltaHttpMappings.prepareResponse(
            Optional.empty(), // no input SpreadsheetDelta
            parameters,
            saved,
            context
        );

        return marshallResponse(
            prepareResponse,
            context
        );
    }

    private JsonNode unmarshallPatch(final HttpEntity entity,
                                     final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.unmarshall(
            JsonNode.parse(
                entity.bodyText()
            ),
            JsonNode.class
        );
    }

    abstract JsonNode preparePatch(final JsonNode delta,
                                   final SpreadsheetEngineHateosResourceHandlerContext context);

    private SpreadsheetDelta loadSpreadsheetDelta(final R selectionRange,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        try {
            return this.load(
                selectionRange,
                context
            );
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
    abstract SpreadsheetDelta load(final R selectionRange,
                                   final SpreadsheetEngineHateosResourceHandlerContext context);

    /**
     * Used to load all the cells within an unhidden column or row.
     */
    final Set<SpreadsheetCell> loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> window,
                                                      final SpreadsheetEngineHateosResourceHandlerContext context) {
        return context.spreadsheetEngine()
            .loadMultipleCellRanges(
                window,
                SpreadsheetEngineEvaluation.COMPUTE_IF_NECESSARY,
                CELLS,
                context
            ).cells();
    }

    private final static Set<SpreadsheetDeltaProperties> CELLS = Sets.of(
        SpreadsheetDeltaProperties.CELLS
    );

    abstract SpreadsheetDelta patch(final SpreadsheetDelta loaded,
                                    final R selectionRange,
                                    final JsonNode patch,
                                    final Map<HttpRequestAttribute<?>, Object> parameters,
                                    final SpreadsheetEngineHateosResourceHandlerContext context);

    abstract SpreadsheetDelta save(final SpreadsheetDelta patched,
                                   final R selectionRange,
                                   final SpreadsheetEngineHateosResourceHandlerContext context);

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
                                            final SpreadsheetDelta delta,
                                            final SpreadsheetEngineHateosResourceHandlerContext context) {
        return SpreadsheetDeltaUrlQueryParameters.window(
            parameters,
            Optional.of(delta),
            context.spreadsheetEngine(),
            context
        );
    }

    /**
     * Turn the {@link SpreadsheetDelta} into JSON inside a {@link HttpEntity}.
     */
    private HttpEntity marshallResponse(final SpreadsheetDelta response,
                                        final SpreadsheetEngineHateosResourceHandlerContext context) {
        return HttpEntity.EMPTY
            .setContentType(
                context.contentType()
                    .setCharset(CharsetName.UTF_8)
            ).addHeader(HateosResourceMappings.X_CONTENT_TYPE_NAME, SpreadsheetDelta.class.getSimpleName())
            .setBodyText(
                context.toJsonText(
                    context.marshall(response)
                )
            ).setContentLength();
    }
}
