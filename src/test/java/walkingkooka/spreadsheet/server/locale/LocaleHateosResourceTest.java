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
import walkingkooka.text.HasTextTesting;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class LocaleHateosResourceTest implements ComparableTesting2<LocaleHateosResource>,
    HasTextTesting,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<LocaleHateosResource>,
    ClassTesting2<LocaleHateosResource>,
    ParseStringTesting<LocaleHateosResource> {

    private final static Locale LOCALE = Locale.forLanguageTag("EN-AU");

    @Test
    public void testWithNullLocaleFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleHateosResource.with(null)
        );
    }

    @Test
    public void testWith() {
        final LocaleHateosResource resource = LocaleHateosResource.with(LOCALE);
        this.checkEquals(
            LOCALE,
            resource.value()
        );

        this.checkEquals(
            "en-AU",
                resource.hateosLinkId()
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
            "EN-AU",
            LocaleHateosResource.with(LOCALE)
        );
    }

    @Override
    public LocaleHateosResource parseString(final String text) {
        return LocaleHateosResource.parse(text);
    }

    @Override
    public Class<? extends RuntimeException> parseStringFailedExpected(final Class<? extends RuntimeException> thrown) {
        return thrown;
    }

    @Override
    public RuntimeException parseStringFailedExpected(final RuntimeException thrown) {
        return thrown;
    }

    // HasText..........................................................................................................

    @Test
    public void testText() {
        this.textAndCheck(
            LocaleHateosResource.with(LOCALE),
            "en-AU"
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrintable() {
        this.treePrintAndCheck(
            LocaleHateosResource.with(LOCALE),
            "en-AU\n"
        );
    }

    // comparable.......................................................................................................

    @Test
    public void testComparableLess() {
        this.compareToAndCheckLess(
            LocaleHateosResource.with(
                Locale.FRANCE
            )
        );
    }

    @Override
    public LocaleHateosResource createComparable() {
        return LocaleHateosResource.with(LOCALE);
    }

    // json.............................................................................................................

    @Override
    public LocaleHateosResource unmarshall(final JsonNode node,
                                final JsonNodeUnmarshallContext context) {
        return LocaleHateosResource.unmarshall(
            node,
            context
        );
    }

    @Override
    public LocaleHateosResource createJsonNodeMarshallingValue() {
        return LocaleHateosResource.with(LOCALE);
    }

    // class............................................................................................................

    @Override
    public Class<LocaleHateosResource> type() {
        return LocaleHateosResource.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
