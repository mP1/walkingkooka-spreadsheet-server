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
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.AcceptLanguage;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;

import java.util.Locale;
import java.util.Optional;

public final class SpreadsheetMetadataHateosResourceHandlerSaveOrUpdateTest extends SpreadsheetMetadataHateosResourceHandlersTestCase2<SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate> {

    SpreadsheetMetadataHateosResourceHandlerSaveOrUpdateTest() {
        super();
    }

    // handle...........................................................................................................

    @Test
    public void testHandleNoneCreatesMetadataWithLocaleWithoutIdFails() {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("user@example.com"));

        this.handleNoneFails(
                Optional.empty(),
                Maps.of(
                        HttpHeaderName.ACCEPT_LANGUAGE,
                        AcceptLanguage.parse("en;q=0.8, fr-CA;q=0.9")
                ),
                new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                        return metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale.orElse(null));
                    }
                },
                IllegalStateException.class
        );
    }

    @Test
    public void testHandleNoneCreatesMetadataWithLocale() {
        final SpreadsheetMetadata metadata =
                SpreadsheetMetadata.EMPTY
                        .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, this.spreadsheetId())
                        .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("user@example.com"));

        final Locale locale = Locale.CANADA_FRENCH;

        this.handleNoneAndCheck(
                Optional.empty(),
                Maps.of(
                        HttpHeaderName.ACCEPT_LANGUAGE,
                        AcceptLanguage.parse("en;q=0.8, fr-CA;q=0.9")
                ),
                new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                        return metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale.get());
                    }
                },
                Optional.of(
                        metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale)
                )
        );
    }

    @Test
    public void testHandleNoneCreatesMetadataWithoutLocale() {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, this.spreadsheetId())
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("user@example.com"));

        this.handleNoneAndCheck(
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                        return metadata;
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
                new FakeSpreadsheetMetadataHateosResourceHandlerContext() {

                    @Override
                    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                        return metadata;
                    }
                },
                Optional.of(metadata)
        );
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate createHandler() {
        return SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate.INSTANCE;
    }

    @Override
    public SpreadsheetMetadataHateosResourceHandlerContext context() {
        return new FakeSpreadsheetMetadataHateosResourceHandlerContext() {
            @Override
            public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                return SpreadsheetMetadataHateosResourceHandlerSaveOrUpdateTest.this.metadata()
                        .remove(SpreadsheetMetadataPropertyName.SPREADSHEET_ID);
            }
        };
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                "create/saveMetadata"
        );
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate> type() {
        return SpreadsheetMetadataHateosResourceHandlerSaveOrUpdate.class;
    }
}
