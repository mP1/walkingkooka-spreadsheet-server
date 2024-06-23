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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.store.MissingStoreException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SpreadsheetContextHateosResourceHandlerMetadataLoadTest extends SpreadsheetContextHateosResourceHandlerMetadataTestCase<SpreadsheetContextHateosResourceHandlerMetadataLoad> {

    SpreadsheetContextHateosResourceHandlerMetadataLoadTest() {
        super();
    }

    // handle...........................................................................................................

    @Test
    public void testHandleIdWithMetadataResourceFails() {
        this.handleOneFails(this.id(),
                Optional.of(this.metadataWithDefaults()),
                this.parameters(),
                IllegalArgumentException.class);
    }

    @Test
    public void testLoadUnknown404() {
        final SpreadsheetId id = this.spreadsheetId();

        this.handleOneFails(
                this.createHandler(
                        new FakeSpreadsheetContext() {
                            @Override
                            public SpreadsheetStoreRepository storeRepository(final SpreadsheetId i) {
                                checkEquals(id, i, "spreadsheetId");
                                return new FakeSpreadsheetStoreRepository() {
                                    @Override
                                    public SpreadsheetMetadataStore metadatas() {
                                        return SpreadsheetMetadataStores.treeMap(); // empty
                                    }
                                };
                            }
                        }),
                id,
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                MissingStoreException.class
        );
    }

    @Test
    public void testLoadExisting() {
        final SpreadsheetId id = SpreadsheetId.with(111);
        final SpreadsheetMetadata metadata = this.metadata(id.value());

        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();
        store.save(metadata);

        this.handleOneAndCheck(
                this.createHandler(
                        new FakeSpreadsheetContext() {
                            @Override
                            public SpreadsheetStoreRepository storeRepository(final SpreadsheetId i) {
                                checkEquals(id, i, "spreadsheetId");
                                return new FakeSpreadsheetStoreRepository() {
                                    @Override
                                    public SpreadsheetMetadataStore metadatas() {
                                        return store;
                                    }
                                };
                            }
                        }),
                id,
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(metadata)
        );
    }

    @Test
    public void testLoadAll() {
        final SpreadsheetMetadata metadata1 = metadata(111);
        final SpreadsheetMetadata metadata2 = metadata(222);

        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();
        store.save(metadata1);
        store.save(metadata2);

        this.handleAllAndCheck(
                this.createHandler(
                        new FakeSpreadsheetContext() {
                            @Override
                            public SpreadsheetMetadataStore metadataStore() {
                                return store;
                            }
                        }
                ),
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(
                        SpreadsheetMetadataSet.with(
                                Sets.of(
                                        metadata1,
                                        metadata2
                                )
                        )
                )
        );
    }

    @Test
    public void testLoadAllCountMissingFrom() {
        final SpreadsheetMetadata metadata1 = metadata(1);
        final SpreadsheetMetadata metadata2 = metadata(2);
        final SpreadsheetMetadata metadata3 = metadata(3);
        final SpreadsheetMetadata metadata4 = metadata(4);

        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();
        store.save(metadata1);
        store.save(metadata2);
        store.save(metadata3);
        store.save(metadata4);

        this.handleAllAndCheck(
                this.createHandler(
                        new FakeSpreadsheetContext() {
                            @Override
                            public SpreadsheetMetadataStore metadataStore() {
                                return store;
                            }
                        }
                ),
                Optional.empty(),
                Maps.of(
                        SpreadsheetContextHateosResourceHandlerMetadataLoad.COUNT,
                        Lists.of("2")
                ),
                Optional.of(
                        SpreadsheetMetadataSet.with(
                                Sets.of(
                                        metadata1,
                                        metadata2
                                )
                        )
                )
        );
    }

    @Test
    public void testLoadAllFromAndCount() {
        final SpreadsheetMetadata metadata1 = metadata(1);
        final SpreadsheetMetadata metadata2 = metadata(2);
        final SpreadsheetMetadata metadata3 = metadata(3);
        final SpreadsheetMetadata metadata4 = metadata(4);

        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();
        store.save(metadata1);
        store.save(metadata2);
        store.save(metadata3);
        store.save(metadata4);

        this.handleAllAndCheck(
                this.createHandler(
                        new FakeSpreadsheetContext() {
                            @Override
                            public SpreadsheetMetadataStore metadataStore() {
                                return store;
                            }
                        }
                ),
                Optional.empty(),
                Maps.of(
                        SpreadsheetContextHateosResourceHandlerMetadataLoad.FROM,
                        Lists.of("1"),
                        SpreadsheetContextHateosResourceHandlerMetadataLoad.COUNT,
                        Lists.of("2")
                ),
                Optional.of(
                        SpreadsheetMetadataSet.with(
                                Sets.of(
                                        metadata2,
                                        metadata3
                                )
                        )
                )
        );
    }

    @Test
    public void testLoadList() {
        final SpreadsheetMetadata metadata1 = metadata(111);
        final SpreadsheetMetadata metadata2 = metadata(222);
        final SpreadsheetMetadata metadata3 = metadata(333);

        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();
        store.save(metadata1);
        store.save(metadata2);
        store.save(metadata3);

        final SpreadsheetMetadataSet all = SpreadsheetMetadataSet.with(
                Sets.of(
                        metadata1,
                        metadata3
                )
        );

        this.handleManyAndCheck(
                this.createHandler(
                        new FakeSpreadsheetContext() {
                            @Override
                            public SpreadsheetMetadataStore metadataStore() {
                                return store;
                            }
                        }
                ),
                all.stream()
                        .map(m -> m.id().get())
                        .collect(Collectors.toCollection(Sets::sorted)),
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(all)
        );
    }

    private SpreadsheetMetadata metadata(final long id) {
        return SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(id))
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 58, 59))
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("modified-by@example.com"))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.of(2024, 4, 2, 15, 25, 0));
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final SpreadsheetContext context = this.context();
        this.toStringAndCheck(this.createHandler(context), context + " loadMetadata");
    }

    // helpers..........................................................................................................

    @Override
    SpreadsheetContextHateosResourceHandlerMetadataLoad createHandler(final SpreadsheetContext context) {
        return SpreadsheetContextHateosResourceHandlerMetadataLoad.with(context);
    }

    @Override
    SpreadsheetContext context() {
        return SpreadsheetContexts.fake();
    }

    private SpreadsheetMetadata metadataWithDefaults() {
        return SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.of(2000, 12, 31, 12, 58, 59));
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetContextHateosResourceHandlerMetadataLoad> type() {
        return SpreadsheetContextHateosResourceHandlerMetadataLoad.class;
    }
}
