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
public class Catalog implements Comparable<Catalog>{
  public static final Logger LOGGER = Logger.getLogger(Catalog.class.getName());
  
  String tableCatalog;
  String catalogSeparator;
  
  TreeMap<String, Schema> schemas = new TreeMap<>();
  
  public Catalog(String tableCatalog, String catalogSeparator) {
    this.tableCatalog = tableCatalog!=null ? tableCatalog : "";
    this.catalogSeparator = catalogSeparator!=null ? catalogSeparator : ".";
  }
  
  public static Collection<Catalog> getCatalogs(DatabaseMetaData metaData) throws SQLException {
    ArrayList<Catalog> catalogs = new ArrayList<>();
    
    ResultSet rs = null;
    try {
      rs = metaData.getCatalogs();
      String catalogSeparator = metaData.getCatalogSeparator();
      while (rs.next()) {
        String tableCatalog = rs.getString("TABLE_CAT");
        Catalog catalog=new Catalog(tableCatalog, catalogSeparator);
        
        catalogs.add(catalog);
      }
      if (catalogs.isEmpty())
        catalogs.add(new Catalog("", "."));
      
    } finally {
      try {
        if (rs!=null && !rs.isClosed())
          rs.close();
        } catch (Exception ex1) {
          
        }
    }
    return catalogs;
  }
  
  public Schema put(Schema schema) {
    return schemas.put(schema.tableSchema.toUpperCase(), schema);
  }
  
  public Schema get(String tableSchema) {
    return schemas.get(tableSchema.toUpperCase());
  }

  @Override
  public int compareTo(Catalog o) {
    return tableCatalog.compareToIgnoreCase(o.tableCatalog);
  }
}
