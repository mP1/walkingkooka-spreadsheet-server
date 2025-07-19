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

package walkingkooka.spreadsheet.server.comparator;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

public final class SpreadsheetComparatorHateosResourceMappings implements PublicStaticHelper {

    // comparator.......................................................................................................

    public static HateosResourceMappings<SpreadsheetComparatorName,
        SpreadsheetComparatorInfo,
        SpreadsheetComparatorInfoSet,
        SpreadsheetComparatorInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProviderHateosResourceHandlerContext() {

        // comparator GET...............................................................................................

        HateosResourceMappings<SpreadsheetComparatorName,
            SpreadsheetComparatorInfo,
            SpreadsheetComparatorInfoSet,
            SpreadsheetComparatorInfo,
            SpreadsheetProviderHateosResourceHandlerContext> comparator = HateosResourceMappings.with(
            SpreadsheetComparatorName.HATEOS_RESOURCE_NAME,
            SpreadsheetComparatorHateosResourceMappings::parseComparatorSelection,
            SpreadsheetComparatorInfo.class, // valueType
            SpreadsheetComparatorInfoSet.class, // collectionType
            SpreadsheetComparatorInfo.class,// resourceType
            SpreadsheetProviderHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            SpreadsheetComparatorInfoHateosResourceHandler.INSTANCE
        );

        return comparator;
    }

    private static HateosResourceSelection<SpreadsheetComparatorName> parseComparatorSelection(final String text,
                                                                                               final SpreadsheetProviderHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetComparatorName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                throw new IllegalArgumentException("Invalid comparator selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                    SpreadsheetComparatorName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private SpreadsheetComparatorHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
