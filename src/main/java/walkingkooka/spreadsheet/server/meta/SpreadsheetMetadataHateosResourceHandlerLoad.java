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

package walkingkooka.spreadsheet.server.meta;

import walkingkooka.collect.set.SortedSets;
import walkingkooka.net.UrlParameterName;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.store.MissingStoreException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link HateosResourceHandler} that invokes {@link SpreadsheetMetadataStore#create(EmailAddress, Optional)}.
 */
final class SpreadsheetMetadataHateosResourceHandlerLoad extends SpreadsheetMetadataHateosResourceHandler
        implements UnsupportedHateosResourceHandlerHandleNone<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet, SpreadsheetMetadataHateosResourceHandlerContext>,
        UnsupportedHateosResourceHandlerHandleRange<SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet, SpreadsheetMetadataHateosResourceHandlerContext> {

    final static SpreadsheetMetadataHateosResourceHandlerLoad INSTANCE = new SpreadsheetMetadataHateosResourceHandlerLoad();

    private SpreadsheetMetadataHateosResourceHandlerLoad() {
    }

    @Override
    public Optional<SpreadsheetMetadata> handleOne(final SpreadsheetId id,
                                                   final Optional<SpreadsheetMetadata> resource,
                                                   final Map<HttpRequestAttribute<?>, Object> parameters,
                                                   final SpreadsheetMetadataHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final Optional<SpreadsheetMetadata> loaded = context.storeRepository(id)
                .metadatas()
                .load(id);
        if (!loaded.isPresent()) {
            throw new MissingStoreException("Unable to load spreadsheet " + id);
        }
        return loaded;
    }

    @Override
    public Optional<SpreadsheetMetadataSet> handleAll(final Optional<SpreadsheetMetadataSet> resource,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                                      final SpreadsheetMetadataHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final int from = FROM.firstParameterValue(parameters)
                .map(Integer::parseInt)
                .orElse(0);
        final int count = COUNT.firstParameterValue(parameters)
                .map(Integer::parseInt)
                .orElse(DEFAULT_COUNT);

        final Set<SpreadsheetMetadata> all = SortedSets.tree(HateosResource.comparator());
        all.addAll(
                context.metadataStore()
                        .values(
                                from,
                                Math.min(
                                        MAX_COUNT,
                                        count
                                )
                        )
        );

        return Optional.of(
                SpreadsheetMetadataSet.with(all)
        );
    }

    // @VisibleForTesting
    final static UrlParameterName FROM = UrlParameterName.with("from");

    // @VisibleForTesting
    final static UrlParameterName COUNT = UrlParameterName.with("count");

    private final static int DEFAULT_COUNT = 20;

    private final static int MAX_COUNT = 40;

    @Override
    public Optional<SpreadsheetMetadataSet> handleMany(final Set<SpreadsheetId> ids,
                                                       final Optional<SpreadsheetMetadataSet> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final SpreadsheetMetadataHateosResourceHandlerContext context) {
        HateosResourceHandler.checkManyIds(ids);
        HateosResourceHandler.checkResource(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        final Set<SpreadsheetMetadata> all = SortedSets.tree(HateosResource.comparator());
        final SpreadsheetMetadataStore store = context.metadataStore();

        for (final SpreadsheetId id : ids) {
            store.load(id)
                    .ifPresent(all::add);
        }

        return Optional.of(
                SpreadsheetMetadataSet.with(all)
        );
    }

    @Override
    String operation() {
        return "loadMetadata";
    }
}
