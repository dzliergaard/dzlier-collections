package com.dzlier.markov;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.dzlier.markov.MarkovChain.Composer;
import com.dzlier.markov.MarkovChain.Node;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;

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
    verifyNode(markov::get, links, 0, 1, 0, 1);
    assertEquals(links.get(1), rootNode.pick().item);

    Node childNode = rootNode.get(links.get(1));
    verifyNode(markov::get, links, 0, 2, 1, 1);
    assertNull(childNode.pick().item);

    assertEquals(markov.generate(), chain);
  }

  @SuppressWarnings("unchecked")
  void testTwoLinkDepthOne(List<V> links) {
    assertEquals(2, links.size());
    K chain = composer.join(links);
    markov.process(chain);

    verifyNode(markov::get, links, 0, 1, 0, 0);
    verifyNode(markov::getMid, links, 1, 2, 1, 0);
  }

  void testThreeLinkDepthTwo(List<V> links) {
    assertEquals(3, links.size());
    K chain = composer.join(links);
    markov.process(chain);

    verifyNode(markov::get, links, 0, 1, 0, 1);
    verifyNode(markov::get, links, 0, 2, 1, 0);

    verifyNode(markov::getMid, links, 1, 2, 1, 1);
    verifyNode(markov::getMid, links, 1, 3, 2, 0);
    verifyNode(markov::getMid, links, 2, 3, 2, 1);

    assertEquals(markov.generate(), chain);
  }

  /**
   * Tests a chain with a repeated link at indeces 2 and 4, such as: {a, b, c, b, d, e}, generating
   * chains with depths of 2 and 3.
   */
  void testSixLink(List<V> links) {
    assertEquals(6, links.size());
    assertEquals(links.get(2), links.get(4));
    K chain = composer.join(links);
    markov.process(chain);

    // Test the chain as it came in
    verifyNode(markov::get, links, 0, 1, 0, 1);
    verifyNode(markov::get, links, 0, 2, 1, 1);
    verifyNode(markov::get, links, 0, 3, 2, 1);
    verifyNode(markov::get, links, 0, 4, 3, 1);
    verifyNode(markov::get, links, 0, 5, 4, 1);
    verifyNode(markov::get, links, 0, 6, 5, 1);

    // Test from middle of chain
    verifyNode(markov::getMid, links, 1, 2, 1, 1);
    verifyNode(markov::getMid, links, 1, 3, 2, 1);
    verifyNode(markov::getMid, links, 2, 3, 2, 2); // repeated link
    verifyNode(markov::getMid, links, 2, 4, 3, 1);
    verifyNode(markov::getMid, links, 3, 4, 3, 1);
    verifyNode(markov::getMid, links, 3, 5, 4, 1);
    verifyNode(markov::getMid, links, 4, 5, 4, 2);
    verifyNode(markov::getMid, links, 4, 6, 5, 1);
    verifyNode(markov::getMid, links, 5, 6, 5, 1);

    // Test generate with depth 2, which may include 0+ of the sections between the repeats
    K generated = markov.generate(2);
    List<V> generatedLinks = composer.separate(generated);
    assertEquals(generatedLinks.subList(0, 3), links.subList(0, 3));
    List<V> end = generatedLinks.subList(generatedLinks.size() - 2, generatedLinks.size());
    assertEquals(end, links.subList(4, 6));

    // Generate with depth 3+ should always be the exact input with only 1 sample.
    assertEquals(markov.generate(), chain);
    assertEquals(markov.generate(3), chain);
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
    markov.process(chain1);
    markov.process(chain2);
    markov.process(chain3);

    verifyNode(markov::get, links1, 0, 1, 0, 2);
    verifyNode(markov::get, links1, 0, 2, 1, 0);

    verifyNode(markov::get, links2, 0, 2, 1, 0);
    verifyNode(markov::get, links3, 0, 1, 0, 1);
    verifyNode(markov::get, links3, 0, 2, 1, 0);

    verifyNode(markov::getMid, links1, 1, 2, 1, 2);
    verifyNode(markov::getMid, links1, 1, 3, 2, 0);

    verifyNode(markov::getMid, links1, 2, 3, 2, 2);
    verifyNode(markov::getMid, links1, 2, 4, 3, 0);
    verifyNode(markov::getMid, links3, 2, 4, 3, 0);

    verifyNode(markov::getMid, links3, 1, 2, 1, 2);
    verifyNode(markov::getMid, links3, 1, 3, 2, 0);

    verifyNode(markov::getMid, links2, 1, 2, 1, 1);
    verifyNode(markov::getMid, links2, 1, 3, 2, 0);
  }

  private void verifyNode(Function<K, Node> getNode,
                          List<V> links,
                          int fromIndex,
                          int toIndex,
                          int expectedIndex,
                          int expectedChildren) {
    Node node = getNode.apply(composer.join(links.subList(fromIndex, toIndex)));
    assertEquals(links.get(expectedIndex), node.item);
    assertEquals(expectedChildren, node.children.size());
  }
}
