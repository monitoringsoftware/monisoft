ALTER TABLE T_Sensors ADD COLUMN `counterNo` VARCHAR(40) DEFAULT NULL;
ALTER TABLE T_Sensors ADD COLUMN `medium` VARCHAR(100) DEFAULT NULL;
ALTER TABLE T_Sensors ADD COLUMN `isResetCounter` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE T_Sensors ADD COLUMN `Constant` decimal(10,5) default NULL;
ALTER TABLE T_Sensors DROP COLUMN `MaxUpdateTime`;
ALTER TABLE T_Sensors DROP COLUMN `Periodic`;
ALTER TABLE T_Sensors MODIFY COLUMN `Interval` INTEGER NOT NULL DEFAULT 0;
ALTER TABLE T_Sensors DROP INDEX `index1` , DROP INDEX `index2`;
ALTER IGNORE TABLE T_Sensors add unique index1 (Sensor);
ALTER IGNORE TABLE T_Sensors add unique index2 (SensorKey);
ALTER TABLE T_Sensors ADD COLUMN `isUsage` TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE T_Building ADD COLUMN `Strasse` VARCHAR(100) DEFAULT NULL;
ALTER TABLE T_Building ADD COLUMN `PLZ` INTEGER UNSIGNED DEFAULT NULL;
ALTER TABLE T_Building ADD COLUMN `Ort` VARCHAR (50) DEFAULT NULL;
ALTER TABLE T_Building ADD COLUMN `Beschreibung` VARCHAR (250) DEFAULT NULL;
ALTER TABLE T_Building DROP COLUMN `NF`;
ALTER TABLE T_Building DROP COLUMN `HNF`;
ALTER TABLE T_Building DROP COLUMN `HNF2`;
ALTER TABLE T_Building DROP COLUMN `HNF3`;
ALTER TABLE T_Building DROP COLUMN `NGF`;
ALTER TABLE T_Building DROP COLUMN `BGF`;
ALTER TABLE T_Building DROP COLUMN `KGF`;
ALTER TABLE T_Building DROP COLUMN `NNF`;
ALTER TABLE T_Building DROP COLUMN `FF`;
ALTER TABLE T_Building DROP COLUMN `VF`;
ALTER TABLE T_Building DROP COLUMN `MA`;
ALTER TABLE T_Building DROP COLUMN `Adresse`;
ALTER TABLE T_Building DROP COLUMN `Flaeche`;
ALTER TABLE T_Building DROP COLUMN `FensterFlaeche`;
ALTER TABLE T_Building ADD COLUMN `ObjektID` int(11) default NULL;
ALTER TABLE T_Building DROP COLUMN `BRI`;
ALTER TABLE T_Building DROP COLUMN `NRI`;
ALTER TABLE T_Building ADD COLUMN `SensorCollectionIDs` VARCHAR(255) DEFAULT NULL;
ALTER TABLE T_Building MODIFY COLUMN `Telefon` BIGINT UNSIGNED DEFAULT NULL;
ALTER TABLE T_Building ADD COLUMN `image` MEDIUMBLOB  DEFAULT NULL;
ALTER TABLE T_Building DROP COLUMN `active`;

ALTER TABLE T_Monthly MODIFY COLUMN `Value` decimal(15,8) default NULL;

ALTER TABLE T_Events DROP INDEX `SensorIndex`;
ALTER TABLE T_Events DROP INDEX `TimeStartIndex`;
ALTER TABLE T_Events DROP PRIMARY KEY;
ALTER IGNORE TABLE T_Events ADD PRIMARY KEY  (`T_Sensors_id_Sensors`, `TimeStart`);

ALTER TABLE T_SensorCollections DROP PRIMARY KEY;
ALTER TABLE T_SensorCollections ADD COLUMN `id` INTEGER  NOT NULL AUTO_INCREMENT PRIMARY KEY;
ALTER TABLE T_SensorCollections DROP INDEX `name_index`;
ALTER TABLE T_SensorCollections ADD INDEX `colname_index`(`colname`);
ALTER TABLE T_SensorCollections ADD COLUMN `climatecorrection` tinyint(1) NOT NULL default '0';

ALTER TABLE T_References ADD COLUMN `Value` decimal(10,3) NOT NULL;
ALTER TABLE T_References ADD COLUMN `Name` varchar(100) NOT NULL;
ALTER TABLE T_References ADD COLUMN `T_Building_id_Building` int(11) DEFAULT NULL;
ALTER TABLE T_References ADD INDEX `building_index`(`T_Building_id_Building`);
ALTER TABLE T_References DROP COLUMN `T_Sensors_id_Sensors`;
ALTER TABLE T_References DROP COLUMN `T_ReferenceHierarchy_id_ReferenceHierarchy`;
ALTER TABLE T_References DROP COLUMN `T_Units_id_Units`;
ALTER TABLE T_References DROP COLUMN `T_Zones_id_Zones`;

