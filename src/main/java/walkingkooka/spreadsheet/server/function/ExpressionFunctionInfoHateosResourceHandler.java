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

import walkingkooka.net.UrlPath;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleMany;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleRange;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;

import java.util.Map;
import java.util.Optional;

/**
 * Provides a single end point to retrieve ALL the {@link ExpressionFunctionInfo} available to this spreadsheet.
 * GETS for individual or a range are not supported and throw {@link UnsupportedOperationException}.
 */
final class ExpressionFunctionInfoHateosResourceHandler implements HateosResourceHandler<ExpressionFunctionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet, SpreadsheetProviderHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleMany<ExpressionFunctionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet, SpreadsheetProviderHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleNone<ExpressionFunctionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet, SpreadsheetProviderHateosResourceHandlerContext>,
    UnsupportedHateosResourceHandlerHandleRange<ExpressionFunctionName, ExpressionFunctionInfo, ExpressionFunctionInfoSet, SpreadsheetProviderHateosResourceHandlerContext> {

    final static ExpressionFunctionInfoHateosResourceHandler INSTANCE = new ExpressionFunctionInfoHateosResourceHandler();

    private ExpressionFunctionInfoHateosResourceHandler() {
        super();
    }

    @Override
    public Optional<ExpressionFunctionInfoSet> handleAll(final Optional<ExpressionFunctionInfoSet> infos,
                                                         final Map<HttpRequestAttribute<?>, Object> parameters,
                                                         final UrlPath path,
                                                         final SpreadsheetProviderHateosResourceHandlerContext context) {
        HateosResourceHandler.checkResourceEmpty(infos);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return Optional.of(
            SpreadsheetExpressionFunctions.infoSet(
                context.spreadsheetProvider()
                    .expressionFunctionInfos()
            )
        );
    }

    @Override
    public Optional<ExpressionFunctionInfo> handleOne(final ExpressionFunctionName name,
                                                      final Optional<ExpressionFunctionInfo> info,
                                                      final Map<HttpRequestAttribute<?>, Object> parameters,
                                                      final UrlPath path,
                                                      final SpreadsheetProviderHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(name);
        HateosResourceHandler.checkResource(info);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkPathEmpty(path);
        HateosResourceHandler.checkContext(context);

        return context.spreadsheetProvider()
            .expressionFunctionInfos()
            .stream()
            .filter(i -> i.name().equals(name))
            .findFirst();
    }

    @Override
    public String toString() {
        return "SpreadsheetProvider.expressionFunctionInfos";
    }
}
