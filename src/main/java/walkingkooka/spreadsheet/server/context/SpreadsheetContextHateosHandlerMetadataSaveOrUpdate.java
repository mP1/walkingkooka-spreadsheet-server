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

import walkingkooka.net.header.AcceptLanguage;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosHandler} that invokes {@link SpreadsheetContext#createMetadata(Optional)}
 */
final class SpreadsheetContextHateosHandlerMetadataSaveOrUpdate extends SpreadsheetContextHateosHandlerMetadata {

    static SpreadsheetContextHateosHandlerMetadataSaveOrUpdate with(final SpreadsheetContext context) {
        checkContext(context);

        return new SpreadsheetContextHateosHandlerMetadataSaveOrUpdate(context);
    }

    private SpreadsheetContextHateosHandlerMetadataSaveOrUpdate(final SpreadsheetContext context) {
        super(context);
    }

    @Override
    public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                   final Optional<SpreadsheetMetadata> resource,
                                                   final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkId(id);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(this.saveMetadata(id, resource));
    }

    /**
     * The request included an id and should also have a {@link SpreadsheetMetadata} this will update the store.
     */
    private SpreadsheetMetadata saveMetadata(final SpreadsheetId id,
                                             final Optional<SpreadsheetMetadata> metadata) {
        HateosHandler.checkResourceNotEmpty(metadata);

        return this.context.storeRepository(id).metadatas().save(metadata.get());
    }

    @Override
    public Optional<SpreadsheetMetadata> handleNone(final Optional<SpreadsheetMetadata> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        return Optional.of(this.createMetadata(resource, parameters));
    }

    /**
     * Fetches the locale from the ACCEPT-LANGUAGE header/parameter and calls the context to create a metadata, apply defaults
     * and also save the metadata to a store.
     */
    private SpreadsheetMetadata createMetadata(final Optional<SpreadsheetMetadata> metadata,
                                               final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResourceEmpty(metadata);

        final SpreadsheetMetadata saved = this.context.createMetadata(HttpHeaderName.ACCEPT_LANGUAGE.parameterValue(parameters)
                .flatMap(this::preferredLocale));
        saved.id()
                .orElseThrow(() -> new IllegalStateException(SpreadsheetMetadata.class.getSimpleName() + " missing id=" + saved));

        return saved;
    }

    private Optional<Locale> preferredLocale(final AcceptLanguage language) {
        return language.value().get(0).value().locale();
    }

    @Override
    String operation() {
        return "create/saveMetadata";
    }
}
