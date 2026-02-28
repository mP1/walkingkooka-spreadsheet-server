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

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;

public final class LocaleHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<LocaleLanguageTag,
        LocaleHateosResource,
        LocaleHateosResourceSet,
        LocaleHateosResource,
        LocaleHateosResourceHandlerContext> localeHateosResourceHandlerContext() {

        return HateosResourceMappings.with(
            LocaleHateosResource.HATEOS_RESOURCE_NAME,
            LocaleHateosResourceMappings::parseSelection,
            LocaleHateosResource.class, // valueType
            LocaleHateosResourceSet.class, // collectionType
            LocaleHateosResource.class,// resourceType
            LocaleHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            LocaleHateosResourceHandlerLoad.INSTANCE
        );
    }

    private static HateosResourceSelection<LocaleLanguageTag> parseSelection(final String text,
                                                                             final LocaleHateosResourceHandlerContext context) {
        final HateosResourceSelection<LocaleLanguageTag> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                selection = HateosResourceSelection.all();
                break;
            default:
                selection = HateosResourceSelection.one(
                    LocaleLanguageTag.parse(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private LocaleHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
