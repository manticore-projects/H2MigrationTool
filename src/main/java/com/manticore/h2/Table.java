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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class Table implements Comparable<Table> {
    public static final Logger LOGGER = Logger.getLogger(Table.class.getName());

    String tableCatalog;
    String tableSchema;
    String tableName;
    String tableType;
    String remarks;
    String typeCatalog;
    String typeSchema;
    String typeName;
    String selfReferenceColName;
    String referenceGeneration;

    TreeSet<Column> columns = new TreeSet<>();
    TreeMap<String, Index> indices = new TreeMap<>();
    PrimaryKey primaryKey = null;

    public Table(
            String tableCatalog,
            String tableSchema,
            String tableName,
            String tableType,
            String remarks,
            String typeCatalog,
            String typeSchema,
            String typeName,
            String selfReferenceColName,
            String referenceGeneration) {
        this.tableCatalog = tableCatalog;
        this.tableSchema = tableSchema;
        this.tableName = tableName;
        this.tableType = tableType;
        this.remarks = remarks;
        this.typeCatalog = typeCatalog;
        this.typeSchema = typeSchema;
        this.typeName = typeName;
        this.selfReferenceColName = selfReferenceColName;
        this.referenceGeneration = referenceGeneration;
    }

    public static Collection<Table> getTables(DatabaseMetaData metaData) throws SQLException {
        ArrayList<Table> tables = new ArrayList<>();
        ArrayList<String> tableTypes = new ArrayList<>();

        ResultSet rs = null;
        try {
            rs = metaData.getTableTypes();
            while (rs.next()) {
                tableTypes.add(rs.getString(1));
            }
            rs.close();

            rs = metaData.getTables(null, null, "%",
                    tableTypes.toArray(new String[tableTypes.size()]));
            while (rs.next()) {
                String tableCatalog =
                        rs.getString("TABLE_CAT"); // TABLE_CATALOG String => catalog name (may be
                                                   // null)
                String tableSchema = rs.getString("TABLE_SCHEM"); // TABLE_SCHEM String => schema
                                                                  // name
                String tableName = rs.getString("TABLE_NAME"); // TABLE_NAME String => table name
                String tableType =
                        rs.getString(
                                "TABLE_TYPE"); // TABLE_TYPE String => table type. Typical types are
                                               // "TABLE",
                // "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY",
                // "ALIAS", "SYNONYM".

                String remarks =
                        rs.getString(
                                "REMARKS"); // REMARKS String => explanatory comment on the table
                                            // (may be null)
                String typeCatalog =
                        rs.getString("TYPE_CAT"); // TYPE_CAT String => the types catalog (may be
                                                  // null)
                String typeSchema =
                        rs.getString("TYPE_SCHEM"); // TYPE_SCHEM String => the types schema (may be
                                                    // null)
                String typeName = rs.getString("TYPE_NAME"); // TYPE_NAME String => type name (may
                                                             // be null)
                String selfReferenceColName =
                        rs.getString(
                                "SELF_REFERENCING_COL_NAME"); // SELF_REFERENCING_COL_NAME String =>
                                                              // name of the
                // designated "identifier" column of a typed table
                // (may be null)
                String referenceGeneration =
                        rs.getString("REF_GENERATION"); // REF_GENERATION String => specifies how
                                                        // values in
                // SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM",
                // "USER", "DERIVED". (may be null)

                Table table =
                        new Table(
                                tableCatalog,
                                tableSchema,
                                tableName,
                                tableType,
                                remarks,
                                typeCatalog,
                                typeSchema,
                                typeName,
                                selfReferenceColName,
                                referenceGeneration);

                tables.add(table);
            }

        } finally {
            try {
                if (rs != null && !rs.isClosed())
                    rs.close();
            } catch (Exception ex1) {

            }
        }
        return tables;
    }

    public void getColumns(DatabaseMetaData metaData) throws SQLException {
        ResultSet rs = null;
        try {
            rs = metaData.getColumns(tableCatalog, tableSchema, tableName, "%");
            while (rs.next()) {
                String tableCatalog =
                        rs.getString("TABLE_CAT"); // TABLE_CATALOG String => catalog name (may be
                                                   // null)
                String tableSchema = rs.getString("TABLE_SCHEM"); // TABLE_SCHEM String => schema
                                                                  // name
                String tableName = rs.getString("TABLE_NAME"); // TABLE_NAME String => table name
                String columnName = rs.getString("COLUMN_NAME"); // COLUMN_NAME String => column
                                                                 // name
                Integer dataType = rs.getInt("DATA_TYPE"); // DATA_TYPE int => SQL type from
                                                           // java.sql.Types
                String typeName =
                        rs.getString(
                                "TYPE_NAME"); // TYPE_NAME String => Data source dependent type
                                              // name, for a UDT the
                // type name is fully qualified
                Integer columnSize = rs.getInt("COLUMN_SIZE"); // COLUMN_SIZE int => column size.
                Integer decimalDigits =
                        rs.getInt(
                                "DECIMAL_DIGITS"); // DECIMAL_DIGITS int => the number of fractional
                                                   // digits. Null is
                // returned for data types where DECIMAL_DIGITS is not
                // applicable.
                Integer numericPrecicionRadix =
                        rs.getInt("NUM_PREC_RADIX"); // NUM_PREC_RADIX int => Radix (typically
                                                     // either 10 or 2)
                Integer nullable = rs.getInt("NULLABLE"); // NULLABLE int => is NULL allowed.
                String remarks =
                        rs.getString("REMARKS"); // REMARKS String => comment describing column (may
                                                 // be null)
                String columnDefinition =
                        rs.getString(
                                "COLUMN_DEF"); // COLUMN_DEF String => default value for the column,
                                               // which should be
                // interpreted as a string when the value is enclosed in single
                // quotes (may be null)
                Integer characterOctetLength =
                        rs.getInt(
                                "CHAR_OCTET_LENGTH"); // CHAR_OCTET_LENGTH int => for char types the
                                                      // maximum number
                // of bytes in the column
                Integer ordinalPosition =
                        rs.getInt(
                                "ORDINAL_POSITION"); // ORDINAL_POSITION int => index of column in
                                                     // table (starting
                // at 1)
                String isNullable =
                        rs.getString(
                                "IS_NULLABLE"); // IS_NULLABLE String => ISO rules are used to
                                                // determine the
                // nullability for a column.
                String scopeCatalog =
                        rs.getString(
                                "SCOPE_CATALOG"); // SCOPE_CATALOG String => catalog of table that
                                                  // is the scope of a
                // reference attribute (null if DATA_TYPE isn't REF)
                String scopeSchema =
                        rs.getString(
                                "SCOPE_SCHEMA"); // SCOPE_SCHEMA String => schema of table that is
                                                 // the scope of a
                // reference attribute (null if the DATA_TYPE isn't REF)
                String scopeTable =
                        rs.getString(
                                "SCOPE_TABLE"); // SCOPE_TABLE String => table name that this the
                                                // scope of a
                // reference attribute (null if the DATA_TYPE isn't REF)
                Short sourceDataType =
                        rs.getShort(
                                "SOURCE_DATA_TYPE"); // SOURCE_DATA_TYPE short => source type of a
                                                     // distinct type or
                // user-generated Ref type, SQL type from java.sql.Types (null
                // if DATA_TYPE isn't DISTINCT or user-generated REF)
                String isAutoIncrement =
                        rs.getString(
                                "IS_AUTOINCREMENT"); // IS_AUTOINCREMENT String => Indicates whether
                                                     // this column is
                // auto incremented

                String isGeneratedColumn =
                        rs.getString(
                                "IS_GENERATEDCOLUMN"); // IS_GENERATEDCOLUMN String => Indicates
                                                       // whether this is a
                // generated column

                Column column =
                        new Column(
                                tableCatalog,
                                tableSchema,
                                tableName,
                                columnName,
                                dataType,
                                typeName,
                                columnSize,
                                decimalDigits,
                                numericPrecicionRadix,
                                nullable,
                                remarks,
                                columnDefinition,
                                characterOctetLength,
                                ordinalPosition,
                                isNullable,
                                scopeCatalog,
                                scopeSchema,
                                scopeTable,
                                sourceDataType,
                                isAutoIncrement,
                                isGeneratedColumn);

                columns.add(column);
            }

        } finally {
            try {
                if (rs != null && !rs.isClosed())
                    rs.close();
            } catch (Exception ex1) {

            }
        }
    }

    public void getIndices(DatabaseMetaData metaData, boolean approximate) throws SQLException {
        ResultSet rs = null;
        try {
            LOGGER.info(tableCatalog + "." + tableSchema + "." + tableName);
            rs = metaData.getIndexInfo(tableCatalog, tableSchema, tableName, false, approximate);
            while (rs.next()) {
                String tableCatalog =
                        rs.getString("TABLE_CAT"); // TABLE_CATALOG String => catalog name (may be
                                                   // null)
                String tableSchema = rs.getString("TABLE_SCHEM"); // TABLE_SCHEM String => schema
                                                                  // name
                String tableName = rs.getString("TABLE_NAME"); // TABLE_NAME String => table name
                Boolean nonUnique =
                        rs.getBoolean(
                                "NON_UNIQUE"); // NON_UNIQUE boolean => Can index values be
                                               // non-unique. false when
                // TYPE is tableIndexStatistic
                String indexQualifier =
                        rs.getString(
                                "INDEX_QUALIFIER"); // INDEX_QUALIFIER String => index catalog (may
                                                    // be null); null
                // when TYPE is tableIndexStatistic
                String indexName =
                        rs.getString("INDEX_NAME"); // INDEX_NAME String => index name; null when
                                                    // TYPE is
                // tableIndexStatistic

                Short type = rs.getShort("TYPE"); // TYPE short => index type:

                Short ordinalPosition =
                        rs.getShort(
                                "ORDINAL_POSITION"); // ORDINAL_POSITION short => column sequence
                                                     // number within
                // index; zero when TYPE is tableIndexStatistic

                String columnName =
                        rs.getString("COLUMN_NAME"); // COLUMN_NAME String => column name; null when
                                                     // TYPE is
                // tableIndexStatistic

                String ascOrDesc =
                        rs.getString(
                                "ASC_OR_DESC"); // ASC_OR_DESC String => column sort sequence, "A"
                                                // => ascending, "D"
                // => descending, may be null if sort sequence is not supported;
                // null when TYPE is tableIndexStatistic

                Long cardinality =
                        rs.getLong(
                                "CARDINALITY"); // CARDINALITY long => When TYPE is
                                                // tableIndexStatistic, then this
                // is the number of rows in the table; otherwise, it is the number
                // of unique values in the index.

                Long pages =
                        rs.getLong(
                                "PAGES"); // PAGES long => When TYPE is tableIndexStatistic then
                                          // this is the number
                // of pages used for the table, otherwise it is the number of pages used
                // for the current index.

                String filterCondition =
                        rs.getString(
                                "FILTER_CONDITION"); // FILTER_CONDITION String => Filter condition,
                                                     // if any. (may be
                // null)

                Index index = indices.get(indexName.toUpperCase());
                if (index == null) {
                    index =
                            new Index(
                                    tableCatalog, tableSchema, tableName, nonUnique, indexQualifier,
                                    indexName, type);
                    indices.put(index.indexName, index);
                }
                index.put(ordinalPosition, columnName, ascOrDesc, cardinality, pages,
                        filterCondition);
            }

        } finally {
            try {
                if (rs != null && !rs.isClosed())
                    rs.close();
            } catch (Exception ex1) {

            }
        }
    }

    public void getPrimaryKey(DatabaseMetaData metaData) throws SQLException {
        ResultSet rs = null;
        try {
            rs = metaData.getPrimaryKeys(tableCatalog, tableSchema, tableName);

            TreeMap<Short, String> columnNames = new TreeMap<>();

            while (rs.next()) {
                String tableCatalog =
                        rs.getString("TABLE_CAT"); // TABLE_CATALOG String => catalog name (may be
                                                   // null)
                String tableSchema = rs.getString("TABLE_SCHEM"); // TABLE_SCHEM String => schema
                                                                  // name
                String tableName = rs.getString("TABLE_NAME"); // TABLE_NAME String => table name
                String columnName = rs.getString("COLUMN_NAME"); // COLUMN_NAME String => column
                                                                 // name
                Short keySequence =
                        rs.getShort(
                                "KEY_SEQ"); // KEY_SEQ short => sequence number within primary key(
                                            // a value of 1
                // represents the first column of the primary key, a value of 2 would
                // represent the second column within the primary key).

                String primaryKeyName =
                        rs.getString("PK_NAME"); // PK_NAME String => primary key name (may be null)

                if (primaryKey == null)
                    primaryKey =
                            new PrimaryKey(tableCatalog, tableSchema, tableName, primaryKeyName);

                columnNames.put(keySequence, columnName);
            }

            for (Entry<Short, String> e : columnNames.entrySet())
                primaryKey.columnNames.add(e.getValue());

        } finally {
            try {
                if (rs != null && !rs.isClosed())
                    rs.close();
            } catch (Exception ex1) {

            }
        }
    }

    @Override
    public int compareTo(Table o) {
        int compareTo = tableCatalog.compareToIgnoreCase(o.tableCatalog);

        if (compareTo == 0)
            compareTo = tableSchema.compareToIgnoreCase(o.tableSchema);

        if (compareTo == 0)
            compareTo = tableName.compareToIgnoreCase(o.tableName);

        return compareTo;
    }

    public boolean add(Column column) {
        return columns.add(column);
    }

    public boolean contains(Column column) {
        return columns.contains(column);
    }

    public Index put(Index index) {
        return indices.put(index.indexName.toUpperCase(), index);
    }

    public boolean containsIndexKey(String indexName) {
        return indices.containsKey(indexName.toUpperCase());
    }

    public Index get(String indexName) {
        return indices.get(indexName.toUpperCase());
    }
}
