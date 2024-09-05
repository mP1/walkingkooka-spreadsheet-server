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

package walkingkooka.spreadsheet.server.export;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.export.SpreadsheetExporterInfo;
import walkingkooka.spreadsheet.export.SpreadsheetExporterInfoSet;
import walkingkooka.spreadsheet.export.SpreadsheetExporterName;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

public final class SpreadsheetExporterHateosResourceMappings implements PublicStaticHelper {

    // exporter.......................................................................................................

    /**
     * A {@link HateosResourceName} with <code>exporter</code>.
     */
    public static final HateosResourceName EXPORTER = HateosResourceName.with("exporter");

    /**
     * Stop creation
     */
    private SpreadsheetExporterHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }

    public static HateosResourceMapping<SpreadsheetExporterName,
            SpreadsheetExporterInfo,
            SpreadsheetExporterInfoSet,
            SpreadsheetExporterInfo,
            SpreadsheetEngineHateosResourceHandlerContext> exporter() {

        // exporter GET...............................................................................................

        HateosResourceMapping<SpreadsheetExporterName,
                SpreadsheetExporterInfo,
                SpreadsheetExporterInfoSet,
                SpreadsheetExporterInfo,
                SpreadsheetEngineHateosResourceHandlerContext> exporter = HateosResourceMapping.with(
                EXPORTER,
                SpreadsheetExporterHateosResourceMappings::parseExporterSelection,
                SpreadsheetExporterInfo.class, // valueType
                SpreadsheetExporterInfoSet.class, // collectionType
                SpreadsheetExporterInfo.class,// resourceType
                SpreadsheetEngineHateosResourceHandlerContext.class // context
        ).setHateosResourceHandler(
                LinkRelation.SELF,
                HttpMethod.GET,
                SpreadsheetExporterInfoHateosResourceHandler.INSTANCE
        );

        return exporter;
    }

    private static HateosResourceSelection<SpreadsheetExporterName> parseExporterSelection(final String text,
                                                                                           final SpreadsheetEngineHateosResourceHandlerContext context) {
        final HateosResourceSelection<SpreadsheetExporterName> selection;

        switch (text) {
            case "":
                selection = HateosResourceSelection.all();
                break;
            case "*":
                throw new IllegalArgumentException("Invalid exporter selection " + CharSequences.quoteAndEscape(text));
            default:
                selection = HateosResourceSelection.one(
                        SpreadsheetExporterName.with(text)
                );
                break;
        }

        return selection;
    }
}