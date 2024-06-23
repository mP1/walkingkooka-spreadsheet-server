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
import walkingkooka.collect.map.Maps;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.header.AcceptLanguage;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStore;
import walkingkooka.spreadsheet.meta.store.SpreadsheetMetadataStores;

import java.util.Locale;
import java.util.Optional;

public final class SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdateTest extends SpreadsheetContextHateosResourceHandlerMetadataTestCase<SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate> {

    SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdateTest() {
        super();
    }

    // handle...........................................................................................................

    @Test
    public void testHandleNoneCreatesMetadataWithLocaleWithoutIdFails() {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("user@example.com"));

        this.handleNoneFails(this.createHandler(
                new FakeSpreadsheetContext() {

                    @Override
                    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                        return metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale.orElse(null));
                    }
                }),
                Optional.empty(),
                Maps.of(HttpHeaderName.ACCEPT_LANGUAGE, AcceptLanguage.parse("en;q=0.8, fr-CA;q=0.9")),
                IllegalStateException.class);
    }

    @Test
    public void testHandleNoneCreatesMetadataWithLocale() {
        final SpreadsheetMetadata metadata =
                SpreadsheetMetadata.EMPTY
                        .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, this.spreadsheetId())
                        .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("user@example.com"));

        final Locale locale = Locale.CANADA_FRENCH;

        this.handleNoneAndCheck(this.createHandler(
                new FakeSpreadsheetContext() {

                    @Override
                    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                        return metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale.get());
                    }
                }),
                Optional.empty(),
                Maps.of(HttpHeaderName.ACCEPT_LANGUAGE, AcceptLanguage.parse("en;q=0.8, fr-CA;q=0.9")),
                Optional.of(metadata.set(SpreadsheetMetadataPropertyName.LOCALE, locale)));
    }

    @Test
    public void testHandleNoneCreatesMetadataWithoutLocale() {
        final SpreadsheetMetadata metadata = SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, this.spreadsheetId())
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("user@example.com"));

        this.handleNoneAndCheck(this.createHandler(
                new FakeSpreadsheetContext() {

                    @Override
                    public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                        return metadata;
                    }
                }),
                Optional.empty(),
                HateosResourceHandler.NO_PARAMETERS,
                Optional.of(metadata));
    }

    @Test
    public void testHandleIdWithoutMetadataResourceFails() {
        this.handleOneFails(this.id(),
                Optional.empty(),
                this.parameters(),
                IllegalArgumentException.class);
    }

    @Test
    public void testHandleIdWithMetadataSaves() {
        final SpreadsheetMetadataStore store = SpreadsheetMetadataStores.treeMap();
        final SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate handler = SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate.with(
                new FakeSpreadsheetContext() {

                    @Override
                    public SpreadsheetMetadata saveMetadata(final SpreadsheetMetadata metadata) {
                        return metadata;
                    }
                }
        );

        final SpreadsheetId id = this.id();
        final SpreadsheetMetadata metadata = this.metadata();

        this.handleOneAndCheck(
                handler,
                id,
                Optional.of(metadata),
                this.parameters(),
                Optional.of(metadata)
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        final SpreadsheetContext context = this.context();
        this.toStringAndCheck(this.createHandler(context), context + " create/saveMetadata");
    }

    // helpers..........................................................................................................

    @Override
    SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate createHandler(final SpreadsheetContext context) {
        return SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate.with(context);
    }

    @Override
    SpreadsheetContext context() {
        return new FakeSpreadsheetContext() {
            @Override
            public SpreadsheetMetadata createMetadata(final Optional<Locale> locale) {
                return SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdateTest.this.metadata().remove(SpreadsheetMetadataPropertyName.SPREADSHEET_ID);
            }
        };
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate> type() {
        return SpreadsheetContextHateosResourceHandlerMetadataSaveOrUpdate.class;
    }
}
