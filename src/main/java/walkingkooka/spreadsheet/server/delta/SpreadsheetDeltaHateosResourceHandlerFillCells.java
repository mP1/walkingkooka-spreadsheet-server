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
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleOne;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRangeReference;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that calls {@link SpreadsheetEngine#fillCells(Collection, SpreadsheetCellRangeReference, SpreadsheetCellRangeReference, SpreadsheetEngineContext)}.
 */
final class SpreadsheetDeltaHateosResourceHandlerFillCells extends SpreadsheetDeltaHateosResourceHandler<SpreadsheetCellReference>
        implements UnsupportedHateosResourceHandlerHandleAll<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleOne<SpreadsheetCellReference, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetDeltaHateosResourceHandlerFillCells with(final SpreadsheetEngine engine) {
        return new SpreadsheetDeltaHateosResourceHandlerFillCells(
                check(engine)
        );
    }

    private SpreadsheetDeltaHateosResourceHandlerFillCells(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public Optional<SpreadsheetDelta> handleRange(final Range<SpreadsheetCellReference> to,
                                                  final Optional<SpreadsheetDelta> resource,
                                                  final Map<HttpRequestAttribute<?>, Object> parameters,
                                                  final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetCellRangeReference range = SpreadsheetSelection.cellRange(to);
        final SpreadsheetDelta delta = HateosResourceHandler.checkResourceNotEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final SpreadsheetCellRangeReference from = FROM.parameterValue(parameters)
                .map(SpreadsheetDeltaHateosResourceHandlerFillCells::mapFirstStringValue)
                .orElse(range);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        context,
                        this.engine.fillCells(
                                delta.cells(),
                                from,
                                range,
                                context
                        )
                )
        );
    }

    private static SpreadsheetCellRangeReference mapFirstStringValue(final List<String> values) {
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
