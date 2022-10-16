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

package walkingkooka.spreadsheet.server.format.editor;

import org.junit.jupiter.api.Test;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetPatternEditorTokenTest extends SpreadsheetPatternEditorTestCase2<SpreadsheetPatternEditorToken>
        implements TreePrintableTesting {

    private final static SpreadsheetPatternEditorTokenKind KIND = SpreadsheetPatternEditorTokenKind.CONDITION;
    private final static String PATTERN = "pattern-123";

    @Test
    public void testWithNullKindFails() {
        this.withFails(
                null,
                PATTERN
        );
    }

    @Test
    public void testWithNullPatternFails() {
        this.withFails(
                KIND,
                null
        );
    }

    private void withFails(final SpreadsheetPatternEditorTokenKind kind,
                           final String pattern) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetPatternEditorToken.with(
                        kind,
                        pattern
                )
        );
    }

    @Test
    public void testWithEmptyPatternFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetPatternEditorToken.with(
                        KIND,
                        ""
                )
        );
    }

    @Test
    public void testWith() {
        this.withAndCheck(
                KIND,
                PATTERN
        );
    }

    private void withAndCheck(final SpreadsheetPatternEditorTokenKind kind,
                              final String pattern) {
        final SpreadsheetPatternEditorToken request = SpreadsheetPatternEditorToken.with(
                kind,
                pattern
        );

        this.checkEquals(kind, request.kind(), "kind");
        this.checkEquals(pattern, request.pattern(), "pattern");
    }

    // treePrint........................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
                this.createObject(),
                "CONDITION\n" +
                        "  \"pattern-123\"\n"
        );
    }

    // Object..........................................................................................................

    @Test
    public void testDifferentKind() {
        this.checkNotEquals(
                SpreadsheetPatternEditorToken.with(
                        SpreadsheetPatternEditorTokenKind.AMPM_FULL,
                        PATTERN
                )
        );
    }

    @Test
    public void testDifferentPattern() {
        this.checkNotEquals(
                SpreadsheetPatternEditorToken.with(
                        KIND,
                        "different-" + PATTERN
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createObject(),
                "CONDITION \"pattern-123\""
        );
    }

    @Override
    public SpreadsheetPatternEditorToken createObject() {
        return SpreadsheetPatternEditorToken.with(
                KIND,
                PATTERN
        );
    }

    @Override
    public SpreadsheetPatternEditorToken unmarshall(final JsonNode json,
                                                    final JsonNodeUnmarshallContext context) {
        return SpreadsheetPatternEditorToken.unmarshall(
                json,
                context
        );
    }

    @Override
    public Class<SpreadsheetPatternEditorToken> type() {
        return SpreadsheetPatternEditorToken.class;
    }
}
