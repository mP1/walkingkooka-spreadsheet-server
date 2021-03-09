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

package walkingkooka.spreadsheet.server.label.hateos;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

abstract class SpreadsheetLabelHateosHandler2 extends SpreadsheetLabelHateosHandler{

    SpreadsheetLabelHateosHandler2(final SpreadsheetLabelStore store) {
        super(store);
    }

    @Override
    public final Optional<SpreadsheetLabelMapping> handleAll(final Optional<SpreadsheetLabelMapping> resource,
                                                             final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.empty();
    }

    @Override
    public final Optional<SpreadsheetLabelMapping> handleList(final List<SpreadsheetLabelName> list,
                                                              final Optional<SpreadsheetLabelMapping> resource,
                                                              final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkList(list);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.empty();
    }

    @Override
    public final Optional<SpreadsheetLabelMapping> handleRange(final Range<SpreadsheetLabelName> range,
                                                               final Optional<SpreadsheetLabelMapping> resource,
                                                               final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(range);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.empty();
    }
}
