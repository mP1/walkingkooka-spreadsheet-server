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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.Range;
import walkingkooka.collect.RangeBound;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.locale.LocaleContexts;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlQueryParameters;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class LocaleHateosResourceHandlerLoadTest implements HateosResourceHandlerTesting<LocaleHateosResourceHandlerLoad, LocaleTag, LocaleHateosResource, LocaleHateosResourceSet, LocaleHateosResourceHandlerContext> {

    private final static LocaleTag EN_AU = LocaleTag.parse("EN-AU");

    private final static LocaleTag EN_NZ = LocaleTag.parse("EN-NZ");

    @Test
    public void testHandleOneLocaleEnAu() {
        this.handleOneAndCheck(
            EN_AU,
            Optional.empty(),
            LocaleHateosResourceHandlerLoad.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                LocaleHateosResource.fromLocale(
                    EN_AU.value()
                )
            )
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
            Optional.empty(),
            Maps.of(
                SpreadsheetUrlQueryParameters.COUNT, Lists.of("10000")
            ),
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                LocaleHateosResourceSet.EMPTY.concatAll(
                    Arrays.stream(Locale.getAvailableLocales())
                        .filter((l) -> false == l.getDisplayName().isEmpty())
                        .map(LocaleHateosResource::fromLocale)
                        .collect(Collectors.toList())
                )
            )
        );
    }

    @Test
    public void testHandleAllWithOffsetAndCount() {
        this.handleAllAndCheck(
            Optional.empty(),
            Maps.of(
                SpreadsheetUrlQueryParameters.OFFSET, Lists.of("130"),
                SpreadsheetUrlQueryParameters.COUNT, Lists.of("2")
            ),
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                LocaleHateosResourceSet.EMPTY.concat(
                        LocaleHateosResource.fromLocale(
                            Locale.forLanguageTag("el-GR")
                        )
                ).concat(
                    LocaleHateosResource.fromLocale(
                        Locale.forLanguageTag("en")
                    )
                )
            )
        );
    }

    @Test
    public void testHandleMany() {
        this.handleManyAndCheck(
            Sets.of(
                EN_AU,
                EN_NZ
            ),
            Optional.empty(),
            LocaleHateosResourceHandlerLoad.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                LocaleHateosResourceSet.EMPTY.concat(
                    LocaleHateosResource.fromLocale(
                        EN_AU.value()
                    )
                ).concat(
                    LocaleHateosResource.fromLocale(
                        EN_NZ.value()
                    )
                )
            )
        );
    }

    @Override
    public LocaleHateosResourceHandlerLoad createHandler() {
        return LocaleHateosResourceHandlerLoad.INSTANCE;
    }

    @Override
    public LocaleTag id() {
        return EN_AU;
    }

    @Override
    public Set<LocaleTag> manyIds() {
        return Set.of(
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
    public Optional<LocaleHateosResource> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<LocaleHateosResourceSet> collectionResource() {
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
                Locale.forLanguageTag("EN-AU")
            ),
            HateosResourceHandlerContexts.fake()
        );
    }

    @Override
    public Class<LocaleHateosResourceHandlerLoad> type() {
        return LocaleHateosResourceHandlerLoad.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }

    @Override
    public String typeNamePrefix() {
        return Locale.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Load";
    }
}
