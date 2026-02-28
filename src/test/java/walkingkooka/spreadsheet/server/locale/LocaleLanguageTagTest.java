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

public final class LocaleLanguageTagTest implements ComparableTesting2<LocaleLanguageTag>,
    JsonNodeMarshallingTesting<LocaleLanguageTag>,
    ClassTesting2<LocaleLanguageTag>,
    ParseStringTesting<LocaleLanguageTag> {

    private final static String LOCALE = "en-AU";

    @Test
    public void testWithNullLocaleFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleLanguageTag.parse(null)
        );
    }

    @Test
    public void testWith() {
        final String languageTag = "en-AU";

        final LocaleLanguageTag localeLanguageTag = LocaleLanguageTag.parse(languageTag);
        this.checkEquals(
            languageTag,
            localeLanguageTag.value()
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
            LocaleLanguageTag.parse(LOCALE)
        );
    }

    @Override
    public LocaleLanguageTag parseString(final String text) {
        return LocaleLanguageTag.parse(text);
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
            LocaleLanguageTag.parse(
                Locale.FRANCE.toLanguageTag()
            )
        );
    }

    @Test
    public void testEqualsDifferentCase() {
        this.compareToAndCheckEquals(
            LocaleLanguageTag.parse("en-AU"),
            LocaleLanguageTag.parse("EN-AU")
        );
    }

    @Override
    public LocaleLanguageTag createComparable() {
        return LocaleLanguageTag.parse(LOCALE);
    }

    // json.............................................................................................................

    @Override
    public LocaleLanguageTag unmarshall(final JsonNode node,
                                        final JsonNodeUnmarshallContext context) {
        return LocaleLanguageTag.unmarshall(
            node,
            context
        );
    }

    @Override
    public LocaleLanguageTag createJsonNodeMarshallingValue() {
        return LocaleLanguageTag.parse(LOCALE);
    }

    // class............................................................................................................

    @Override
    public Class<LocaleLanguageTag> type() {
        return LocaleLanguageTag.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
