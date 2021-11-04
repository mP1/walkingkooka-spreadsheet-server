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

import walkingkooka.collect.Range;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#loadCell(SpreadsheetCellReference, SpreadsheetEngineEvaluation, SpreadsheetEngineContext)}.
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

        final SpreadsheetCellReference home = firstParameterValueAndConvert(HOME, parameters, SpreadsheetCellReference::parseCell);

        final double xOffset = firstDoubleParameterValue(X_OFFSET, parameters);
        final double yOffset = firstDoubleParameterValue(Y_OFFSET, parameters);

        final double width = firstDoubleParameterValue(WIDTH, parameters);
        final double height = firstDoubleParameterValue(HEIGHT, parameters);

        final Optional<SpreadsheetSelection> selection = selection(parameters);

        final SpreadsheetCellRange window = this.engine.range(
                SpreadsheetViewport.with(home, xOffset, yOffset, width, height),
                selection,
                this.context
        );

        final Map<HttpRequestAttribute<?>, Object> parametersAndWindow = Maps.ordered();
        parametersAndWindow.putAll(parameters);
        parametersAndWindow.put(WINDOW, Lists.of(window.toString()));

        return this.handleRange0(
                window,
                resource,
                parametersAndWindow
        );
    }

    // @VisibleForTesting
    final static UrlParameterName HOME = UrlParameterName.with("home");
    final static UrlParameterName X_OFFSET = UrlParameterName.with("xOffset");
    final static UrlParameterName Y_OFFSET = UrlParameterName.with("yOffset");
    final static UrlParameterName WIDTH = UrlParameterName.with("width");
    final static UrlParameterName HEIGHT = UrlParameterName.with("height");

    private static double firstDoubleParameterValue(final UrlParameterName parameter,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstParameterValueAndConvert(
                parameter,
                parameters,
                Double::parseDouble
        );
    }

    private static <T> T firstParameterValueAndConvert(final UrlParameterName parameter,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final Function<String, T> converter) {
        final String value = parameter.firstParameterValueOrFail(parameters);
        try {
            return converter.apply(value);
        } catch (final Exception convertFailed) {
            throw new IllegalArgumentException("Invalid value for parameter: " + parameter);
        }
    }

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
                        this.loadCell(cell)
                )
        );
    }

    SpreadsheetDelta loadCell(final SpreadsheetCellReference reference) {
        return this.engine.loadCell(
                reference,
                this.evaluation,
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
                SpreadsheetSelection.cellRange(cells),
                resource,
                parameters
        );
    }

    private Optional<SpreadsheetDelta> handleRange0(final SpreadsheetCellRange range,
                                                    final Optional<SpreadsheetDelta> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        return Optional.ofNullable(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.engine.loadCells(range, this.evaluation, this.context)
                )
        );
    }

    @Override
    String operation() {
        return "loadCell " + this.evaluation;
    }
}
