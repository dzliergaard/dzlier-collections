package com.dzlier.markov;

import com.dzlier.markov.MarkovChain.Composer;
import java.util.List;
import lombok.Getter;

/**
 * A top-level class that contains key and value classes for a {@link MarkovChain} to break down
 */
class Markov {
  static class Chain {
    static int IDS = 0;
    final int id;
    @Getter final List<Link> links;

    Chain(List<Link> links) {
      this.links = links;
      this.id = IDS++;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) {
        return false;
      }
      if (!(o instanceof Chain)) {
        return false;
      }
      Chain that = (Chain) o;
      return this.links.equals(that.links);
    }
  }

  static class Link {
    static int IDS = 0;
    final int id;

    Link() {
      this.id = IDS++;
    }

    static Chain makeChain(List<Link> list) {
      return new Chain(list);
    }
  }

  static class ChainComposer extends Composer<Chain, Link> {
    ChainComposer() {
      super(Chain::getLinks, Link::makeChain);
    }
  }
}
