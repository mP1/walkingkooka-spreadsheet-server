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
package walkingkooka.spreadsheet.server.plugin;

import walkingkooka.collect.set.Sets;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.net.http.server.hateos.HateosResourceMapping;
import walkingkooka.reflect.StaticHelper;
import walkingkooka.route.Router;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

import java.util.Objects;

/**
 * Factory that returns a {@link Router} for interacting with plugins.
 */
final class PluginHateosResourceHandlersRouter implements StaticHelper {

    /**
     * Builds a {@link Router} that handles all operations, using the given {@link HateosResourceHandler handlers}.
     */
    static Router<HttpRequestAttribute<?>, HttpHandler> router(final AbsoluteUrl baseUrl,
                                                               final Indentation indentation,
                                                               final LineEnding lineEnding,
                                                               final PluginHateosResourceHandlerContext context) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(indentation, "indentation");
        Objects.requireNonNull(lineEnding, "lineEnding");
        Objects.requireNonNull(context, "context");

        // metadata GET, POST...........................................................................................

        return HateosResourceMapping.router(
                baseUrl,
                Sets.of(
                        PluginHttpMappings.plugin()
                ),
                indentation,
                lineEnding,
                context
        );
    }

    /**
     * Stop creation.
     */
    private PluginHateosResourceHandlersRouter() {
        throw new UnsupportedOperationException();
    }
}
