package com.dzlier.markov;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.dzlier.markov.MarkovChain.Composer;
import com.dzlier.markov.MarkovChain.Node;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * Base class for unit tests on {@link MarkovChain}
 */
class MarkovChainTestBase<K, V> {
  MarkovChain<K, V> markov;
  Composer<K, V> composer;

  @SuppressWarnings("unchecked")
  void testSingleLink(V link) {
    K chain = composer.join(Lists.newArrayList(link));
    markov.process(chain);

    assertEquals(link, markov.get(chain).item);
    assertEquals(markov.generate(), chain);
  }

  @SuppressWarnings("unchecked")
  void testTwoLink(List<V> links) {
    assertEquals(2, links.size());
    K chain = composer.join(links);
    markov.process(chain);

    MarkovChain<K, V>.Node rootNode = markov.get(composer.join(links.subList(0, 1)));
    verifyNode(rootNode, links.get(0), 1);
    assertEquals(links.get(1), rootNode.random().item);

    Node childNode = rootNode.get(links.get(1));
    verifyNode(childNode, links.get(1), 1);
    assertNull(childNode.random().item);

    assertEquals(markov.generate(), chain);
  }

  @SuppressWarnings("unchecked")
  void testTwoLinkDepthOne(List<V> links) {
    assertEquals(2, links.size());
    K chain = composer.join(links);
    markov.depth = 1;
    markov.process(chain);

    verifyNode(markov.get(composer.join(links.subList(0, 1))), links.get(0), 0);

    MarkovChain<K, V>.Node childNode = markov.get(chain);
    verifyNode(childNode, links.get(1), 0);

    childNode = markov.getMid(composer.join(links.subList(1, 2)));
    verifyNode(childNode, links.get(1), 0);

    V v = null;
    assertNull(childNode.get(v));
  }

  void testThreeLinkDepthTwo(List<V> links) {
    assertEquals(3, links.size());
    K chain = composer.join(links);
    markov.depth = 2;
    markov.process(chain);

    verifyNode(markov.get(composer.join(links.subList(0, 1))), links.get(0), 1);
    verifyNode(markov.get(composer.join(links.subList(0, 2))), links.get(1), 0);
    verifyNode(markov.get(chain), links.get(2), 0);

    verifyNode(markov.getMid(composer.join(links.subList(1, 2))), links.get(1), 1);
    verifyNode(markov.getMid(composer.join(links.subList(1, 3))), links.get(2), 0);
    verifyNode(markov.getMid(composer.join(links.subList(2, 3))), links.get(2), 1);

    assertEquals(markov.generate(), chain);
  }

  /**
   * Tests a chain with a repeated link at indeces 2 and 4, such as the string chain
   * "hello world and mom and dad". Note the repeated "and" at positions 2 and 4.
   */
  void testSixLinkDepthTwoWithRepeat(List<V> links) {
    assertEquals(6, links.size());
    assertEquals(links.get(2), links.get(4));
    K chain = composer.join(links);
    markov.depth = 2;
    markov.process(chain);

    // Test the chain as it came in
    verifyNode(markov.get(composer.join(links.subList(0, 1))), links.get(0), 1);
    verifyNode(markov.get(composer.join(links.subList(0, 2))), links.get(1), 0);
    verifyNode(markov.get(composer.join(links.subList(0, 3))), links.get(2), 0);
    verifyNode(markov.get(composer.join(links.subList(0, 4))), links.get(3), 0);
    verifyNode(markov.get(composer.join(links.subList(0, 5))), links.get(4), 0);
    verifyNode(markov.get(composer.join(links.subList(0, 6))), links.get(5), 0);

    // Test variations due to the repeated link in indeces 2 and 4
    List<V> variation = Lists.newArrayList(links.subList(0, 3));
    variation.add(links.get(5));
    verifyNode(markov.get(composer.join(variation)), links.get(5), 0);

    // Test from middle of chain
    verifyNode(markov.getMid(composer.join(links.subList(1, 2))), links.get(1), 1);
    verifyNode(markov.getMid(composer.join(links.subList(1, 3))), links.get(2), 0);
    verifyNode(markov.getMid(composer.join(links.subList(2, 3))), links.get(2), 2); // repeated link
    verifyNode(markov.getMid(composer.join(links.subList(2, 4))), links.get(3), 0);
    verifyNode(markov.getMid(composer.join(links.subList(3, 4))), links.get(3), 1);
    verifyNode(markov.getMid(composer.join(links.subList(3, 5))), links.get(4), 0);
    verifyNode(markov.getMid(composer.join(links.subList(4, 5))), links.get(4), 2);
    verifyNode(markov.getMid(composer.join(links.subList(4, 6))), links.get(5), 0);
    verifyNode(markov.getMid(composer.join(links.subList(5, 6))), links.get(5), 1);

    // Test generated chain, which may include 0 or more of the sections between the repeats
    K generated = markov.generate();
    List<V> generatedLinks = composer.separate(generated);
    assertEquals(generatedLinks.subList(0, 3), links.subList(0, 3));
    List<V> end = generatedLinks.subList(generatedLinks.size() - 2, generatedLinks.size());
    assertEquals(end, links.subList(4, 6));
  }

