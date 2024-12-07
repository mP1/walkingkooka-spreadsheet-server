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
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.reflect.PublicStaticHelperTesting;

import java.lang.reflect.Method;
import java.util.OptionalInt;

public final class SpreadsheetUrlQueryParametersTest implements PublicStaticHelperTesting<SpreadsheetUrlQueryParameters> {

    // count............................................................................................................

    @Test
    public void testCountMissing() {
        this.checkEquals(
                OptionalInt.empty(),
                SpreadsheetUrlQueryParameters.count(
                        Maps.empty()
                )
        );
    }

    @Test
    public void testCount() {
        this.checkEquals(
                OptionalInt.of(123),
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
    public void testFromMissingParameter() {
        this.checkEquals(
                OptionalInt.empty(),
                SpreadsheetUrlQueryParameters.from(
                        Maps.empty()
                )
        );
    }

    @Test
    public void testFrom() {
        this.checkEquals(
                OptionalInt.of(123),
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
