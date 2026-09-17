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
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.provider.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosHandlerContext;

public final class SpreadsheetFormatterHateosResourceMappings implements PublicStaticHelper {

    /**
     * {@see SpreadsheetFormatterSelectorEditHttpHandlerCell}
     */
    public static HttpHandler<SpreadsheetEngineHateosHandlerContext> spreadsheetFormatterSelectorEditHttpHandlerCell() {
        return SpreadsheetFormatterSelectorEditHttpHandlerCell.INSTANCE;
    }

    /**
     * {@see SpreadsheetFormatterSelectorEditHttpHandlerMetadata}
     */
    public static HttpHandler<SpreadsheetEngineHateosHandlerContext> spreadsheetFormatterSelectorEditHttpHandlerMetadata() {
        return SpreadsheetFormatterSelectorEditHttpHandlerMetadata.INSTANCE;
    }

    /**
     * {@see SpreadsheetFormatterMenuHttpHandler}
     */
    public static HttpHandler<SpreadsheetEngineHateosHandlerContext> spreadsheetFormatterMenuHttpHandler() {
        return SpreadsheetFormatterMenuHttpHandler.INSTANCE;
    }

    public static HateosResourceMappings<SpreadsheetFormatterName,
        SpreadsheetFormatterInfo,
        SpreadsheetFormatterInfoSet,
        SpreadsheetFormatterInfo,
        SpreadsheetProviderHateosHandlerContext> spreadsheetProviderHateosHandlerContext() {

        return HateosResourceMappings.with(
            SpreadsheetFormatterName.HATEOS_RESOURCE_NAME,
            (final String text, final SpreadsheetProviderHateosHandlerContext context) -> HateosResourceSelection.parseOneOrAll(
                text,
                SpreadsheetFormatterName::with
            ),
            SpreadsheetFormatterInfo.class, // valueType
            SpreadsheetFormatterInfoSet.class, // collectionType
            SpreadsheetFormatterInfo.class,// resourceType
            SpreadsheetProviderHateosHandlerContext.class
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            SpreadsheetFormatterInfoHateosResourceHandler.INSTANCE
        );
    }

    /**
     * Stop creation
     */
    private SpreadsheetFormatterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
