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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Andreas Reichel<andreas@manticore-projects.com>
 */
public class MetaDataTools {

    public static final Logger LOGGER = Logger.getLogger(MetaDataTools.class.getName());
    public static final String H2_VERSION = "2.0.201";
    public static final String DB_FILE_URI_STR =
            "file:/home/are/Downloads/cmb/.manticore/ifrsbox_202101";

    public static Collection<Recommendation> verifyDecimalPrecision(Connection con)
            throws SQLException {
        ArrayList<Recommendation> recommendations = new ArrayList<>();

        MetaData meta = new MetaData(con);
        meta.build();

        for (Catalog cat : meta.getCatalogs().values()) {
            for (Schema schema : cat.schemas.values()) {
                for (Table table : schema.tables.values()) {
                    for (Column column : table.columns) {
                        if (Set.of(java.sql.Types.DECIMAL, java.sql.Types.NUMERIC)
                                .contains(column.dataType)
                                && (column.columnSize > 128 || column.decimalDigits > 128)) {
                            LOGGER.warning("Found suspicious column: " + column);

                            int precision = 0;
                            int scale = 0;
                            String sqlStr =
                                    "SELECT \"" + column.columnName + "\" FROM \""
                                            + column.tableSchema + "\".\"" + column.tableName
                                            + "\"";
                            try (Statement st = con.createStatement();
                                    ResultSet rs = st.executeQuery(sqlStr)) {
                                while (rs.next()) {
                                    BigDecimal d = rs.getBigDecimal(1);

                                    // "23.456" --> DECIMAL(5,3)
                                    precision = Math.max(precision, d.precision());
                                    scale = Math.max(scale, d.scale());
                                }

                                LOGGER.fine(
                                        "Suggest: " + column.columnName + "\t" + column.typeName
                                                + "(" + precision + ", " + scale + ")");

                                String issue =
                                        "Invalid Decimal Precision/Scale: " + column.tableSchema
                                                + "." + column.tableName + "." + column.columnName
                                                + "    " + column.typeName + " ("
                                                + column.columnSize + "." + column.decimalDigits
                                                + ")";

                                String alterStatementStr = "ALTER TABLE " + column.tableSchema + "."
                                        + column.tableName + "\n" +
                                        "MODIFY COLUMN " + column.columnName + " " + column.typeName
                                        + "(" + precision + "," + scale + ");\n";

                                Recommendation recommendation = new Recommendation(
                                        issue,
                                        alterStatementStr);

                                recommendations.add(recommendation);
                            }
                        }
                    }
                }
            }
        }

        return recommendations;
    }
}
