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

package walkingkooka.spreadsheet.server.convert;

import walkingkooka.convert.provider.ConverterInfo;
import walkingkooka.convert.provider.ConverterInfoSet;
import walkingkooka.convert.provider.ConverterName;
import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.server.SpreadsheetHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

public final class ConverterHateosResourceMappings implements PublicStaticHelper {

    // converter.......................................................................................................

    public static HateosResourceMapping<ConverterName,
            ConverterInfo,
            ConverterInfoSet,
            ConverterInfo,
            SpreadsheetHateosResourceHandlerContext> converter() {

        // converter GET...............................................................................................

        HateosResourceMapping<ConverterName,
                ConverterInfo,
                ConverterInfoSet,
                ConverterInfo,
                SpreadsheetHateosResourceHandlerContext> converter = HateosResourceMapping.with(
                CONVERTER,
                ConverterHateosResourceMappings::parseConverterSelection,
                ConverterInfo.class, // valueType
                ConverterInfoSet.class, // collectionType
                ConverterInfo.class,// resourceType
                SpreadsheetHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                ConverterInfoHateosResourceHandler.INSTANCE
        );

        return converter;
    }

    private static HateosResourceSelection<ConverterName> parseConverterSelection(final String text,
                                                                                  final SpreadsheetHateosResourceHandlerContext context) {
        final HateosResourceSelection<ConverterName> selection;

        switch (text) {
            case "":
                selection = HateosResourceSelection.all();
                break;
            case "*":
                throw new IllegalArgumentException("Invalid converter selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                        ConverterName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>converter</code>.
     */
    public static final HateosResourceName CONVERTER = HateosResourceName.with("converter");

    /**
     * Stop creation
     */
    private ConverterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}