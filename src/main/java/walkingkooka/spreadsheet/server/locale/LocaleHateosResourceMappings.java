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

package walkingkooka.spreadsheet.server.locale;

import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

public final class LocaleHateosResourceMappings implements PublicStaticHelper {

    // importer.......................................................................................................

    /**
     * Stop creation
     */
    private LocaleHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }

    public static HateosResourceMappings<LocaleTag,
        LocaleHateosResource,
        LocaleHateosResourceSet,
        LocaleHateosResource,
        SpreadsheetEngineHateosResourceHandlerContext> locale() {

        // importer GET...............................................................................................

        HateosResourceMappings<LocaleTag,
            LocaleHateosResource,
            LocaleHateosResourceSet,
            LocaleHateosResource,
            SpreadsheetEngineHateosResourceHandlerContext> locales = HateosResourceMappings.with(
            LocaleTag.HATEOS_RESOURCE_NAME,
            LocaleHateosResourceMappings::parseLocaleHateosResourceSelection,
            LocaleHateosResource.class, // valueType
            LocaleHateosResourceSet.class, // collectionType
            LocaleHateosResource.class,// resourceType
            SpreadsheetEngineHateosResourceHandlerContext.class // context
        );

        return locales;
    }

    private static HateosResourceSelection<LocaleTag> parseLocaleHateosResourceSelection(final String text,
                                                                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<LocaleTag> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                throw new IllegalArgumentException("Invalid locale selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                    LocaleTag.parse(text)
                );
                break;
        }

        return selection;
    }
}
