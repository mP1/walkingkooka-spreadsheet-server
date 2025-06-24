package walkingkooka.spreadsheet.server.locale;

import walkingkooka.collect.iterator.Iterators;
import walkingkooka.collect.set.ImmutableSortedSetDefaults;
import walkingkooka.collect.set.SortedSets;
import walkingkooka.text.CharacterConstant;
import walkingkooka.text.HasText;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.TreePrintable;
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable {@link Set} containing unique {@link Locale locales}.
 */
public final class LocaleSet extends AbstractSet<Locale>
    implements ImmutableSortedSetDefaults<LocaleSet, Locale>,
    HasText,
    TreePrintable {

    /**
     * An empty {@link LocaleSet}.
     */
    public static final LocaleSet EMPTY = new LocaleSet(SortedSets.empty());

    /**
     * The comma which separates the CSV text representation.
     */
    public static final CharacterConstant SEPARATOR = CharacterConstant.COMMA;

    /**
     * Compare Locales using {@link Locale#toLanguageTag()}.
     */
    public static final Comparator<Locale> COMPARATOR = Comparator.comparing(Locale::toLanguageTag);

    /**
     * Factory that creates {@link LocaleSet} with the given locales.
     */
    public static LocaleSet with(final SortedSet<Locale> locales) {
        return EMPTY.setElements(locales);
    }

    private static LocaleSet withCopy(final SortedSet<Locale> locales) {
        return locales.isEmpty() ?
            EMPTY :
            new LocaleSet(locales);
    }

    /**
     * Accepts a string of csv {@link Locale} with optional whitespace around locales ignored.
     */
    public static LocaleSet parse(final String text) {
        Objects.requireNonNull(text, "text");

        final SortedSet<Locale> locales = SortedSets.tree(COMPARATOR);

        final int length = text.length();
        final StringBuilder locale = new StringBuilder();

        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            switch (c) {
                case ',':
                    append(
                        locale,
                        locales
                    );
                    locale.setLength(0);
                    break;
                default:
                    locale.append(c);
                    break;
            }
        }

        append(
            locale,
            locales
        );

        return withCopy(locales);
    }

    private static void append(final StringBuilder b,
                               final SortedSet<Locale> locales) {
        final String text = b.toString()
            .trim();
        if (false == text.isEmpty()) {
            locales.add(
                Locale.forLanguageTag(text)
            );
        }
    }

    private LocaleSet(final SortedSet<Locale> locales) {
        super();
        this.locales = locales;
    }

    // ImmutableSortedSet...............................................................................................

    @Override
    public Iterator<Locale> iterator() {
        return Iterators.readOnly(
            this.locales.iterator()
        );
    }

    @Override
    public int size() {
        return this.locales.size();
    }

    @Override
    public Comparator<Locale> comparator() {
        return COMPARATOR;
    }

    @Override
    public LocaleSet subSet(Locale from,
                            Locale to) {
        return withCopy(
            this.locales.subSet(
                from,
                to
            )
        );
    }

    @Override
    public LocaleSet headSet(Locale locale) {
        return withCopy(
            this.locales.headSet(locale)
        );
    }

    @Override
    public LocaleSet tailSet(Locale locale) {
        return withCopy(
            this.locales.tailSet(locale)
        );
    }

    @Override
    public Locale first() {
        return this.locales.first();
    }

    @Override
    public Locale last() {
        return this.locales.last();
    }

    @Override
    public SortedSet<Locale> toSet() {
        final TreeSet<Locale> treeSet = new TreeSet<>(COMPARATOR);
        treeSet.addAll(this.locales);
        return treeSet;
    }

    @Override
    public LocaleSet setElements(final SortedSet<Locale> locales) {
        final LocaleSet LocaleSet;

        if (locales instanceof LocaleSet) {
            LocaleSet = (LocaleSet) locales;
        } else {
            final TreeSet<Locale> copy = new TreeSet<>(COMPARATOR);
            copy.addAll(
                Objects.requireNonNull(locales, "locales")
            );
            LocaleSet = this.locales.equals(copy) ?
                this :
                withCopy(copy);
        }

        return LocaleSet;
    }

    private SortedSet<Locale> locales;

    @Override
    public void elementCheck(final Locale locale) {
        Objects.requireNonNull(locale, "locale");
    }

    // HasText..........................................................................................................

    @Override
    public String text() {
        return SEPARATOR.toSeparatedString(
            this,
            Locale::toLanguageTag
        );
    }

    // TreePrintable....................................................................................................

    @Override
    public void printTree(final IndentingPrinter printer) {
        for (final Locale locale : this) {
            printer.println(
                locale.toLanguageTag()
            );
        }
    }

    // Json.............................................................................................................

    /**
     * Returns a CSV string with locale tags separated by commas.
     */
    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.string(
            this.text()
        );
    }

    static void register() {
        // helps force registry of json marshaller
    }

    static LocaleSet unmarshall(final JsonNode node,
                                final JsonNodeUnmarshallContext context) {
        return parse(
            node.stringOrFail()
        );
    }

    static {
        JsonNodeContext.register(
            JsonNodeContext.computeTypeName(LocaleSet.class),
            LocaleSet::unmarshall,
            LocaleSet::marshall,
            LocaleSet.class
        );
    }
}
