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
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnOrRowReference;

import java.util.Map;
import java.util.Optional;

/**
 * Base clas for either of the insert before or after which handles basic validation and then execute a template method
 * with the original column or row and count.
 */
abstract class SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsert<R extends SpreadsheetColumnOrRowReference & Comparable<R>>
        extends SpreadsheetEngineHateosHandlerSpreadsheetDelta2<R> {

    SpreadsheetEngineHateosHandlerSpreadsheetDeltaInsert(final SpreadsheetEngine engine,
                                                         final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public final Optional<SpreadsheetDelta> handleOne(final R columnOrRow,
                                                      final Optional<SpreadsheetDelta> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkReference(columnOrRow);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.insert(
                                columnOrRow,
                                count(parameters)
                        )
                )
        );
    }

    abstract void checkReference(R columnOrRow);

    /**
     * Sub classes must perform the insert before or after operation here.
     */
    abstract SpreadsheetDelta insert(final R columnOrRow, final int count);

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public final Optional<SpreadsheetDelta> handleRange(final Range<R> columnOrRow,
                                                        final Optional<SpreadsheetDelta> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        checkRangeBounded(columnOrRow, this.rangeLabel());
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(
                this.prepareResponse(
                        resource,
                        parameters,
                        this.insert(
                                columnOrRow,
                                count(parameters)
                        )
                )
        );
    }

    abstract String rangeLabel();

    /**
     * Sub classes must perform the insert before or after operation here.
     */
    abstract SpreadsheetDelta insert(final Range<R> columnOrRow, final int count);
}
