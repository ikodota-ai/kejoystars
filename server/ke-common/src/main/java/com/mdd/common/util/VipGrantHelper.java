package com.mdd.common.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.common.entity.RechargeOrder;
import com.mdd.common.entity.user.User;
import com.mdd.common.enums.PaymentEnum;
import com.mdd.common.mapper.RechargeOrderMapper;
import com.mdd.common.mapper.user.UserMapper;

import java.math.BigDecimal;

/**
 * VIP赠送/充值统一处理
 *
 * 统一注册赠送、登录首次赠送、后台手动赠送三处逻辑:
 *   1) 通过 (userId, pay_way=4, pay_type=batchKey) 幂等去重
 *   2) 写入 la_recharge_order 一条记录 (pay_way=4)
 *   3) 有效期: 已开通则从原到期时间叠加, 否则从当前时间起算, vipDownCount=50
 */
public class VipGrantHelper {

    /**
     * 判断用户是否已经领过该批次赠送
     */
    public static boolean hasGranted(RechargeOrderMapper rechargeOrderMapper, Integer userId, String batchKey) {
        if (userId == null || batchKey == null || batchKey.isEmpty()) {
            return false;
        }
        Long count = rechargeOrderMapper.selectCount(new QueryWrapper<RechargeOrder>()
                .eq("user_id", userId)
                .eq("pay_way", PaymentEnum.GIFT_PAY.getCode())
                .eq("pay_type", batchKey)
                .isNull("delete_time"));
        return count != null && count > 0;
    }

    /**
     * 赠送VIP (幂等)
     *
     * @param userMapper           用户Mapper
     * @param rechargeOrderMapper  订单Mapper
     * @param user                 用户对象 (会在方法内更新 vipExpired/vipDownCount 并 updateById)
     * @param days                 赠送天数, <=0 直接返回 false
     * @param batchKey             批次标识 (字典项value, 如 gift_new_user)
     * @return true=已发放, false=未发放(参数无效或已发放过)
     */
    public static boolean grant(UserMapper userMapper,
                                RechargeOrderMapper rechargeOrderMapper,
                                User user,
                                int days,
                                String batchKey) {
        if (user == null || days <= 0 || batchKey == null || batchKey.isEmpty()) {
            return false;
        }
        if (hasGranted(rechargeOrderMapper, user.getId(), batchKey)) {
            return false;
        }

        long now = System.currentTimeMillis() / 1000;
        long addSeconds = 60L * 60 * 24 * days;
        Integer current = user.getVipExpired();
        if (current == null || current <= now) {
            user.setVipExpired((int) (now + addSeconds));
            user.setVipDownCount(50);
        } else {
            user.setVipExpired((int) (current + addSeconds));
            if (user.getVipDownCount() == null || user.getVipDownCount() <= 0 || user.getVipDownCount() > 50) {
                user.setVipDownCount(50);
            }
        }
        user.setUpdateTime(now);
        if (user.getId() != null) {
            userMapper.updateById(user);
        }

        // 写赠送记录
        RechargeOrder order = new RechargeOrder();
        order.setUserId(user.getId());
        order.setRid(0);
        order.setSn(rechargeOrderMapper.randMakeOrderSn("sn"));
        order.setPayWay(PaymentEnum.GIFT_PAY.getCode());
        order.setPayType(batchKey);
        order.setPayStatus(1);
        order.setPayTime(now);
        order.setOrderAmount(new BigDecimal(days));
        order.setOrderTerminal(0);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        rechargeOrderMapper.insert(order);
        return true;
    }

    /**
     * 用户新建流程使用 (User尚未insert, 只调整字段, 由调用方负责insert user;
     * 订单记录会在 user 已入库并拿到 id 后由调用方补写, 见 grantAfterInsert)
     */
    public static void adjustVipExpire(User user, int days) {
        if (user == null || days <= 0) {
            return;
        }
        long now = System.currentTimeMillis() / 1000;
        long addSeconds = 60L * 60 * 24 * days;
        Integer current = user.getVipExpired();
        if (current == null || current <= now) {
            user.setVipExpired((int) (now + addSeconds));
            user.setVipDownCount(50);
        } else {
            user.setVipExpired((int) (current + addSeconds));
            if (user.getVipDownCount() == null || user.getVipDownCount() <= 0 || user.getVipDownCount() > 50) {
                user.setVipDownCount(50);
            }
        }
    }

    /**
     * 写一条赠送记录 (用于新用户注册: adjustVipExpire + insert(user) 后调用)
     */
    public static void writeGiftOrder(RechargeOrderMapper rechargeOrderMapper,
                                       Integer userId, int days, String batchKey) {
        if (userId == null || days <= 0 || batchKey == null || batchKey.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis() / 1000;
        RechargeOrder order = new RechargeOrder();
        order.setUserId(userId);
        order.setRid(0);
        order.setSn(rechargeOrderMapper.randMakeOrderSn("sn"));
        order.setPayWay(PaymentEnum.GIFT_PAY.getCode());
        order.setPayType(batchKey);
        order.setPayStatus(1);
        order.setPayTime(now);
        order.setOrderAmount(new BigDecimal(days));
        order.setOrderTerminal(0);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        rechargeOrderMapper.insert(order);
    }
}
