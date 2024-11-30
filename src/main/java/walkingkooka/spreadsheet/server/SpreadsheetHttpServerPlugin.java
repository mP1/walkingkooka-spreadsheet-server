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

import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.http.server.HttpHandler;
import walkingkooka.net.http.server.HttpRequestAttribute;
import walkingkooka.route.RouteMappings;

final class SpreadsheetHttpServerPlugin extends SpreadsheetHttpServerPluginGwt {

    @GwtIncompatible
    static RouteMappings<HttpRequestAttribute<?>, HttpHandler> httpHandler(final SpreadsheetHttpServer server,
                                                                           final RouteMappings<HttpRequestAttribute<?>, HttpHandler> mappings,
                                                                           final AbsoluteUrl apiPlugin) {
        return server.plugin0(
                mappings,
                apiPlugin
        );
    }
}
