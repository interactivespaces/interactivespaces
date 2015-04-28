/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime.standalone.messaging;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.data.json.JsonMapper;
import interactivespaces.util.data.json.StandardJsonMapper;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for handling messages.
 *
 * @author Trevor Pering
 */
public final class MessageUtils {
  /**
   * String for splitting trace filter redaction.
   */
  private static final String TRACE_FILTER_SPLIT_STRING = "@@@";

  /**
   * Private constructor for utility class.
   */
  private MessageUtils() {
  }

  /**
   * Clone a message, redacting any indicated parts.
   *
   * @param target
   *          source message to clone
   * @param redact
   *          fields to redact
   *
   * @return clone of target with redacted parts
   */
  @SuppressWarnings("unchecked")
  static MessageMap deepCloneAndRedact(MessageMap target, MessageMap redact) {
    MessageMap clone = new MessageMap();
    for (String key : target.keySet()) {
      Object targetEntry = target.get(key);
      Object redactEntry = redact == null ? null : redact.get(key);
      clone.put(key, deepCloneAndRedact(targetEntry, redactEntry));
    }
    return clone;
  }

  /**
   * Clone an object, redacting any indicated parts.
   *
   * @param targetEntry
   *          source object to clone
   * @param redactEntry
   *          fields to redact
   *
   * @return clone of target with redacted parts
   */
  @SuppressWarnings("unchecked")
  private static Object deepCloneAndRedact(Object targetEntry, Object redactEntry) {
    if (targetEntry instanceof Map) {
      return deepCloneAndRedact(MessageMap.fromMap(targetEntry), MessageMap.fromMap(redactEntry));
    } else if (targetEntry instanceof List) {
      return deepCloneAndRedact((List<Object>) targetEntry, redactEntry);
    } else if (redactEntry != null) {
      return redactEntry;
    } else {
      return targetEntry;
    }
  }

  /**
   * Clone a list, redacting any indicated parts.
   *
   * @param target
   *          source list to clone
   * @param redact
   *          fields to redact
   *
   * @return clone of target with redacted parts
   */
  @SuppressWarnings("unchecked")
  private static List<Object> deepCloneAndRedact(List<Object> target, Object redact) {
    List<Object> output = Lists.newArrayList();

    List<Object> source = (redact instanceof List) ? (List) redact : target;
    for (Object entry : source) {
      output.add(deepCloneAndRedact(entry, null));
    }
    return output;
  }

  /**
   * Verify if a message matches a template. The template is a data object (consisting of lists, items, or maps), that
   * should verify/match the input target. At each level, the entries should be identical -- except in the case of
   * a list, for which the target merely needs to contain all the items in the template (but may have more).
   * Extra fields in the target with no corresponding template entry are OK and will not prevent a match. Any
   * type mismatch will result in a falure to match (unless .equals() works for the object).
   *
   * The current implementation does not correctly work with lists of complex items (such as lists of maps), but
   * there is no real use case for that and so it's not critical. Typically a list will be a list of simple objects
   * (e.g, strings).
   *
   * @param target
   *          message to match
   * @param template
   *          template to match against
   *
   * @return {@code true} if the message matches the template
   */
  @SuppressWarnings("unchecked")
  static boolean deepMatches(MessageMap target, MessageMap template) {
    for (String key : template.keySet()) {
      Object targetEntry = target.get(key);
      Object templateEntry = template.get(key);

      if (targetEntry instanceof Map && templateEntry instanceof Map) {
        if (!deepMatches(MessageMap.fromMap(targetEntry), MessageMap.fromMap(templateEntry))) {
          return false;
        }
      } else if (templateEntry instanceof List && targetEntry instanceof List) {
        if (!((List) targetEntry).containsAll((List) templateEntry)) {
          return false;
        }
      } else if (templateEntry == null) {
        return targetEntry == null;
      } else if (!templateEntry.equals(targetEntry)) {
        return false;
      }
    }
    return true; // All elements of the map matched!
  }

  /**
   * Make message suitable for tracing, given the input message and message whitelist. This process involves
   * first checking to see if it's a matched trace message, and if so, then redacting any necessary parts.
   *
   * @param message
   *          message to turn into a trace message
   * @param messageWhiteList
   *          whitelist of messages to trace
   *
   * @return cloned trace message, or {@code null} if not to be traces
   */
  public static MessageMap makeTraceMessage(MessageMap message, MessageSetList messageWhiteList) {
    for (MessageSet check : messageWhiteList) {
      if (deepMatches(message, check.get(MessageSet.MESSAGE_KEY))) {
        if (check.size() > 1) {
          return deepCloneAndRedact(message, check.get(MessageSet.REDACT_KEY));
        }
        return message;
      }
    }
    return null;
  }

  /**
   * Read a message set list from a file.
   *
   * @param inputFile
   *          file to convert into a list of message sets
   *
   * @return message set list
   */
  static MessageSetList readMessageList(File inputFile) {
    MessageSetList messageList = new MessageSetList();

    try {
      BufferedReader checkReader = new BufferedReader(new FileReader(inputFile));
      String line;
      while ((line = checkReader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }
        String[] parts = trimmed.split(TRACE_FILTER_SPLIT_STRING);
        MessageSet messageSet = new MessageSet();
        messageSet.put(MessageSet.MESSAGE_KEY, MessageMap.fromString(parts[0]));
        if (parts.length > 1) {
          messageSet.put(MessageSet.REDACT_KEY, MessageMap.fromString(parts[1]));
        }
        if (parts.length > 2) {
          throw new SimpleInteractiveSpacesException("Excess parts to message line: " + line);
        }
        messageList.add(messageSet);
      }
      checkReader.close();
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While processing check file " + inputFile.getAbsolutePath(), e);
    }

    return messageList;
  }

  /**
   * Convenience type for a message map.
   */
  public static class MessageMap extends HashMap<String, Object> {

    /**
     * Json mapper for message conversion.
     */
    private static final JsonMapper MAPPER = StandardJsonMapper.INSTANCE;

    /**
     * Create a new message map from an existing map.
     *
     * @param map
     *          map to transform into a message.
     *
     * @return message map
     */
    @SuppressWarnings("unchecked")
    static MessageMap fromMap(Object map) {
      if (map instanceof MessageMap) {
        return (MessageMap) map;
      }
      MessageMap newMap = new MessageMap();
      newMap.putAll((Map<String, Object>) map);
      return newMap;
    }

    /**
     * Create a new message map from a string.
     *
     * @param input
     *          input to parse into a message.
     *
     * @return message map
     */
    static MessageMap fromString(String input) {
      MessageMap newMap = new MessageMap();
      Map<String, Object> objectMap = MAPPER.parseObject(input);
      newMap.putAll(objectMap);
      return newMap;
    }
  }

  /**
   * Convenience type to contain different parts of a message set.
   */
  public static class MessageSet extends HashMap<String, MessageMap> {

    /**
     * Key for accessing primary message.
     */
    public static final String MESSAGE_KEY = "message";

    /**
     * Key for storing redacted message signature.
     */
    public static final String REDACT_KEY = "redact";

  }

  /**
   * Convenience type to contain a list of message sets.
   */
  public static class MessageSetList extends ArrayList<MessageSet> {
  }
}
