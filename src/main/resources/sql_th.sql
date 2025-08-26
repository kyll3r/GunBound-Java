-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               11.7.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Dumping structure for table gbth.chest
CREATE TABLE IF NOT EXISTS `chest` (
  `Idx` int(10) NOT NULL AUTO_INCREMENT,
  `Item` int(11) NOT NULL,
  `Wearing` varchar(1) DEFAULT '0',
  `Acquisition` varchar(1) DEFAULT '0',
  `Expire` datetime DEFAULT NULL,
  `Volume` tinyint(1) DEFAULT NULL,
  `PlaceOrder` varchar(50) DEFAULT '0',
  `Recovered` varchar(50) DEFAULT '0',
  `OwnerId` varchar(16) NOT NULL,
  `ExpireType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Idx`) USING BTREE,
  KEY `OwnerId` (`OwnerId`),
  CONSTRAINT `fk_chest_owner` FOREIGN KEY (`OwnerId`) REFERENCES `user` (`UserId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Itens de inventário do jogador';

-- Dumping data for table gbth.chest: ~5 rows (approximately)
INSERT INTO `chest` (`Idx`, `Item`, `Wearing`, `Acquisition`, `Expire`, `Volume`, `PlaceOrder`, `Recovered`, `OwnerId`, `ExpireType`) VALUES
	(1, 229584, '0', 'C', NULL, 1, '10000', '0', 'kyll3r', 'I'),
	(2, 98305, '1', 'C', NULL, 1, '20000', '0', 'kyll3r', 'I'),
	(3, 32769, '1', 'C', NULL, 1, '0', '0', 'kyll3r', 'I'),
	(4, 204821, '1', 'C', NULL, 1, '0', '0', 'kyll3r', 'I'),
	(5, 204822, '1', 'C', NULL, 1, '0', '0', 'kyll3r', 'I');

-- Dumping structure for table gbth.game
CREATE TABLE IF NOT EXISTS `game` (
  `UserId` varchar(16) NOT NULL,
  `NickName` varchar(16) NOT NULL DEFAULT '',
  `Guild` varchar(8) NOT NULL DEFAULT '',
  `GuildRank` int(11) NOT NULL DEFAULT 0,
  `MemberGuildCount` smallint(6) NOT NULL DEFAULT 0,
  `Gold` int(10) unsigned NOT NULL DEFAULT 0,
  `Cash` int(10) unsigned NOT NULL DEFAULT 0,
  `EventScore0` int(11) NOT NULL DEFAULT 0,
  `EventScore1` int(11) NOT NULL DEFAULT 0,
  `EventScore2` int(11) NOT NULL DEFAULT 0,
  `EventScore3` int(11) NOT NULL DEFAULT 0,
  `Prop1` varchar(201) NOT NULL DEFAULT '',
  `Prop2` varchar(201) NOT NULL DEFAULT '',
  `AdminGift` smallint(6) NOT NULL DEFAULT 0,
  `TotalScore` int(11) NOT NULL DEFAULT 1000,
  `SeasonScore` int(11) NOT NULL DEFAULT 1000,
  `TotalGrade` smallint(6) NOT NULL DEFAULT 19,
  `SeasonGrade` smallint(6) NOT NULL DEFAULT 19,
  `TotalRank` int(11) NOT NULL DEFAULT 0,
  `SeasonRank` int(11) NOT NULL DEFAULT 0,
  `AccumShot` int(10) unsigned NOT NULL DEFAULT 0,
  `AccumDamage` int(10) unsigned NOT NULL DEFAULT 0,
  `LastUpdateTime` timestamp NULL DEFAULT NULL,
  `NoRankUpdate` tinyint(1) NOT NULL DEFAULT 0,
  `ClientData` varbinary(200) DEFAULT NULL,
  `Country` int(11) NOT NULL DEFAULT 0,
  `GiftProhibitTime` timestamp NOT NULL DEFAULT '2000-01-01 08:00:00',
  PRIMARY KEY (`UserId`),
  UNIQUE KEY `NickName_UNIQUE` (`NickName`),
  KEY `UserId` (`UserId`),
  KEY `Guild` (`Guild`),
  CONSTRAINT `game_ibfk_1` FOREIGN KEY (`UserId`) REFERENCES `user` (`UserId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table gbth.game: ~4 rows (approximately)
INSERT INTO `game` (`UserId`, `NickName`, `Guild`, `GuildRank`, `MemberGuildCount`, `Gold`, `Cash`, `EventScore0`, `EventScore1`, `EventScore2`, `EventScore3`, `Prop1`, `Prop2`, `AdminGift`, `TotalScore`, `SeasonScore`, `TotalGrade`, `SeasonGrade`, `TotalRank`, `SeasonRank`, `AccumShot`, `AccumDamage`, `LastUpdateTime`, `NoRankUpdate`, `ClientData`, `Country`, `GiftProhibitTime`) VALUES
	('br', 'br', 'BRLegacy', 1, 1, 1118890, 0, 0, 0, 0, 3, '', '', 0, 2274, 1274, 0, 0, 1, 0, 0, 0, NULL, 1, _binary 0x0000c3bf01c3bf01c3bf0124, 212, '2000-01-01 06:00:00'),
	('kyll3r', 'KyLL3R', 'GBLegacy', 1, 2, 99999999, 9999999, 0, 0, 0, 3, '', '', 0, 2536, 1536, 13, 14, 8, 34, 0, 0, NULL, 1, _binary 0x0800c3bf01c3bf01c3bf0124, 212, '2000-01-01 06:00:00'),
	('test', 'Test', 'TestG', 1, 1, 1121590, 0, 0, 0, 0, 3, '', '', 0, 2341, 1341, 13, 13, 1, 0, 0, 0, NULL, 1, _binary 0x0000c3bf01c3bf01c3bf0124, 212, '2000-01-01 06:00:00'),
	('test1', 'Test1', 'TestG2', 1, 1, 1118890, 0, 0, 0, 0, 3, '', '', 0, 2274, 1274, 13, 13, 1, 0, 0, 0, NULL, 1, _binary 0x0000c3bf01c3bf01c3bf0124, 212, '2000-01-01 06:00:00');

-- Dumping structure for table gbth.user
CREATE TABLE IF NOT EXISTS `user` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `UserId` varchar(16) NOT NULL DEFAULT '',
  `Gender` tinyint(1) NOT NULL DEFAULT 0,
  `Password` varchar(16) NOT NULL DEFAULT '',
  `Status` varchar(10) NOT NULL DEFAULT '',
  `MuteTime` timestamp NULL DEFAULT '2000-01-01 08:00:00',
  `RestrictTime` datetime DEFAULT '2000-01-01 00:00:00',
  `Authority` int(11) NOT NULL DEFAULT 0,
  `Authority2` int(11) NOT NULL DEFAULT 0,
  `AuthorityBackup` int(11) DEFAULT 0,
  `E_Mail` varchar(50) NOT NULL DEFAULT '',
  `Country` int(11) NOT NULL DEFAULT 0,
  `User_Level` int(11) NOT NULL DEFAULT 0,
  `Dia` int(11) DEFAULT 0,
  `Mes` int(11) DEFAULT 0,
  `Ano` int(11) DEFAULT 0,
  `Created` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `user_UNIQUE` (`UserId`),
  KEY `Id` (`Id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table gbth.user: ~4 rows (approximately)
INSERT INTO `user` (`Id`, `UserId`, `Gender`, `Password`, `Status`, `MuteTime`, `RestrictTime`, `Authority`, `Authority2`, `AuthorityBackup`, `E_Mail`, `Country`, `User_Level`, `Dia`, `Mes`, `Ano`, `Created`) VALUES
	(1, 'kyll3r', 0, '1234', '0', '2000-01-01 06:00:00', '2000-01-01 00:00:00', 100, 0, 0, 'kyll3r@live.com', 212, 1, 0, 0, 0, NULL),
	(2, 'test', 1, '1234', '0', '2000-01-01 06:00:00', '2000-01-01 00:00:00', 100, 0, 0, 'test@test.com', 212, 1, 0, 0, 0, NULL),
	(3, 'test1', 0, '1234', '0', '2000-01-01 06:00:00', '2000-01-01 00:00:00', 100, 0, 0, 'test1@test.com', 212, 1, 0, 0, 0, NULL),
	(4, 'br', 1, 'br', '0', '2000-01-01 06:00:00', '2000-01-01 00:00:00', 100, 0, 0, 'br@test.com', 212, 1, 0, 0, 0, NULL);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               11.7.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Dumping structure for table gbth.chest
CREATE TABLE IF NOT EXISTS `chest` (
  `Idx` int(10) NOT NULL AUTO_INCREMENT,
  `Item` int(11) NOT NULL,
  `Wearing` varchar(1) DEFAULT '0',
  `Acquisition` varchar(1) DEFAULT '0',
  `Expire` datetime DEFAULT NULL,
  `Volume` tinyint(1) DEFAULT NULL,
  `PlaceOrder` varchar(50) DEFAULT '0',
  `Recovered` varchar(50) DEFAULT '0',
  `OwnerId` varchar(16) NOT NULL,
  `ExpireType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`Idx`) USING BTREE,
  KEY `OwnerId` (`OwnerId`),
  CONSTRAINT `fk_chest_owner` FOREIGN KEY (`OwnerId`) REFERENCES `user` (`UserId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Itens de inventário do jogador';

-- Dumping data for table gbth.chest: ~12 rows (approximately)
INSERT INTO `chest` (`Idx`, `Item`, `Wearing`, `Acquisition`, `Expire`, `Volume`, `PlaceOrder`, `Recovered`, `OwnerId`, `ExpireType`) VALUES
	(1, 98399, '0', 'C', NULL, 1, '0', '0', 'kyll3r', 'I'),
	(2, 98305, '1', 'C', NULL, 1, '10000', '0', 'kyll3r', 'I'),
	(3, 32769, '1', 'C', NULL, 1, '20000', '0', 'kyll3r', 'I'),
	(33, 204801, '0', 'C', '2025-08-26 22:45:45', 1, '30000', '0', 'kyll3r', 'I'),
	(45, 32786, '0', 'G', NULL, 1, '40000', '0', 'kyll3r', 'I'),
	(46, 32861, '0', 'G', NULL, 1, '50000', '0', 'kyll3r', 'I'),
	(47, 98305, '0', 'C', NULL, 1, '10000', '0', 'test1', 'I'),
	(48, 32769, '0', 'C', NULL, 1, '20000', '0', 'test1', 'I'),
	(49, 32769, '0', 'C', NULL, 1, '20000', '0', 'br', 'I'),
	(50, 98305, '0', 'C', NULL, 1, '10000', '0', 'br', 'I'),
	(51, 32769, '1', 'C', NULL, 1, '20000', '0', 'test', 'I'),
	(52, 98305, '1', 'C', NULL, 1, '10000', '0', 'test', 'I');

-- Dumping structure for table gbth.menu
CREATE TABLE IF NOT EXISTS `menu` (
  `Idx` int(11) NOT NULL AUTO_INCREMENT,
  `No` int(11) NOT NULL,
  `ItemCount` int(11) DEFAULT 0,
  `Item1` int(11) DEFAULT NULL,
  `Period1` int(10) unsigned DEFAULT NULL,
  `Volume1` int(10) unsigned DEFAULT NULL,
  `Item2` int(11) DEFAULT NULL,
  `Period2` int(10) unsigned DEFAULT NULL,
  `Volume2` int(10) unsigned DEFAULT NULL,
  `Item3` int(11) DEFAULT NULL,
  `Period3` int(10) unsigned DEFAULT NULL,
  `Volume3` int(10) unsigned DEFAULT NULL,
  `Item4` int(11) DEFAULT NULL,
  `Period4` int(10) unsigned DEFAULT NULL,
  `Volume4` int(10) unsigned DEFAULT NULL,
  `Item5` int(11) DEFAULT NULL,
  `Period5` int(10) unsigned DEFAULT NULL,
  `Volume5` int(10) unsigned DEFAULT NULL,
  `ImgNo` int(21) DEFAULT 0,
  `ImgShop` varchar(255) DEFAULT NULL,
  `Menu_Name` varchar(40) NOT NULL DEFAULT '?',
  `Menu_Desc` varchar(255) NOT NULL DEFAULT '?',
  `Menu_Image` varchar(255) NOT NULL DEFAULT '?',
  `Genero` varchar(11) DEFAULT NULL,
  `Part` varchar(10) DEFAULT NULL,
  `Delay` int(2) DEFAULT 0,
  `Popularity` int(2) DEFAULT 0,
  `Attack` int(2) DEFAULT 0,
  `Defense` int(2) DEFAULT 0,
  `Energy` int(2) DEFAULT 0,
  `Shield_Recovery` int(2) DEFAULT 0,
  `Item_Skip_Delay` int(2) DEFAULT 0,
  `Pit_Angle` int(2) DEFAULT 0,
  `Is_New` int(3) DEFAULT 0,
  `Is_Visible` tinyint(1) DEFAULT 0,
  `Seal_Enchant` tinyint(1) DEFAULT 0,
  `Can_Gift` tinyint(1) DEFAULT 0,
  `Can_Stack` tinyint(1) DEFAULT 0,
  `Color` int(3) DEFAULT 0,
  `ExType` int(1) DEFAULT NULL,
  `PlaceOrder` int(11) DEFAULT 0,
  `ShopOption1` tinyint(2) DEFAULT 0,
  `ShopOption2` tinyint(2) DEFAULT 0,
  `ShopOption3` tinyint(3) DEFAULT 0,
  `PriceByCashForH` int(10) DEFAULT NULL,
  `PriceByCashForD` int(10) DEFAULT NULL,
  `PriceByCashForW` int(10) unsigned DEFAULT NULL,
  `PriceByCashForM` int(10) unsigned DEFAULT NULL,
  `PriceByCashForY` int(10) unsigned DEFAULT NULL,
  `PriceByCashForI` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForH` int(10) DEFAULT NULL,
  `PriceByGoldForD` int(10) DEFAULT NULL,
  `PriceByGoldForW` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForM` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForY` int(10) unsigned DEFAULT NULL,
  `PriceByGoldForI` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForW` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForM` int(10) unsigned DEFAULT NULL,
  `PriceByGCoinForI` int(10) unsigned DEFAULT NULL,
  `Enable_2Hour` int(1) DEFAULT 0,
  `Enable_Day` int(1) DEFAULT 0,
  `Enable_Week` int(1) DEFAULT 0,
  `Enable_Moth` int(1) DEFAULT 0,
  `Enable_Unlimited` int(1) DEFAULT 0,
  `Enable_Gold` int(1) DEFAULT 0,
  `Enable_Cash` int(1) DEFAULT 0,
  `Enable_GCoin` int(1) DEFAULT 0,
  `Type` int(2) DEFAULT 0,
  `Wearable` int(2) DEFAULT 0,
  `Location` int(3) DEFAULT NULL,
  `Dat` int(2) DEFAULT 0,
  PRIMARY KEY (`Idx`),
  UNIQUE KEY `No` (`No`)
) ENGINE=MyISAM AUTO_INCREMENT=118 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=DYNAMIC;

-- Dumping data for table gbth.menu: 117 rows
/*!40000 ALTER TABLE `menu` DISABLE KEYS */;
INSERT INTO `menu` (`Idx`, `No`, `ItemCount`, `Item1`, `Period1`, `Volume1`, `Item2`, `Period2`, `Volume2`, `Item3`, `Period3`, `Volume3`, `Item4`, `Period4`, `Volume4`, `Item5`, `Period5`, `Volume5`, `ImgNo`, `ImgShop`, `Menu_Name`, `Menu_Desc`, `Menu_Image`, `Genero`, `Part`, `Delay`, `Popularity`, `Attack`, `Defense`, `Energy`, `Shield_Recovery`, `Item_Skip_Delay`, `Pit_Angle`, `Is_New`, `Is_Visible`, `Seal_Enchant`, `Can_Gift`, `Can_Stack`, `Color`, `ExType`, `PlaceOrder`, `ShopOption1`, `ShopOption2`, `ShopOption3`, `PriceByCashForH`, `PriceByCashForD`, `PriceByCashForW`, `PriceByCashForM`, `PriceByCashForY`, `PriceByCashForI`, `PriceByGoldForH`, `PriceByGoldForD`, `PriceByGoldForW`, `PriceByGoldForM`, `PriceByGoldForY`, `PriceByGoldForI`, `PriceByGCoinForW`, `PriceByGCoinForM`, `PriceByGCoinForI`, `Enable_2Hour`, `Enable_Day`, `Enable_Week`, `Enable_Moth`, `Enable_Unlimited`, `Enable_Gold`, `Enable_Cash`, `Enable_GCoin`, `Type`, `Wearable`, `Location`, `Dat`) VALUES
	(1, 32768, 0, 32768, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Standard', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(2, 32842, 0, 32842, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Skeleton', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 22000, NULL, NULL, 0, 0, NULL, 220000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(3, 32882, 0, 32882, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Apprentice Wizard', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(4, 32883, 0, 32883, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Heaven Angel', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 11000, NULL, NULL, 0, 0, NULL, 110000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(5, 32769, 0, 32769, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Space Marine', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 2000, NULL, NULL, 0, 0, NULL, 20000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(6, 32880, 0, 32880, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Priest', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 80000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(7, 32881, 0, 32881, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Leaf Warrior', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 10000, NULL, NULL, 0, 0, NULL, 100000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(8, 32770, 0, 32770, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'School Uniform', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 1000, NULL, NULL, 0, 0, NULL, 10000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(9, 32868, 0, 32868, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Medic', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(10, 32869, 0, 32869, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Cyber Rabbit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(11, 32771, 0, 32771, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Sholuder Strap', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 1200, NULL, NULL, 0, 0, NULL, 12000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(12, 32772, 0, 32772, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Pirate Captain', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(13, 32773, 0, 32773, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Roman General', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4000, NULL, NULL, 0, 0, NULL, 40000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(14, 32774, 0, 32774, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Light Armor', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3000, NULL, NULL, 0, 0, NULL, 30000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(15, 32775, 0, 32775, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Heavy Armor', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3500, NULL, NULL, 0, 0, NULL, 35000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(16, 32776, 0, 32776, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Space Suit A', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(17, 32777, 0, 32777, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Red Devil', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 1500, NULL, NULL, 0, 0, NULL, 15000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(18, 32778, 0, 32778, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Rider Uniform', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3800, NULL, NULL, 0, 0, NULL, 38000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(19, 32779, 0, 32779, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Navy Uniform', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4500, NULL, NULL, 0, 0, NULL, 45000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(20, 32780, 0, 32780, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Cowboy', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5000, NULL, NULL, 0, 0, NULL, 50000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(21, 32781, 0, 32781, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Pharaoh', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 10000, NULL, NULL, 0, 0, NULL, 100000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(22, 32782, 0, 32782, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Swim Suit A', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 800, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(23, 32783, 0, 32783, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'RobinHood', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3000, NULL, NULL, 0, 0, NULL, 30000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(24, 32784, 0, 32784, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Safari Wear', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3800, NULL, NULL, 0, 0, NULL, 38000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(25, 32785, 0, 32785, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Arabian Clothes', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4200, NULL, NULL, 0, 0, NULL, 42000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(26, 32786, 0, 32786, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Arch Angel', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 20000, NULL, NULL, 0, 0, NULL, 200000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(27, 32787, 0, 32787, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'FrendShip A', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(28, 32788, 0, 32788, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Korean Wear A', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3500, NULL, NULL, 0, 0, NULL, 35000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(29, 32789, 0, 32789, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'GreatKing', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4000, NULL, NULL, 0, 0, NULL, 40000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(30, 32790, 0, 32790, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Warriror A', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5000, NULL, NULL, 0, 0, NULL, 50000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(31, 32791, 0, 32791, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Chinese Clothes', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3500, NULL, NULL, 0, 0, NULL, 35000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(32, 32792, 0, 32792, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Ranger Clothes', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5000, NULL, NULL, 0, 0, NULL, 50000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(33, 32793, 0, 32793, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Thanksgiving', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 2500, NULL, NULL, 0, 0, NULL, 25000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(34, 32794, 0, 32794, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Magician A', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4000, NULL, NULL, 0, 0, NULL, 40000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(35, 32795, 0, 32795, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Indian Wear', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5000, NULL, NULL, 0, 0, NULL, 50000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(36, 32796, 0, 32796, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Gladiator', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5000, NULL, NULL, 0, 0, NULL, 50000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(37, 32797, 0, 32797, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Hawaiian Wear', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4000, NULL, NULL, 0, 0, NULL, 40000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(38, 32798, 0, 32798, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Devil Clothes', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(39, 32799, 0, 32799, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Mechanic Armor', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(40, 32800, 0, 32800, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Fabre Clothes', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6800, NULL, NULL, 0, 0, NULL, 68000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(41, 32801, 0, 32801, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Officer Uniform', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(42, 32802, 0, 32802, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'SWAT Combat', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7500, NULL, NULL, 0, 0, NULL, 75000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(43, 32803, 0, 32803, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Ballroom Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(44, 32804, 0, 32804, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'High School Band', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(45, 32805, 0, 32805, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Santa Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 1500, NULL, NULL, 0, 0, NULL, 15000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(46, 32806, 0, 32806, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Boarder Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7500, NULL, NULL, 0, 0, NULL, 75000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(47, 32807, 0, 32807, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Golden Armor', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 25000, NULL, NULL, 0, 0, NULL, 250000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(48, 32808, 0, 32808, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Indian Wear2', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5500, NULL, NULL, 0, 0, NULL, 55000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(49, 32809, 0, 32809, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Indiana Wear', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(50, 32810, 0, 32810, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Tuxedo', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 15000, NULL, NULL, 0, 0, NULL, 150000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(51, 32811, 0, 32811, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Duke Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(52, 32812, 0, 32812, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Raincoat', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3600, NULL, NULL, 0, 0, NULL, 36000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(53, 32813, 0, 32813, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Baseball Jacket', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3800, NULL, NULL, 0, 0, NULL, 38000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(54, 32814, 0, 32814, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Wood body', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4000, NULL, NULL, 0, 0, NULL, 40000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(55, 32815, 0, 32815, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Mummy Clothes', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8500, NULL, NULL, 0, 0, NULL, 85000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(56, 32816, 0, 32816, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Clown Custom C', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(57, 32817, 0, 32817, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Hip-Hop Wear', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3800, NULL, NULL, 0, 0, NULL, 38000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(58, 32818, 0, 32818, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Indian Fighter', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7500, NULL, NULL, 0, 0, NULL, 75000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(59, 32819, 0, 32819, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Street Fighter', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 3500, NULL, NULL, 0, 0, NULL, 35000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(60, 32820, 0, 32820, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Black Cloak', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(61, 32821, 0, 32821, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Road Shooter', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4500, NULL, NULL, 0, 0, NULL, 45000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(62, 32822, 0, 32822, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Military Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4500, NULL, NULL, 0, 0, NULL, 45000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(63, 32823, 0, 32823, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Flamingo', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(64, 32824, 0, 32824, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Black Dress', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5000, NULL, NULL, 0, 0, NULL, 50000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(65, 32825, 0, 32825, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Disco Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4000, NULL, NULL, 0, 0, NULL, 40000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(66, 32826, 0, 32826, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Orange Blade', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(67, 32827, 0, 32827, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Red Devil', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 4000, NULL, NULL, 0, 0, NULL, 40000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(68, 32828, 0, 32828, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Black coat', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(69, 32829, 0, 32829, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, '.', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(70, 32830, 0, 32830, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, '.', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(71, 32831, 0, 32831, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, '.', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(72, 32832, 0, 32832, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Samurai Clothing', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8200, NULL, NULL, 0, 0, NULL, 82000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(73, 32833, 0, 32833, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Muaythai Clothing', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7800, NULL, NULL, 0, 0, NULL, 78000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(74, 32834, 0, 32834, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Elf Clothes A', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9200, NULL, NULL, 0, 0, NULL, 92000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(75, 32835, 0, 32835, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Ice Hockey Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 11000, NULL, NULL, 0, 0, NULL, 110000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(76, 32836, 0, 32836, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Rocker Suit', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9000, NULL, NULL, 0, 0, NULL, 90000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(77, 32837, 0, 32837, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Kendo Uniform', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 12000, NULL, NULL, 0, 0, NULL, 120000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(78, 32838, 0, 32838, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Fireman Uniform', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 12000, NULL, NULL, 0, 0, NULL, 120000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(79, 32839, 0, 32839, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Panda Clothes', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9500, NULL, NULL, 0, 0, NULL, 95000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(80, 32840, 0, 32840, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Great Devil Clothing', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 14500, NULL, NULL, 0, 0, NULL, 145000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(81, 32841, 0, 32841, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Frankenstein', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7500, NULL, NULL, 0, 0, NULL, 75000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(82, 32843, 0, 32843, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Angel of Death', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9500, NULL, NULL, 0, 0, NULL, 95000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(83, 32844, 0, 32844, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Metallic Armour', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8500, NULL, NULL, 0, 0, NULL, 85000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(84, 32845, 0, 32845, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Scarecrow Boy', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 5000, NULL, NULL, 0, 0, NULL, 50000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(85, 32846, 0, 32846, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Creature Violet', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 140000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(86, 32847, 0, 32847, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Prince Mermaid', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9000, NULL, NULL, 0, 0, NULL, 90000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(87, 32848, 0, 32848, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Ocean King', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 80000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(88, 32849, 0, 32849, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Dracula', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(89, 32850, 0, 32850, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Viking', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 10000, NULL, NULL, 0, 0, NULL, 100000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(90, 32851, 0, 32851, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Plumpy', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(91, 32852, 0, 32852, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Medieval Clothing', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(92, 32853, 0, 32853, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Latin Clothing', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 11000, NULL, NULL, 0, 0, NULL, 110000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(93, 32854, 0, 32854, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Musketeer', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 12000, NULL, NULL, 0, 0, NULL, 120000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(94, 32855, 0, 32855, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Persian king', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9000, NULL, NULL, 0, 0, NULL, 90000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(95, 32856, 0, 32856, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Arabian Prince', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 11500, NULL, NULL, 0, 0, NULL, 115000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(96, 32857, 0, 32857, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Shamanist', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9500, NULL, NULL, 0, 0, NULL, 95000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(97, 32858, 0, 32858, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Indian Chief(B)', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 25000, NULL, NULL, 0, 0, NULL, 250000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(98, 32859, 0, 32859, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Imperial Guards', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(99, 32860, 0, 32860, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'The Great General', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 18000, NULL, NULL, 0, 0, NULL, 180000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(100, 32861, 0, 32861, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'General', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 12000, NULL, NULL, 0, 0, NULL, 120000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(101, 32862, 0, 32862, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Kangsi', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 12500, NULL, NULL, 0, 0, NULL, 125000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(102, 32863, 0, 32863, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Archangel II', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 22000, NULL, NULL, 0, 0, NULL, 220000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(103, 32864, 0, 32864, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Insect Warrior', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 18000, NULL, NULL, 0, 0, NULL, 180000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(104, 32865, 0, 32865, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Black Wizard', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 9500, NULL, NULL, 0, 0, NULL, 95000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(105, 32866, 0, 32866, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Anubis', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(106, 32867, 0, 32867, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Snowman', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 999999, NULL, NULL, 0, 0, NULL, 9999990, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(107, 32870, 0, 32870, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Beast King', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 999999, NULL, NULL, 0, 0, NULL, 9999990, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(108, 32871, 0, 32871, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Moth Warrior', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 999999, NULL, NULL, 0, 0, NULL, 9999990, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(109, 32872, 0, 32872, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Ski Costume', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7500, NULL, NULL, 0, 0, NULL, 75000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(110, 32873, 0, 32873, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Boy\'s fur coat ', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 80000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(111, 32874, 0, 32874, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Cat Tuxedo', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 80000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(112, 32875, 0, 32875, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Puppy Tuxedo', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 80000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(113, 32876, 0, 32876, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, '??? ????', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 7000, NULL, NULL, 0, 0, NULL, 70000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(114, 32877, 0, 32877, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Baseball Style', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 6000, NULL, NULL, 0, 0, NULL, 60000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(115, 32878, 0, 32878, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Easter Bunny', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 80000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(116, 32879, 0, 32879, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Easter Bunny', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 80000, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0),
	(117, 204804, 0, 204804, 86400, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 'Easter Bunny', '?', '?', NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0, 0, 0, 0, NULL, NULL, 0, 0, NULL, 8000, NULL, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, NULL, 0);
/*!40000 ALTER TABLE `menu` ENABLE KEYS */;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
