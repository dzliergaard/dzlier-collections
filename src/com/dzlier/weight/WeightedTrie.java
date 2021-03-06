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

package com.dzlier.weight;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Implementation of Trie with weighted children to choose from
 *
 * Deprecated: Use {@link com.dzlier.markov.MarkovChain} instead.
 */
@Deprecated
public class WeightedTrie<T> {

  @VisibleForTesting
  TrieNode root;

  public WeightedTrie() {
    this.root = new TrieNode(null);
  }

  /**
   * Adds the chain of non-null T elements down through the trie. If a part of the chain already
   * exists, it increases that part's weight by weight
   *
   * @param weight weight to add to each element of chain
   * @param chain chain of elements to add from the root down. Each item in the chain is added as a
   * child of the previous element.
   * @return Whether TrieNode was modified as a result of the action.
   */
  public boolean addChain(Double weight, T[] chain) {
    return root.addChain(weight, chain) != null;
  }

  /**
   * @param defaultValue default value to return if the chain does not exist
   * @param groups in-order ancestors of the level from which we want a random child
   * @return Random child of last element in groups, or defaultValue if chain does not exist
   */
  @SafeVarargs
  public final T random(T defaultValue, T... groups) {
    return root.random(groups).orElse(defaultValue);
  }

  /**
   * @param num number of top items to get from the resultant list
   * @param groups in-order ancestors of the level from which we want the top num children
   */
  @SafeVarargs
  public final List<T> top(Integer num, T... groups) {
    List<TrieNode> list = root
        .get(groups)
        .map(TrieNode::getChildren)
        .map(children -> children.top(num))
        .orElse(new ArrayList<>());
    return list.stream().map(n -> n.item).collect(Collectors.toList());
  }

  @VisibleForTesting
  class TrieNode {

    @VisibleForTesting
    T item;
    @VisibleForTesting
    @Getter(AccessLevel.PRIVATE)
    CombiningWeightedList<TrieNode> children;

    TrieNode(T item) {
      this.item = item;
      this.children = new CombiningWeightedList<TrieNode>(TrieNode::matches);
    }

    TrieNode addChain(Double weight, T[] groups) {
      TrieNode node = this;
      for (T group : groups) {
        if (group == null) {
          return null;
        }

        node = node.children.add(weight, new TrieNode(group));
      }
      return node;
    }

    Optional<TrieNode> get(T[] groups) {
      Optional<TrieNode> node = Optional.of(this);
      for (T group : groups) {
        node = node
            .map(TrieNode::getChildren)
            .map(children -> children.findFirst(t -> t.matches(new TrieNode(group))))
            .orElse(Optional.empty());
      }
      return node;
    }

    T random() {
      if (this.children.isEmpty()) {
        return null;
      }
      return this.children.random().item;
    }

    Optional<T> random(T[] groups) {
      return get(groups).map(TrieNode::random);
    }

    boolean matches(TrieNode that) {
      return that != null && (this.item == null ? that.item == null : this.item.equals(that.item));
    }
  }
}
