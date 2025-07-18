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

package walkingkooka.spreadsheet.server.parser;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.SpreadsheetParserName;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerLinkRelations;

public final class SpreadsheetParserHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<SpreadsheetParserName,
        SpreadsheetParserInfo,
        SpreadsheetParserInfoSet,
        SpreadsheetParserInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProviderHateosResourceHandlerContext() {

        // parser GET...............................................................................................

        return HateosResourceMappings.with(
            SpreadsheetParserName.HATEOS_RESOURCE_NAME,
            SpreadsheetParserHateosResourceMappings::parseSelection,
            SpreadsheetParserInfo.class, // valueType
            SpreadsheetParserInfoSet.class, // collectionType
            SpreadsheetParserInfo.class, // resourceType
            SpreadsheetProviderHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            SpreadsheetParserInfoHateosResourceHandler.INSTANCE
        );
    }

    public static HateosResourceMappings<SpreadsheetParserName,
        SpreadsheetParserInfo,
        SpreadsheetParserInfoSet,
        SpreadsheetParserInfo,
        SpreadsheetEngineHateosResourceHandlerContext> engine() {

        // parser GET...............................................................................................

        return HateosResourceMappings.with(
            SpreadsheetParserName.HATEOS_RESOURCE_NAME,
            SpreadsheetParserHateosResourceMappings::parseSelection,
            SpreadsheetParserInfo.class, // valueType
            SpreadsheetParserInfoSet.class, // collectionType
            SpreadsheetParserInfo.class, // resourceType
            SpreadsheetEngineHateosResourceHandlerContext.class // context
        ).setHateosHttpEntityHandler(
            SpreadsheetServerLinkRelations.EDIT,
            HttpMethod.GET,
            SpreadsheetParserSelectorEditHateosHttpEntityHandler.instance()
        );
    }

    private static HateosResourceSelection<SpreadsheetParserName> parseSelection(final String text,
                                                                                 final HateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetParserName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                selection = HateosResourceSelection.all();
                break;
            default:
                selection = HateosResourceSelection.one(
                    SpreadsheetParserName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private SpreadsheetParserHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
