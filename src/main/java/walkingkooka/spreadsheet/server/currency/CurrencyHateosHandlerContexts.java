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

import walkingkooka.currency.CurrencyContext;
import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.reflect.PublicStaticHelper;

public final class CurrencyHateosHandlerContexts implements PublicStaticHelper {

    /**
     * {@see BasicCurrencyHateosHandlerContext}
     */
    public static CurrencyHateosHandlerContext basic(final CurrencyContext currencyContext,
                                                     final HateosHandlerContext hateosHandlerContext) {
        return BasicCurrencyHateosHandlerContext.with(
            currencyContext,
            hateosHandlerContext
        );
    }

    /**
     * {@see FakeCurrencyHateosHandlerContext}
     */
    public static FakeCurrencyHateosHandlerContext fake() {
        return new FakeCurrencyHateosHandlerContext();
    }

    /**
     * Stop creation
     */
    private CurrencyHateosHandlerContexts() {
        throw new UnsupportedOperationException();
    }
}
