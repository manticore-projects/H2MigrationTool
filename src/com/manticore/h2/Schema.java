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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 *
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class Schema implements Comparable<Schema> {

  public static final Logger LOGGER = Logger.getLogger(Schema.class.getName());

  String tableSchema;
  String tableCatalog;

  TreeMap<String, Table> tables = new TreeMap<>();

  public Schema(String tableSchema, String tableCatalog) {
    this.tableSchema = tableSchema != null ? tableSchema : "";
    this.tableCatalog = tableCatalog != null ? tableCatalog : "";
  }

  public static Collection<Schema> getSchemas(DatabaseMetaData metaData) throws SQLException {
    ArrayList<Schema> schemas = new ArrayList<>();

    ResultSet rs = null;
    try {
      rs = metaData.getSchemas();
      while (rs.next()) {
        String tableSchema = rs.getString("TABLE_SCHEM"); // TABLE_SCHEM String => schema name
        String tableCatalog =
            rs.getString("TABLE_CATALOG"); // TABLE_CATALOG String => catalog name (may be null)
        Schema schema = new Schema(tableSchema, tableCatalog);

        schemas.add(schema);
      }
      if (schemas.isEmpty()) schemas.add(new Schema("", "."));

    } finally {
      try {
        if (rs != null && !rs.isClosed()) rs.close();
      } catch (Exception ex1) {

      }
    }
    return schemas;
  }

  public Table put(Table table) {
    return tables.put(table.tableName.toUpperCase(), table);
  }

  public Table get(String tableName) {
    return tables.get(tableName.toUpperCase());
  }

  @Override
  public int compareTo(Schema o) {
    int compareTo = tableCatalog.compareToIgnoreCase(o.tableCatalog);

    if (compareTo == 0) compareTo = tableSchema.compareToIgnoreCase(o.tableSchema);

    return compareTo;
  }
}
