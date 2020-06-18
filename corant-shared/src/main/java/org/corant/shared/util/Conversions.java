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

import static org.corant.shared.util.Classes.asClass;
import static org.corant.shared.util.Maps.mapOf;
import static org.corant.shared.util.ObjectUtils.defaultObject;
import static org.corant.shared.util.Streams.streamOf;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.corant.shared.conversion.Conversion;
import org.corant.shared.conversion.ConverterHints;
import org.corant.shared.conversion.converter.NumberBigDecimalConverter;
import org.corant.shared.conversion.converter.NumberBigIntegerConverter;
import org.corant.shared.conversion.converter.StringBigDecimalConverter;
import org.corant.shared.conversion.converter.StringBigIntegerConverter;

/**
 * corant-shared
 *
 * @author bingo 上午10:05:26
 *
 */
public class Conversions {

  private Conversions() {
    super();
  }

  /**
   * Convert an object to BigDecimal object, supports String to BigDecimal Number to BigDecimal
   *
   * @param obj
   * @return the converted object
   *
   * @see StringBigDecimalConverter
   * @see NumberBigDecimalConverter
   */
  public static BigDecimal toBigDecimal(Object obj) {
    return toBigDecimal(obj, null);
  }

  /**
   * Convert an object to BigDecimal object, supports String to BigDecimal Number to BigDecimal,
   * return the given alternative object if the converted object is null else return the converted
   * obejct.
   *
   * @param obj
   * @param altVal the alternative object if converted object is null
   * @return the converted object
   * @see StringBigDecimalConverter
   * @see NumberBigDecimalConverter
   */
  public static BigDecimal toBigDecimal(Object obj, BigDecimal altVal) {
    return defaultObject(Conversion.convert(obj, BigDecimal.class), altVal);
  }

  /**
   * Convert an object to rounding BigDecimal object with scale, supports String to BigDecimal
   * Number to BigDecimal, return the given alternative object if the converted object is null else
   * return the converted obejct.
   *
   * @param obj
   * @param altVal
   * @param scale
   * @param roundingMode
   * @return the converted object
   * @see StringBigDecimalConverter
   * @see NumberBigDecimalConverter
   */
  public static BigDecimal toBigDecimal(Object obj, BigDecimal altVal, int scale,
      int roundingMode) {
    BigDecimal d = defaultObject(Conversion.convert(obj, BigDecimal.class), altVal);
    return d == null ? null : d.setScale(scale, roundingMode);
  }

  /**
   * Convert an object to rounding BigDecimal object with scale, use
   * {@code BigDecimal#ROUND_HALF_UP}. supports String to BigDecimal Number to BigDecimal
   *
   * @param obj
   * @param scale
   * @return the converted object
   */
  public static BigDecimal toBigDecimal(Object obj, int scale) {
    BigDecimal d = toBigDecimal(obj, null);
    return d == null ? null : d.setScale(scale, BigDecimal.ROUND_HALF_UP);
  }

  /**
   * Convert an object to BigDecimal list.
   *
   * Supports String[], Number[], Iterable&lt;String&gt;, Iterable&lt;? extends Number&gt;,
   * Iterator&lt;String&gt;, Iterator&lt;? extends Number&gt;, Enumeration&lt;String&gt;,
   * Enumeration&lt;? extends Number&gt;
   *
   * @param obj
   * @return the converted object list
   *
   * @see Conversion#convert(Object, Class, Supplier, Map)
   */
  public static List<BigDecimal> toBigDecimalList(Object obj) {
    return toList(obj, BigDecimal.class);
  }

  /**
   * Convert an object to BigInteger object, supports String to BigInteger Number to BigInteger
   *
   * @param obj
   * @return the converted object
   *
   * @see StringBigIntegerConverter
   * @see NumberBigIntegerConverter
   */
  public static BigInteger toBigInteger(Object obj) {
    return toBigInteger(obj, null);
  }

