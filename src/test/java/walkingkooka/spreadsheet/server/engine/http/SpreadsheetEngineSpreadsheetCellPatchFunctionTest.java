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

package walkingkooka.spreadsheet.server.engine.http;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.util.FunctionTesting;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineSpreadsheetCellPatchFunctionTest implements ClassTesting<SpreadsheetEngineSpreadsheetCellPatchFunction>,
        FunctionTesting<SpreadsheetEngineSpreadsheetCellPatchFunction, JsonNode, JsonNode>,
        ToStringTesting<SpreadsheetEngineSpreadsheetCellPatchFunction> {

    private final static SpreadsheetCellReference REFERENCE = SpreadsheetExpressionReference.parseCell("B2");
    private final static SpreadsheetEngine ENGINE = SpreadsheetEngines.fake();
    private final static SpreadsheetEngineContext CONTEXT = new FakeSpreadsheetEngineContext() {
        @Override
        public SpreadsheetMetadata metadata() {
            return SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                    .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                    .loadFromLocale();
        }
    };

    @Test
    public void testWithNullSpreadsheetCellReferenceFails() {
        assertThrows(
                NullPointerException.class, () ->
                        SpreadsheetEngineSpreadsheetCellPatchFunction.with(
                                null,
                                ENGINE,
                                CONTEXT
                        )
        );
    }

    @Test
    public void testWithNullEngineFails() {
        assertThrows(
                NullPointerException.class, () ->
                        SpreadsheetEngineSpreadsheetCellPatchFunction.with(
                                REFERENCE,
                                null,
                                CONTEXT
                        )
        );
    }

    @Test
    public void testWithNullContextFails() {
        assertThrows(
                NullPointerException.class, () ->
                        SpreadsheetEngineSpreadsheetCellPatchFunction.with(
                                REFERENCE,
                                ENGINE,
                                null
                        )
        );
    }

    @Test
    public void testApply() {
        final SpreadsheetCell cell = SpreadsheetCell.with(
                REFERENCE,
                SpreadsheetFormula.EMPTY
                        .setText("=2")
        );
        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(
                                cell
                        )
                );
        this.applyAndCheck(
                SpreadsheetEngineSpreadsheetCellPatchFunction.with(
                        REFERENCE,
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadCell(final SpreadsheetCellReference cell,
                                                             final SpreadsheetEngineEvaluation evaluation,
                                                             final SpreadsheetEngineContext context) {
                                assertSame(cell, REFERENCE, "reference");
                                assertSame(CONTEXT, context, "context");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(
                                                        SpreadsheetCell.with(
                                                                REFERENCE,
                                                                SpreadsheetFormula.EMPTY
                                                                        .setText("=1")
                                                        )
                                                )
                                        );
                            }

                            @Override
                            public SpreadsheetDelta saveCell(final SpreadsheetCell c,
                                                             final SpreadsheetEngineContext context) {
                                assertEquals(cell, c, "cell");
                                assertSame(CONTEXT, context, "context");
                                return response;
                            }
                        },
                        CONTEXT
                ),
                JsonNode.parse(
                        "{\n" +
                                "  \"formula\": {\n" +
                                "    \"text\": \"=2\"\n" +
                                "  }\n" +
                                "}"
                ),
                JsonNodeMarshallContexts.basic().marshall(
                        response
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createFunction(), ENGINE + " " + CONTEXT);
    }

    @Override
    public SpreadsheetEngineSpreadsheetCellPatchFunction createFunction() {
        return SpreadsheetEngineSpreadsheetCellPatchFunction.with(
                REFERENCE,
                ENGINE,
                CONTEXT
        );
    }

    @Override
    public Class<SpreadsheetEngineSpreadsheetCellPatchFunction> type() {
        return SpreadsheetEngineSpreadsheetCellPatchFunction.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
