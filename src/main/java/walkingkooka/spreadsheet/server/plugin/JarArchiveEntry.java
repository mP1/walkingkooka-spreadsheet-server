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
import walkingkooka.tree.json.JsonNode;
import walkingkooka.tree.json.JsonPropertyName;
import walkingkooka.tree.json.marshall.JsonNodeContext;
import walkingkooka.tree.json.marshall.JsonNodeMarshallContext;
import walkingkooka.tree.json.marshall.JsonNodeUnmarshallContext;

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

    // JsonNodeContext...................................................................................................

    /**
     * Factory that creates a {@link JarArchiveEntry} parse a {@link JsonNode}.
     */
    static JarArchiveEntry unmarshall(final JsonNode node,
                                      final JsonNodeUnmarshallContext context) {
        Objects.requireNonNull(node, "node");

        String name = null;
        Boolean directory = null;
        Long size = null;
        Long compressedSize = null;
        Integer method = null;
        LocalDateTime create = null;
        LocalDateTime lastModified = null;

        for (final JsonNode child : node.objectOrFail().children()) {
            final JsonPropertyName jsonPropertyName = child.name();
            switch (jsonPropertyName.value()) {
                case NAME_PROPERTY_STRING:
                    name = context.unmarshall(
                            child,
                            String.class
                    );
                    break;
                case DIRECTORY_PROPERTY_STRING:
                    directory = context.unmarshall(
                            child,
                            Boolean.class
                    );
                    break;
                case SIZE_PROPERTY_STRING:
                    size = context.unmarshall(
                            child,
                            Long.class
                    );
                    break;
                case COMPRESSED_SIZE_PROPERTY_STRING:
                    compressedSize = context.unmarshall(
                            child,
                            Long.class
                    );
                    break;
                case METHOD_PROPERTY_STRING:
                    method = context.unmarshall(
                            child,
                            Integer.class
                    );
                    break;
                case CREATE_PROPERTY_STRING:
                    create = context.unmarshall(
                            child,
                            LocalDateTime.class
                    );
                    break;
                case LAST_MODIFIED_PROPERTY_STRING:
                    lastModified = context.unmarshall(
                            child,
                            LocalDateTime.class
                    );
                    break;
                default:
                    JsonNodeUnmarshallContext.unknownPropertyPresent(
                            jsonPropertyName,
                            node
                    );
                    break;
            }
        }

        if (null == name) {
            JsonNodeUnmarshallContext.requiredPropertyMissing(NAME_PROPERTY, node);
        }

        return with(
                name,
                directory,
                size,
                compressedSize,
                method,
                create,
                lastModified
        );
    }

    private JsonNode marshall(final JsonNodeMarshallContext context) {
        return JsonNode.object()
                .set(
                        NAME_PROPERTY,
                        context.marshall(this.name)
                ).set(
                        DIRECTORY_PROPERTY,
                        context.marshall(this.directory)
                ).set(
                        SIZE_PROPERTY,
                        context.marshall(this.size)
                ).set(
                        COMPRESSED_SIZE_PROPERTY,
                        context.marshall(this.compressedSize)
                ).set(
                        METHOD_PROPERTY,
                        context.marshall(this.method)
                ).set(
                        CREATE_PROPERTY,
                        context.marshall(this.create)
                ).set(
                        LAST_MODIFIED_PROPERTY,
                        context.marshall(this.lastModified)
                );
    }

    private final static String NAME_PROPERTY_STRING = "name";

    private final static String DIRECTORY_PROPERTY_STRING = "directory";

    private final static String SIZE_PROPERTY_STRING = "size";

    private final static String COMPRESSED_SIZE_PROPERTY_STRING = "compressedSize";

    private final static String METHOD_PROPERTY_STRING = "method";

    private final static String CREATE_PROPERTY_STRING = "create";

    private final static String LAST_MODIFIED_PROPERTY_STRING = "lastModified";

    final static JsonPropertyName NAME_PROPERTY = JsonPropertyName.with(NAME_PROPERTY_STRING);

    final static JsonPropertyName DIRECTORY_PROPERTY = JsonPropertyName.with(DIRECTORY_PROPERTY_STRING);

    final static JsonPropertyName SIZE_PROPERTY = JsonPropertyName.with(SIZE_PROPERTY_STRING);

    final static JsonPropertyName COMPRESSED_SIZE_PROPERTY = JsonPropertyName.with(COMPRESSED_SIZE_PROPERTY_STRING);

    final static JsonPropertyName METHOD_PROPERTY = JsonPropertyName.with(METHOD_PROPERTY_STRING);

    final static JsonPropertyName CREATE_PROPERTY = JsonPropertyName.with(CREATE_PROPERTY_STRING);

    final static JsonPropertyName LAST_MODIFIED_PROPERTY = JsonPropertyName.with(LAST_MODIFIED_PROPERTY_STRING);

    static {
        LocalDateTime.now();

        JsonNodeContext.register(
                JsonNodeContext.computeTypeName(JarArchiveEntry.class),
                JarArchiveEntry::unmarshall,
                JarArchiveEntry::marshall,
                JarArchiveEntry.class
        );
    }
}
