SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `T_User`
-- ----------------------------
DROP TABLE IF EXISTS `T_User`;
CREATE TABLE `T_User` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL DEFAULT '',
  `password` varchar(255) NOT NULL DEFAULT '',
  `age` int(11) NOT NULL DEFAULT '0',
  `old_address` varchar(255) NOT NULL DEFAULT '',
  `now_address` varchar(255) NOT NULL DEFAULT '',
  `state` tinyint(4) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `T_User`
-- ----------------------------
BEGIN;
INSERT INTO `T_User` VALUES ('1', 'test', 'test', '22', 'hz', 'sh', '0', '2017-08-05 13:54:42', '2017-08-05 13:54:42'), ('2', 'twogoods', 'twogoods', '26', '杭州', '上海', '0', '2017-08-05 13:54:53', '2017-08-05 13:55:08');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
