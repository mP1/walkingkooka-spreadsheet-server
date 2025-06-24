package walkingkooka.spreadsheet.server.locale;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.test.ParseStringTesting;
import walkingkooka.text.HasTextTesting;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Locale;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class LocaleSetTest implements ImmutableSortedSetTesting<LocaleSet, Locale>,
    HasTextTesting,
    ParseStringTesting<LocaleSet>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<LocaleSet> {

    private final static Locale EN_AU = Locale.forLanguageTag("EN-AU");

    private final static Locale EN_NZ = Locale.forLanguageTag("EN-NZ");

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleSet.with(null)
        );
    }

    @Test
    public void testDeleteBecomesEmpty() {
        assertSame(
            LocaleSet.EMPTY,
            LocaleSet.EMPTY.concat(EN_AU)
                .delete(EN_AU)
        );
    }

    @Test
    public void testSetElementsWithLocaleSet() {
        final String text = "EN,EN-AU,EN-NZ";
        final LocaleSet set = LocaleSet.parse(text);

        assertSame(
            set,
            LocaleSet.parse(text)
                .setElements(set)
        );
    }

    @Override
    public LocaleSet createSet() {
        final SortedSet<Locale> sortedSet = SortedSets.tree(LocaleSet.COMPARATOR);

        sortedSet.add(Locale.ENGLISH);
        sortedSet.add(EN_AU);
        sortedSet.add(EN_NZ);

        return LocaleSet.with(sortedSet);
    }

    // parseString......................................................................................................

    // JRE Locale seems to simply ignore invalid characters, unable to really call Locale.forLanguageTag(String) and fail

    @Override
    public void testParseStringEmptyFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testParseEmpty() {
        assertSame(
            LocaleSet.EMPTY,
            this.parseStringAndCheck(
                "",
                LocaleSet.EMPTY
            )
        );
    }

    @Test
    public void testParseOnlySpaces() {
        assertSame(
            LocaleSet.EMPTY,
            this.parseStringAndCheck(
                "   ",
                LocaleSet.EMPTY
            )
        );
    }

    @Test
    public void testParseOne() {
        this.parseStringAndCheck(
            "EN",
            LocaleSet.EMPTY.concat(Locale.ENGLISH)
        );
    }

    @Test
    public void testParseMixedCase() {
        this.parseStringAndCheck(
            "en, EN-AU, en-nz",
            this.createSet()
        );
    }

    @Override
    public LocaleSet parseString(final String text) {
        return LocaleSet.parse(text);
    }

    @Override
    public Class<? extends RuntimeException> parseStringFailedExpected(final Class<? extends RuntimeException> type) {
        return type;
    }

    @Override
    public RuntimeException parseStringFailedExpected(final RuntimeException type) {
        return type;
    }

    // HasText..........................................................................................................

    @Test
    public void testText() {
        this.textAndCheck(
            this.createSet(),
            "en,en-AU,en-NZ"
        );
    }

    // TreePrintable....................................................................................................

    @Test
    public void testTreePrint() {
        this.treePrintAndCheck(
            this.createSet(),
            "en\n" +
                "en-AU\n" +
                "en-NZ\n"
        );
    }

    // json.............................................................................................................

    @Test
    public void testMarshall() {
        this.marshallAndCheck(
            this.createJsonNodeMarshallingValue(),
            "\"en,en-AU,en-NZ\""
        );
    }

    @Test
    public void testUnmarshall() {
        this.unmarshallAndCheck(
            "\"EN,EN-AU,EN-NZ\"",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Test
    public void testUnmarshallIgnoresWhitespace() {
        this.unmarshallAndCheck(
            "\" EN , EN-AU, EN-NZ \"",
            this.createJsonNodeMarshallingValue()
        );
    }

    @Test
    public void testMarshallUnmarshallAllAvailableLocales() {
        final SortedSet<Locale> locales = SortedSets.tree(LocaleSet.COMPARATOR);
        locales.addAll(
            Lists.of(
                Locale.getAvailableLocales()
            )
        );

        this.checkNotEquals(
            SortedSets.empty(),
            locales
        );

        this.marshallRoundTripTwiceAndCheck(
            LocaleSet.with(locales)
        );
    }

    @Override
    public LocaleSet unmarshall(final JsonNode jsonNode,
                                final JsonNodeUnmarshallContext context) {
        return LocaleSet.unmarshall(
            jsonNode,
            context
        );
    }

    @Override
    public LocaleSet createJsonNodeMarshallingValue() {
        return this.createSet();
    }

    // class............................................................................................................

    @Override
    public Class<LocaleSet> type() {
        return LocaleSet.class;
    }
}