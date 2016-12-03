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

import com.dzlier.combine.CombiningList;
import java.util.Optional;
import java.util.function.BiFunction;

public class CombiningWeightedList<E> extends WeightedList<E> {

  private final CombiningList<Node> combiningList;
  private final BiFunction<Node, Node, Boolean> nodeMatcher;

  /**
   * List that combines elements as it adds them, with primitive equivalence when deciding whether
   * to combine. Use {@code CombiningWeightedList(BiFunction<E, E, Boolean>)} instead.
   */
  public CombiningWeightedList() {
    this((e1, e2) -> e1 == e2);
  }

  /**
   * List that combines elements as it adds them, using itemMatcher to decide whether to combine.
   *
   * @param itemMatcher {@link BiFunction} that accepts to elements E and returns boolean of whether
   * to combine them.
   */
  public CombiningWeightedList(BiFunction<E, E, Boolean> itemMatcher) {
    super();
    nodeMatcher = (n1, n2) -> itemMatcher.apply(n1.element, n2.element);
    this.combiningList = new CombiningList<>(backingList, nodeMatcher);
  }

  /**
   * Adds an element with given weight. If element already exists in list, combines them instead.
   *
   * @param weight weight to initialize element with, or add to existing element to be combined.
   * @param element element to add or combine with pre-existing elements.
   * @return Element added to list, or preexisting list item if combined.
   */
  public E add(Double weight, E element) {
    if (weight <= 0 || element == null) {
      return null;
    }

    total += weight;
    Node toAdd = new Node(weight, element);
    combiningList.add(toAdd);
    return Optional.ofNullable(combiningList.get(toAdd)).map(n -> n.element).orElse(null);
  }

  /**
   * Adds element with weight of 1.
   *
   * @param element element to add to list
   * @return Whether list was modified by this add.
   */
  @Override
  public boolean add(E element) {
    return add(1.0, element) != null || element == null;
  }
}
