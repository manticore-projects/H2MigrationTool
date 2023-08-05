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

import java.util.LinkedList;
import java.util.Objects;

/**
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reference)) {
            return false;
        }

        Reference reference = (Reference) o;

        if (!Objects.equals(pkTableCatalog, reference.pkTableCatalog)) {
            return false;
        }
        if (!Objects.equals(pkTableSchema, reference.pkTableSchema)) {
            return false;
        }
        if (!pkTableName.equals(reference.pkTableName)) {
            return false;
        }
        if (!Objects.equals(fkTableCatalog, reference.fkTableCatalog)) {
            return false;
        }
        if (!Objects.equals(fkTableSchema, reference.fkTableSchema)) {
            return false;
        }
        if (!fkTableName.equals(reference.fkTableName)) {
            return false;
        }
        if (!Objects.equals(updateRule, reference.updateRule)) {
            return false;
        }
        if (!Objects.equals(deleteRule, reference.deleteRule)) {
            return false;
        }
        if (!fkName.equals(reference.fkName)) {
            return false;
        }
        if (!pkName.equals(reference.pkName)) {
            return false;
        }
        if (!Objects.equals(deferrability, reference.deferrability)) {
            return false;
        }
        return Objects.equals(columns, reference.columns);
    }

    @Override
    public int hashCode() {
        int result = pkTableCatalog != null ? pkTableCatalog.hashCode() : 0;
        result = 31 * result + (pkTableSchema != null ? pkTableSchema.hashCode() : 0);
        result = 31 * result + pkTableName.hashCode();
        result = 31 * result + (fkTableCatalog != null ? fkTableCatalog.hashCode() : 0);
        result = 31 * result + (fkTableSchema != null ? fkTableSchema.hashCode() : 0);
        result = 31 * result + fkTableName.hashCode();
        result = 31 * result + (updateRule != null ? updateRule.hashCode() : 0);
        result = 31 * result + (deleteRule != null ? deleteRule.hashCode() : 0);
        result = 31 * result + fkName.hashCode();
        result = 31 * result + pkName.hashCode();
        result = 31 * result + (deferrability != null ? deferrability.hashCode() : 0);
        result = 31 * result + (columns != null ? columns.hashCode() : 0);
        return result;
    }
}
