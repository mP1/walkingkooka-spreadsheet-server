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
import walkingkooka.collect.Range;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class SpreadsheetContextHateosResourceHandlerMetadataTestCase<H extends SpreadsheetContextHateosResourceHandlerMetadata>
        extends SpreadsheetContextHateosResourceHandlerTestCase2<H> {

    SpreadsheetContextHateosResourceHandlerMetadataTestCase() {
        super();
    }

    @Test
    public final void testHandleAllFails() {
        this.handleAllFails(this.collectionResource(),
                this.parameters(),
                UnsupportedOperationException.class);
    }

    @Test
    public final void testHandleManyFails() {
        this.handleManyFails(
                this.manyIds(),
                this.collectionResource(),
                this.parameters(),
                UnsupportedOperationException.class);
    }

    @Test
    public final void testHandleRangeFails() {
        this.handleRangeFails(this.range(),
                this.collectionResource(),
                this.parameters(),
                UnsupportedOperationException.class);
    }

    // helpers..........................................................................................................

    @Override
    abstract H createHandler(final SpreadsheetContext context);

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
                .set(SpreadsheetMetadataPropertyName.CREATOR, creatorEmail)
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, createDateTime)
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
}
