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

import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.spreadsheet.provider.HasSpreadsheetProvider;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

public interface SpreadsheetProviderHateosHandlerContext extends HateosHandlerContext,
    ProviderContext,
    HasSpreadsheetProvider {

    @Override
    SpreadsheetProviderHateosHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor);

    @Override
    SpreadsheetProviderHateosHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor);
}
