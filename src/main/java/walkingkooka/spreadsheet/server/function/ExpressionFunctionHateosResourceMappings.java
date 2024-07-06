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

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.expression.FunctionExpressionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;

public final class ExpressionFunctionHateosResourceMappings implements PublicStaticHelper {

    // function.......................................................................................................

    public static HateosResourceMapping<FunctionExpressionName,
            ExpressionFunctionInfo,
            ExpressionFunctionInfoSet,
            ExpressionFunctionInfo,
            SpreadsheetEngineHateosResourceHandlerContext> function() {

        // function GET...............................................................................................

        HateosResourceMapping<FunctionExpressionName,
                ExpressionFunctionInfo,
                ExpressionFunctionInfoSet,
                ExpressionFunctionInfo,
                SpreadsheetEngineHateosResourceHandlerContext> function = HateosResourceMapping.with(
                        FUNCTION,
                        ExpressionFunctionHateosResourceMappings::parseFunctionSelection,
                        ExpressionFunctionInfo.class, // valueType
                        ExpressionFunctionInfoSet.class, // collectionType
                        ExpressionFunctionInfo.class,// resourceType
                        SpreadsheetEngineHateosResourceHandlerContext.class // context
                )
                .setHateosResourceHandler(
                        LinkRelation.SELF,
                        HttpMethod.GET,
                        ExpressionFunctionInfoHateosResourceHandler.INSTANCE
                );

        return function;
    }

    private static HateosResourceSelection<FunctionExpressionName> parseFunctionSelection(final String text,
                                                                                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<FunctionExpressionName> selection;

        switch (text) {
            case "":
                selection = HateosResourceSelection.all();
                break;
            case "*":
                throw new IllegalArgumentException("Invalid function selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                        FunctionExpressionName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>function</code>.
     */
    private static final HateosResourceName FUNCTION = HateosResourceName.with("expression-function");

    /**
     * Stop creation
     */
    private ExpressionFunctionHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
