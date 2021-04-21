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

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosHandler} that attempts to create/save or update a {@link SpreadsheetLabelMapping}.
 */
final class SpreadsheetLabelHateosHandlerSaveOrUpdate extends SpreadsheetLabelHateosHandler2 {

    static SpreadsheetLabelHateosHandlerSaveOrUpdate with(final SpreadsheetLabelStore store) {
        checkStore(store);

        return new SpreadsheetLabelHateosHandlerSaveOrUpdate(store);
    }

    private SpreadsheetLabelHateosHandlerSaveOrUpdate(final SpreadsheetLabelStore store) {
        super(store);
    }

    @Override
    public final Optional<SpreadsheetLabelMapping> handleNone(final Optional<SpreadsheetLabelMapping> resource,
                                                              final Map<HttpRequestAttribute<?>, Object> parameters) {
        final SpreadsheetLabelMapping mapping = HateosHandler.checkResourceNotEmpty(resource);
        HateosHandler.checkParameters(parameters);

        return this.saveOrUpdate(mapping);
    }

    @Override
    public Optional<SpreadsheetLabelMapping> handleOne(final SpreadsheetLabelName id,
                                                       final Optional<SpreadsheetLabelMapping> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkId(id);
        final SpreadsheetLabelMapping mapping = HateosHandler.checkResourceNotEmpty(resource);
        HateosHandler.checkParameters(parameters);

        // a rename is actually a delete followed by a save which creates
        if(!id.equals(mapping.id().orElse(null))) {
            this.store.delete(id);
        }

        return this.saveOrUpdate(mapping);
    }

    private Optional<SpreadsheetLabelMapping> saveOrUpdate(final SpreadsheetLabelMapping mapping) {
        return Optional.of(this.store.save(mapping));
    }

    @Override
    public String toString() {
        return "load " + this.store;
    }
}
