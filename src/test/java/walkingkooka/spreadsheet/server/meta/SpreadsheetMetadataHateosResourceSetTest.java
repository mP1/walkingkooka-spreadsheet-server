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
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.Sets;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicClassTesting;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallerTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.SortedSet;

public final class SpreadsheetMetadataHateosResourceSetTest implements ImmutableSortedSetTesting<SpreadsheetMetadataHateosResourceSet, SpreadsheetMetadata>,
    JsonNodeMarshallerTesting<SpreadsheetMetadataHateosResourceSet>,
    SpreadsheetMetadataTesting,
    PublicClassTesting<SpreadsheetMetadataHateosResourceSet> {

    // Set..............................................................................................................

    @Override
    public SpreadsheetMetadataHateosResourceSet createSet() {
        final SortedSet<SpreadsheetMetadata> sortedSet = SortedSets.tree(SpreadsheetMetadataHateosResourceSet.COMPARATOR);

        sortedSet.add(SpreadsheetMetadataTesting.METADATA_EN_AU);

        return SpreadsheetMetadataHateosResourceSet.withCopy(sortedSet);
    }

    // json.............................................................................................................

    @Test
    public void testMarshallEmpty() {
        this.marshallAndCheck(
            SpreadsheetMetadataHateosResourceSet.withCopy(
                SortedSets.empty()
            ),
            JsonNode.array()
        );
    }

    @Test
    public void testMarshallNotEmpty() {
        final SpreadsheetMetadataHateosResourceSet set = SpreadsheetMetadataHateosResourceSet.EMPTY.concat(
            SpreadsheetMetadata.EMPTY
        );

        this.marshallAndCheck(
            set,
            JsonNode.array()
                .appendChild(
                    JsonNode.object()
                )
        );
    }

    @Test
    public void testMarshallNotEmpty2() {
        final SpreadsheetMetadataHateosResourceSet set = SpreadsheetMetadataHateosResourceSet.EMPTY.concat(
            SpreadsheetMetadata.EMPTY.set(
                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                SpreadsheetId.with(1)
            )
        );

        this.marshallAndCheck(
            set,
            "[\n" +
                "  {\n" +
                "    \"spreadsheetId\": \"1\"\n" +
                "  }\n" +
                "]"
        );
    }

    // json............................................................................................................

    @Override
    public SpreadsheetMetadataHateosResourceSet unmarshall(final JsonNode node,
                                                           final JsonNodeUnmarshallContext context) {
        return SpreadsheetMetadataHateosResourceSet.unmarshall(
            node,
            context
        );
    }

    @Override
    public SpreadsheetMetadataHateosResourceSet createJsonNodeMarshallingValue() {
        return SpreadsheetMetadataHateosResourceSet.EMPTY.setElements(
            Sets.of(
                SpreadsheetMetadata.EMPTY.set(
                    SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                    SpreadsheetId.with(1)
                ),
                SpreadsheetMetadata.EMPTY.set(
                    SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                    SpreadsheetId.with(2)
                )
            )
        );
    }

    // Class............................................................................................................

    @Override
    public Class<SpreadsheetMetadataHateosResourceSet> type() {
        return SpreadsheetMetadataHateosResourceSet.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
