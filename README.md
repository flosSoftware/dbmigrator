# dbmigrator
This is a Java/PHP tool for exporting DB tables between different schemas/databases. 
Offers a Java GUI to configure the process. 
The result is a series of PHP scripts that can be launched to perform data export/import.
Supports the following DB: **mysql, sql server**. 
Generated scripts work require **mysqli** and **sqlsrv** PHP extensions installed.
## HOW TO USE IT
Run the class **Gui.java** and then choose the appropriate connection parameters. Then go to *File...->Connect to DB*.
![](https://albertof.com/img/dbmigrator/-1.png)
Choose one table from the source and the destination DB using the dropdowns.
![](https://albertof.com/img/dbmigrator/0.png)
Go to *File...->Save and get scripts*. Then open the file **report.xls** inside **dbmigrator** folder.
![](https://albertof.com/img/dbmigrator/1.png)
Copy and launch the ALTER TABLE statements to synchronize database table definitions (if some fields are missing in one table).
Go to *File...->Connect to DB* and choose the fields to map.
![](https://albertof.com/img/dbmigrator/2.png)
Go to *File...->Save and get scripts*. After that, go into **dbmigrator/script** folder. This is the folder that contains the PHP script to launch for DB import/export.
![](https://albertof.com/img/dbmigrator/3.png)
If you make changes to the DB connection parameters, always go to *File...->Connect to DB* and re-choose the correct tables/fields.
You can also convert multiple table going to *File...->Add mapping*.
## Project configuration
This project has been created with Eclipse Oxygen and m2eclipse (maven v. 3.3.9). There is a local maven repository in local-repo/ directory because some libs are not available as maven dependencies.
## WIP
Add support to Oracle DB.
