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
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetExpressionReferenceSimilaritiesTest implements HashCodeEqualsDefinedTesting2<SpreadsheetExpressionReferenceSimilarities>,
        ClassTesting<SpreadsheetExpressionReferenceSimilarities>, JsonNodeMarshallingTesting<SpreadsheetExpressionReferenceSimilarities>, ToStringTesting<SpreadsheetExpressionReferenceSimilarities> {

    private final static SpreadsheetCellReference REFERENCE = SpreadsheetCellReference.parseCellReference("B2");
    private final static SpreadsheetLabelName LABEL = SpreadsheetLabelName.labelName("Label123");
    private final static SpreadsheetCellReference LABEL_REFERENCE = SpreadsheetCellReference.parseCellReference("C3");
    private final static SpreadsheetLabelMapping MAPPING = LABEL.mapping(LABEL_REFERENCE);

    @Test
    public void testWithNullReferenceFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetExpressionReferenceSimilarities.with(null, Sets.of(MAPPING)));
    }

    @Test
    public void testWithNullLabelsFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetExpressionReferenceSimilarities.with(Optional.of(REFERENCE), null));
    }

    @Test
    public void testWithoutCellReference() {
        this.withAndCheck(null, MAPPING);
    }

    @Test
    public void testWithCellReference() {
        this.withAndCheck(REFERENCE);
    }

    @Test
    public void testWithCellReferenceAndMapping() {
        this.withAndCheck(REFERENCE, MAPPING);
    }

    private void withAndCheck(final SpreadsheetCellReference reference,
                              final SpreadsheetLabelMapping...labels) {
        this.withAndCheck(reference, Sets.of(labels));
    }

    private void withAndCheck(final SpreadsheetCellReference reference,
                              final Set<SpreadsheetLabelMapping> labels) {
        final SpreadsheetExpressionReferenceSimilarities similar = SpreadsheetExpressionReferenceSimilarities.with(Optional.ofNullable(reference), labels);
        assertEquals(Optional.ofNullable(reference), similar.cellReference(), "cellReference");
        assertEquals(labels, similar.labels(), "labels");
    }

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
                this.createObject(),
                "{\n" +
                        "  \"cell-reference\": \"B2\",\n" +
                        "  \"labels\": [{\n" +
                        "    \"label\": \"Label123\",\n" +
                        "    \"reference\": \"C3\"\n" +
                        "  }]\n" +
                        "}"
        );
    }

    @Test
    public void testDifferentCellReference() {
        this.checkNotEquals(SpreadsheetExpressionReferenceSimilarities.with(
                Optional.of(SpreadsheetCellReference.parseCellReference("Z99")),
                Sets.of(MAPPING)
        ));
    }

    @Test
    public void testDifferentLabels() {
        this.checkNotEquals(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.of(REFERENCE),
                        Sets.of(MAPPING, SpreadsheetLabelName.labelName("Label99").mapping(REFERENCE))
                )
        );
    }

    @Test
    public void testToString() {
        this.toStringAndCheck(this.createObject(), "B2 Label123=C3");
    }

    @Test
    public void testToString2() {
        this.toStringAndCheck(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.of(REFERENCE),
                        Sets.of(MAPPING, SpreadsheetLabelName.labelName("Label99").mapping(SpreadsheetCellReference.parseCellReference("Z9")))
                ),
                "B2 Label123=C3, Label99=Z9"
        );
    }

    @Override
    public SpreadsheetExpressionReferenceSimilarities createObject() {
        return SpreadsheetExpressionReferenceSimilarities.with(
                Optional.of(REFERENCE),
                Sets.of(MAPPING)
        );
    }

    @Override
    public SpreadsheetExpressionReferenceSimilarities createJsonNodeMappingValue() {
        return this.createObject();
    }

    @Override
    public SpreadsheetExpressionReferenceSimilarities unmarshall(final JsonNode node,
                                                                 final JsonNodeUnmarshallContext context) {
        return SpreadsheetExpressionReferenceSimilarities.unmarshall(node, context);
    }

    @Override
    public Class<SpreadsheetExpressionReferenceSimilarities> type() {
        return SpreadsheetExpressionReferenceSimilarities.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
