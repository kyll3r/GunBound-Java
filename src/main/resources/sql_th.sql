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
