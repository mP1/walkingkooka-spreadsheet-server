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

package walkingkooka.spreadsheet.server.datetimesymbols;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleTag;

public final class DateTimeSymbolsHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<LocaleTag,
        DateTimeSymbolsHateosResource,
        DateTimeSymbolsHateosResourceSet,
        DateTimeSymbolsHateosResource,
        LocaleHateosResourceHandlerContext> mappings() {

        return HateosResourceMappings.with(
            DateTimeSymbolsHateosResource.HATEOS_RESOURCE_NAME,
            DateTimeSymbolsHateosResourceMappings::parseSelection,
            DateTimeSymbolsHateosResource.class, // valueType
            DateTimeSymbolsHateosResourceSet.class, // collectionType
            DateTimeSymbolsHateosResource.class,// resourceType
            LocaleHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            DateTimeSymbolsHateosResourceHandlerLoad.INSTANCE
        );
    }

    private static HateosResourceSelection<LocaleTag> parseSelection(final String text,
                                                                     final LocaleHateosResourceHandlerContext context) {
        final HateosResourceSelection<LocaleTag> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                selection = HateosResourceSelection.all();
                break;
            default:
                selection = HateosResourceSelection.one(
                    LocaleTag.parse(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private DateTimeSymbolsHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
