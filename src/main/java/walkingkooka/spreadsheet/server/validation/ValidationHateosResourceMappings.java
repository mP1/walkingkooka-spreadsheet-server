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

package walkingkooka.spreadsheet.server.validation;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMappings;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.SpreadsheetProviderHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;
import walkingkooka.validation.provider.ValidatorInfo;
import walkingkooka.validation.provider.ValidatorInfoSet;
import walkingkooka.validation.provider.ValidatorName;

public final class ValidationHateosResourceMappings implements PublicStaticHelper {

    // validator.......................................................................................................

    public static HateosResourceMappings<ValidatorName,
        ValidatorInfo,
        ValidatorInfoSet,
        ValidatorInfo,
        SpreadsheetProviderHateosResourceHandlerContext> spreadsheetProviderHateosResourceHandlerContext() {

        // validator GET...............................................................................................

        return HateosResourceMappings.with(
            ValidatorName.HATEOS_RESOURCE_NAME,
            ValidationHateosResourceMappings::parseSelection,
            ValidatorInfo.class, // valueType
            ValidatorInfoSet.class, // collectionType
            ValidatorInfo.class,// resourceType
            SpreadsheetProviderHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
            LinkRelation.SELF,
            HttpMethod.GET,
            ValidatorInfoHateosResourceHandler.INSTANCE
        );
    }

    private static HateosResourceSelection<ValidatorName> parseSelection(final String text,
                                                                         final SpreadsheetProviderHateosResourceHandlerContext context) {
        final HateosResourceSelection<ValidatorName> selection;

        switch (text) {
            case HateosResourceSelection.NONE:
                selection = HateosResourceSelection.all();
                break;
            case HateosResourceSelection.ALL:
                throw new IllegalArgumentException("Invalid validator selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                    ValidatorName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * Stop creation
     */
    private ValidationHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
