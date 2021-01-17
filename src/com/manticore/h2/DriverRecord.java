/*
 * Copyright (C) 2020 Andreas Reichel<andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.manticore.h2;

import java.net.URL;

/**
 *
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class DriverRecord implements Comparable<DriverRecord> {

  int majorVersion;
  int minorVersion;
  int buildId;
  URL url;

  public DriverRecord(int majorVersion, int minorVersion, int buildId, URL url) {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.buildId = buildId;
    this.url = url;
  }

  @Override
  public int compareTo(DriverRecord t) {
    int compareTo = Integer.compare(majorVersion, t.majorVersion);

    if (compareTo == 0)
      compareTo = Integer.compare(minorVersion, t.minorVersion);

    if (compareTo == 0)
      compareTo = Integer.compare(buildId, t.buildId);

    return compareTo;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 29 * hash + this.majorVersion;
    hash = 29 * hash + this.minorVersion;
    hash = 29 * hash + this.buildId;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final DriverRecord other = (DriverRecord) obj;
    if (this.majorVersion != other.majorVersion)
      return false;
    if (this.minorVersion != other.minorVersion)
      return false;
    if (this.buildId != other.buildId)
      return false;
    return true;
  }

  public String getVersion() {
    return majorVersion + "." + minorVersion + "." + buildId;
  }

  @Override
  public String toString() {
    return "H2-" + majorVersion + "." + minorVersion + "." + buildId;
  }
}
