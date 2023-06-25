######################################
H2 Migration Tool
######################################

.. toctree::
   :maxdepth: 2
   :hidden:

   usage
   Changelog <changelog.md>
   Java API <javadoc.rst>

.. image:: https://badgen.net/maven/v/maven-central/com.manticore-projects.jdbc/H2MigrationTool
    :alt: Maven Badge

.. image:: https://app.codacy.com/project/badge/Grade/f9024986ff4c44199119b3f63ad18f73
    :alt: Codacy Badge
    :target: https://app.codacy.com/gh/manticore-projects/H2MigrationTool/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade

.. image:: https://coveralls.io/repos/github/manticore-projects/H2MigrationTool/badge.svg?branch=main
    :alt: Coveralls Badge
    :target: https://coveralls.io/github/manticore-projects/H2MigrationTool?branch=main

.. image:: https://img.shields.io/badge/License-GPL-blue
    :alt: License Badge

.. image:: https://img.shields.io/github/release/manticore-projects/H2MigrationTool?include_prereleases=&sort=semver&color=blue
    :alt: GitGub Release Badge

.. image:: https://img.shields.io/github/issues/manticore-projects/H2MigrationTool
    :alt: GitGub Issues Badge

**H2MigrationTool** is a Java Application for migrating `H2 Database <https://www.h2database.com/>`_ Files from older into newer versions. It will export an existing database to SQL using an old H2 Driver and then create a new Database using the new Driver.
It also supports the Recovery of corrupted Databases and direct creation from SQL scripts.

.. sidebar:: Graphical User Interface

    .. image:: _static/H2MigrationTool.png
       :width: 100 %
       :alt: H2MigrationTool Graphical User Interface
       :align: right

Latest stable release: |H2MIGRATIONTOOL_STABLE_VERSION_LINK|

Development version: |H2MIGRATIONTOOL_SNAPSHOT_VERSION_LINK|

`GitHub Repository <https://github.com/manticore-projects/H2MigrationTool>`_

.. code-block:: sh
    :caption: Sample SQL Statement

    java -jar H2MigrationTool.jar                       \
        -l /home/are/Downloads/h2-libs                  \
        -f 1.4.199 -t 2.0.201 -d /home/are/databases    \
        -c ZIP -o VARIABLE_BINARY                       \
        --force

    java -cp H2MigrationTool.jar com.manticore.Recovery \
        -f 1.3.176                                      \
        -d /home/are/databases/riskbox.h2.db


*******************************
Features
*******************************

* **Migrate H2 Database** via the `Export to Script <https://www.h2database.com/html/commands.html#script>`_ and `Create from Script <https://www.h2database.com/html/commands.html#runscript>`_ commands, using old and new H2 Drivers for each
* **Recover** H2 Databases via the `H2 Recovery Tool <http://www.h2database.com/javadoc/org/h2/tools/Recover.html>`_
* **Fat JAR** including all relevant H2 Drivers
*  **UI** Graphical User Interface
* **CLI** Command Line Interface
* **File** or **Directory** operation
* **Compression** (Zip or GZip)







