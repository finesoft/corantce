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

import static org.corant.shared.util.Classes.tryAsClass;
import static org.corant.shared.util.Empties.isNotEmpty;
import static org.corant.shared.util.Streams.streamOf;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.corant.shared.exception.CorantRuntimeException;

/**
 * corant-shared
 *
 * @author bingo 下午11:26:00
 */
public class Objects {

  public static final Object[] EMPTY_ARRAY = {};

  protected Objects() {}

  /**
   * <p>
   * Appends all the elements of the given arrays into a new array.
   * <p>
   * The new array contains all of the element of {@code src} followed by all of the elements
   * {@code ts}. When an array is returned, it is always a new array.
   *
   * @param <T> the element type
   * @param array the first array whose elements are added to the new array
   * @param elements the second array whose elements are added to the new array
   * @return append
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] append(T[] array, T... elements) {
    if (array == null && elements == null) {
      return null;
    } else if (elements != null && (array == null || array.length == 0)) {
      return Arrays.copyOf(elements, elements.length);
    } else if (array != null && (elements == null || elements.length == 0)) {
      return Arrays.copyOf(array, array.length);
    }
    final Class<?> act = array.getClass().getComponentType();
    final T[] appendArray = (T[]) Array.newInstance(act, array.length + elements.length);
    System.arraycopy(array, 0, appendArray, 0, array.length);
    try {
      System.arraycopy(elements, 0, appendArray, array.length, elements.length);
    } catch (ArrayStoreException e) {
      Class<?> tt = elements.getClass().getComponentType();
      if (!act.isAssignableFrom(tt)) {
        throw new IllegalArgumentException(
            "Cannot append " + tt.getName() + " in an array of " + act.getName(), e);
      }
      throw e;
    }
    return appendArray;
  }

  /**
   * <p>
   * Appends all the elements of the given arrays which the given source array not contains into a
   * new array.
   * <p>
   * The new array contains all of the element of {@code src} followed by all of the {@code ts}
   * elements that not contains in the {@code src}. When an array is returned, it is always a new
   * array.
   *
   * @param <E> the element type
   * @param array the first array whose elements are added to the new array
   * @param elements the second array whose elements are added to the new array
   * @return append
   */
  @SuppressWarnings("unchecked")
  public static <E> E[] appendIfAbsent(E[] array, E... elements) {
    if (array == null && elements == null) {
      return null;
    } else if (elements != null && (array == null || array.length == 0)) {
      return Arrays.copyOf(elements, elements.length);
    } else if (array != null && (elements == null || elements.length == 0)) {
      return Arrays.copyOf(array, array.length);
    }
    final Class<?> act = array.getClass().getComponentType();
    final E[] appendArray = (E[]) Array.newInstance(act, array.length + elements.length);
    System.arraycopy(array, 0, appendArray, 0, array.length);
    int length = array.length;
    try {
      for (E e : elements) {
        boolean contains = false;
        for (E s : appendArray) {
          if (areEqual(e, s)) {
            contains = true;
            break;
          }
        }
        if (!contains) {
          appendArray[length] = e;
          length++;
        }
      }
    } catch (ArrayStoreException e) {
      Class<?> tt = elements.getClass().getComponentType();
      if (!act.isAssignableFrom(tt)) {
        throw new IllegalArgumentException(
            "Cannot append " + tt.getName() + " in an array of " + act.getName(), e);
      }
      throw e;
    }
    return Arrays.copyOf(appendArray, length);
  }

  /**
   * @see java.util.Objects#deepEquals(Object, Object)
   */
  public static boolean areDeepEqual(Object a, Object b) {
    return java.util.Objects.deepEquals(a, b);
  }

  /**
   * @see java.util.Objects#equals(Object, Object)
   */
  public static boolean areEqual(Object a, Object b) {
    return java.util.Objects.equals(a, b);
  }

  /**
   * Returns true if the arguments are equal to each other or the result of the comparison is 0 and
   * false otherwise. Consequently, if both arguments are null, true is returned and if exactly one
   * argument is null, false is returned. Otherwise, equality is determined by using the equals or
   * compareTo method of the first argument. Therefore, this method can be used to determine the
   * equivalence of numeric values.
   *
   * @param <T> the object type
   * @param a a comparable object
   * @param b a comparable object to be compared with a for equality
   */
  public static <T extends Number & Comparable<T>> boolean areEqual(T a, T b) {
    return java.util.Objects.equals(a, b) || a != null && b != null && a.compareTo(b) == 0;
  }

  /**
   * @see java.util.Objects#toString(Object)
   */
  public static String asString(Object o) {
    return java.util.Objects.toString(o);
  }

  /**
   * @see java.util.Objects#toString(Object, String)
   */
  public static String asString(Object o, String nullDefault) {
    return java.util.Objects.toString(o, nullDefault);
  }

  public static String[] asStrings(Iterable<?> it) {
    return asStrings(null, streamOf(it).map(java.util.Objects::toString).toArray(Object[]::new));
  }

