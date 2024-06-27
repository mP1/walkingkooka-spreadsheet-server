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

import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.UnsupportedHateosResourceHandlerHandleNone;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelMapping;
import walkingkooka.spreadsheet.reference.SpreadsheetLabelName;
import walkingkooka.spreadsheet.server.engine.SpreadsheetEngineHateosResourceHandlerContext;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link HateosResourceHandler} that attempts to load a {@link SpreadsheetLabelMapping} with the given {@link SpreadsheetLabelName}.
 */
final class SpreadsheetLabelHateosResourceHandlerDelete extends SpreadsheetLabelHateosResourceHandler
        implements UnsupportedHateosResourceHandlerHandleNone<SpreadsheetLabelName, SpreadsheetLabelMapping, SpreadsheetLabelMapping, SpreadsheetEngineHateosResourceHandlerContext> {

    static SpreadsheetLabelHateosResourceHandlerDelete INSTANCE = new SpreadsheetLabelHateosResourceHandlerDelete();

    private SpreadsheetLabelHateosResourceHandlerDelete() {
        super();
    }

    @Override
    public Optional<SpreadsheetLabelMapping> handleOne(final SpreadsheetLabelName id,
                                                       final Optional<SpreadsheetLabelMapping> resource,
                                                       final Map<HttpRequestAttribute<?>, Object> parameters,
                                                       final SpreadsheetEngineHateosResourceHandlerContext context) {
        HateosResourceHandler.checkId(id);
        HateosResourceHandler.checkResourceEmpty(resource);
        HateosResourceHandler.checkParameters(parameters);
        HateosResourceHandler.checkContext(context);

        context.storeRepository()
                .labels()
                .delete(id);
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "delete with labelName ";
    }
}
