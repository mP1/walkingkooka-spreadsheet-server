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
import walkingkooka.collect.map.Maps;
import walkingkooka.environment.AuditInfo;
import walkingkooka.net.UrlPath;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.AcceptLanguage;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.FakeSpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetContext;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

public final class SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdateTest extends SpreadsheetMetadataHateosResourceHandlersTestCase2<SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate> {

    private final static AuditInfo AUDIT_INFO = AuditInfo.with(
        USER,
        LocalDateTime.of(1999, 12, 31, 12, 58, 59),
        USER,
        LocalDateTime.of(2024, 4, 2, 15, 25, 0)
    );

    // handle...........................................................................................................

    @Test
    @Override
    public void testHandleOneNullParametersFails() {
        this.handleOneFails(
            this.id(),
            Optional.of(
                SpreadsheetMetadata.EMPTY
            ),
            null,
            this.path(),
            this.context(),
            NullPointerException.class
        );
    }

    @Test
    @Override
    public void testHandleOneNullPathFails() {
        this.handleOneFails(
            this.id(),
            Optional.of(
                SpreadsheetMetadata.EMPTY
            ),
            this.parameters(),
            null,
            this.context(),
            NullPointerException.class
        );
    }

    @Test
    @Override
    public void testHandleOneNullContextFails() {
        this.handleOneFails(
            this.id(),
            Optional.of(
                SpreadsheetMetadata.EMPTY
            ),
            this.parameters(),
            this.path(),
            null,
            NullPointerException.class
        );
    }

    @Test
    public void testHandleAllFails() {
        this.handleAllFails(
            this.collectionResource(),
            this.parameters(),
            this.path(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public void testHandleNoneCreatesMetadataWithLocale() {
        final SpreadsheetMetadata metadata =
            SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, this.spreadsheetId())
                .set(SpreadsheetMetadataPropertyName.AUDIT_INFO, AUDIT_INFO);

        final Locale locale = Locale.CANADA_FRENCH;

        this.handleNoneAndCheck(
            Optional.empty(),
            Maps.of(
                HttpHeaderName.ACCEPT_LANGUAGE,
                AcceptLanguage.parse("en;q=0.8, fr-CA;q=0.9")
            ),
            UrlPath.EMPTY,
            new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

                @Override
                public SpreadsheetContext createSpreadsheetContext(final EmailAddress user,
                                                                   final Optional<Locale> locale) {
                    return new FakeSpreadsheetContext() {

                        @Override
                        public SpreadsheetMetadata spreadsheetMetadata() {
                            return metadata.set(
                                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                SPREADSHEET_ID
                            ).set(
                                SpreadsheetMetadataPropertyName.AUDIT_INFO,
                                metadata.getOrFail(SpreadsheetMetadataPropertyName.AUDIT_INFO)
                                    .setCreatedBy(user)
                                    .setModifiedBy(user)
                            ).set(
                                SpreadsheetMetadataPropertyName.LOCALE,
                                locale.get()
                            );
                        }
                    };
                }

                @Override
                public Optional<EmailAddress> user() {
                    return Optional.of(USER);
                }
            },
            Optional.of(
                metadata.set(
                    SpreadsheetMetadataPropertyName.LOCALE,
                    locale
                )
            )
        );
    }

    @Test
    public void testHandleNoneCreatesMetadataWithoutLocale() {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
            .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, this.spreadsheetId())
            .set(SpreadsheetMetadataPropertyName.AUDIT_INFO, AUDIT_INFO);

        this.handleNoneAndCheck(
            Optional.empty(),
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

                @Override
                public SpreadsheetContext createSpreadsheetContext(final EmailAddress user,
                                                                   final Optional<Locale> locale) {
                    return new FakeSpreadsheetContext() {

                        @Override
                        public SpreadsheetMetadata spreadsheetMetadata() {
                            return metadata.set(
                                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                SPREADSHEET_ID
                            ).set(
                                SpreadsheetMetadataPropertyName.AUDIT_INFO,
                                metadata.getOrFail(SpreadsheetMetadataPropertyName.AUDIT_INFO)
                                    .setCreatedBy(user)
                                    .setModifiedBy(user)
                            ).setOrRemove(
                                SpreadsheetMetadataPropertyName.LOCALE,
                                locale.orElse(null)
                            );
                        }
                    };
                }

                @Override
                public Optional<EmailAddress> user() {
                    return Optional.of(USER);
                }
            },
            Optional.of(metadata)
        );
    }

    @Test
    public void testHandleIdWithoutMetadataResourceFails() {
        this.handleOneFails(
            this.id(),
            Optional.empty(),
            this.parameters(),
            this.path(),
            this.context(),
            IllegalArgumentException.class
        );
    }

    @Test
    public void testHandleIdWithMetadataSaves() {
        final SpreadsheetId id = this.id();
        final SpreadsheetMetadata metadata = this.metadata();

        this.handleOneAndCheck(
            id,
            Optional.of(metadata),
            this.parameters(),
            this.path(),
            new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

                @Override
                public Optional<SpreadsheetContext> spreadsheetContext(final SpreadsheetId i) {
                    return Optional.ofNullable(
                        id.equals(i) ?
                            new FakeSpreadsheetContext() {
                                @Override
                                public SpreadsheetEngineContext spreadsheetEngineContext() {
                                    return new FakeSpreadsheetEngineContext() {
                                        @Override
                                        public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                                            return metadata;
                                        }
                                    };
                                }
                            } :
                            null
                    );
                }
            },
            Optional.of(metadata)
        );
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate createHandler() {
        return SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate.INSTANCE;
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext context() {
        return new FakeSpreadsheetMetadataHateosResourceHandlerContext() {
            @Override
            public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                return SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdateTest.this.metadata()
                    .remove(SpreadsheetMetadataPropertyName.SPREADSHEET_ID);
            }

            @Override
            public Optional<EmailAddress> user() {
                return Optional.of(USER);
            }
        };
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            "createSaveUpdateMetadata"
        );
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate> type() {
        return SpreadsheetMetadataHateosResourceHandlerCreateOrSaveOrUpdate.class;
    }
}
