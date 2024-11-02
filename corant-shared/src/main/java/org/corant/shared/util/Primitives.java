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

import static org.corant.shared.util.Maps.immutableMapOf;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.corant.shared.exception.CorantRuntimeException;

/**
 * corant-shared
 *
 * @author bingo 下午2:52:21
 */
public class Primitives {

  public static final boolean[] EMPTY_BOOLEAN_ARRAY = {};
  public static final byte[] EMPTY_BYTE_ARRAY = Bytes.EMPTY_ARRAY;
  public static final char[] EMPTY_CHAR_ARRAY = Chars.EMPTY_ARRAY;
  public static final short[] EMPTY_SHORT_ARRAY = {};
  public static final int[] EMPTY_INTEGER_ARRAY = {};
  public static final long[] EMPTY_LONG_ARRAY = {};
  public static final float[] EMPTY_FLOAT_ARRAY = {};
  public static final double[] EMPTY_DOUBLE_ARRAY = {};

  public static final Boolean[] EMPTY_WRAP_BOOLEAN_ARRAY = {};
  public static final Byte[] EMPTY_WRAP_BYTE_ARRAY = {};
  public static final Character[] EMPTY_WRAP_CHAR_ARRAY = {};
  public static final Short[] EMPTY_WRAP_SHORT_ARRAY = {};
  public static final Integer[] EMPTY_WRAP_INTEGER_ARRAY = {};
  public static final Long[] EMPTY_WRAP_LONG_ARRAY = {};
  public static final Float[] EMPTY_WRAP_FLOAT_ARRAY = {};
  public static final Double[] EMPTY_WRAP_DOUBLE_ARRAY = {};

  public static final Map<String, Class<?>> NAME_PRIMITIVE_MAP =
      immutableMapOf("boolean", Boolean.TYPE, "byte", Byte.TYPE, "char", Character.TYPE, "short",
          Short.TYPE, "int", Integer.TYPE, "long", Long.TYPE, "float", Float.TYPE, "double",
          Double.TYPE, "void", Void.TYPE);

  public static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP =
      immutableMapOf(Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Character.TYPE,
          Character.class, Short.TYPE, Short.class, Integer.TYPE, Integer.class, Long.TYPE,
          Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class, Void.TYPE, Void.TYPE);

  public static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_MAP =
      Collections.unmodifiableMap(PRIMITIVE_WRAPPER_MAP.entrySet().stream()
          .collect(Collectors.toMap(Entry::getValue, Entry::getKey)));

