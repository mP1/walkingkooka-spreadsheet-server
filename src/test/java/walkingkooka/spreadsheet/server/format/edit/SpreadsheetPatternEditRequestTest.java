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

    private final static SpreadsheetPatternKind KIND = SpreadsheetPatternKind.DATE_PARSE_PATTERN;
    private final static String PATTERN = "pattern-123";
    private final static List<String> SAMPLES = Lists.of(
            "Sample1a",
            "Sample2b"
    );

    @Test
    public void testWithNullKindFails() {
        this.withFails(
                null,
                PATTERN,
                SAMPLES
        );
    }

    @Test
    public void testWithNullPatternFails() {
        this.withFails(
                KIND,
                null,
                SAMPLES
        );
    }

    @Test
    public void testWithNullSamplesFails() {
        this.withFails(
                KIND,
                PATTERN,
                null
        );
    }

    private void withFails(final SpreadsheetPatternKind kind,
                           final String pattern,
                           final List<String> samples) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetPatternEditRequest.with(
                        kind,
                        pattern,
                        samples
                )
        );
    }

    @Test
    public void testWithEmptyPattern() {
        this.withAndCheck(
                KIND,
                "",
                SAMPLES
        );
    }

    @Test
    public void testWithEmptySamples() {
        this.withAndCheck(
                KIND,
                PATTERN,
                Lists.empty()
        );
    }

    private void withAndCheck(final SpreadsheetPatternKind kind,
                              final String pattern,
                              final List<String> samples) {
        final SpreadsheetPatternEditRequest request = SpreadsheetPatternEditRequest.with(
                kind,
                pattern,
                samples
        );

        this.checkEquals(kind, request.kind(), "kind");
        this.checkEquals(pattern, request.pattern(), "pattern");
        this.checkEquals(samples, request.samples(), "samples");
    }

    // treePrint........................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
                this.createObject(),
                "spreadsheet-date-parse-pattern\n" +
                        "  \"pattern-123\"\n" +
                        "    \"Sample1a\"\n" +
                        "    \"Sample2b\"\n"
        );
    }

    @Test
    public void testTreePrintEmptyPattern() {
        this.treePrintAndCheck(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        "",
                        SAMPLES
                ),
                "spreadsheet-date-parse-pattern\n" +
                        "  \"\"\n" +
                        "    \"Sample1a\"\n" +
                        "    \"Sample2b\"\n"
        );
    }

    @Test
    public void testTreePrintEmptySamples() {
        this.treePrintAndCheck(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        PATTERN,
                        Lists.empty()
                ),
                "spreadsheet-date-parse-pattern\n" +
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
                        SAMPLES
                )
        );
    }

    @Test
    public void testJsonRoundtripEmptySamples() {
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
                        SAMPLES
                )
        );
    }

    @Test
    public void testDifferentPattern() {
        this.checkNotEquals(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        "different-" + PATTERN,
                        SAMPLES
                )
        );
    }

    @Test
    public void testDifferentSamples() {
        this.checkNotEquals(
                SpreadsheetPatternEditRequest.with(
                        KIND,
                        PATTERN,
                        Lists.of(
                                "Different-sample-1a"
                        )
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createObject(),
                "spreadsheet-date-parse-pattern \"pattern-123\" \"Sample1a\", \"Sample2b\""
        );
    }

    @Override
    public SpreadsheetPatternEditRequest createObject() {
        return SpreadsheetPatternEditRequest.with(
                KIND,
                PATTERN,
                SAMPLES
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
