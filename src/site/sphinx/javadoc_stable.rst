
.. raw:: html

    <div id="floating-toc">
        <div class="search-container">
            <input type="button" id="toc-hide-show-btn"></input>
            <input type="text" id="toc-search" placeholder="Search" />
        </div>
        <ul id="toc-list"></ul>
    </div>



#######################################################################
API 1.7
#######################################################################

Base Package: com.manticore


..  _com.manticore:
***********************************************************************
Base
***********************************************************************

..  _com.manticore.Recovery:

=======================================================================
Recovery
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **Recovery** ()


| **main** (args)
|          :ref:`String<java.lang.String>` args



..  _com.manticore.h2:
***********************************************************************
h2
***********************************************************************

..  _com.manticore.h2.Recommendation.Type

=======================================================================
Recommendation.Type
=======================================================================

[DECIMAL_PRECISION]


..  _com.manticore.h2.Catalog:

=======================================================================
Catalog
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **Catalog** (tableCatalog, catalogSeparator)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` catalogSeparator


| **getCatalogs** (metaData) → :ref:`Collection<java.util.Collection>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`Collection<java.util.Collection>`



| **put** (schema) → :ref:`Schema<com.manticore.h2.Schema>`
|          :ref:`Schema<com.manticore.h2.Schema>` schema
|          returns :ref:`Schema<com.manticore.h2.Schema>`



| **get** (tableSchema) → :ref:`Schema<com.manticore.h2.Schema>`
|          :ref:`String<java.lang.String>` tableSchema
|          returns :ref:`Schema<com.manticore.h2.Schema>`



| *@Override*
| **compareTo** (o) → int
|          :ref:`Catalog<com.manticore.h2.Catalog>` o
|          returns int



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _com.manticore.h2.Column:

=======================================================================
Column
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **Column** (tableCatalog, tableSchema, tableName, columnName, dataType, typeName, columnSize, decimalDigits, numericPrecisionRadix, nullable, remarks, columnDefinition, characterOctetLength, ordinalPosition, isNullable, scopeCatalog, scopeSchema, scopeTable, sourceDataType, isAutomaticIncrement, isGeneratedColumn)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` columnName
|          :ref:`Integer<java.lang.Integer>` dataType
|          :ref:`String<java.lang.String>` typeName
|          :ref:`Integer<java.lang.Integer>` columnSize
|          :ref:`Integer<java.lang.Integer>` decimalDigits
|          :ref:`Integer<java.lang.Integer>` numericPrecisionRadix
|          :ref:`Integer<java.lang.Integer>` nullable
|          :ref:`String<java.lang.String>` remarks
|          :ref:`String<java.lang.String>` columnDefinition
|          :ref:`Integer<java.lang.Integer>` characterOctetLength
|          :ref:`Integer<java.lang.Integer>` ordinalPosition
|          :ref:`String<java.lang.String>` isNullable
|          :ref:`String<java.lang.String>` scopeCatalog
|          :ref:`String<java.lang.String>` scopeSchema
|          :ref:`String<java.lang.String>` scopeTable
|          :ref:`Short<java.lang.Short>` sourceDataType
|          :ref:`String<java.lang.String>` isAutomaticIncrement
|          :ref:`String<java.lang.String>` isGeneratedColumn


| *@Override*
| **compareTo** (o) → int
|          :ref:`Column<com.manticore.h2.Column>` o
|          returns int



| *@Override*
| **toString** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _com.manticore.h2.DriverRecord:

=======================================================================
DriverRecord
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **DriverRecord** (majorVersion, minorVersion, patchID, buildId, url)
|          int majorVersion
|          int minorVersion
|          int patchID
|          :ref:`String<java.lang.String>` buildId
|          :ref:`URL<java.net.URL>` url


| *@Override*
| **compareTo** (t) → int
|          :ref:`DriverRecord<com.manticore.h2.DriverRecord>` t
|          returns int



| *@Override*
| **hashCode** () → int
|          returns int



| *@Override*
| **equals** (obj) → boolean
|          :ref:`Object<java.lang.Object>` obj
|          returns boolean



| **getVersion** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| *@Override*
| **toString** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`




..  _com.manticore.h2.ErrorDialog:

=======================================================================
ErrorDialog
=======================================================================

*extends:* :ref:`JDialog<javax.swing.JDialog>` 

