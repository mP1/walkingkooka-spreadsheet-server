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

package walkingkooka.spreadsheet.server.export;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.export.SpreadsheetExporterInfo;
import walkingkooka.spreadsheet.export.SpreadsheetExporterInfoSet;
import walkingkooka.spreadsheet.export.SpreadsheetExporterName;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

public final class SpreadsheetExporterHateosResourceMappings implements PublicStaticHelper {

    // exporter........................................................................................................

    public static HateosResourceMappings<SpreadsheetExporterName,
        SpreadsheetExporterInfo,
        SpreadsheetExporterInfoSet,
        SpreadsheetExporterInfo,
        SpreadsheetProviderHateosResourceHandlerContext> exporter() {

        // exporter GET...............................................................................................

        return HateosResourceMappings.with(
            SpreadsheetExporterName.HATEOS_RESOURCE_NAME,
            SpreadsheetExporterHateosResourceMappings::parseSelection,
            SpreadsheetExporterInfo.class, // valueType
            SpreadsheetExporterInfoSet.class, // collectionType
            SpreadsheetExporterInfo.class,// resourceType
            SpreadsheetProviderHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            SpreadsheetExporterInfoHateosResourceHandler.INSTANCE
        );
    }

    private static HateosResourceSelection<SpreadsheetExporterName> parseSelection(final String text,
                                                                                   final SpreadsheetProviderHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetExporterName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                throw new IllegalArgumentException("Invalid exporter selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                    SpreadsheetExporterName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private SpreadsheetExporterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
