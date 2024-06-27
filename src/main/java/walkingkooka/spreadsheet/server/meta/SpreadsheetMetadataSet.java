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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.hateos.HateosResource;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * A read only {@link Set} of {@link SpreadsheetMetadata} sorted by {@link walkingkooka.spreadsheet.SpreadsheetId}.
 */
public final class SpreadsheetMetadataSet extends AbstractSet<SpreadsheetMetadata> {

    /**
     * Factory that creates a {@link SpreadsheetMetadataSet} with the provided {@link SpreadsheetMetadata}.
     */
    public static SpreadsheetMetadataSet with(final Set<SpreadsheetMetadata> metadatas) {
        Objects.requireNonNull(metadatas, "metadatas");

        final Set<SpreadsheetMetadata> copy = Sets.sorted(HateosResource.comparator());
        copy.addAll(metadatas);
        return new SpreadsheetMetadataSet(copy);
    }

    private SpreadsheetMetadataSet(final Set<SpreadsheetMetadata> metadatas) {
        this.metadatas = metadatas;
    }

    // AbstractSet......................................................................................................

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

    private final Set<SpreadsheetMetadata> metadatas;

    // json.............................................................................................................

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetMetadataSet.class),
                SpreadsheetMetadataSet::unmarshall,
                SpreadsheetMetadataSet::marshall,
                SpreadsheetMetadataSet.class
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    // @VisibleForTesting
    static SpreadsheetMetadataSet unmarshall(final JsonNode node,
                                             final JsonNodeUnmarshallContext context) {
        return with(
                context.unmarshallSet(
                        node,
                        SpreadsheetMetadata.class
                )
        );
    }
}
