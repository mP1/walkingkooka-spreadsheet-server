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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.RangeBound;
import walkingkooka.collect.set.Sets;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.math.DecimalNumberSymbols;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceHandlerContexts;
import walkingkooka.spreadsheet.server.locale.LocaleTag;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DecimalNumberSymbolsHateosResourceHandlerLoadTest implements HateosResourceHandlerTesting<DecimalNumberSymbolsHateosResourceHandlerLoad, LocaleTag, DecimalNumberSymbolsHateosResource, DecimalNumberSymbolsHateosResourceSet, LocaleHateosResourceHandlerContext> {

    private final static LocaleTag EN_AU = LocaleTag.parse("EN-AU");

    private final static LocaleTag EN_NZ = LocaleTag.parse("EN-NZ");

    @Test
    public void testHandleOneLocaleEnAu() {
        this.handleOneAndCheck(
            EN_AU,
            Optional.empty(),
            DecimalNumberSymbolsHateosResourceHandlerLoad.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                DecimalNumberSymbolsHateosResource.fromLocale(
                    EN_AU.value()
                )
            )
        );
    }

    @Override
    public DecimalNumberSymbolsHateosResourceHandlerLoad createHandler() {
        return DecimalNumberSymbolsHateosResourceHandlerLoad.INSTANCE;
    }

    @Override
    public LocaleTag id() {
        return EN_AU;
    }

    @Override
    public Set<LocaleTag> manyIds() {
        return Sets.of(
            EN_AU,
            EN_NZ
        );
    }

    @Override
    public Range<LocaleTag> range() {
        return Range.with(
            RangeBound.inclusive(
                EN_AU
            ),
            RangeBound.inclusive(
                EN_NZ
            )
        );
    }

    @Override
    public Optional<DecimalNumberSymbolsHateosResource> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<DecimalNumberSymbolsHateosResourceSet> collectionResource() {
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
    public LocaleHateosResourceHandlerContext context() {
        return LocaleHateosResourceHandlerContexts.basic(
            LocaleContexts.jre(
                EN_AU.value()
            ),
            HateosResourceHandlerContexts.fake()
        );
    }

    @Override
    public Class<DecimalNumberSymbolsHateosResourceHandlerLoad> type() {
        return DecimalNumberSymbolsHateosResourceHandlerLoad.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return DecimalNumberSymbols.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Load";
    }
}
