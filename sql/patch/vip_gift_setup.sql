-- ============================================================
-- VIP 赠送: 字典 + 菜单权限 (幂等, 可重复执行)
-- pay_way=4 = 后台赠送 (共享: 注册/首次登录/后台手动赠送)
-- pay_type 字段复用为 batch_key (来自字典 gift_batch)
-- 去重: (user_id, pay_way=4, pay_type=batch_key)
-- ============================================================

-- 1) 字典类型: gift_batch
INSERT INTO `la_dict_type` (`name`, `type`, `status`, `remark`, `create_time`, `update_time`)
SELECT 'VIP赠送批次', 'gift_batch', 1, '控制后台/注册赠送批次, 同用户同批次仅一次', UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_dict_type`) t WHERE t.`type`='gift_batch');

SET @gift_type_id := (SELECT `id` FROM `la_dict_type` WHERE `type`='gift_batch' LIMIT 1);

-- 2) 字典数据: 常见批次(注册赠送/节日福利/版本补偿)
INSERT INTO `la_dict_data` (`type_id`, `type_value`, `name`, `value`, `remark`, `sort`, `status`, `create_time`, `update_time`)
SELECT @gift_type_id, 'gift_batch', '新用户注册赠送', 'gift_new_user', '注册/首次登录自动发放', 1, 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_dict_data`) d WHERE d.`type_value`='gift_batch' AND d.`value`='gift_new_user');

INSERT INTO `la_dict_data` (`type_id`, `type_value`, `name`, `value`, `remark`, `sort`, `status`, `create_time`, `update_time`)
SELECT @gift_type_id, 'gift_batch', '节日福利', 'gift_festival', '节假日福利, 每次新批次请复制一条并改value', 10, 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_dict_data`) d WHERE d.`type_value`='gift_batch' AND d.`value`='gift_festival');

INSERT INTO `la_dict_data` (`type_id`, `type_value`, `name`, `value`, `remark`, `sort`, `status`, `create_time`, `update_time`)
SELECT @gift_type_id, 'gift_batch', '版本升级补偿', 'gift_version_compensate', '版本升级临时补偿', 20, 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_dict_data`) d WHERE d.`type_value`='gift_batch' AND d.`value`='gift_version_compensate');

INSERT INTO `la_dict_data` (`type_id`, `type_value`, `name`, `value`, `remark`, `sort`, `status`, `create_time`, `update_time`)
SELECT @gift_type_id, 'gift_batch', '手动补送(旧用户)', 'gift_manual_legacy', '注册赠送功能上线前的老用户手动补发', 30, 1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_dict_data`) d WHERE d.`type_value`='gift_batch' AND d.`value`='gift_manual_legacy');

-- 3) 菜单权限: user.user/gift (挂在"用户列表"菜单下)
SET @user_menu_id := (SELECT `id` FROM `la_system_menu` WHERE `perms`='user.user/list' LIMIT 1);

INSERT INTO `la_system_menu` (`pid`,`type`,`name`,`icon`,`sort`,`perms`,`paths`,`component`,`selected`,`params`,`is_cache`,`is_show`,`is_disable`,`create_time`,`update_time`)
SELECT IFNULL(@user_menu_id, 0),'A','赠送VIP','',0,'user.user/gift','','','','',1,1,0,UNIX_TIMESTAMP(),UNIX_TIMESTAMP()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `la_system_menu`) m WHERE m.`perms`='user.user/gift');
