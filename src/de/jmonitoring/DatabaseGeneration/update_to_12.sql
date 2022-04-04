DROP TABLE IF EXISTS T_Virtual;
ALTER TABLE T_Sensors MODIFY COLUMN Virtual VARCHAR(255) DEFAULT NULL;
ALTER TABLE T_Sensors DROP COLUMN T_Virtual_id_Virtual;
ALTER TABLE T_Sensors MODIFY COLUMN Periodic TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE T_Sensors MODIFY COLUMN Manual TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE T_Sensors MODIFY COLUMN isEvent TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE T_Sensors MODIFY COLUMN isCounter TINYINT(1) NOT NULL DEFAULT 0;
CREATE TABLE IF NOT EXISTS T_Config (`DBVersion` FLOAT NOT NULL DEFAULT 1) ENGINE = MyISAM;
ALTER TABLE T_Sensors MODIFY COLUMN Factor FLOAT NOT NULL DEFAULT 1;
ALTER TABLE T_Sensors MODIFY COLUMN Sensor VARCHAR(100)  NOT NULL DEFAULT '';
ALTER TABLE T_Sensors MODIFY COLUMN Description VARCHAR(255) DEFAULT '';
ALTER TABLE T_Sensors MODIFY COLUMN `Name` VARCHAR(150) NOT NULL DEFAULT '';
ALTER TABLE T_Sensors MODIFY COLUMN T_Units_id_Units INTEGER UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE T_Sensors ADD COLUMN T_Building_id_Building int(10) unsigned DEFAULT NULL;
ALTER TABLE T_Building CHANGE COLUMN `Name` `BuildingName` VARCHAR(45) DEFAULT NULL;
ALTER TABLE T_Sensors CHANGE COLUMN `Name` `SensorKey` VARCHAR(150) DEFAULT NULL;
ALTER TABLE T_History MODIFY COLUMN `Value` DECIMAL(15,5) NOT NULL;


--
-- Table structure for table `T_ReferenceHierarchy`
--

CREATE TABLE IF NOT EXISTS `T_ReferenceHierarchy` (
  `id_ReferenceHierarchy` int(11) NOT NULL auto_increment,
  `ReferenceName` varchar(255) NOT NULL,
  `Value` float default NULL,
  `T_Units_id_Units` int(11) default NULL,
  `lft` int(11) NOT NULL,
  `right` int(11) NOT NULL,
  PRIMARY KEY USING BTREE (`id_ReferenceHierarchy`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

--
-- Table structure for table `T_References`
--

CREATE TABLE IF NOT EXISTS `T_References` (
  `id_References` int(11) NOT NULL auto_increment,
  `T_Sensors_id_Sensors` int(11) NOT NULL,
  `T_ReferenceHierarchy_id_ReferenceHierarchy` int(11) NOT NULL,
  PRIMARY KEY  (`id_References`)
) ENGINE=MyISAM AUTO_INCREMENT=1;

CREATE TABLE IF NOT EXISTS `T_Categories` (
  `id` int(11) NOT NULL auto_increment,
  `lft` int(11) NOT NULL,
  `rgt` int(11) NOT NULL,
  `catnode` varchar(255) character set utf8 NOT NULL,
  `catset` varchar(255) character set utf8 NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

DELETE FROM T_Config;
INSERT INTO T_Config set DBVersion = 1.2;