  /**
   * Tests a chain with a repeated link at indeces 2 and 4, such as the string chain
   * "hello world and mom and dad". Note the repeated "and" at positions 2 and 4.
   * Unlike testSixLinkDepthTwoWithRepeat, a depth of 3 should mean there is no variation
   * in generated chains.
   */
  void testSixLinkDepthThreeWithRepeat(List<V> links) {
    assertEquals(6, links.size());
    assertEquals(links.get(2), links.get(4));
    K chain = composer.join(links);
    markov.depth = 3;
    markov.process(chain);

    verifyNode(markov.get(composer.join(links.subList(0, 1))), links.get(0), 1);
    verifyNode(markov.get(composer.join(links.subList(0, 2))), links.get(1), 1);
    verifyNode(markov.get(composer.join(links.subList(0, 3))), links.get(2), 0);
    verifyNode(markov.get(composer.join(links.subList(0, 4))), links.get(3), 0);
    verifyNode(markov.get(composer.join(links.subList(0, 5))), links.get(4), 0);
    verifyNode(markov.get(composer.join(links.subList(0, 6))), links.get(5), 0);

    // Test from middle of chain
    verifyNode(markov.getMid(composer.join(links.subList(1, 2))), links.get(1), 1);
    verifyNode(markov.getMid(composer.join(links.subList(1, 3))), links.get(2), 1);
    verifyNode(markov.getMid(composer.join(links.subList(2, 3))), links.get(2), 2); // repeated link
    verifyNode(markov.getMid(composer.join(links.subList(2, 4))), links.get(3), 1);
    verifyNode(markov.getMid(composer.join(links.subList(3, 4))), links.get(3), 1);
    verifyNode(markov.getMid(composer.join(links.subList(3, 5))), links.get(4), 1);
    verifyNode(markov.getMid(composer.join(links.subList(4, 5))), links.get(4), 2);
    verifyNode(markov.getMid(composer.join(links.subList(4, 6))), links.get(5), 1);
    verifyNode(markov.getMid(composer.join(links.subList(5, 6))), links.get(5), 1);

    // with a depth of 3, there should only be one way to generate the above
    assertEquals(markov.generate(), chain);
  }

  /**
   * Tests a markov chain with three inputs with the following equivalencies:
   * {a, b, c, d}
   * {a, e, c, d}
   * {f, d, c, b}
   */
  void testThreeChainsDepthTwo(List<V> links1, List<V> links2, List<V> links3) {
    assertEquals(4, links1.size());
    assertEquals(4, links2.size());
    assertEquals(4, links3.size());
    assertEquals(links1.get(0), links2.get(0));  // a == a
    assertEquals(links1.get(1), links3.get(3));  // b == b
    assertEquals(links1.get(2), links2.get(2));  // c == c
    assertEquals(links1.get(2), links3.get(2));  // c == c
    assertEquals(links1.get(3), links2.get(3));  // d == d

    K chain1 = composer.join(links1);
    K chain2 = composer.join(links2);
    K chain3 = composer.join(links3);
    markov.depth = 2;
    markov.process(chain1);
    markov.process(chain2);
    markov.process(chain3);

    verifyNode(markov.get(composer.join(links1.subList(0, 1))), links1.get(0), 2);
    verifyNode(markov.get(composer.join(links1.subList(0, 2))), links1.get(1), 0);
    verifyNode(markov.get(composer.join(links1.subList(0, 3))), links1.get(2), 0);
    verifyNode(markov.get(composer.join(links1.subList(0, 4))), links1.get(3), 0);

    verifyNode(markov.get(composer.join(links2.subList(0, 2))), links2.get(1), 0);
    verifyNode(markov.get(composer.join(links3.subList(0, 1))), links3.get(0), 1);
    verifyNode(markov.get(composer.join(links3.subList(0, 2))), links3.get(1), 0);

    verifyNode(markov.getMid(composer.join(links1.subList(1, 2))), links1.get(1), 2);
    verifyNode(markov.getMid(composer.join(links1.subList(1, 3))), links1.get(2), 0);

    verifyNode(markov.getMid(composer.join(links1.subList(2, 3))), links1.get(2), 2);
    verifyNode(markov.getMid(composer.join(links1.subList(2, 4))), links1.get(3), 0);
    verifyNode(markov.getMid(composer.join(links3.subList(2, 4))), links3.get(3), 0);

    verifyNode(markov.getMid(composer.join(links3.subList(1, 2))), links3.get(1), 2);
    verifyNode(markov.getMid(composer.join(links3.subList(1, 3))), links3.get(2), 0);

    verifyNode(markov.getMid(composer.join(links2.subList(1, 2))), links2.get(1), 1);
    verifyNode(markov.getMid(composer.join(links2.subList(1, 3))), links2.get(2), 0);
  }
//
//  private String findRandom(V expected) {
//    String actual;
//    actual = markov.get(HELLO).random().item;
//    for(int i = 0; i < 25; i++) {
//      if(actual.equals(expected)) {
//        break;
//      }
//      actual = markov.get(HELLO).random().item;
//    }
//    return actual;
//  }

  private void verifyNode(Node node, V content, int children) {
    assertEquals(content, node.item);
    assertEquals(children, node.children.size());
  }
}
