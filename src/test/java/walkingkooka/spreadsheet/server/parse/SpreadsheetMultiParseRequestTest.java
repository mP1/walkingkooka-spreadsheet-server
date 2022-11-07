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

package walkingkooka.spreadsheet.server.parse;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertThrows;


public final class SpreadsheetMultiParseRequestTest extends SpreadsheetParserTestCase2<SpreadsheetMultiParseRequest> {

    @Test
    public void testWithNullRequests() {
        assertThrows(NullPointerException.class, () -> SpreadsheetMultiParseRequest.with(null));
    }

    // json.............................................................................................................

    @Test
    public void testJsonRoundtrip() {
        this.marshallRoundTripTwiceAndCheck(this.createObject());
    }

    @Test
    public void testJsonRoundtrip2() {
        this.marshallRoundTripTwiceAndCheck(
                SpreadsheetMultiParseRequest.with(
                        Lists.of(
                                SpreadsheetParseRequest.with("dd:mm:yyyy", SpreadsheetMultiParser.SPREADSHEET_DATE_FORMATTER),
                                SpreadsheetParseRequest.with("dd:mm:yyyy;dd:mm:yyyy", SpreadsheetMultiParser.SPREADSHEET_DATE_PARSER),
                                SpreadsheetParseRequest.with("dd:mm:yyyy hh:mm", SpreadsheetMultiParser.SPREADSHEET_DATE_TIME_FORMATTER),
                                SpreadsheetParseRequest.with("dd:mm:yyyy hh:mm;dd:mm:yyyy hh:mm", SpreadsheetMultiParser.SPREADSHEET_DATE_TIME_PARSER),
                                SpreadsheetParseRequest.with("@@", SpreadsheetMultiParser.SPREADSHEET_TEXT_FORMATTER),
                                SpreadsheetParseRequest.with("hh:mm;hh:mm", SpreadsheetMultiParser.SPREADSHEET_TIME_FORMATTER),
                                SpreadsheetParseRequest.with("hh:mm", SpreadsheetMultiParser.SPREADSHEET_TIME_PARSER)
                        )
                )
        );
    }

    // Object...........................................................................................................

    @Test
    public void testDifferentRequests() {
        this.checkNotEquals(SpreadsheetMultiParseRequest.with(
                Lists.of(
                        SpreadsheetParseRequest.with("different-text", SpreadsheetMultiParser.SPREADSHEET_DATE_TIME_PARSER))
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "[\"yyyy-mm-ddd\" \"" + SpreadsheetMultiParser.SPREADSHEET_DATE_PARSER + "\"]");
    }

    @Override
    public SpreadsheetMultiParseRequest createObject() {
        return SpreadsheetMultiParseRequest.with(Lists.of(this.request()));
    }

    private SpreadsheetParseRequest request() {
        return SpreadsheetParseRequest.with(
                "yyyy-mm-ddd",
                SpreadsheetMultiParser.SPREADSHEET_DATE_PARSER
        );
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetMultiParseRequest> type() {
        return SpreadsheetMultiParseRequest.class;
    }

    // Json.............................................................................................................

    @Override
    public SpreadsheetMultiParseRequest unmarshall(final JsonNode node,
                                                   final JsonNodeUnmarshallContext context) {
        return SpreadsheetMultiParseRequest.unmarshall(node, context);
    }
}
