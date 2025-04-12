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

import org.junit.jupiter.api.Test;
import walkingkooka.environment.AuditInfo;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public final class SpreadsheetMetadataHateosResourceHandlerDeleteTest extends SpreadsheetMetadataHateosResourceHandlersTestCase2<SpreadsheetMetadataHateosResourceHandlerDelete> {

    SpreadsheetMetadataHateosResourceHandlerDeleteTest() {
        super();
    }

    // handle...........................................................................................................

    @Test
    public void testHandleAllFails() {
        this.handleAllFails(
            this.collectionResource(),
            this.parameters(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleIdWithMetadataResourceFails() {
        this.handleOneFails(
            this.id(),
            Optional.of(this.metadataWithDefaults()),
            this.parameters(),
            this.context(),
            IllegalArgumentException.class
        );
    }

    @Test
    public void testHandleOneDeleteUnknownOk() {
        final SpreadsheetId id = this.spreadsheetId();

        this.handleOneAndCheck(
            id,
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            new FakeSpreadsheetMetadataHateosResourceHandlerContext() {
                @Override
                public SpreadsheetStoreRepository storeRepository(final SpreadsheetId i) {
                    checkEquals(id, i, "spreadsheetId");
                    return new FakeSpreadsheetStoreRepository() {
                        @Override
                        public SpreadsheetMetadataStore metadatas() {
                            return SpreadsheetMetadataTesting.spreadsheetMetadataStore(); // empty
                        }
                    };
                }
            },
            Optional.empty()
        );
    }

    @Test
    public void testHandleOneDeleteExisting() {
        final SpreadsheetId id = SpreadsheetId.with(111);
        final SpreadsheetMetadata metadata = this.metadata(id.value());

        final SpreadsheetMetadataStore store = SpreadsheetMetadataTesting.spreadsheetMetadataStore();
        store.save(metadata);

        this.handleOneAndCheck(
            id,
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            new FakeSpreadsheetMetadataHateosResourceHandlerContext() {
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
            },
            Optional.empty()
        );
    }

    private SpreadsheetMetadata metadata(final long id) {
        return SpreadsheetMetadata.EMPTY
            .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(id))
            .set(
                SpreadsheetMetadataPropertyName.AUDIT_INFO,
                AuditInfo.with(
                    USER,
                    LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                    USER,
                    LocalDateTime.of(2024, 4, 2, 15, 25, 0)
                )
            ).set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH);
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerDelete createHandler() {
        return SpreadsheetMetadataHateosResourceHandlerDelete.INSTANCE;
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext context() {
        return SpreadsheetMetadataHateosResourceHandlerContexts.fake();
    }

    private SpreadsheetMetadata metadataWithDefaults() {
        return SpreadsheetMetadata.EMPTY.set(
            SpreadsheetMetadataPropertyName.AUDIT_INFO,
            AuditInfo.with(
                USER,
                LocalDateTime.of(1999, 12, 31, 12, 58, 59),
                USER,
                LocalDateTime.of(1999, 12, 31, 12, 58, 59)
                )
            );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            "deleteMetadata"
        );
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetMetadataHateosResourceHandlerDelete> type() {
        return SpreadsheetMetadataHateosResourceHandlerDelete.class;
    }
}
