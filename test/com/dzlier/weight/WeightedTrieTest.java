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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link WeightedTrie}
 */
public class WeightedTrieTest {
    private static final Double ONE = 1.0;
    private static final Double TWO = 2.0;
    private static final Double THREE = 3.0;
    private static final Double FIVE = 5.0;
    private static final Double SIX = 6.0;
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    public static final String E = "E";

    private WeightedTrie<String> trie;

    @Before
    public void setup() {
        trie = new WeightedTrie<>();
    }

    @Test
    public void testAddChain() {
        assertEquals(ONE, trie.addChain(ONE, new String[] { A, B, C }));
        assertEquals(TWO, trie.addChain(TWO, new String[] { A, D, C }));
        assertEquals(FIVE, trie.addChain(THREE, new String[] { A, D }));
        assertEquals(A, trie.root.children.get(0).item);
        assertEquals(SIX, trie.root.children.getWeight(node -> node.matchesItem(A)));
        assertEquals(B, trie.root.children.get(0).children.get(0).item);
        assertEquals(ONE, trie.root.children.get(0).children.getWeight(node -> node.matchesItem(B)));
        assertEquals(C, trie.root.children.get(0).children.get(0).children.get(0).item);
        assertEquals(ONE, trie.root.children.get(0).children.get(0).children.getWeight(node -> node.matchesItem(C)));

        assertEquals(D, trie.root.children.get(0).children.get(1).item);
        assertEquals(FIVE, trie.root.children.get(0).children.getWeight(node -> node.matchesItem(D)));
        assertEquals(C, trie.root.children.get(0).children.get(1).children.get(0).item);
        assertEquals(TWO, trie.root.children.get(0).children.get(1).children.getWeight(node -> node.matchesItem(C)));
    }

    @Test
    public void testTop() {
        assertEquals(ONE, trie.addChain(ONE, new String[] { A, B, C, D }));
        assertEquals(TWO, trie.addChain(TWO, new String[] { A, D, C }));
        assertEquals(FIVE, trie.addChain(THREE, new String[] { A, D }));
        List<String> levelTwo = trie.top(3, A);
        assertEquals(2, levelTwo.size());
        assertEquals(D, levelTwo.get(0));
        assertEquals(B, levelTwo.get(1));
    }

    @Test
    public void testRandom() {
        assertEquals(ONE, trie.addChain(ONE, new String[] { A, B, C, D }));
        assertEquals(TWO, trie.addChain(TWO, new String[] { A, D, C }));
        assertEquals(FIVE, trie.addChain(THREE, new String[] { A, D }));
        assertTrue(trie.random(E, A).matches("B|D"));
        assertEquals(C, trie.random(E, A, B));
        assertEquals(D, trie.random(E, A, B, C));
        assertEquals(E, trie.random(E, A, B, D));
        assertEquals(E, trie.random(E, D, A, B));
    }
}
