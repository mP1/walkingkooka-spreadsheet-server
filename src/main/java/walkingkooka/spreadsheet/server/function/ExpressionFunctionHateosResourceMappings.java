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
import walkingkooka.spreadsheet.SpreadsheetExpressionFunctionNames;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;

public final class ExpressionFunctionHateosResourceMappings implements PublicStaticHelper {

    // function.......................................................................................................

    public static HateosResourceMapping<ExpressionFunctionName,
            ExpressionFunctionInfo,
            ExpressionFunctionInfoSet,
            ExpressionFunctionInfo,
            SpreadsheetEngineHateosResourceHandlerContext> function() {

        // function GET...............................................................................................

        HateosResourceMapping<ExpressionFunctionName,
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

    private static HateosResourceSelection<ExpressionFunctionName> parseFunctionSelection(final String text,
                                                                                          final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<ExpressionFunctionName> selection;

        switch (text) {
            case "":
                selection = HateosResourceSelection.all();
                break;
            case "*":
                throw new IllegalArgumentException("Invalid function selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                        ExpressionFunctionName.with(text)
                                .setCaseSensitivity(SpreadsheetExpressionFunctionNames.CASE_SENSITIVITY)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>function</code>.
     */
    public static final HateosResourceName FUNCTION = HateosResourceName.with("function");

    /**
     * Stop creation
     */
    private ExpressionFunctionHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
