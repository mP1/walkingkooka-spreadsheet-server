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

package walkingkooka.spreadsheet.server.importer;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterInfo;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterInfoSet;
import walkingkooka.spreadsheet.importer.SpreadsheetImporterName;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

public final class SpreadsheetImporterHateosResourceMappings implements PublicStaticHelper {

    // importer.......................................................................................................

    public static HateosResourceMappings<SpreadsheetImporterName,
        SpreadsheetImporterInfo,
        SpreadsheetImporterInfoSet,
        SpreadsheetImporterInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProviderHateosResourceHandlerContext() {

        // importer GET...............................................................................................

        return HateosResourceMappings.with(
            SpreadsheetImporterName.HATEOS_RESOURCE_NAME,
            SpreadsheetImporterHateosResourceMappings::parseSelection,
            SpreadsheetImporterInfo.class, // valueType
            SpreadsheetImporterInfoSet.class, // collectionType
            SpreadsheetImporterInfo.class,// resourceType
            SpreadsheetProviderHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            SpreadsheetImporterInfoHateosResourceHandler.INSTANCE
        );
    }

    private static HateosResourceSelection<SpreadsheetImporterName> parseSelection(final String text,
                                                                                   final SpreadsheetProviderHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetImporterName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                throw new IllegalArgumentException("Invalid importer selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                    SpreadsheetImporterName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private SpreadsheetImporterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
