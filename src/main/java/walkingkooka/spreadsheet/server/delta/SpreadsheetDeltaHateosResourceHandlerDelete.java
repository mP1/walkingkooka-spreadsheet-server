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
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleAll;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReferenceKind;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract base class for DELETE / INSERT a column or row or range of either
 */
abstract class SpreadsheetDeltaHateosResourceHandlerDelete<R extends SpreadsheetSelection & Comparable<R>> extends SpreadsheetDeltaHateosResourceHandler<R>
    implements UnsupportedHateosResourceHandlerHandleAll<R, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    SpreadsheetDeltaHateosResourceHandlerDelete() {
        super();
    }

    @Override
    public final Optional<SpreadsheetDelta> handleOne(final R columnOrRow,
                                                      final Optional<SpreadsheetDelta> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                                      final UrlPath path,
                                                      final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkReference(columnOrRow);

        return Optional.of(
            this.executeAndPrepareResponse(
                columnOrRow,
                1,
                resource,
                parameters,
                path,
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
                                                        final UrlPath path,
                                                        final SpreadsheetEngineHateosResourceHandlerContext context) {
        final String label = this.rangeLabel();
        Objects.requireNonNull(columnOrRow, label);

        if (!columnOrRow.lowerBound().isInclusive() || !columnOrRow.upperBound().isInclusive()) {
            throw new IllegalArgumentException("Range with both " + label + " required=" + columnOrRow);
        }

        final R lower = columnOrRow.lowerBound()
            .value()
            .get();
        final R upper = columnOrRow.upperBound()
            .value()
            .get();

        final SpreadsheetColumnOrRowReferenceKind kind = lower.columnOrRowReferenceKind();

        return Optional.of(
            this.executeAndPrepareResponse(
                lower,
                kind.value(upper) - kind.value(lower) + 1,
                resource,
                parameters,
                path,
                context
            )
        );
    }

    abstract String rangeLabel();

    private SpreadsheetDelta executeAndPrepareResponse(final R lower,
                                                       final int count,
                                                       final Optional<SpreadsheetDelta> in,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final UrlPath path,
                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(in);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
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
                                      final SpreadsheetEngineHateosResourceHandlerContext context);
}