  public static boolean[] append(final boolean[] array, final boolean... elements) {
    if (array == null) {
      return elements == null ? EMPTY_BOOLEAN_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      boolean[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static byte[] append(final byte[] array, final byte... elements) {
    if (array == null) {
      return elements == null ? EMPTY_BYTE_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      byte[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static char[] append(final char[] array, final char... elements) {
    if (array == null) {
      return elements == null ? EMPTY_CHAR_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      char[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static double[] append(final double[] array, final double... elements) {
    if (array == null) {
      return elements == null ? EMPTY_DOUBLE_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      double[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static float[] append(final float[] array, final float... elements) {
    if (array == null) {
      return elements == null ? EMPTY_FLOAT_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      float[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static int[] append(final int[] array, final int... elements) {
    if (array == null) {
      return elements == null ? EMPTY_INTEGER_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      int[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static long[] append(final long[] array, final long... elements) {
    if (array == null) {
      return elements == null ? EMPTY_LONG_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      long[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static short[] append(final short[] array, final short... elements) {
    if (array == null) {
      return elements == null ? EMPTY_SHORT_ARRAY : elements.clone();
    } else if (elements == null) {
      return array.clone();
    } else {
      short[] ret = Arrays.copyOf(array, array.length + elements.length);
      System.arraycopy(elements, 0, ret, array.length, elements.length);
      return ret;
    }
  }

  public static int[] distinct(int[] array) {
    if (array == null) {
      return null;
    } else {
      final int len = array.length;
      final int[] distinct = new int[len];
      if (len == 0) {
        return distinct;
      } else {
        int index = 0;
        for (int i = 0; i < len; i++) {
          boolean contains = false;
          for (int j = 0; j < i; j++) {
            if (array[i] == array[j]) {
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

  public static boolean[] insertAt(final int index, final boolean[] array,
      final boolean... values) {
    if (array == null) {
      return EMPTY_BOOLEAN_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final boolean[] ret = new boolean[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static byte[] insertAt(final int index, final byte[] array, final byte... values) {
    if (array == null) {
      return EMPTY_BYTE_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final byte[] ret = new byte[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static char[] insertAt(final int index, final char[] array, final char... values) {
    if (array == null) {
      return EMPTY_CHAR_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final char[] ret = new char[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static double[] insertAt(final int index, final double[] array, final double... values) {
    if (array == null) {
      return EMPTY_DOUBLE_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final double[] ret = new double[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static float[] insertAt(final int index, final float[] array, final float... values) {
    if (array == null) {
      return EMPTY_FLOAT_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final float[] ret = new float[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static int[] insertAt(final int index, final int[] array, final int... values) {
    if (array == null) {
      return EMPTY_INTEGER_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final int[] ret = new int[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static long[] insertAt(final int index, final long[] array, final long... values) {
    if (array == null) {
      return EMPTY_LONG_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final long[] ret = new long[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static short[] insertAt(final int index, final short[] array, final short... values) {
    if (array == null) {
      return EMPTY_SHORT_ARRAY;
    }
    if (values == null) {
      return array.clone();
    }
    if (index < 0 || index > array.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
    }
    final short[] ret = new short[array.length + values.length];
    System.arraycopy(values, 0, ret, index, values.length);
    if (index > 0) {
      System.arraycopy(array, 0, ret, 0, index);
    }
    if (index < array.length) {
      System.arraycopy(array, index, ret, index + values.length, array.length - index);
    }
    return ret;
  }

  public static boolean isPrimitiveArray(final Class<?> clazz) {
    return clazz.isArray() && clazz.getComponentType().isPrimitive();
  }

  public static boolean isPrimitiveOrWrapper(final Class<?> clazz) {
    return clazz != null && (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
  }

  public static boolean isPrimitiveWrapper(final Class<?> clazz) {
    return WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
  }

  public static boolean isPrimitiveWrapperArray(final Class<?> clazz) {
    return clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType());
  }

  public static boolean isSimpleClass(Class<?> type) {
    return isPrimitiveOrWrapper(type) || String.class.equals(type)
        || Number.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type)
        || Enum.class.isAssignableFrom(type) || Timestamp.class.isAssignableFrom(type)
        || TemporalAccessor.class.isAssignableFrom(type) || URL.class.isAssignableFrom(type)
        || URI.class.isAssignableFrom(type) || TemporalAmount.class.isAssignableFrom(type)
        || Currency.class.isAssignableFrom(type) || Locale.class.isAssignableFrom(type)
        || TimeZone.class.isAssignableFrom(type);
  }

  public static byte[] removeAll(final byte[] array, final byte... elements) {
    if (array == null || array.length == 0) {
      return EMPTY_BYTE_ARRAY;
    } else if (elements == null || elements.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int newLength = 0;
      byte[] ret = new byte[length];
      for (byte b : array) {
        boolean remove = false;
        for (byte e : elements) {
          if (b == e) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = b;
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  public static char[] removeAll(final char[] array, final char... elements) {
    if (array == null || array.length == 0) {
      return EMPTY_CHAR_ARRAY;
    } else if (elements == null || elements.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int newLength = 0;
      char[] ret = new char[length];
      for (char c : array) {
        boolean remove = false;
        for (char e : elements) {
          if (c == e) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = c;
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  public static double[] removeAll(final double[] array, final double... elements) {
    if (array == null || array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    } else if (elements == null || elements.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int newLength = 0;
      double[] ret = new double[length];
      for (double d : array) {
        boolean remove = false;
        for (double e : elements) {
          if (d == e) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = d;
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  public static float[] removeAll(final float[] array, final float... elements) {
    if (array == null || array.length == 0) {
      return EMPTY_FLOAT_ARRAY;
    } else if (elements == null || elements.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int newLength = 0;
      float[] ret = new float[length];
      for (float f : array) {
        boolean remove = false;
        for (float e : elements) {
          if (f == e) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = f;
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  public static int[] removeAll(final int[] array, final int... elements) {
    if (array == null || array.length == 0) {
      return EMPTY_INTEGER_ARRAY;
    } else if (elements == null || elements.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int newLength = 0;
      int[] ret = new int[length];
      for (int i : array) {
        boolean remove = false;
        for (int e : elements) {
          if (i == e) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = i;
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  public static long[] removeAll(final long[] array, final long... elements) {
    if (array == null || array.length == 0) {
      return EMPTY_LONG_ARRAY;
    } else if (elements == null || elements.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int newLength = 0;
      long[] ret = new long[length];
      for (long l : array) {
        boolean remove = false;
        for (long e : elements) {
          if (l == e) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = l;
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  public static short[] removeAll(final short[] array, final short... elements) {
    if (array == null || array.length == 0) {
      return EMPTY_SHORT_ARRAY;
    } else if (elements == null || elements.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int newLength = 0;
      short[] ret = new short[length];
      for (short s : array) {
        boolean remove = false;
        for (short e : elements) {
          if (s == e) {
            remove = true;
            break;
          }
        }
        if (!remove) {
          ret[newLength] = s;
          newLength++;
        }
      }
      return Arrays.copyOf(ret, newLength);
    }
  }

  public static boolean[] removeAt(final boolean[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_BOOLEAN_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      boolean[] ret = new boolean[length];
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

  public static byte[] removeAt(final byte[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_BYTE_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      byte[] ret = new byte[length];
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

  public static char[] removeAt(final char[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_CHAR_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      char[] ret = new char[length];
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

  public static double[] removeAt(final double[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      double[] ret = new double[length];
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

  public static float[] removeAt(final float[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_FLOAT_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      float[] ret = new float[length];
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

  public static int[] removeAt(final int[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_INTEGER_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      int[] ret = new int[length];
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

  public static long[] removeAt(final long[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_LONG_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      long[] ret = new long[length];
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

  public static short[] removeAt(final short[] array, final int... indices) {
    if (array == null || array.length == 0) {
      return EMPTY_SHORT_ARRAY;
    } else if (indices == null || indices.length == 0) {
      return array.clone();
    } else {
      int length = array.length;
      short[] ret = new short[length];
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

  public static boolean[] slice(final boolean[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static boolean[] slice(final boolean[] array, final int start, final int end,
      final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_BOOLEAN_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_BOOLEAN_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final boolean[] sliceArray = new boolean[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static byte[] slice(final byte[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static byte[] slice(final byte[] array, final int start, final int end, final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_BYTE_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_BYTE_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final byte[] sliceArray = new byte[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static char[] slice(final char[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static char[] slice(final char[] array, final int start, final int end, final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_CHAR_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_CHAR_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final char[] sliceArray = new char[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static double[] slice(final double[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static double[] slice(final double[] array, final int start, final int end,
      final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_DOUBLE_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final double[] sliceArray = new double[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static float[] slice(final float[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static float[] slice(final float[] array, final int start, final int end, final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_FLOAT_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_FLOAT_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final float[] sliceArray = new float[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static int[] slice(final int[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static int[] slice(final int[] array, final int start, final int end, final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_INTEGER_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_INTEGER_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final int[] sliceArray = new int[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static long[] slice(final long[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static long[] slice(final long[] array, final int start, final int end, final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_LONG_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_LONG_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final long[] sliceArray = new long[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static short[] slice(final short[] array, final int start, final int end) {
    return slice(array, start, end, 1);
  }

  public static short[] slice(final short[] array, final int start, final int end, final int step) {
    if (array == null || array.length == 0) {
      return EMPTY_SHORT_ARRAY;
    }
    final int length = array.length;
    final int useStart = start < 0 ? length + start : start;
    final int useEnd = Math.min((end < 0 ? length + end : end), length);
    final int sliceLength = useEnd - useStart;
    if (sliceLength < 0 || useStart > (length - 1)) {
      return EMPTY_SHORT_ARRAY;
    }
    final int useStep = Math.max(step, 1);
    final short[] sliceArray = new short[sliceLength];
    int j = 0;
    for (int i = useStart; i < useEnd; i += useStep, j++) {
      sliceArray[j] = array[i];
    }
    if (sliceLength - j == 1) {
      return sliceArray;
    }
    return Arrays.copyOf(sliceArray, j);
  }

  public static boolean[] unwrap(final Boolean[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BOOLEAN_ARRAY;
    }
    final boolean[] result = new boolean[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static byte[] unwrap(final Byte[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_BYTE_ARRAY;
    }
    final byte[] result = new byte[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static char[] unwrap(final Character[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_CHAR_ARRAY;
    }
    final char[] result = new char[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> unwrap(final Class<T> clazz) {
    if (clazz != null && !clazz.isPrimitive()) {
      return (Class<T>) WRAPPER_PRIMITIVE_MAP.get(clazz);
    } else {
      return clazz;
    }
  }

  public static double[] unwrap(final Double[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_DOUBLE_ARRAY;
    }
    final double[] result = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static float[] unwrap(final Float[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_FLOAT_ARRAY;
    }
    final float[] result = new float[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static int[] unwrap(final Integer[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_INTEGER_ARRAY;
    }
    final int[] result = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static long[] unwrap(final Long[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_LONG_ARRAY;
    }
    final long[] result = new long[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static short[] unwrap(final Short[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_SHORT_ARRAY;
    }
    final short[] result = new short[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Class<?>[] unwrapAll(final Class<?>... classes) {
    if (classes == null || classes.length == 0) {
      return classes;
    }
    final Class<?>[] convertedClasses = new Class[classes.length];
    for (int i = 0; i < classes.length; i++) {
      convertedClasses[i] = unwrap(classes[i]);
    }
    return convertedClasses;
  }

  public static Boolean[] wrap(final boolean[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_BOOLEAN_ARRAY;
    }
    final Boolean[] result = new Boolean[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i] ? Boolean.TRUE : Boolean.FALSE;
    }
    return result;
  }

  public static Byte[] wrap(final byte[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_BYTE_ARRAY;
    }
    final Byte[] result = new Byte[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Character[] wrap(final char[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_CHAR_ARRAY;
    }
    final Character[] result = new Character[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> wrap(final Class<T> clazz) {
    if (clazz != null && clazz.isPrimitive()) {
      return (Class<T>) PRIMITIVE_WRAPPER_MAP.get(clazz);
    } else {
      return clazz;
    }
  }

  public static Double[] wrap(final double[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_DOUBLE_ARRAY;
    }
    final Double[] result = new Double[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Float[] wrap(final float[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_FLOAT_ARRAY;
    }
    final Float[] result = new Float[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Integer[] wrap(final int[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_INTEGER_ARRAY;
    }
    final Integer[] result = new Integer[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Long[] wrap(final long[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_LONG_ARRAY;
    }
    final Long[] result = new Long[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Short[] wrap(final short[] array) {
    if (array == null) {
      return null;
    } else if (array.length == 0) {
      return EMPTY_WRAP_SHORT_ARRAY;
    }
    final Short[] result = new Short[array.length];
    for (int i = 0; i < array.length; i++) {
      result[i] = array[i];
    }
    return result;
  }

  public static Type wrap(final Type type) {
    if (type instanceof Class) {
      return wrap((Class<?>) type);
    } else {
      return type;
    }
  }

  public static Class<?>[] wrapAll(final Class<?>... classes) {
    if (classes == null || classes.length == 0) {
      return classes;
    }
    final Class<?>[] convertedClasses = new Class[classes.length];
    for (int i = 0; i < classes.length; i++) {
      convertedClasses[i] = wrap(classes[i]);
    }
    return convertedClasses;
  }

  public static Object[] wrapArray(Object array) {
    if (array == null) {
      return null;
    } else if (array instanceof Object[]) {
      return (Object[]) array;
    } else if (array.getClass().isArray()) {
      Class<?> eleType = array.getClass().getComponentType();
      if (eleType.equals(Integer.TYPE)) {
        return wrap((int[]) array);
      } else if (eleType.equals(Short.TYPE)) {
        return wrap((short[]) array);
      } else if (eleType.equals(Byte.TYPE)) {
        return wrap((byte[]) array);
      } else if (eleType.equals(Boolean.TYPE)) {
        return wrap((boolean[]) array);
      } else if (eleType.equals(Float.TYPE)) {
        return wrap((float[]) array);
      } else if (eleType.equals(Double.TYPE)) {
        return wrap((double[]) array);
      } else if (eleType.equals(Long.TYPE)) {
        return wrap((long[]) array);
      } else {
        final int len = Array.getLength(array);
        Object[] result = new Object[len];
        for (int i = 0; i < len; i++) {
          result[i] = Array.get(array, i);
        }
        return result;
      }
    }
    throw new CorantRuntimeException("The given parameter must be an array!");
  }

  public boolean contains(final byte[] array, final byte element) {
    return indexOf(array, element, 0) != -1;
  }

  public boolean contains(final char[] array, final char element) {
    return indexOf(array, element, 0) != -1;
  }

  public boolean contains(final double[] array, final double element) {
    return indexOf(array, element, 0) != -1;
  }

  public boolean contains(final float[] array, final float element) {
    return indexOf(array, element, 0) != -1;
  }

  public boolean contains(final int[] array, final int element) {
    return indexOf(array, element, 0) != -1;
  }

  public boolean contains(final long[] array, final long element) {
    return indexOf(array, element, 0) != -1;
  }

  public boolean contains(final short[] array, final short element) {
    return indexOf(array, element, 0) != -1;
  }

  public int indexOf(final boolean[] array, final boolean element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }

  public int indexOf(final byte[] array, final byte element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }

  public int indexOf(char[] array, char element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }

  public int indexOf(final double[] array, final double element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }

  public int indexOf(final float[] array, final float element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }

  public int indexOf(final int[] array, final int element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }

  public int indexOf(final long[] array, final long element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }

  public int indexOf(final short[] array, final short element, final int fromIndex) {
    if (array != null) {
      int length = array.length;
      if (fromIndex < 0 || fromIndex > (length - 1)) {
        throw new IndexOutOfBoundsException("Index: " + fromIndex + ", Length: " + length);
      }
      for (int i = fromIndex; i < length; i++) {
        if (array[i] == element) {
          return i;
        }
      }
    }
    return -1;
  }
}
