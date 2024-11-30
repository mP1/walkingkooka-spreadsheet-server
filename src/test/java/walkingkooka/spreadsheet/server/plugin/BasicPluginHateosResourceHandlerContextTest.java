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
import walkingkooka.environment.EnvironmentValueName;
import walkingkooka.environment.FakeEnvironmentContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicPluginHateosResourceHandlerContextTest implements PluginHateosResourceHandlerContextTesting<BasicPluginHateosResourceHandlerContext>,
        SpreadsheetMetadataTesting {

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.basic(
            JsonNodeMarshallUnmarshallContexts.basic(
                    JSON_NODE_MARSHALL_CONTEXT,
                    JSON_NODE_UNMARSHALL_CONTEXT
            )
    );
    private final static ProviderContext PROVIDER_CONTEXT = ProviderContexts.basic(
            new FakeEnvironmentContext() {
                @Override
                public <T> Optional<T> environmentValue(final EnvironmentValueName<T> name) {
                    Objects.requireNonNull(name, "name");
                    throw new UnsupportedOperationException();
                }
            },
            PluginStores.treeMap()
    );

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> BasicPluginHateosResourceHandlerContext.with(
                        null,
                        PROVIDER_CONTEXT
                )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> BasicPluginHateosResourceHandlerContext.with(
                        HATEOS_RESOURCE_HANDLER_CONTEXT,
                        null
                )
        );
    }

    @Override
    public void testUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicPluginHateosResourceHandlerContext createContext() {
        return BasicPluginHateosResourceHandlerContext.with(
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                PROVIDER_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<BasicPluginHateosResourceHandlerContext> type() {
        return BasicPluginHateosResourceHandlerContext.class;
    }
}
