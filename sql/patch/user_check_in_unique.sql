-- ============================================================
-- 签到防并发: la_user_check_in 增加 check_date 列 + 唯一索引
-- 修复"一天内并发多次签到"漏洞
-- 幂等, 可重复执行
-- ============================================================

-- 1) 增加 check_date 列 (存在则跳过)
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_user_check_in'
    AND COLUMN_NAME = 'check_date'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `la_user_check_in` ADD COLUMN `check_date` DATE NULL COMMENT ''签到日期'' AFTER `create_time`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 回填已有数据的 check_date = DATE(create_time)
UPDATE `la_user_check_in` SET `check_date` = DATE(`create_time`) WHERE `check_date` IS NULL;

-- 3) 去掉同一 (userid, check_date) 的重复行 (保留最小 id 那条)
DELETE t1 FROM `la_user_check_in` t1
INNER JOIN `la_user_check_in` t2
  ON t1.userid = t2.userid
  AND t1.check_date = t2.check_date
  AND t1.id > t2.id;

-- 4) 增加唯一索引 (存在则跳过)
SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'la_user_check_in'
    AND INDEX_NAME = 'uk_userid_check_date'
);
SET @sql := IF(@idx_exists = 0,
  'ALTER TABLE `la_user_check_in` ADD UNIQUE KEY `uk_userid_check_date` (`userid`,`check_date`)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
