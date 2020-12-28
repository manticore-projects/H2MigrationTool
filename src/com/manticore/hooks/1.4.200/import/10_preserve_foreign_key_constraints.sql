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

/**
 *
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */

SELECT 'ALTER TABLE '
--       || b.fktable_catalog
--       || '.'
       || b.fktable_schema
       || '.'
       || b.fktable_name
       || ' ADD FOREIGN KEY ('
       || b.fkcolumn_name
       || ') REFERENCES '
--       || b.pktable_catalog
--       || '.'
       || b.pktable_schema
       || '.'
       || b.pktable_name
       || '('
       || b.pkcolumn_name
       || ');' sql_text
FROM   information_schema.TABLE_CONSTRAINTS a
       INNER JOIN information_schema.CROSS_REFERENCES b
               ON a.table_catalog = b.fktable_catalog
                  AND a.table_schema = b.fktable_schema
                  AND a.constraint_name = b.fk_name
WHERE  constraint_type = 'FOREIGN KEY'
       AND ( constraint_catalog, constraint_schema, constraint_name ) IN (SELECT table_catalog
                                                                                 , table_schema
                                                                                 , constraint_name
                                                                          FROM   information_schema.INDEXES
                                                                          WHERE  is_generated); 