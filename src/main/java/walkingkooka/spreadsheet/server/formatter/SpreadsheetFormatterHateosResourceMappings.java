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

package walkingkooka.spreadsheet.server.formatter;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfo;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Objects;

public final class SpreadsheetFormatterHateosResourceMappings implements PublicStaticHelper {

    // formatter.......................................................................................................

    public static HateosResourceMapping<SpreadsheetFormatterName,
            SpreadsheetFormatterInfo,
            SpreadsheetFormatterInfoSet,
            SpreadsheetFormatterInfo,
            SpreadsheetEngineHateosResourceHandlerContext> formatter(final SpreadsheetEngineContext context) {
        Objects.requireNonNull(context, "context");

        // formatter GET...............................................................................................

        HateosResourceMapping<SpreadsheetFormatterName,
                SpreadsheetFormatterInfo,
                SpreadsheetFormatterInfoSet,
                SpreadsheetFormatterInfo,
                SpreadsheetEngineHateosResourceHandlerContext> formatter = HateosResourceMapping.with(
                FORMATTER,
                SpreadsheetFormatterHateosResourceMappings::parseFormatterSelection,
                SpreadsheetFormatterInfo.class, // valueType
                SpreadsheetFormatterInfoSet.class, // collectionType
                SpreadsheetFormatterInfo.class,// resourceType
                SpreadsheetEngineHateosResourceHandlerContext.class
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                SpreadsheetFormatterInfoHateosResourceHandler.with(context)
        ).setHateosHttpEntityHandler(
                LinkRelation.with("format"),
                HttpMethod.POST,
                SpreadsheetFormatterFormatHateosHttpEntityHandler.instance()
        );

        return formatter;
    }

    private static HateosResourceSelection<SpreadsheetFormatterName> parseFormatterSelection(final String text,
                                                                                             final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetFormatterName> selection;

        switch (text) {
            case "":
                //GET /formatters
                selection = HateosResourceSelection.all();
                break;
            case "*":
                //POST /formatters/*/format
                selection = HateosResourceSelection.all();
                break;
            default:
                selection = HateosResourceSelection.one(
                        SpreadsheetFormatterName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>formatter</code>.
     */
    private static final HateosResourceName FORMATTER = HateosResourceName.with("formatter");

    /**
     * Stop creation
     */
    private SpreadsheetFormatterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
