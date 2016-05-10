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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.transformation.SortedList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import com.dzlier.combine.Combine;
import com.google.common.collect.Lists;
import com.sun.javafx.collections.ObservableListWrapper;

/**
 * Implementation of a list where elements are weighted for random access
 * 
 * @param <E>
 *            The element type to choose from
 */
public class WeightedList<E> extends AbstractList<E> {
    protected ObservableListWrapper<Node> backingList;
    protected Double total = 0.0;

    public WeightedList() {
        backingList = new ObservableListWrapper<>(new ArrayList<>());
    }

    /**
     * Adds the non-null element element with a weight of 1. {@code add(double, E)} is recommended over this,
     * to ensure specific weights for each element.
     *
     * @param element
     *            element to add to list
     * @return whether list was modified.
     */
    @Override
    public boolean add(@NonNull E element) {
        return add(1.0, element) > 0;
    }

    /**
     * Adds non-null element with a given weight
     *
     * @param weight
     *            weight for new element
     * @param element
     *            element to add to list
     * @return weight of added element
     */
    public Double add(Double weight, @NonNull E element) {
        if (weight <= 0 || element == null) {
            return 0.0;
        }

        total += weight;
        backingList.add(new Node(weight, element));
        return weight;
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public E get(int index) {
        return backingList.get(index).element;
    }

    /**
     * Gets the weight of a given item in the list
     * 
     * @param item
     *            item to get the weight for
     * @return the weight of item, if it appears in the list
     */
    public Double getWeight(E item) {
        return backingList.stream().filter(n -> n.itemEquals(item)).findFirst().map(Node::getWeight).orElse(-1.0);
    }

    /**
     * Gets the weight of the first item in the list that matches matcher
     *
     * @param matcher
     *            matcher to find the item to get the weight of
     * @return the weight of first item that matches matcher, if it appears in the list
     */
    public Double getWeight(Predicate<E> matcher) {
        return backingList.stream().filter(n -> matcher.test(n.element)).findFirst().map(Node::getWeight).orElse(-1.0);
    }

    /**
     * Returns a random entry from the backing list based on element weights
     * 
     * @return randomly weighted entry
     */
    public E random() {
        Double value = new Random().nextDouble() * total;
        E last = null;
        for (Node n : backingList) {
            value -= n.weight;
            if (value < 0) {
                return n.element;
            }
            last = n.element;
        }
        return last;
    }

    /**
     * finds the first element of backingList that matches matcher
     * 
     *
     * @param matcher
     *            matcher to find the item to get the weight of
     * @return first match in backingList to matcher
     */
    public Optional<E> findFirst(Predicate<E> matcher) {
        return backingList.sorted().stream().map(n -> n.element).filter(matcher).findFirst();
    }

    /**
     * Return the num heaviest entries
     *
     * @param num
     *            number of top entries to return
     * @return num first entries based on element weight
     */
    public List<E> top(int num) {
        if (num < 1) {
            return null;
        }
        SortedList<Node> sorted = backingList.sorted();
        if (num >= size()) {
            return Lists.newArrayList(sorted).stream().map(n -> n.element).collect(Collectors.toList());
        }
        List<E> result = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            result.add(sorted.get(i).element);
        }
        return result;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        backingList.forEach(n -> action.accept(n.element));
    }

    @Override
    public Iterator<E> iterator() {
        return backingList.stream().map(n -> n.element).iterator();
    }

    @Override
    public ListIterator<E> listIterator() {
        return backingList.stream().map(n -> n.element).collect(Collectors.toList()).listIterator();
    }

    @Override
    public Stream<E> parallelStream() {
        return backingList.stream().map(n -> n.element).collect(Collectors.toList()).parallelStream();
    }

    @Override
    public Stream<E> stream() {
        return backingList.stream().map(n -> n.element).collect(Collectors.toList()).stream();
    }

    @AllArgsConstructor
    protected class Node implements Comparable<Node>, Combine<Node> {
        @NonNull @Getter(AccessLevel.PRIVATE) Double weight;
        @NonNull final E element;

        boolean itemEquals(E that) {
            return element.equals(that);
        }

        boolean itemMatches(Predicate<E> matcher) {
            return matcher.test(element);
        }

        @Override
        public int compareTo(Node o) {
            return o.weight.compareTo(weight);
        }

        @Override
        public boolean combine(Node other) {
            if (other == null || other.weight <= 0) {
                return false;
            }
            Double thisWeight = this.weight;
            this.weight += other.weight;
            other.weight += thisWeight;
            return true;
        }
    }
}
