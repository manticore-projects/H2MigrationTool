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
import java.util.TreeMap;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class Index {

    String tableCatalog;
    String tableSchema;
    String tableName;
    Boolean nonUnique;
    String indexQualifier;
    String indexName;
    Short type;

    TreeMap<Short, IndexColumn> columns = new TreeMap<>();

    public Index(String tableCatalog, String tableSchema, String tableName, Boolean nonUnique,
            String indexQualifier,
            String indexName, Short type) {
        this.tableCatalog = tableCatalog;
        this.tableSchema = tableSchema;
        this.tableName = tableName;
        this.nonUnique = nonUnique;
        this.indexQualifier = indexQualifier;
        this.indexName = indexName;
        this.type = type;
    }

    public IndexColumn put(Short ordinalPosition, String columnName, String ascOrDesc,
            Long cardinality,
            Long pages, String filterCondition) {
        IndexColumn column = new IndexColumn(ordinalPosition, columnName, ascOrDesc, cardinality,
                pages, filterCondition);
        return columns.put(ordinalPosition, column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Index)) {
            return false;
        }

        Index index = (Index) o;

        if (!Objects.equals(tableCatalog, index.tableCatalog)) {
            return false;
        }
        if (!Objects.equals(tableSchema, index.tableSchema)) {
            return false;
        }
        if (!tableName.equals(index.tableName)) {
            return false;
        }
        if (!nonUnique.equals(index.nonUnique)) {
            return false;
        }
        if (!indexQualifier.equals(index.indexQualifier)) {
            return false;
        }
        if (!indexName.equals(index.indexName)) {
            return false;
        }
        if (!type.equals(index.type)) {
            return false;
        }
        return columns.equals(index.columns);
    }

    @Override
    public int hashCode() {
        int result = tableCatalog != null ? tableCatalog.hashCode() : 0;
        result = 31 * result + (tableSchema != null ? tableSchema.hashCode() : 0);
        result = 31 * result + tableName.hashCode();
        result = 31 * result + nonUnique.hashCode();
        result = 31 * result + indexQualifier.hashCode();
        result = 31 * result + indexName.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + columns.hashCode();
        return result;
    }
}
