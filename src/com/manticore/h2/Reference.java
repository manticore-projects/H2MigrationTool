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

import java.util.LinkedList;

/**
 *
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class Reference {

  String pkTableCatalog;
  String pkTableSchema;
  String pkTableName;
  String fkTableCatalog;
  String fkTableSchema;
  String fkTableName;
  Short updateRule;
  Short deleteRule;
  String fkName;
  String pkName;
  Short deferrability;

  LinkedList<String[]> columns = new LinkedList<>();

  public Reference(String pkTableCatalog, String pkTableSchema, String pkTableName,
                   String fkTableCatalog, String fkTableSchema, String fkTableName, Short updateRule,
                   Short deleteRule, String fkName, String pkName, Short deferrability) {
    this.pkTableCatalog = pkTableCatalog;
    this.pkTableSchema = pkTableSchema;
    this.pkTableName = pkTableName;
    this.fkTableCatalog = fkTableCatalog;
    this.fkTableSchema = fkTableSchema;
    this.fkTableName = fkTableName;
    this.updateRule = updateRule;
    this.deleteRule = deleteRule;
    this.fkName = fkName;
    this.pkName = pkName;
    this.deferrability = deferrability;
  }

}
