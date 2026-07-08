-- ============================================================
-- APP版本管理：补建表 + 补菜单（幂等，可重复执行）
-- 适用于已升级的旧库（未导入 install.sql 中新增部分）
-- 表前缀: la_
-- ============================================================

-- 1) 建表（存在则不动，保留已有数据）
CREATE TABLE IF NOT EXISTS `la_app_version` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `platform` varchar(16) NOT NULL DEFAULT '' COMMENT '平台: android=安卓, ios=苹果',
  `channel` varchar(32) NOT NULL DEFAULT 'production' COMMENT '通道: production/beta/internal',
  `version_name` varchar(32) NOT NULL DEFAULT '' COMMENT '版本名称, 如1.4.2',
  `version_code` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '版本号(整数, 用于比较)',
  `min_version_code` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '最低兼容版本号, 低于该值强制更新',
  `is_force` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '是否强制更新: 0=否, 1=是',
  `bundle_id` varchar(128) NOT NULL DEFAULT '' COMMENT '包名/BundleId',
  `install_url` varchar(1024) NOT NULL DEFAULT '' COMMENT '安装地址: iOS为itms-services或分发平台链接, Android为apk直链',
  `download_url` varchar(1024) NOT NULL DEFAULT '' COMMENT '下载地址: apk/ipa原始文件直链',
  `package_size` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '安装包大小(字节)',
  `package_md5` varchar(64) NOT NULL DEFAULT '' COMMENT '安装包MD5',
  `release_note` text COMMENT '更新说明',
  `status` varchar(16) NOT NULL DEFAULT 'draft' COMMENT '状态: draft=草稿, online=上线, offline=下线',
  `gray_percent` int(10) unsigned NOT NULL DEFAULT '100' COMMENT '灰度百分比(0-100)',
  `publish_time` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '发布时间',
  `create_time` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `update_time` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  `delete_time` int(10) unsigned DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_platform_channel_code` (`platform`,`channel`,`version_code`) USING BTREE,
  KEY `idx_online` (`platform`,`channel`,`status`,`publish_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP版本发布表';

-- 2) 父菜单：不存在才插入
INSERT INTO `la_system_menu`
  (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
SELECT 0,'C','APP版本','el-icon-Cellphone',50,'app.version/list','appVersion','app/version/index','','',0,1,0,1751846400,1751846400
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_system_menu`) m WHERE m.`perms`='app.version/list');

-- 3) 取父菜单 id
SET @pid := (SELECT `id` FROM `la_system_menu` WHERE `perms`='app.version/list' LIMIT 1);

-- 4) 按钮权限（逐条判断）
INSERT INTO `la_system_menu` (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
SELECT @pid,'A','详情','',0,'app.version/detail','','','','',1,1,0,1751846400,1751846400
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_system_menu`) m WHERE m.`perms`='app.version/detail');

INSERT INTO `la_system_menu` (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
SELECT @pid,'A','新增','',0,'app.version/add','','','','',1,1,0,1751846400,1751846400
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_system_menu`) m WHERE m.`perms`='app.version/add');

INSERT INTO `la_system_menu` (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
SELECT @pid,'A','编辑','',0,'app.version/edit','','','','',1,1,0,1751846400,1751846400
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_system_menu`) m WHERE m.`perms`='app.version/edit');

INSERT INTO `la_system_menu` (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
SELECT @pid,'A','删除','',0,'app.version/del','','','','',1,1,0,1751846400,1751846400
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_system_menu`) m WHERE m.`perms`='app.version/del');

INSERT INTO `la_system_menu` (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
SELECT @pid,'A','发布','',0,'app.version/publish','','','','',1,1,0,1751846400,1751846400
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_system_menu`) m WHERE m.`perms`='app.version/publish');