| **ErrorDialog** (owner, exception)
|          :ref:`Dialog<java.awt.Dialog>` owner
|          :ref:`Exception<java.lang.Exception>` exception


| **ErrorDialog** (owner, exception)
|          :ref:`Frame<java.awt.Frame>` owner
|          :ref:`Exception<java.lang.Exception>` exception


| **ErrorDialog** (owner, exception)
|          :ref:`Window<java.awt.Window>` owner
|          :ref:`Exception<java.lang.Exception>` exception


| **show** (owner, exception)
|          :ref:`Dialog<java.awt.Dialog>` owner
|          :ref:`Exception<java.lang.Exception>` exception


| **show** (owner, exception)
|          :ref:`Frame<java.awt.Frame>` owner
|          :ref:`Exception<java.lang.Exception>` exception


| **show** (owner, exception)
|          :ref:`Window<java.awt.Window>` owner
|          :ref:`Exception<java.lang.Exception>` exception


| **show** (component, exception)
|          :ref:`Component<java.awt.Component>` component
|          :ref:`Exception<java.lang.Exception>` exception



..  _com.manticore.h2.H2MigrationTool:

=======================================================================
H2MigrationTool
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **H2MigrationTool** ()


| **getDriverRecords** () → :ref:`Set<java.util.Set>`
|          returns :ref:`Set<java.util.Set>`



| **getTempFolderName** () → :ref:`String<java.lang.String>`
|          returns :ref:`String<java.lang.String>`



| **getAbsoluteFile** (filename) → :ref:`File<java.io.File>`
|          :ref:`String<java.lang.String>` filename
|          returns :ref:`File<java.io.File>`



| **getAbsoluteFileName** (filename) → :ref:`String<java.lang.String>`
|          :ref:`String<java.lang.String>` filename
|          returns :ref:`String<java.lang.String>`



| **findFilesInPathRecursively** (parentPath, depth, prefix, suffix) → :ref:`Collection<java.util.Collection>`
|          :ref:`Path<java.nio.file.Path>` parentPath
|          int depth
|          :ref:`String<java.lang.String>` prefix
|          :ref:`String<java.lang.String>` suffix
|          returns :ref:`Collection<java.util.Collection>`



| **findFilesInPathRecursively** (parentPath, depth, fileFilters) → :ref:`Collection<java.util.Collection>`
|          :ref:`Path<java.nio.file.Path>` parentPath
|          int depth
|          :ref:`FileFilter<java.io.FileFilter>` fileFilters
|          returns :ref:`Collection<java.util.Collection>`



| **findH2Drivers** (pathName) → :ref:`Collection<java.util.Collection>`
|          :ref:`String<java.lang.String>` pathName
|          returns :ref:`Collection<java.util.Collection>`



| **findH2Databases** (pathName, fileFilters) → :ref:`Collection<java.util.Collection>`
|          :ref:`String<java.lang.String>` pathName
|          :ref:`FileFilter<java.io.FileFilter>` fileFilters
|          returns :ref:`Collection<java.util.Collection>`



| **readDriverRecords** () → :ref:`TreeSet<java.util.TreeSet>`
|          returns :ref:`TreeSet<java.util.TreeSet>`



| **readDriverRecords** (resourceName) → :ref:`TreeSet<java.util.TreeSet>`
|          :ref:`String<java.lang.String>` resourceName
|          returns :ref:`TreeSet<java.util.TreeSet>`



| **readDriverRecord** (path)
|          :ref:`Path<java.nio.file.Path>` path


| **readDriverRecord** (url)
|          :ref:`URL<java.net.URL>` url


| **loadDriver** (version) → :ref:`Driver<java.sql.Driver>`
|          :ref:`String<java.lang.String>` version
|          returns :ref:`Driver<java.sql.Driver>`



| **loadDriver** (resourceStr, version) → :ref:`Driver<java.sql.Driver>`
|          :ref:`String<java.lang.String>` resourceStr
|          :ref:`String<java.lang.String>` version
|          returns :ref:`Driver<java.sql.Driver>`



| **loadDriver** (driverRecords, version) → :ref:`Driver<java.sql.Driver>`
|          :ref:`TreeSet<java.util.TreeSet>` driverRecords
|          :ref:`String<java.lang.String>` version
|          returns :ref:`Driver<java.sql.Driver>`



