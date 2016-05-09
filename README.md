# dzlier-collections
Collections utilities including combining lists and weighted tries.

A few convenience collection types that I was using in my RPTools project, but decided to surface separately.

## Combining Collections

These collection types can combine elements as they are added instead of replacing or duplicating them. 
The elements that are being combined must implement the interface Combine, so that the collections know
what to do when it finds it must combine elements.

### CombiningList

CombiningList surfaces a new add method add(E element, Predicate<E> matcher) that allows you to 
provide a predicate to match against elements already in the list. You must supply a backing list
to the constructor that it will forward method calls to accordingly. The method returns the
number of elements of the list that were modified, or 1 if the element was added as normal (no
elements matched the Predicate).

### CombiningMap

CombiningMaps must use a value type that implements Combine. When calling CombiningMap.put(k, v), if the
key k already has a value associated with it, the existing value is combined with the new value instead of
replacing it.

## Weighted Collections

Weighted collections allow you to specify weights along with the elements. The weights are then used to
retrieve the top X elements in the collection. The collections also support getting a pseudo-random element,
where the randomness is determined by the elements' relative weights.

### WeightedList

In addition to the above, WeightedList also provides the method findFirst(Predicate matcher), which is a 
quicker way of writing list.stream().filter(matcher).findFirst().

### WeightedTrie

WeightedTrie is a Trie implementation which allows you to quickly add chains of elements down the trie, 
combining equal elements and their weights as it does so. For example:

<pre>
trie.addChain(1.0, new String[] { "A", "B", "C" });
trie.addChain(2.0, new String[] { "A", "D", "C" });
trie.addChain(3.0, new String[] { "A", "D" });
</pre>

In this example, the top level of the Trie will contain a single element, "A", which has a weight of 6 (1 + 2 + 3).
"A" will have two children, "B" with a weight of 1, and "D" with a weight of 5 (2 + 3). "B"s single child "C" will 
also have a weight of 1, while "D"s single child "C" will have a weight of 2.
