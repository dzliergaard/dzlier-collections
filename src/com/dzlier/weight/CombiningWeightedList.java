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

import java.util.function.Predicate;

import lombok.NonNull;

import com.dzlier.combine.CombiningList;

public class CombiningWeightedList<E> extends WeightedList<E> {
    private CombiningList<Node> combiningList;

    public CombiningWeightedList() {
        super();
        combiningList = new CombiningList<>(backingList);
    }

    /**
     * Adds the non-null element {@param element} with the weight of {@param weight}
     * 
     * @return adjusted weight of the element that was added/modified
     */
    @Override
    public Double add(Double weight, @NonNull E element) {
        if (weight < 0 || element == null) {
            return -1.0;
        }

        total += weight;
        Node toAdd = new Node(weight, element);
        combiningList.add(toAdd, n -> n.itemEquals(element));
        return toAdd.weight;
    }

    /**
     * Adds the non-null element {@param element} with the weight of {@param weight}. If an element of the list
     * matches {@param matcher}, then {@param element} is combined with it instead.
     *
     * @return adjusted weight of the element that was added/modified
     */
    public Double add(Double weight, @NonNull E element, @NonNull Predicate<E> matcher) {
        if (weight < 0 || element == null) {
            return -1.0;
        }
        total += weight;

        Node toAdd = new Node(weight, element);
        combiningList.add(toAdd, n -> n.itemMatches(matcher));
        return toAdd.weight;
    }
}
