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
import walkingkooka.ToStringBuilder;
import walkingkooka.ToStringBuilderOption;
import walkingkooka.text.CharSequences;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An individual file or directory entry within a JAR file.
 */
public final class JarArchiveEntry {

    public static JarArchiveEntry with(final String name,
                                       final boolean directory,
                                       final long size,
                                       final long compressedSize,
                                       final int method,
                                       final LocalDateTime create,
                                       final LocalDateTime lastModified) {
        return new JarArchiveEntry(
                CharSequences.failIfNullOrEmpty(name, "name"),
                directory,
                checkPositiveNumber(size, "size"),
                checkPositiveNumber(compressedSize, "compressedSize"),
                checkPositiveNumber(method, "method"),
                Objects.requireNonNull(create, "create"),
                Objects.requireNonNull(lastModified, "lastModified")
        );
    }

    private static int checkPositiveNumber(final int value,
                                           final String label) {
        if (value < 0) {
            throw new IllegalArgumentException("Invalid " + label + " " + value + " < 0");
        }
        return value;
    }

    private static long checkPositiveNumber(final long value,
                                            final String label) {
        if (value < 0) {
            throw new IllegalArgumentException("Invalid " + label + " " + value + " < 0");
        }
        return value;
    }

    private JarArchiveEntry(final String name,
                            final boolean directory,
                            final long size,
                            final long compressedSize,
                            final int method,
                            final LocalDateTime create,
                            final LocalDateTime lastModified) {
        this.name = name;
        this.directory = directory;
        this.size = size;
        this.compressedSize = compressedSize;
        this.method = method;
        this.create = create;
        this.lastModified = lastModified;
    }

    public String name() {
        return this.name;
    }

    private final String name;

    public boolean isDirectory() {
        return this.directory;
    }

    private final boolean directory;

    public long size() {
        return this.size;
    }

    private final long size;

    public long compressedSize() {
        return this.compressedSize;
    }

    private final long compressedSize;

    public int method() {
        return this.method;
    }

    private final int method;

    public LocalDateTime create() {
        return this.create;
    }

    private final LocalDateTime create;

    public LocalDateTime lastModified() {
        return this.lastModified;
    }

    private final LocalDateTime lastModified;

    // HashCodeEqualsDefined..........................................................................................

    @Override
    public int hashCode() {
        return Objects.hash(
                this.name,
                this.directory,
                this.size,
                this.compressedSize,
                this.method,
                this.create,
                this.lastModified
        );
    }

    @Override
    public boolean equals(final Object other) {
        return this == other ||
                other instanceof JarArchiveEntry &&
                        this.equals0(Cast.to(other));
    }

    private boolean equals0(final JarArchiveEntry other) {
        return this.name.equals(other.name) &&
                this.directory == other.directory &&
                this.size == other.size &&
                this.compressedSize == other.compressedSize &&
                this.method == other.method &&
                this.create.equals(other.create) &&
                this.lastModified.equals(other.lastModified);
    }

    @Override
    public String toString() {
        return ToStringBuilder.empty()
                .disable(ToStringBuilderOption.SKIP_IF_DEFAULT_VALUE)
                .value(this.name)
                .value(this.directory ? "(directory)" : "(file)")
                .label("size")
                .value(this.size)
                .label("compressedSize")
                .value(this.compressedSize)
                .label("method")
                .value(this.method)
                .label("create")
                .value(this.create)
                .label("lastModified")
                .value(this.lastModified)
                .build();
    }
}