  public static BigInteger toBigInteger(Object obj, BigInteger altVal) {
    return defaultObject(Conversion.convert(obj, BigInteger.class), altVal);
  }

  public static List<BigInteger> toBigIntegerList(Object obj) {
    return toList(obj, BigInteger.class);
  }

  public static Boolean toBoolean(Object obj) {
    return defaultObject(Conversion.convert(obj, Boolean.class), Boolean.FALSE);
  }

  public static Byte toByte(Object obj) {
    return toByte(obj, null);
  }

  public static Byte toByte(Object obj, Byte altVal) {
    return defaultObject(Conversion.convert(obj, Byte.class), altVal);
  }

  public static Character toCharacter(Object obj) {
    return Conversion.convert(obj, Character.class, null);
  }

  public static Class<?> toClass(Object obj) {
    return Conversion.convert(obj, Class.class);
  }

  public static <T, C extends Collection<T>> C toCollection(Object obj, Class<T> itemClass,
      Supplier<C> collectionFactory) {
    return Conversion.convert(obj, itemClass, collectionFactory, null);
  }

  public static Currency toCurrency(Object obj) {
    return toCurrency(obj, null);
  }

  public static Currency toCurrency(Object obj, Currency altVal) {
    return defaultObject(Conversion.convert(obj, Currency.class), altVal);
  }

  public static Double toDouble(Object obj) {
    return toDouble(obj, null);
  }

  public static Double toDouble(Object obj, Double altVal) {
    return defaultObject(Conversion.convert(obj, Double.class), altVal);
  }

  public static List<Double> toDoubleList(Object obj) {
    return toList(obj, Double.class);
  }

  public static Duration toDuration(Object obj) {
    return toDuration(obj, null);
  }

  public static Duration toDuration(Object obj, Duration altVal) {
    return defaultObject(Conversion.convert(obj, Duration.class), altVal);
  }

  public static <T extends Enum<T>> T toEnum(Object obj, Class<T> enumClazz) {
    return Conversion.convert(obj, enumClazz);
  }

  public static <T extends Enum<T>> List<T> toEnumList(Object obj, Class<T> enumClazz) {
    return toList(obj, enumClazz);
  }

  public static Float toFloat(Object obj) {
    return toFloat(obj, null);
  }

  public static Float toFloat(Object obj, Float altVal) {
    return defaultObject(Conversion.convert(obj, Float.class), altVal);
  }

  public static List<Float> toFloatList(Object obj) {
    return toList(obj, Float.class);
  }

  public static Instant toInstant(Object obj) {
    return toInstant(obj, (Map<String, ?>) null, null);
  }

  public static Instant toInstant(Object obj, Instant altVal) {
    return toInstant(obj, (Map<String, ?>) null, altVal);
  }

  public static Instant toInstant(Object obj, Map<String, ?> hints) {
    return toInstant(obj, hints, null);
  }

  public static Instant toInstant(Object obj, Map<String, ?> hints, Instant altVal) {
    return defaultObject(Conversion.convert(obj, Instant.class, hints), altVal);
  }

  public static List<Instant> toInstantList(Object obj) {
    return toList(obj, Instant.class);
  }

  public static List<Instant> toInstantList(Object obj, Map<String, ?> hints) {
    return toList(obj, Instant.class, hints);
  }

  public static Integer toInteger(Object obj) {
    return toInteger(obj, null);
  }

  public static Integer toInteger(Object obj, Integer altVal) {
    return defaultObject(Conversion.convert(obj, Integer.class), altVal);
  }

  public static List<Integer> toIntegerList(Object obj) {
    return toList(obj, Integer.class);
  }

  public static <T> List<T> toList(Object obj, Class<T> clazz) {
    return toList(obj, clazz, null);
  }

