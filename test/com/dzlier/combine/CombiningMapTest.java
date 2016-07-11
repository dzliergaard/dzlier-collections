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

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CombiningMap}
 */
public class CombiningMapTest {

  private static final Integer ID_A = 1;
  private static final Integer ID_B = 2;
  private static final Integer ID_C = 3;
  private static final Integer ID_D = 4;
  private static final Integer NO_MATCH_ID = 100;
  private CombiningMap<Integer, Combinable> map;
  private Combinable A;
  private Combinable B;
  private Combinable C;
  private Combinable D;

  @Before
  public void setup() {
    map = new CombiningMap<>();
    A = new Combinable(ID_A);
    B = new Combinable(ID_B);
    C = new Combinable(ID_C);
    D = new Combinable(ID_D);
  }

  @Test
  public void testPutNoneMatch() {
    map.put(ID_A, A);
    map.put(ID_B, B);
    map.put(ID_C, C);

    assertEquals("Map should have 3 elements", map.size(), 3);
    assertCombinations(A, null, 0, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, null, 0, false);
  }

  @Test
  public void testPutFirstMatches() {
    assertEquals(A, map.put(ID_A, A));
    assertEquals(B, map.put(ID_B, B));
    assertEquals(A, map.put(ID_A, C));
    assertEquals(D, map.put(ID_D, D));

    assertEquals("Map should have 3 elements", map.size(), 3);
    assertCombinations(A, C, 1, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, null, 0, true);
    assertCombinations(D, null, 0, false);
  }

  @Test
  public void testPutMiddleMatches() {
    assertEquals(A, map.put(ID_A, A));
    assertEquals(B, map.put(ID_B, B));
    assertEquals(C, map.put(ID_C, C));
    assertEquals(B, map.put(ID_B, D));

    assertEquals("Map should have 3 elements", map.size(), 3);
    assertCombinations(A, null, 0, false);
    assertCombinations(B, D, 1, false);
    assertCombinations(C, null, 0, false);
    assertCombinations(D, null, 0, true);
  }

  @Test
  public void testPutLastMatches() {
    assertEquals(A, map.put(ID_A, A));
    assertEquals(B, map.put(ID_B, B));
    assertEquals(C, map.put(ID_C, C));
    assertEquals(C, map.put(ID_C, D));

    assertEquals("Map should have 3 elements", map.size(), 3);
    assertCombinations(A, null, 0, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, D, 1, false);
    assertCombinations(D, null, 0, true);
  }

  @Test
  public void testPutElementMatchedTwice() {
    assertEquals(A, map.put(ID_A, A));
    assertEquals(B, map.put(ID_B, B));
    assertEquals(A, map.put(ID_A, C));
    assertEquals(A, map.put(ID_A, D));

    assertEquals("Map should have 2 elements", map.size(), 2);
    assertCombinations(A, D, 2, false);
    assertCombinations(B, null, 0, false);
    assertCombinations(C, null, 0, true);
    assertCombinations(D, null, 0, true);
  }

  private void assertCombinations(Combinable item, Object lastCombined, int numCombined,
      boolean hasBeenCombined) {
    assertEquals(item.lastCombined, lastCombined);
    assertEquals(item.combined, numCombined);
    assertEquals(item.hasBeenCombined, hasBeenCombined);
    assertEquals(!hasBeenCombined, map.containsValue(item));
  }
}
