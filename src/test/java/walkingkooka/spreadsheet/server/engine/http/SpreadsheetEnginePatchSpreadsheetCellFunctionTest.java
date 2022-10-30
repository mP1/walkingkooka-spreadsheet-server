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
import walkingkooka.collect.set.Sets;
import walkingkooka.color.Color;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.SpreadsheetCell;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetDeltaProperties;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineEvaluation;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetFormatPattern;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;
import walkingkooka.spreadsheet.reference.SpreadsheetCellRange;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.text.FontStyle;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

public final class SpreadsheetEnginePatchSpreadsheetCellFunctionTest extends SpreadsheetEnginePatchTestCase<SpreadsheetEnginePatchSpreadsheetCellFunction, SpreadsheetCellRange> {

    @Test
    public void testPatchCellReferenceWithCells() {
        final SpreadsheetCell patch = CELL_REFERENCE_B2.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL_REFERENCE_B2.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
                .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.applyAndCheck(
                CELL_REFERENCE_B2,
                "", // queryString
                this.marshall(
                        SpreadsheetDelta.EMPTY.setCells(
                                Sets.of(patch)
                        )
                ),
                Sets.of(loaded),
                Sets.of(saved),
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(saved)
                        )
        );
    }

    @Test
    public void testPatchCellReferenceWithCellsAndQueryStringViewportSelection() {
        final SpreadsheetCell patch = CELL_REFERENCE_B2.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL_REFERENCE_B2.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
                .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        this.applyAndCheck(
                CELL_REFERENCE_B2,
                "selectionType=cell&selection=Z99", // queryString
                this.marshall(
                        SpreadsheetDelta.EMPTY.setCells(
                                Sets.of(patch)
                        )
                ),
                Sets.of(loaded),
                Sets.of(saved),
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(saved)
                        ).setViewportSelection(
                                Optional.of(
                                        SpreadsheetSelection.parseCell("Z99")
                                                .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                                )
                        )
        );
    }

    @Test
    public void testPatchCellLabelWithCells() {
        final SpreadsheetCell patch = CELL_REFERENCE_B2.setFormula(SpreadsheetFormula.EMPTY.setText("='patched"));

        final TextStyle style = TextStyle.EMPTY.set(TextStylePropertyName.FONT_STYLE, FontStyle.ITALIC);
        final SpreadsheetCell loaded = CELL_REFERENCE_B2.setFormula(SpreadsheetFormula.EMPTY.setText("='before"))
                .setStyle(style);
        final SpreadsheetCell saved = patch.setStyle(style);

        final String patchString = this.marshall(
                        SpreadsheetDelta.EMPTY.setCells(
                                Sets.of(patch)
                        )
                ).toString()
                .replace(
                        CELL_REFERENCE_B2.toString(),
                        LABELB2.toString()
                );
        this.checkEquals(
                false,
                patchString.contains("" + '"' + CELL_REFERENCE_B2 + '"'),
                () -> "PATCH must not contain " + CELL_REFERENCE_B2 + "\n" + patchString
        );
        this.checkEquals(
                true,
                patchString.contains(LABELB2.toString()),
                () -> "PATCH must contain " + LABELB2 + "\n" + patchString
        );

        this.applyAndCheck(
                CELL_REFERENCE_B2,
                "", // queryString
                JsonNode.parse(patchString),
                Sets.of(loaded),
                Sets.of(saved),
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(saved)
                        )
        );
    }

    @Test
    public void testPatchCellRangeWithCells() {
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final TextStyle styleB2 = TextStyle.EMPTY.set(
                TextStylePropertyName.COLOR,
                Color.parse("#222222")
        );

        final SpreadsheetCellReference c3 = SpreadsheetSelection.parseCell("C3");
        final TextStyle styleC3 = TextStyle.EMPTY.set(
                TextStylePropertyName.COLOR,
                Color.parse("#333333")
        );

        final Set<SpreadsheetCell> loaded = Sets.of(
                b2.setFormula(
                        SpreadsheetFormula.EMPTY.setText("='before b2")
                ).setStyle(styleB2),
                c3.setFormula(
                        SpreadsheetFormula.EMPTY.setText("='before c3")
                ).setStyle(styleC3)
        );

        final SpreadsheetCell patchB2 = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("='patched-b2")
        );

        final SpreadsheetCell patchC3 = c3.setFormula(
                SpreadsheetFormula.EMPTY.setText("='patched-c3")
        );

        final Set<SpreadsheetCell> saved = Sets.of(
                patchB2.setStyle(styleB2),
                patchC3.setStyle(styleC3)
        );

        this.applyAndCheck(
                SpreadsheetSelection.parseCellRange("A1:D4"),
                "", // queryString
                this.marshall(
                        SpreadsheetDelta.EMPTY.setCells(
                                Sets.of(
                                        patchB2,
                                        patchC3
                                )
                        )
                ),
                loaded,
                saved,
                SpreadsheetDelta.EMPTY
                        .setCells(saved)
        );
    }

    @Test
    public void testPatchCellRangeWithFormatPattern() {
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final SpreadsheetCell loadedB2 = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("='before b2")
        ).setFormatPattern(
                Optional.of(
                        SpreadsheetPattern.parseTextFormatPattern("\"format-before-b2\"")
                )
        );

        final SpreadsheetCellReference b3 = SpreadsheetSelection.parseCell("B3");
        final SpreadsheetCell loadedB3 = b3.setFormula(
                SpreadsheetFormula.EMPTY.setText("='before b3")
        ).setFormatPattern(
                Optional.of(
                        SpreadsheetPattern.parseTextFormatPattern("\"format-before-b3\"")
                )
        );

        final Set<SpreadsheetCell> loaded = Sets.of(
                loadedB2,
                loadedB3
        );

        final SpreadsheetFormatPattern patchedFormat = SpreadsheetPattern.parseTextFormatPattern("\"#format-patched\"");

        final Set<SpreadsheetCell> saved = Sets.of(
                SpreadsheetSelection.parseCell("B1")
                        .setFormula(SpreadsheetFormula.EMPTY)
                        .setFormatPattern(
                                Optional.of(patchedFormat)
                        ),
                loadedB2.setFormatPattern(
                        Optional.of(patchedFormat)
                ),
                loadedB3.setFormatPattern(
                        Optional.of(patchedFormat)
                )
        );

        this.applyAndCheck(
                SpreadsheetSelection.parseCellRange("B1:B3"),
                "", // queryString
                JsonNode.object()
                        .set(
                                JsonPropertyName.with("format-pattern"),
                                this.marshallWithType(patchedFormat)
                        ),
                loaded,
                saved,
                SpreadsheetDelta.EMPTY
                        .setCells(saved)
        );
    }

    @Test
    public void testPatchCellRangeWithStyle() {
        final SpreadsheetCellReference b2 = SpreadsheetSelection.parseCell("B2");
        final TextStyle styleB2 = TextStyle.EMPTY.set(
                TextStylePropertyName.COLOR,
                Color.parse("#222222")
        );
        final SpreadsheetCell loadedB2 = b2.setFormula(
                SpreadsheetFormula.EMPTY.setText("='before2")
        ).setStyle(styleB2);

        final SpreadsheetCellReference b3 = SpreadsheetSelection.parseCell("B3");
        final TextStyle styleB3 = TextStyle.EMPTY.set(
                TextStylePropertyName.COLOR,
                Color.parse("#333333")
        );
        final SpreadsheetCell loadedB3 = b3.setFormula(
                SpreadsheetFormula.EMPTY.setText("='before3")
        ).setStyle(styleB3);

        final Set<SpreadsheetCell> loaded = Sets.of(
                loadedB2,
                loadedB3
        );

        final TextStyle patchedStyle = TextStyle.EMPTY
                .set(
                        TextStylePropertyName.FONT_STYLE,
                        FontStyle.ITALIC
                );

        final Set<SpreadsheetCell> saved = Sets.of(
                SpreadsheetSelection.parseCell("B1")
                        .setFormula(SpreadsheetFormula.EMPTY)
                        .setStyle(patchedStyle),
                loadedB2.setStyle(
                        styleB2.merge(patchedStyle)
                ),
                loadedB3.setStyle(
                        styleB3.merge(patchedStyle)
                )
        );

        this.applyAndCheck(
                SpreadsheetSelection.parseCellRange("B1:B3"),
                "", // queryString
                JsonNode.object()
                        .set(
                                JsonPropertyName.with("style"),
                                this.marshall(patchedStyle)
                        ),
                loaded,
                saved,
                SpreadsheetDelta.EMPTY
                        .setCells(saved)
        );
    }

    private void applyAndCheck(final SpreadsheetSelection selection,
                               final String queryString,
                               final JsonNode request,
                               final Set<SpreadsheetCell> loaded,
                               final Set<SpreadsheetCell> saved,
                               final SpreadsheetDelta response) {
        this.applyAndCheck(
                SpreadsheetEnginePatchSpreadsheetCellFunction.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/cell/" + selection + "?" + queryString);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadCells(final SpreadsheetSelection loadSelection,
                                                              final SpreadsheetEngineEvaluation evaluation,
                                                              final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(
                                        selection.toCellRangeOrFail(),
                                        loadSelection,
                                        "selection"
                                );
                                assertSame(CONTEXT, context, "context");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(loaded);
                            }

                            @Override
                            public SpreadsheetDelta saveCells(final Set<SpreadsheetCell> cells,
                                                              final SpreadsheetEngineContext context) {
                                checkEquals(
                                        saved,
                                        cells,
                                        "saved cells");
                                assertSame(CONTEXT, context, "context");

                                return response;
                            }

                            @Override
                            public Optional<SpreadsheetViewportSelection> navigate(final SpreadsheetViewportSelection viewportSelection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(viewportSelection);
                            }
                        },
                        CONTEXT
                ),
                marshall(request),
                marshall(response)
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createFunction(),
                "Patch cell: " + REQUEST + " " + ENGINE + " " + CONTEXT
        );
    }

    @Override
    public SpreadsheetEnginePatchSpreadsheetCellFunction createFunction(final HttpRequest request,
                                                                        final SpreadsheetEngine engine,
                                                                        final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchSpreadsheetCellFunction.with(
                request,
                engine,
                context
        );
    }

    @Override
    public Class<SpreadsheetEnginePatchSpreadsheetCellFunction> type() {
        return SpreadsheetEnginePatchSpreadsheetCellFunction.class;
    }
}
