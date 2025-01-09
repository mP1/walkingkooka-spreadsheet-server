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

package walkingkooka.spreadsheet.server.delta;

import walkingkooka.ToStringBuilder;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.spreadsheet.reference.SpreadsheetCellReference;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
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

    public final static Optional<SpreadsheetCellReference> NO_CELL_REFERENCE = Optional.empty();

    public final static Optional<SpreadsheetLabelName> NO_LABEL = Optional.empty();

    /**
     * Factory that creates a {@link SpreadsheetExpressionReferenceSimilarities}
     */
    public static SpreadsheetExpressionReferenceSimilarities with(final Optional<SpreadsheetCellReference> cellReference,
                                                                  final Optional<SpreadsheetLabelName> label,
                                                                  final Set<SpreadsheetLabelMapping> labelMappings) {
        Objects.requireNonNull(cellReference, "cellReference");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(labelMappings, "labelMappings");

        return new SpreadsheetExpressionReferenceSimilarities(
            cellReference,
            label,
            Sets.immutable(labelMappings)
        );
    }

    private SpreadsheetExpressionReferenceSimilarities(final Optional<SpreadsheetCellReference> cellReference,
                                                       final Optional<SpreadsheetLabelName> label,
                                                       final Set<SpreadsheetLabelMapping> labelMappings) {
        super();

        if (label.isPresent()) {
            final SpreadsheetLabelName label2 = label.get();
            if (labelMappings.stream()
                .anyMatch(m -> m.label().equals(label2))) {
                throw new IllegalArgumentException("Label " + label2 + " present within mappings: " + labelMappings);
            }
        }

        this.cellReference = cellReference;
        this.label = label;
        this.labelMappings = labelMappings;
    }

    public Optional<SpreadsheetCellReference> cellReference() {
        return this.cellReference;
    }

    private final Optional<SpreadsheetCellReference> cellReference;

    /**
     * When present the {@link SpreadsheetLabelName label} os unknown and does not have a {@link SpreadsheetLabelMapping}.
     */
    public Optional<SpreadsheetLabelName> label() {
        return this.label;
    }

    private final Optional<SpreadsheetLabelName> label;

    public Set<SpreadsheetLabelMapping> labelMappings() {
        return this.labelMappings;
    }

    private final Set<SpreadsheetLabelMapping> labelMappings;

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(this.cellReference, this.label, this.labelMappings);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
            other instanceof SpreadsheetExpressionReferenceSimilarities && this.equals0((SpreadsheetExpressionReferenceSimilarities) other);
    }

    private boolean equals0(final SpreadsheetExpressionReferenceSimilarities other) {
        return this.cellReference.equals(other.cellReference) &&
            this.labelMappings.equals(other.labelMappings);
    }

    @Override
    public String toString() {
        return ToStringBuilder.empty()
            .value(this.cellReference)
            .value(this.label)
            .value(this.labelMappings)
            .build();
    }

    // json.............................................................................................................

    static SpreadsheetExpressionReferenceSimilarities unmarshall(final JsonNode node,
                                                                 final JsonNodeUnmarshallContext context) {
        Optional<SpreadsheetCellReference> cellReference = Optional.empty();
        Optional<SpreadsheetLabelName> label = Optional.empty();
        Set<SpreadsheetLabelMapping> labelMappings = Sets.empty();

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName name = child.name();

            switch (name.value()) {
                case CELL_REFERENCE_PROPERTY_STRING:
                    cellReference = Optional.of(
                        context.unmarshall(child, SpreadsheetCellReference.class)
                    );
                    break;
                case LABEL_PROPERTY_STRING:
                    label = Optional.of(
                        context.unmarshall(child, SpreadsheetLabelName.class)
                    );
                    break;
                case LABEL_MAPPINGS_PROPERTY_STRING:
                    labelMappings = context.unmarshallSet(child, SpreadsheetLabelMapping.class);
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(name, node);
            }
        }

        return with(cellReference, label, labelMappings);
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        final List<JsonNode> children = Lists.array();

        final Optional<SpreadsheetCellReference> reference = this.cellReference();
        if (reference.isPresent()) {
            children.add(context.marshall(reference.get()).setName(CELL_REFERENCE_PROPERTY));
        }

        final Optional<SpreadsheetLabelName> label = this.label();
        if (label.isPresent()) {
            children.add(context.marshall(label.get()).setName(LABEL_PROPERTY));
        }

        final Set<SpreadsheetLabelMapping> labelMappings = this.labelMappings();
        if (!labelMappings.isEmpty()) {
            children.add(
                context.marshallCollection(labelMappings)
                    .setName(LABEL_MAPPINGS_PROPERTY)
            );
        }

        return JsonNode.object()
            .setChildren(children);
    }

    private final static String CELL_REFERENCE_PROPERTY_STRING = "cell-reference";
    private final static String LABEL_PROPERTY_STRING = "label";
    private final static String LABEL_MAPPINGS_PROPERTY_STRING = "label-mappings";

    // @VisibleForTesting
    final static JsonPropertyName CELL_REFERENCE_PROPERTY = JsonPropertyName.with(CELL_REFERENCE_PROPERTY_STRING);
    // @VisibleForTesting
    final static JsonPropertyName LABEL_PROPERTY = JsonPropertyName.with(LABEL_PROPERTY_STRING);
    // @VisibleForTesting
    final static JsonPropertyName LABEL_MAPPINGS_PROPERTY = JsonPropertyName.with(LABEL_MAPPINGS_PROPERTY_STRING);

    static {
        // force static initializers to run, preventing Json type name lookup failures.
        SpreadsheetSelection.labelName("Label")
            .mapping(SpreadsheetSelection.A1);

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
