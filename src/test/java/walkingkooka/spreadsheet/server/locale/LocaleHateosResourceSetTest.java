package walkingkooka.spreadsheet.server.locale;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.set.ImmutableSortedSetTesting;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.test.ParseStringTesting;
import walkingkooka.text.HasTextTesting;
import walkingkooka.text.printer.TreePrintableTesting;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeMarshallingTesting;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.Arrays;
import java.util.Locale;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class LocaleHateosResourceSetTest implements ImmutableSortedSetTesting<LocaleHateosResourceSet, LocaleHateosResource>,
    HasTextTesting,
    ParseStringTesting<LocaleHateosResourceSet>,
    TreePrintableTesting,
    JsonNodeMarshallingTesting<LocaleHateosResourceSet> {

    private final static LocaleHateosResource EN_AU = LocaleHateosResource.parse("EN-AU");

    private final static LocaleHateosResource EN_NZ = LocaleHateosResource.parse("EN-NZ");

    @Test
    public void testWithNullFails() {
        assertThrows(
            NullPointerException.class,
            () -> LocaleHateosResourceSet.with(null)
        );
    }

    @Test
    public void testDeleteBecomesEmpty() {
        assertSame(
            LocaleHateosResourceSet.EMPTY,
            LocaleHateosResourceSet.EMPTY.concat(EN_AU)
                .delete(EN_AU)
        );
    }

    @Test
    public void testSetElementsWithLocaleHateosResourceSet() {
        final String text = "EN,EN-AU,EN-NZ";
        final LocaleHateosResourceSet set = LocaleHateosResourceSet.parse(text);

        assertSame(
            set,
            LocaleHateosResourceSet.parse(text)
                .setElements(set)
        );
    }

    @Override
    public LocaleHateosResourceSet createSet() {
        final SortedSet<LocaleHateosResource> sortedSet = SortedSets.tree();

        sortedSet.add(
            LocaleHateosResource.with(Locale.ENGLISH)
        );
        sortedSet.add(EN_AU);
        sortedSet.add(EN_NZ);

        return LocaleHateosResourceSet.with(
            SortedSets.of(
                LocaleHateosResource.with(Locale.ENGLISH),
                EN_AU,
                EN_NZ
            )
        );
    }

    // parseString......................................................................................................

    // JRE Locale seems to simply ignore invalid characters, unable to really call Locale.parse(String) and fail

    @Override
    public void testParseStringEmptyFails() {
        throw new UnsupportedOperationException();
    }

    @Test
    public void testParseEmpty() {
        assertSame(
            LocaleHateosResourceSet.EMPTY,
            this.parseStringAndCheck(
                "",
                LocaleHateosResourceSet.EMPTY
            )
        );
    }

    @Test
    public void testParseOnlySpaces() {
        assertSame(
            LocaleHateosResourceSet.EMPTY,
            this.parseStringAndCheck(
                "   ",
                LocaleHateosResourceSet.EMPTY
            )
        );
    }

    @Test
    public void testParseOne() {
        this.parseStringAndCheck(
            "EN",
            LocaleHateosResourceSet.EMPTY.concat(
                LocaleHateosResource.with(
                    (Locale.ENGLISH)
                )
            )
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
    public LocaleHateosResourceSet parseString(final String text) {
        return LocaleHateosResourceSet.parse(text);
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
    public void testMarshallUnmarshallAllAvailableLocaleHateosResources() {
        final SortedSet<LocaleHateosResource> locales = SortedSets.tree();
        locales.addAll(
            Arrays.stream(
                Locale.getAvailableLocales()
            ).map(LocaleHateosResource::with)
                .collect(Collectors.toList())
        );

        this.checkNotEquals(
            SortedSets.empty(),
            locales
        );

        this.marshallRoundTripTwiceAndCheck(
            LocaleHateosResourceSet.with(locales)
        );
    }

    @Override
    public LocaleHateosResourceSet unmarshall(final JsonNode jsonNode,
                                              final JsonNodeUnmarshallContext context) {
        return LocaleHateosResourceSet.unmarshall(
            jsonNode,
            context
        );
    }

    @Override
    public LocaleHateosResourceSet createJsonNodeMarshallingValue() {
        return this.createSet();
    }

    // class............................................................................................................

    @Override
    public Class<LocaleHateosResourceSet> type() {
        return LocaleHateosResourceSet.class;
    }
}