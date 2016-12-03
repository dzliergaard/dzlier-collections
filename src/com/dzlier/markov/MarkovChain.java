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
 * vice versa. A basic example is a {@link Composer} of strings that separates and joins on spaces,
 * which is available from {@code MarkovChain.stringChain(" ")}.
 */
public class MarkovChain<K, V> {

  private final Node mid;
  private final Node root;
  @VisibleForTesting final Composer<K, V> composer;

  private final int maxDepth;

  /**
   * Create a {@link MarkovChain} that accepts and generates objects of type K by breaking them down
   * into type V via the provided {@link Composer}. Max depth option limits the size of the trees
   * the chain creates.
   *
   * @param composer Composer that can break down K's into V's, and vice versa.
   * @param maxDepth Maximum depth of chain.
   */
  public MarkovChain(Composer<K, V> composer, int maxDepth) {
    this.root = new Node(null);
    this.mid = new Node(null);
    this.composer = composer;
    this.maxDepth = Math.max(1, maxDepth);
  }

  /**
   * Create a {@link MarkovChain} that accepts and generates objects of type K by breaking them down
   * into type V via the provided {@link Composer}.
   *
   * @param composer Composer that can break down K's into V's, and vice versa.
   */
  public MarkovChain(Composer<K, V> composer) {
    this.root = new Node(null);
    this.mid = new Node(null);
    this.composer = composer;
    this.maxDepth = Integer.MAX_VALUE;
  }

  /**
   * Create a maximum maxDepth {@link MarkovChain} that accepts and generates objects of type K by
   * breaking them down into type V via the provided separator and joiner functions.
   *
   * @param separator Function that separates K's into chains of V's.
   * @param joiner Function that joins chains of V's back into K's.
   */
  public MarkovChain(Function<K, List<V>> separator, Function<List<V>, K> joiner) {
    this(new Composer<>(separator, joiner));
  }

  /**
   * Create a {@link MarkovChain} that accepts and generates objects of type K by breaking them down
   * into type V via the provided separator and joiner functions. Max depth option limits the size
   * of the trees the chain creates.
   *
   * @param separator Function that separates K's into chains of V's.
   * @param joiner Function that joins chains of V's back into K's.
   * @param maxDepth Maximum maxDepth of chain.
   */
  public MarkovChain(Function<K, List<V>> separator, Function<List<V>, K> joiner, int maxDepth) {
    this(new MarkovChain.Composer<>(separator, joiner), maxDepth);
  }

  /**
   * Creates a {@link MarkovChain} that transforms strings by splitting and joining them on the
   * provided delimiter.
   *
   * @param delimiter {@link String} to split & join Strings on.
   * @param maxDepth Maximum depth of chain.
   * @return New {@link MarkovChain}.
   */
  public static MarkovChain<String, String> stringChain(String delimiter, int maxDepth) {
    Splitter splitter = Splitter.on(delimiter);
    Joiner joiner = Joiner.on(delimiter);
    return new MarkovChain<>(new Composer<>(splitter::splitToList, joiner::join), maxDepth);
  }

  /**
   * Creates a {@link MarkovChain} that transforms strings by splitting and joining them on the
   * provided delimiter. Max depth option limits the size of the trees the chain creates.
   *
   * @param delimiter {@link String} to split & join Strings on.
   * @return New {@link MarkovChain}.
   */
  public static MarkovChain<String, String> stringChain(String delimiter) {
    return stringChain(delimiter, Integer.MAX_VALUE);
  }

  /**
   * Splits the provided item K into series of 0 or more V's, and adds them to the markov chain.
   *
   * @param item K to split.
   */
  public void process(K item) {
    process(item, 1.0);
  }

  /**
   * Similar to {@code process(K)}, but forces the given weight instead of using 1.
   * @param item Item to be processed.
   * @param weight Forced added weight of object in chain.
   */
  public void process(K item, Double weight) {
    List<V> chain = Lists.newLinkedList(this.composer.separate(item));
    if (chain.size() <= maxDepth) {
      root.add(chain, weight).isEnd(weight);
    } else {
      root.add(chain.subList(0, maxDepth), weight);
    }
    while (chain.size() > 0) {
      chain.remove(0);
      Node node = mid.add(chain.subList(0, Math.min(chain.size(), maxDepth)), weight);
      if (chain.size() < maxDepth) {
        node.isEnd(weight);
      }
    }
  }

