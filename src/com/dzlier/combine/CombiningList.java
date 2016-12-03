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
package com.dzlier.combine;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 * Implementation of {@link List} that checks whether an element
 *
 * @param <E> Elements of the list
 */
public class CombiningList<E extends Combine<E>> extends AbstractList<E> {

  private final List<E> backingList;
  private final BiFunction<E, E, Boolean> matcher;

  /**
   * Basic {@link CombiningList} that uses primitive equivalence as a matcher to determine whether
   * to combine its elements when adding. Use {@code CombiningList(backingList, matcher)} instead.
   *
   * @param backingList Backing list to store elements in.
   */
  public CombiningList(@NonNull List<E> backingList) {
    this(backingList, (e1, e2) -> e1 == e2);
  }

  /**
   * Combines elements based on {@link BiFunction} matcher that takes accepts to E elements and
   * returns whether they should be combined or not.
   *
   * @param backingList Backing list in which to store elements.
   * @param matcher {@link BiFunction} that evaluates whether elements should be combined.
   */
  public CombiningList(@NonNull List<E> backingList, BiFunction<E, E, Boolean> matcher) {
    this.backingList = backingList;
    this.matcher = matcher;
  }

  @Override
  public E get(int index) {
    return backingList.get(index);
  }

  /**
   * Returns first list element such that input {@code BiFunction}.apply(item, e) == true.
   *
   * @return First matching element E from list.
   */
  public E get(E toFind) {
    return backingList.stream().filter(e -> matcher.apply(e, toFind)).findFirst().orElse(null);
  }

  @Override
  public int size() {
    return backingList.size();
  }

  @Override
  public int indexOf(Object o) {
    return backingList.indexOf(o);
  }

  /**
   * If list contains any elements that match matcher, combine element with each of those elements
   * Otherwise add as normal.
   *
   * @param element Element to add/combine.
   * @return Number of elements that were modified (1 if added).
   */
  @Override
  public boolean add(E element) {
    if (element == null || matcher == null) {
      return false;
    }
    boolean modified = stream()
                           .filter(e -> matcher.apply(e, element))
                           .map(e -> e.combine(element))
                           .count() > 0;
    if (!modified) {
      backingList.add(element);
    }
    return true;
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    backingList.forEach(action);
  }

  @Override
  public Iterator<E> iterator() {
    return backingList.iterator();
  }

  @Override
  public ListIterator<E> listIterator() {
    return backingList.listIterator();
  }

  @Override
  public Stream<E> parallelStream() {
    return backingList.parallelStream();
  }

  @Override
  public Stream<E> stream() {
    return backingList.stream();
  }
}
