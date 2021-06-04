# H2MigrationTool
A software tool for migration of an old H2 database into a new H2 database format automatically.

## Usage
```man
  java -jar H2MigrationTool.jar -l <arg> -f <arg> -t <arg> -d <arg>
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
   -h,--help                 Show the help mesage.
```
## Example
```bash
java -jar H2MigrationTool.jar -l /home/are/Downloads/h2-libs                        \
                              -f 1.4.199 -t 2.0.201 -d /home/are/.manticore/riskbox \
                              -c ZIP -o VARIABLE_BINARY                             \
                              --force
```
## Graphical User Interface
![image](https://user-images.githubusercontent.com/18080123/120748212-9bea7980-c52c-11eb-96f0-101f0e47e3eb.png)
