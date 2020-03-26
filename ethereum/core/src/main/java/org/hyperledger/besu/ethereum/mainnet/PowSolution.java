/*
 * Copyright 2020 Whiteblock Inc.
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
package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.ethereum.core.Hash;

import java.util.Arrays;
import java.util.Objects;

public class PowSolution {
  final long nonce;
  final byte[] powHash;

  public PowSolution(final long nonce, final byte[] powHash) {
    this.nonce = nonce;
    this.powHash = powHash;
  }

  public long getNonce() {
    return nonce;
  }

  public byte[] getPowHash() {
    return powHash;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PowSolution that = (PowSolution) o;
    return nonce == that.nonce
        && Arrays.equals(powHash, that.powHash);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(nonce);
    result = 31 * result + Arrays.hashCode(powHash);
    return result;
  }
}
