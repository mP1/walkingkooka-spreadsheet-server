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
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelectionAnchor;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

public final class SpreadsheetEnginePatchSpreadsheetCellFunctionTest extends SpreadsheetEnginePatchTestCase<SpreadsheetEnginePatchSpreadsheetCellFunction, SpreadsheetCellReference> {

    private final static SpreadsheetCellReference REFERENCE = SpreadsheetExpressionReference.parseCell("B2");

    @Test
    public void testApply() {
        this.applyAndCheck2("", Optional.empty());
    }

    @Test
    public void testApplySelectionQueryParameter() {
        this.applyAndCheck2(
                "?selectionType=cell&selection=Z99",
                Optional.of(
                        SpreadsheetSelection.parseCell("Z99")
                                .setAnchor(SpreadsheetViewportSelectionAnchor.NONE)
                )
        );
    }

    @Test
    private void applyAndCheck2(final String queryString,
                                final Optional<SpreadsheetViewportSelection> viewportSelection) {
        final SpreadsheetCell cell = REFERENCE.setFormula(
                SpreadsheetFormula.EMPTY
                        .setText("=2")
        );

        final TextStyle style = TextStyle.EMPTY
                .set(TextStylePropertyName.BACKGROUND_COLOR, Color.BLACK);

        final SpreadsheetDelta request = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(cell)
                );
        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(
                                cell.setStyle(style)
                        )
                ).setSelection(viewportSelection);

        this.applyAndCheck(
                SpreadsheetEnginePatchSpreadsheetCellFunction.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/cell/" + REFERENCE + queryString);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadCell(final SpreadsheetCellReference cellReference,
                                                             final SpreadsheetEngineEvaluation evaluation,
                                                             final Set<SpreadsheetDeltaProperties> deltaProperties,
                                                             final SpreadsheetEngineContext context) {
                                checkEquals(REFERENCE, cellReference, "reference");
                                assertSame(CONTEXT, context, "context");

                                return SpreadsheetDelta.EMPTY
                                        .setCells(
                                                Sets.of(
                                                        cell.setFormula(
                                                                SpreadsheetFormula.EMPTY
                                                                        .setText("=-1")
                                                        ).setStyle(style)
                                                )
                                        );
                            }

                            @Override
                            public SpreadsheetDelta saveCell(final SpreadsheetCell c,
                                                             final SpreadsheetEngineContext context) {
                                checkEquals(cell.setStyle(style), c, "cell");
                                assertSame(CONTEXT, context, "context");

                                return response;
                            }

                            @Override
                            public Optional<SpreadsheetViewportSelection> navigate(final SpreadsheetViewportSelection selection,
                                                                                   final SpreadsheetEngineContext context) {
                                return Optional.of(selection);
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
