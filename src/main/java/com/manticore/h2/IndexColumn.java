/*
 * Copyright (C) 2020 Andreas Reichel<andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.manticore.h2;

import java.util.Objects;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class IndexColumn implements Comparable<IndexColumn> {

    Short ordinalPosition;
    String columnName;
    String ascOrDesc;
    Long cardinality;
    Long pages;
    String filterCondition;

    public IndexColumn(Short ordinalPosition, String columnName, String ascOrDesc, Long cardinality,
            Long pages, String filterCondition) {
        this.ordinalPosition = ordinalPosition;
        this.columnName = columnName;
        this.ascOrDesc = ascOrDesc;
        this.cardinality = cardinality;
        this.pages = pages;
        this.filterCondition = filterCondition;
    }

    @Override
    public int compareTo(IndexColumn o) {
        return ordinalPosition.compareTo(o.ordinalPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IndexColumn)) {
            return false;
        }

        IndexColumn that = (IndexColumn) o;

        if (!ordinalPosition.equals(that.ordinalPosition)) {
            return false;
        }
        if (!columnName.equals(that.columnName)) {
            return false;
        }
        if (!Objects.equals(ascOrDesc, that.ascOrDesc)) {
            return false;
        }
        if (!Objects.equals(cardinality, that.cardinality)) {
            return false;
        }
        if (!Objects.equals(pages, that.pages)) {
            return false;
        }
        return Objects.equals(filterCondition, that.filterCondition);
    }

    @Override
    public int hashCode() {
        int result = ordinalPosition.hashCode();
        result = 31 * result + columnName.hashCode();
        result = 31 * result + (ascOrDesc != null ? ascOrDesc.hashCode() : 0);
        result = 31 * result + (cardinality != null ? cardinality.hashCode() : 0);
        result = 31 * result + (pages != null ? pages.hashCode() : 0);
        result = 31 * result + (filterCondition != null ? filterCondition.hashCode() : 0);
        return result;
    }
}
