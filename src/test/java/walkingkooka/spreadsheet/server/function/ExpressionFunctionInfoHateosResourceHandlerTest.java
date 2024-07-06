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

package walkingkooka.spreadsheet.server.function;

import org.junit.jupiter.api.Test;
import walkingkooka.ToStringTesting;
import walkingkooka.collect.Range;
import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.server.engine.FakeSpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ExpressionFunctionInfoHateosResourceHandlerTest implements HateosResourceHandlerTesting<ExpressionFunctionInfoHateosResourceHandler,
        FunctionExpressionName,
        ExpressionFunctionInfo,
        ExpressionFunctionInfoSet,
        SpreadsheetEngineHateosResourceHandlerContext>,
        ToStringTesting<ExpressionFunctionInfoHateosResourceHandler> {

    // hateos...........................................................................................................

    private final static ExpressionFunctionInfo INFO1 = ExpressionFunctionInfo.with(
            Url.parseAbsolute("https://example.com/1"),
            FunctionExpressionName.with("function-1")
    );

    private final static ExpressionFunctionInfo INFO2 = ExpressionFunctionInfo.with(
            Url.parseAbsolute("https://example.com/2"),
            FunctionExpressionName.with("function-2")
    );

    private final static SpreadsheetEngineHateosResourceHandlerContext CONTEXT = new FakeSpreadsheetEngineHateosResourceHandlerContext() {

        @Override
        public Set<ExpressionFunctionInfo> expressionFunctionInfos() {
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
                FunctionExpressionName.with("Unknown"),
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
                        ExpressionFunctionInfoSet.with(
                                Sets.of(
                                        INFO1,
                                        INFO2
                                )
                        )
                )
        );
    }

    @Override
    public ExpressionFunctionInfoHateosResourceHandler createHandler() {
        return ExpressionFunctionInfoHateosResourceHandler.INSTANCE;
    }

    @Override
    public FunctionExpressionName id() {
        return FunctionExpressionName.with("id-spreadsheet-function-name");
    }

    @Override
    public Set<FunctionExpressionName> manyIds() {
        return Sets.of(
                INFO1.name(),
                INFO2.name()
        );
    }

    @Override
    public Range<FunctionExpressionName> range() {
        return Range.singleton(
                FunctionExpressionName.with("range-spreadsheet-function-name")
        );
    }

    @Override
    public Optional<ExpressionFunctionInfo> resource() {
        return Optional.empty();
    }

    @Override
    public Optional<ExpressionFunctionInfoSet> collectionResource() {
        return Optional.empty();
    }

    @Override
    public Map<HttpRequestAttribute<?>, Object> parameters() {
        return Maps.empty();
    }

    @Override
    public SpreadsheetEngineHateosResourceHandlerContext context() {
        return CONTEXT;
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
                this.createHandler(),
                "SpreadsheetEngineContext.expressionFunctionInfos"
        );
    }

    // type naming......................................................................................................

    @Override
    public String typeNamePrefix() {
        return ExpressionFunctionInfo.class.getSimpleName();
    }

    // Class............................................................................................................

    @Override
    public Class<ExpressionFunctionInfoHateosResourceHandler> type() {
        return ExpressionFunctionInfoHateosResourceHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
