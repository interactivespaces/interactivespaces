package interactivespaces.workbench;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.data.json.JsonMapper;
import org.apache.commons.logging.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 */
public class ProtocolReceptor {

  /**
   * For converting json strings.
   */
  private static final JsonMapper MAPPER = new JsonMapper();

  /**
   * Map of target protocol receptors.
   */
  private Map<String, Object> targetMap = Maps.newHashMap();

  /**
   * Logger to use.
   */
  private final Log log;

  /**
   * Create a new instance.
   *
   * @param log
   *          logger to use
   */
  public ProtocolReceptor(Log log) {
    this.log = log;
  }

  /**
   * Add a target object.
   *
   * @param targetId
   *          target id
   * @param target
   *          target object
   */
  public void addTarget(String targetId, Object target) {
    targetMap.put(targetId, target);
  }

  /**
   * Reflect input from a given file path.
   *
   * @param inputPath
   *          file path to process
   */
  public void reflectFromPath(String inputPath) {
    try {
      List<String> parameters = Files.readLines(new File(inputPath), Charset.defaultCharset());
      for (String line : parameters) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
          continue;
        }
        String[] parts = trimmed.split("=", 2);
        if (parts.length != 2) {
          throw new SimpleInteractiveSpacesException("Invalid property line syntax: " + line);
        }
        String[] targets = parts[0].split("\\.", 2);
        String propertyContainer = targets[0];
        if (!targetMap.containsKey(propertyContainer)) {
          throw new SimpleInteractiveSpacesException("Target object " + propertyContainer + " not registered");
        }
        Object propertyObject = targetMap.get(propertyContainer);
        String propertyName = targets[1];
        String propertyValue = parts[1];
        setTargetProperty(propertyObject, propertyName, propertyValue);
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Could not read/process spec file " + inputPath, e);
    }
  }

  /**
   * Set/add the dynamically specified project property. This will search the project class for a matching
   * setter or adder, and then set or add the property, accordingly.
   *
   * @param target
   *          the target object on which to set the property
   * @param name
   *          property name to set or add
   * @param value
   *          value to set
   */
  public void setTargetProperty(Object target, String name, String value) {
    try {
      String camlName = name.substring(0, 1).toUpperCase() + name.substring(1);
      Method setter = findMethod(target.getClass(), "set", camlName);
      setter = setter != null ? setter : findMethod(target.getClass(), "add", camlName);
      if (setter == null) {
        throw new SimpleInteractiveSpacesException("Matching set/add method not found");
      }
      Class<?> parameterType = setter.getParameterTypes()[0];
      if (parameterType.isAssignableFrom(String.class)) {
        setter.invoke(target, value);
        return;
      }

      Constructor<?> constructor = findStringConstructor(parameterType);
      if (constructor != null) {
        setter.invoke(target, constructor.newInstance(value));
        return;
      }

      Method converter = findMethod(parameterType, "parse", parameterType.getSimpleName());
      if (converter != null) {
        setter.invoke(target, converter.invoke(null, value));
        return;
      }

      Class<?> targetType = setter.getParameterTypes()[0];
      Object valueObject = buildFromJson(targetType, value);
      setter.invoke(target, valueObject);

    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(
          String.format("Could not set/add property %s value %s", name, value), e);
    }
  }

  /**
   * Dynamically find a matching method.
   *
   * @param targetClass
   *          the target class to query
   * @param prefix
   *          function prefix name
   * @param name
   *          function property name
   *
   * @return method found, or {@code null}
   */
  Method findMethod(Class<?> targetClass, String prefix, String name) {
    String methodName = prefix + name;
    Method[] methods = targetClass.getMethods();
    for (Method method : methods) {
      if (method.getName().equals(methodName)) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
          return method;
        }
      }
    }
    log.debug(String.format("Could not find method %s in class %s", methodName, targetClass.getName()));
    return null;
  }

  /**
   * Find a matching string constructor for the given type.
   *
   * @param targetType
   *          target class for which to find a constructor
   * @param <T>
   *          target type
   *
   * @return matching string constructor, or {@code null} if none
   */
  public <T> Constructor<T> findStringConstructor(Class<T> targetType) {
    try {
      return targetType.getConstructor(String.class);
    } catch (Exception e) {
      log.debug(String.format("Could not find string constructor for %s", targetType.getName()));
      return null;
    }
  }

  /**
   * Build a class of a given type from a json string.
   *
   * @param targetType
   *          type to build
   * @param json
   *          json input
   * @param <T>
   *          output type
   *
   * @return created object
   */
  public <T> T buildFromJson(Class<T> targetType, String json) {
    try {
      T targetObject = targetType.newInstance();
      Map<String, Object> fieldMap;
      try {
        fieldMap = MAPPER.parseObject(json);
      } catch (Exception e) {
        throw new SimpleInteractiveSpacesException("Parsing json: " + json, e);
      }
      for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
        String fieldName = entry.getKey();
        String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
          Method method = targetType.getMethod(methodName, entry.getValue().getClass());
          method.invoke(targetObject, entry.getValue());
        } catch (Exception e) {
          throw new SimpleInteractiveSpacesException("Could not find method " + methodName, e);
        }
      }
      return targetObject;
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("Building from json for " + targetType.getSimpleName(), e);
    }
  }

}
