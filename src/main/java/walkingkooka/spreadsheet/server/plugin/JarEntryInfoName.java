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

package walkingkooka.spreadsheet.server.plugin;

import walkingkooka.Cast;
import walkingkooka.compare.Comparators;
import walkingkooka.naming.Name;
import walkingkooka.predicate.character.CharPredicate;
import walkingkooka.predicate.character.CharPredicates;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.CharacterConstant;

/**
 * The filename of an entry with a JAR file archive.
 */
public final class JarEntryInfoName implements Name,
        Comparable<JarEntryInfoName> {

    /**
     * Separator character that is itself an illegal character within a {@link JarEntryInfoName} and may be used to form a range.
     */
    public final static CharacterConstant SEPARATOR = CharacterConstant.with(':');

    final static String MANIFEST_MF_STRING = "/META-INF/MANIFEST.MF";

    public final static JarEntryInfoName MANIFEST_MF = new JarEntryInfoName(MANIFEST_MF_STRING);

    /**
     * Jar file name must start with slash.
     */
    final static CharPredicate INITIAL = CharPredicates.is('/');

    final static CharPredicate PART = CharPredicates.is(SEPARATOR.character())
            .negate();

    /**
     * Only requirement is that the name starts with {@link #SEPARATOR}.
     */
    public static boolean isChar(final int pos,
                                 final char c) {
        return (
                0 == pos ?
                        INITIAL :
                        PART
        ).test(c);
    }

    public final static int MIN_LENGTH = 1;

    /**
     * The maximum valid length
     */
    public final static int MAX_LENGTH = 255;

    /**
     * Factory that creates a {@link JarEntryInfoName}
     */
    public static JarEntryInfoName with(final String name) {
        CharPredicates.failIfNullOrEmptyOrInitialAndPartFalse(
                name,
                "name",
                INITIAL,
                PART
        );
        return MANIFEST_MF_STRING.equalsIgnoreCase(name) ?
                MANIFEST_MF :
                new JarEntryInfoName(name);
    }

    /**
     * Private constructor
     */
    private JarEntryInfoName(final String name) {
        super();
        this.name = name;
    }

    @Override
    public String value() {
        return this.name;
    }

    private final String name;

    /**
     * Note MIN/MAX length is not tested in the ctor, wrappers should invoke this method after calling new with their label.
     */
    public JarEntryInfoName checkLength(final String label) {
        Name.checkLength(
                label,
                this.value(),
                MIN_LENGTH,
                MAX_LENGTH
        );

        return this;
    }

    @Override
    public CaseSensitivity caseSensitivity() {
        return CASE_SENSITIVITY;
    }

    private final static CaseSensitivity CASE_SENSITIVITY = CaseSensitivity.SENSITIVE;

    private boolean isManifest() {
        return MANIFEST_MF_STRING.equalsIgnoreCase(this.value());
    }

    // Object...........................................................................................................

    @Override
    public int hashCode() {
        return CASE_SENSITIVITY.hash(this.name);
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof JarEntryInfoName &&
                        this.equals0(Cast.to(other));
    }

    private boolean equals0(final JarEntryInfoName other) {
        return this.compareTo(other) == 0;
    }

    @Override
    public String toString() {
        return this.name;
    }

    // Comparable.......................................................................................................

    /**
     * Special cases the /META-INF/MANIFEST.MF file to be sorted first.
     */
    @Override
    public int compareTo(final JarEntryInfoName other) {
        final int compareTo;

        final boolean manifest = this.isManifest();
        final boolean otherManifest = other.isManifest();

        if (manifest || otherManifest) {
            if (manifest && otherManifest) {
                compareTo = Comparators.EQUAL;
            } else {
                compareTo = manifest ?
                        Comparators.LESS :
                        Comparators.MORE;
            }
        } else {
            compareTo = CASE_SENSITIVITY.comparator()
                    .compare(
                            this.value(),
                            other.value()
                    );
        }

        return compareTo;
    }
}