  /**
   * Generate K of probabilistically sequenced components V, with the seed maxDepth provided. Seed
   * maxDepth determines how many links back the chain looks when choosing a next segment.
   *
   * @param depth Desired seed depth, up to max depth of tree.
   * @return New K probabilistically resembling sample base, based on seed depth.
   */
  public K generate(int depth) {
    depth = Math.min(depth, maxDepth);
    List<V> seed = new LinkedList<>();
    Node node = root.pick();
    while (seed.size() < depth) {
      if (node.item == null) {
        break;
      }
      seed.add(node.item);
      node = node.pick();
    }
    // Null-value node indicates natural end of chain.
    if (node != null && node.item == null) {
      return composer.join(seed);
    }
    node = mid.get(seed.subList(1, seed.size())).pick();
    while (node != null && !node.isEnd) {
      Optional.ofNullable(node.item).ifPresent(seed::add);
      node = mid.get(seed.subList(seed.size() - depth + 1, seed.size())).pick();
    }
    return composer.join(seed);
  }

  /**
   * Generate a K comprised of probabilistically sequenced components V, with maximum seed maxDepth.
   * Seed maxDepth determines how many links back the chain looks when choosing a next segment.
   *
   * @return Item K probabilistically resembling sample base based on seed maxDepth.
   */
  public K generate() {
    return generate(Integer.MAX_VALUE);
  }

  @VisibleForTesting
  Node get(K item) {
    return root.get(this.composer.separate(item));
  }

  @VisibleForTesting
  Node getMid(K item) {
    return mid.get(this.composer.separate(item));
  }

  @VisibleForTesting
  class Node {

    @VisibleForTesting final V item;
    @VisibleForTesting CombiningWeightedList<Node> children;
    final boolean isEnd;

    Node(V item) {
      children = new CombiningWeightedList<>(Node::matches);
      this.item = item;
      this.isEnd = false;
    }

    private Node() {
      this.item = null;
      this.isEnd = true;
    }

    Node add(List<V> chain, Double weight) {
      Node node = this;
      for (V link : chain) {
        Node newNode = new Node(link);
        node.children.add(weight, newNode);
        node = node.children.findFirst(n -> n.matches(newNode)).orElse(null);
      }
      return node;
    }

    Node isEnd(Double weight) {
      Node endNode = new Node();
      children.add(weight, endNode);
      return children.findFirst(n -> n.matches(null)).orElse(null);
    }

    Node get(V... chain) {
      return get(Lists.newArrayList(chain));
    }

    Node get(List<V> chain) {
      Node node = this;
      for (V link : chain) {
        if (node == null) {
          break;
        }
        node = node.children.findFirst(n -> n.matches(new Node(link))).orElse(null);
      }
      return node;
    }

    Node pick() {
      return children.random();
    }

    private boolean matches(Node that) {
      if (that == null) {
        return false;
      }
      if (this.isEnd || that.isEnd) {
        return this.isEnd && that.isEnd;
      }
      return this.item == null ? that.item == null : this.item.equals(that.item);
    }
  }

  /**
   * Used by {@link MarkovChain} to break down items of type K1 into chains of V1's, and vice versa.
   */
  public static class Composer<K1, V1> {

    private final Function<K1, List<V1>> separatorFunction;
    private final Function<List<V1>, K1> joinerFunction;

    /**
     * Create a {@link Composer} that uses provided separator and joiner functions to break K1's
     * into chains of V1's, and vice versa.
     *
     * @param separator Function that separates K1's into chains of V1's.
     * @param joiner Function that joins chains of V1's into K1's.
     */
    public Composer(Function<K1, List<V1>> separator, Function<List<V1>, K1> joiner) {
      this.separatorFunction = separator;
      this.joinerFunction = joiner;
    }

    List<V1> separate(K1 t) {
      return separatorFunction.apply(t);
    }

    K1 join(List<V1> ts) {
      return joinerFunction.apply(ts);
    }
  }

}
