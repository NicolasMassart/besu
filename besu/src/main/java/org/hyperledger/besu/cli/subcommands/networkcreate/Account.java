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

import static java.util.Objects.requireNonNull;

import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.Wei;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.checkerframework.checker.nullness.qual.NonNull;

// TODO Handle errors
class Account implements GenesisFragmentable {
  private Address address;
  private Wei balance;

  public Account(
      @NonNull @JsonProperty("address") Address address,
      @NonNull @JsonProperty("balance") Wei balance) {
    this.address = requireNonNull(address, "Account address not defined.");
    this.balance = requireNonNull(balance, "Account balance not defined.");
  }

  public Address getAddress() {
    return address;
  }

  public Wei getBalance() {
    return balance;
  }

  @JsonIgnore
  @Override
  public ObjectNode getGenesisFragment() {
    final ObjectMapper mapper = new ObjectMapper();
    final ObjectNode fragment = mapper.createObjectNode();
    fragment.put("balance", balance.toString());
    return fragment;
  }
}
