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

import walkingkooka.ToStringBuilder;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Holds the results of invoking a {@link walkingkooka.spreadsheet.reference.store.SpreadsheetLabelStore#findSimilar(String, int)}.
 */
public final class SpreadsheetExpressionReferenceSimilarities implements HateosResource<String> {

    /**
     * Factory that creates a {@link SpreadsheetExpressionReferenceSimilarities}
     */
    public static SpreadsheetExpressionReferenceSimilarities with(final Optional<SpreadsheetCellReference> cellReference,
                                                                  final Set<SpreadsheetLabelMapping> labels) {
        Objects.requireNonNull(cellReference, "cellReference");
        Objects.requireNonNull(labels, "labels");

        return new SpreadsheetExpressionReferenceSimilarities(cellReference, Sets.immutable(labels));
    }

    private SpreadsheetExpressionReferenceSimilarities(final Optional<SpreadsheetCellReference> cellReference,
                                                       final Set<SpreadsheetLabelMapping> labels) {
        super();
        this.cellReference = cellReference;
        this.labels = labels;
    }

    public Optional<SpreadsheetCellReference> cellReference() {
        return this.cellReference;
    }

    private final Optional<SpreadsheetCellReference> cellReference;

    public Set<SpreadsheetLabelMapping> labels() {
        return this.labels;
    }

    private Set<SpreadsheetLabelMapping> labels;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(this.cellReference, this.labels);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof SpreadsheetExpressionReferenceSimilarities && this.equals0((SpreadsheetExpressionReferenceSimilarities) other);
    }

    private boolean equals0(final SpreadsheetExpressionReferenceSimilarities other) {
        return this.cellReference.equals(other.cellReference) &&
                this.labels.equals(other.labels);
    }

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .value(this.cellReference)
                .value(this.labels)
                .build();
    }

    // json.............................................................................................................

    static SpreadsheetExpressionReferenceSimilarities unmarshall(final JsonNode node,
                                                                 final JsonNodeUnmarshallContext context) {
        Optional<SpreadsheetCellReference> cellReference = Optional.empty();
        Set<SpreadsheetLabelMapping> labels = Sets.empty();

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();

            switch (name.value()) {
                case CELL_REFERENCE_PROPERTY_STRING:
                    cellReference = Optional.of(
                            context.unmarshall(child, SpreadsheetCellReference.class)
                    );
                    break;
                case LABELS_PROPERTY_STRING:
                    labels = context.unmarshallSet(child, SpreadsheetLabelMapping.class);
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
            }
        }

        return with(cellReference, labels);
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        final List<JsonNode> children = Lists.array();

        final Optional<SpreadsheetCellReference> reference = this.cellReference();
        if (reference.isPresent()) {
            children.add(context.marshall(reference.get()).setName(CELL_REFERENCE_PROPERTY));
        }

        final Set<SpreadsheetLabelMapping> labels = this.labels();
        if (!labels.isEmpty()) {
            children.add(context.marshallSet(labels).setName(LABELS_PROPERTY));
        }

        return JsonNode.object()
                .setChildren(children);
    }

    private final static String CELL_REFERENCE_PROPERTY_STRING = "cell-reference";
    private final static String LABELS_PROPERTY_STRING = "labels";

    // @VisibleForTesting
    final static JsonPropertyName CELL_REFERENCE_PROPERTY = JsonPropertyName.with(CELL_REFERENCE_PROPERTY_STRING);
    // @VisibleForTesting
    final static JsonPropertyName LABELS_PROPERTY = JsonPropertyName.with(LABELS_PROPERTY_STRING);

    static {
        // force static initializers to run, preventing Json type name lookup failures.
        SpreadsheetLabelName.labelName("Label").mapping(SpreadsheetCellReference.parseCellReference("A1"));

        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetExpressionReferenceSimilarities.class),
                SpreadsheetExpressionReferenceSimilarities::unmarshall,
                SpreadsheetExpressionReferenceSimilarities::marshall,
                SpreadsheetExpressionReferenceSimilarities.class
        );
    }

    // HateosResource...................................................................................................

    @Override
    public String hateosLinkId() {
        return "";
    }

    @Override
    public Optional<String> id() {
        return Optional.empty();
    }
}
