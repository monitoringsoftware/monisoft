-- MySQL dump 10.11
--
-- Host:          	    Database:
-- ------------------------------------------------------
-- Server version	5.0.32-Debian_7etch10-log
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
--
-- Table structure for table `T_Building`
--
DROP TABLE IF EXISTS `T_Building`;
CREATE TABLE `T_Building` (
  `id_Building` int(10) unsigned NOT NULL auto_increment,
  `BuildingName` varchar(100) default NULL,
  `Strasse` varchar(100) default NULL,
  `PLZ` int(10) unsigned default NULL,
  `Ansprechpartner` varchar(255) default NULL,
  `Telefon` bigint(20) unsigned default NULL,
  `Netzwerkdaten` varchar(255) default NULL,
  `Ort` varchar (50) default NULL,
  `Beschreibung` varchar(250) default NULL,
  `SensorCollectionIDs` varchar(255) default NULL,
  `image` MEDIUMBLOB DEFAULT NULL,
  PRIMARY KEY  (`id_Building`),
  UNIQUE `index1` (`BuildingName`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

--
-- Table structure for table `T_Config`
--

DROP TABLE IF EXISTS `T_Config`;
CREATE TABLE `T_Config` (`DBVersion` FLOAT NOT NULL) ENGINE = MyISAM;
INSERT INTO `T_Config` set DBVersion = 2.11;

--
-- Table structure for table `T_CounterChanges`
--

DROP TABLE IF EXISTS `T_CounterChanges`;
CREATE TABLE `T_CounterChanges` (
  `T_Sensors_id_Sensors` int(11) NOT NULL,
  `Time` datetime NOT NULL,
  `LastValue` decimal(15,5) default NULL,
  `FirstValue` decimal(15,5) default NULL,
  PRIMARY KEY  (`T_Sensors_id_Sensors`,`Time`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `T_Events`
--

DROP TABLE IF EXISTS `T_Events`;
CREATE TABLE `T_Events` (
  `TimeStart` datetime NOT NULL,
  `TimeSpan` bigint(20) unsigned NOT NULL,
  `State` smallint(6) default NULL,
  `T_Sensors_id_Sensors` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`T_Sensors_id_Sensors`, `TimeStart`)
) ENGINE=MyISAM PACK_KEYS=1;

--
-- Table structure for table `T_Factors`
--

DROP TABLE IF EXISTS `T_Factors`;
CREATE TABLE `T_Factors` (
  `T_Sensors_id_Sensors` int(10) unsigned NOT NULL,
  `Value` float NOT NULL,
  `Time` datetime NOT NULL
) ENGINE=MyISAM;

--
-- Table structure for table `T_Graphics`
--

DROP TABLE IF EXISTS `T_Graphics`;
CREATE TABLE `T_Graphics` (
  `id_Graphics` int(11) NOT NULL auto_increment,
  `Name` varchar(50) NOT NULL,
  `Description` MEDIUMBLOB default NULL,
  PRIMARY KEY  (`id_Graphics`),
  UNIQUE `index1` (`Name`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

--
-- Table structure for table `T_History`
--

DROP TABLE IF EXISTS `T_History`;
CREATE TABLE `T_History` (
  `T_Log_id_Log` int(10) unsigned NOT NULL,
  `T_Sensors_id_Sensors` smallint(5) unsigned NOT NULL,
  `Value` decimal(15,5) NOT NULL,
  `TimeStamp` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`TimeStamp`,`T_Sensors_id_Sensors`),
  KEY `T_History_FKIndex1` (`T_Sensors_id_Sensors`)
) ENGINE=MyISAM AUTO_INCREMENT=1 PACK_KEYS=1;

--
-- Table structure for table `T_Log`
--

DROP TABLE IF EXISTS `T_Log`;
CREATE TABLE `T_Log` (
  `id_Log` int(10) unsigned NOT NULL auto_increment,
  `Description` varchar(255) NOT NULL,
  `Time` datetime NOT NULL,
  `EventType` int(10) unsigned NOT NULL,
  `Value` float default NULL,
  `User` varchar(20) NOT NULL,
  PRIMARY KEY  (`id_Log`)
) ENGINE=MyISAM AUTO_INCREMENT=1;

--
-- Table structure for table `T_Sensors`
--

DROP TABLE IF EXISTS `T_Sensors`;
CREATE TABLE `T_Sensors` (
  `id_Sensors` int(10) unsigned NOT NULL auto_increment,
  `T_Units_id_Units` int(10) unsigned NOT NULL DEFAULT 0,
  `Sensor` varchar(100) NOT NULL DEFAULT '',
  `MinWE` int(11) default NULL,
  `MaxWE` int(11) default NULL,
  `MaxChangeTimeWT` smallint(5) unsigned default NULL,
  `isCounter` tinyint(1) NOT NULL DEFAULT 0,
  `Description` varchar(1024) NOT NULL DEFAULT '',
  `Interval` int(11) NOT NULL DEFAULT 0,
  `Virtual` VARCHAR(1024) DEFAULT NULL,
  `MinWT` int(11) default NULL,
  `MaxWT` int(11) default NULL,
  `SensorKey` varchar(150) NOT NULL DEFAULT '',
  `Factor` float NOT NULL DEFAULT 1,
  `MaxChangeTimeWE` smallint(5) unsigned default NULL,
  `Manual` tinyint(1) NOT NULL DEFAULT 0,
  `isEvent` tinyint(1) NOT NULL DEFAULT 0,
  `T_Building_id_Building` int(10) unsigned default NULL,
  `counterNo` varchar(40) default NULL,
  `medium` varchar(100) default NULL,
  `isResetCounter` tinyint(1) NOT NULL DEFAULT 0,
  `Constant` decimal(10,5) default NULL,
  `isUsage` tinyint(1) NOT NULL DEFAULT 0,
  `UtcPlusX` int(11) DEFAULT '0',
  `Summertime` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY  (`id_Sensors`),
  KEY `T_Sensors_FKIndex1` (`T_Units_id_Units`),
  UNIQUE `index1` (`Sensor`),
  UNIQUE `index2` USING BTREE (`SensorKey`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;


--
-- Table structure for table `T_Units`
--

DROP TABLE IF EXISTS `T_Units`;
CREATE TABLE `T_Units` (
  `id_Units` int(10) unsigned NOT NULL auto_increment,
  `Unit` varchar(10) NOT NULL,
  PRIMARY KEY  (`id_Units`),
  UNIQUE `index1` (`Unit`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

--
-- Table structure for table `T_ReferenceHierarchy`
--

DROP TABLE IF EXISTS`T_ReferenceHierarchy`;
CREATE TABLE `T_ReferenceHierarchy` (
  `id_ReferenceHierarchy` int(11) NOT NULL auto_increment,
  `ReferenceName` varchar(255) NOT NULL,
  `Value` float default NULL,
  `T_Units_id_Units` int(11) default NULL,
  `lft` int(11) NOT NULL,
  `right` int(11) NOT NULL,
  PRIMARY KEY USING BTREE (`id_ReferenceHierarchy`),
  UNIQUE `index1` (`ReferenceName`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

--
-- Table structure for table `T_References`
--

DROP TABLE IF EXISTS`T_References`;
CREATE TABLE `T_References` (
  `id_References` int(11) NOT NULL auto_increment,
  `Value` decimal(10,3) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `T_Building_id_Building` int(11) default NULL,
  PRIMARY KEY  (`id_References`),
  KEY `building_index` (`T_Building_id_Building`),
  UNIQUE `index1` (`Name`, `T_Building_id_Building`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

--
-- Table structure for table `T_Categories`
--

DROP TABLE IF EXISTS`T_Categories`;
CREATE TABLE `T_Categories` (
  `id` int(11) NOT NULL auto_increment,
  `lft` int(11) NOT NULL,
  `rgt` int(11) NOT NULL,
  `catnode` varchar(255) character set utf8 NOT NULL,
  `catset` varchar(255) character set utf8 NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;

--
-- Table structure for table `T_SensorCollections`
--

DROP TABLE IF EXISTS `T_SensorCollections`;
CREATE TABLE `T_SensorCollections` (
  `colname` varchar(255) NOT NULL,
  `sensors` varchar(5000) NOT NULL,
  `creator` tinyint(3) unsigned NOT NULL default '0',
  `id` int(11) NOT NULL auto_increment,
  `climatecorrection` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE `colname_index` (`colname`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `T_Monthly`
--

DROP TABLE IF EXISTS `T_Monthly`;
CREATE TABLE `T_Monthly` (
  `T_Sensors_id_Sensors` int(11) NOT NULL,
  `Month` tinyint(3) unsigned NOT NULL,
  `Year` smallint(5) unsigned NOT NULL,
  `Value` decimal(15,8) default NULL,
  `T_Log_id_Log` int(10) unsigned NOT NULL,
  PRIMARY KEY USING BTREE (`Year`,`Month`,`T_Sensors_id_Sensors`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

INSERT INTO `T_Categories` SET lft=1, rgt=2, catnode='Messpunktkategorien', catset='KAT';
INSERT INTO `T_Log` SET id_Log=0, Description='default', Time='0000-00-00 00:00:00', EventType=4,Value=0,User='default';
UPDATE T_Log SET id_Log= 0 WHERE EventType=4;

--
-- Table structure for table `T_WeatherDefinition`
--

DROP TABLE IF EXISTS `T_WeatherDefinition`;
CREATE TABLE `T_WeatherDefinition` (
  `category` varchar(255) NOT NULL,
  `T_Sensors_id_Sensors` int(11) default NULL,
  PRIMARY KEY  (`category`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `T_Clusters`
--

DROP TABLE IF EXISTS `T_Clusters`;
CREATE TABLE `T_Clusters` (
  `id_Clusters` int(11) NOT NULL auto_increment,
  `ClusterName` varchar(100) NOT NULL,
  `ClusterKat` int(11) NOT NULL,
  `Buildings` varchar(2000) character set utf8 default NULL,
  PRIMARY KEY  (`id_Clusters`),
  UNIQUE KEY `unique` USING BTREE (`ClusterName`,`ClusterKat`)
) ENGINE=MyISAM AUTO_INCREMENT=59 DEFAULT CHARSET=utf8;

--
-- Table structure for table `T_References`
--

DROP TABLE IF EXISTS `T_ReferenceNames`;
CREATE TABLE `T_ReferenceNames` (
  `RefName` varchar(40) NOT NULL,
  `unitID` int(10) unsigned NOT NULL,
  `Description` varchar(100) NOT NULL,
  `id` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  USING BTREE (`RefName`),
  UNIQUE KEY `id` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


INSERT INTO `T_ReferenceNames` VALUES  ('BGF',48,'Brutto-Grundfläche',1),
 ('NGF',48,'Netto-Grundfläche',2),
 ('HNF1',48,'Wohnen und Aufenthalt',3),
 ('HNF2',48,'Büroarbeit',4),
 ('HNF3',48,'Produktion, Hand- und Maschinenarbeit, Experimente',5),
 ('HNF4',48,'Lagern, Verteilen und Verkaufen',6),
 ('HNF5',48,'Bildung, Unterricht und Kultur',7),
 ('HNF6',48,'Heilen und Pflegen',8),
 ('EBF',48,'Energiebezugsfläche',9),
 ('TF',48,'Technische Funktionsfläche',10),
 ('NF',48,'Nutzfläche',11),
 ('KGF',48,'Konstruktionsfläche',12),
 ('VF',48,'Verkehrsfläche',13),
 ('MA',51,'Anzahl Arbeitsplätze',14),
 ('BRI',26,'Brutto-Rauminhalt',15),
 ('NRI',26,'Netto-Rauminhalt',16),
 ('KRI',26,'Konstruktions-Rauminhalt',17),
 ('FensterFL',48,'Fensterfläche',18);

--
-- Table structure for table `T_DWDClimateFactors`
--

DROP TABLE IF EXISTS `T_DWDClimateFactors`;
CREATE TABLE `T_DWDClimateFactors` (
  `PLZ` int(10) unsigned NOT NULL default '0',
  `Factor` double NOT NULL default '1',
  `StartDate` date NOT NULL,
  PRIMARY KEY  (`PLZ`,`StartDate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Table structure for table `T_Annotations`
--

DROP TABLE IF EXISTS `T_Annotations`;
CREATE TABLE `T_Annotations` (
  `name` varchar(300) NOT NULL,
  `object` mediumtext NOT NULL,
  PRIMARY KEY  (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

/*!40000 ALTER TABLE `T_Annotations` DISABLE KEYS */;
INSERT INTO `T_Annotations` (`name`,`object`) VALUES 
 ('Komfortbereich nach DIN ISO 1946 Teil 2','<AnnotationContainer><annotationName>Komfortbereich nach DIN ISO 1946 Teil 2</annotationName><annotations><AnnotationElement><fillColor><red>0</red>        <green>219</green><blue>153</blue><alpha>255</alpha></fillColor><lineColor><red>200</red><green>0</green>        <blue>0</blue><alpha>255</alpha></lineColor><fillAlpha>74</fillAlpha><lineAlpha>0</lineAlpha><stroke>        <width>1.0</width><join>0</join><cap>2</cap><miterlimit>10.0</miterlimit><dash__phase>0.0</dash__phase></stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>20.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>26.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>      </points>      <name>1.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>/java.lang.Double-array><java.lang.Double-array><double>26.0</double><double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>29.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>        </java.lang.Double-array>      </points>      <name>2.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>153</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>26.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>30.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>33.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>26.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>22.0</double>        </java.lang.Double-array>      </points>      <name>3.</name>      <closed>true</closed>    </AnnotationElement>  </annotations></AnnotationContainer>'),
 ('Komfortbereich nach ISO 7730','<AnnotationContainer>  <annotationName>Komfortbereich nach ISO 7730</annotationName>  <annotations>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>153</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>      </points>      <name>1.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>153</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>13.0</double>          <double>23.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>23.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>25.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>25.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>23.5</double>        </java.lang.Double-array>      </points>      <name>2.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>      </points>      <name>3.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>23.0</double>        </java.lang.Double-array>      </points>      <name>4.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>13.0</double>          <double>23.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>23.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>23.5</double>        </java.lang.Double-array>      </points>      <name>5.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>13.0</double>          <double>25.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>25.5</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>25.5</double>        </java.lang.Double-array>      </points>      <name>6.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>120</red>        <green>255</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>19.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>19.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>      </points>      <name>7.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>120</red>        <green>255</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>12.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>24.0</double>        </java.lang.Double-array>      </points>      <name>8.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>120</red>        <green>255</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>13.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>23.0</double>        </java.lang.Double-array>      </points>      <name>9.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>120</red>        <green>255</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>13.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>27.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>27.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>13.0</double>          <double>26.0</double>        </java.lang.Double-array>      </points>      <name>10.</name>      <closed>true</closed>    </AnnotationElement>  </annotations></AnnotationContainer>'),
 ('Komfortgrenze nach Bielefelder Urteil (\"32/6\"-Regel)','<AnnotationContainer>  <annotationName>Komfortgrenze nach Bielefelder Urteil (&quot;32/6&quot;-Regel)</annotationName>  <annotations>    <AnnotationElement>      <fillColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>102</fillAlpha>      <lineAlpha>255</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>32.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>44.0</double>        </java.lang.Double-array>      </points>      <name>1.</name>      <closed>false</closed>    </AnnotationElement>  </annotations></AnnotationContainer>'),
 ('Komfortbereich DIN ISO 1946 mit Bielefelfer Urteil (\"32/6\"-Regel)','AnnotationContainer>  <annotationName>Komfortbereich DIN ISO 1946 mit Bielefelfer Urteil (&quot;32/6&quot;-Regel)</annotationName>  <annotations>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>20.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>26.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>      </points>      <name>1.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>26.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>29.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>        </java.lang.Double-array>      </points>      <name>2.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>153</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>26.0</double>          <double>22.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>30.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>33.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>26.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>22.0</double>        </java.lang.Double-array>      </points>      <name>3.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>102</fillAlpha>      <lineAlpha>255</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>32.0</double>          <double>26.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>44.0</double>        </java.lang.Double-array>      </points>      <name>4.</name>      <closed>false</closed>    </AnnotationElement>  </annotations></AnnotationContainer>'),
 ('Komfortbereich nach DIN EN 15251','<AnnotationContainer>  <annotationName>Komfortbereich nach DIN EN 15251</annotationName>  <annotations>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>153</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>21.8</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>33.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>37.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>24.1</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>      </points>      <name>1.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>21.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>21.8</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>33.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>32.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>20.8</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>21.0</double>        </java.lang.Double-array>      </points>      <name>2.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>23.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>23.0</double>        </java.lang.Double-array>      </points>      <name>3.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>120</red>        <green>255</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>20.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>20.8</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>32.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>31.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>19.8</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>15.0</double>          <double>19.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>19.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>20.0</double>        </java.lang.Double-array>      </points>      <name>4.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>0</red>        <green>219</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>10.0</double>          <double>24.1</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>37.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>38.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>25.1</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>24.1</double>        </java.lang.Double-array>      </points>      <name>5.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>120</red>        <green>255</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>-30.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>24.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>25.0</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>-30.0</double>          <double>24.0</double>        </java.lang.Double-array>      </points>      <name>6.</name>      <closed>true</closed>    </AnnotationElement>    <AnnotationElement>      <fillColor>        <red>120</red>        <green>255</green>        <blue>153</blue>        <alpha>255</alpha>      </fillColor>      <lineColor>        <red>200</red>        <green>0</green>        <blue>0</blue>        <alpha>255</alpha>      </lineColor>      <fillAlpha>74</fillAlpha>      <lineAlpha>0</lineAlpha>      <stroke>        <width>1.0</width>        <join>0</join>        <cap>2</cap>        <miterlimit>10.0</miterlimit>        <dash__phase>0.0</dash__phase>      </stroke>      <points>        <java.lang.Double-array>          <double>10.0</double>          <double>25.1</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>38.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>50.0</double>          <double>39.3</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>26.1</double>        </java.lang.Double-array>        <java.lang.Double-array>          <double>10.0</double>          <double>25.1</double>        </java.lang.Double-array>      </points>      <name>7.</name>      <closed>true</closed>    </AnnotationElement>  </annotations></AnnotationContainer>');
/*!40000 ALTER TABLE `T_Annotations` ENABLE KEYS */;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2009-06-08  9:50:50
