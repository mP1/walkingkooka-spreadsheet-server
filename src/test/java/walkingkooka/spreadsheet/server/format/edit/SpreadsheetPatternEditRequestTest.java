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

package walkingkooka.spreadsheet.server.format.edit;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPatternKind;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetPatternEditRequestTest extends SpreadsheetPatternEditorTestCase2<SpreadsheetPatternEditRequest>
        implements TreePrintableTesting {

    private final static SpreadsheetPatternKind KIND = SpreadsheetPatternKind.DATE_PARSE_PATTERNS;
    private final static String PATTERN = "pattern-123";
    private final static List<String> VALUES = Lists.of(
            "Value1a",
            "Value2b"
    );

    @Test
    public void testWithNullKindFails() {
        this.withFails(
                null,
                PATTERN,
                VALUES
        );
    }

    @Test
    public void testWithNullPatternFails() {
        this.withFails(
                KIND,
                null,
                VALUES
        );
    }

    @Test
    public void testWithNullValuesFails() {
        this.withFails(
                KIND,
                PATTERN,
                null
        );
    }

    private void withFails(final SpreadsheetPatternKind kind,
                           final String pattern,
                           final List<String> values) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetPatternEditRequest.with(
                        kind,
                        pattern,
                        values
                )
        );
    }

    @Test
    public void testWithEmptyPattern() {
        this.withAndCheck(
                KIND,
                "",
                VALUES
        );
    }

    @Test
    public void testWithEmptyValues() {
        this.withAndCheck(
                KIND,
                PATTERN,
                Lists.empty()
        );
    }

    private void withAndCheck(final SpreadsheetPatternKind kind,
                              final String pattern,
                              final List<String> values) {
        final SpreadsheetPatternEditRequest request = SpreadsheetPatternEditRequest.with(
                kind,
                pattern,
                values
        );

        this.checkEquals(kind, request.kind(), "kind");
        this.checkEquals(pattern, request.pattern(), "pattern");
        this.checkEquals(values, request.values(), "values");
    }

    // treePrint........................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
                this.createObject(),
                "spreadsheet-date-parse-patterns\n" +
                        "  \"pattern-123\"\n" +
                        "    \"Value1a\"\n" +
                        "    \"Value2b\"\n"
        );
    }

    @Test
    public void testTreePrintEmptyPattern() {
        this.treePrintAndCheck(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        "",
                        VALUES
                ),
                "spreadsheet-date-parse-patterns\n" +
                        "  \"\"\n" +
                        "    \"Value1a\"\n" +
                        "    \"Value2b\"\n"
        );
    }

    @Test
    public void testTreePrintEmptyValues() {
        this.treePrintAndCheck(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        PATTERN,
                        Lists.empty()
                ),
                "spreadsheet-date-parse-patterns\n" +
                        "  \"pattern-123\"\n"
        );
    }

    // json.............................................................................................................

    @Test
    public void testJsonRoundtripEmptyPattern() {
        this.marshallRoundTripTwiceAndCheck(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        "",
                        VALUES
                )
        );
    }

    @Test
    public void testJsonRoundtripEmptyValues() {
        this.marshallRoundTripTwiceAndCheck(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        PATTERN,
                        Lists.empty()
                )
        );
    }

    // Object..........................................................................................................

    @Test
    public void testDifferentKind() {
        this.checkNotEquals(
                SpreadsheetPatternEditRequest.with(
                        SpreadsheetPatternKind.DATE_FORMAT_PATTERN,
                        PATTERN,
                        VALUES
                )
        );
    }

    @Test
    public void testDifferentPattern() {
        this.checkNotEquals(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        "different-" + PATTERN,
                        VALUES
                )
        );
    }

    @Test
    public void testDifferentValues() {
        this.checkNotEquals(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        PATTERN,
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
                "spreadsheet-date-parse-patterns \"pattern-123\" \"Value1a\", \"Value2b\""
        );
    }

    @Override
    public SpreadsheetPatternEditRequest createObject() {
        return SpreadsheetPatternEditRequest.with(
                KIND,
                PATTERN,
                VALUES
        );
    }

    @Override
    public SpreadsheetPatternEditRequest unmarshall(final JsonNode json,
                                                    final JsonNodeUnmarshallContext context) {
        return SpreadsheetPatternEditRequest.unmarshall(
                json,
                context
        );
    }

    @Override
    public Class<SpreadsheetPatternEditRequest> type() {
        return SpreadsheetPatternEditRequest.class;
    }
}
