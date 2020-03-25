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
package org.hyperledger.besu.ethereum.blockcreation;

import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderBuilder;
import org.hyperledger.besu.ethereum.core.SealableBlockHeader;
import org.hyperledger.besu.ethereum.core.Wei;
import org.hyperledger.besu.ethereum.eth.transactions.PendingTransactions;
import org.hyperledger.besu.ethereum.mainnet.Keccak256Pow;
import org.hyperledger.besu.ethereum.mainnet.Keccak256PowSolution;
import org.hyperledger.besu.ethereum.mainnet.Keccak256PowSolver;
import org.hyperledger.besu.ethereum.mainnet.Keccak256PowSolverInputs;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.tuweni.units.bigints.UInt256;

public class Keccak256PowBlockCreator extends AbstractBlockCreator<Void> {

  private final Keccak256PowSolver nonceSolver;

  public Keccak256PowBlockCreator(
      final Address coinbase,
      final ExtraDataCalculator extraDataCalculator,
      final PendingTransactions pendingTransactions,
      final ProtocolContext<Void> protocolContext,
      final ProtocolSchedule<Void> protocolSchedule,
      final Function<Long, Long> gasLimitCalculator,
      final Keccak256PowSolver nonceSolver,
      final Wei minTransactionGasPrice,
      final BlockHeader parentHeader) {
    super(
        coinbase,
        extraDataCalculator,
        pendingTransactions,
        protocolContext,
        protocolSchedule,
        gasLimitCalculator,
        minTransactionGasPrice,
        coinbase,
        parentHeader);

    this.nonceSolver = nonceSolver;
  }

  @Override
  protected BlockHeader createFinalBlockHeader(final SealableBlockHeader sealableBlockHeader) {
    final Keccak256PowSolverInputs workDefinition = generateNonceSolverInputs(sealableBlockHeader);
    final Keccak256PowSolution solution;
    try {
      solution =
          nonceSolver.solveFor(Keccak256PowSolver.Keccak256PowSolverJob.createFromInputs(workDefinition));
    } catch (final InterruptedException ex) {
      throw new CancellationException();
    } catch (final ExecutionException ex) {
      throw new RuntimeException("Failure occurred during nonce calculations.", ex);
    }
    return BlockHeaderBuilder.create()
        .populateFrom(sealableBlockHeader)
        .mixHash(solution.getMixHash())
        .nonce(solution.getNonce())
        .blockHeaderFunctions(blockHeaderFunctions)
        .buildBlockHeader();
  }

  private Keccak256PowSolverInputs generateNonceSolverInputs(
      final SealableBlockHeader sealableBlockHeader) {
    final BigInteger difficulty = sealableBlockHeader.getDifficulty().toBigInteger();
    final UInt256 target =
        difficulty.equals(BigInteger.ONE)
            ? UInt256.MAX_VALUE
            : UInt256.valueOf(Keccak256Pow.TARGET_UPPER_BOUND.divide(difficulty));

    return new Keccak256PowSolverInputs(
        target, Keccak256Pow.hashHeader(sealableBlockHeader), sealableBlockHeader.getNumber());
  }

  public Optional<Keccak256PowSolverInputs> getWorkDefinition() {
    return nonceSolver.getWorkDefinition();
  }

  public Optional<Long> getHashesPerSecond() {
    return nonceSolver.hashesPerSecond();
  }

  public boolean submitWork(final Keccak256PowSolution solution) {
    return nonceSolver.submitSolution(solution);
  }

  @Override
  public void cancel() {
    super.cancel();
    nonceSolver.cancel();
  }
}