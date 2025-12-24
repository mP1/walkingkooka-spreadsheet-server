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
import walkingkooka.spreadsheet.expression.SpreadsheetExpressionEvaluationContexts;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReference;
import walkingkooka.spreadsheet.reference.SpreadsheetExpressionReferenceLoaders;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelNameResolvers;
import walkingkooka.spreadsheet.server.SpreadsheetEngineHateosResourceHandlerContext;
import walkingkooka.spreadsheet.server.net.SpreadsheetUrlPathTemplate;
import walkingkooka.spreadsheet.store.repo.SpreadsheetStoreRepository;
import walkingkooka.spreadsheet.value.SpreadsheetCell;
import walkingkooka.template.TemplateValueName;

import java.util.Map;

/**
 * A handler that returns a {@link SpreadsheetFormatterSelectorEdit} loading the cell in the URL so the {@link SpreadsheetCell} value
 * can be used when generating samples.
 */
final class SpreadsheetFormatterSelectorEditHateosHttpHandlerCell extends SpreadsheetFormatterSelectorEditHateosHttpHandler {

    /**
     * Singleton
     */
    final static SpreadsheetFormatterSelectorEditHateosHttpHandlerCell INSTANCE = new SpreadsheetFormatterSelectorEditHateosHttpHandlerCell();

    private SpreadsheetFormatterSelectorEditHateosHttpHandlerCell() {
    }

    @Override
    SpreadsheetFormatterSelectorEdit extractSelectorAndProduceEdit(final UrlPath path,
                                                                   final SpreadsheetEngineHateosResourceHandlerContext context) {
        final Map<TemplateValueName, Object> values = TEMPLATE.extract(path);
        final SpreadsheetExpressionReference cellOrLabel = (SpreadsheetExpressionReference) values.get(SpreadsheetUrlPathTemplate.SPREADSHEET_EXPRESSION_REFERENCE);
        final String spreadsheetFormatterSelector = (String) values.get(STRING);

        final SpreadsheetStoreRepository repo = context.storeRepository();

        return this.produceEdit(
            spreadsheetFormatterSelector,
            SpreadsheetExpressionReferenceLoaders.spreadsheetStoreRepository(repo)
                .loadCell(
                    SpreadsheetLabelNameResolvers.labelStore(
                            repo.labels()
                        ).resolveIfLabelOrFail(cellOrLabel)
                        .toCell(),
                    SpreadsheetExpressionEvaluationContexts.fake()
                ),
            context
        );
    }

    private final static SpreadsheetUrlPathTemplate TEMPLATE = SpreadsheetUrlPathTemplate.parse("/api/spreadsheet/${SpreadsheetId}/cell/${SpreadsheetExpressionReference}/formatter-edit/${String}");

    private final static TemplateValueName STRING = TemplateValueName.with("String");
}
