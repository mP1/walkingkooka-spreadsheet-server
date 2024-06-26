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

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that attempts to create/save or update a {@link SpreadsheetLabelMapping}.
 */
final class SpreadsheetLabelHateosResourceHandlerSaveOrUpdate extends SpreadsheetLabelHateosResourceHandler {

    final static SpreadsheetLabelHateosResourceHandlerSaveOrUpdate INSTANCE = new SpreadsheetLabelHateosResourceHandlerSaveOrUpdate();

    private SpreadsheetLabelHateosResourceHandlerSaveOrUpdate() {
        super();
    }

    @Override
    public Optional<SpreadsheetLabelMapping> handleNone(final Optional<SpreadsheetLabelMapping> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters,
                                                        final SpreadsheetEngineHateosResourceHandlerContext context) {
        final SpreadsheetLabelMapping mapping = HateosResourceHandler.checkResourceNotEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        return this.saveOrUpdate(
                mapping,
                context
        );
    }

    @Override
    public Optional<SpreadsheetLabelMapping> handleOne(final SpreadsheetLabelName id,
                                                       final Optional<SpreadsheetLabelMapping> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        final SpreadsheetLabelMapping mapping = HateosResourceHandler.checkResourceNotEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        // a rename is actually a delete followed by a save which creates
        if (!id.equals(mapping.id().orElse(null))) {
            context.storeRepository()
                    .labels()
                    .delete(id);
        }

        return this.saveOrUpdate(
                mapping,
                context
        );
    }

    private Optional<SpreadsheetLabelMapping> saveOrUpdate(final SpreadsheetLabelMapping mapping,
                                                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        return Optional.of(
                context.storeRepository()
                        .labels()
                        .save(mapping)
        );
    }

    @Override
    public String toString() {
        return SpreadsheetLabelStore.class.getSimpleName() + ".load ";
    }
}
