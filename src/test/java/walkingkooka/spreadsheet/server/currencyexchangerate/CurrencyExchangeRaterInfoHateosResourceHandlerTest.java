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

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.set.Sets;
import walkingkooka.currency.provider.CurrencyExchangeRaterInfo;
import walkingkooka.currency.provider.CurrencyExchangeRaterInfoSet;
import walkingkooka.currency.provider.CurrencyExchangeRaterName;
import walkingkooka.net.Url;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting2;
import walkingkooka.spreadsheet.provider.FakeSpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.server.FakeSpreadsheetProviderHateosHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosHandlerContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CurrencyExchangeRaterInfoHateosResourceHandlerTest implements HateosResourceHandlerTesting2<CurrencyExchangeRaterInfoHateosResourceHandler,
    CurrencyExchangeRaterName,
    CurrencyExchangeRaterInfo,
    CurrencyExchangeRaterInfoSet,
    SpreadsheetProviderHateosHandlerContext>,
    ToStringTesting<CurrencyExchangeRaterInfoHateosResourceHandler> {

    // hateos...........................................................................................................

    private final static CurrencyExchangeRaterInfo INFO1 = CurrencyExchangeRaterInfo.with(
        Url.parseAbsolute("https://example.com/1"),
        CurrencyExchangeRaterName.with("currency-exchange-rater-1")
    );

    private final static CurrencyExchangeRaterInfo INFO2 = CurrencyExchangeRaterInfo.with(
        Url.parseAbsolute("https://example.com/2"),
        CurrencyExchangeRaterName.with("currency-exchange-rater-2")
    );

    private final static SpreadsheetProviderHateosHandlerContext CONTEXT = new FakeSpreadsheetProviderHateosHandlerContext() {

        @Override
        public SpreadsheetProvider spreadsheetProvider() {
            return new FakeSpreadsheetProvider() {
                @Override
                public CurrencyExchangeRaterInfoSet currencyExchangeRaterInfos() {
                    return CurrencyExchangeRaterInfoSet.EMPTY.setElements(
                        Sets.of(
                            INFO1,
                            INFO2
                        )
                    );
                }
            };
        }
    };

    @Test
    public void testHandleOne() {
        this.handleOneAndCheck(
            INFO1.name(),
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            CONTEXT,
            Optional.of(INFO1)
        );
    }

    @Test
    public void testHandleOneNotFound() {
        this.handleOneAndCheck(
            CurrencyExchangeRaterName.with("unknown"),
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            CONTEXT,
            Optional.empty()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            CONTEXT,
            Optional.of(
                CurrencyExchangeRaterInfoSet.EMPTY.setElements(
                    Sets.of(
                        INFO1,
                        INFO2
                    )
                )
            )
        );
    }

    @Override
    public CurrencyExchangeRaterInfoHateosResourceHandler createHandler() {
        return CurrencyExchangeRaterInfoHateosResourceHandler.INSTANCE;
    }

    @Override
    public CurrencyExchangeRaterName id() {
        return CurrencyExchangeRaterName.with("id-currency-exchange-rater-name");
    }

    @Override
    public Set<CurrencyExchangeRaterName> manyIds() {
        return Sets.of(
            INFO1.name(),
            INFO2.name()
        );
    }

    @Override
    public Range<CurrencyExchangeRaterName> range() {
        return Range.singleton(
            CurrencyExchangeRaterName.with("range-currency-exchange-rater-name")
        );
    }

    @Override
    public Optional<CurrencyExchangeRaterInfo> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<CurrencyExchangeRaterInfoSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return HateosResourceHandler.NO_PARAMETERS;
    }

    @Override
    public UrlPath path() {
        return UrlPath.EMPTY;
    }

    @Override
    public SpreadsheetProviderHateosHandlerContext context() {
        return CONTEXT;
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            "spreadsheetProvider.currencyExchangeRaterInfos"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return CurrencyExchangeRaterInfo.class.getSimpleName();
    }

    // Class............................................................................................................

    @Override
    public Class<CurrencyExchangeRaterInfoHateosResourceHandler> type() {
        return CurrencyExchangeRaterInfoHateosResourceHandler.class;
    }
}
