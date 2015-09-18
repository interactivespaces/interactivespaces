/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.activity.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;

import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Test the {@link StandardConfigurationPropertyAnnotationProcessor}.
 *
 * @author Oleksandr Kelepko
 */
public class StandardConfigurationPropertyAnnotationProcessorTest {

  Configuration configuration = SimpleConfiguration.newConfiguration();
  final String property = "some.property.name";

  final Log log = new SimpleLog("StandardConfigurationPropertyAnnotationProcessorTest");

  class Injected {
    public Injected() {
      injectConfigValues(this);
    }
  }

  private void injectConfigValues(Object obj) {
    new StandardConfigurationPropertyAnnotationProcessor(configuration, log).process(obj);
  }

  private void setConfigValue(Object value) {
    configuration.setValue(property, String.valueOf(value));
  }

  // int

  @Test
  public void requiredInt_exists_ok() {
    String value = "" + Integer.MAX_VALUE;
    setConfigValue(value);
    int actual = new Injected() {
      @ConfigurationProperty(property)
      private int field;
    }.field;
    int expected = Integer.parseInt(value);
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredInt_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private int field;
    };
  }

  @Test
  public void defaultInt_exists_ok() {
    final int expected = 42;
    setConfigValue(expected);
    int actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private int field = 1 + expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultInt_doesNotExist_ok() {
    final int expected = 42;
    int actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private int field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  // Integer

  @Test
  public void requiredInteger_exists_ok() {
    Integer expected = Integer.MAX_VALUE;
    setConfigValue(expected);
    Integer actual = new Injected() {
      @ConfigurationProperty(property)
      private Integer field;
    }.field;
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredInteger_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private Integer field;
    };
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredInteger_valueNotNull_throwsException() {
    Integer value = 1;
    setConfigValue(value);
    new Object() {
      @ConfigurationProperty(property)
      private Integer field = 0;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    };
  }

  @Test
  public void defaultInteger_exists_ok() {
    final Integer expected = 42;
    setConfigValue(expected);
    Integer actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Integer field = 1 + expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultInteger_doesNotExist_ok() {
    final Integer expected = 42;
    Integer actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Integer field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertSame(expected, actual);
  }

  // long

  @Test
  public void requiredLongPrimitive_exists_ok() {
    long expected = Long.MAX_VALUE;
    setConfigValue(expected);
    long actual = new Injected() {
      @ConfigurationProperty(property)
      private long field;
    }.field;
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredLongPrimitive_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private long field;
    };
  }

  @Test
  public void defaultLongPrimitive_exists_ok() {
    final long expected = 42;
    setConfigValue(expected);
    long actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private long field = 1 + expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultLongPrimitive_doesNotExist_ok() {
    final long expected = 42L;
    long actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private long field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }

    }.field;
    assertEquals(expected, actual);
  }

  // Long

  @Test
  public void requiredLong_exists_ok() {
    Long expected = Long.MAX_VALUE;
    setConfigValue(expected);
    Long actual = new Injected() {
      @ConfigurationProperty(property)
      private Long field;
    }.field;
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredLong_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private Long field;
    };
  }

  @Test
  public void defaultLong_exists_ok() {
    final Long expected = Long.MAX_VALUE;
    setConfigValue(expected);
    Long actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Long field = 1 + expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultLong_doesNotExist_ok() {
    final Long expected = 42L;
    Long actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Long field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertSame(expected, actual);
  }

  // double

  @Test
  public void requiredDoublePrimitive_exists_ok() {
    double expected = Math.PI;
    setConfigValue(expected);
    double actual = new Injected() {
      @ConfigurationProperty(property)
      private double field;
    }.field;
    assertEquals(expected, actual, 1e-20);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredDoublePrimitive_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private double field;
    };
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredDoublePrimitive_notDefaultValue_throwsException() {
    Double value = 0.0;
    setConfigValue(value);
    new Object() {
      @ConfigurationProperty(property)
      private double field = -0.0;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    };
  }

  @Test
  public void defaultDoublePrimitive_exists_ok() {
    final double expected = Math.PI;
    setConfigValue(expected);
    double actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private double field = 1 + expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual, 1e-20);
  }

  @Test
  public void defaultDoublePrimitive_doesNotExist_ok() {
    final double expected = Math.PI;
    double actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private double field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }

    }.field;
    assertEquals(expected, actual, 1e-20);
  }

  // Double

  @Test
  public void requiredDouble_exists_ok() {
    Double expected = Math.PI;
    setConfigValue(expected);
    Double actual = new Injected() {
      @ConfigurationProperty(property)
      private Double field;
    }.field;
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredDouble_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private Double field;
    };
  }

  @Test
  public void defaultDouble_exists_ok() {
    Double expected = Math.PI;
    setConfigValue(expected);
    Double actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Double field;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultDouble_doesNotExist_ok() {
    final Double expected = Math.PI;
    Double actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Double field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }

    }.field;
    assertSame(expected, actual);
  }

  // boolean

  @Test
  public void requiredBooleanPrimitive_exists_ok() {
    boolean expected = true;
    setConfigValue(expected);
    boolean actual = new Injected() {
      @ConfigurationProperty(property)
      private boolean field;
    }.field;
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredBooleanPrimitive_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private boolean field;
    };
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredBooleanPrimitive_notDefaultValue_throwsException() {
    Boolean value = false;
    setConfigValue(value);
    new Object() {
      @ConfigurationProperty(property)
      private boolean field = true;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    };
  }

  @Test
  public void defaultBooleanPrimitive_exists_ok() {
    final boolean expected = true;
    setConfigValue(expected);
    boolean actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private boolean field = !expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultBooleanPrimitive_doesNotExist_ok() {
    final boolean expected = true;
    boolean actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private boolean field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }

    }.field;
    assertEquals(expected, actual);
  }

  // Boolean

  @Test
  public void requiredBoolean_exists_ok() {
    String value = "I am a Boolean";
    setConfigValue(value);
    Boolean actual = new Injected() {
      @ConfigurationProperty(property)
      private Boolean field;
    }.field;
    Boolean expected = Boolean.valueOf(value);
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredBoolean_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private Boolean field;
    };
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredBoolean_valueNotNull_throwsException() {
    Boolean value = false;
    setConfigValue(value);
    new Object() {
      @ConfigurationProperty(property)
      private Boolean field = true;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    };
  }

  @Test
  public void defaultBoolean_exists_ok() {
    final Boolean expected = true;
    setConfigValue(expected);
    Boolean actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Boolean field;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultBoolean_doesNotExist_ok() {
    final Boolean expected = true;
    Boolean actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Boolean field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertSame(expected, actual);
  }

  // String

  @Test
  public void requiredString_exists_ok() {
    String value = "Hello IS";
    setConfigValue(value);
    String actual = new Injected() {
      @ConfigurationProperty(property)
      private String field;
    }.field;
    assertEquals(value, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredString_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private String field;
    };
  }

  @Test
  public void defaultString_exists_ok() {
    final String expected = "Hello IS";
    setConfigValue(expected);
    String actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private String field = "Good bye IS";

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertEquals(expected, actual);
  }

  @Test
  public void defaultString_doesNotExist_ok() {
    final String expected = "Hello IS";
    String actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private String field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }

    }.field;
    assertSame(expected, actual);
  }

  // List

  @Test
  public void requiredList_exists_ok() {
    String value = "1,2,3,4";
    setConfigValue(value);
    List<String> actual = new Injected() {
      @ConfigurationProperty(property)
      private List<String> field;
    }.field;
    List<String> expected = Arrays.asList(value.split(","));
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredList_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private List<String> field;
    };
  }

  @Test
  public void defaultList_exists_ok() {
    String value = "1,2,3,4";
    setConfigValue(value);
    List<String> actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private List<String> field = Arrays.asList("Hello", "IS");

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    List<String> expected = Arrays.asList(value.split(","));
    assertEquals(expected, actual);
  }

  @Test
  public void defaultList_doesNotExist_ok() {
    final List<String> expected = Arrays.asList("Hello", "IS");
    List<String> actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private List<String> field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertSame(expected, actual);
  }

  @Test
  public void requiredIterable_exists_injectsList() {
    String value = "1,2,3,4";
    setConfigValue(value);
    Iterable<String> actual = new Injected() {
      @ConfigurationProperty(property)
      private Iterable<String> field;
    }.field;
    List<String> expected = Arrays.asList(value.split(","));
    assertEquals(expected, actual);
  }

  // Set

  @Test
  public void requiredSet_exists_ok() {
    String value = "1,2,3,4";
    setConfigValue(value);
    Set<String> actual = new Injected() {
      @ConfigurationProperty(property)
      private Set<String> field;
    }.field;
    Set<String> expected = Sets.newHashSet(value.split(","));
    assertEquals(expected, actual);
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void requiredSet_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private Set<String> field;
    };
  }

  @Test
  public void defaultSet_exists_ok() {
    String value = "1,2,3,4";
    setConfigValue(value);
    Set<String> actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Set<String> field = Sets.newHashSet("Hello", "IS");

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    Set<String> expected = Sets.newHashSet(value.split(","));
    assertEquals(expected, actual);
  }

  @Test
  public void defaultSet_doesNotExist_ok() {
    final Set<String> expected = Sets.newHashSet("Hello", "IS");
    Set<String> actual = new Object() {
      @ConfigurationProperty(name = property, required = false)
      private Set<String> field = expected;

      {
        // need to process annotations after default value is set
        injectConfigValues(this);
      }
    }.field;
    assertSame(expected, actual);
  }

  // Unsupported type

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void unsupportedType_exists_throwsException() {
    String value = "123";
    setConfigValue(value);
    new Injected() {
      @ConfigurationProperty(property)
      private Number field;
    };
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void unsupportedType_doesNotExist_throwsException() {
    new Injected() {
      @ConfigurationProperty(property)
      private Number field;
    };
  }

  @Test(expected = SimpleInteractiveSpacesException.class)
  public void finalField_exists_throwsException() {
    String value = "123";
    setConfigValue(value);
    new Injected() {
      @ConfigurationProperty(property)
      private final int field = 42;
    };
  }

  @Test
  public void superclasses_ok() {
    final String propertySuperY = "super.y";
    final String propertySubX = "sub.x";

    class Super {
      @ConfigurationProperty(propertySuperY)
      private int y;
    }

    class Sub extends Super {
      @ConfigurationProperty(propertySubX)
      int x;
    }

    int superY = 123;
    int subX = 456;
    configuration.setValue(propertySuperY, "" + superY);
    configuration.setValue(propertySubX, "" + subX);
    Sub sub = new Sub();
    injectConfigValues(sub);
    assertEquals(sub.x, subX);

    Super sup = sub;
    assertEquals(sup.y, superY);
  }

  @Test
  public void shadowedFields_sameTypes_ok() {
    final String propertySuperX = "super.x";
    final String propertySubX = "sub.x";

    class Super {
      @ConfigurationProperty(propertySuperX)
      int x;
    }

    class Sub extends Super {
      @ConfigurationProperty(propertySubX)
      int x;
    }

    int superX = 123;
    int subX = 456;
    configuration.setValue(propertySuperX, "" + superX);
    configuration.setValue(propertySubX, "" + subX);
    Sub sub = new Sub();
    injectConfigValues(sub);
    assertEquals(sub.x, subX);

    Super sup = sub;
    assertEquals(sup.x, superX);
  }

  @Test
  public void shadowedFields_differentTypes_ok() {
    final String propertySuperX = "super.x";
    final String propertySubX = "sub.x";

    class Super {
      @ConfigurationProperty(propertySuperX)
      int x;
    }

    class Sub extends Super {
      @ConfigurationProperty(propertySubX)
      Double x;
    }

    int superX = 123;
    Double subX = Math.E;
    configuration.setValue(propertySuperX, "" + superX);
    configuration.setValue(propertySubX, "" + subX);
    Sub sub = new Sub();
    injectConfigValues(sub);

    assertEquals(sub.x, subX);
    Super sup = sub;
    assertEquals(sup.x, superX);
  }
}
