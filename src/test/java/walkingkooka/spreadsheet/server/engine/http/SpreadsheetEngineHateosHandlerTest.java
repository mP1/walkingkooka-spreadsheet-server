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

package walkingkooka.spreadsheet.server.engine.http;

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.hateos.HateosHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetEngineHateosHandlerTest extends SpreadsheetEngineHateosHandlerTestCase<SpreadsheetEngineHateosHandler<?, ?, ?>> {

    @Test
    public void testCountParameterMissingFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetEngineHateosHandler.count(HateosHandler.NO_PARAMETERS)
        );
    }

    @Test
    public void testCountParameterMissingFails2() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetEngineHateosHandler.count(
                        Maps.of(SpreadsheetEngineHateosHandler.COUNT, Lists.empty())
                )
        );
    }

    @Test
    public void testInvalidCountParameterMissingFails() {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetEngineHateosHandler.count(
                        Maps.of(SpreadsheetEngineHateosHandler.COUNT, Lists.of("!invalid"))
                )
        );
        assertEquals("Invalid count parameter got \"!invalid\"", thrown.getMessage());
    }

    @Test
    public void testCount() {
        assertEquals(
                123,
                SpreadsheetEngineHateosHandler.count(
                        Maps.of(SpreadsheetEngineHateosHandler.COUNT, Lists.of("123"))
                )
        );
    }

    @Override
    public Class<SpreadsheetEngineHateosHandler<?, ?, ?>> type() {
        return Cast.to(SpreadsheetEngineHateosHandler.class);
    }
}
