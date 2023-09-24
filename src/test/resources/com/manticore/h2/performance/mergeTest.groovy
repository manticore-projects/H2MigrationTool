def String[] sqlStr = [
    "CREATE TABLE IF NOT EXISTS A (number VARCHAR(128) not NULL, intent INT not NULL, objID VARCHAR(30) not NULL, objType SMALLINT  not NULL, PRIMARY KEY (number,intent,objID,objType));",
    "CREATE INDEX IF NOT EXISTS A_IDX ON A(objType,objID,intent);",
    "CREATE INDEX IF NOT EXISTS A_type_IDX ON A (objType);",
    "DROP TABLE IF EXISTS B; ",
    "CREATE TABLE IF NOT EXISTS B (number VARCHAR(128) not NULL, intent INT not NULL, objID VARCHAR(30) not NULL, objType SMALLINT  not NULL, PRIMARY KEY (number,intent,objID,objType));",
    "CREATE INDEX IF NOT EXISTS B_IDX ON B(objType,objID,intent);",
    "DELETE FROM A T WHERE EXISTS (SELECT NULL from A S WHERE T.objID=S.objID AND T.objType=S.objType AND T.intent=S.intent AND T.number<>S.number);",
    "MERGE INTO A T USING (SELECT * FROM B) AS S ON T.objID=S.objID AND T.objType=S.objType AND T.intent=S.intent AND T.number=S.number WHEN NOT MATCHED THEN INSERT (objID, objType, number, intent) VALUES (S.objID, S.objType, S.number, S.intent);",
    "DROP TABLE B CASCADE"
]

statement.execute(sqlStr[0]);
statement.execute(sqlStr[1]);
statement.execute(sqlStr[2]);

for (int loop = 0, number = 0; loop < 300; ++loop) {
    statement.execute(sqlStr[3]);
    statement.execute(sqlStr[4]);
    statement.execute(sqlStr[5]);

    for (int i = 0; i < 100; ++i) {
        ++number;
        statement.execute("MERGE INTO B (number,intent,objID,objType) VALUES ('"
                + number
                + "',1, '"
                + number
                + "', 1);");
    }
    // ANALYZE after massive DML
    //statement.execute("Analyze;");

    statement.execute(sqlStr[6]);
    statement.execute(sqlStr[7]);
    statement.execute(sqlStr[8]);

    logger.fine "loop = $loop"
}

