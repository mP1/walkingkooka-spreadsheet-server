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
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * Abstract base class for DELETE / INSERT a column or row or range of either
 */
abstract class SpreadsheetDeltaHateosResourceHandlerDelete<R extends SpreadsheetColumnOrRowReference & Comparable<R>> extends SpreadsheetDeltaHateosResourceHandler<R>
    implements UnsupportedHateosResourceHandlerHandleAll<R, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    SpreadsheetDeltaHateosResourceHandlerDelete(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public final Optional<SpreadsheetDelta> handleOne(final R columnOrRow,
                                                      final Optional<SpreadsheetDelta> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                                      final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkReference(columnOrRow);

        return Optional.of(
            this.executeAndPrepareResponse(
                columnOrRow,
                1,
                resource,
                parameters,
                context
            )
        );
    }

    abstract void checkReference(R columnOrRow);

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public final Optional<SpreadsheetDelta> handleRange(final Range<R> columnOrRow,
                                                        final Optional<SpreadsheetDelta> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters,
                                                        final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkRangeBounded(columnOrRow, this.rangeLabel());

        final R lower = columnOrRow.lowerBound()
            .value()
            .get();
        final R upper = columnOrRow.upperBound()
            .value()
            .get();

        return Optional.of(
            this.executeAndPrepareResponse(
                lower,
                upper.value() - lower.value() + 1,
                resource,
                parameters,
                context
            )
        );
    }

    abstract String rangeLabel();

    private SpreadsheetDelta executeAndPrepareResponse(final R lower,
                                                       final int count,
                                                       final Optional<SpreadsheetDelta> in,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(in);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return this.prepareResponse(
            in,
            parameters,
            context,
            this.execute(
                lower,
                count,
                context
            )
        );
    }

    /**
     * Sub classes must perform the delete or insert option.
     */
    abstract SpreadsheetDelta execute(final R lower,
                                      final int count,
                                      final SpreadsheetEngineContext context);
}
