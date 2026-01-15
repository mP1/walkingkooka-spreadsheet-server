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

import walkingkooka.Context;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.route.Router;
import walkingkooka.spreadsheet.meta.SpreadsheetId;
import walkingkooka.spreadsheet.server.SpreadsheetServerContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContextObjectPostProcessor;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContextPreProcessor;

import java.util.Locale;

/**
 * A {@link Context} for spreadsheets.
 */
public interface SpreadsheetMetadataHateosResourceHandlerContext extends SpreadsheetServerContext {

    @Override
    SpreadsheetMetadataHateosResourceHandlerContext setObjectPostProcessor(final JsonNodeMarshallContextObjectPostProcessor processor);

    @Override
    SpreadsheetMetadataHateosResourceHandlerContext setPreProcessor(final JsonNodeUnmarshallContextPreProcessor processor);

    /**
     * A {@link Router} that can handle http requests for the given identified spreadsheet.
     */
    Router<HttpRequestAttribute<?>, HttpHandler> httpRouter(final SpreadsheetId id);

    @Override
    default EnvironmentContext cloneEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    default SpreadsheetMetadataHateosResourceHandlerContext setEnvironmentContext(final EnvironmentContext environmentContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <T> void setEnvironmentValue(final EnvironmentValueName<T> name,
                                         final T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void removeEnvironmentValue(final EnvironmentValueName<?> name) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setLocale(final Locale locale) {
        throw new UnsupportedOperationException();
    }
}
