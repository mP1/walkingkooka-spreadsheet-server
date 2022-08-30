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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An abstract {@link HateosHandler} that includes uses a {@link SpreadsheetEngine} and {@link SpreadsheetEngineContext} to do things.
 */
abstract class SpreadsheetEngineHateosHandlerSpreadsheetDelta<I extends Comparable<I>>
        extends SpreadsheetEngineHateosHandler<I, SpreadsheetDelta, SpreadsheetDelta> {

    static void checkCell(final SpreadsheetCellReference cell) {
        Objects.requireNonNull(cell, "cell");
    }

    /**
     * Checks that the range bounds are not null and both are inclusive.
     * Complains if the resource is null.
     */
    static void checkRangeBounded(final Range<?> range, final String label) {
        Objects.requireNonNull(range, label);

        if (!range.lowerBound().isInclusive() || !range.upperBound().isInclusive()) {
            throw new IllegalArgumentException("Range with both " + label + " required=" + range);
        }
    }

    /**
     * Package private to limit sub classing.
     */
    SpreadsheetEngineHateosHandlerSpreadsheetDelta(final SpreadsheetEngine engine,
                                                   final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public final String toString() {
        return SpreadsheetEngine.class.getSimpleName() + "." + this.operation();
    }

    abstract String operation();

    /**
     * Applies the window if any was present on the input {@link SpreadsheetDelta}
     */
    final SpreadsheetDelta prepareResponse(final Optional<SpreadsheetDelta> in,
                                           final Map<HttpRequestAttribute<?>, Object> parameters,
                                           final SpreadsheetDelta out) {
        return out.setWindow(
                SpreadsheetEngineHttps.window(
                        in,
                        parameters
                )

        ).setViewportSelection(
                this.viewportSelection(
                        in,
                        parameters
                )
        );
    }
}
