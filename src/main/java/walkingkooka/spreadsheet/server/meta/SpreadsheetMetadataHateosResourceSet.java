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

import walkingkooka.collect.iterator.Iterators;
import walkingkooka.collect.set.ImmutableSortedSetDefaults;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A read only {@link Set} of {@link SpreadsheetMetadata} sorted by {@link walkingkooka.spreadsheet.meta.SpreadsheetId}.
 */
public final class SpreadsheetMetadataHateosResourceSet extends AbstractSet<SpreadsheetMetadata>
    implements ImmutableSortedSetDefaults<SpreadsheetMetadataHateosResourceSet, SpreadsheetMetadata> {

    /**
     * Comparator that uses the {@link SpreadsheetMetadata#id()}, also supporting those without a {@link SpreadsheetId}.
     */
    public final static Comparator<SpreadsheetMetadata> COMPARATOR = (SpreadsheetMetadata left,
                                                                      final SpreadsheetMetadata right) ->
        string(left).compareTo(string(right));

    private static String string(final SpreadsheetMetadata spreadsheetMetadata) {
        return spreadsheetMetadata.get(SpreadsheetMetadataPropertyName.SPREADSHEET_ID)
            .map(SpreadsheetId::text)
            .orElse("");
    }

    public final static SpreadsheetMetadataHateosResourceSet EMPTY = new SpreadsheetMetadataHateosResourceSet(
        SortedSets.tree(COMPARATOR)
    );

    /**
     * Factory that creates a {@link SpreadsheetMetadataHateosResourceSet} with the provided {@link SpreadsheetMetadata}.
     */
    // @VisibleForTesting
    static SpreadsheetMetadataHateosResourceSet withCopy(final SortedSet<SpreadsheetMetadata> metadatas) {
        return metadatas.isEmpty() ?
            EMPTY :
            new SpreadsheetMetadataHateosResourceSet(metadatas);
    }

    private SpreadsheetMetadataHateosResourceSet(final SortedSet<SpreadsheetMetadata> metadatas) {
        super();

        this.metadatas = metadatas;
    }

    // ImmutableSortedSet...............................................................................................

    @Override
    public Iterator<SpreadsheetMetadata> iterator() {
        return Iterators.readOnly(
            this.metadatas.iterator()
        );
    }

    @Override
    public int size() {
        return this.metadatas.size();
    }

    @Override
    public Comparator<SpreadsheetMetadata> comparator() {
        return COMPARATOR;
    }

    @Override
    public SpreadsheetMetadataHateosResourceSet subSet(final SpreadsheetMetadata from,
                                                       final SpreadsheetMetadata to) {
        return withCopy(
            this.metadatas.subSet(
                from,
                to
            )
        );
    }

    @Override
    public SpreadsheetMetadataHateosResourceSet headSet(final SpreadsheetMetadata metadata) {
        return withCopy(
            this.metadatas.headSet(metadata)
        );
    }

    @Override
    public SpreadsheetMetadataHateosResourceSet tailSet(final SpreadsheetMetadata metadata) {
        return withCopy(
            this.metadatas.tailSet(metadata)
        );
    }

    @Override
    public SpreadsheetMetadata first() {
        return this.metadatas.first();
    }

    @Override
    public SpreadsheetMetadata last() {
        return this.metadatas.last();
    }

    @Override
    public SortedSet<SpreadsheetMetadata> toSet() {
        return new TreeSet<>(this.metadatas);
    }

    @Override
    public SpreadsheetMetadataHateosResourceSet setElements(final Collection<SpreadsheetMetadata> metadatas) {
        final SpreadsheetMetadataHateosResourceSet spreadsheetMetadataHateosResourceSet;

        if (metadatas instanceof SpreadsheetMetadataHateosResourceSet) {
            spreadsheetMetadataHateosResourceSet = (SpreadsheetMetadataHateosResourceSet) metadatas;
        } else {
            final TreeSet<SpreadsheetMetadata> copy = new TreeSet<>(COMPARATOR);
            copy.addAll(
                Objects.requireNonNull(metadatas, "metadatas")
            );
            spreadsheetMetadataHateosResourceSet = this.metadatas.equals(copy) ?
                this :
                withCopy(copy);
        }

        return spreadsheetMetadataHateosResourceSet;
    }

    private final SortedSet<SpreadsheetMetadata> metadatas;

    @Override
    public void elementCheck(final SpreadsheetMetadata metadata) {
        Objects.requireNonNull(metadata, "metadata");
    }

    // json.............................................................................................................

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(SpreadsheetMetadataHateosResourceSet.class),
            SpreadsheetMetadataHateosResourceSet::unmarshall,
            SpreadsheetMetadataHateosResourceSet::marshall,
            SpreadsheetMetadataHateosResourceSet.class
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    // @VisibleForTesting
    static SpreadsheetMetadataHateosResourceSet unmarshall(final JsonNode node,
                                                           final JsonNodeUnmarshallContext context) {
        return EMPTY.setElements(
            context.unmarshallSet(
                node,
                SpreadsheetMetadata.class
            )
        );
    }
}
