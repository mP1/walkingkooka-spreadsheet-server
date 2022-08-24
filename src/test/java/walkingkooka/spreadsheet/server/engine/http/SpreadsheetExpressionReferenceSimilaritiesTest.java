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
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetExpressionReferenceSimilaritiesTest implements HashCodeEqualsDefinedTesting2<SpreadsheetExpressionReferenceSimilarities>,
        ClassTesting<SpreadsheetExpressionReferenceSimilarities>, JsonNodeMarshallingTesting<SpreadsheetExpressionReferenceSimilarities>, ToStringTesting<SpreadsheetExpressionReferenceSimilarities> {

    private final static SpreadsheetCellReference REFERENCE = SpreadsheetSelection.parseCell("B2");
    private final static SpreadsheetLabelName LABEL = SpreadsheetSelection.labelName("Label123");
    private final static SpreadsheetCellReference LABEL_REFERENCE = SpreadsheetSelection.parseCell("C3");
    private final static SpreadsheetLabelMapping MAPPING = SpreadsheetSelection.labelName("Label234").mapping(LABEL_REFERENCE);

    @Test
    public void testWithNullReferenceFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetExpressionReferenceSimilarities.with(null, Optional.of(LABEL), Sets.of(MAPPING)));
    }

    @Test
    public void testWithNullLabelFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetExpressionReferenceSimilarities.with(Optional.of(REFERENCE), null, Sets.of(MAPPING)));
    }

    @Test
    public void testWithNullLabelMappingsFails() {
        assertThrows(NullPointerException.class, () -> SpreadsheetExpressionReferenceSimilarities.with(Optional.of(REFERENCE), Optional.of(LABEL), null));
    }

    @Test
    public void testWithLabelWithinMappingsFails() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> SpreadsheetExpressionReferenceSimilarities.with(Optional.of(REFERENCE), Optional.of(LABEL), Set.of(LABEL.mapping(REFERENCE))));
        this.checkEquals("Label Label123 present within mappings: [Label123=B2]", thrown.getMessage(), "message");
    }

    @Test
    public void testWithLabelAndMappings() {
        this.withAndCheck(null, LABEL, MAPPING);
    }

    @Test
    public void testWithCellReferenceAndMappings() {
        this.withAndCheck(REFERENCE, null, MAPPING);
    }

    @Test
    public void testWithCellReferenceLabelAndMapping() {
        this.withAndCheck(REFERENCE, LABEL, MAPPING);
    }

    private void withAndCheck(final SpreadsheetCellReference reference,
                              final SpreadsheetLabelName label,
                              final SpreadsheetLabelMapping... labelMappings) {
        this.withAndCheck(reference, label, Sets.of(labelMappings));
    }

    private void withAndCheck(final SpreadsheetCellReference reference,
                              final SpreadsheetLabelName label,
                              final Set<SpreadsheetLabelMapping> labelMappings) {
        final SpreadsheetExpressionReferenceSimilarities similar = SpreadsheetExpressionReferenceSimilarities.with(
                Optional.ofNullable(reference),
                Optional.ofNullable(label),
                labelMappings
        );
        this.checkEquals(Optional.ofNullable(reference), similar.cellReference(), "cellReference");
        this.checkEquals(labelMappings, similar.labelMappings(), "labelMappings");
    }

    @Test
    public void testMarshallReference() {
        this.marshallAndCheck(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.of(REFERENCE),
                        Optional.empty(),
                        Sets.of(MAPPING)
                ),
                "{\n" +
                        "  \"cell-reference\": \"B2\",\n" +
                        "  \"label-mappings\": [\n" +
                        "    {\n" +
                        "      \"label\": \"Label234\",\n" +
                        "      \"reference\": \"C3\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );
    }

    @Test
    public void testMarshallLabel() {
        this.marshallAndCheck(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.empty(),
                        Optional.of(LABEL),
                        Sets.of(MAPPING)
                ),
                "{\n" +
                        "  \"label\": \"Label123\",\n" +
                        "  \"label-mappings\": [\n" +
                        "    {\n" +
                        "      \"label\": \"Label234\",\n" +
                        "      \"reference\": \"C3\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );
    }

    @Test
    public void testMarshallReferenceAndLabel() {
        this.marshallAndCheck(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.of(REFERENCE),
                        Optional.of(LABEL),
                        Sets.of(MAPPING)
                ),
                "{\n" +
                        "  \"cell-reference\": \"B2\",\n" +
                        "  \"label\": \"Label123\",\n" +
                        "  \"label-mappings\": [\n" +
                        "    {\n" +
                        "      \"label\": \"Label234\",\n" +
                        "      \"reference\": \"C3\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );
    }

    @Test
    public void testDifferentCellReference() {
        this.checkNotEquals(SpreadsheetExpressionReferenceSimilarities.with(
                Optional.of(SpreadsheetSelection.parseCell("Z99")),
                Optional.of(LABEL),
                Sets.of(MAPPING)
        ));
    }

    @Test
    public void testDifferentLabel() {
        this.checkNotEquals(SpreadsheetExpressionReferenceSimilarities.with(
                Optional.of(SpreadsheetSelection.parseCell("Z99")),
                Optional.of(SpreadsheetSelection.labelName("DifferentLabel99")),
                Sets.of(MAPPING)
        ));
    }

    @Test
    public void testDifferentLabelMappings() {
        this.checkNotEquals(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.of(REFERENCE),
                        Optional.of(LABEL),
                        Sets.of(MAPPING, SpreadsheetSelection.labelName("Label99").mapping(REFERENCE))
                )
        );
    }

    @Test
    public void testToStringCellReferenceAndMapping() {
        this.toStringAndCheck(this.createObject(), "B2 Label123 Label234=C3");
    }

    @Test
    public void testToStringCellReferenceAndMapping2() {
        this.toStringAndCheck(
                SpreadsheetExpressionReferenceSimilarities.with(
                        Optional.of(REFERENCE),
                        Optional.of(LABEL),
                        Sets.of(MAPPING, SpreadsheetSelection.labelName("Label99").mapping(SpreadsheetSelection.parseCell("Z9")))
                ),
                "B2 Label123 Label234=C3, Label99=Z9"
        );
    }

    @Override
    public SpreadsheetExpressionReferenceSimilarities createObject() {
        return SpreadsheetExpressionReferenceSimilarities.with(
                Optional.of(REFERENCE),
                Optional.of(LABEL),
                Sets.of(MAPPING)
        );
    }

    @Override
    public SpreadsheetExpressionReferenceSimilarities createJsonNodeMarshallingValue() {
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
