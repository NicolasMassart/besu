/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.besu.plugin.services.storage.rocksdb.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyperledger.besu.plugin.services.helper.Conditions.FILE_DOES_NOT_EXIST;
import static org.hyperledger.besu.plugin.services.helper.Conditions.FILE_EXISTS;
import static org.hyperledger.besu.plugin.services.helper.Conditions.shouldContain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DatabaseMetadataTest {
  @Rule public final TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void getVersion() {
    final DatabaseMetadata databaseMetadata = new DatabaseMetadata(42);
    assertThat(databaseMetadata).isNotNull();
    assertThat(databaseMetadata.getVersion()).isEqualTo(42);
  }

  @Test
  public void metaFileShouldBeSoughtIntoDataDirFirst() throws Exception {
    final Path tempDataDir = createAndWrite("data", "DATABASE_METADATA.json", "{\"version\":42}");
    final Path tempDatabaseDir = createAndWrite("db", "DATABASE_METADATA.json", "{\"version\":99}");
    final DatabaseMetadata databaseMetadata =
        DatabaseMetadata.lookUpFrom(tempDatabaseDir, tempDataDir);
    assertThat(databaseMetadata).isNotNull();
    assertThat(databaseMetadata.getVersion()).isEqualTo(42);
  }

  @Test
  public void metaFileNotFoundInDataDirShouldLookupIntoDbDir() throws Exception {
    final Path tempDataDir = temporaryFolder.newFolder().toPath().resolve("data");
    Files.createDirectories(tempDataDir);
    final Path tempDatabaseDir = createAndWrite("db", "DATABASE_METADATA.json", "{\"version\":42}");
    final Path metadataPathInDataDir = tempDataDir.resolve("DATABASE_METADATA.json");
    assertThat(metadataPathInDataDir).is(FILE_DOES_NOT_EXIST);
    final DatabaseMetadata databaseMetadata =
        DatabaseMetadata.lookUpFrom(tempDatabaseDir, tempDataDir);
    assertThat(databaseMetadata).isNotNull();
    assertThat(databaseMetadata.getVersion()).isEqualTo(42);
    assertThat(metadataPathInDataDir).is(FILE_EXISTS);
    assertThat(metadataPathInDataDir).is(shouldContain("{\"version\":42}"));
  }

  private Path createAndWrite(final String dir, final String file, final String content)
      throws IOException {
    return createAndWrite(temporaryFolder, dir, file, content);
  }

  private Path createAndWrite(
      final TemporaryFolder temporaryFolder,
      final String dir,
      final String file,
      final String content)
      throws IOException {
    final Path tmpDir = temporaryFolder.newFolder().toPath().resolve(dir);
    Files.createDirectories(tmpDir);
    createAndWrite(tmpDir.resolve(file), content);
    return tmpDir;
  }

  private void createAndWrite(final Path path, final String content) throws IOException {
    path.toFile().createNewFile();
    Files.writeString(path, content);
  }
}
