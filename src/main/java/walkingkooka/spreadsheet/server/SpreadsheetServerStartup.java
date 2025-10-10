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

package walkingkooka.spreadsheet.server;

import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.SpreadsheetStartup;
import walkingkooka.spreadsheet.server.datetimesymbols.DateTimeSymbolsHateosResourceSet;
import walkingkooka.spreadsheet.server.decimalnumbersymbols.DecimalNumberSymbolsHateosResourceSet;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterMenuList;
import walkingkooka.spreadsheet.server.formatter.SpreadsheetFormatterSelectorEdit;
import walkingkooka.spreadsheet.server.locale.LocaleHateosResourceSet;
import walkingkooka.spreadsheet.server.locale.LocaleTag;
import walkingkooka.spreadsheet.server.meta.SpreadsheetMetadataSet;
import walkingkooka.spreadsheet.server.parser.SpreadsheetParserSelectorEdit;
import walkingkooka.spreadsheet.server.plugin.JarEntryInfoList;
import walkingkooka.tree.json.marshall.JsonNodeContext;

/**
 * Used to force all values types to register their {@link JsonNodeContext#register}
 */
public final class SpreadsheetServerStartup implements PublicStaticHelper {

    static {
        SpreadsheetStartup.init();

        DateTimeSymbolsHateosResourceSet.EMPTY.size();
        DecimalNumberSymbolsHateosResourceSet.EMPTY.size();

        JarEntryInfoList.EMPTY.size();

        LocaleHateosResourceSet.EMPTY.size();
        LocaleTag.parse("en-AU");

        SpreadsheetMetadataSet.with(
            Sets.empty()
        );

        SpreadsheetFormatterMenuList.with(Lists.empty());

        try {
            SpreadsheetFormatterSelectorEdit.parse(
                null,
                null
            );
        } catch (final NullPointerException ignore) {
            // nop
        }

        try {
            SpreadsheetParserSelectorEdit.parse(
                null,
                null
            );
        } catch (final NullPointerException ignore) {
            // nop
        }
    }

    public static void init() {
        // NOP
    }

    private SpreadsheetServerStartup() {
        throw new UnsupportedOperationException();
    }
}
