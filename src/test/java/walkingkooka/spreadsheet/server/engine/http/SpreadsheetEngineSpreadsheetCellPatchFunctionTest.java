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
import walkingkooka.color.Color;
import walkingkooka.net.RelativeUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpRequest;
import walkingkooka.net.http.server.HttpRequests;
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
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.reference.SpreadsheetViewportSelection;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.text.TextStyle;
import walkingkooka.tree.text.TextStylePropertyName;
import walkingkooka.util.FunctionTesting;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineSpreadsheetCellPatchFunctionTest implements ClassTesting<SpreadsheetEngineSpreadsheetCellPatchFunction>,
        FunctionTesting<SpreadsheetEngineSpreadsheetCellPatchFunction, JsonNode, JsonNode>,
        ToStringTesting<SpreadsheetEngineSpreadsheetCellPatchFunction> {

    private final static HttpRequest REQUEST = HttpRequests.fake();
    private final static SpreadsheetCellReference REFERENCE = SpreadsheetExpressionReference.parseCell("B2");
    private final static SpreadsheetEngine ENGINE = SpreadsheetEngines.fake();
    private final static SpreadsheetEngineContext CONTEXT = new FakeSpreadsheetEngineContext() {
        @Override
        public SpreadsheetMetadata metadata() {
            return SpreadsheetMetadata.NON_LOCALE_DEFAULTS
                    .set(SpreadsheetMetadataPropertyName.LOCALE, Locale.forLanguageTag("EN-AU"))
                    .loadFromLocale();
        }

        @Override
        public SpreadsheetStoreRepository storeRepository() {
            return new FakeSpreadsheetStoreRepository() {
                @Override
                public SpreadsheetLabelStore labels() {
                    return SpreadsheetLabelStores.fake();
                }
            };
        }
    };

    @Test
    public void testWithNullRequestFails() {
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
                                REQUEST,
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
                                REQUEST,
                                ENGINE,
                                null
                        )
        );
    }

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

    @Test
    private void applyAndCheck2(final String queryString,
                                final Optional<SpreadsheetViewportSelection> viewportSelection) {
        final SpreadsheetCell cell = SpreadsheetCell.with(
                REFERENCE,
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
                SpreadsheetEngineSpreadsheetCellPatchFunction.with(
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
                        },
                        CONTEXT
                ),
                marshall(request),
                marshall(response)
        );
    }

    private JsonNode marshall(final Object object) {
        return JsonNodeMarshallContexts.basic()
                .marshall(object);
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createFunction(), REQUEST + " " + ENGINE + " " + CONTEXT);
    }

    @Override
    public SpreadsheetEngineSpreadsheetCellPatchFunction createFunction() {
        return SpreadsheetEngineSpreadsheetCellPatchFunction.with(
                REQUEST,
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
