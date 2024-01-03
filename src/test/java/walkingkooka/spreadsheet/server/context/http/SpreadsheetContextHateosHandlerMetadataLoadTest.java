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

package walkingkooka.spreadsheet.server.context.http;

import org.junit.jupiter.api.Test;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;
import walkingkooka.spreadsheet.server.context.FakeSpreadsheetContext;
import walkingkooka.spreadsheet.server.context.SpreadsheetContext;
import walkingkooka.spreadsheet.server.context.SpreadsheetContexts;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.store.LoadStoreException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public final class SpreadsheetContextHateosHandlerMetadataLoadTest extends SpreadsheetContextHateosHandlerMetadataTestCase<SpreadsheetContextHateosHandlerMetadataLoad> {

    SpreadsheetContextHateosHandlerMetadataLoadTest() {
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
                HateosHandler.NO_PARAMETERS,
                LoadStoreException.class
        );
    }

    @Test
    public void testLoadExisting() {
        final SpreadsheetId id = this.spreadsheetId();

        final EmailAddress creatorEmail = EmailAddress.parse("creator@example.com");
        final LocalDateTime createDateTime = LocalDateTime.of(1999, 12, 31, 12, 58, 59);
        final EmailAddress modifiedEmail = EmailAddress.parse("modified@example.com");
        final LocalDateTime modifiedDateTime = LocalDateTime.of(2000, 1, 2, 12, 58, 59);

        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, id)
                .set(SpreadsheetMetadataPropertyName.CREATOR, creatorEmail)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, createDateTime)
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, modifiedEmail)
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, modifiedDateTime);

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
                HateosHandler.NO_PARAMETERS,
                Optional.of(metadata)
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final SpreadsheetContext context = this.context();
        this.toStringAndCheck(this.createHandler(context), context + " loadMetadata");
    }

    // helpers..........................................................................................................

    @Override
    SpreadsheetContextHateosHandlerMetadataLoad createHandler(final SpreadsheetContext context) {
        return SpreadsheetContextHateosHandlerMetadataLoad.with(context);
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
    public Class<SpreadsheetContextHateosHandlerMetadataLoad> type() {
        return SpreadsheetContextHateosHandlerMetadataLoad.class;
    }
}
