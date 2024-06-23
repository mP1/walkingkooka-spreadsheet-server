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

package walkingkooka.spreadsheet.server.label;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

abstract class SpreadsheetLabelHateosResourceHandler implements HateosResourceHandler<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping> {

    static void checkStore(final SpreadsheetLabelStore store) {
        Objects.requireNonNull(store, "store");
    }

    SpreadsheetLabelHateosResourceHandler(final SpreadsheetLabelStore store) {
        super();
        this.store = store;
    }

    @Override
    public final Optional<SpreadsheetLabelMapping> handleAll(final Optional<SpreadsheetLabelMapping> resource,
                                                             final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);

        return Optional.empty();
    }

    @Override
    public final Optional<SpreadsheetLabelMapping> handleMany(final Set<SpreadsheetLabelName> ids,
                                                              final Optional<SpreadsheetLabelMapping> resource,
                                                              final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkManyIds(ids);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);

        return Optional.empty();
    }

    @Override
    public final Optional<SpreadsheetLabelMapping> handleRange(final Range<SpreadsheetLabelName> labels,
                                                               final Optional<SpreadsheetLabelMapping> resource,
                                                               final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkIdRange(labels);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);

        return Optional.empty();
    }

    final SpreadsheetLabelStore store;
}
