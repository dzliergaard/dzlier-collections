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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Unit tests for {@link WeightedList}
 */
public class WeightedListTest {

  private static final Double ONE = 1.0;
  private static final Double THREE = 3.0;
  private static final Double FIVE = 5.0;
  private static final String A = "A";
  private static final String B = "B";
  private static final String C = "C";
  private static final String D = "D";

  private WeightedList<String> list;

  @Before
  public void setup() {
    list = new WeightedList<>();
  }

  @Test
  public void testNoWeightAdd() {
    assertTrue(list.add(A));
    assertEquals(list.size(), 1);
  }

  @Test
  public void testWeightedAdd() {
    assertEquals(new Double(THREE), list.add(THREE, A));
    assertEquals(new Double(FIVE), list.add(FIVE, B));
    assertEquals(new Double(ONE), list.add(ONE, C));
  }

  @Test
  public void testFindFirst() {
    list.add(ONE, C);
    list.add(THREE, A);
    list.add(FIVE, B);

    assertEquals(B, list.findFirst(s -> s.matches("\\w")).get());
    assertEquals(A, list.findFirst(s -> s.contains(A)).get());
    assertEquals(C, list.findFirst(s -> s.contains(C)).get());
    assertFalse(list.findFirst(s -> s.contains(D)).isPresent());
  }

  @Test
  public void testTop() {
    list.add(THREE, A);
    list.add(FIVE, B);
    list.add(ONE, C);

    List<String> topOne = list.top(1);
    List<String> topTwo = list.top(2);
    List<String> topThree = list.top(3);
    List<String> topFour = list.top(4);

    assertEquals(1, topOne.size());
    assertEquals(B, topOne.get(0));

    assertEquals(2, topTwo.size());
    assertEquals(B, topTwo.get(0));
    assertEquals(A, topTwo.get(1));

    assertEquals(3, topThree.size());
    assertEquals(B, topThree.get(0));
    assertEquals(A, topThree.get(1));
    assertEquals(C, topThree.get(2));

    assertEquals(3, topFour.size());
    assertEquals(B, topFour.get(0));
    assertEquals(A, topFour.get(1));
    assertEquals(C, topFour.get(2));
  }

  @Test
  public void testGetWeight() {
    list.add(THREE, A);
    list.add(FIVE, B);
    list.add(ONE, C);

    assertEquals(THREE, list.getWeight(A));
    assertEquals(FIVE, list.getWeight(B));
    assertEquals(ONE, list.getWeight(C));
  }

  @Test
  public void testGetWeightMatcher() {
    list.add(THREE, A);
    list.add(FIVE, B);
    list.add(ONE, C);

    assertEquals(THREE, list.getWeight(s -> s.equals(A)));
    assertEquals(FIVE, list.getWeight(s -> s.equals(B)));
    assertEquals(ONE, list.getWeight(s -> s.equals(C)));
  }
}
