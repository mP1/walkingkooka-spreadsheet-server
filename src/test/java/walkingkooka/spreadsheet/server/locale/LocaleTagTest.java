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
import walkingkooka.compare.ComparableTesting2;
import walkingkooka.reflect.ClassTesting2;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.test.ParseStringTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class LocaleTagTest implements ComparableTesting2<LocaleTag>,
    JsonNodeMarshallingTesting<LocaleTag>,
    ClassTesting2<LocaleTag>,
    ParseStringTesting<LocaleTag> {

    private final static Locale LOCALE = Locale.forLanguageTag("EN-AU");

    @Test
    public void testWithNullLocaleFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleTag.with(null)
        );
    }

    @Test
    public void testWith() {
        final Locale locale = Locale.forLanguageTag("en-AU");

        final LocaleTag localeTag = LocaleTag.with(locale);
        this.checkEquals(
            locale,
            localeTag.value()
        );
    }

    // parse............................................................................................................

    @Override
    public void testParseStringEmptyFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testParse() {
        this.parseStringAndCheck(
            "en-AU",
            LocaleTag.with(LOCALE)
        );
    }

    @Override
    public LocaleTag parseString(final String text) {
        return LocaleTag.parse(text);
    }

    @Override
    public Class<? extends RuntimeException> parseStringFailedExpected(final Class<? extends RuntimeException> thrown) {
        return thrown;
    }

    @Override
    public RuntimeException parseStringFailedExpected(final RuntimeException thrown) {
        return thrown;
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        this.compareToAndCheckLess(
            LocaleTag.with(
                Locale.FRANCE
            )
        );
    }

    @Override
    public LocaleTag createComparable() {
        return LocaleTag.with(LOCALE);
    }

    // json.............................................................................................................

    @Override
    public LocaleTag unmarshall(final JsonNode node,
                                final JsonNodeUnmarshallContext context) {
        return LocaleTag.unmarshall(
            node,
            context
        );
    }

    @Override
    public LocaleTag createJsonNodeMarshallingValue() {
        return LocaleTag.with(LOCALE);
    }

    // class............................................................................................................

    @Override
    public Class<LocaleTag> type() {
        return LocaleTag.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
