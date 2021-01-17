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

import java.sql.Driver;
import java.sql.Connection;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andreas Reichel<andreas@manticore-projects.com>
 *
 */

public class MetaDataTools {

  public static final Logger LOGGER = Logger.getLogger(MetaDataTools.class.getName());
  public static final String H2_VERSION = "2.0.201";
  public static final String DB_FILE_URI_STR = "file:/home/are/Downloads/cmb/.manticore/ifrsbox_202101";

  public static void main(String[] args) {
    try {
      Properties properties = new Properties();
      properties.setProperty("user", "sa");
      properties.setProperty("password", "");

      Driver driver = H2MigrationTool.loadDriver("", H2_VERSION);

      try (Connection con = driver.connect("jdbc:h2:" + DB_FILE_URI_STR, properties);) {
        MetaData meta = new MetaData(con);
        meta.build();

        for (Catalog cat : meta.catalogs.values())
          for (Schema schema : cat.schemas.values())
            for (Table table : schema.tables.values())
              for (Column column : table.columns)
                if ( Set.of( java.sql.Types.DECIMAL,  java.sql.Types.NUMERIC).contains(column.dataType) && (column.columnSize > 128 || column.decimalDigits > 128))
                  LOGGER.warning("Found suspicious column: " + column.toString());

      }
    } catch (Exception ex) {
      Logger.getLogger(MetaDataTools.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
