/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.h2;

import java.net.URL;

/** @author are */
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

      if (compareTo == 0) compareTo = Integer.compare(minorVersion, t.minorVersion);

      if (compareTo == 0) compareTo = Integer.compare(buildId, t.buildId);

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
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final DriverRecord other = (DriverRecord) obj;
      if (this.majorVersion != other.majorVersion) return false;
      if (this.minorVersion != other.minorVersion) return false;
      if (this.buildId != other.buildId) return false;
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