  public static <T> List<T> toList(Object obj, Class<T> clazz, Map<String, ?> hints) {
    if (obj instanceof Collection) {
      return Conversion.convert((Collection<?>) obj, ArrayList::new, clazz, hints);
    } else if (obj instanceof Object[]) {
      return Conversion.convert((Object[]) obj, ArrayList::new, clazz, hints);
    }
    return Conversion.convert(obj, clazz, ArrayList::new, hints);
  }

  public static <T> List<T> toList(Object obj, Function<Object, T> convert) {
    List<T> values = new ArrayList<>();
    if (obj instanceof Iterable<?>) {
      values = streamOf((Iterable<?>) obj).map(convert).collect(Collectors.toList());
    } else if (obj instanceof Object[]) {
      values = streamOf((Object[]) obj).map(convert).collect(Collectors.toList());
    }
    return values;
  }

  public static LocalDate toLocalDate(Object obj) {
    return toLocalDate(obj, (Map<String, ?>) null, null);
  }

  public static LocalDate toLocalDate(Object obj, LocalDate altVal) {
    return toLocalDate(obj, (Map<String, ?>) null, altVal);
  }

  public static LocalDate toLocalDate(Object obj, Map<String, ?> hints, LocalDate altVal) {
    return defaultObject(Conversion.convert(obj, LocalDate.class, hints), altVal);
  }

  public static LocalDate toLocalDate(Object obj, String pattern) {
    return toLocalDate(obj, pattern, null);
  }

  public static LocalDate toLocalDate(Object obj, String pattern, LocalDate altVal) {
    if (pattern != null) {
      return toLocalDate(obj, mapOf(ConverterHints.CVT_TEMPORAL_FMT_PTN_KEY, pattern), altVal);
    } else {
      return toLocalDate(obj, (Map<String, ?>) null, altVal);
    }
  }

  public static LocalDate toLocalDate(Object obj, ZoneId zoneId) {
    if (zoneId != null) {
      return toLocalDate(obj, mapOf(ConverterHints.CVT_ZONE_ID_KEY, zoneId), null);
    } else {
      return toLocalDate(obj, (Map<String, ?>) null, null);
    }
  }

  public static List<LocalDate> toLocalDateList(Object obj, Map<String, ?> hints) {
    return toList(obj, LocalDate.class, hints);
  }

  public static LocalDateTime toLocalDateTime(Object obj) {
    return toLocalDateTime(obj, (Map<String, ?>) null, null);
  }

  public static LocalDateTime toLocalDateTime(Object obj, LocalDateTime altVal) {
    return toLocalDateTime(obj, (Map<String, ?>) null, altVal);
  }

  public static LocalDateTime toLocalDateTime(Object obj, Map<String, ?> hints,
      LocalDateTime altVal) {
    return defaultObject(Conversion.convert(obj, LocalDateTime.class, hints), altVal);
  }

  public static LocalDateTime toLocalDateTime(Object obj, String pattern) {
    return toLocalDateTime(obj, pattern, null);
  }

  public static LocalDateTime toLocalDateTime(Object obj, String pattern, LocalDateTime altVal) {
    if (pattern != null) {
      return toLocalDateTime(obj, mapOf(ConverterHints.CVT_TEMPORAL_FMT_PTN_KEY, pattern), altVal);
    } else {
      return toLocalDateTime(obj, (Map<String, ?>) null, null);
    }

  }

  public static LocalDateTime toLocalDateTime(Object obj, ZoneId zoneId) {
    if (zoneId != null) {
      return toLocalDateTime(obj, mapOf(ConverterHints.CVT_ZONE_ID_KEY, zoneId), null);
    } else {
      return toLocalDateTime(obj, (Map<String, ?>) null, null);
    }
  }

  public static List<LocalDateTime> toLocalDateTimeList(Object obj, String pattern) {
    if (pattern != null) {
      return toList(obj, LocalDateTime.class,
          mapOf(ConverterHints.CVT_TEMPORAL_FMT_PTN_KEY, pattern));
    } else {
      return toList(obj, LocalDateTime.class);
    }
  }

