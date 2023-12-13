******************************
How to use it
******************************

Precompiled Binaries
=============================

Latest stable release: |H2MIGRATIONTOOL_STABLE_VERSION_LINK|

Development version: |H2MIGRATIONTOOL_SNAPSHOT_VERSION_LINK|

.. code-block:: sh
    :caption: Graphical User Interface

    java -jar H2MigrationTool-all.jar

.. code-block:: sh
    :caption: MIGRATION Command Line Options

    java -jar H2MigrationTool-all.jar -l <arg> -f <arg> -t <arg> -d <arg>
         [-u <arg>] [-p <arg>] [-s <arg>] [-c <arg>] -o <arg> [--force] [-h]

    -l,--lib-dir <arg>        Folder containing the H2 jar files.
    -f,--version-from <arg>   Old H2 version of the existing database.
    -t,--version-to <arg>     New H2 version to upgrade to.
    -d,--db-file <arg>        The existing H2 database (old format).
    -u,--user <arg>           The database username.
    -p,--password <arg>       The database password.
    -s,--script-file <arg>    The export script file.
    -c,--compression <arg>    The Compression Method [ZIP, GZIP]
    -o,--options <arg>        The upgrade options [VARIABLE_BINARY]
       --force                Overwrite files and continue on failure.
    -h,--help                 Show the help message.

.. code-block:: sh
    :caption: RECOVERY Command Line Options

    java -cp H2MigrationTool-all.jar com.manticore.Recovery [-l <arg>] -f
         <arg> -d <arg> [-h]

    -l,--lib-dir <arg>        (Relative) Folder containing the H2 jar files.
    -f,--version-from <arg>   H2 version of the existing database.
    -d,--db-file <arg>        The (relative) existing H2 database file.
    -h,--help                 Show the help message.


Compile from Source Code
==============================

You will need to have ``JDK 8`` or ``JDK 11`` or ``JDK 17`` installed.

.. tab:: Maven

    .. code-block:: shell

        git clone https://github.com/manticore-projects/H2MigrationTool.git
        cd H2MigrationTool
        mvn install

.. tab:: Gradle

    .. code-block:: shell

        git clone https://github.com/manticore-projects/H2MigrationTool.git
        cd H2MigrationTool
        gradle build



Maven Artifacts
==============================

.. tab:: Maven Release

    .. code-block:: xml
        :substitutions:

        <dependency>
            <groupId>com.manticore-projects.tools</groupId>
            <artifactId>h2migrationtool</artifactId>
            <version>|H2MIGRATIONTOOL_VERSION|</version>
        </dependency>

.. tab:: Maven Snapshot

    .. code-block:: xml
        :substitutions:

        <repositories>
            <repository>
                <id>sonatype-snapshots</id>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
                <url>https://oss.sonatype.org/content/groups/public/</url>
            </repository>
        </repositories>
        <dependency>
            <groupId>com.manticore-projects.tools</groupId>
            <artifactId>h2migrationtool</artifactId>
            <version>|H2MIGRATIONTOOL_SNAPSHOT_VERSION|</version>
        </dependency>

.. tab:: Gradle Stable

    .. code-block:: groovy
        :substitutions:

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation 'com.manticore-projects.tools:h2migrationtool:|H2MIGRATIONTOOL_VERSION|'
        }

.. tab:: Gradle Snapshot

    .. code-block:: groovy
        :substitutions:

        repositories {
            maven {
                url = uri('https://oss.sonatype.org/content/groups/public/')
            }
        }

        dependencies {
            implementation 'com.manticore-projects.tools:h2migrationtool:|H2MIGRATIONTOOL_SNAPSHOT_VERSION|'
        }



