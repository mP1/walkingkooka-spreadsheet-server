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

import walkingkooka.build.MissingBuilder;
import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetViewportWindows;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewport;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#loadCells(Set, SpreadsheetEngineEvaluation, Set, SpreadsheetEngineContext)}.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell extends SpreadsheetEngineHateosHandlerSpreadsheetDelta<SpreadsheetCellReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell with(final SpreadsheetEngineEvaluation evaluation,
                                                                       final SpreadsheetEngine engine,
                                                                       final SpreadsheetEngineContext context) {
        Objects.requireNonNull(evaluation, "evaluation");

        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell(evaluation,
                engine,
                context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaLoadCell(final SpreadsheetEngineEvaluation evaluation,
                                                                   final SpreadsheetEngine engine,
                                                                   final SpreadsheetEngineContext context) {
        super(engine, context);
        this.evaluation = evaluation;
    }

    @Override
    public Optional<SpreadsheetDelta> handleAll(final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

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
        final SpreadsheetEngineContext context = this.context;
        final Optional<SpreadsheetViewport> navigatedViewport = engine.navigate(
                viewport,
                context
        );

        final SpreadsheetViewportWindows window = this.engine.window(
                viewport.rectangle(),
                true, // includeFrozenColumnsRows,
                SpreadsheetEngine.NO_SELECTION, // no selection
                context
        );

        final Map<HttpRequestAttribute<?>, Object> parametersWindowAndDelta = Maps.sorted();
        parametersWindowAndDelta.put(SpreadsheetEngineHttps.WINDOW,
                Lists.of(
                        window.toString()
                )
        );

        final Optional<List<String>> delta = DELTA_PROPERTIES.parameterValue(parameters);
        if (delta.isPresent()) {
            parametersWindowAndDelta.put(
                    DELTA_PROPERTIES,
                    delta.get()
            );
        }

        return this.handleRange0(
                window.cellRanges(),
                resource,
                navigatedViewport,
                Maps.immutable(
                        parametersWindowAndDelta
                )
        );
    }

    /**
     * Optional query parameter, where the value is a CSV of camel-case {@link SpreadsheetDeltaProperties}.
     */
    final static UrlParameterName DELTA_PROPERTIES = UrlParameterName.with("properties");

    final static String MISSING_VIEWPORT = "Missing: " +
            MissingBuilder.empty()
                    .add(SpreadsheetEngineHttps.HOME.value())
                    .add(SpreadsheetEngineHttps.WIDTH.value())
                    .add(SpreadsheetEngineHttps.HEIGHT.value())
                    .add(SpreadsheetEngineHttps.INCLUDE_FROZEN_COLUMNS_ROWS.value())
                    .build();

    // handleOne........................................................................................................

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkCell(cell);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.loadCell(
                                cell,
                                deltaProperties(parameters)
                        )
                )
        );
    }

    private SpreadsheetDelta loadCell(final SpreadsheetCellReference reference,
                                      final Set<SpreadsheetDeltaProperties> deltaProperties) {
        return this.engine.loadCells(
                reference,
                this.evaluation,
                deltaProperties,
                this.context
        );
    }

    private final SpreadsheetEngineEvaluation evaluation;

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> cells,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(cells);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return this.handleRange0(
                Sets.of(SpreadsheetSelection.cellRange(cells)),
                resource,
                Optional.empty(), // no viewport ignore query parameters
                parameters
        );
    }

    private Optional<SpreadsheetDelta> handleRange0(final Set<SpreadsheetCellRange> window,
                                                    final Optional<SpreadsheetDelta> resource,
                                                    final Optional<SpreadsheetViewport> viewport,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        return Optional.ofNullable(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.loadCells(
                                window,
                                deltaProperties(parameters)
                        ).setViewport(viewport)
                )
        );
    }

    private SpreadsheetDelta loadCells(final Set<SpreadsheetCellRange> cells,
                                       final Set<SpreadsheetDeltaProperties> deltaProperties) {
        return this.engine.loadCells(
                cells,
                this.evaluation,
                deltaProperties,
                this.context
        );
    }

    private static Set<SpreadsheetDeltaProperties> deltaProperties(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return SpreadsheetDeltaProperties.csv(
                DELTA_PROPERTIES.firstParameterValue(parameters)
                        .orElse(null)
        );
    }

    @Override
    String operation() {
        return "loadCell " + this.evaluation;
    }
}