  public static Locale toLocale(Object obj) {
    return toLocale(obj, null);
  }

  public static Locale toLocale(Object obj, Locale altVal) {
    return defaultObject(Conversion.convert(obj, Locale.class), altVal);
  }

  public static Long toLong(Object obj) {
    return toLong(obj, null);
  }

  public static Long toLong(Object obj, Long altVal) {
    return defaultObject(Conversion.convert(obj, Long.class), altVal);
  }

  public static List<Long> toLongList(Object obj) {
    return toList(obj, Long.class);
  }

  public static <T> T toObject(Object obj, Class<T> clazz) {
    return Conversion.convert(obj, clazz);
  }

  public static <T> T toObject(Object obj, Class<T> clazz, Map<String, ?> hints) {
    return Conversion.convert(obj, clazz, hints);
  }

  public static Object toObject(Object obj, String className) {
    return toObject(obj, asClass(className));
  }

  public static <T> Set<T> toSet(Object obj, Class<T> clazz) {
    return toSet(obj, clazz, null);
  }

  public static <T> Set<T> toSet(Object obj, Class<T> clazz, Map<String, ?> hints) {
    if (obj instanceof Collection) {
      return Conversion.convert((Collection<?>) obj, HashSet::new, clazz, hints);
    } else if (obj instanceof Object[]) {
      return Conversion.convert((Object[]) obj, HashSet::new, clazz, hints);
    }
    return Conversion.convert(obj, clazz, HashSet::new, hints);
  }

  public static Short toShort(Object obj) {
    return toShort(obj, null);
  }

  public static Short toShort(Object obj, Short altVal) {
    return defaultObject(Conversion.convert(obj, Short.class), altVal);
  }

  public static List<Short> toShortList(Object obj) {
    return toList(obj, Short.class);
  }

  public static String toString(Object obj) {
    return obj == null ? null : obj.toString();
  }

  public static TimeZone toTimeZone(Object obj) {
    return Conversion.convert(obj, TimeZone.class);
  }

  public static ZonedDateTime toZonedDateTime(Object obj) {
    return toZonedDateTime(obj, (Map<String, ?>) null, null);
  }

  public static ZonedDateTime toZonedDateTime(Object obj, Map<String, ?> hints,
      ZonedDateTime altVal) {
    return defaultObject(Conversion.convert(obj, ZonedDateTime.class, hints), altVal);
  }

  public static ZonedDateTime toZonedDateTime(Object obj, String pattern) {
    return toZonedDateTime(obj, pattern, null);
  }

  public static ZonedDateTime toZonedDateTime(Object obj, String pattern, ZonedDateTime altVal) {
    if (pattern != null) {
      return toZonedDateTime(obj, mapOf(ConverterHints.CVT_TEMPORAL_FMT_PTN_KEY, pattern), altVal);
    } else {
      return toZonedDateTime(obj, (Map<String, ?>) null, altVal);
    }
  }

  public static ZonedDateTime toZonedDateTime(Object obj, ZonedDateTime altVal) {
    return toZonedDateTime(obj, (Map<String, ?>) null, altVal);
  }

  public static ZonedDateTime toZonedDateTime(Object obj, ZoneId zoneId) {
    if (zoneId != null) {
      return toZonedDateTime(obj, mapOf(ConverterHints.CVT_ZONE_ID_KEY, zoneId), null);
    } else {
      return toZonedDateTime(obj, (Map<String, ?>) null, null);
    }
  }

  public static List<ZonedDateTime> toZonedDateTimeList(Object obj, String pattern) {
    if (pattern != null) {
      return toList(obj, ZonedDateTime.class,
          mapOf(ConverterHints.CVT_TEMPORAL_FMT_PTN_KEY, pattern));
    } else {
      return toList(obj, ZonedDateTime.class);
    }
  }
}