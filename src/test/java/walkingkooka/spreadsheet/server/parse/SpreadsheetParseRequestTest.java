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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetParseRequestTest extends SpreadsheetParserTestCase2<SpreadsheetParseRequest> {

    private final static String TEXT = "yyyy/mm/ddd";
    private final static String PARSER = SpreadsheetMultiParser.SPREADSHEET_DATE_PARSER;


    @Test
    public void testWithNullTextFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetParseRequest.with(null, PARSER));
    }

    @Test
    public void testWithNullParserFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetParseRequest.with(TEXT, null));
    }

    @Test
    public void testWithEmptyParserFails() {
        assertThrows(IllegalArgumentException.class, () -> SpreadsheetParseRequest.with(TEXT, ""));
    }

    @Test
    public void testWith() {
        final SpreadsheetParseRequest request = SpreadsheetParseRequest.with(TEXT, PARSER);
        this.checkEquals(TEXT, request.text(), "text");
        this.checkEquals(PARSER, request.parser(), "PARSER");
    }

    // Object............................................................................................................

    @Test
    public void testDifferentText() {
        this.checkNotEquals(SpreadsheetParseRequest.with("2000/1/2", PARSER));
    }

    @Test
    public void testDifferentParser() {
        this.checkNotEquals(SpreadsheetParseRequest.with(TEXT, "different"));
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "\"" + TEXT + "\" \"" + PARSER + "\"");
    }

    // Json..............................................................................................................

    @Test
    public void testJsonRoundtrip() {
        this.marshallRoundTripTwiceAndCheck(this.createObject());
    }

    @Test
    public void testJsonRoundtrip2() {
        this.marshallRoundTripTwiceAndCheck(SpreadsheetParseRequest.with("hh:mm:ss", SpreadsheetMultiParser.SPREADSHEET_TIME_PARSER));
    }

    @Override
    public SpreadsheetParseRequest createObject() {
        return SpreadsheetParseRequest.with(TEXT, PARSER);
    }

    @Override
    public Class<SpreadsheetParseRequest> type() {
        return SpreadsheetParseRequest.class;
    }

    @Override
    public SpreadsheetParseRequest unmarshall(final JsonNode node,
                                              final JsonNodeUnmarshallContext context) {
        return SpreadsheetParseRequest.unmarshall(node, context);
    }
}
