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

import java.util.Optional;
import java.util.TreeMap;

/**
 * Implementation of a Navigable {@link TreeMap} that combines values associated with keys instead
 * of replacing them
 *
 * @param <K> Key element
 * @param <V> Value element
 */
public class CombiningMap<K, V extends Combine<V>> extends TreeMap<K, V> {

  /**
   * If tree already contains a value for key k, it combines the existing value with value v
   *
   * By definition, this method does not return the previous value for key k since it does not
   * remove values, so it returns the current element for key k instead.
   *
   * @param k key
   * @param v value, implements {@link Combine} so it can be combined if necessary
   * @return current element for item k
   */
  @Override
  public V put(K k, V v) {
    return Optional.ofNullable(this.get(k)).map(val -> {
      val.combine(v);
      return val;
    }).orElseGet(() -> {
      super.put(k, v);
      return v;
    });
  }
}
