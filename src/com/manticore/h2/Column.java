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

/**
 *
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

    if (compareTo == 0)
      compareTo =
          tableSchema == null && o.tableSchema == null
              ? 0
              : tableSchema != null
                  ? tableSchema.compareToIgnoreCase(o.tableSchema)
                  : -o.tableSchema.compareToIgnoreCase(tableSchema);

    if (compareTo == 0) compareTo = tableName.compareToIgnoreCase(o.tableName);

    if (compareTo == 0) compareTo = ordinalPosition.compareTo(o.ordinalPosition);

    return compareTo;
  }
}
