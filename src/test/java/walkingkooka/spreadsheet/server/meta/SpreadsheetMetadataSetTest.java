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
import walkingkooka.collect.set.Sets;
import walkingkooka.net.http.server.hateos.HateosResourceSetTesting;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

public final class SpreadsheetMetadataSetTest implements HateosResourceSetTesting<SpreadsheetMetadataSet, SpreadsheetMetadata, SpreadsheetId>,
        SpreadsheetMetadataTesting,
        ClassTesting<SpreadsheetMetadataSet> {

    // Set..............................................................................................................

    @Override
    public SpreadsheetMetadataSet createSet() {
        return SpreadsheetMetadataSet.with(
                Sets.of(
                        SpreadsheetMetadataTesting.METADATA_EN_AU
                )
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshallEmpty() {
        this.marshallAndCheck(
                SpreadsheetMetadataSet.with(Sets.empty()),
                JsonNode.array()
        );
    }

    @Test
    public void testMarshallNotEmpty() {
        final SpreadsheetMetadataSet set = SpreadsheetMetadataSet.with(
                Sets.of(SpreadsheetMetadata.EMPTY)
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
        final SpreadsheetMetadataSet set = SpreadsheetMetadataSet.with(
                Sets.of(
                        SpreadsheetMetadata.EMPTY.set(
                                SpreadsheetMetadataPropertyName.SPREADSHEET_ID,
                                SpreadsheetId.with(1)
                        )
                )
        );

        this.marshallAndCheck(
                set,
                "[\n" +
                        "  {\n" +
                        "    \"spreadsheet-id\": \"1\"\n" +
                        "  }\n" +
                        "]"
        );
    }

    // json............................................................................................................

    @Override
    public SpreadsheetMetadataSet unmarshall(final JsonNode node,
                                             final JsonNodeUnmarshallContext context) {
        return SpreadsheetMetadataSet.unmarshall(
                node,
                context
        );
    }

    @Override
    public SpreadsheetMetadataSet createJsonNodeMarshallingValue() {
        return SpreadsheetMetadataSet.with(
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
    public Class<SpreadsheetMetadataSet> type() {
        return SpreadsheetMetadataSet.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
