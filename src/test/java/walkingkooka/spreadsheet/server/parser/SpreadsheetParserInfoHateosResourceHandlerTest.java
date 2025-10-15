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

package walkingkooka.spreadsheet.server.parser;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.Url;
import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.parser.provider.SpreadsheetParserName;
import walkingkooka.spreadsheet.provider.FakeSpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.server.FakeSpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpreadsheetParserInfoHateosResourceHandlerTest implements HateosResourceHandlerTesting<SpreadsheetParserInfoHateosResourceHandler,
    SpreadsheetParserName,
    SpreadsheetParserInfo,
    SpreadsheetParserInfoSet,
    SpreadsheetProviderHateosResourceHandlerContext>,
    ToStringTesting<SpreadsheetParserInfoHateosResourceHandler> {

    // hateos...........................................................................................................

    private final static SpreadsheetParserInfo INFO1 = SpreadsheetParserInfo.with(
        Url.parseAbsolute("https://example.com/1"),
        SpreadsheetParserName.with("parser-1")
    );

    private final static SpreadsheetParserInfo INFO2 = SpreadsheetParserInfo.with(
        Url.parseAbsolute("https://example.com/2"),
        SpreadsheetParserName.with("parser-2")
    );

    private final static SpreadsheetProviderHateosResourceHandlerContext CONTEXT = new FakeSpreadsheetProviderHateosResourceHandlerContext() {

        @Override
        public SpreadsheetProvider spreadsheetProvider() {
            return new FakeSpreadsheetProvider() {
                @Override
                public SpreadsheetParserInfoSet spreadsheetParserInfos() {
                    return SpreadsheetParserInfoSet.with(
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
            this.context(),
            Optional.of(INFO1)
        );
    }

    @Test
    public void testHandleOneNotFound() {
        this.handleOneAndCheck(
            SpreadsheetParserName.with("unknown"),
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.empty()
        );
    }

    @Test
    public void testHandleAll() {
        this.handleAllAndCheck(
            Optional.empty(), // resource
            HateosResourceHandler.NO_PARAMETERS,
            UrlPath.EMPTY,
            this.context(),
            Optional.of(
                SpreadsheetParserInfoSet.with(
                    Sets.of(
                        INFO1,
                        INFO2
                    )
                )
            )
        );
    }

    @Override
    public SpreadsheetParserInfoHateosResourceHandler createHandler() {
        return SpreadsheetParserInfoHateosResourceHandler.INSTANCE;
    }

    @Override
    public SpreadsheetParserName id() {
        return SpreadsheetParserName.with("id-spreadsheet-parser-name");
    }

    @Override
    public Set<SpreadsheetParserName> manyIds() {
        return Sets.of(
            INFO1.name(),
            INFO2.name()
        );
    }

    @Override
    public Range<SpreadsheetParserName> range() {
        return Range.singleton(
            SpreadsheetParserName.with("range-spreadsheet-parser-name")
        );
    }

    @Override
    public Optional<SpreadsheetParserInfo> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<SpreadsheetParserInfoSet> collectionResource() {
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
    public SpreadsheetProviderHateosResourceHandlerContext context() {
        return CONTEXT;
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            this.createHandler(),
            "SpreadsheetProvider.spreadsheetParserInfos"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetParserInfo.class.getSimpleName();
    }

    // Class............................................................................................................

    @Override
    public Class<SpreadsheetParserInfoHateosResourceHandler> type() {
        return SpreadsheetParserInfoHateosResourceHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
