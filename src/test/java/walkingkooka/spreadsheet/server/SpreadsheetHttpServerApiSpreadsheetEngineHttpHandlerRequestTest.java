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
import walkingkooka.net.http.server.FakeHttpRequest;
import walkingkooka.net.http.server.HttpResponses;

public final class SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequestTest extends SpreadsheetHttpServerTestCase2<SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest> {

    @Test
    public void testToString() {
        final String toString = "Request 123";
        this.toStringAndCheck(
                SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest.with(
                        new FakeHttpRequest() {
                            @Override
                            public String toString() {
                                return toString;
                            }
                        },
                        HttpResponses.fake(),
                        null
                ),
                toString
        );
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest> type() {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandlerRequest.class;
    }

    // TypeNameTesting..................................................................................................

    @Override
    public String typeNamePrefix() {
        return SpreadsheetHttpServerApiSpreadsheetEngineHttpHandler.class.getSimpleName();
    }

    @Override
    public String typeNameSuffix() {
        return "Request";
    }
}
