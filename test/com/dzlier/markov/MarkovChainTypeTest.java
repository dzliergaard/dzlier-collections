package com.dzlier.markov;

import com.dzlier.markov.Markov.Chain;
import com.dzlier.markov.Markov.ChainComposer;
import com.dzlier.markov.Markov.Link;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;

/**
 * Unit tests for {@link MarkovChain} on complex types.
 */
public class MarkovChainTypeTest extends MarkovChainTestBase<Chain, Link> {
  @Test
  public void testSingleLink() {
    composer = new ChainComposer();
    markov = new MarkovChain<>(composer);
    Link link = new Link();
    testSingleLink(link);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTwoLink() {
    composer = new ChainComposer();
    markov = new MarkovChain<>(composer);
    List<Link> links = Lists.newArrayList(new Link(), new Link());
    testTwoLink(links);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTwoLinkDepthOne() {
    composer = new ChainComposer();
    markov = new MarkovChain<>(composer, 1);
    List<Link> links = Lists.newArrayList(new Link(), new Link());
    testTwoLinkDepthOne(links);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testThreeLinkDepthTwo() {
    composer = new ChainComposer();
    markov = new MarkovChain<>(composer, 2);
    List<Link> links = Lists.newArrayList(new Link(), new Link(), new Link());
    testThreeLinkDepthTwo(links);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSixLink() {
    composer = new ChainComposer();
    markov = new MarkovChain<>(composer);
    List<Link> links = Lists.newArrayList();
    links.add(new Link());
    links.add(new Link());
    links.add(new Link());
    links.add(new Link());
    // link in indeces 2 and 5 should be the same
    links.add(links.get(2));
    links.add(new Link());
    testSixLink(links);
  }

  @Test
  public void testThreeChainsDepthTwo() {
    composer = new ChainComposer();
    markov = new MarkovChain<>(composer, 2);
    Link a = new Link();
    Link b = new Link();
    Link c = new Link();
    Link d = new Link();
    Link e = new Link();
    Link f = new Link();
    List<Link> chain1 = Lists.newArrayList(a, b, c, d);
    List<Link> chain2 = Lists.newArrayList(a, e, c, d);
    List<Link> chain3 = Lists.newArrayList(f, d, c, b);
    testThreeChainsDepthTwo(chain1, chain2, chain3);
  }

}
