/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.suites.bundle;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.corant.shared.util.ClassPaths;
import org.corant.shared.util.ClassPaths.ResourceInfo;

/**
 * corant-suites-bundle
 *
 * @author bingo 下午3:47:37
 *
 */
public class PropertyResourceBundle extends ResourceBundle {

  public static final String LOCALE_SPT = "_";

  private Map<String, Object> lookup;

  private long lastModifiedTime;

  private Locale locale;

  private String baseBundleName;

  @SuppressWarnings({"rawtypes", "unchecked"})
  public PropertyResourceBundle(ResourceInfo fo) throws IOException {
    baseBundleName = fo.getResourceName();
    locale = PropertyResourceBundle.detectLocaleByName(baseBundleName);
    lastModifiedTime = Instant.now().toEpochMilli();
    Properties properties = new Properties();
    properties.load(new InputStreamReader(fo.openStream(), Charset.forName("utf-8")));
    lookup = new HashMap(properties);
  }

  public static Map<String, PropertyResourceBundle> getBundles(String classPath,
      Predicate<ResourceInfo> fs) {
    Map<String, PropertyResourceBundle> map = new HashMap<>();
    try {
      ClassPaths.from(classPath).getResources().filter(fs).forEach((fo) -> {
        try {
          map.putIfAbsent(fo.getResourceName(), new PropertyResourceBundle(fo));
        } catch (IOException e) {
        }
      });
    } catch (IOException e) {
    }
    return map;
  }

  protected static Locale detectLocaleByName(String name) {
    int f = name != null ? name.indexOf(LOCALE_SPT) : -1;
    if (f > 0) {
      return LocaleUtils.toLocale(FilenameUtils.getBaseName(name.substring(f + 1)));
    } else {
      return Locale.getDefault();
    }
  }

  public Map<String, String> dump() {
    Map<String, String> map = new HashMap<>();
    Enumeration<String> msgKeys = getKeys();
    while (msgKeys.hasMoreElements()) {
      String msgKey = msgKeys.nextElement();
      String mfv = getString(msgKey);
      if (mfv != null) {
        map.put(msgKey, mfv);
      }
    }
    return map;
  }

  @Override
  public String getBaseBundleName() {
    return baseBundleName;
  }

  @Override
  public Enumeration<String> getKeys() {
    ResourceBundle parent = this.parent;
    return new ResourceBundleEnumeration(lookup.keySet(), parent != null ? parent.getKeys() : null);
  }

  public long getLastModifiedTime() {
    return lastModifiedTime;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  protected Object handleGetObject(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    return lookup.get(key);
  }

  public static class ResourceBundleEnumeration implements Enumeration<String> {

    Set<String> set;
    Iterator<String> iterator;
    Enumeration<String> enumeration; // may remain null

    String next = null;

    /**
     * Constructs a resource bundle enumeration.
     *
     * @param set an set providing some elements of the enumeration
     * @param enumeration an enumeration providing more elements of the enumeration.
     *        enumeration may be null.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResourceBundleEnumeration(Set<String> set, Enumeration enumeration) {
      this.set = set;
      iterator = set.iterator();
      this.enumeration = enumeration;
    }

    @Override
    public boolean hasMoreElements() {
      if (next == null) {
        if (iterator.hasNext()) {
          next = iterator.next();
        } else if (enumeration != null) {
          while (next == null && enumeration.hasMoreElements()) {
            next = enumeration.nextElement();
            if (set.contains(next)) {
              next = null;
            }
          }
        }
      }
      return next != null;
    }

    @Override
    public String nextElement() {
      if (hasMoreElements()) {
        String result = next;
        next = null;
        return result;
      } else {
        throw new NoSuchElementException();
      }
    }
  }
}
