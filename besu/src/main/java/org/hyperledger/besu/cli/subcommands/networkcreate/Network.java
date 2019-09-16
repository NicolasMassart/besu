/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.hyperledger.besu.cli.subcommands.networkcreate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;

import org.hyperledger.besu.config.JsonUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Network implements Verifiable, Generatable {

  private static final String CLIQUE_GENESIS_TEMPLATE =
      "/networkcreate/clique-genesis-template.json";
  private final String name;
  private final BigInteger chainId;
  private final Clique clique;
  private final List<Account> accounts;

  private static final Logger LOG = LogManager.getLogger();

  public Network(
      @JsonProperty("name") final String name,
      @JsonProperty("chain-id") final BigInteger chainId,
      @JsonProperty("clique") final Clique clique,
      @JsonProperty("accounts") final List<Account> accounts) {

    if (chainId != null) {
      this.chainId = chainId;
    } else {
      Random random = new Random();
      this.chainId = new BigInteger(100, random);
    }

    this.name = name;
    this.clique = clique;
    this.accounts = accounts;
  }

  public String getName() {
    return name;
  }

  public BigInteger getChainId() {
    return chainId;
  }

  public Clique getClique() {
    return clique;
  }

  @JsonInclude(Include.NON_ABSENT)
  public Optional<List<Account>> getAccounts() {
    return Optional.ofNullable(accounts);
  }

  @Override
  public InitConfigurationErrorHandler verify(final InitConfigurationErrorHandler errorHandler) {
    if (isNull(name)) errorHandler.add("Network name", "null", "Network name not defined.");
    if (isNull(clique)) errorHandler.add("Network clique", "null", "Network clique not defined.");
    if (chainId.compareTo(BigInteger.ZERO) <= 0)
      errorHandler.add(
          "Network id",
          getChainId().toString(),
          "Chain ID must be a positive, greater than zero integer.");
    // TODO verify clique and accounts
    return errorHandler;
  }

  @Override
  public void generate(final Path outputDirectoryPath) {
    final Path outputGenesisFile = outputDirectoryPath.resolve("genesis.json");
    try {
      Files.write(outputGenesisFile, buildGenesis().getBytes(UTF_8), StandardOpenOption.CREATE_NEW);
      LOG.info("Genesis file wrote to {}", outputGenesisFile);
    } catch (IOException e) {
      LOG.error("Unable to write genesis file", e);
    }
  }

  private String buildGenesis() {
    try {
      final ObjectNode genesisTemplate = loadGenesisFromTemplateFile();
      ObjectNode config = (ObjectNode) genesisTemplate.get("config");

      config.put("chainId", chainId);

      ObjectNode cliqueFragment = clique.getGenesisFragment();
      config.set("clique", cliqueFragment);

      genesisTemplate.put("extraData", clique.getExtraData());

      ObjectMapper objectMapper = new ObjectMapper();

      ObjectNode alloc = (ObjectNode) genesisTemplate.get("alloc");
      for (Account account : accounts) {
        ObjectNode accountFragment = account.getGenesisFragment();
        alloc.replace(account.getAddress().toUnprefixedString(), accountFragment);
      }

      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(genesisTemplate);
    } catch (JsonProcessingException e) {
      LOG.error("Unable to generate genesis file JSON", e);
    }
    return "";
  }

  private ObjectNode loadGenesisFromTemplateFile() {
    try {
      final URL genesisTemplateFile = this.getClass().getResource(CLIQUE_GENESIS_TEMPLATE);
      final String genesisTemplateSource = Resources.toString(genesisTemplateFile, UTF_8);
      return JsonUtil.objectNodeFromString(genesisTemplateSource);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to get genesis template " + CLIQUE_GENESIS_TEMPLATE);
    }
  }
}
