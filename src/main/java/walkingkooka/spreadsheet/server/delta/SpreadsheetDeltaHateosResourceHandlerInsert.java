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
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetUrlQueryParameters;

import java.util.Map;
import java.util.Optional;

/**
 * Base clas for either of the insert before or after which handles basic validation and then execute a template method
 * with the original column or row and count.
 */
abstract class SpreadsheetDeltaHateosResourceHandlerInsert<R extends SpreadsheetSelection & Comparable<R>>
    extends SpreadsheetDeltaHateosResourceHandler<R>
    implements UnsupportedHateosResourceHandlerHandleAll<R, SpreadsheetDelta, SpreadsheetDelta, SpreadsheetEngineHateosResourceHandlerContext> {

    SpreadsheetDeltaHateosResourceHandlerInsert(final SpreadsheetEngine engine) {
        super(engine);
    }

    @Override
    public final Optional<SpreadsheetDelta> handleOne(final R columnOrRow,
                                                      final Optional<SpreadsheetDelta> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                                      final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkReference(columnOrRow);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            this.prepareResponse(
                resource,
                parameters,
                context,
                this.insert(
                    columnOrRow,
                    count(parameters),
                    context
                )
            )
        );
    }

    abstract void checkReference(R columnOrRow);

    /**
     * Sub classes must perform the insert before or after operation here.
     */
    abstract SpreadsheetDelta insert(final R columnOrRow,
                                     final int count,
                                     final SpreadsheetEngineContext context);

    @Override
    public final Optional<SpreadsheetDelta> handleRange(final Range<R> columnOrRow,
                                                        final Optional<SpreadsheetDelta> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters,
                                                        final SpreadsheetEngineHateosResourceHandlerContext context) {
        checkRangeBounded(columnOrRow, this.rangeLabel());
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            this.prepareResponse(
                resource,
                parameters,
                context,
                this.insert(
                    columnOrRow,
                    count(parameters),
                    context
                )
            )
        );
    }

    private int count(final Map<HttpRequestAttribute<?>, Object> parameters) {
        return SpreadsheetUrlQueryParameters.count(parameters)
            .orElseThrow(() -> new IllegalArgumentException("Missing parameter " + SpreadsheetUrlQueryParameters.COUNT));
    }

    abstract String rangeLabel();

    /**
     * Sub classes must perform the insert before or after operation here.
     */
    abstract SpreadsheetDelta insert(final Range<R> columnOrRow,
                                     final int count,
                                     final SpreadsheetEngineContext context);
}
