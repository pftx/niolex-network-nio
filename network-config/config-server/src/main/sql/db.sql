CREATE DATABASE IF NOT EXISTS config_svr DEFAULT CHARACTER SET UTF8;

USE config_svr;

GRANT SELECT,INSERT,UPDATE,DELETE ON config_svr.* TO 'configserver'@'%' IDENTIFIED BY '9df3d178b4c149';

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

INSERT INTO `user_info` VALUES (1,'su','c0b69acfd4a83d695385cee755275037a9b84467','ADMIN');
INSERT INTO `user_info` VALUES (2,'node','47e109e43e482e50f87504263e8dd0073a810856','NODE');
INSERT INTO `user_info` VALUES (3,'admin','a98911f6abcf4d8d52fb6ad5d1d517dad8b73024','OP');
INSERT INTO `user_info` VALUES (4,'op','a98911f6abcf4d8d52fb6ad5d1d517dad8b73024','OP');

