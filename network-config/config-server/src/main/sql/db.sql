CREATE DATABASE IF NOT EXISTS DEV DEFAULT CHARACTER SET UTF8;

USE DEV;

CREATE TABLE IF NOT EXISTS `user_info` (
  `urid` int(11) NOT NULL AUTO_INCREMENT,
  `urname` varchar(50) NOT NULL,
  `digest` varchar(50) NOT NULL,
  `urole` varchar(10) NOT NULL DEFAULT 'NODE',
  PRIMARY KEY (`urid`),
  UNIQUE KEY `urname` (`urname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `auth_info` (
  `aurid` int(11) NOT NULL,
  `groupid` int(11) NOT NULL,
  PRIMARY KEY (`aurid`,`groupid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `group_info` (
  `groupid` int(11) NOT NULL AUTO_INCREMENT,
  `groupname` varchar(100) NOT NULL,
  PRIMARY KEY (`groupid`),
  UNIQUE KEY `groupname` (`groupname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `config_info` (
  `groupid` int(11) NOT NULL,
  `ckey` varchar(100) NOT NULL,
  `value` text NOT NULL,
  `curid` int(11) NOT NULL,
  `uurid` int(11) NOT NULL,
  `updatetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`groupid`,`ckey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


