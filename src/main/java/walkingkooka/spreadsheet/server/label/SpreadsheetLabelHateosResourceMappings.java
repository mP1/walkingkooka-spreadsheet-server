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
package walkingkooka.spreadsheet.server.label;

import walkingkooka.net.header.LinkRelation;
import walkingkooka.net.http.HttpMethod;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.net.http.server.hateos.HateosResourceName;
import walkingkooka.net.http.server.hateos.HateosResourceSelection;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.reference.SpreadsheetSelection;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.text.CharSequences;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}.
 */
public final class SpreadsheetLabelHateosResourceMappings implements PublicStaticHelper {

    /**
     * A {@link HateosResourceName} with <code>metadata</code>.
     */
    private final static HateosResourceName LABEL = HateosResourceName.with("label");

    private static HateosResourceSelection<SpreadsheetLabelName> parse(final String text,
                                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        try {
            HateosResourceSelection<SpreadsheetLabelName> selection;

            if (text.isEmpty()) {
                selection = HateosResourceSelection.none();
            } else {
                selection = HateosResourceSelection.one(SpreadsheetSelection.labelName(text));
            }

            return selection;
        } catch (final Exception cause) {
            throw new IllegalArgumentException("Invalid label name " + CharSequences.quoteAndEscape(text));
        }
    }

    /**
     * Used to form the metadata load and save services
     * <pre>
     * /api/spreadsheet/$spreadsheet-id/label
     * </pre>
     */
    private final static LinkRelation<?> METADATA_LINK_RELATION = LinkRelation.SELF;

    /**
     * Singleton instance
     */
    public static HateosResourceMapping<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetEngineHateosResourceHandlerContext> INSTANCE = HateosResourceMapping.with(
            LABEL,
            SpreadsheetLabelHateosResourceMappings::parse,
            SpreadsheetLabelMapping.class,
            SpreadsheetLabelMapping.class,
            SpreadsheetLabelMapping.class,
            SpreadsheetEngineHateosResourceHandlerContext.class
    ).setHateosResourceHandler(
            METADATA_LINK_RELATION,
            HttpMethod.DELETE,
            SpreadsheetLabelHateosResourceHandlerDelete.INSTANCE
    ).setHateosResourceHandler(
            METADATA_LINK_RELATION,
            HttpMethod.GET,
            SpreadsheetLabelHateosResourceHandlerLoad.INSTANCE
    ).setHateosResourceHandler(
            METADATA_LINK_RELATION,
            HttpMethod.POST,
            SpreadsheetLabelHateosResourceHandlerSaveOrUpdate.INSTANCE
    );

    /**
     * Stop creation.
     */
    private SpreadsheetLabelHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
