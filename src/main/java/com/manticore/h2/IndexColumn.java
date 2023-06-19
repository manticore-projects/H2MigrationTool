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

/**
 *
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
}
