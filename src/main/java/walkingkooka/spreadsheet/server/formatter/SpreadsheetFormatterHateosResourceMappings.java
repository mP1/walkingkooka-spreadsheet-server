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
import walkingkooka.net.http.server.hateos.HateosHttpHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;

public final class SpreadsheetFormatterHateosResourceMappings implements PublicStaticHelper {

    /**
     * {@see SpreadsheetFormatterSelectorEditHateosHttpHandlerCell}
     */
    public static HateosHttpHandler<SpreadsheetEngineHateosResourceHandlerContext> spreadsheetFormatterSelectorEditHateosHttpHandlerCell() {
        return SpreadsheetFormatterSelectorEditHateosHttpHandlerCell.INSTANCE;
    }

    /**
     * {@see SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata}
     */
    public static HateosHttpHandler<SpreadsheetEngineHateosResourceHandlerContext> spreadsheetFormatterSelectorEditHateosHttpHandlerMetadata() {
        return SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata.INSTANCE;
    }

    /**
     * {@see SpreadsheetFormatterMenuHateosHttpHandler}
     */
    public static HateosHttpHandler<SpreadsheetEngineHateosResourceHandlerContext> spreadsheetFormatterMenuHateosHttpHandler() {
        return SpreadsheetFormatterMenuHateosHttpHandler.INSTANCE;
    }

    public static HateosResourceMappings<SpreadsheetFormatterName,
        SpreadsheetFormatterInfo,
        SpreadsheetFormatterInfoSet,
        SpreadsheetFormatterInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProviderHateosResourceHandlerContext() {

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
