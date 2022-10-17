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
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetPatternEditTokenTest extends SpreadsheetPatternEditorTestCase2<SpreadsheetPatternEditToken>
        implements TreePrintableTesting {

    private final static SpreadsheetPatternEditTokenKind KIND = SpreadsheetPatternEditTokenKind.CONDITION;
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

    private void withFails(final SpreadsheetPatternEditTokenKind kind,
                           final String pattern) {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetPatternEditToken.with(
                        kind,
                        pattern
                )
        );
    }

    @Test
    public void testWithEmptyPatternFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetPatternEditToken.with(
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

    private void withAndCheck(final SpreadsheetPatternEditTokenKind kind,
                              final String pattern) {
        final SpreadsheetPatternEditToken request = SpreadsheetPatternEditToken.with(
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
                SpreadsheetPatternEditToken.with(
                        SpreadsheetPatternEditTokenKind.AMPM_FULL,
                        PATTERN
                )
        );
    }

    @Test
    public void testDifferentPattern() {
        this.checkNotEquals(
                SpreadsheetPatternEditToken.with(
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
    public SpreadsheetPatternEditToken createObject() {
        return SpreadsheetPatternEditToken.with(
                KIND,
                PATTERN
        );
    }

    @Override
    public SpreadsheetPatternEditToken unmarshall(final JsonNode json,
                                                  final JsonNodeUnmarshallContext context) {
        return SpreadsheetPatternEditToken.unmarshall(
                json,
                context
        );
    }

    @Override
    public Class<SpreadsheetPatternEditToken> type() {
        return SpreadsheetPatternEditToken.class;
    }
}
