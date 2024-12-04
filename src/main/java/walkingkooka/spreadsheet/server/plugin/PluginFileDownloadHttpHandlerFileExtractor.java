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
import walkingkooka.Binary;
import walkingkooka.net.header.ContentDispositionFileName;
import walkingkooka.net.header.ContentDispositionType;
import walkingkooka.net.header.HttpHeaderName;
import walkingkooka.net.header.MediaType;
import walkingkooka.net.http.HttpEntity;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Given a JAR file tries to extract a file with the requested filename and also identifies the content-type of the response.
 */
class PluginFileDownloadHttpHandlerFileExtractor extends PluginFileDownloadHttpHandlerFileExtractorGwt {

    @GwtIncompatible
    static HttpEntity extractFile(final Binary binary,
                                  final String filename,
                                  final BiFunction<String, Binary, MediaType> contentTypeDetector) throws IOException {
        HttpEntity response = HttpEntity.EMPTY;

        final String filenameWithoutLeadingSlash = filename.substring(1);

        try (final ZipInputStream zipInputStream = new ZipInputStream(binary.inputStream())) {
            for (; ; ) {
                final ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (null == zipEntry) {
                    break;
                }

                if (filenameWithoutLeadingSlash.equals(zipEntry.getName())) {
                    final Binary content = Binary.with(
                            zipInputStream.readAllBytes()
                    );

                    response = HttpEntity.EMPTY.setContentType(
                                    contentTypeDetector.apply(
                                            filename,
                                            content
                                    )
                            ).addHeader(
                                    HttpHeaderName.CONTENT_DISPOSITION,
                                    ContentDispositionType.ATTACHMENT.setFilename(
                                            ContentDispositionFileName.notEncoded(filename)
                                    )
                            ).setBody(content)
                            .setContentLength();
                    break;
                }
            }
        }

        return response;
    }

    PluginFileDownloadHttpHandlerFileExtractor() {
        throw new UnsupportedOperationException();
    }
}
