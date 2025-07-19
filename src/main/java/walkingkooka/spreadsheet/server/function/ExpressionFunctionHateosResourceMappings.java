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
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionFunctions;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;
import walkingkooka.tree.expression.ExpressionFunctionName;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfo;
import walkingkooka.tree.expression.function.provider.ExpressionFunctionInfoSet;

public final class ExpressionFunctionHateosResourceMappings implements PublicStaticHelper {

    // function.......................................................................................................

    public static HateosResourceMappings<ExpressionFunctionName,
        ExpressionFunctionInfo,
        ExpressionFunctionInfoSet,
        ExpressionFunctionInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProviderHateosResourceHandlerContext() {

        // function GET...............................................................................................

        return HateosResourceMappings.with(
                HATEOS_RESOURCE_NAME,
                ExpressionFunctionHateosResourceMappings::parseSelection,
                ExpressionFunctionInfo.class, // valueType
                ExpressionFunctionInfoSet.class, // collectionType
                ExpressionFunctionInfo.class,// resourceType
                SpreadsheetProviderHateosResourceHandlerContext.class // context
            )
            .setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                ExpressionFunctionInfoHateosResourceHandler.INSTANCE
            );
    }

    private static HateosResourceSelection<ExpressionFunctionName> parseSelection(final String text,
                                                                                  final SpreadsheetProviderHateosResourceHandlerContext context) {
        final HateosResourceSelection<ExpressionFunctionName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                throw new IllegalArgumentException("Invalid function selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                    SpreadsheetExpressionFunctions.name(text)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>function</code>.
     */
    public static final HateosResourceName HATEOS_RESOURCE_NAME = HateosResourceName.with("function");

    /**
     * Stop creation
     */
    private ExpressionFunctionHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
