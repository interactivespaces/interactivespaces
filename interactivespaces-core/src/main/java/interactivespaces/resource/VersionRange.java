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

/**
 * A range of {@link Version}s.
 *
 * @author Keith M. Hughes
 */
public class VersionRange {

  /**
   * The symbol showing the upper part of the range is open.
   */
  public static final String RANGE_UPPER_OPEN = ")";

  /**
   * The symbol showing the upper part of the range is closed.
   */
  public static final String RANGE_UPPER_CLOSED = "]";

  /**
   * The symbol showing the lower part of the range is closed.
   */
  public static final String RANGE_LOWER_CLOSED = "[";

  /**
   * The separator between the versions in the range.
   */
  public static final String RANGE_SEPARATOR = ",";

  /**
   * Parse a range string.
   *
   * @param minimumVersion
   *          the minimum version
   * @param maximumVersion
   *          the maximum version
   *
   * @return the range for the string
   *
   * @throws SimpleInteractiveSpacesException
   *           improperly formatted range or a {@code null} string
   */
  public static VersionRange parseVersionRange(String minimumVersion, String maximumVersion)
      throws SimpleInteractiveSpacesException {
    return new VersionRange(Version.parseVersion(minimumVersion), Version.parseVersion(maximumVersion), false);
  }

  /**
   * Parse a range string.
   *
   * @param range
   *          the range string
   *
   * @return the range for the string
   *
   * @throws SimpleInteractiveSpacesException
   *           improperly formatted range or a {@code null} string
   */
  public static VersionRange parseVersionRange(String range) throws SimpleInteractiveSpacesException {
    if (range != null) {
      range = range.trim();

      if (range.startsWith(RANGE_LOWER_CLOSED)) {
        int pos = range.indexOf(RANGE_SEPARATOR, 1);
        if (pos == -1) {
          throw new SimpleInteractiveSpacesException(String.format("Illegal version range %s, missing comma", range));
        }

        Version minimum = Version.parseVersion(range.substring(1, pos));

        Version maximum = null;
        boolean inclusive = false;
        if (range.endsWith(RANGE_UPPER_CLOSED)) {
          inclusive = true;
        } else if (!range.endsWith(RANGE_UPPER_OPEN)) {
          throw new SimpleInteractiveSpacesException(String.format(
              "Illegal version range %s, does not end with ] or )", range));
        }

        maximum = Version.parseVersion(range.substring(pos + 1, range.length() - 1));

        return new VersionRange(minimum, maximum, inclusive);
      } else {
        // Not a full range, but should be a single version
        return new VersionRange(Version.parseVersion(range));
      }
    } else {
      throw new SimpleInteractiveSpacesException("Range is null");
    }
  }

  /**
   * The minimum of the range.
   */
  private Version minimum;

  /**
   * The maximum of the range.
   */
  private Version maximum;

  /**
   * Is the range inclusive?
   */
  private boolean inclusive;

  /**
   * Construct a range which will only match one version.
   *
   * @param version
   *          the version
   */
  public VersionRange(Version version) {
    this(version, version, true);
  }

  /**
   * Construct a range with a minimum and maximum.
   *
   * <p>
   * The range will be marked inclusive if {@code minimum} equals {@code maximum}, since exclusive could never be
   * satisfied.
   *
   * @param minimum
   *          the minimum
   * @param maximum
   *          the maximum
   * @param inclusive
   *          {@code true} if should be inclusive
   */
  public VersionRange(Version minimum, Version maximum, boolean inclusive) {
    if (minimum.equals(maximum)) {
      this.minimum = minimum;
      this.maximum = maximum;
      inclusive = true;
    } else if (maximum.lessThan(minimum)) {
      this.minimum = maximum;
      this.maximum = minimum;

    } else {
      this.minimum = minimum;
      this.maximum = maximum;
    }
    this.inclusive = inclusive;
  }

  /**
   * Get the minimum.
   *
   * @return the minimum
   */
  public Version getMinimum() {
    return minimum;
  }

  /**
   * Set the minimum.
   *
   * @param minimum
   *          the new minimum
   */
  public void setMinimum(Version minimum) {
    this.minimum = minimum;
  }

  /**
   * Get the maximum.
   *
   * @return the maximum
   */
  public Version getMaximum() {
    return maximum;
  }

  /**
   * Set the maximum.
   *
   * @param maximum
   *          the new maximum
   */
  public void setMaximum(Version maximum) {
    this.maximum = maximum;
  }

  /**
   * Is the range inclusive of the maximum?
   *
   * @return {@code true} if inclusive
   */
  public boolean isInclusive() {
    return inclusive;
  }

  /**
   * Set whether the range inclusive of the maximum.
   *
   * @param inclusive
   *          {@code true} if inclusive
   */
  public void setInclusive(boolean inclusive) {
    this.inclusive = inclusive;
  }

  /**
   * Does the range contain the given version?
   *
   * @param version
   *          the version being checked
   *
   * @return {@code true} if the version is in the range
   */
  public boolean contains(Version version) {
    if (version.lessThan(minimum)) {
      return false;
    }

    int maxComp = version.compareTo(maximum);
    return (maxComp < 0) || (maxComp == 0 && inclusive);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (inclusive ? 1231 : 1237);
    result = prime * result + ((maximum == null) ? 0 : maximum.hashCode());
    result = prime * result + ((minimum == null) ? 0 : minimum.hashCode());
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
    VersionRange other = (VersionRange) obj;
    if (inclusive != other.inclusive) {
      return false;
    }
    if (maximum == null) {
      if (other.maximum != null) {
        return false;
      }
    } else if (!maximum.equals(other.maximum)) {
      return false;
    }
    if (minimum == null) {
      if (other.minimum != null) {
        return false;
      }
    } else if (!minimum.equals(other.minimum)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    if (minimum.equals(maximum)) {
      builder.append(minimum.toString());
    } else {
      builder.append(RANGE_LOWER_CLOSED).append(minimum.toString()).append(", ").append(maximum.toString())
          .append(inclusive ? RANGE_UPPER_CLOSED : RANGE_UPPER_OPEN);
    }

    return builder.toString();
  }
}
