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

package walkingkooka.spreadsheet.server.context;

import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.ArrayList;

/**
 * Payload for lists of {@link SpreadsheetMetadata}.
 */
public final class SpreadsheetMetadataList extends ArrayList<SpreadsheetMetadata> {

    static {
        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(SpreadsheetMetadataList.class),
                SpreadsheetMetadataList::unmarshall,
                SpreadsheetMetadataList::marshall,
                SpreadsheetMetadataList.class
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return context.marshallCollection(this);
    }

    // @VisibleForTesting
    static SpreadsheetMetadataList unmarshall(final JsonNode node,
                                              final JsonNodeUnmarshallContext context) {
        final SpreadsheetMetadataList list = empty();
        list.addAll(
                context.unmarshallList(
                        node,
                        SpreadsheetMetadata.class
                )
        );
        return list;
    }

    /**
     * Factory that creates an empty {@link SpreadsheetMetadataList}.
     */
    public static SpreadsheetMetadataList empty() {
        return new SpreadsheetMetadataList();
    }

    private SpreadsheetMetadataList() {
    }
}
