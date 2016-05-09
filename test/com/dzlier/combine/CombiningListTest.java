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

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CombiningList}
 */
public class CombiningListTest {
    private static final Integer ID_A = 1;
    private static final Integer ID_B = 2;
    private static final Integer ID_C = 3;
    private static final Integer ID_D = 4;
    private static final Integer NO_MATCH_ID = 100;
    private ArrayList<Combinable> backingList;
    private CombiningList<Combinable> list;
    private Combinable A;
    private Combinable B;
    private Combinable C;
    private Combinable D;

    @Before
    public void setup() {
        backingList = new ArrayList<>();
        list = new CombiningList<>(backingList);
        A = new Combinable(ID_A);
        B = new Combinable(ID_B);
        C = new Combinable(ID_C);
        D = new Combinable(ID_D);
    }

    @Test
    public void testNoCombineAdd() throws Exception {
        list.add(A);
        list.add(A);

        assertListSize(2);
        assertCombinations(A, null, 0, false);
    }

    @Test
    public void testAddNoneMatch() {
        list.add(A, comb -> comb.id == NO_MATCH_ID);
        list.add(B, comb -> comb.id == NO_MATCH_ID);
        list.add(C, comb -> comb.id == NO_MATCH_ID);

        assertListSize(3);
        assertCombinations(A, null, 0, false);
        assertCombinations(B, null, 0, false);
        assertCombinations(C, null, 0, false);
    }

    @Test
    public void testAddFirstMatches() {
        assertEquals(1, list.add(A, comb -> comb.id == ID_A));
        assertEquals(1, list.add(B, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(C, comb -> comb.id == ID_A));
        assertEquals(1, list.add(D, comb -> comb.id == ID_D));

        assertListSize(3);
        assertCombinations(A, C, 1, false);
        assertCombinations(B, null, 0, false);
        assertCombinations(C, null, 0, true);
        assertCombinations(D, null, 0, false);
    }

    @Test
    public void testAddMiddleMatches() {
        assertEquals(1, list.add(A, comb -> comb.id == ID_C));
        assertEquals(1, list.add(B, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(C, comb -> comb.id == ID_C));
        assertEquals(1, list.add(D, comb -> comb.id == ID_B));

        assertListSize(3);
        assertCombinations(A, null, 0, false);
        assertCombinations(B, D, 1, false);
        assertCombinations(C, null, 0, false);
        assertCombinations(D, null, 0, true);
    }

    @Test
    public void testAddLastMatches() {
        assertEquals(1, list.add(A, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(B, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(C, comb -> comb.id == ID_D));
        assertEquals(1, list.add(D, comb -> comb.id == ID_C));

        assertListSize(3);
        assertCombinations(A, null, 0, false);
        assertCombinations(B, null, 0, false);
        assertCombinations(C, D, 1, false);
        assertCombinations(D, null, 0, true);
    }

    @Test
    public void testAddTwoElementsModified() {
        assertEquals(1, list.add(A, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(B, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(C, comb -> comb.id == ID_D));
        assertEquals(2, list.add(D, comb -> comb.id <= ID_B));

        assertListSize(3);
        assertCombinations(A, D, 1, false);
        assertCombinations(B, D, 1, false);
        assertCombinations(C, null, 0, false);
        assertCombinations(D, null, 0, true);
    }

    @Test
    public void testAddElementMatchedTwice() {
        assertEquals(1, list.add(A, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(B, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(C, comb -> comb.id == ID_A));
        assertEquals(1, list.add(D, comb -> comb.id == ID_A));

        assertListSize(2);
        assertCombinations(A, D, 2, false);
        assertCombinations(B, null, 0, false);
        assertCombinations(C, null, 0, true);
        assertCombinations(D, null, 0, true);
    }

    @Test
    public void testAddOneMatchedOnceOneMatchedTwice() {
        assertEquals(1, list.add(A, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(B, comb -> comb.id == NO_MATCH_ID));
        assertEquals(2, list.add(C, comb -> comb.id <= ID_B));
        assertEquals(1, list.add(D, comb -> comb.id == ID_A));

        assertListSize(2);
        assertCombinations(A, D, 2, false);
        assertCombinations(B, C, 1, false);
        assertCombinations(C, null, 0, true);
        assertCombinations(D, null, 0, true);
    }

    @Test
    public void testAddMatchNonAddedElement() {
        assertEquals(1, list.add(A, comb -> comb.id == NO_MATCH_ID));
        assertEquals(1, list.add(B, comb -> comb.id == ID_A));
        assertEquals(1, list.add(C, comb -> comb.id == ID_B));

        assertListSize(2);
        assertCombinations(A, B, 1, false);
        assertCombinations(B, null, 0, true);
        assertCombinations(C, null, 0, false);
    }

    private void assertListSize(int expected) {
        assertEquals(String.format("List should have %d elements", expected), list.size(), expected);
        assertEquals(String.format("Backing list should have %d elements", expected), backingList.size(), expected);
    }

    private void assertCombinations(Combinable item, Object lastCombined, int numCombined, boolean hasBeenCombined) {
        assertEquals(item.lastCombined, lastCombined);
        assertEquals(item.combined, numCombined);
        assertEquals(item.hasBeenCombined, hasBeenCombined);
    }
}
