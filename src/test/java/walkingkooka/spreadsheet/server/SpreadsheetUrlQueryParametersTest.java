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
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.map.Maps;
import walkingkooka.net.http.server.hateos.HateosResourceHandler;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetUrlQueryParametersTest implements PublicStaticHelperTesting<SpreadsheetUrlQueryParameters> {

    // count............................................................................................................
    
    @Test
    public void testCountParameterMissingFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetUrlQueryParameters.count(HateosResourceHandler.NO_PARAMETERS)
        );
    }

    @Test
    public void testCountParameterMissingFails2() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetUrlQueryParameters.count(
                        Maps.of(SpreadsheetUrlQueryParameters.COUNT, Lists.empty())
                )
        );
    }

    @Test
    public void testCountParameterInvalidMissingFails() {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetUrlQueryParameters.count(
                        Maps.of(SpreadsheetUrlQueryParameters.COUNT, Lists.of("!invalid"))
                )
        );
        this.checkEquals("Invalid count parameter got \"!invalid\"", thrown.getMessage());
    }

    @Test
    public void testCountParameter() {
        this.checkEquals(
                123,
                SpreadsheetUrlQueryParameters.count(
                        Maps.of(
                                SpreadsheetUrlQueryParameters.COUNT,
                                Lists.of("123")
                        )
                )
        );
    }

    // from.............................................................................................................

    @Test
    public void testFromMissingFails() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetUrlQueryParameters.from(HateosResourceHandler.NO_PARAMETERS)
        );
    }

    @Test
    public void testFromMissingFails2() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetUrlQueryParameters.from(
                        Maps.of(SpreadsheetUrlQueryParameters.FROM, Lists.empty())
                )
        );
    }

    @Test
    public void testFromInvalidMissingFails() {
        final IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> SpreadsheetUrlQueryParameters.from(
                        Maps.of(SpreadsheetUrlQueryParameters.FROM, Lists.of("!invalid"))
                )
        );
        this.checkEquals("Invalid from parameter got \"!invalid\"", thrown.getMessage());
    }

    @Test
    public void testFrom() {
        this.checkEquals(
                123,
                SpreadsheetUrlQueryParameters.from(
                        Maps.of(
                                SpreadsheetUrlQueryParameters.FROM,
                                Lists.of("123")
                        )
                )
        );
    }

    // Class............................................................................................................

    @Override
    public Class<SpreadsheetUrlQueryParameters> type() {
        return SpreadsheetUrlQueryParameters.class;
    }

    @Override
    public boolean canHavePublicTypes(final Method method) {
        return false;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
