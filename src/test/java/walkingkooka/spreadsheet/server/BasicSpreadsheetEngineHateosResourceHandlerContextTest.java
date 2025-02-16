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
import walkingkooka.net.Url;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;
import walkingkooka.tree.json.marshall.JsonNodeMarshallUnmarshallContexts;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetEngineHateosResourceHandlerContextTest implements SpreadsheetEngineHateosResourceHandlerContextTesting<BasicSpreadsheetEngineHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting {

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.basic(
        JsonNodeMarshallUnmarshallContexts.basic(
            JSON_NODE_MARSHALL_CONTEXT,
            JSON_NODE_UNMARSHALL_CONTEXT
        )
    );

    private final static SpreadsheetEngineContext SPREADSHEET_ENGINE_CONTEXT = SpreadsheetEngineContexts.basic(
        Url.parseAbsolute("https://example.com"),
        METADATA_EN_AU,
        new FakeSpreadsheetStoreRepository() {
            @Override
            public SpreadsheetLabelStore labels() {
                return SpreadsheetLabelStores.fake();
            }
        },
        SpreadsheetMetadataPropertyName.FORMULA_FUNCTIONS,
        SPREADSHEET_PROVIDER,
        PROVIDER_CONTEXT
    );

    // with.............................................................................................................

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosResourceHandlerContext.with(
                null,
                SPREADSHEET_ENGINE_CONTEXT,
                SPREADSHEET_FORMATTER_CONTEXT,
                SPREADSHEET_PROVIDER
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetEngineContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosResourceHandlerContext.with(
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                null,
                SPREADSHEET_FORMATTER_CONTEXT,
                SPREADSHEET_PROVIDER
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetFormatterContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosResourceHandlerContext.with(
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_ENGINE_CONTEXT,
                null,
                SPREADSHEET_PROVIDER
            )
        );
    }

    @Test
    public void testWithNullSpreadsheetProviderFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicSpreadsheetEngineHateosResourceHandlerContext.with(
                HATEOS_RESOURCE_HANDLER_CONTEXT,
                SPREADSHEET_ENGINE_CONTEXT,
                SPREADSHEET_FORMATTER_CONTEXT,
                null
            )
        );
    }

    // SpreadsheetEngineHateosResourceHandlerContextTesting...................................................................

    @Override
    public void testResolveLabelWithNullFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testCheckToStringOverridden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicSpreadsheetEngineHateosResourceHandlerContext createContext() {
        return BasicSpreadsheetEngineHateosResourceHandlerContext.with(
            HATEOS_RESOURCE_HANDLER_CONTEXT,
            SPREADSHEET_ENGINE_CONTEXT,
            SPREADSHEET_FORMATTER_CONTEXT,
            SPREADSHEET_PROVIDER
        );
    }

    @Override
    public String currencySymbol() {
        return SPREADSHEET_FORMATTER_CONTEXT.currencySymbol();
    }

    @Override
    public char decimalSeparator() {
        return SPREADSHEET_FORMATTER_CONTEXT.decimalSeparator();
    }

    @Override
    public String exponentSymbol() {
        return SPREADSHEET_FORMATTER_CONTEXT.exponentSymbol();
    }

    @Override
    public char groupSeparator() {
        return SPREADSHEET_FORMATTER_CONTEXT.groupSeparator();
    }

    @Override
    public MathContext mathContext() {
        return SPREADSHEET_FORMATTER_CONTEXT.mathContext();
    }

    @Override
    public char negativeSign() {
        return SPREADSHEET_FORMATTER_CONTEXT.negativeSign();
    }

    @Override
    public char percentageSymbol() {
        return SPREADSHEET_FORMATTER_CONTEXT.percentageSymbol();
    }

    @Override
    public char positiveSign() {
        return SPREADSHEET_FORMATTER_CONTEXT.positiveSign();
    }

    // class............................................................................................................

    @Override
    public Class<BasicSpreadsheetEngineHateosResourceHandlerContext> type() {
        return BasicSpreadsheetEngineHateosResourceHandlerContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
