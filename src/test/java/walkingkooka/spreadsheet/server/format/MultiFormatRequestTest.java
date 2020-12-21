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
import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;


public final class MultiFormatRequestTest extends FormatterTestCase2<MultiFormatRequest> implements JsonNodeMarshallingTesting<MultiFormatRequest> {

    @Test
    public void testWithNullRequests() {
        assertThrows(NullPointerException.class, () -> MultiFormatRequest.with(null));
    }

    // Json..............................................................................................................

    @Test
    public void testJsonRoundtrip2() {
        this.marshallRoundTripTwiceAndCheck(
                MultiFormatRequest.with(
                        Lists.of(
                                FormatRequest.with(LocalDate.of(2000, 1, 2),
                                        SpreadsheetFormatPattern.parseDateFormatPattern("dd/mm/yyyy")),
                                FormatRequest.with(LocalDateTime.of(2000, 1, 2, 12, 58, 59),
                                        SpreadsheetFormatPattern.parseDateTimeFormatPattern("dd/mm/yyyy hh:mm")),
                                FormatRequest.with(LocalTime.of(12, 58, 59),
                                        SpreadsheetFormatPattern.parseTimeFormatPattern("hh:mm"))
                        )
                )
        );
    }

    // Object...........................................................................................................

    @Test
    public void testDifferentRequests() {
        this.checkNotEquals(MultiFormatRequest.with(
                Lists.of(
                        FormatRequest.with(LocalDate.of(2000, 1, 2),
                                SpreadsheetFormatPattern.parseDateFormatPattern("dd/mm/yyyy")))
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "[1999-12-31 yyyy/mm/dd]");
    }

    @Override
    public MultiFormatRequest createObject() {
        return MultiFormatRequest.with(Lists.of(this.request()));
    }

    private FormatRequest request() {
        return FormatRequest.with(
                LocalDate.of(1999, 12, 31),
                SpreadsheetFormatPattern.parseDateFormatPattern("yyyy/mm/dd")
        );
    }

    @Override
    public Class<MultiFormatRequest> type() {
        return MultiFormatRequest.class;
    }

    // Json.............................................................................................................

    @Override
    public MultiFormatRequest unmarshall(final JsonNode node,
                                         final JsonNodeUnmarshallContext context) {
        return MultiFormatRequest.unmarshall(node, context);
    }
}
