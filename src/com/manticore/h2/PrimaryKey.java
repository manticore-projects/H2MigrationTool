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
public class PrimaryKey {

  String tableCatalog;
  String tableSchema;
  String tableName;
  String primaryKeyName;

  LinkedList<String> columnNames = new LinkedList<>();

  public PrimaryKey(String tableCatalog, String tableSchema, String tableName, String primaryKeyName) {
    this.tableCatalog = tableCatalog;
    this.tableSchema = tableSchema;
    this.tableName = tableName;
    this.primaryKeyName = primaryKeyName;
  }
}
