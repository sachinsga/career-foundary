# Prerequisites
```
Maven
Jdk 8
```

# Below steps needs to be followed for executing spark application

## clone the repository
git clone https://github.com/sachinsga/career-foundary.git

## switch to the project and build the project

```
cd career-foundary
mvn clean package
```

## scp the distribution to the cluster
there will be a zip file generated after successful build in target dir with the name Challenge-1.0-SNAPSHOT.zip, scp this file to the spark cluster
```
scp target/Challenge-1.0-SNAPSHOT.zip  username@192.168.110.119:~
```


## ssh to the spark cluster and switch to the dir where distribution was copied in the last command, unzip the distribution
```
unzip Challenge-1.0-SNAPSHOT.zip -d .
```

this will create a dir challenge/ and there will be following sub dir under this
```
etc/
bin/
lib/
```

## open the challenge.conf file under etc subdir and update the conf file to have mysql related properties
**NOTE: please dont change any property name. All the property values must be under double quotes**

```
vi challenge/etc/challenge.conf
```
add the values before executing the spark application

```
MYSQL_DB_USERNAME=""
MYSQL_DB_PASSWORD=""
MYSQL_DB_HOST=""
MYSQL_DB_PORT=""
MYSQL_DB_DATABASE=""
MYSQL_DB_TABLE="btc_stddev_daily"
```

## executing the spark application
```
sh challenge/bin/run.sh
```

after successful run of the spark application a table in the configured mysql database will be created with the name **btc_stddev_daily** having two columns 
```
day date
stddev double
```
