package com.dzlier.markov;

import com.dzlier.markov.MarkovChain.Composer;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;

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

    Chain(Link link) {
      this.links = new ArrayList<>();
      this.links.add(link);
      this.id = IDS++;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (!(o instanceof Chain)) return false;
      EqualsBuilder eb = new EqualsBuilder();
      eb.append(this.links, ((Chain)o).links);
      return eb.isEquals();
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
