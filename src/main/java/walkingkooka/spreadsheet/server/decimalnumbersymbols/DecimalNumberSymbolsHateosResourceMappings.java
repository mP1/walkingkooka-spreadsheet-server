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

package walkingkooka.spreadsheet.server.decimalnumbersymbols;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleTag;
import walkingkooka.spreadsheet.server.net.SpreadsheetServerLinkRelations;

public final class DecimalNumberSymbolsHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<LocaleTag,
        DecimalNumberSymbolsHateosResource,
        DecimalNumberSymbolsHateosResourceSet,
        DecimalNumberSymbolsHateosResource,
        LocaleHateosResourceHandlerContext> localeHateosResourceHandlerContext() {

        return HateosResourceMappings.with(
            DecimalNumberSymbolsHateosResource.HATEOS_RESOURCE_NAME,
            DecimalNumberSymbolsHateosResourceMappings::parseSelection,
            DecimalNumberSymbolsHateosResource.class, // valueType
            DecimalNumberSymbolsHateosResourceSet.class, // collectionType
            DecimalNumberSymbolsHateosResource.class,// resourceType
            LocaleHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            DecimalNumberSymbolsHateosResourceHandlerLoad.INSTANCE
        ).setHateosHttpHandler(
            SpreadsheetServerLinkRelations.LOCALE_STARTS_WITH.toUrlPathName()
                .get(),
            DecimalNumberSymbolsFindByLocaleStartsWithHateosHttpHandler.INSTANCE
        );
    }

    private static HateosResourceSelection<LocaleTag> parseSelection(final String text,
                                                                     final LocaleHateosResourceHandlerContext context) {
        final HateosResourceSelection<LocaleTag> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.none();
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
    private DecimalNumberSymbolsHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
