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

package walkingkooka.spreadsheet.server.context.hateos;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;
import walkingkooka.store.Store;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link HateosHandler} that requires a {@link Store metadata}.
 */
abstract class SpreadsheetContextHateosHandlerMetadata implements HateosHandler<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadata> {

    /**
     * Checks required factory method parameters are not null.
     */
    static void checkContext(final SpreadsheetContext context) {
        Objects.requireNonNull(context, "context");
    }

    SpreadsheetContextHateosHandlerMetadata(final SpreadsheetContext context) {
        super();
        this.context = context;
    }

    @Override
    public final Optional<SpreadsheetMetadata> handleAll(final Optional<SpreadsheetMetadata> resource,
                                                         final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetMetadata> handleList(final List<SpreadsheetId> list,
                                                    final Optional<SpreadsheetMetadata> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkList(list);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }


    @Override
    public final Optional<SpreadsheetMetadata> handleRange(final Range<SpreadsheetId> range,
                                                           final Optional<SpreadsheetMetadata> resource,
                                                           final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(range);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    final SpreadsheetContext context;

    @Override
    public final String toString() {
        return this.context + " " + this.operation();
    }

    abstract String operation();
}
