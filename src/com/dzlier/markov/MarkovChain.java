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

import com.dzlier.weight.CombiningWeightedList;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Java implementation of a Markov chain of generic type. When creating a Markov chain, you must
 * provide a {@link Composer} which is able to separate a single element of K into multiple V's, and
 * vice versa. A basic example is a Composer of strings that separates and joins on spaces, which is
 * available from {@code MarkovChain.stringChain(" ")}.
 */
public class MarkovChain<K, V> {

  private final Node mid;
  private final Node root;
  @VisibleForTesting
  final Composer<K, V> composer;

  @VisibleForTesting
  int depth;

  public MarkovChain(Composer<K, V> composer, int depth) {
    this.root = new Node(null);
    this.mid = new Node(null);
    this.depth = depth;
    this.composer = composer;
  }

  public MarkovChain(Composer<K, V> composer) {
    this(composer, Integer.MAX_VALUE);
  }

  public MarkovChain(Function<K, List<V>> separator, Function<List<V>, K> joiner) {
    this(new Composer<>(separator, joiner));
  }

  public MarkovChain(Function<K, List<V>> separator, Function<List<V>, K> joiner, int depth) {
    this(new Composer<>(separator, joiner), depth);
  }

  /**
   * Creates a {@link MarkovChain} with no depth limit that transforms strings by splitting and
   * joining them on the provided delimiter
   *
   * @param delimiter {@link String} to split & join Strings on
   * @return new {@link MarkovChain}
   */
  public static MarkovChain<String, String> stringChain(String delimiter) {
    return stringChain(delimiter, Integer.MAX_VALUE);
  }

  /**
   * Creates a {@link MarkovChain} with provided depth that transforms strings by splitting and
   * joining them on the provided delimiter
   *
   * @param delimiter {@link String} to split & join Strings on
   * @param depth depth of chain
   * @return new {@link MarkovChain}
   */
  public static MarkovChain<String, String> stringChain(String delimiter, int depth) {
    Splitter splitter = Splitter.on(delimiter);
    Joiner joiner = Joiner.on(delimiter);
    return new MarkovChain<>(new Composer<>(splitter::splitToList, joiner::join), depth);
  }

  /**
   * Using the {@link Composer}, splits the provided item K into series of 0 or more V's, and adds
   * them to the markov chain
   *
   * @param item K to split into series of 0 or more V's.
   */
  public void process(K item) {
    List<V> chain = Lists.newLinkedList(this.composer.separate(item));
    if (chain.size() <= depth) {
      root.add(chain).end();
    } else {
      root.add(chain.subList(0, depth));
      Node node = mid;
      while (chain.size() > 0) {
        chain.remove(0);
        node = mid.add(chain.subList(0, Math.min(chain.size(), depth)));
        if (chain.size() < depth) {
          node.end();
        }
      }
    }
  }

  /**
   * Using all the previously provided sample K's, generate a K comprised of probabilistically
   * sequenced components V.
   *
   * @return An item K probabilistically resembling previous examples given.
   */
  public K generate() {
    List<V> chain = new LinkedList<>();
    Node node = root.random();
    while (chain.size() < depth && node.item != null) {
      chain.add(node.item);
      node = node.random();
    }
    // Null-value node indicates natural end of chain.
    if (node != null && node.item == null) {
      return composer.join(chain);
    }
    node = mid.get(chain.subList(1, chain.size())).random();
    while (node != null && node.item != null) {
      Optional.ofNullable(node.item).ifPresent(chain::add);
      node = mid.get(chain.subList(chain.size() - depth + 1, chain.size())).random();
    }
    return composer.join(chain);
  }

  @VisibleForTesting
  Node get(K item) {
    List<V> chain = this.composer.separate(item);
    if (chain.size() <= depth) {
      return root.get(chain);
    }
    return mid.get(chain.subList(chain.size() - depth, chain.size()));
  }

  @VisibleForTesting
  Node getMid(K item) {
    return mid.get(this.composer.separate(item));
  }

  @VisibleForTesting
  class Node {

    @VisibleForTesting final V item;
    @VisibleForTesting CombiningWeightedList<Node> children;

    Node(V item) {
      children = new CombiningWeightedList<>();
      this.item = item;
    }

    Node add(List<V> chain) {
      Node node = this;
      for (V link : chain) {
        node.children.add(new Node(link), n -> n.matchesItem(link));
        node = node.children.findFirst(n -> n.matchesItem(link)).orElse(null);
      }
      return node;
    }

    Node end() {
      children.add(new Node(null), n -> n.matchesItem(null));
      return children.findFirst(n -> n.matchesItem(null)).orElse(null);
    }

    Node get(V... chain) {
      return get(Lists.newArrayList(chain));
    }

    Node get(List<V> chain) {
      Node node = this;
      for (V link : chain) {
        node = node.children.findFirst(n -> n.matchesItem(link)).orElse(null);
      }
      return node;
    }

    Node random() {
      return children.random();
    }

    private boolean matchesItem(V that) {
      if (this.item == null) {
        return that == null;
      }
      return this.item.equals(that);
    }
  }

  public static class Composer<K1, V1> {

    private final Function<K1, List<V1>> separatorFunction;
    private final Function<List<V1>, K1> joinerFunction;

    public Composer(Function<K1, List<V1>> separator, Function<List<V1>, K1> joiner) {
      this.separatorFunction = separator;
      this.joinerFunction = joiner;
    }

    public List<V1> separate(K1 t) {
      return separatorFunction.apply(t);
    }

    public K1 join(List<V1> ts) {
      return joinerFunction.apply(ts);
    }
  }

}
