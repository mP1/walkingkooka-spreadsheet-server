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
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.store.LoadStoreException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosHandler} that invokes {@link SpreadsheetContext#createMetadata(Optional)}.
 */
final class SpreadsheetContextHateosHandlerMetadataLoad extends SpreadsheetContextHateosHandlerMetadata {

    static SpreadsheetContextHateosHandlerMetadataLoad with(final SpreadsheetContext context) {
        checkContext(context);

        return new SpreadsheetContextHateosHandlerMetadataLoad(context);
    }

    private SpreadsheetContextHateosHandlerMetadataLoad(final SpreadsheetContext context) {
        super(context);
    }

    @Override
    public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                   final Optional<SpreadsheetMetadata> resource,
                                                   final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkId(id);
        HateosHandler.checkResourceEmpty(resource);
        HateosHandler.checkParameters(parameters);

        final Optional<SpreadsheetMetadata> loaded = this.context.storeRepository(id)
                .metadatas()
                .load(id);
        if (!loaded.isPresent()) {
            throw new LoadStoreException("Unable to load spreadsheet " + id);
        }
        return loaded;
    }

    @Override
    public Optional<SpreadsheetMetadata> handleNone(final Optional<SpreadsheetMetadata> resource,
                                                    final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }


    @Override
    public Optional<SpreadsheetMetadataList> handleAll(final Optional<SpreadsheetMetadataList> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        final int from = FROM.firstParameterValue(parameters)
                .map(Integer::parseInt)
                .orElse(0);
        final int count = COUNT.firstParameterValue(parameters)
                .map(Integer::parseInt)
                .orElse(DEFAULT_COUNT);

        final SpreadsheetMetadataList all = SpreadsheetMetadataList.empty();
        all.addAll(
                this.context.metadataStore()
                        .values(
                                from,
                                Math.min(
                                        MAX_COUNT,
                                        count
                                )
                        )
        );

        return Optional.of(all);
    }

    // @VisibleForTesting
    final static UrlParameterName FROM = UrlParameterName.with("from");

    // @VisibleForTesting
    final static UrlParameterName COUNT = UrlParameterName.with("count");

    private final static int DEFAULT_COUNT = 20;

    private final static int MAX_COUNT = 40;

    @Override
    public Optional<SpreadsheetMetadataList> handleList(final List<SpreadsheetId> list,
                                                        final Optional<SpreadsheetMetadataList> resource,
                                                        final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkList(list);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        final SpreadsheetMetadataList all = SpreadsheetMetadataList.empty();
        final SpreadsheetMetadataStore store = this.context.metadataStore();

        for (final SpreadsheetId id : list) {
            store.load(id)
                    .ifPresent(all::add);
        }

        return Optional.of(all);
    }

    @Override
    public Optional<SpreadsheetMetadataList> handleRange(final Range<SpreadsheetId> range,
                                                         final Optional<SpreadsheetMetadataList> resource,
                                                         final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosHandler.checkRange(range);
        HateosHandler.checkResource(resource);
        HateosHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    String operation() {
        return "loadMetadata";
    }
}
