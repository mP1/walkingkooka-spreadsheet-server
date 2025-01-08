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

package walkingkooka.spreadsheet.server.formatter;

import org.junit.jupiter.api.Test;
import walkingkooka.Cast;
import walkingkooka.HashCodeEqualsDefinedTesting2;
import walkingkooka.ToStringTesting;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterName;
import walkingkooka.spreadsheet.format.SpreadsheetFormatterSelector;
import walkingkooka.spreadsheet.format.pattern.SpreadsheetPattern;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SpreadsheetFormatterFormatRequestTest implements HashCodeEqualsDefinedTesting2<SpreadsheetFormatterFormatRequest<LocalDate>>,
    ClassTesting<SpreadsheetFormatterFormatRequest<LocalDate>>,
    ToStringTesting<SpreadsheetFormatterFormatRequest<LocalDate>> {

    private final static SpreadsheetFormatterSelector SELECTOR = SpreadsheetPattern.parseDateFormatPattern("dd/mm/yyyyy")
        .spreadsheetFormatterSelector();

    private final static LocalDate VALUE = LocalDate.of(1999, 12, 31);

    @Test
    public void testWithNullSelectorFails() {
        assertThrows(
            NullPointerException.class,
            () -> SpreadsheetFormatterFormatRequest.with(
                null,
                VALUE
            )
        );
    }

    @Test
    public void testWith() {
        final SpreadsheetFormatterFormatRequest<LocalDate> request = SpreadsheetFormatterFormatRequest.with(
            SELECTOR,
            VALUE
        );

        this.checkEquals(
            SELECTOR,
            request.selector(),
            "selector"
        );
        this.checkEquals(
            VALUE,
            request.value(),
            "value"
        );
    }

    @Test
    public void testWithNullValue() {
        final SpreadsheetFormatterFormatRequest<LocalDate> request = SpreadsheetFormatterFormatRequest.with(
            SELECTOR,
            null
        );

        this.checkEquals(
            SELECTOR,
            request.selector(),
            "selector"
        );
        this.checkEquals(
            null,
            request.value(),
            "value"
        );
    }

    // equals...........................................................................................................

    @Test
    public void testEqualsDifferentSelector() {
        this.checkNotEquals(
            SpreadsheetFormatterFormatRequest.with(
                SELECTOR.setName(SpreadsheetFormatterName.TEXT_FORMAT_PATTERN),
                VALUE
            )
        );
    }

    @Test
    public void testEqualsDifferentValue() {
        this.checkNotEquals(
            SpreadsheetFormatterFormatRequest.with(
                SELECTOR,
                LocalDateTime.now()
            )
        );
    }

    @Override
    public SpreadsheetFormatterFormatRequest<LocalDate> createObject() {
        return SpreadsheetFormatterFormatRequest.with(
            SELECTOR,
            VALUE
        );
    }

    // toString.........................................................................................................

    @Test
    public void testToString() {
        this.toStringAndCheck(
            SpreadsheetFormatterFormatRequest.with(
                SELECTOR,
                VALUE
            ),
            SELECTOR + " " + VALUE
        );
    }

    @Test
    public void testToStringStringValue() {
        this.toStringAndCheck(
            SpreadsheetFormatterFormatRequest.with(
                SELECTOR,
                "abc"
            ),
            SELECTOR + " \"abc\""
        );
    }

    @Test
    public void testToStringNullValue() {
        this.toStringAndCheck(
            SpreadsheetFormatterFormatRequest.with(
                SELECTOR,
                null
            ),
            SELECTOR.toString()
        );
    }

    // class............................................................................................................

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }

    @Override
    public Class<SpreadsheetFormatterFormatRequest<LocalDate>> type() {
        return Cast.to(SpreadsheetFormatterFormatRequest.class);
    }
}
