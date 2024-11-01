/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.shared.util;

import static org.corant.shared.util.Assertions.shouldBeTrue;
import static org.corant.shared.util.Conversions.toObject;
import static org.corant.shared.util.Empties.isNotEmpty;
import static org.corant.shared.util.Empties.sizeOf;
import static org.corant.shared.util.Iterables.collectionOf;
import static org.corant.shared.util.Objects.forceCast;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.corant.shared.ubiquity.Immutable.ImmutableListBuilder;
import org.corant.shared.ubiquity.Mutable.MutableInteger;

/**
 * corant-shared
 *
 * @author bingo 上午12:31:10
 */
public class Lists {

  private Lists() {}

  /**
   * Returns either the passed in list, or if the list is {@code null} or {@link List#isEmpty()} the
   * value of {@code defaultList}.
   *
   * @param <E> the element type
   * @param list the list, possibly {@code null} or {@link List#isEmpty()}
   * @param defaultList the returned values if list is {@code null} or {@link List#isEmpty()}
   */
  public static <E> List<E> defaultEmpty(Collection<E> list, List<E> defaultList) {
    if (list == null || list.isEmpty()) {
      return defaultList;
    }
    if (list instanceof List<E> s) {
      return s;
    } else {
      return new ArrayList<>(list);
    }
  }

  /**
   * Returns either the passed in list, or if the list is {@code null} or {@link List#isEmpty()} the
   * result of {@code supplier}.
   *
   * @param <E> the element type
   * @param list the list, possibly {@code null} or {@link List#isEmpty()}
   * @param supplier the returned values supplier if list is {@code null} or {@link List#isEmpty()}
   */
  public static <E> List<E> defaultEmpty(Collection<E> list, Supplier<List<E>> supplier) {
    if (list == null || list.isEmpty()) {
      return supplier.get();
    }
    if (list instanceof List<E> s) {
      return s;
    } else {
      return new ArrayList<>(list);
    }
  }

  /**
   * Convert and returns the element at the specified position in the list. If the passing index is
   * negative means that search element from last to first position.
   *
   * @param <T> the element type
   * @param list the list to get a value from
   * @param index the index to get
   * @param clazz the return class
   *
   * @see #get(List, int)
   */
  public static <T> T get(List<?> list, int index, Class<T> clazz) {
    return toObject(get(list, index), clazz);
  }

  /**
   * Returns the element at the specified position in the list. If the passing index is negative
   * means that search element from last to first position.
   *
   * <pre>
   * example:
   * get(list,-1) equals list.get(list.size()-1)
   * get(list,-2) equals list.get(list.size()-2)
   * </pre>
   *
   * @param <E> the element type
   * @param list the list to get a value from
   * @param index the index to get
   */
  public static <E> E get(List<? extends E> list, int index) {
    return index < 0 ? list.get(sizeOf(list) + index) : list.get(index);
  }

  /**
   * Null safe immutable list converter
   *
   * @param <E> the element type
   * @param collection the collection to get a value from
   * @return an immutable list that combined by the passed in collection
   */
  public static <E> List<E> immutableList(Collection<? extends E> collection) {
    if (collection == null) {
      return Collections.emptyList();
    } else if (collection instanceof List) {
      return Collections.unmodifiableList((List<? extends E>) collection);
    } else {
      return Collections.unmodifiableList(newArrayList(collection));
    }
  }

  @SafeVarargs
  public static <E> ImmutableListBuilder<E> immutableListBuilder(final E... objects) {
    return new ImmutableListBuilder<>(objects);
  }

  public static <E> ImmutableListBuilder<E> immutableListBuilder(Iterable<? extends E> iterable) {
    return new ImmutableListBuilder<>(iterable);
  }

  /**
   * Convert an array to a non-null immutable list
   *
   * @param <E> the element type
   * @param objects the objects to construct an immutable list
   * @return an immutable list that combined by the passed in array
   */
  @SafeVarargs
  public static <E> List<E> immutableListOf(final E... objects) {
    if (objects == null || objects.length == 0) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(listOf(objects));
  }

  /**
   * Convert an array to a non-null linked list
   *
   * @param <E> the element type
   * @param objects the objects to construct a linked list
   * @return a linked list that combined by the passed in array
   */
  @SafeVarargs
  public static <E> LinkedList<E> linkedListOf(final E... objects) {
    LinkedList<E> list = new LinkedList<>();
    if (objects != null) {
      Collections.addAll(list, objects);
    }
    return list;
  }

  /**
   * Convert an array to non-null list
   *
   * @param <E> the element type
   * @param objects the objects to construct a list
   * @return a list that combined by the passed in array
   */
  @SafeVarargs
  public static <E> List<E> listOf(final E... objects) {
    return collectionOf(ArrayList::new, objects);
  }

