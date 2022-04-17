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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.text.CharSequences;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        final Set<SpreadsheetCellRange> window = this.window(
                resource,
                parameters
        );

        final Map<HttpRequestAttribute<?>, Object> parametersAndWindow = Maps.ordered();
        parametersAndWindow.putAll(parameters);
        parametersAndWindow.put(
                SpreadsheetEngineHttps.WINDOW,
                Lists.of(
                        window.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(","))
                )
        );

        return this.handleRange0(
                window,
                resource,
                parametersAndWindow
        );
    }

    private Set<SpreadsheetCellRange> window(final Optional<SpreadsheetDelta> resource,
                                             final Map<HttpRequestAttribute<?>, Object> parameters) {
        return this.engine.range(
                this.home(parameters)
                        .viewport(
                                this.width(parameters),
                                this.height(parameters)
                        ),
                this.includeFrozenColumnsRows(parameters),
                this.selection(
                        resource,
                        parameters
                ),
                this.context
        );
    }

    private SpreadsheetCellReference home(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstParameterValueAndConvert(
                HOME,
                parameters,
                SpreadsheetCellReference::parseCell
        );
    }

    // @VisibleForTesting
    final static UrlParameterName HOME = UrlParameterName.with("home");

    private double width(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstDoubleParameterValue(WIDTH, parameters);
    }

    final static UrlParameterName WIDTH = UrlParameterName.with("width");

    private double height(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstDoubleParameterValue(HEIGHT, parameters);
    }

    final static UrlParameterName HEIGHT = UrlParameterName.with("height");

    private static double firstDoubleParameterValue(final UrlParameterName parameter,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstParameterValueAndConvert(
                parameter,
                parameters,
                Double::parseDouble
        );
    }

    private boolean includeFrozenColumnsRows(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return firstParameterValueAndConvert(
                INCLUDE_FROZEN_COLUMNS_ROWS,
                parameters,
                Boolean::parseBoolean
        );
    }

    final static UrlParameterName INCLUDE_FROZEN_COLUMNS_ROWS = UrlParameterName.with("includeFrozenColumnsRows");

    private static <T> T firstParameterValueAndConvert(final UrlParameterName parameter,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final Function<String, T> converter) {
        final String value = parameter.firstParameterValueOrFail(parameters);
        try {
            return converter.apply(value);
        } catch (final Exception convertFailed) {
            throw new IllegalArgumentException("Invalid query parameter " + parameter + "=" + CharSequences.quoteIfChars(value));
        }
    }

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
                Sets.of(SpreadsheetSelection.cellRange(cells)),
                resource,
                parameters
        );
    }

    private Optional<SpreadsheetDelta> handleRange0(final Set<SpreadsheetCellRange> window,
                                                    final Optional<SpreadsheetDelta> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        return Optional.ofNullable(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.engine.loadCells(
                                window,
                                this.evaluation,
                                this.context
                        )
                )
        );
    }

    @Override
    String operation() {
        return "loadCell " + this.evaluation;
    }
}
