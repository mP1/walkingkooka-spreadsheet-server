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

package walkingkooka.spreadsheet.server.formatter;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.FakeSpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContexts;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatterInfoHateosResourceHandlerTest implements HateosResourceHandlerTesting<SpreadsheetFormatterInfoHateosResourceHandler,
        SpreadsheetFormatterName,
        SpreadsheetFormatterInfo,
        SpreadsheetFormatterInfoSet,
        SpreadsheetEngineHateosResourceHandlerContext>,
        ToStringTesting<SpreadsheetFormatterInfoHateosResourceHandler> {

    @Test
    public void testWithNullContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> SpreadsheetFormatterInfoHateosResourceHandler.with(null)
        );
    }

    // hateos...........................................................................................................

    private final static SpreadsheetFormatterInfo INFO1 = SpreadsheetFormatterInfo.with(
            Url.parseAbsolute("https://example.com/1"),
            SpreadsheetFormatterName.with("formatter-1")
    );

    private final static SpreadsheetFormatterInfo INFO2 = SpreadsheetFormatterInfo.with(
            Url.parseAbsolute("https://example.com/2"),
            SpreadsheetFormatterName.with("formatter-2")
    );

    private final static FakeSpreadsheetEngineContext CONTEXT = new FakeSpreadsheetEngineContext() {

        @Override
        public Set<SpreadsheetFormatterInfo> spreadsheetFormatterInfos() {
            return Sets.of(
                    INFO1,
                    INFO2
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
                SpreadsheetFormatterName.with("Unknown"),
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
                        SpreadsheetFormatterInfoSet.with(
                                Sets.of(
                                        INFO1,
                                        INFO2
                                )
                        )
                )
        );
    }

    @Override
    public SpreadsheetFormatterInfoHateosResourceHandler createHandler() {
        return SpreadsheetFormatterInfoHateosResourceHandler.with(CONTEXT);
    }

    @Override
    public SpreadsheetFormatterName id() {
        return SpreadsheetFormatterName.with("id-spreadsheet-formatter-name");
    }

    @Override
    public Set<SpreadsheetFormatterName> manyIds() {
        return Sets.of(
                INFO1.name(),
                INFO2.name()
        );
    }

    @Override
    public Range<SpreadsheetFormatterName> range() {
        return Range.singleton(
                SpreadsheetFormatterName.with("range-spreadsheet-formatter-name")
        );
    }

    @Override
    public Optional<SpreadsheetFormatterInfo> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetFormatterInfoSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return SpreadsheetEngineHateosResourceHandlerContexts.fake();
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                "SpreadsheetEngineContext.spreadsheetFormatterInfos"
        );
    }

    // Class............................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetFormatterInfo.class.getSimpleName();
    }

    @Override
    public Class<SpreadsheetFormatterInfoHateosResourceHandler> type() {
        return SpreadsheetFormatterInfoHateosResourceHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
