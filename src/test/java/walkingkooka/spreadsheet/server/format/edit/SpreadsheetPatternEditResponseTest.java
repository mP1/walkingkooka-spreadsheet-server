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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY TOKENS, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.spreadsheet.server.format.edit;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetPatternEditResponseTest extends SpreadsheetPatternEditorTestCase2<SpreadsheetPatternEditResponse>
        implements TreePrintableTesting {

    private final static List<SpreadsheetPatternEditToken> TOKENS = Lists.of(
            SpreadsheetPatternEditToken.with(
                    SpreadsheetPatternEditTokenKind.DAY_NAME_FULL,
                    "ddddd"
            ),
            SpreadsheetPatternEditToken.with(
                    SpreadsheetPatternEditTokenKind.MONTH_NAME_FULL,
                    "mmmmm"
            )
    );

    private final static List<String> VALUES = Lists.of(
            "Value1a",
            "Value2b"
    );

    @Test
    public void testWithNullTokensFails() {
        this.withFails(
                null,
                VALUES
        );
    }

    @Test
    public void testWithNullValuesFails() {
        this.withFails(
                TOKENS,
                null
        );
    }

    private void withFails(final List<SpreadsheetPatternEditToken> tokens,
                           final List<String> values) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetPatternEditResponse.with(
                        tokens,
                        values
                )
        );
    }

    @Test
    public void testWithEmptyTokens() {
        this.withAndCheck(
                Lists.empty(),
                VALUES
        );
    }

    @Test
    public void testWithEmptyValues() {
        this.withAndCheck(
                TOKENS,
                Lists.empty()
        );
    }

    private void withAndCheck(final List<SpreadsheetPatternEditToken> tokens,
                              final List<String> values) {
        final SpreadsheetPatternEditResponse request = SpreadsheetPatternEditResponse.with(
                tokens,
                values
        );

        this.checkEquals(tokens, request.tokens(), "tokens");
        this.checkEquals(values, request.values(), "values");
    }

    // treePrint........................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
                this.createObject(),
                "tokens\n" +
                        "  DAY_NAME_FULL\n" +
                        "    \"ddddd\"\n" +
                        "  MONTH_NAME_FULL\n" +
                        "    \"mmmmm\"\n" +
                        "values\n" +
                        "    \"Value1a\"\n" +
                        "    \"Value2b\"\n"
        );
    }

    @Test
    public void testTreePrintEmptyValues() {
        this.treePrintAndCheck(
                SpreadsheetPatternEditResponse.with(
                        TOKENS,
                        Lists.empty()
                ),
                "tokens\n" +
                        "  DAY_NAME_FULL\n" +
                        "    \"ddddd\"\n" +
                        "  MONTH_NAME_FULL\n" +
                        "    \"mmmmm\"\n"
        );
    }

    // json.............................................................................................................

    @Test
    public void testJsonRoundtripEmptyValues() {
        this.marshallRoundTripTwiceAndCheck(
                SpreadsheetPatternEditResponse.with(
                        TOKENS,
                        Lists.empty()
                )
        );
    }

    // Object..........................................................................................................

    @Test
    public void testDifferentTokens() {
        this.checkNotEquals(
                SpreadsheetPatternEditResponse.with(
                        Lists.of(
                                SpreadsheetPatternEditToken.with(
                                        SpreadsheetPatternEditTokenKind.DIGIT_ZERO,
                                        "0"
                                )
                        ),
                        VALUES
                )
        );
    }

    @Test
    public void testDifferentValues() {
        this.checkNotEquals(
                SpreadsheetPatternEditResponse.with(
                        TOKENS,
                        Lists.of(
                                "Different-value-1a"
                        )
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createObject(),
                "DAY_NAME_FULL \"ddddd\", MONTH_NAME_FULL \"mmmmm\" \"Value1a\", \"Value2b\""
        );
    }

    @Override
    public SpreadsheetPatternEditResponse createObject() {
        return SpreadsheetPatternEditResponse.with(
                TOKENS,
                VALUES
        );
    }

    @Override
    public SpreadsheetPatternEditResponse unmarshall(final JsonNode json,
                                                     final JsonNodeUnmarshallContext context) {
        return SpreadsheetPatternEditResponse.unmarshall(
                json,
                context
        );
    }

    @Override
    public Class<SpreadsheetPatternEditResponse> type() {
        return SpreadsheetPatternEditResponse.class;
    }
}
