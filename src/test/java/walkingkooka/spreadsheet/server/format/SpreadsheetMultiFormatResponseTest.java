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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;


public final class SpreadsheetMultiFormatResponseTest extends SpreadsheetFormatterTestCase2<SpreadsheetMultiFormatResponse> {

    @Test
    public void testWithNullRequests() {
        assertThrows(NullPointerException.class, () -> SpreadsheetMultiFormatResponse.with(null));
    }

    // Json.............................................................................................................

    @Test
    public void testJsonRoundtripMixed() {
        this.marshallRoundTripTwiceAndCheck(
                SpreadsheetMultiFormatResponse.with(
                        Lists.of(
                                "formatted-text-123",
                                BigDecimal.valueOf(1.5),
                                LocalDate.now(),
                                LocalDateTime.now(),
                                LocalTime.now()
                        )
                )
        );
    }

    // Object...........................................................................................................

    @Test
    public void testDifferentRequests() {
        this.checkNotEquals(SpreadsheetMultiFormatResponse.with(
                Lists.of(
                        "different"
                )
        ));
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "[hello]");
    }

    @Override
    public SpreadsheetMultiFormatResponse createObject() {
        return SpreadsheetMultiFormatResponse.with(Lists.of("hello"));
    }

    @Override
    public SpreadsheetMultiFormatResponse unmarshall(final JsonNode from,
                                                     final JsonNodeUnmarshallContext context) {
        return SpreadsheetMultiFormatResponse.unmarshall(from, context);
    }

    // ClassTesting......................................................................................................

    @Override
    public Class<SpreadsheetMultiFormatResponse> type() {
        return SpreadsheetMultiFormatResponse.class;
    }
}
