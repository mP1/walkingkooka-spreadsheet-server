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
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.spreadsheet.SpreadsheetColumn;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.reference.SpreadsheetColumnReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;

public final class SpreadsheetEnginePatchSpreadsheetColumnFunctionTest extends SpreadsheetEnginePatchTestCase<SpreadsheetEnginePatchSpreadsheetColumnFunction, SpreadsheetColumnReference> {

    private final static SpreadsheetColumnReference REFERENCE = SpreadsheetExpressionReference.parseColumn("Z");

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
                                .setAnchor(SpreadsheetViewportSelection.NO_ANCHOR)
                )
        );
    }

    private void applyAndCheck2(final String queryString,
                                final Optional<SpreadsheetViewportSelection> viewportSelection) {
        final SpreadsheetColumn column = REFERENCE.column()
                .setHidden(true);

        final SpreadsheetDelta request = SpreadsheetDelta.EMPTY
                .setColumns(
                        Sets.of(column)
                );
        final SpreadsheetDelta response = SpreadsheetDelta.EMPTY
                .setColumns(
                        Sets.of(column)
                ).setSelection(viewportSelection);

        this.applyAndCheck(
                SpreadsheetEnginePatchSpreadsheetColumnFunction.with(
                        new FakeHttpRequest() {
                            @Override
                            public RelativeUrl url() {
                                return Url.parseRelative("/column/" + REFERENCE + queryString);
                            }
                        },
                        new FakeSpreadsheetEngine() {
                            @Override
                            public SpreadsheetDelta loadColumn(final SpreadsheetColumnReference columnReference,
                                                               final SpreadsheetEngineContext context) {
                                checkEquals(REFERENCE, columnReference, "reference");
                                assertSame(CONTEXT, context, "context");

                                return SpreadsheetDelta.EMPTY
                                        .setColumns(
                                                Sets.of(column)
                                        );
                            }

                            @Override
                            public SpreadsheetDelta saveColumn(final SpreadsheetColumn c,
                                                               final SpreadsheetEngineContext context) {
                                checkEquals(column, c, "column");
                                assertSame(CONTEXT, context, "context");

                                return response;
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
                "Patch column: " + REQUEST + " " + ENGINE + " " + CONTEXT
        );
    }

    @Override
    public SpreadsheetEnginePatchSpreadsheetColumnFunction createFunction(final HttpRequest request,
                                                                          final SpreadsheetEngine engine,
                                                                          final SpreadsheetEngineContext context) {
        return SpreadsheetEnginePatchSpreadsheetColumnFunction.with(
                request,
                engine,
                context
        );
    }

    @Override
    public Class<SpreadsheetEnginePatchSpreadsheetColumnFunction> type() {
        return SpreadsheetEnginePatchSpreadsheetColumnFunction.class;
    }
}
