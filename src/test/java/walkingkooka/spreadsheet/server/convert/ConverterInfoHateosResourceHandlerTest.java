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

package walkingkooka.spreadsheet.server.convert;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.convert.provider.ConverterInfo;
import walkingkooka.convert.provider.ConverterInfoSet;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.FakeSpreadsheetHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ConverterInfoHateosResourceHandlerTest implements HateosResourceHandlerTesting<ConverterInfoHateosResourceHandler,
        ConverterName,
        ConverterInfo,
        ConverterInfoSet,
        SpreadsheetHateosResourceHandlerContext>,
        ToStringTesting<ConverterInfoHateosResourceHandler> {

    // hateos...........................................................................................................

    private final static ConverterInfo INFO1 = ConverterInfo.with(
            Url.parseAbsolute("https://example.com/1"),
            ConverterName.with("converter-1")
    );

    private final static ConverterInfo INFO2 = ConverterInfo.with(
            Url.parseAbsolute("https://example.com/2"),
            ConverterName.with("converter-2")
    );

    private final static SpreadsheetHateosResourceHandlerContext CONTEXT = new FakeSpreadsheetHateosResourceHandlerContext() {

        @Override
        public ConverterInfoSet converterInfos() {
            return ConverterInfoSet.with(
                    Sets.of(
                            INFO1,
                            INFO2
                    )
            );
        }
    };

    @Test
    public void testHandleOne() {
        this.handleOneAndCheck(
                INFO1.name(),
                Optional.empty(), // resource
                Maps.empty(), // parameters
                this.context(),
                Optional.of(INFO1)
        );
    }

    @Test
    public void testHandleOneNotFound() {
        this.handleOneAndCheck(
                ConverterName.with("Unknown"),
                Optional.empty(), // resource
                Maps.empty(), // parameters
                this.context(),
                Optional.empty()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
                Optional.empty(), // resource
                Maps.empty(), // parameters
                this.context(),
                Optional.of(
                        ConverterInfoSet.with(
                                Sets.of(
                                        INFO1,
                                        INFO2
                                )
                        )
                )
        );
    }

    @Override
    public ConverterInfoHateosResourceHandler createHandler() {
        return ConverterInfoHateosResourceHandler.INSTANCE;
    }

    @Override
    public ConverterName id() {
        return ConverterName.with("id-converter-name");
    }

    @Override
    public Set<ConverterName> manyIds() {
        return Sets.of(
                INFO1.name(),
                INFO2.name()
        );
    }

    @Override
    public Range<ConverterName> range() {
        return Range.singleton(
                ConverterName.with("range-converter-name")
        );
    }

    @Override
    public Optional<ConverterInfo> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<ConverterInfoSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetHateosResourceHandlerContext context() {
        return CONTEXT;
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                "SpreadsheetEngineContext.converterInfos"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return ConverterInfo.class.getSimpleName();
    }

    // Class............................................................................................................

    @Override
    public Class<ConverterInfoHateosResourceHandler> type() {
        return ConverterInfoHateosResourceHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
