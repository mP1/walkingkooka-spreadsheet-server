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

package walkingkooka.spreadsheet.server.formatter;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetServerLinkRelations;

import java.util.Objects;

public final class SpreadsheetFormatterHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<SpreadsheetFormatterName,
        SpreadsheetFormatterInfo,
        SpreadsheetFormatterInfoSet,
        SpreadsheetFormatterInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProvider() {

        return HateosResourceMappings.with(
            SpreadsheetFormatterName.HATEOS_RESOURCE_NAME,
            SpreadsheetFormatterHateosResourceMappings::parseSelection,
            SpreadsheetFormatterInfo.class, // valueType
            SpreadsheetFormatterInfoSet.class, // collectionType
            SpreadsheetFormatterInfo.class,// resourceType
            SpreadsheetProviderHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            SpreadsheetFormatterInfoHateosResourceHandler.INSTANCE
        );
    }

    // engine...........................................................................................................

    public static HateosResourceMappings<SpreadsheetFormatterName,
        SpreadsheetFormatterInfo,
        SpreadsheetFormatterInfoSet,
        SpreadsheetFormatterInfo,
        SpreadsheetEngineHateosResourceHandlerContext> engine(final SpreadsheetEngineContext context) {
        Objects.requireNonNull(context, "context");

        return HateosResourceMappings.with(
            SpreadsheetFormatterName.HATEOS_RESOURCE_NAME,
            SpreadsheetFormatterHateosResourceMappings::parseSelection,
            SpreadsheetFormatterInfo.class, // valueType
            SpreadsheetFormatterInfoSet.class, // collectionType
            SpreadsheetFormatterInfo.class,// resourceType
            SpreadsheetEngineHateosResourceHandlerContext.class
        ).setHateosHttpEntityHandler(
            SpreadsheetServerLinkRelations.EDIT,
            HttpMethod.GET,
            SpreadsheetFormatterSelectorEditHateosHttpEntityHandler.instance()
        ).setHateosHttpEntityHandler(
            SpreadsheetServerLinkRelations.MENU,
            HttpMethod.GET,
            SpreadsheetFormatterMenuHateosHttpEntityHandler.instance()
        );
    }

    private static HateosResourceSelection<SpreadsheetFormatterName> parseSelection(final String text,
                                                                                    final HateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetFormatterName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                //GET /formatters
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                //POST /formatters/*/format
                selection = HateosResourceSelection.all();
                break;
            default:
                // POST /formatter/formatter-name
                selection = HateosResourceSelection.one(
                    SpreadsheetFormatterName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private SpreadsheetFormatterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
