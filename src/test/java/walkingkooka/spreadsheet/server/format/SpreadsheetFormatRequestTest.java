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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatRequestTest extends SpreadsheetFormatterTestCase2<SpreadsheetFormatRequest> {

    private final static LocalDate VALUE = LocalDate.of(1999, 12, 31);
    private final static SpreadsheetDateFormatPattern PATTERN = SpreadsheetFormatPattern.parseDateFormatPattern("dd/mm/yyyy");


    @Test
    public void testWithNullValueFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetFormatRequest.with(null, PATTERN));
    }

    @Test
    public void testWithNullPatternFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetFormatRequest.with(VALUE, null));
    }

    @Test
    public void testWith() {
        final SpreadsheetFormatRequest request = SpreadsheetFormatRequest.with(VALUE, PATTERN);
        this.checkEquals(VALUE, request.value(), "value");
        this.checkEquals(PATTERN, request.pattern(), "PATTERN");
    }

    // Json..............................................................................................................

    @Test
    public void testJsonRoundtripLocalDateSpreadsheetDateFormatPattern() {
        this.marshallRoundTripTwiceAndCheck(this.createObject());
    }

    @Test
    public void testJsonRoundtripLocalDateTimeSpreadsheetDateTimeFormatPattern() {
        this.marshallRoundTripTwiceAndCheck(SpreadsheetFormatRequest.with(
                LocalDateTime.now(),
                SpreadsheetFormatPattern.parseDateTimeFormatPattern("yyyy-mm-dd hh-mm")));
    }

    @Test
    public void testJsonRoundtripLocalTimeSpreadsheetTimeFormatPattern() {
        this.marshallRoundTripTwiceAndCheck(SpreadsheetFormatRequest.with(
                LocalTime.now(),
                SpreadsheetFormatPattern.parseTimeFormatPattern("hh-mm:ss")));
    }

    // Object............................................................................................................

    @Test
    public void testDifferentValue() {
        this.checkNotEquals(SpreadsheetFormatRequest.with(LocalDate.now(), PATTERN));
    }

    @Test
    public void testDifferentPattern() {
        this.checkNotEquals(SpreadsheetFormatRequest.with(VALUE, SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/mmm/dd")));
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createObject(),
                "1999-12-31 \"dd/mm/yyyy\""
        );
    }

    @Override
    public SpreadsheetFormatRequest createObject() {
        return SpreadsheetFormatRequest.with(VALUE, PATTERN);
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetFormatRequest> type() {
        return SpreadsheetFormatRequest.class;
    }

    // json.............................................................................................................

    @Override
    public SpreadsheetFormatRequest unmarshall(final JsonNode node,
                                               final JsonNodeUnmarshallContext context) {
        return SpreadsheetFormatRequest.unmarshall(node, context);
    }
}