| **loadDriver** (driverRecord) → :ref:`Driver<java.sql.Driver>`
|          :ref:`DriverRecord<com.manticore.h2.DriverRecord>` driverRecord
|          returns :ref:`Driver<java.sql.Driver>`



| **unloadDriver** (driver)
|          :ref:`Driver<java.sql.Driver>` driver


| **getDriverRecord** (driverRecords, majorVersion, minorVersion, patchId, buildID) → :ref:`DriverRecord<com.manticore.h2.DriverRecord>`
|          :ref:`Set<java.util.Set>` driverRecords
|          int majorVersion
|          int minorVersion
|          int patchId
|          :ref:`String<java.lang.String>` buildID
|          returns :ref:`DriverRecord<com.manticore.h2.DriverRecord>`



| **getDriverRecord** (driverRecords, majorVersion, minorVersion) → :ref:`DriverRecord<com.manticore.h2.DriverRecord>`
|          :ref:`Set<java.util.Set>` driverRecords
|          int majorVersion
|          int minorVersion
|          returns :ref:`DriverRecord<com.manticore.h2.DriverRecord>`



| **getDriverRecord** (driverRecords, version) → :ref:`DriverRecord<com.manticore.h2.DriverRecord>`
|          :ref:`Set<java.util.Set>` driverRecords
|          :ref:`String<java.lang.String>` version
|          returns :ref:`DriverRecord<com.manticore.h2.DriverRecord>`



| **main** (args)
|          :ref:`String<java.lang.String>` args


| **getDriverRecord** (version) → :ref:`DriverRecord<com.manticore.h2.DriverRecord>`
|          :ref:`String<java.lang.String>` version
|          returns :ref:`DriverRecord<com.manticore.h2.DriverRecord>`



| **writeScript** (driverRecord, databaseFileName, user, password, scriptFileName, options, connectionParameters) → :ref:`ScriptResult<com.manticore.h2.H2MigrationTool.ScriptResult>`
|          :ref:`DriverRecord<com.manticore.h2.DriverRecord>` driverRecord
|          :ref:`String<java.lang.String>` databaseFileName
|          :ref:`String<java.lang.String>` user
|          :ref:`String<java.lang.String>` password
|          :ref:`String<java.lang.String>` scriptFileName
|          :ref:`String<java.lang.String>` options
|          :ref:`String<java.lang.String>` connectionParameters
|          returns :ref:`ScriptResult<com.manticore.h2.H2MigrationTool.ScriptResult>`



| **writeRecoveryScript** (driverRecord, folderName, databaseFileName) → :ref:`ScriptResult<com.manticore.h2.H2MigrationTool.ScriptResult>`
|          :ref:`DriverRecord<com.manticore.h2.DriverRecord>` driverRecord
|          :ref:`String<java.lang.String>` folderName
|          :ref:`String<java.lang.String>` databaseFileName
|          returns :ref:`ScriptResult<com.manticore.h2.H2MigrationTool.ScriptResult>`



| **migrate** (versionFrom, versionTo, databaseFileName, user, password, scriptFileName, compression, upgradeOptions, overwrite, force, connectionParameters) → :ref:`ScriptResult<com.manticore.h2.H2MigrationTool.ScriptResult>`
|          :ref:`String<java.lang.String>` versionFrom
|          :ref:`String<java.lang.String>` versionTo
|          :ref:`String<java.lang.String>` databaseFileName
|          :ref:`String<java.lang.String>` user
|          :ref:`String<java.lang.String>` password
|          :ref:`String<java.lang.String>` scriptFileName
|          :ref:`String<java.lang.String>` compression
|          :ref:`String<java.lang.String>` upgradeOptions
|          boolean overwrite
|          boolean force
|          :ref:`String<java.lang.String>` connectionParameters
|          returns :ref:`ScriptResult<com.manticore.h2.H2MigrationTool.ScriptResult>`



| **migrateAuto** (databaseFileName)
|          :ref:`String<java.lang.String>` databaseFileName


| **migrateAuto** (versionTo, databaseFileName, user, password, scriptFileName, compression, upgradeOptions, overwrite, force)
|          :ref:`String<java.lang.String>` versionTo
|          :ref:`String<java.lang.String>` databaseFileName
|          :ref:`String<java.lang.String>` user
|          :ref:`String<java.lang.String>` password
|          :ref:`String<java.lang.String>` scriptFileName
|          :ref:`String<java.lang.String>` compression
|          :ref:`String<java.lang.String>` upgradeOptions
|          boolean overwrite
|          boolean force



