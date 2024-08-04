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
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfo;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorInfoSet;
import walkingkooka.spreadsheet.compare.SpreadsheetComparatorName;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

public final class SpreadsheetComparatorsHateosResourceMappings implements PublicStaticHelper {

    // comparator.......................................................................................................

    public static HateosResourceMapping<SpreadsheetComparatorName,
            SpreadsheetComparatorInfo,
            SpreadsheetComparatorInfoSet,
            SpreadsheetComparatorInfo,
            SpreadsheetEngineHateosResourceHandlerContext> comparator() {

        // comparator GET...............................................................................................

        HateosResourceMapping<SpreadsheetComparatorName,
                SpreadsheetComparatorInfo,
                SpreadsheetComparatorInfoSet,
                SpreadsheetComparatorInfo,
                SpreadsheetEngineHateosResourceHandlerContext> comparator = HateosResourceMapping.with(
                COMPARATOR,
                SpreadsheetComparatorsHateosResourceMappings::parseComparatorSelection,
                SpreadsheetComparatorInfo.class, // valueType
                SpreadsheetComparatorInfoSet.class, // collectionType
                SpreadsheetComparatorInfo.class,// resourceType
                SpreadsheetEngineHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                SpreadsheetComparatorInfoHateosResourceHandler.INSTANCE
        );

        return comparator;
    }

    private static HateosResourceSelection<SpreadsheetComparatorName> parseComparatorSelection(final String text,
                                                                                               final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetComparatorName> selection;

        switch (text) {
            case "":
                selection = HateosResourceSelection.all();
                break;
            case "*":
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
     * A {@link HateosResourceName} with <code>comparator</code>.
     */
    public static final HateosResourceName COMPARATOR = HateosResourceName.with("comparator");

    /**
     * Stop creation
     */
    private SpreadsheetComparatorsHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
