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
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertThrows;


public final class MultiParseRequestTest extends ParserTestCase2<MultiParseRequest> implements JsonNodeMarshallingTesting<MultiParseRequest> {

    @Test
    public void testWithNullRequests() {
        assertThrows(NullPointerException.class, () -> MultiParseRequest.with(null));
    }

    // json.............................................................................................................

    @Test
    public void testJsonRoundtrip() {
        this.marshallRoundTripTwiceAndCheck(this.createObject());
    }

    @Test
    public void testJsonRoundtrip2() {
        this.marshallRoundTripTwiceAndCheck(
                MultiParseRequest.with(
                        Lists.of(
                                ParseRequest.with("dd:mm:yyyy", MultiParser.SPREADSHEET_DATE_FORMATTER),
                                ParseRequest.with("dd:mm:yyyy;dd:mm:yyyy", MultiParser.SPREADSHEET_DATE_PARSERS),
                                ParseRequest.with("dd:mm:yyyy hh:mm", MultiParser.SPREADSHEET_DATE_TIME_FORMATTER),
                                ParseRequest.with("dd:mm:yyyy hh:mm;dd:mm:yyyy hh:mm", MultiParser.SPREADSHEET_DATE_TIME_PARSERS),
                                ParseRequest.with("@@", MultiParser.SPREADSHEET_TEXT_FORMATTER),
                                ParseRequest.with("hh:mm;hh:mm", MultiParser.SPREADSHEET_TIME_FORMATTER),
                                ParseRequest.with("hh:mm", MultiParser.SPREADSHEET_TIME_PARSERS)
                        )
                )
        );
    }

    // Object...........................................................................................................

    @Test
    public void testDifferentRequests() {
        this.checkNotEquals(MultiParseRequest.with(
                Lists.of(
                        ParseRequest.with("different-text", MultiParser.SPREADSHEET_DATE_TIME_PARSERS))
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "[\"yyyy-mm-ddd\" \"" + MultiParser.SPREADSHEET_DATE_PARSERS + "\"]");
    }

    @Override
    public MultiParseRequest createObject() {
        return MultiParseRequest.with(Lists.of(this.request()));
    }

    private ParseRequest request() {
        return ParseRequest.with(
                "yyyy-mm-ddd",
                MultiParser.SPREADSHEET_DATE_PARSERS
        );
    }

    @Override
    public Class<MultiParseRequest> type() {
        return MultiParseRequest.class;
    }

    // Json.............................................................................................................

    @Override
    public final MultiParseRequest createJsonNodeMappingValue() {
        return this.createObject();
    }

    @Override
    public MultiParseRequest unmarshall(final JsonNode node,
                                        final JsonNodeUnmarshallContext context) {
        return MultiParseRequest.unmarshall(node, context);
    }
}
