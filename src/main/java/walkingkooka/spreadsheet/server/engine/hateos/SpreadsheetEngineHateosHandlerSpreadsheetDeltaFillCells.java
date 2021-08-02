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

package walkingkooka.spreadsheet.server.engine.hateos;

import walkingkooka.collect.Range;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosHandler} that calls {@link SpreadsheetEngine#fillCells(Collection, SpreadsheetCellRange, SpreadsheetCellRange, SpreadsheetEngineContext)}.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells extends SpreadsheetEngineHateosHandlerSpreadsheetDelta2<SpreadsheetCellReference> {

    static SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells with(final SpreadsheetEngine engine,
                                                                        final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells(final SpreadsheetEngine engine,
                                                                    final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetDelta> handleOne(final SpreadsheetCellReference cell,
                                                final Optional<SpreadsheetDelta> resource,
                                                final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkCell(cell);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> to,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters) {
        final SpreadsheetCellRange range = SpreadsheetSelection.cellRange(to);
        final SpreadsheetDelta delta = HateosHandler.checkResourceNotEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final SpreadsheetCellRange from = FROM.parameterValue(parameters)
                .map(SpreadsheetEngineHateosHandlerSpreadsheetDeltaFillCells::mapFirstStringValue)
                .orElse(range);

        return Optional.of(delta.setCells(this.engine.fillCells(delta.cells(),
                from,
                range,
                this.context).cells()));
    }

    private static SpreadsheetCellRange mapFirstStringValue(final List<String> values) {
        return values.stream()
                .limit(1)
                .map(SpreadsheetExpressionReference::parseCellRange)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Required parameter " + FROM + " missing"));
    }

    final static UrlParameterName FROM = UrlParameterName.with("from");

    @Override
    String operation() {
        return "fillCells"; // SpreadsheetEngine#fillCells
    }
}
