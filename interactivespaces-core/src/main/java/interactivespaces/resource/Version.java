/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.resource;

import interactivespaces.SimpleInteractiveSpacesException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version for a resource.
 *
 * <p>
 * Qualifiers are ignored for comparison reasons.
 *
 * @author Keith M. Hughes
 */
public class Version implements Comparable<Version> {

  /**
   * The separator for the version components.
   */
  public static final char VERSION_SECTION_SEPARATOR = '.';

  /**
   * The regular expression for recognizing a version qualifier.
   */
  public static final String QUALIFIER_REGEX = "[a-zA-Z0-9][a-zA-Z0-9_]*";

  /**
   * Pattern for the version.
   */
  public static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+)(\\.[0-9]+)?(\\.[0-9]+)?(\\."
      + QUALIFIER_REGEX + ")?$");

  /**
   * Pattern for just the qualifier.
   */
  public static final Pattern QUALIFIER_PATTERN = Pattern.compile("^" + QUALIFIER_REGEX + "$");

  /**
   * Description of a legal version.
   */
  public static final String VERSION_FORMAT_DESCRIPTION = "A version must be of the form major.minor.micro\n"
      + "where each section is a series of digits.\n"
      + "It can also be of the form major.minor.micro.qualifier where qualifier starts with\n"
      + "a letter or digit, and is followed by letters, digits,\n" + "or underscores.";

  /**
   * Is the candidate have legal syntax for a version?
   *
   * @param candidate
   *          the candidate version
   *
   * @return {@code true} if legal syntax
   */
  public static boolean isLegalSyntax(String candidate) {
    return Version.VERSION_PATTERN.matcher(candidate).matches();
  }

  /**
   * Parse a version string.
   *
   * @param version
   *          the version as a string, can be {@code null}
   *
   * @return the version represented by the string, or {@code null} if version was {@code null}
   *
   * @throws SimpleInteractiveSpacesException
   *           improperly formatted version
   */
  public static Version parseVersionIncludeNull(String version) throws SimpleInteractiveSpacesException {
    return version != null ? parseVersion(version) : null;
  }

  /**
   * Parse a version string.
   *
   * @param version
   *          the version as a string
   *
   * @return the version represented by the string
   *
   * @throws SimpleInteractiveSpacesException
   *           improperly formatted version or a {@code null} string
   */
  public static Version parseVersion(String version) throws SimpleInteractiveSpacesException {
    if (version != null) {
      version = version.trim();
      Matcher matcher = VERSION_PATTERN.matcher(version);
      if (matcher.matches()) {
        String major = matcher.group(1);
        String minor = matcher.group(2);
        String incremental = matcher.group(3);

        String qualifier = matcher.group(4);
        if (qualifier != null) {
          qualifier = qualifier.substring(1);
        }
        return new Version(Integer.parseInt(major), (minor != null ? Integer.parseInt(minor.substring(1)) : 0),
            (incremental != null ? Integer.parseInt(incremental.substring(1)) : 0), qualifier);
      } else {
        throw new SimpleInteractiveSpacesException(String.format("Illegal version %s", version));
      }
    } else {
      throw new SimpleInteractiveSpacesException("Illegal version null");
    }
  }

  /**
   * The major version.
   */
  private final int major;

  /**
   * The minor version.
   */
  private final int minor;

  /**
   * The micro version.
   */
  private final int micro;

  /**
   * Any qualifier.
   */
  private final String qualifier;

  /**
   * Construct a version.
   *
   * @param major
   *          major version
   * @param minor
   *          minor version
   * @param micro
   *          micro version
   * @param qualifier
   *          qualifier, can be {@code null}
   */
  public Version(int major, int minor, int micro, String qualifier) {
    if (qualifier == null) {
      qualifier = "";
    }

    this.major = major;
    this.minor = minor;
    this.micro = micro;
    this.qualifier = qualifier;

    validate();
  }

  /**
   * Construct a version with a null qualifier.
   *
   * @param major
   *          major version
   * @param minor
   *          minor version
   * @param micro
   *          micro version
   */
  public Version(int major, int minor, int micro) {
    this(major, minor, micro, null);
  }

  /**
   * Get the major number.
   *
   * @return the major number
   */
  public int getMajor() {
    return major;
  }

  /**
   * Get the minor number.
   *
   * @return the minor number
   */
  public int getMinor() {
    return minor;
  }

  /**
   * Get the micro number.
   *
   * @return the micro number
   */
  public int getMicro() {
    return micro;
  }

  /**
   * Get the qualifier.
   *
   * @return the qualifier
   */
  public String getQualifier() {
    return qualifier;
  }

  /**
   * Is the current version strictly less than the other version?
   *
   * @param other
   *          the other version
   *
   * @return {@code true} if less than
   */
  public boolean lessThan(Version other) {
    return compareTo(other) < 0;
  }

  /**
   * Is the current version less than or equal the other version?
   *
   * @param other
   *          the other version
   *
   * @return {@code true} if less than or equal
   */
  public boolean lessThanOrEqual(Version other) {
    return compareTo(other) <= 0;
  }

  /**
   * Is the current version strictly greater than the other version?
   *
   * @param other
   *          the other version
   *
   * @return {@code true} if strictly greater than
   */
  public boolean greaterThan(Version other) {
    return compareTo(other) > 0;
  }

  /**
   * Is the current version greater than or equal the other version?
   *
   * @param other
   *          the other version
   *
   * @return {@code true} if greater than or equal
   */
  public boolean greaterThanOrEqual(Version other) {
    return compareTo(other) >= 0;
  }

  /**
   * Get a new version with an incremented minor. The minor and micro are then {@code 0}. The qualifier is left alone.
   *
   * @return a newly constructed version
   */
  public Version incrementMajor() {
    return new Version(major + 1, 0, 0, qualifier);
  }

  /**
   * Get a new version with an incremented minor. The micro is then {@code 0}. The qualifier is left alone.
   *
   * @return a newly constructed version
   */
  public Version incrementMinor() {
    return new Version(major, minor + 1, 0, qualifier);
  }

  /**
   * Get a new version with an incremented micro. The qualifier is left alone.
   *
   * @return a newly constructed version
   */
  public Version incrementMicro() {
    return new Version(major, minor, micro + 1, qualifier);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + micro;
    result = prime * result + major;
    result = prime * result + minor;
    result = prime * result + qualifier.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Version other = (Version) obj;
    return (major == other.major) && (minor == other.minor) && (micro == other.micro)
        && qualifier.equals(other.qualifier);
  }

  @Override
  public int compareTo(Version o) {
    if (o == this) {
      return 0;
    }

    int diff = major - o.major;
    if (diff == 0) {
      diff = minor - o.minor;

      if (diff == 0) {
        diff = micro - o.micro;

        if (diff == 0) {
          diff = qualifier.compareTo(o.qualifier);
        }
      }
    }

    return diff;
  }

  @Override
  public String toString() {
    StringBuilder builder =
        new StringBuilder().append(major).append(VERSION_SECTION_SEPARATOR).append(minor)
            .append(VERSION_SECTION_SEPARATOR).append(micro);

    if (!qualifier.isEmpty()) {
      builder.append(VERSION_SECTION_SEPARATOR).append(qualifier);
    }
    return builder.toString();
  }

  /**
   * Validate the version components.
   */
  private void validate() {
    if (major < 0) {
      throw new SimpleInteractiveSpacesException(String.format("Major version number cannot be negative: %d", major));
    }
    if (minor < 0) {
      throw new SimpleInteractiveSpacesException(String.format("Minor version number cannot be negative: %d", minor));
    }
    if (micro < 0) {
      throw new SimpleInteractiveSpacesException(String.format("Micro version number cannot be negative: %d", micro));
    }
    if (!qualifier.isEmpty() && !Version.QUALIFIER_PATTERN.matcher(qualifier).matches()) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Version qualifiers must use only characters a-z, A-Z, 0-9, _ and - cannot be negative: %s", qualifier));
    }
  }
}
