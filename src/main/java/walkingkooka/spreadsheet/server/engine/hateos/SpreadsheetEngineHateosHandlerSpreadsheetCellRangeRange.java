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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetViewport;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosHandler} that computes the {@link SpreadsheetCellRange} for a given {@link SpreadsheetViewport}.
 */
final class SpreadsheetEngineHateosHandlerSpreadsheetCellRangeRange extends SpreadsheetEngineHateosHandler2<SpreadsheetViewport, SpreadsheetCellRange, SpreadsheetCellRange> {

    static SpreadsheetEngineHateosHandlerSpreadsheetCellRangeRange with(final SpreadsheetEngine engine,
                                                                        final SpreadsheetEngineContext context) {
        check(engine, context);
        return new SpreadsheetEngineHateosHandlerSpreadsheetCellRangeRange(engine, context);
    }

    private SpreadsheetEngineHateosHandlerSpreadsheetCellRangeRange(final SpreadsheetEngine engine,
                                                                    final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<SpreadsheetCellRange> handleOne(final SpreadsheetViewport viewport,
                                                    final Optional<SpreadsheetCellRange> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        Objects.requireNonNull(viewport, "viewport");
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(
                this.engine.range(
                        viewport,
                        Optional.empty(),
                        this.context
                )
        );
    }

    @Override
    public Optional<SpreadsheetCellRange> handleRange(final Range<SpreadsheetViewport> range,
                                                      final Optional<SpreadsheetCellRange> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(range);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return SpreadsheetEngine.class.getSimpleName() + ".range";
    }
}
