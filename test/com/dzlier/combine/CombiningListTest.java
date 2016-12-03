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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Unit tests for {@link CombiningList}
 */
public class CombiningListTest {

  private static Combinable A;
  private static Combinable B;
  private static Combinable C;
  private static Combinable D;
  private ArrayList<Combinable> backingList;
  private CombiningList<Combinable> list;

  @Before
  public void setup() {
    backingList = new ArrayList<>();
    list = new CombiningList<>(backingList, (c1, c2) -> c1.canCombineWith.contains(c2));
    A = new Combinable();
    B = new Combinable();
    C = new Combinable();
    D = new Combinable();
  }

  @Test
  public void testNoCombineAdd() throws Exception {
    list.add(A);
    list.add(B);

    assertListSize(2);
    assertCombinations(A, null, 0, false);
  }

  @Test
  public void testAddNoneMatch() {
    list.add(A);
    list.add(B);
    list.add(C);

    assertListSize(3);
    assertCombinations(A, null, 0, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, null, 0, false);
  }

  @Test
  public void testAddFirstMatches() {
    A.canCombineWith(C);
    assertTrue(list.add(A));
    assertTrue(list.add(B));
    assertTrue(list.add(C));
    assertTrue(list.add(D));

    assertListSize(3);
    assertCombinations(A, C, 1, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, null, 0, true);
    assertCombinations(D, null, 0, false);
  }

  @Test
  public void testAddMiddleMatches() {
    C.canCombineWith(A);
    B.canCombineWith(D);
    assertTrue(list.add(A));
    assertTrue(list.add(B));
    assertTrue(list.add(C));
    assertTrue(list.add(D));

    assertListSize(2);
    assertCombinations(A, C, 1, false);
    assertCombinations(B, D, 1, false);
    assertCombinations(C, null, 0, true);
    assertCombinations(D, null, 0, true);
  }

  @Test
  public void testAddLastMatches() {
    C.canCombineWith(D);
    assertTrue(list.add(A));
    assertTrue(list.add(B));
    assertTrue(list.add(C));
    assertTrue(list.add(D));

    assertListSize(3);
    assertCombinations(A, null, 0, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, D, 1, false);
    assertCombinations(D, null, 0, true);
  }

  @Test
  public void testAddTwoElementsModified() {
    A.canCombineWith(D);
    B.canCombineWith(D);
    assertTrue(list.add(A));
    assertTrue(list.add(B));
    assertTrue(list.add(C));
    assertTrue(list.add(D));

    assertListSize(3);
    assertCombinations(A, D, 1, false);
    assertCombinations(B, D, 1, false);
    assertCombinations(C, null, 0, false);
    assertCombinations(D, null, 0, true);
  }

  @Test
  public void testAddElementMatchedTwice() {
    A.canCombineWith(C);
    A.canCombineWith(D);
    assertTrue(list.add(A));
    assertTrue(list.add(B));
    assertTrue(list.add(C));
    assertTrue(list.add(D));

    assertListSize(2);
    assertCombinations(A, D, 2, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, null, 0, true);
    assertCombinations(D, null, 0, true);
  }

  @Test
  public void testAddOneMatchedOnceOneMatchedTwice() {
    A.canCombineWith(C);
    A.canCombineWith(D);
    B.canCombineWith(C);
    assertTrue(list.add(A));
    assertTrue(list.add(B));
    assertTrue(list.add(C));
    assertTrue(list.add(D));

    assertListSize(2);
    assertCombinations(A, D, 2, false);
    assertCombinations(B, C, 1, false);
    assertCombinations(C, null, 0, true);
    assertCombinations(D, null, 0, true);
  }

  @Test
  public void testAddMatchNonAddedElement() {
    A.canCombineWith(B);
    B.canCombineWith(C);
    assertTrue(list.add(A));
    assertTrue(list.add(B));
    assertTrue(list.add(C));

    assertListSize(2);
    assertCombinations(A, B, 1, false);
    assertCombinations(B, null, 0, true);
    assertCombinations(C, null, 0, false);
  }

  private void assertListSize(int expected) {
    assertEquals(String.format("List should have %d elements", expected), list.size(), expected);
    assertEquals(String.format("Backing list should have %d elements", expected),
        backingList.size(), expected);
  }

  private void assertCombinations(Combinable item, Object lastCombined, int numCombined,
      boolean hasBeenCombined) {
    assertEquals(item.lastCombined, lastCombined);
    assertEquals(item.combined, numCombined);
    assertEquals(item.hasBeenCombined, hasBeenCombined);
  }
}
