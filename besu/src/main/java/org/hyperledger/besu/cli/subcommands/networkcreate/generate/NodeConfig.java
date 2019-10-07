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

package org.hyperledger.besu.cli.subcommands.networkcreate.generate;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import com.moandjiezana.toml.TomlWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeConfig {

  private static final Logger LOG = LogManager.getLogger();

  private final StringBuilder stringBuilder = new StringBuilder();
  private final TomlWriter writer = new TomlWriter.Builder().build();
  private static final String CONFIG_FILENAME = "config.toml";

  public void write(final Path nodeDir) {
    LOG.debug(stringBuilder.toString());
    try {
      Files.write(
          nodeDir.resolve(CONFIG_FILENAME),
          stringBuilder.toString().getBytes(UTF_8),
          StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      LOG.error("Unable to write node configuration file", e);
    }
  }

  public NodeConfig addEmptyLine() {
    stringBuilder.append(System.lineSeparator());
    return this;
  }

  public NodeConfig addComment(final String comment) {
    stringBuilder.append("# ").append(comment).append(System.lineSeparator());
    return this;
  }

  public NodeConfig addOption(final String optionName, final Object optionValue) {
    stringBuilder.append(writer.write(Map.of(optionName, optionValue)));
    return this;
  }
}
