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
package walkingkooka.spreadsheet.server.meta;

import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.reflect.PublicStaticHelper;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.SpreadsheetId;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.tree.json.JsonNode;

import java.util.function.UnaryOperator;

/**
 * A collection of factory methods to create various {@link HateosResourceHandler}, {@link Router} etc.
 */
public final class SpreadsheetMetadataHateosResourceMappings implements PublicStaticHelper {

    /**
     * {@see SpreadsheetMetadataPatchFunction}
     */
    public static UnaryOperator<JsonNode> patch(final SpreadsheetId id,
                                                final SpreadsheetMetadataHateosResourceHandlerContext context) {
        return SpreadsheetMetadataPatchFunction.with(id, context);
    }

    /**
     * {@see SpreadsheetMetadataHateosResourceHandlersRouter}
     */
    public static Router<HttpRequestAttribute<?>, HttpHandler> router(final AbsoluteUrl baseUrl,
                                                                      final Indentation indentation,
                                                                      final LineEnding lineEnding,
                                                                      final SpreadsheetMetadataHateosResourceHandlerContext spreadsheetMetadataHateosResourceHandlerContext) {
        return SpreadsheetMetadataHateosResourceHandlersRouter.with(
                baseUrl,
                indentation,
                lineEnding,
                spreadsheetMetadataHateosResourceHandlerContext
        );
    }

    /**
     * Stop creation.
     */
    private SpreadsheetMetadataHateosResourceMappings() {
        throw new UnsupportedOperationException();
    }
}
