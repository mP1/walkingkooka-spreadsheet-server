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
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContext;
import walkingkooka.spreadsheet.engine.SpreadsheetEngineContexts;
import walkingkooka.spreadsheet.engine.SpreadsheetEngines;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataPropertyName;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStore;
import walkingkooka.spreadsheet.store.SpreadsheetLabelStores;
import walkingkooka.spreadsheet.store.repo.FakeSpreadsheetStoreRepository;

import java.math.MathContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicSpreadsheetHateosResourceHandlerContextTest implements SpreadsheetHateosResourceHandlerContextTesting<BasicSpreadsheetHateosResourceHandlerContext>,
        SpreadsheetMetadataTesting {

    private final static SpreadsheetEngineContext SPREADSHEET_ENGINE_CONTEXT = SpreadsheetEngineContexts.basic(
            Url.parseAbsolute("https://example.com"),
            NOW,
            METADATA_EN_AU,
            SpreadsheetEngines.fake(),
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
    public void testWithNullJsonNodeMarshallContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> BasicSpreadsheetHateosResourceHandlerContext.with(
                        null,
                        JSON_NODE_UNMARSHALL_CONTEXT,
                        SPREADSHEET_ENGINE_CONTEXT,
                        SPREADSHEET_FORMATTER_CONTEXT,
                        SPREADSHEET_PROVIDER
                )
        );
    }

    @Test
    public void testWithNullJsonNodeUnmarshallContextFails() {
        assertThrows(
                NullPointerException.class,
                () -> BasicSpreadsheetHateosResourceHandlerContext.with(
                        JSON_NODE_MARSHALL_CONTEXT,
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
                () -> BasicSpreadsheetHateosResourceHandlerContext.with(
                        JSON_NODE_MARSHALL_CONTEXT,
                        JSON_NODE_UNMARSHALL_CONTEXT,
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
                () -> BasicSpreadsheetHateosResourceHandlerContext.with(
                        JSON_NODE_MARSHALL_CONTEXT,
                        JSON_NODE_UNMARSHALL_CONTEXT,
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
                () -> BasicSpreadsheetHateosResourceHandlerContext.with(
                        JSON_NODE_MARSHALL_CONTEXT,
                        JSON_NODE_UNMARSHALL_CONTEXT,
                        SPREADSHEET_ENGINE_CONTEXT,
                        SPREADSHEET_FORMATTER_CONTEXT,
                        null
                )
        );
    }

    // SpreadsheetHateosResourceHandlerContextTesting...................................................................

    @Override
    public void testResolveLabelWithNullFails() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void testCheckToStringOverridden() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicSpreadsheetHateosResourceHandlerContext createContext() {
        return BasicSpreadsheetHateosResourceHandlerContext.with(
                JSON_NODE_MARSHALL_CONTEXT,
                JSON_NODE_UNMARSHALL_CONTEXT,
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
    public Class<BasicSpreadsheetHateosResourceHandlerContext> type() {
        return BasicSpreadsheetHateosResourceHandlerContext.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PACKAGE_PRIVATE;
    }
}
