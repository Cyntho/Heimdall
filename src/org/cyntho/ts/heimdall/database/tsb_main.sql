-- phpMyAdmin SQL Dump
-- version 4.7.4
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Erstellungszeit: 29. Dez 2017 um 14:21
-- Server-Version: 10.1.25-MariaDB
-- PHP-Version: 5.6.31

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `teamspeak_heimdall`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_perm_group_list`
--
-- Erstellt am: 01. Nov 2017 um 18:21
--

CREATE TABLE IF NOT EXISTS `tsb_perm_group_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rank` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) DEFAULT NULL,
  `descr` varchar(255) DEFAULT NULL,
  `bot_shutdown` tinyint(4) NOT NULL DEFAULT '0',
  `bot_restart` tinyint(4) NOT NULL DEFAULT '0',
  `bot_feature` tinyint(4) NOT NULL DEFAULT '0',
  `help` tinyint(4) NOT NULL DEFAULT '0',
  `info` tinyint(4) NOT NULL DEFAULT '0',
  `version` tinyint(4) NOT NULL DEFAULT '0',
  `move_multi` tinyint(4) NOT NULL DEFAULT '0',
  `multi_private` tinyint(4) NOT NULL DEFAULT '0',
  `multi_poke` tinyint(4) NOT NULL DEFAULT '0',
  `multi_kick` tinyint(4) NOT NULL DEFAULT '0',
  `stat_high` tinyint(4) NOT NULL DEFAULT '0',
  `stat_low` tinyint(4) NOT NULL DEFAULT '0',
  `promote` tinyint(4) NOT NULL DEFAULT '0',
  `demote` tinyint(4) NOT NULL DEFAULT '0',
  `write_as_bot` tinyint(4) NOT NULL DEFAULT '0',
  `poke_as_bot` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ranking` (`rank`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_perm_group_members`
--
-- Erstellt am: 01. Nov 2017 um 13:44
--

CREATE TABLE IF NOT EXISTS `tsb_perm_group_members` (
  `uuid` varchar(255) NOT NULL,
  `groupId` int(11) NOT NULL,
  PRIMARY KEY (`uuid`),
  UNIQUE KEY `uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_perm_special`
--
-- Erstellt am: 02. Dez 2017 um 14:58
--

CREATE TABLE IF NOT EXISTS `tsb_perm_special` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `bot_shutdown` tinyint(4) NOT NULL DEFAULT '0',
  `bot_restart` tinyint(4) NOT NULL DEFAULT '0',
  `bot_feature` tinyint(4) NOT NULL DEFAULT '0',
  `help` tinyint(4) NOT NULL DEFAULT '0',
  `info` tinyint(4) NOT NULL DEFAULT '0',
  `version` tinyint(4) NOT NULL DEFAULT '0',
  `move_multi` tinyint(4) NOT NULL DEFAULT '0',
  `multi_private` tinyint(4) NOT NULL DEFAULT '0',
  `multi_poke` tinyint(4) NOT NULL DEFAULT '0',
  `multi_kick` tinyint(4) NOT NULL DEFAULT '0',
  `stat_high` tinyint(4) NOT NULL DEFAULT '0',
  `stat_low` tinyint(4) NOT NULL DEFAULT '0',
  `promote` tinyint(4) NOT NULL DEFAULT '0',
  `demote` tinyint(4) NOT NULL DEFAULT '0',
  `write_as_bot` tinyint(4) NOT NULL DEFAULT '0',
  `poke_as_bot` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_user_history_login`
--
-- Erstellt am: 08. Aug 2017 um 14:30
--

CREATE TABLE IF NOT EXISTS `tsb_user_history_login` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(225) NOT NULL,
  `login` bigint(20) NOT NULL DEFAULT '0',
  `logout` bigint(20) NOT NULL DEFAULT '0',
  `ip` varchar(255) NOT NULL,
  `version` varchar(56) NOT NULL,
  `connections` int(11) NOT NULL,
  `os` varchar(256) NOT NULL,
  `country` varchar(256) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_user_history_nickname`
--
-- Erstellt am: 08. Aug 2017 um 20:43
--

CREATE TABLE IF NOT EXISTS `tsb_user_history_nickname` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `name_sc` varchar(255) NOT NULL,
  `counter` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_user_list`
--
-- Erstellt am: 07. Aug 2017 um 12:37
--

CREATE TABLE IF NOT EXISTS `tsb_user_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `loginFirst` int(20) NOT NULL,
  `loginLatest` int(20) NOT NULL,
  `loginChannel` int(11) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  `nickname` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_wm_list`
--
-- Erstellt am: 30. Okt 2017 um 16:02
--

CREATE TABLE IF NOT EXISTS `tsb_wm_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `refValue` varchar(255) NOT NULL DEFAULT '0',
  `refType` int(11) NOT NULL DEFAULT '0',
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `msgPoke` text,
  `msgServer` text,
  `msgPrivate` text,
  `msgChannel` text,
  `isPoke` tinyint(4) NOT NULL DEFAULT '0',
  `isServer` tinyint(4) NOT NULL DEFAULT '0',
  `isPrivate` tinyint(4) NOT NULL DEFAULT '0',
  `isChannel` tinyint(4) NOT NULL DEFAULT '0',
  `limitType` int(11) NOT NULL DEFAULT '0',
  `limitation` int(20) NOT NULL DEFAULT '0',
  `active` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `tsb_wm_ref`
--
-- Erstellt am: 07. Aug 2017 um 12:12
--

CREATE TABLE IF NOT EXISTS `tsb_wm_ref` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `msgid` int(11) NOT NULL,
  `uuid` varchar(255) NOT NULL,
  `remaining` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
