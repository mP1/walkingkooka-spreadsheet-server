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

import org.junit.jupiter.api.Test;
import walkingkooka.convert.ConverterContexts;
import walkingkooka.environment.EnvironmentContext;
import walkingkooka.environment.EnvironmentContexts;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.plugin.ProviderContext;
import walkingkooka.plugin.ProviderContexts;
import walkingkooka.plugin.store.PluginStores;
import walkingkooka.spreadsheet.provider.SpreadsheetProvider;
import walkingkooka.spreadsheet.provider.SpreadsheetProviders;
import walkingkooka.tree.expression.ExpressionNumberKind;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContexts;

import java.math.MathContext;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetProviderHateosResourceHandlerContextTest implements SpreadsheetProviderHateosResourceHandlerContextTesting<BasicSpreadsheetProviderHateosResourceHandlerContext> {

    private final static SpreadsheetProvider SPREADSHEET_PROVIDER = SpreadsheetProviders.fake();

    private final static ProviderContext PROVIDER_CONTEXT = ProviderContexts.basic(
        ConverterContexts.fake(),
        EnvironmentContexts.empty(
            LocalDateTime::now,
            EnvironmentContext.ANONYMOUS
        ),
        PluginStores.treeMap()
    );

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.basic(
        JsonNodeMarshallUnmarshallContexts.basic(
            JsonNodeMarshallContexts.basic(),
            JsonNodeUnmarshallContexts.basic(
                ExpressionNumberKind.BIG_DECIMAL,
                MathContext.DECIMAL32
            )
        )
    );

    @Test
    public void testWithNullSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetProviderHateosResourceHandlerContext.with(
                null,
                PROVIDER_CONTEXT,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullProviderContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetProviderHateosResourceHandlerContext.with(
                SPREADSHEET_PROVIDER,
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetProviderHateosResourceHandlerContext.with(
                SPREADSHEET_PROVIDER,
                PROVIDER_CONTEXT,
                null
            )
        );
    }

    @Override
    public BasicSpreadsheetProviderHateosResourceHandlerContext createContext() {
        return BasicSpreadsheetProviderHateosResourceHandlerContext.with(
            SPREADSHEET_PROVIDER,
            PROVIDER_CONTEXT,
            HATEOS_RESOURCE_HANDLER_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetProviderHateosResourceHandlerContext> type() {
        return BasicSpreadsheetProviderHateosResourceHandlerContext.class;
    }
}