..  _com.manticore.h2.H2MigrationTool.ScriptResult:

=======================================================================
H2MigrationTool.ScriptResult
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **ScriptResult** (scriptFileName, commands)
|          :ref:`String<java.lang.String>` scriptFileName
|          :ref:`List<java.util.List>` commands



..  _com.manticore.h2.H2MigrationUI:

=======================================================================
H2MigrationUI
=======================================================================

*extends:* :ref:`JFrame<javax.swing.JFrame>` 

| **H2MigrationUI** ()


| **executeAndWait** (worker, component, textArea)
|          :ref:`SwingWorker<javax.swing.SwingWorker>` worker
|          :ref:`Component<java.awt.Component>` component
|          :ref:`JTextArea<javax.swing.JTextArea>` textArea


| **executeAndWait** (worker, component)
|          :ref:`SwingWorker<javax.swing.SwingWorker>` worker
|          :ref:`Component<java.awt.Component>` component


| **buildUI** (visible)
|          boolean visible



..  _com.manticore.h2.Index:

=======================================================================
Index
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **Index** (tableCatalog, tableSchema, tableName, nonUnique, indexQualifier, indexName, type)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`Boolean<java.lang.Boolean>` nonUnique
|          :ref:`String<java.lang.String>` indexQualifier
|          :ref:`String<java.lang.String>` indexName
|          :ref:`Short<java.lang.Short>` type


| **put** (ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition) → :ref:`IndexColumn<com.manticore.h2.IndexColumn>`
|          :ref:`Short<java.lang.Short>` ordinalPosition
|          :ref:`String<java.lang.String>` columnName
|          :ref:`String<java.lang.String>` ascOrDesc
|          :ref:`Long<java.lang.Long>` cardinality
|          :ref:`Long<java.lang.Long>` pages
|          :ref:`String<java.lang.String>` filterCondition
|          returns :ref:`IndexColumn<com.manticore.h2.IndexColumn>`



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _com.manticore.h2.IndexColumn:

=======================================================================
IndexColumn
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **IndexColumn** (ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition)
|          :ref:`Short<java.lang.Short>` ordinalPosition
|          :ref:`String<java.lang.String>` columnName
|          :ref:`String<java.lang.String>` ascOrDesc
|          :ref:`Long<java.lang.Long>` cardinality
|          :ref:`Long<java.lang.Long>` pages
|          :ref:`String<java.lang.String>` filterCondition


| *@Override*
| **compareTo** (o) → int
|          :ref:`IndexColumn<com.manticore.h2.IndexColumn>` o
|          returns int



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _com.manticore.h2.MetaData:

=======================================================================
MetaData
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **MetaData** (con)
|          :ref:`Connection<java.sql.Connection>` con


| **build** ()


| **put** (catalog) → :ref:`Catalog<com.manticore.h2.Catalog>`
|          :ref:`Catalog<com.manticore.h2.Catalog>` catalog
|          returns :ref:`Catalog<com.manticore.h2.Catalog>`



| **getCatalogs** () → :ref:`Map<java.util.Map>`
|          returns :ref:`Map<java.util.Map>`



| **put** (schema) → :ref:`Schema<com.manticore.h2.Schema>`
|          :ref:`Schema<com.manticore.h2.Schema>` schema
|          returns :ref:`Schema<com.manticore.h2.Schema>`



| **put** (table) → :ref:`Table<com.manticore.h2.Table>`
|          :ref:`Table<com.manticore.h2.Table>` table
|          returns :ref:`Table<com.manticore.h2.Table>`




..  _com.manticore.h2.MetaDataTools:

=======================================================================
MetaDataTools
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **MetaDataTools** ()


| **verifyDecimalPrecision** (con) → :ref:`Collection<java.util.Collection>`
|          :ref:`Connection<java.sql.Connection>` con
|          returns :ref:`Collection<java.util.Collection>`




..  _com.manticore.h2.PrimaryKey:

