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

import org.junit.jupiter.api.Test;
import walkingkooka.net.AbsoluteUrl;
import walkingkooka.net.Url;
import walkingkooka.net.http.server.HttpHandlerTesting;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class PluginHttpHandlerTest implements HttpHandlerTesting<PluginHttpHandler>,
        ClassTesting<PluginHttpHandler> {

    private final static AbsoluteUrl SERVER_URL = Url.parseAbsolute("https://example.com");

    private final static Indentation INDENTATION = Indentation.SPACES2;

    private final static LineEnding LINE_ENDING = LineEnding.NL;
    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.fake();

    private final static ProviderContext PROVIDER_CONTEXT = ProviderContexts.fake();

    @Test
    public void testWithNullServerUrlFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginHttpHandler.with(
                        null,
                        INDENTATION,
                        LINE_ENDING,
                        HATEOS_RESOURCE_HANDLER_CONTEXT,
                        PROVIDER_CONTEXT
                )
        );
    }

    @Test
    public void testWithNullIndentationFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginHttpHandler.with(
                        SERVER_URL,
                        null,
                        LINE_ENDING,
                        HATEOS_RESOURCE_HANDLER_CONTEXT,
                        PROVIDER_CONTEXT
                )
        );
    }

    @Test
    public void testWithNullLineEndingFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginHttpHandler.with(
                        SERVER_URL,
                        INDENTATION,
                        null,
                        HATEOS_RESOURCE_HANDLER_CONTEXT,
                        PROVIDER_CONTEXT
                )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginHttpHandler.with(
                        SERVER_URL,
                        INDENTATION,
                        LINE_ENDING,
                        null,
                        PROVIDER_CONTEXT
                )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> PluginHttpHandler.with(
                        SERVER_URL,
                        INDENTATION,
                        LINE_ENDING,
                        HATEOS_RESOURCE_HANDLER_CONTEXT,
                        null
                )
        );
    }

    // HttpHandler......................................................................................................

    @Override
    public PluginHttpHandler createHttpHandler() {
        return PluginHttpHandler.with(
                SERVER_URL,
                INDENTATION,
                LINE_ENDING,
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<PluginHttpHandler> type() {
        return PluginHttpHandler.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
