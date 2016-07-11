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

import lombok.NonNull;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link List} that checks whether an element
 *
 * @param <E> Elements of the list
 */
public class CombiningList<E extends Combine<E>> extends AbstractList<E> {

  private final List<E> backingList;

  public CombiningList(@NonNull List<E> backingList) {
    this.backingList = backingList;
  }

  @Override
  public E get(int index) {
    return backingList.get(index);
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
   * Otherwise add as normal
   *
   * @param element element to add/combine
   * @param matcher matcher to determine if existing elements should be combined with new one
   * @return number of elements that were modified (1 if added)
   */
  public int add(E element, Predicate<E> matcher) {
    if (element == null || matcher == null) {
      return 0;
    }
    int numChanged = stream()
        .filter(matcher)
        .map(e -> e.combine(element))
        .collect(Collectors.summingInt(changed -> changed ? 1 : 0));
    if (numChanged > 0) {
      return numChanged;
    }
    backingList.add(element);
    return 1;
  }

  /**
   * Adds a element to backing list without performing any combination. Advised to use {@code add(E,
   * Predicate<E>)} instead.
   *
   * @param element element to add to list
   * @return whether list was modified.
   */
  @Override
  public boolean add(E element) {
    return add(element, e -> false) > 0;
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