=======================================================================
PrimaryKey
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **PrimaryKey** (tableCatalog, tableSchema, tableName, primaryKeyName)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` primaryKeyName


| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _com.manticore.h2.Recommendation:

=======================================================================
Recommendation
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **Recommendation** (issue, recommendation)
|          :ref:`String<java.lang.String>` issue
|          :ref:`String<java.lang.String>` recommendation



..  _com.manticore.h2.Reference:

=======================================================================
Reference
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` 

| **Reference** (pkTableCatalog, pkTableSchema, pkTableName, fkTableCatalog, fkTableSchema, fkTableName, updateRule, deleteRule, fkName, pkName, deferrability)
|          :ref:`String<java.lang.String>` pkTableCatalog
|          :ref:`String<java.lang.String>` pkTableSchema
|          :ref:`String<java.lang.String>` pkTableName
|          :ref:`String<java.lang.String>` fkTableCatalog
|          :ref:`String<java.lang.String>` fkTableSchema
|          :ref:`String<java.lang.String>` fkTableName
|          :ref:`Short<java.lang.Short>` updateRule
|          :ref:`Short<java.lang.Short>` deleteRule
|          :ref:`String<java.lang.String>` fkName
|          :ref:`String<java.lang.String>` pkName
|          :ref:`Short<java.lang.Short>` deferrability


| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _com.manticore.h2.Schema:

=======================================================================
Schema
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **Schema** (tableSchema, tableCatalog)
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableCatalog


| **getSchemas** (metaData) → :ref:`Collection<java.util.Collection>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`Collection<java.util.Collection>`



| **put** (table) → :ref:`Table<com.manticore.h2.Table>`
|          :ref:`Table<com.manticore.h2.Table>` table
|          returns :ref:`Table<com.manticore.h2.Table>`



| **get** (tableName) → :ref:`Table<com.manticore.h2.Table>`
|          :ref:`String<java.lang.String>` tableName
|          returns :ref:`Table<com.manticore.h2.Table>`



| *@Override*
| **compareTo** (o) → int
|          :ref:`Schema<com.manticore.h2.Schema>` o
|          returns int



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int




..  _com.manticore.h2.Table:

=======================================================================
Table
=======================================================================

*extends:* :ref:`Object<java.lang.Object>` *implements:* :ref:`Comparable<java.lang.Comparable>` 

| **Table** (tableCatalog, tableSchema, tableName, tableType, remarks, typeCatalog, typeSchema, typeName, selfReferenceColName, referenceGeneration)
|          :ref:`String<java.lang.String>` tableCatalog
|          :ref:`String<java.lang.String>` tableSchema
|          :ref:`String<java.lang.String>` tableName
|          :ref:`String<java.lang.String>` tableType
|          :ref:`String<java.lang.String>` remarks
|          :ref:`String<java.lang.String>` typeCatalog
|          :ref:`String<java.lang.String>` typeSchema
|          :ref:`String<java.lang.String>` typeName
|          :ref:`String<java.lang.String>` selfReferenceColName
|          :ref:`String<java.lang.String>` referenceGeneration


| **getTables** (metaData) → :ref:`Collection<java.util.Collection>`
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          returns :ref:`Collection<java.util.Collection>`



| **getColumns** (metaData)
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData


| **getIndices** (metaData, approximate)
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData
|          boolean approximate


| **getPrimaryKey** (metaData)
|          :ref:`DatabaseMetaData<java.sql.DatabaseMetaData>` metaData


| *@Override*
| **compareTo** (o) → int
|          :ref:`Table<com.manticore.h2.Table>` o
|          returns int



| **add** (column) → boolean
|          :ref:`Column<com.manticore.h2.Column>` column
|          returns boolean



| **contains** (column) → boolean
|          :ref:`Column<com.manticore.h2.Column>` column
|          returns boolean



| **put** (index) → :ref:`Index<com.manticore.h2.Index>`
|          :ref:`Index<com.manticore.h2.Index>` index
|          returns :ref:`Index<com.manticore.h2.Index>`



| **containsIndexKey** (indexName) → boolean
|          :ref:`String<java.lang.String>` indexName
|          returns boolean



| **get** (indexName) → :ref:`Index<com.manticore.h2.Index>`
|          :ref:`String<java.lang.String>` indexName
|          returns :ref:`Index<com.manticore.h2.Index>`



| *@Override*
| **equals** (o) → boolean
|          :ref:`Object<java.lang.Object>` o
|          returns boolean



| *@Override*
| **hashCode** () → int
|          returns int