  /**
   * Convert an enumeration to a non-null list
   *
   * @param <E> the element type
   * @param enumeration the elements that the list should contain
   * @return a list that combined by the passed in enumeration
   */
  public static <E> List<E> listOf(final Enumeration<? extends E> enumeration) {
    List<E> list = new ArrayList<>();
    if (enumeration != null) {
      while (enumeration.hasMoreElements()) {
        list.add(enumeration.nextElement());
      }
    }
    return list;
  }

  /**
   * Convert an iterable to a non-null list.
   * <p>
   * Note: If the given iterable object itself is a list, return the object itself directly.
   *
   * @param <E> the element type
   * @param iterable the elements that the list should contain
   * @return a list that combined by the passed in iterable
   */
  public static <E> List<E> listOf(final Iterable<? extends E> iterable) {
    if (iterable instanceof List) {
      return forceCast(iterable);
    } else if (iterable != null) {
      return listOf(iterable.iterator());
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Convert an iterator to a non-null list
   *
   * @param <E> the element type
   * @param iterator the elements that the list should contain
   * @return a list that combined by the passed in iterator
   */
  public static <E> List<E> listOf(final Iterator<? extends E> iterator) {
    List<E> list = new ArrayList<>();
    if (iterator != null) {
      while (iterator.hasNext()) {
        list.add(iterator.next());
      }
    }
    return list;
  }

  /**
   * Constructs a new array list containing the elements in the specified collection, null safe.
   *
   * @see ArrayList#ArrayList(Collection)
   *
   * @param <E> the element type
   * @param initials the collection whose elements are to be placed into the array list
   */
  public static <E> List<E> newArrayList(final Collection<E> initials) {
    return initials == null ? new ArrayList<>() : new ArrayList<>(initials);
  }

  /**
   * Null safe removeIf, execution begins only if the parameters passed in are not null.
   *
   * @param <C> the collection type
   * @param <E> the element type
   * @param collection the collection that elements will be removed
   * @param p the predicate which returns true for elements to be removed
   */
  public static <C extends Collection<E>, E> C removeIf(final C collection,
      Predicate<? super E> p) {
    if (collection != null && p != null) {
      collection.removeIf(p);
    }
    return collection;
  }

  /**
   * Split a collection into sub-lists with size.
   *
   * @param <E> the element type
   * @param size the sub-list size
   * @param collection the collection to split
   */
  public static <E> List<List<E>> split(int size, Collection<E> collection) {
    shouldBeTrue(size > 0);
    if (collection == null || collection.isEmpty()) {
      return new ArrayList<>();
    }
    final MutableInteger counter = new MutableInteger(0);
    return new ArrayList<>(collection.stream()
        .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size)).values());
  }

  /**
   * Expand the {@link List#subList(int, int)}, add convenient reverse index support. When the given
   * index>=0, the processing process is the same as {@link List#subList(int, int)}, when the
   * index<0, the reverse index is used.
   *
   * @param <E> the element type
   * @param list the list to gain the sub list
   * @param fromIndex low-end point (inclusive) of the subList
   * @param toIndex high-end point (exclusive) of the subList
   *
   * @see List#subList(int, int)
   */
  public static <E> List<E> subList(List<E> list, int fromIndex, int toIndex) {
    if (list == null) {
      return null;
    }
    int size = list.size();
    int beginIndex = fromIndex < 0 ? size + fromIndex : fromIndex;
    int endIndex = toIndex < 0 ? size + toIndex : toIndex;
    return list.subList(beginIndex, endIndex);
  }

  /**
   * Swaps the elements at the specified positions in the specified list.(If the specified positions
   * are equal, invoking this method leaves the list unchanged.)
   *
   * @param <E> the element type
   * @param l the list in which the elements at two positions will be swapped
   * @param i the position index
   * @param j the other position index
   */
  public static <E> void swap(List<? extends E> l, int i, int j) {
    Collections.swap(l, i, j);
  }

  /**
   * Use the specified conversion function to convert the given list element type
   *
   * @param <S> the source element type
   * @param <T> the target element type
   * @param list the source element list
   * @param converter the conversion function
   */
  public static <S, T> List<T> transform(final List<S> list,
      final Function<? super S, ? extends T> converter) {
    return list == null ? null : list.stream().map(converter).collect(Collectors.toList());
  }

  /**
   * Returns a new list containing the given collections. The List.addAll(Collection) operation is
   * used to append the given collections into a new list.
   *
   * @param <E> the element type
   * @param collections the collections to be union
   */
  @SafeVarargs
  public static <E> List<E> union(Collection<? extends E>... collections) {
    List<E> union = new ArrayList<>();
    if (isNotEmpty(collections)) {
      for (Collection<? extends E> collection : collections) {
        if (collection != null) {
          union.addAll(collection);
        }
      }
    }
    return union;
  }
}
