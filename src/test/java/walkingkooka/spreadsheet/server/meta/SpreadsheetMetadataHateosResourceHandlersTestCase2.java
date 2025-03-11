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
import walkingkooka.collect.Range;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class SpreadsheetMetadataHateosResourceHandlersTestCase2<H extends SpreadsheetMetadataHateosResourceHandler>
    extends SpreadsheetMetadataHateosResourceHandlerTestCase<H>
    implements HateosResourceHandlerTesting<H, SpreadsheetId, SpreadsheetMetadata, SpreadsheetMetadataSet, SpreadsheetMetadataHateosResourceHandlerContext> {

    SpreadsheetMetadataHateosResourceHandlersTestCase2() {
        super();
    }

    @Test
    public final void testHandleManyFails() {
        this.handleManyFails(
            this.manyIds(),
            this.collectionResource(),
            this.parameters(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    @Test
    public final void testHandleRangeFails() {
        this.handleRangeFails(
            this.range(),
            this.collectionResource(),
            this.parameters(),
            this.context(),
            UnsupportedOperationException.class
        );
    }

    // helpers..........................................................................................................

    @Override
    public final SpreadsheetId id() {
        return this.spreadsheetId();
    }

    @Override
    public final Set<SpreadsheetId> manyIds() {
        return Sets.of(this.spreadsheetId());
    }

    @Override
    public final Range<SpreadsheetId> range() {
        return Range.singleton(this.spreadsheetId());
    }

    final SpreadsheetMetadata metadata() {
        final EmailAddress creatorEmail = EmailAddress.parse("creator@example.com");
        final LocalDateTime createDateTime = LocalDateTime.of(1999, 12, 31, 12, 58, 59);
        final EmailAddress modifiedEmail = EmailAddress.parse("modified@example.com");
        final LocalDateTime modifiedDateTime = LocalDateTime.of(2000, 1, 2, 12, 58, 59);

        return SpreadsheetMetadata.EMPTY
            .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, this.id())
            .set(SpreadsheetMetadataPropertyName.CREATED_BY, creatorEmail)
            .set(SpreadsheetMetadataPropertyName.CREATED_TIMESTAMP, createDateTime)
            .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.ENGLISH)
            .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, modifiedEmail)
            .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, modifiedDateTime);
    }

    final SpreadsheetId spreadsheetId() {
        return SpreadsheetId.with(0x1234);
    }

    @Override
    public final Optional<SpreadsheetMetadata> resource() {
        return Optional.empty();
    }

    @Override
    public final Optional<SpreadsheetMetadataSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public final Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosResourceHandler.NO_PARAMETERS;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public final String typeNameSuffix() {
        return "";
    }
}
