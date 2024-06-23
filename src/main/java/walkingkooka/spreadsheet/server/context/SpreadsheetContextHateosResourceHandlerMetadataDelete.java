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

package walkingkooka.spreadsheet.server.context;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that handles deletions.
 */
final class SpreadsheetContextHateosResourceHandlerMetadataDelete extends SpreadsheetContextHateosResourceHandlerMetadata {

    static SpreadsheetContextHateosResourceHandlerMetadataDelete with(final SpreadsheetContext context) {
        checkContext(context);

        return new SpreadsheetContextHateosResourceHandlerMetadataDelete(context);
    }

    private SpreadsheetContextHateosResourceHandlerMetadataDelete(final SpreadsheetContext context) {
        super(context);
    }

    @Override
    public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                   final Optional<SpreadsheetMetadata> resource,
                                                   final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkId(id);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);

        this.context.storeRepository(id)
                .metadatas()
                .delete(id);
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetMetadata> handleNone(final Optional<SpreadsheetMetadata> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }


    @Override
    public Optional<SpreadsheetMetadataSet> handleAll(final Optional<SpreadsheetMetadataSet> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetMetadataSet> handleMany(final Set<SpreadsheetId> ids,
                                                       final Optional<SpreadsheetMetadataSet> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkManyIds(ids);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SpreadsheetMetadataSet> handleRange(final Range<SpreadsheetId> ids,
                                                        final Optional<SpreadsheetMetadataSet> resource,
                                                         final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkIdRange(ids);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    String operation() {
        return "deleteMetadata";
    }
}
