#!/bin/bash
#
# ./migrate-dc-db-image-table.sh <user> <passwd> <old_db> <copy_of_old_db> <new_db> <new_db_schema>
#

# Creating a new database to copy old one
mysql -u $1 -p$2 << EOF
# Create temporary db to copy old db
CREATE DATABASE $4;
EOF

# Copying old database to new temporary database
mysqldump -u $1 -p$2 $3 > htrcvirtdb.sql
mysqldump -u $1 -p$2 $3 | mysql -u $1 -p$2 $4

# Create new db for the new schema
mysql -u $1 -p$2 << EOF
DROP DATABASE IF EXISTS $5;
CREATE DATABASE $5 DEFAULT CHARACTER SET utf8;
GRANT ALL PRIVILEGES ON $5.* TO 'root'@'localhost';
EOF



# Add new schema to new database
mysql -u $1 -p$2 $5 < $6

mysql -u $1 -p$2 << EOF
# Migrate users table
USE $4;
ALTER TABLE users ADD COLUMN imageleftquota int(11) DEFAULT NULL after diskleftquota;
UPDATE users SET imageleftquota=5;

INSERT INTO $5.users SELECT * from $4.users;

# Migrate images table
USE $4;
ALTER TABLE images ADD COLUMN imageid varchar(128) DEFAULT NULL FIRST;
ALTER TABLE images ADD COLUMN source_vm varchar(128) DEFAULT NULL after loginpassword;
ALTER TABLE images ADD COLUMN public tinyint(1) DEFAULT '0' after source_vm;
ALTER TABLE images ADD COLUMN owner varchar(64) DEFAULT NULL after public;
ALTER TABLE images ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP after owner;
ALTER TABLE images ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP after owner;
UPDATE images SET imageid="f3c2a554-5197-492f-a0fe-03e4330a5938", status="ACTIVE", owner="8bca447f-6692-46e7-8c04-802cd911fb36", public=1 where imagename="ubuntu-16-04";
UPDATE images SET imageid="f6f02532-05a7-4ffd-8a90-43b42113671f", status="DELETED", owner="8bca447f-6692-46e7-8c04-802cd911fb36", public=1 where imagename="ubuntu-16-04-iso";
UPDATE images SET imageid="fde25f21-da72-4752-8ca4-366fa94ccb5e", status="DELETED", owner="8bca447f-6692-46e7-8c04-802cd911fb36", public=1 where imagename="ubuntu-16-04-no-password";
UPDATE images SET imageid="fe9a028d-6682-4a13-92ef-e42f9cec13fd", status="ACTIVE", owner="8bca447f-6692-46e7-8c04-802cd911fb36", public=1 where imagename="ubuntu-16-04-with-sample-volumes";
UPDATE images SET imageid="81d73e5f-7b9c-45a1-b0a1-e2c6461ee979", status="DELETED", owner="8bca447f-6692-46e7-8c04-802cd911fb36", public=1 where imagename="ubuntu-dc-management-image";
UPDATE images SET imageid="d2a09995-7fed-41c6-afa3-984be3ab07ae", status="DELETED", owner="8bca447f-6692-46e7-8c04-802cd911fb36", public=1 where imagename="uncamp2015-image";

INSERT INTO $5.images SELECT * from $4.images;

# Insert values to vmhosts table in new database
INSERT INTO $5.vmhosts SELECT * from $4.vmhosts;

# Migrate vms table
USE $4;
ALTER TABLE vms ADD vmname VARCHAR(128) NOT NULL AFTER vmid;
UPDATE vms SET vmname=vmid;
ALTER TABLE vms ADD COLUMN imageid VARCHAR(128) DEFAULT NULL after workingdir;
UPDATE vms SET imageid="f3c2a554-5197-492f-a0fe-03e4330a5938" where imagename="ubuntu-16-04";
UPDATE vms SET imageid="f6f02532-05a7-4ffd-8a90-43b42113671f" where imagename="ubuntu-16-04-iso";
UPDATE vms SET imageid="fde25f21-da72-4752-8ca4-366fa94ccb5e" where imagename="ubuntu-16-04-no-password";
UPDATE vms SET imageid="fe9a028d-6682-4a13-92ef-e42f9cec13fd" where imagename="ubuntu-16-04-with-sample-volumes";
UPDATE vms SET imageid="81d73e5f-7b9c-45a1-b0a1-e2c6461ee979" where imagename="ubuntu-dc-management-image";
UPDATE vms SET imageid="d2a09995-7fed-41c6-afa3-984be3ab07ae" where imagename="uncamp2015-image";

INSERT INTO $5.vms SELECT * from $4.vms;

# Migrate results table
INSERT INTO $5.results SELECT * from $4.results;

# Migrate vmactivity table
INSERT INTO $5.vmactivity SELECT * from $4.vmactivity;

# Migrate ports table
INSERT INTO $5.ports SELECT * from $4.ports;

# Migrate uservmmap table
INSERT INTO $5.uservmmap SELECT * from $4.uservmmap;

EOF
