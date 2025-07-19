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

package walkingkooka.spreadsheet.server.formhandler;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;
import walkingkooka.validation.form.provider.FormHandlerInfo;
import walkingkooka.validation.form.provider.FormHandlerInfoSet;
import walkingkooka.validation.form.provider.FormHandlerName;

public final class FormHandlerHateosResourceMappings implements PublicStaticHelper {

    public static HateosResourceMappings<FormHandlerName,
        FormHandlerInfo,
        FormHandlerInfoSet,
        FormHandlerInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProviderHateosResourceHandlerContext() {

        // formHandler GET...............................................................................................

        return HateosResourceMappings.with(
            FormHandlerName.HATEOS_RESOURCE_NAME,
            FormHandlerHateosResourceMappings::parseFormHandlerSelection,
            FormHandlerInfo.class, // valueType
            FormHandlerInfoSet.class, // collectionType
            FormHandlerInfo.class,// resourceType
            SpreadsheetProviderHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            FormHandlerInfoHateosResourceHandler.INSTANCE
        );
    }

    private static HateosResourceSelection<FormHandlerName> parseFormHandlerSelection(final String text,
                                                                                      final SpreadsheetProviderHateosResourceHandlerContext context) {
        final HateosResourceSelection<FormHandlerName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                throw new IllegalArgumentException("Invalid formHandler selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                    FormHandlerName.with(text)
                );
                break;
        }

        return selection;
    }
    
    /**
     * Stop creation
     */
    private FormHandlerHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
