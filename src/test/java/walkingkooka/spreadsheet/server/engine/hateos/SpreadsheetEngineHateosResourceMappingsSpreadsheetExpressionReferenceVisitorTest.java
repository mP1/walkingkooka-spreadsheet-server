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

package walkingkooka.spreadsheet.server.engine.hateos;

import org.junit.jupiter.api.Test;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceVisitorTesting;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetRange;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStores;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitorTest implements SpreadsheetExpressionReferenceVisitorTesting<SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor> {

    @Test
    public void testNullReferenceFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor.reference(null, SpreadsheetLabelStores.fake())
        );
    }

    @Test
    public void testNullLabelStoreFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor.reference(cell(), null)
        );
    }

    @Test
    public void testUnknownLabel() {
        final SpreadsheetLabelName label = this.label();
        this.referenceAndCheck(
                label,
                SpreadsheetLabelStores.treeMap(),
                null
        );
    }

    @Test
    public void testReference() {
        final SpreadsheetCellReference cell = this.cell();
        this.referenceAndCheck(
                cell,
                SpreadsheetLabelStores.fake(),
                cell
        );
    }

    @Test
    public void testLabelToReference() {
        final SpreadsheetLabelName label = this.label();
        final SpreadsheetCellReference cell = this.cell();

        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(label.mapping(cell));

        this.referenceAndCheck(
                label,
                store,
                cell
        );
    }

    @Test
    public void testLabelToLabelToReference() {
        final SpreadsheetLabelName label = this.label();
        final SpreadsheetLabelName label2 = SpreadsheetLabelName.labelName("Label22222");
        final SpreadsheetCellReference cell = this.cell();

        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(label.mapping(label2));
        store.save(label2.mapping(cell));

        this.referenceAndCheck(
                label,
                store,
                cell
        );
    }

    @Test
    public void testRange() {
        final SpreadsheetCellReference cell = this.cell();
        final SpreadsheetCellReference cell2 = SpreadsheetCellReference.parseCellReference("Z99");
        final SpreadsheetRange range = SpreadsheetRange.with(cell.range(cell2));

        final SpreadsheetLabelName label = this.label();

        final SpreadsheetLabelStore store = SpreadsheetLabelStores.treeMap();
        store.save(label.mapping(range));

        this.referenceAndCheck(
                cell,
                SpreadsheetLabelStores.fake(),
                cell
        );
    }

    private SpreadsheetCellReference cell() {
        return SpreadsheetCellReference.parseCellReference("A2");
    }

    private SpreadsheetLabelName label() {
        return SpreadsheetLabelName.labelName("label123");
    }

    private void referenceAndCheck(final SpreadsheetExpressionReference reference,
                                   final SpreadsheetLabelStore store,
                                   final SpreadsheetCellReference expected) {
        assertEquals(
                Optional.ofNullable(expected),
                SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor.reference(reference, store),
                () -> "reference " + reference + " store=" + store
        );
    }

    @Override
    public SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor createVisitor() {
        return new SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor(null);
    }

    @Override
    public Class<SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor> type() {
        return SpreadsheetEngineHateosResourceMappingsSpreadsheetExpressionReferenceVisitor.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return SpreadsheetEngineHateosResourceMappings.class.getSimpleName();
    }
}