  public static String[] asStrings(Object... objs) {
    return asStrings(null, objs);
  }

  public static String[] asStrings(UnaryOperator<String> uo, Object... objs) {
    if (objs == null) {
      return Strings.EMPTY_ARRAY;
    }
    if (uo == null) {
      return Arrays.stream(objs).map(o -> asString(o, Strings.NULL)).toArray(String[]::new);
    } else {
      return Arrays.stream(objs).map(o -> uo.apply(asString(o, Strings.NULL)))
          .toArray(String[]::new);
    }
  }

  /**
   * Null safe comparison of Comparables. null is assumed to be less than a non-null value.
   * <p>
   * Note: Code come from apache.
   *
   * @param <T> the comparable object type
   * @param c1 a comparable object
   * @param c2 a comparable object to be compared with a
   */
  public static <T extends Comparable<? super T>> int compare(final T c1, final T c2) {
    if (c1 == c2) {
      return 0;
    }
    if (c1 == null) {
      return -1;
    }
    if (c2 == null) {
      return 1;
    }
    return c1.compareTo(c2);
  }

  /**
   * @see java.util.Objects#compare(Object, Object, Comparator)
   */
  public static <T> int compare(T a, T b, Comparator<? super T> c) {
    return java.util.Objects.compare(a, b, c);
  }

  /**
   * Returns true if the given array is not null and contains the given element otherwise false.
   *
   * @param <T> the element type
   * @param array the array to check
   * @param element the element to check
   */
  public static <T> boolean contains(T[] array, T element) {
    return indexOf(array, element) != -1;
  }

