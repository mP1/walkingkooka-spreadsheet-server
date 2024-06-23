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

package walkingkooka.spreadsheet.server.engine;

import walkingkooka.collect.Range;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.spreadsheet.engine.SpreadsheetEngine;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link ExpressionFunctionInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class SpreadsheetEngineHateosResourceHandlerExpressionFunctions extends SpreadsheetEngineHateosResourceHandler<FunctionExpressionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet> {

    static SpreadsheetEngineHateosResourceHandlerExpressionFunctions with(final SpreadsheetEngine engine,
                                                                          final SpreadsheetEngineContext context) {
        check(
                engine,
                context
        );
        return new SpreadsheetEngineHateosResourceHandlerExpressionFunctions(engine, context);
    }

    private SpreadsheetEngineHateosResourceHandlerExpressionFunctions(final SpreadsheetEngine engine,
                                                                      final SpreadsheetEngineContext context) {
        super(engine, context);
    }

    @Override
    public Optional<ExpressionFunctionInfoSet> handleAll(final Optional<ExpressionFunctionInfoSet> infos,
                                                          final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);

        return Optional.of(
                ExpressionFunctionInfoSet.with(
                        this.context.expressionFunctionInfos()
                )
        );
    }

    @Override
    public Optional<ExpressionFunctionInfo> handleOne(final FunctionExpressionName name,
                                                      final Optional<ExpressionFunctionInfo> info,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);

        return this.context.expressionFunctionInfos()
                .stream()
                .filter(i -> i.name().equals(name))
                .findFirst();
    }

    @Override
    public Optional<ExpressionFunctionInfoSet> handleRange(final Range<FunctionExpressionName> nameRange,
                                                           final Optional<ExpressionFunctionInfoSet> infos,
                                                            final Map<HttpRequestAttribute<?>, Object> parameters) {
        HateosResourceHandler.checkIdRange(nameRange);
        HateosResourceHandler.checkResource(infos);
        HateosResourceHandler.checkParameters(parameters);

        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "SpreadsheetEngineContext.expressionFunctionInfos";
    }
}
