SELECT 'ALTER TABLE '
       || b.fktable_catalog
       || '.'
       || b.fktable_schema
       || '.'
       || b.fktable_name
       || ' DROP CONSTRAINT '
       || b.fk_name
       || ';' sql_text
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