rename table T_Counterchanges to T_CounterChanges;
ALTER TABLE T_CounterChanges DROP PRIMARY KEY;
ALTER TABLE T_CounterChanges DROP INDEX `timeindex`;
ALTER IGNORE TABLE T_CounterChanges ADD PRIMARY KEY (`T_Sensors_id_Sensors`, `Time`);
ALTER TABLE T_CounterChanges MODIFY COLUMN `LastValue` decimal(15,5) default NULL;
ALTER TABLE T_CounterChanges MODIFY COLUMN `FirstValue` decimal(15,5) default NULL;

ALTER IGNORE TABLE T_Units ADD UNIQUE index1 (Unit);
UPDATE T_Units set id_Units = 999 where id_Units = 0;
UPDATE T_Sensors set T_Units_id_Units = 999 where T_Units_id_Units = 0;
DELETE FROM T_Units where id_Units = 0;
ALTER TABLE T_Units MODIFY COLUMN `id_Units` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;
INSERT INTO T_Units SET Unit='GWh';
INSERT INTO T_Units SET Unit='TWh';
INSERT INTO T_Units SET Unit='GW';
INSERT INTO T_Units SET Unit='TW';
INSERT INTO T_Units SET Unit= 'klux';
INSERT INTO T_Units SET Unit='t';
INSERT INTO T_Units SET Unit='J/g';
INSERT INTO T_Units SET Unit='kg/h';
INSERT INTO T_Units SET Unit='var';
INSERT INTO T_Units SET Unit='varh';
INSERT INTO T_Units SET Unit='VA';
ALTER TABLE T_Units DROP COLUMN `isUsageUnit`;

ALTER TABLE `T_Graphics` MODIFY COLUMN `Description` MEDIUMBLOB  DEFAULT NULL;

CREATE TABLE IF NOT EXISTS `T_SensorCollections` (
  `colname` varchar(100) NOT NULL,
  `sensors` varchar(255) NOT NULL,
  `creator` tinyint(3) unsigned NOT NULL default '0',
  `id` int(11) NOT NULL auto_increment,
  PRIMARY KEY  (`id`),
  KEY `colname_index` (`colname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `T_Monthly` (
  `T_Sensors_id_Sensors` int(11) NOT NULL,
  `Month` tinyint(3) unsigned NOT NULL,
  `Year` smallint(5) unsigned NOT NULL,
  `Value` decimal(15,8) default NULL,
  `T_Log_id_Log` int(10) unsigned NOT NULL,
  PRIMARY KEY  USING BTREE (`Year`,`Month`,`T_Sensors_id_Sensors`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS T_Conditions;
DROP TABLE IF EXISTS T_Dates;
DROP TABLE IF EXISTS T_Daily;
DROP TABLE IF EXISTS T_Hours;
DROP TABLE IF EXISTS T_Hourly;
DROP TABLE IF EXISTS T_UnitConversion;
DROP TABLE IF EXISTS T_Zones;


CREATE TABLE IF NOT EXISTS `T_ReferenceNames` (
  `RefName` varchar(40) NOT NULL,
  `unitID` int(10) unsigned NOT NULL,
  `Description` varchar(100) NOT NULL,
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  USING BTREE (`RefName`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `T_Clusters` (
  `id_Clusters` int(11) NOT NULL auto_increment,
  `ClusterName` varchar(100) NOT NULL,
  `ClusterKat` int(11) NOT NULL,
  `Buildings` varchar(2000) character set utf8 default NULL,
  PRIMARY KEY  (`id_Clusters`),
  UNIQUE KEY `unique` USING BTREE (`ClusterName`,`ClusterKat`)
) ENGINE=MyISAM AUTO_INCREMENT=59 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `T_WeatherDefinition` (
  `category` varchar(255) NOT NULL,
  `T_Sensors_id_Sensors` int(11) default NULL,
  PRIMARY KEY  (`category`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `T_DWDClimateFactors` (
  `PLZ` int(10) unsigned NOT NULL default '0',
  `Factor` double NOT NULL default '1',
  `StartDate` date NOT NULL,
  PRIMARY KEY  (`PLZ`,`StartDate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `T_Annotations` (
  `name` varchar(300) NOT NULL,
  `object` mediumtext NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

ALTER TABLE T_History DROP COLUMN `id_History`;

------- NICHT VERGESSEN DIE EMPFOHLENE DB-VERSION und den Eintrag in create_structure.sql ANZUPASSEN!!!!!!!!!!!!!!!!!!!
UPDATE T_Config set DBVersion = 2.9;
------- NICHT VERGESSEN DIE EMPFOHLENE DB-VERSION und den Eintrag in create_structure.sql ANZUPASSEN!!!!!!!!!!!!!!!!!!!