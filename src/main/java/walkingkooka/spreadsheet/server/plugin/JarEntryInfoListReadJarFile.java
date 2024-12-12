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

import javaemul.internal.annotations.GwtIncompatible;
import walkingkooka.collect.list.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class JarEntryInfoListReadJarFile extends JarEntryInfoListReadJarFileGwt {

    @GwtIncompatible
    static JarEntryInfoList readJarFile(final InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream");

        final List<JarEntryInfo> entries = Lists.array();

        try (final ZipInputStream jarInputStream = new ZipInputStream(inputStream)) {
            for (; ; ) {
                final ZipEntry zipEntry = jarInputStream.getNextEntry();
                if (null == zipEntry) {
                    break;
                }

                entries.add(
                        JarEntryInfo.with(
                                JarEntryInfoName.with(
                                        "/" + zipEntry.getName()
                                ),
                                optionalLong(
                                        zipEntry.getSize()
                                ), // size
                                optionalLong(
                                        zipEntry.getCompressedSize()
                                ), // compressedSize
                                optionalInt(
                                        zipEntry.getMethod()
                                ), // method,
                                optionalLong(
                                        zipEntry.getCrc()
                                ), // crc
                                toLocalTime(zipEntry.getCreationTime()), // create
                                toLocalTime(zipEntry.getLastModifiedTime()) // last modified
                        )
                );
            }

            return JarEntryInfoList.with(entries);
        } catch (final IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    @GwtIncompatible
    private static OptionalInt optionalInt(final int value) {
        return value > -1 ?
                OptionalInt.of(value) :
                OptionalInt.empty();
    }

    @GwtIncompatible
    private static OptionalLong optionalLong(final long value) {
        return value > -1 ?
                OptionalLong.of(value) :
                OptionalLong.empty();
    }

    @GwtIncompatible
    private static Optional<LocalDateTime> toLocalTime(final FileTime fileTime) {
        return Optional.ofNullable(
                null != fileTime ?
                        LocalDateTime.ofInstant(
                                fileTime.toInstant(),
                                GMT
                        ) :
                        null
        );
    }

    @GwtIncompatible
    private final static ZoneId GMT = ZoneId.of("GMT");

    private JarEntryInfoListReadJarFile() {
        throw new UnsupportedOperationException();
    }
}
