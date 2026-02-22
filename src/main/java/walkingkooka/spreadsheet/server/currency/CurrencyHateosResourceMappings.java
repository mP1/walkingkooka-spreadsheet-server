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

package walkingkooka.spreadsheet.server.currency;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;

public final class CurrencyHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<CurrencyCode,
        CurrencyHateosResource,
        CurrencyHateosResourceSet,
        CurrencyHateosResource,
        CurrencyHateosResourceHandlerContext> currencyHateosResourceHandlerContext() {

        return HateosResourceMappings.with(
            CurrencyHateosResource.HATEOS_RESOURCE_NAME,
            CurrencyHateosResourceMappings::parseSelection,
            CurrencyHateosResource.class, // valueType
            CurrencyHateosResourceSet.class, // collectionType
            CurrencyHateosResource.class,// resourceType
            CurrencyHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            CurrencyHateosResourceHandlerLoad.INSTANCE
        );
    }

    private static HateosResourceSelection<CurrencyCode> parseSelection(final String text,
                                                                        final CurrencyHateosResourceHandlerContext context) {
        final HateosResourceSelection<CurrencyCode> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                selection = HateosResourceSelection.all();
                break;
            default:
                selection = HateosResourceSelection.one(
                    CurrencyCode.parse(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private CurrencyHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
