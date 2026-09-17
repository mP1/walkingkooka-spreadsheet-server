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

package walkingkooka.spreadsheet.server.currencyexchangerate;

import walkingkooka.currency.provider.CurrencyExchangeRaterInfo;
import walkingkooka.currency.provider.CurrencyExchangeRaterInfoSet;
import walkingkooka.currency.provider.CurrencyExchangeRaterName;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosHandlerContext;

public final class CurrencyExchangeRaterHateosResourceMappings implements PublicStaticHelper {

    public final static HateosResourceName HATEOS_RESOURCE_NAME = CurrencyExchangeRaterName.HATEOS_RESOURCE_NAME;

    public static HateosResourceMappings<CurrencyExchangeRaterName,
        CurrencyExchangeRaterInfo,
        CurrencyExchangeRaterInfoSet,
        CurrencyExchangeRaterInfo,
        SpreadsheetProviderHateosHandlerContext> spreadsheetProviderHateosHandlerContext() {

        return HateosResourceMappings.with(
            HATEOS_RESOURCE_NAME,
            (final String text, final SpreadsheetProviderHateosHandlerContext context) -> HateosResourceSelection.parseOneOrAll(
                text,
                CurrencyExchangeRaterName::with
            ),
            CurrencyExchangeRaterInfo.class, // valueType
            CurrencyExchangeRaterInfoSet.class, // collectionType
            CurrencyExchangeRaterInfo.class,// resourceType
            SpreadsheetProviderHateosHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            CurrencyExchangeRaterInfoHateosResourceHandler.INSTANCE
        );
    }

    /**
     * Stop creation
     */
    private CurrencyExchangeRaterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
