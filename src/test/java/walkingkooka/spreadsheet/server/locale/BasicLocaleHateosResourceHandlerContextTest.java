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

package walkingkooka.spreadsheet.server.locale;

import org.junit.jupiter.api.Test;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContext;
import walkingkooka.net.http.server.hateos.HateosResourceHandlerContexts;
import walkingkooka.spreadsheet.meta.SpreadsheetMetadataTesting;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BasicLocaleHateosResourceHandlerContextTest implements LocaleHateosResourceHandlerContextTesting<BasicLocaleHateosResourceHandlerContext>,
    SpreadsheetMetadataTesting {

    private final static HateosResourceHandlerContext HATEOS_RESOURCE_HANDLER_CONTEXT = HateosResourceHandlerContexts.basic(
        INDENTATION,
        LINE_ENDING,
        JSON_NODE_MARSHALL_UNMARSHALL_CONTEXT
    );

    @Test
    public void testWithNullLocaleContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicLocaleHateosResourceHandlerContext.with(
                null,
                HATEOS_RESOURCE_HANDLER_CONTEXT
            )
        );
    }

    @Test
    public void testWithNullHateosResourceHandlerContextFails() {
        assertThrows(
            NullPointerException.class,
            () -> BasicLocaleHateosResourceHandlerContext.with(
                LOCALE_CONTEXT,
                null
            )
        );
    }

    @Override
    public BasicLocaleHateosResourceHandlerContext createContext() {
        return BasicLocaleHateosResourceHandlerContext.with(
            LOCALE_CONTEXT,
            HATEOS_RESOURCE_HANDLER_CONTEXT
        );
    }

    // class............................................................................................................

    @Override
    public Class<BasicLocaleHateosResourceHandlerContext> type() {
        return BasicLocaleHateosResourceHandlerContext.class;
    }
}