  /**
   * Return true if at least one of the given parameter is not null and false otherwise.
   */
  public static boolean containsNotNull(Object... objs) {
    if (isNotEmpty(objs)) {
      for (Object obj : objs) {
        if (isNotNull(obj)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * If the first given parameter is not null, returns the value, otherwise returns the result of
   * the given supplier.
   *
   * @param <T> the object type
   * @param obj the object to return if not null
   * @param supplier the supplier for supporting the return value if the first given parameter is
   *        null
   */
  public static <T> T defaultObject(T obj, Supplier<? extends T> supplier) {
    return obj != null ? obj : supplier.get();
  }

  /**
   * If the first given parameter is not null, returns the value, otherwise returns the second given
   * parameter.
   *
   * @param <T> the object type
   * @param obj the object to return if not null
   * @param altObj the alternative object to return if the first given parameter is null
   */
  public static <T> T defaultObject(T obj, T altObj) {
    return obj != null ? obj : altObj;
  }

  /**
   * Remove duplicate elements of the array and return a new array of unique elements sorted in the
   * original order. Return null if the given {@code src} array is null, return an empty new array
   * if the given {@code src} array is empty.
   *
   * @param <T> the element type
   * @param array the original array
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] distinct(T[] array) {
    if (array == null) {
      return null;
    } else {
      final int len = array.length;
      final T[] distinct = (T[]) Array.newInstance(array.getClass().getComponentType(), len);
      if (len == 0) {
        return distinct;
      } else {
        int index = 0;
        for (int i = 0; i < len; i++) {
          boolean contains = false;
          for (int j = 0; j < i; j++) {
            if (areEqual(array[i], array[j])) {
              contains = true;
              break;
            }
          }
          if (!contains) {
            distinct[index] = array[i];
            index++;
          }
        }
        return Arrays.copyOf(distinct, index);
      }
    }
  }

  /**
   * Simple cast object to expect type.
   *
   * @param <T> the object type
   * @param o the object to be cast
   * @return forceCast
   */
  @SuppressWarnings("unchecked")
  public static <T> T forceCast(Object o) {
    return o != null ? (T) o : null;
  }

  /**
   * @see java.util.Objects#hash(Object...)
   */
  public static int hash(Object... values) {
    return java.util.Objects.hash(values);
  }

  /**
   * @see java.util.Objects#hashCode(Object)
   */
  public static int hashCode(Object o) {
    return java.util.Objects.hashCode(o);
  }

  public static <T> int indexOf(T[] array, T element) {
    return indexOf(array, element, 0);
  }

  /**
   * Searches the specified array for the specified value, returns index of the search key, if it is
   * contained in the array; otherwise, return -1.
   *
   * @param <T> the array component type
   * @param array the array to be searched
   * @param element the value to be searched for
   * @param fromIndex the search from index
   * @return index of the search key, if it is contained in the array; otherwise, return -1.
   */
  public static <T> int indexOf(T[] array, T element, final int fromIndex) {
    if (array != null) {
      int len = array.length;
      if (fromIndex < 0 || fromIndex > (len - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + len);
      }
      for (int i = fromIndex; i < len; i++) {
        if (areEqual(array[i], element)) {
          return i;
        }
      }
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] insertAt(final int index, final T[] array, final T... elements) {
    if (array == null) {
      return null;
    }
    if (elements == null) {
      return Arrays.copyOf(array, array.length);
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final Class<?> act = array.getClass().getComponentType();
    final T[] ret = (T[]) Array.newInstance(act, array.length + elements.length);
    System.arraycopy(elements, 0, ret, index, elements.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + elements.length, array.length - index);
    }
    return ret;
  }

  /**
   * Return true if all given parameter are not null and false otherwise.
   */
  public static boolean isNoneNull(Object... objs) {
    if (isNotEmpty(objs)) {
      for (Object obj : objs) {
        if (isNull(obj)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Return true if given parameter is not null and false otherwise.
   */
  public static boolean isNotNull(Object obj) {
    return java.util.Objects.nonNull(obj);
  }

  /**
   * Return false if given parameter is not null and true otherwise.
   */
  public static boolean isNull(Object obj) {
    return java.util.Objects.isNull(obj);
  }

  /**
   * Return the max one, if the two parameters are the same, then return the first. If any of the
   * comparable are null, return the greatest of the non-null objects.
   *
   * @param <T> the comparable type
   * @param comparables the given comparable object to find out the max one
   */
  @SuppressWarnings("unchecked")
  public static <T extends Comparable<? super T>> T max(final T... comparables) {
    T result = null;
    if (comparables != null) {
      for (final T value : comparables) {
        int c;
        if (value == result) {
          c = 0;
        } else if (value == null) {
          c = -1;
        } else if (result == null) {
          c = 1;
        } else {
          c = value.compareTo(result);
        }
        if (c > 0) {
          result = value;
        }
      }
    }
    return result;
  }

  /**
   * Return the min one, if the two parameters are the same, then return the first. If any of the
   * comparables are null, return the least of the non-null objects.
   *
   * @param <T> the comparable type
   * @param comparables the given comparable object to find out the min one
   */
  @SuppressWarnings("unchecked")
  public static <T extends Comparable<? super T>> T min(final T... comparables) {
    T result = null;
    if (comparables != null) {
      for (final T value : comparables) {
        int c;
        if (value == result) {
          c = 0;
        } else if (value == null) {
          c = 1;
        } else if (result == null) {
          c = -1;
        } else {
          c = value.compareTo(result);
        }
        if (c < 0) {
          result = value;
        }
      }
    }
    return result;
  }

  /**
   * Returns a new instance of the given class, use {@link Class#getDeclaredConstructor(Class...)}.
   *
   * @param <T> the instance type
   * @param cls the instance class
   */
  public static <T> T newInstance(Class<T> cls) {
    try {
      // return cls != null ? cls.newInstance() : null;// JDK8
      return cls != null ? cls.getDeclaredConstructor().newInstance() : null;
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new CorantRuntimeException(e);
    }
  }

  public static <T> Optional<T> optionalCast(Object o, Class<T> cls) {
    return Optional.ofNullable(tryCast(o, cls));
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] removeAt(final T[] array, final int... indices) {
    if (array == null) {
      return null;
    } else if (indices == null || indices.length == 0) {
      return Arrays.copyOf(array, array.length);
    } else {
      int length = array.length;
      final Class<?> act = array.getClass().getComponentType();
      final T[] ret = (T[]) Array.newInstance(act, array.length);
      int newLength = 0;
      for (int i = 0; i < length; i++) {
        boolean remove = false;
        for (int index : indices) {
          if (i == index) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = array[i];
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  /**
   * Null safe removeIf, execution begins only if the parameters passed in are not null.
   *
   * <p>
   * This method returns a new array with the same elements of the input array except the element
   * that pass predicate tests. The component type of the returned array is always the same as that
   * of the input array.
   *
   * @param <T> the collection type
   * @param array the array that elements will be removed
   * @param predicate the predicate which returns true for elements to be removed
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] removeIf(T[] array, Predicate<? super T> predicate) {
    if (array == null || predicate == null) {
      return array;
    }
    final Class<?> act = array.getClass().getComponentType();
    final T[] removedArray = (T[]) Array.newInstance(act, array.length);
    int j = 0;
    for (T element : array) {
      if (!predicate.test(element)) {
        removedArray[j++] = element;
      }
    }
    return Arrays.copyOf(removedArray, j);
  }

  /**
   * Swaps the two specified elements in the specified array.
   *
   * @param <T> the element type
   * @param array the array in which the elements at two positions will be swapped
   * @param i the position index
   * @param j the other position index
   */
  public static <T> void swap(T[] array, int i, int j) {
    final T t = array[i];
    array[i] = array[j];
    array[j] = t;
  }

  public static <T> T tryCast(Object o, Class<T> cls) {
    return cls.isInstance(o) ? cls.cast(o) : null;
  }

  public static <T> T tryNewInstance(String className) {
    try {
      return forceCast(newInstance(tryAsClass(className)));
    } catch (Exception e) {
      // Noop!
    }
    return null;
  }

}
