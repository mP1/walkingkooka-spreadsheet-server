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

package walkingkooka.spreadsheet.server.currency;

import org.junit.jupiter.api.Test;
import walkingkooka.net.http.server.hateos.HateosHandlerContext;
import walkingkooka.net.http.server.hateos.HateosHandlerContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicCurrencyHateosHandlerContextTest implements CurrencyHateosHandlerContextTesting<BasicCurrencyHateosHandlerContext>,
    SpreadsheetMetadataTesting {

    private final static HateosHandlerContext HATEOS_HANDLER_CONTEXT = HateosHandlerContexts.basic(
        INDENTATION,
        LINE_ENDING,
        JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT
    );

    @Test
    public void testWithNullCurrencyContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicCurrencyHateosHandlerContext.with(
                null,
                HATEOS_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicCurrencyHateosHandlerContext.with(
                CURRENCY_CONTEXT,
                null
            )
        );
    }

    @Override
    public BasicCurrencyHateosHandlerContext createContext() {
        return BasicCurrencyHateosHandlerContext.with(
            CURRENCY_CONTEXT,
            HATEOS_HANDLER_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<BasicCurrencyHateosHandlerContext> type() {
        return BasicCurrencyHateosHandlerContext.class;
    }
}
