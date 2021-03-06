/*
 * Collection utility classes
 * Copyright (C) 2016 Dane Zeke Liergaard
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.dzlier.markov;

import com.google.common.base.Splitter;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for {@link MarkovChain} on Strings.
 */
public class MarkovChainStringTest extends MarkovChainTestBase<String, String> {
  public static final Splitter SPLITTER = Splitter.on(" ");

  @Test
  public void testSingleLink() {
    markov = MarkovChain.stringChain(" ");
    composer = markov.composer;
    testSingleLink("hello");
  }

  @Test
  public void testTwoLink() {
    markov = MarkovChain.stringChain(" ");
    composer = markov.composer;
    testTwoLink(SPLITTER.splitToList("hello world"));
  }

  @Test
  public void testTwoLinkDepthOne() {
    markov = MarkovChain.stringChain(" ", 1);
    composer = markov.composer;
    testTwoLinkDepthOne(SPLITTER.splitToList("hello world"));
  }

  @Test
  public void testThreeLinkDepthTwo() {
    markov = MarkovChain.stringChain(" ", 2);
    composer = markov.composer;
    testThreeLinkDepthTwo(SPLITTER.splitToList("hello again world"));
  }

  @Test
  public void testSixLink() {
    markov = MarkovChain.stringChain(" ");
    composer = markov.composer;
    testSixLink(SPLITTER.splitToList("hello world and mom and dad"));
  }


  @Test
  public void testThreeChainsDepthTwo() {
    markov = MarkovChain.stringChain(" ", 2);
    composer = markov.composer;
    List<String> chain1 = SPLITTER.splitToList("hello world and dad");
    List<String> chain2 = SPLITTER.splitToList("hello mom and dad");
    List<String> chain3 = SPLITTER.splitToList("goodbye dad and world");
    testThreeChainsDepthTwo(chain1, chain2, chain3);
  }
}
