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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/** @author Andreas Reichel <andreas@manticore-projects.com> */
public class DriverRecord implements Comparable<DriverRecord> {
  public static final Logger LOGGER = Logger.getLogger(DriverRecord.class.getName());

  int majorVersion;
  int minorVersion;
  int patchId;
  String buildId;
  URL url;

  // 	git rev-list --topo-order -10000 HEAD --pretty=reference --abbrev-commit --reverse | sed -n '1p;0~2p' > ~/data/src/H2MigrationTool/src/com/manticore/h2/h2-git.log
  public static final ArrayList<String> buildIDs = new ArrayList<>();

  public DriverRecord(int majorVersion, int minorVersion, int patchID, String buildId, URL url) {

    if (buildId != null && !buildId.isEmpty())
      synchronized (buildIDs) {
        if (buildIDs.isEmpty()) {
          try (InputStream in =
              ClassLoader.getSystemResourceAsStream("com/manticore/h2/h2-git.log")) {
            for (String line : IOUtils.readLines(in, Charset.defaultCharset())) {
              String id = line.split(" ", 2)[0];
              buildIDs.add(id);
            }
          } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
          }
        }
      }

    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.patchId = patchID;		
    this.buildId = buildId == null || buildId.isBlank() ? "" : buildId;
    this.url = url;
  }

  @Override
  public int compareTo(DriverRecord t) {
    int compareTo = Integer.compare(majorVersion, t.majorVersion);

    if (compareTo == 0) compareTo = Integer.compare(minorVersion, t.minorVersion);

    if (compareTo == 0) compareTo = Integer.compare(patchId, t.patchId);

    if (compareTo == 0) {
      if (buildId.isEmpty() && !t.buildId.isEmpty()) compareTo = -1;
      else if (!buildId.isEmpty() && t.buildId.isEmpty()) compareTo = 1;
      else if (!buildId.isEmpty() && !t.buildId.isEmpty()) {
        int i1 = buildIDs.indexOf(buildId);
        int i2 = buildIDs.indexOf(t.buildId);
        compareTo = Integer.compare(i1, i2);
      }
    }

    return compareTo;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + this.majorVersion;
    hash = 41 * hash + this.minorVersion;
    hash = 41 * hash + this.patchId;
    hash = 41 * hash + Objects.hashCode(this.buildId);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final DriverRecord other = (DriverRecord) obj;
    if (this.majorVersion != other.majorVersion) return false;
    if (this.minorVersion != other.minorVersion) return false;
    if (this.patchId != other.patchId) return false;
    if (!Objects.equals(this.buildId, other.buildId)) return false;
    return true;
  }

  public String getVersion() {
    return majorVersion
        + "."
        + minorVersion
        + "."
        + patchId
        + (!buildId.isEmpty() ? ("-" + buildId) : "");
  }

  @Override
  public String toString() {
    return "H2-" + getVersion();
  }
}
