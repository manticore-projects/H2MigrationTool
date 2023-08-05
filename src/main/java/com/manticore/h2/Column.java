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
public class Column implements Comparable<Column> {

    String tableCatalog;
    String tableSchema;
    String tableName;
    String columnName;
    Integer dataType;
    String typeName;
    Integer columnSize;
    Integer decimalDigits;
    Integer numericPrecisionRadix;
    Integer nullable;
    String remarks;
    String columnDefinition;
    Integer characterOctetLength;
    Integer ordinalPosition;
    String isNullable;
    String scopeCatalog;
    String scopeSchema;
    String scopeTable;
    Short sourceDataType;
    String isAutomaticIncrement;
    String isGeneratedColumn;

    public Column(
            String tableCatalog,
            String tableSchema,
            String tableName,
            String columnName,
            Integer dataType,
            String typeName,
            Integer columnSize,
            Integer decimalDigits,
            Integer numericPrecisionRadix,
            Integer nullable,
            String remarks,
            String columnDefinition,
            Integer characterOctetLength,
            Integer ordinalPosition,
            String isNullable,
            String scopeCatalog,
            String scopeSchema,
            String scopeTable,
            Short sourceDataType,
            String isAutomaticIncrement,
            String isGeneratedColumn) {
        this.tableCatalog = tableCatalog;
        this.tableSchema = tableSchema;
        this.tableName = tableName;
        this.columnName = columnName;
        this.dataType = dataType;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.decimalDigits = decimalDigits;
        this.numericPrecisionRadix = numericPrecisionRadix;
        this.nullable = nullable;
        this.remarks = remarks;
        this.columnDefinition = columnDefinition;
        this.characterOctetLength = characterOctetLength;
        this.ordinalPosition = ordinalPosition;
        this.isNullable = isNullable;
        this.scopeCatalog = scopeCatalog;
        this.scopeSchema = scopeSchema;
        this.scopeTable = scopeTable;
        this.sourceDataType = sourceDataType;
        this.isAutomaticIncrement = isAutomaticIncrement;
        this.isGeneratedColumn = isGeneratedColumn;
    }

    @Override
    public int compareTo(Column o) {
        int compareTo =
                tableCatalog == null && o.tableCatalog == null
                        ? 0
                        : tableCatalog != null
                                ? tableCatalog.compareToIgnoreCase(o.tableCatalog)
                                : -o.tableCatalog.compareToIgnoreCase(tableCatalog);

        if (compareTo == 0) {
            compareTo =
                    tableSchema == null && o.tableSchema == null
                            ? 0
                            : tableSchema != null
                                    ? tableSchema.compareToIgnoreCase(o.tableSchema)
                                    : -o.tableSchema.compareToIgnoreCase(tableSchema);
        }

        if (compareTo == 0) {
            compareTo = tableName.compareToIgnoreCase(o.tableName);
        }

        if (compareTo == 0) {
            compareTo = ordinalPosition.compareTo(o.ordinalPosition);
        }

        return compareTo;
    }

    @Override
    public String toString() {
        return tableCatalog + "." + tableSchema + "." + tableName + "." + columnName + "\t"
                + typeName + " (" + columnSize + ", " + decimalDigits + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Column)) {
            return false;
        }

        Column column = (Column) o;

        if (!Objects.equals(tableCatalog, column.tableCatalog)) {
            return false;
        }
        if (!Objects.equals(tableSchema, column.tableSchema)) {
            return false;
        }
        if (!tableName.equals(column.tableName)) {
            return false;
        }
        if (!columnName.equals(column.columnName)) {
            return false;
        }
        if (!dataType.equals(column.dataType)) {
            return false;
        }
        if (!Objects.equals(typeName, column.typeName)) {
            return false;
        }
        if (!columnSize.equals(column.columnSize)) {
            return false;
        }
        if (!Objects.equals(decimalDigits, column.decimalDigits)) {
            return false;
        }
        if (!Objects.equals(numericPrecisionRadix, column.numericPrecisionRadix)) {
            return false;
        }
        if (!Objects.equals(nullable, column.nullable)) {
            return false;
        }
        if (!Objects.equals(remarks, column.remarks)) {
            return false;
        }
        if (!Objects.equals(columnDefinition, column.columnDefinition)) {
            return false;
        }
        if (!Objects.equals(characterOctetLength, column.characterOctetLength)) {
            return false;
        }
        if (!Objects.equals(ordinalPosition, column.ordinalPosition)) {
            return false;
        }
        if (!Objects.equals(isNullable, column.isNullable)) {
            return false;
        }
        if (!Objects.equals(scopeCatalog, column.scopeCatalog)) {
            return false;
        }
        if (!Objects.equals(scopeSchema, column.scopeSchema)) {
            return false;
        }
        if (!Objects.equals(scopeTable, column.scopeTable)) {
            return false;
        }
        if (!Objects.equals(sourceDataType, column.sourceDataType)) {
            return false;
        }
        if (!Objects.equals(isAutomaticIncrement, column.isAutomaticIncrement)) {
            return false;
        }
        return Objects.equals(isGeneratedColumn, column.isGeneratedColumn);
    }

    @Override
    public int hashCode() {
        int result = tableCatalog != null ? tableCatalog.hashCode() : 0;
        result = 31 * result + (tableSchema != null ? tableSchema.hashCode() : 0);
        result = 31 * result + tableName.hashCode();
        result = 31 * result + columnName.hashCode();
        result = 31 * result + dataType.hashCode();
        result = 31 * result + (typeName != null ? typeName.hashCode() : 0);
        result = 31 * result + columnSize.hashCode();
        result = 31 * result + (decimalDigits != null ? decimalDigits.hashCode() : 0);
        result = 31 * result
                + (numericPrecisionRadix != null ? numericPrecisionRadix.hashCode() : 0);
        result = 31 * result + (nullable != null ? nullable.hashCode() : 0);
        result = 31 * result + (remarks != null ? remarks.hashCode() : 0);
        result = 31 * result + (columnDefinition != null ? columnDefinition.hashCode() : 0);
        result = 31 * result + (characterOctetLength != null ? characterOctetLength.hashCode() : 0);
        result = 31 * result + (ordinalPosition != null ? ordinalPosition.hashCode() : 0);
        result = 31 * result + (isNullable != null ? isNullable.hashCode() : 0);
        result = 31 * result + (scopeCatalog != null ? scopeCatalog.hashCode() : 0);
        result = 31 * result + (scopeSchema != null ? scopeSchema.hashCode() : 0);
        result = 31 * result + (scopeTable != null ? scopeTable.hashCode() : 0);
        result = 31 * result + (sourceDataType != null ? sourceDataType.hashCode() : 0);
        result = 31 * result + (isAutomaticIncrement != null ? isAutomaticIncrement.hashCode() : 0);
        result = 31 * result + (isGeneratedColumn != null ? isGeneratedColumn.hashCode() : 0);
        return result;
    }
}
