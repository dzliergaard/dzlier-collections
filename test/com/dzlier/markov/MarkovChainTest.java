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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.dzlier.markov.MarkovChain.Node;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/**
 * Unit tests for {@link MarkovChain}
 */
public class MarkovChainTest {

  private static final String HELLO = "hello";
  private static final String WORLD = "world";
  private static final String AGAIN = "again";
  private static final String AND = "and";
  private static final String DAD = "dad";
  private static final String MOM = "mom";
  private MarkovChain<String, String> markov;

  @Test
  public void testSingleLink() {
    markov = MarkovChain.stringChain(" ");
    markov.separateAndAdd(HELLO);

    assertEquals(HELLO, markov.get(HELLO).item);
    assertEquals(markov.generate(), HELLO);
  }

  @Test
  public void testTwoLink() {
    markov = MarkovChain.stringChain(" ");
    markov.separateAndAdd("hello world");

    MarkovChain<String, String>.Node helloNode = markov.get(HELLO);
    verifyNode(helloNode, HELLO, 1);
    assertEquals(WORLD, helloNode.random().item);
    Node worldNode = helloNode.get(WORLD);
    verifyNode(worldNode, WORLD, 1);
    assertNull(worldNode.random().item);

    assertEquals(markov.generate(), "hello world");
  }

  @Test
  public void testTwoLinkDepthOne() {
    markov = MarkovChain.stringChain(" ", 1);
    markov.separateAndAdd("hello world");
    assertEquals(markov.depth, 1);

    verifyNode(markov.get(HELLO), HELLO, 0);

    MarkovChain<String, String>.Node worldNode = markov.get("hello world");
    verifyNode(worldNode, WORLD, 0);
    assertNull(worldNode.get((String)null));
  }

  @Test
  public void testThreeLinkDepthTwo() {
    markov = MarkovChain.stringChain(" ", 2);
    markov.separateAndAdd("hello again world");
    assertEquals(markov.depth, 2);

    verifyNode(markov.get(HELLO), HELLO, 1);
    verifyNode(markov.get("hello again"), AGAIN, 0);
    verifyNode(markov.get("hello again world"), WORLD, 0);

    assertEquals(markov.generate(), "hello again world");
  }

  @Test
  public void testSixLinkDepthTwo() {
    markov = MarkovChain.stringChain(" ", 2);
    markov.separateAndAdd("hello world and hello mom and dad");
    assertEquals(markov.depth, 2);

    verifyNode(markov.get(HELLO), HELLO, 1);
    verifyNode(markov.get("hello world"), WORLD, 0);
    verifyNode(markov.get("hello world and"), AND, 0);
    verifyNode(markov.get("hello world and hello"), HELLO, 0);
    verifyNode(markov.get("hello world and dad"), DAD, 0);
    verifyNode(markov.get("hello world and hello mom"), MOM, 0);
    verifyNode(markov.get("hello world and hello mom and"), AND, 0);
    verifyNode(markov.get("hello world and hello mom and dad"), DAD, 0);

    verifyNode(markov.mid.get(AND), AND, 2);

    String generated = markov.generate();
    // could be 0 or more "mom and"s in between beginning and end
    assertThat(generated, CoreMatchers.startsWith("hello world and"));
    assertThat(generated, CoreMatchers.endsWith("and dad"));
  }

  @Test
  public void testSixLinkDepthThree() {
    markov = MarkovChain.stringChain(" ", 3);
    String sample = "hello world and hello mom and dad";
    markov.separateAndAdd(sample);
    assertEquals(markov.depth, 3);

    verifyNode(markov.get(HELLO), HELLO, 1);
    verifyNode(markov.get("hello world"), WORLD, 1);
    verifyNode(markov.get("hello world and"), AND, 0);
    verifyNode(markov.get("hello world and hello"), HELLO, 0);
    verifyNode(markov.get("hello world and hello mom"), MOM, 0);
    verifyNode(markov.get("hello world and hello mom and"), AND, 0);
    verifyNode(markov.get("hello world and hello mom and dad"), DAD, 0);
    verifyNode(markov.mid.get(AND), AND, 2);

    // with a depth of 3, there should only be one way to generate the above
    assertEquals(markov.generate(), sample);
  }

  @Test
  public void testTwoChainsDepthTwo() {
    markov = MarkovChain.stringChain(" ", 2);
    markov.separateAndAdd("hello world and dad");
    markov.separateAndAdd("hello mom and dad");
    assertEquals(markov.depth, 2);

    verifyNode(markov.get(HELLO), HELLO, 2);
    verifyNode(markov.get("hello world"), WORLD, 0);
    verifyNode(markov.get("hello mom"), MOM, 0);
    verifyNode(markov.get("hello world and"), AND, 0);
    verifyNode(markov.get("hello mom and"), AND, 0);
    verifyNode(markov.get("hello world and dad"), DAD, 0);
    verifyNode(markov.get("hello mom and dad"), DAD, 0);
    verifyNode(markov.mid.get(AND), AND, 1);
    verifyNode(markov.mid.get(AND, DAD), DAD, 0);
    verifyNode(markov.mid.get(WORLD), WORLD, 1);
    verifyNode(markov.mid.get(MOM), MOM, 1);

    String generated = markov.generate();
    // could be 0 or more "mom and"s in between beginning and end
    assertThat(generated, CoreMatchers.startsWith(HELLO));
    assertThat(generated, CoreMatchers.endsWith("and dad"));

    assertEquals(WORLD, findRandom(WORLD));
    assertEquals(MOM, findRandom(MOM));
  }

  private String findRandom(String expected) {
    String actual;
    actual = markov.get(HELLO).random().item;
    for(int i = 0; i < 25; i++) {
      if(actual.equals(expected)) {
        break;
      }
      actual = markov.get(HELLO).random().item;
    }
    return actual;
  }

  private void verifyNode(Node node, String content, int children) {
    assertEquals(content, node.item);
    assertEquals(children, node.children.size());
  }
}
