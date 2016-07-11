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

/**
 * Test class that implements {@link Combine} interface
 */
class Combinable implements Combine<Combinable> {

  final int id;
  // number of other elements that have been combined into this one
  int combined = 0;
  // last element to be combined into this one
  Combinable lastCombined;
  // if this element has been combined into another element
  boolean hasBeenCombined = false;

  Combinable(int id) {
    this.id = id;
  }

  @Override
  public boolean combine(Combinable other) {
    combined += 1;
    lastCombined = other;
    other.hasBeenCombined = true;
    return true;
  }

  @Override
  public int compareTo(Combinable o) {
    return 0;
  }
}
