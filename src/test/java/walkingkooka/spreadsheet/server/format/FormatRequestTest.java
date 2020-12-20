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

package walkingkooka.spreadsheet.server.format;

import org.junit.jupiter.api.Test;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetDateFormatPattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class FormatRequestTest extends FormatterTestCase2<FormatRequest> {

    private final static LocalDate VALUE = LocalDate.of(1999, 12, 31);
    private final static SpreadsheetDateFormatPattern PATTERN = SpreadsheetFormatPattern.parseDateFormatPattern("dd/mm/yyyy");


    @Test
    public void testWithNullValueFails() {
        assertThrows(NullPointerException.class, () -> FormatRequest.with(null, PATTERN));
    }

    @Test
    public void testWithNullPatternFails() {
        assertThrows(NullPointerException.class, () -> FormatRequest.with(VALUE, null));
    }

    @Test
    public void testWith() {
        final FormatRequest request = FormatRequest.with(VALUE, PATTERN);
        assertEquals(VALUE, request.value(), "value");
        assertEquals(PATTERN, request.pattern(), "PATTERN");
    }

    // Object............................................................................................................

    @Test
    public void testDifferentValue() {
        this.checkNotEquals(FormatRequest.with(LocalDate.now(), PATTERN));
    }

    @Test
    public void testDifferentPattern() {
        this.checkNotEquals(FormatRequest.with(VALUE, SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/mmm/dd")));
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "1999-12-31 dd/mm/yyyy");
    }

    @Override
    public FormatRequest createObject() {
        return FormatRequest.with(VALUE, PATTERN);
    }

    @Override
    public Class<FormatRequest> type() {
        return FormatRequest.class;
    }
}
