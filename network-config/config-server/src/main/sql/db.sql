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
  PRIMARY KEY (`groupid`,`ckey`),
  KEY `updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `user_info` VALUES (1,'su','e0cd517d1e61d0388d1a71e20a2347fa543a8dcb','ADMIN');
INSERT INTO `user_info` VALUES (2,'node','557789600cf2ad6ae93cc0e6c230fecd053bb22a','NODE');
INSERT INTO `user_info` VALUES (3,'admin','bd9bc3f780be96f03584da249b0439cc36151d0e','OP');
INSERT INTO `user_info` VALUES (4,'op','bd9bc3f780be96f03584da249b0439cc36151d0e','OP');

INSERT INTO `auth_info` VALUES (2,1);
INSERT INTO `auth_info` VALUES (2,2);

INSERT INTO `group_info` VALUES (1,'testme');
INSERT INTO `group_info` VALUES (2,'configserver.test.demo');

INSERT INTO `config_info` VALUES (1,'demo','hello, world!',2,2,'2016-05-20 16:30:49');
INSERT INTO `config_info` VALUES (1,'demo.key','demo.value',2,2,'2016-05-21 16:30:49');
INSERT INTO `config_info` VALUES (1,'line','(C) Copy Right @ All rights reserved.',2,2,'2016-05-22 16:30:49');
INSERT INTO `config_info` VALUES (2,'demo.key','demo.value',2,2,'2012-05-21 16:30:49');

