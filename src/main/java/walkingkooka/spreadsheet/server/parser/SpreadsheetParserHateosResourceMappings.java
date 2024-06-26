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

package walkingkooka.spreadsheet.server.parser;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.format.SpreadsheetParserInfo;
import walkingkooka.spreadsheet.format.SpreadsheetParserInfoSet;
import walkingkooka.spreadsheet.format.SpreadsheetParserName;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

import java.util.Objects;

public final class SpreadsheetParserHateosResourceMappings implements PublicStaticHelper {

    // parser.......................................................................................................

    public static HateosResourceMapping<SpreadsheetParserName,
            SpreadsheetParserInfo,
            SpreadsheetParserInfoSet,
            SpreadsheetParserInfo,
            SpreadsheetEngineHateosResourceHandlerContext> parser(final SpreadsheetEngineContext context) {
        Objects.requireNonNull(context, "context");

        // parser GET...............................................................................................

        HateosResourceMapping<SpreadsheetParserName,
                SpreadsheetParserInfo,
                SpreadsheetParserInfoSet,
                SpreadsheetParserInfo,
                SpreadsheetEngineHateosResourceHandlerContext> parser = HateosResourceMapping.with(
                PARSER,
                SpreadsheetParserHateosResourceMappings::parseParserSelection,
                SpreadsheetParserInfo.class, // valueType
                SpreadsheetParserInfoSet.class, // collectionType
                SpreadsheetParserInfo.class, // resourceType
                SpreadsheetEngineHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                SpreadsheetParserInfoHateosResourceHandler.with(context)
        );

        return parser;
    }

    private static HateosResourceSelection<SpreadsheetParserName> parseParserSelection(final String text,
                                                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetParserName> selection;

        switch (text) {
            case "":
                selection = HateosResourceSelection.all();
                break;
            case "*":
                throw new IllegalArgumentException("Invalid parser selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                        SpreadsheetParserName.with(text)
                );
                break;
        }

        return selection;
    }

    /**
     * A {@link HateosResourceName} with <code>parser</code>.
     */
    private static final HateosResourceName PARSER = HateosResourceName.with("parser");

    /**
     * Stop creation
     */
    private SpreadsheetParserHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
