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

package walkingkooka.spreadsheet.server.engine.http;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.net.email.EmailAddress;
import walkingkooka.net.http.server.hateos.HateosHandlerTesting;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetParsePatterns;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.tree.expression.ExpressionNumberKind;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SpreadsheetEngineHateosHandlerTestCase2<H extends SpreadsheetEngineHateosHandler<I, V, C>, I extends Comparable<I>, V, C>
        extends SpreadsheetEngineHateosHandlerTestCase<H>
        implements HateosHandlerTesting<H, I, V, C>,
        ToStringTesting<H> {

    SpreadsheetEngineHateosHandlerTestCase2() {
        super();
    }

    @Test
    public final void testWithNullEngineFails() {
        assertThrows(NullPointerException.class, () -> this.createHandler(null, this.engineContext()));
    }

    @Test
    public final void testWithNullEngineContextSupplierFails() {
        assertThrows(NullPointerException.class, () -> this.createHandler(this.engine(), null));
    }

    @Override
    public final H createHandler() {
        return this.createHandler(this.engine(), this.engineContext());
    }

    abstract H createHandler(final SpreadsheetEngine engine,
                             final SpreadsheetEngineContext context);

    /**
     * Creates a {@link SpreadsheetMetadata} with id=1 and all the necessary required properties
     */
    final SpreadsheetMetadata metadata() {
        return SpreadsheetMetadata.EMPTY
                .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                .loadFromLocale()
                .set(SpreadsheetMetadataPropertyName.SPREADSHEET_ID, SpreadsheetId.with(1))
                .set(SpreadsheetMetadataPropertyName.CREATOR, EmailAddress.parse("creator@example.com"))
                .set(SpreadsheetMetadataPropertyName.CREATE_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 0))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_BY, EmailAddress.parse("modified@example.com"))
                .set(SpreadsheetMetadataPropertyName.MODIFIED_DATE_TIME, LocalDateTime.of(1999, 12, 31, 12, 0))
                .set(SpreadsheetMetadataPropertyName.CELL_CHARACTER_WIDTH, 1)
                .set(SpreadsheetMetadataPropertyName.DATETIME_OFFSET, 0L)
                .set(SpreadsheetMetadataPropertyName.DEFAULT_YEAR, 20)
                .set(SpreadsheetMetadataPropertyName.EXPRESSION_NUMBER_KIND, ExpressionNumberKind.BIG_DECIMAL)
                .set(SpreadsheetMetadataPropertyName.PRECISION, 0)
                .set(SpreadsheetMetadataPropertyName.ROUNDING_MODE, RoundingMode.HALF_UP)
                .set(SpreadsheetMetadataPropertyName.TWO_DIGIT_YEAR, 50)
                .set(SpreadsheetMetadataPropertyName.TEXT_FORMAT_PATTERN, SpreadsheetParsePatterns.parseTextFormatPattern("@"));
    }

    abstract SpreadsheetEngine engine();

    abstract SpreadsheetEngineContext engineContext();
}
