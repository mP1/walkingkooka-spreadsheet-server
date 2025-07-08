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

import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerLinkRelations;

public final class MetadataHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<SpreadsheetId,
        SpreadsheetMetadata,
        SpreadsheetMetadata,
        SpreadsheetMetadata,
        SpreadsheetEngineHateosResourceHandlerContext> metadata() {

        return HateosResourceMappings.with(
            HateosResourceName.with("metadata"), // not "spreadsheet" to avoid repetition in urls
            MetadataHateosResourceMappings::parseSelection,
            SpreadsheetMetadata.class, // valueType
            SpreadsheetMetadata.class, // collectionType
            SpreadsheetMetadata.class,// resourceType
            SpreadsheetEngineHateosResourceHandlerContext.class // context
        ).setHateosHttpEntityHandler(
            SpreadsheetServerLinkRelations.VERIFY,
            HttpMethod.POST,
            ConverterSelectorVerifyHateosHttpEntityHandler.INSTANCE
        );
    }

    private static HateosResourceSelection<SpreadsheetId> parseSelection(final String text,
                                                                         final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetId> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                selection = HateosResourceSelection.all();
                break;
            default:
                throw new IllegalArgumentException("Invalid selection: " + text);
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private MetadataHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
