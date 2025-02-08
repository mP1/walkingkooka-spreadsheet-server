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

import walkingkooka.build.MissingBuilder;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#loadCells(SpreadsheetSelection, SpreadsheetEngineEvaluation, Set, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerLoadCell extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference> {

    static SpreadsheetDeltaHateosResourceHandlerLoadCell with(final SpreadsheetEngineEvaluation evaluation,
                                                              final SpreadsheetEngine engine) {
        Objects.requireNonNull(evaluation, "evaluation");

        return new SpreadsheetDeltaHateosResourceHandlerLoadCell(
            evaluation,
            check(engine)
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerLoadCell(final SpreadsheetEngineEvaluation evaluation,
                                                          final SpreadsheetEngine engine) {
        super(engine);
        this.evaluation = evaluation;
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        // compute window including navigation.
        final Optional<SpreadsheetViewport> maybeViewport = this.viewport(
            parameters,
            resource,
            true // includeNavigation
        );

        if (false == maybeViewport.isPresent()) {
            throw new IllegalArgumentException(MISSING_VIEWPORT);
        }

        final SpreadsheetViewport viewport = maybeViewport.get();

        final SpreadsheetEngine engine = this.engine;

        final Optional<SpreadsheetViewport> maybeNavigatedViewport = engine.navigate(
            viewport,
            context
        );

        // if the selection moved need to SAVE!
        SpreadsheetMetadata metadata = context.spreadsheetMetadata();

        if (maybeNavigatedViewport.isPresent()) {
            metadata = context.storeRepository()
                .metadatas()
                .save(
                    metadata.set(
                        SpreadsheetMetadataPropertyName.VIEWPORT,
                        maybeNavigatedViewport.get()
                    )
                );
        }

        return this.handleAll0(
            resource,
            parameters,
            maybeNavigatedViewport.orElse(viewport),
            SpreadsheetDeltaHateosResourceHandlerLoadCellSpreadsheetEngineHateosResourceHandlerContext.with(
                metadata,
                context
            )
        );
    }

    /**
     * This is invoked after any navigations to the viewport.
     */
    private Optional<SpreadsheetDelta> handleAll0(final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final SpreadsheetViewport viewport,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetViewportWindows window = this.engine.window(
            viewport.rectangle(),
            true, // includeFrozenColumnsRows,
            SpreadsheetEngine.NO_SELECTION, // no selection
            context
        );

        // copy all the parameters plus the resolved window, which will be used by prepareResponse.
        final Map<HttpRequestAttribute<?>, Object> parametersPlusWindow = Maps.ordered();
        parametersPlusWindow.putAll(parameters);
        parametersPlusWindow.put(
            SpreadsheetDeltaUrlQueryParameters.WINDOW,
            Lists.of(
                window.toString()
            )
        );

        return this.handleRange0(
            window.cellRanges(),
            resource,
            Optional.of(viewport),
            Maps.immutable(
                parametersPlusWindow
            ),
            context
        );
    }

    final static String MISSING_VIEWPORT = "Missing: " +
        MissingBuilder.empty()
            .add(SpreadsheetDeltaUrlQueryParameters.HOME.value())
            .add(SpreadsheetDeltaUrlQueryParameters.WIDTH.value())
            .add(SpreadsheetDeltaUrlQueryParameters.HEIGHT.value())
            .add(SpreadsheetDeltaUrlQueryParameters.INCLUDE_FROZEN_COLUMNS_ROWS.value())
            .build();

    // handleOne........................................................................................................

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters,
                                                final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkCell(cell);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            this.prepareResponse(
                resource,
                parameters,
                context,
                this.loadCell(
                    cell,
                    SpreadsheetDeltaProperties.extract(parameters),
                    context
                )
            )
        );
    }

    private SpreadsheetDelta loadCell(final SpreadsheetCellReference reference,
                                      final Set<SpreadsheetDeltaProperties> deltaProperties,
                                      final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.engine.loadCells(
            reference,
            this.evaluation,
            deltaProperties,
            context
        );
    }

    private final SpreadsheetEngineEvaluation evaluation;

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> ids,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkIdRange(ids);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return this.handleRange0(
            Sets.of(
                SpreadsheetSelection.cellRange(ids)
            ),
            resource,
            Optional.empty(), // no viewport ignore query parameters
            parameters,
            context
        );
    }

    private Optional<SpreadsheetDelta> handleRange0(final Set<SpreadsheetCellRangeReference> window,
                                                    final Optional<SpreadsheetDelta> resource,
                                                    final Optional<SpreadsheetViewport> viewport,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters,
                                                    final SpreadsheetEngineHateosResourceHandlerContext context) {
        return Optional.ofNullable(
            this.prepareResponse(
                resource,
                parameters,
                context,
                this.loadMultipleCellRanges(
                    window,
                    SpreadsheetDeltaProperties.extract(parameters),
                    context
                ).setViewport(viewport)
            )
        );
    }

    private SpreadsheetDelta loadMultipleCellRanges(final Set<SpreadsheetCellRangeReference> cells,
                                                    final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                    final SpreadsheetEngineHateosResourceHandlerContext context) {
        return this.engine.loadMultipleCellRanges(
            cells,
            this.evaluation,
            deltaProperties,
            context
        );
    }

    @Override
    String operation() {
        return "loadCell " + this.evaluation;
    }
}
