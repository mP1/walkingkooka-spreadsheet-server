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

import walkingkooka.net.UrlPath;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadata;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlPathTemplate;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.template.TemplateValueName;

import java.util.Map;

/**
 * A handler returns the {@link SpreadsheetFormatterSelectorEdit} with no {@link SpreadsheetCell} in the context.
 */
final class SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata extends SpreadsheetFormatterSelectorEditHateosHttpHandler {

    /**
     * Singleton
     */
    final static SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata INSTANCE = new SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata();

    private SpreadsheetFormatterSelectorEditHateosHttpHandlerMetadata() {
    }

    @Override
    SpreadsheetFormatterSelectorEdit extractSelectorAndProduceEdit(final UrlPath path,
                                                                   final SpreadsheetEngineHateosResourceHandlerContext context) {
        final Map<TemplateValueName, Object> values = TEMPLATE.extract(path);
        final String spreadsheetFormatterSelector = (String) values.get(STRING);

        return this.produceEdit(
            spreadsheetFormatterSelector,
            SpreadsheetMetadata.NO_CELL,
            context
        );
    }

    private final static SpreadsheetUrlPathTemplate TEMPLATE = SpreadsheetUrlPathTemplate.parse("/api/spreadsheet/${SpreadsheetId}/metadata/${SpreadsheetMetadataPropertyName}/edit/${String}");

    private final static TemplateValueName STRING = TemplateValueName.with("String");
}
