-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.32-Debian_7etch8-log


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;



DROP TABLE IF EXISTS `T_Units`;
CREATE TABLE  `T_Units` (
  `id_Units` int(10) unsigned NOT NULL auto_increment,
  `Unit` varchar(10) NOT NULL,
  `isUsageUnit` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY  (`id_Units`)
) ENGINE=MyISAM AUTO_INCREMENT=1 CHARACTER SET utf8;



/*!40000 ALTER TABLE `T_Units` DISABLE KEYS */;
LOCK TABLES `T_Units` WRITE;
INSERT INTO `T_Units` VALUES
 (1,'°C',0),
 (2,'W',0),
 (3,'Wh',1),
 (4,'ppm',0),
 (5,'%',0),
 (6,'m³/h',0),
 (7,'Pa',0),
 (8,'lux',0),
 (9,'m/s',0),
 (10,'s',0),
 (11,'W/m²',0),
 (12,'kW',0),
 (13,'MW',0),
 (14,'kWh',1),
 (15,'MWh',1),
 (16,'l/h',0),
 (17,'km/h',0),
 (18,'min',0),
 (19,'h',0),
 (20,'d',0),
 (21,'°',0),
 (22,'bool',0),
 (23,'Ws',1),
 (24,'l/m²',0),
 (25,'hPa',0),
 (26,'m³',1),
 (27,'g/kg',0),
 (28,'mm',0),
 (29,'cm',0),
 (30,'m',0),
 (31,'g',0),
 (32,'kg',0),
 (33,'db',0),
 (34,'V',0),
 (35,'A',0),
 (36,'mV',0),
 (37,'mA',0),
 (38,'cd',0),
 (39,'lm',0),
 (40,'°F',0),
 (41,'l',1),
 (42,'n/a',0),
 (43,'bar',0),
 (44,'klx',0),
 (45,'kWh/m²d',0),
 (46,'',0),
 (47,'to',0),
 (48,'m²',0),
 (49,'mm/m²',0),
 (50,'Nm³',0),
 (51,'bft',0),
 (52,'N',0),
 (53,'J',0),
 (54,'kJ',0),
 (55,'mbar',0),
 (56,'lx',0),
 (57,'mW',0),
 (58,'0/1',0),
 (59,'mWh',1),
 (60,'cbm',1),
 (61,'kWs',1),
 (62,'SKE',1),
 (63,'Nm',1),
 (64,'MJ',1),
 (65,'GWh',1),
 (66,'TWh',1),
 (67,'GW',1),
 (68,'TW',1),
 (69,'klux',0),
 (70,'t',0),
 (71,'J/g',0),
 (72,'kg/h',0),
 (73,'var',0),
 (74,'varh',0),
 (75,'VA',0);

UNLOCK TABLES;
/*!40000 ALTER TABLE `T_Units` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
