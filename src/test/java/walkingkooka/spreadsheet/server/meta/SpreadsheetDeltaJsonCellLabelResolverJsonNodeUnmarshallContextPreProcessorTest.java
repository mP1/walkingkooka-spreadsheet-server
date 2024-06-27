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

package walkingkooka.spreadsheet.server.meta;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.spreadsheet.SpreadsheetFormula;
import walkingkooka.spreadsheet.engine.SpreadsheetDelta;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.util.BiFunctionTesting;

public final class SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessorTest implements BiFunctionTesting<SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessor,
        JsonNode,
        Class<?>,
        JsonNode> {

    private final static SpreadsheetLabelName LABEL1 = SpreadsheetExpressionReference.labelName("Label123");
    private final static SpreadsheetLabelName LABEL2 = SpreadsheetExpressionReference.labelName("Label456");

    private final static SpreadsheetCellReference CELL1 = SpreadsheetExpressionReference.parseCell("B2");
    private final static SpreadsheetCellReference CELL2 = SpreadsheetExpressionReference.parseCell("C3");
    private static final SpreadsheetFormula FORMULA1 = SpreadsheetFormula.EMPTY
            .setText("=1+2+3");
    private static final SpreadsheetFormula FORMULA2 = SpreadsheetFormula.EMPTY
            .setText("=4+5");

    @Test
    public void testEmpty() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
        );
    }

    @Test
    public void testOnlyLabels() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
                        .setLabels(Sets.of(LABEL1.mapping(CELL1), LABEL2.mapping(CELL2)))
        );
    }

    @Test
    public void testOnlyDeletedCells() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
                        .setDeletedCells(Sets.of(CELL1, CELL2))
        );
    }

    @Test
    public void testOnlyColumnWidths() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
                        .setColumnWidths(Maps.of(SpreadsheetExpressionReference.parseColumn("Z"), 99.0))
        );
    }

    @Test
    public void testOnlyRowHeights() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
                        .setRowHeights(Maps.of(SpreadsheetExpressionReference.parseRow("99"), 98.0))
        );
    }

    @Test
    public void testCellsWithOnlyCellReferences() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        CELL1.setFormula(FORMULA1)
                                )
                        )
        );
    }

    @Test
    public void testCellsWithOnlyCellReferences2() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        CELL1.setFormula(FORMULA1),
                                        CELL2.setFormula(FORMULA2)
                                )
                        )
        );
    }

    @Test
    public void testCellsWithFormulaIncludingLabel() {
        this.applyAndCheck2(
                SpreadsheetDelta.EMPTY
                        .setCells(
                                Sets.of(
                                        CELL1.setFormula(
                                                SpreadsheetFormula.EMPTY
                                                        .setText("=1+" + LABEL1)
                                        )
                                )
                        )
        );
    }

    @Test
    public void testCellsWithLabel() {
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(
                                CELL1.setFormula(FORMULA1)
                        )
                );
        this.applyAndCheck2(
                marshall(expected).toString().replace(CELL1.toString(), LABEL1.toString()),
                expected
        );
    }

    @Test
    public void testTwoCellsWithEachWithLabel() {
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(
                                CELL1.setFormula(FORMULA1),
                                CELL2.setFormula(FORMULA2)
                        )
                );
        this.applyAndCheck2(
                marshall(expected)
                        .toString()
                        .replace(CELL1.toString(), LABEL1.toString())
                        .replace(CELL2.toString(), LABEL2.toString())
                ,
                expected
        );
    }

    @Test
    public void testCellsWithLabelAndAnotherCellWithReference() {
        final SpreadsheetDelta expected = SpreadsheetDelta.EMPTY
                .setCells(
                        Sets.of(
                                CELL1.setFormula(FORMULA1),
                                CELL2.setFormula(FORMULA2)
                        )
                );
        this.applyAndCheck2(
                marshall(expected).toString().replace(CELL1.toString(), LABEL1.toString()),
                expected
        );
    }

    private void applyAndCheck2(final SpreadsheetDelta expected) {
        this.applyAndCheck2(
                marshall(expected).toString(),
                expected
        );
    }

    private void applyAndCheck2(final String json,
                                final SpreadsheetDelta expected) {
        this.applyAndCheck(
                JsonNode.parse(json),
                SpreadsheetDelta.class,
                marshall(expected)
        );
    }

    private JsonNode marshall(final SpreadsheetDelta delta) {
        return JsonNodeMarshallContexts.basic()
                .marshall(delta);
    }

    @Override
    public SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessor createBiFunction() {
        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();

        store.save(LABEL1.mapping(CELL1));
        store.save(LABEL2.mapping(CELL2));

        return SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessor.with(store);
    }

    @Override
    public Class<SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessor> type() {
        return SpreadsheetDeltaJsonCellLabelResolverJsonNodeUnmarshallContextPreProcessor.class;
    }
}
