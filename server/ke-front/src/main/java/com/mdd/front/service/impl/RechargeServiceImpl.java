package com.mdd.front.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.RechargeOrder;
import com.mdd.common.entity.user.User;
import com.mdd.common.enums.AccountLogEnum;
import com.mdd.common.enums.PaymentEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.RechargeOrderMapper;
import com.mdd.common.mapper.log.UserAccountLogMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.util.ConfigUtils;
import com.mdd.common.util.StringUtils;
import com.mdd.common.util.TimeUtils;
import com.mdd.front.LikeFrontThreadLocal;
import com.mdd.front.service.IPayService;
import com.mdd.front.service.IRechargeService;
import com.mdd.front.service.IUserAccountLogService;
import com.mdd.front.validate.PaymentValidate;
import com.mdd.front.validate.RechargeValidate;
import com.mdd.front.validate.common.PageValidate;
import com.mdd.front.vo.RechargeConfigVo;
import com.mdd.front.vo.RechargeRecordVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 充值余额服务实现类
 */
@Service
public class RechargeServiceImpl implements IRechargeService {

    @Resource
    UserAccountLogMapper userAccountLogMapper;
    @Resource
    RechargeOrderMapper rechargeOrderMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    IPayService iPayService;

    /**
     * 充值配置
     *
     * @author fzr
     *  @param userId 用户ID
     * @return RechargeConfigVo
     */
    @Override
    public RechargeConfigVo config(Integer userId) {
        User user = userMapper.selectById(userId);
        Map<String, String> config = ConfigUtils.get("recharge");

        RechargeConfigVo vo = new RechargeConfigVo();
        vo.setStatus(Integer.parseInt(config.getOrDefault("status", "0")));
        vo.setMinAmount(new BigDecimal(config.getOrDefault("min_amount", "0")));
        vo.setUserMoney(user.getUserMoney());
        return vo;
    }

    /**
     * 充值记录
     *
     * @author fzr
     * @param userId 用户ID
     * @param pageValidate 分页参数
     * @return PageResult<RechargeRecordVo>
     */
    @Override
    public PageResult<RechargeRecordVo> record(Integer userId, PageValidate pageValidate) {
        Integer pageNo   = pageValidate.getPage_no();
        Integer pageSize = pageValidate.getPage_size();

        QueryWrapper<RechargeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("pay_status", PaymentEnum.OK_PAID.getCode());
        // C端"充值记录"不展示后台赠送订单, 避免"充值30元"文案误导
        queryWrapper.ne("pay_way", PaymentEnum.GIFT_PAY.getCode());
        queryWrapper.orderByDesc("id");

        IPage<RechargeOrder> iPage = rechargeOrderMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);

        List<RechargeRecordVo> list = new LinkedList<>();
        for (RechargeOrder rechargeOrder : iPage.getRecords()) {
            RechargeRecordVo vo = new RechargeRecordVo();
            vo.setId(rechargeOrder.getId());
            vo.setRid( rechargeOrder.getRid() );
            vo.setPayType( rechargeOrder.getPayType() );
            vo.setPayStatus(rechargeOrder.getPayStatus());
            vo.setAction(1);
            vo.setOrderAmount(rechargeOrder.getOrderAmount());
            vo.setCreateTime(TimeUtils.timestampToDate(rechargeOrder.getPayTime()));
            vo.setTips("充值" + vo.getOrderAmount() + "元");
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 创建充值订单
     *
     * @author fzr
     * @param userId 用户ID
     * @param terminal 设备端
     * @param rechargeValidate 参数
     * @return Map<String, Object>
     */
    @Override
    @Transactional
    public Map<String, Object> placeOrder(Integer userId, Integer terminal, RechargeValidate rechargeValidate) {
        RechargeConfigVo config = this.config(userId);
        if (config.getStatus().equals(0)) {
            throw new OperateException("充值功能已关闭");
        }

//        if (rechargeValidate.getId().compareTo(config.getMinAmount()) < 0) {
//            throw new OperateException("充值金额不能少于" + config.getMinAmount());
//        }
        rechargeValidate.setMoney( null );
        //通过ID获取充值金额
        String r = ConfigUtils.get("recharge", "list");
        if (!r.isEmpty()) {
            JSONArray rConfig = JSONArray.parseArray(r);
            for (int i=0; i < rConfig.size(); i++) {
                JSONObject item = rConfig.getJSONObject(i);
                if (item.getInteger("id").equals(rechargeValidate.getRid())) {
                    rechargeValidate.setMoney(item.getBigDecimal("amount"));
                    rechargeValidate.setPayType(item.getString("title"));
                    break;
                }
            }
        }
        if (rechargeValidate.getMoney() == null) {
            throw new OperateException("充值金额参数错误");
        }

        RechargeOrder order = new RechargeOrder();
        order.setRid( rechargeValidate.getRid() );
        order.setPayType(rechargeValidate.getPayType() );
        order.setUserId(userId);
        order.setOrderTerminal(terminal);
        order.setSn(rechargeOrderMapper.randMakeOrderSn("sn"));
        order.setPayStatus(0);
        order.setRefundStatus(0);
        order.setOrderAmount(rechargeValidate.getMoney());
        order.setCreateTime(System.currentTimeMillis() / 1000);
        order.setUpdateTime(System.currentTimeMillis() / 1000);
        rechargeOrderMapper.insert(order);

        Object result = prepay("recharge", PaymentEnum.WX_PAY.getCode(), order.getId(), null);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("order_id", order.getId());
        response.put("from", "recharge");
        response.put("result", result);

        return response;
    }

    @ApiOperation("发起支付")
    public Object prepay(String scene, Integer payWay, Integer orderId, String code) {

        // 接收参数
        if (StringUtils.isNull(scene)) {
            scene = "recharge";
        }

        PaymentValidate requestObj = new PaymentValidate();
        requestObj.setScene(scene);
        requestObj.setPayWay(payWay);
        requestObj.setOrderId(orderId);
        requestObj.setCode(code);

        Integer terminal = LikeFrontThreadLocal.getTerminal();
        requestObj.setTerminal(terminal);

        // 订单处理
        int payStatus = 0;
        RechargeOrder rechargeOrder = rechargeOrderMapper.selectById(orderId);

        Assert.notNull(rechargeOrder, "订单不存在");
        Assert.isTrue(!payWay.equals(PaymentEnum.WALLET_PAY.getCode()), "支付类型不被支持");

        requestObj.setUserId(rechargeOrder.getUserId());
        requestObj.setOutTradeNo(rechargeOrder.getSn());
        requestObj.setOrderAmount(rechargeOrder.getOrderAmount());
        requestObj.setDescription("充值");
        requestObj.setAttach("recharge");
        payStatus = rechargeOrder.getPayStatus();

        rechargeOrder.setPayWay(payWay);
        rechargeOrderMapper.updateById(rechargeOrder);

        // 订单校验
        if (payStatus != 0) {
            throw new OperateException("订单已支付");
        }
        // 发起支付
        return iPayService.prepay(requestObj, terminal, code);
    }

    @Override
    @Transactional
    public void updatePayOrderStatusToPaid(String outTradeNo, String tradeNo) {
        RechargeOrder order = rechargeOrderMapper.selectOne(new QueryWrapper<RechargeOrder>().eq("sn", outTradeNo).isNull("delete_time"));
        if (StringUtils.isNull(order)) {
            return;
        }

        if (order.getPayStatus().equals(PaymentEnum.OK_PAID.getCode())) { //如果是已支付的状态，则不会再更新
            return;
        }
        Integer userId = order.getUserId();
        String orderSn = outTradeNo;

        User user = userMapper.selectById(order.getUserId());
        user.setUserMoney(user.getUserMoney().add(order.getOrderAmount()));
        user.setTotalRechargeAmount(user.getTotalRechargeAmount().add(order.getOrderAmount()));
        userMapper.updateById(user);

        //add account log
        userAccountLogMapper.add(userId, AccountLogEnum.UM_INC_RECHARGE.getCode(), order.getOrderAmount(), order.getId(), order.getSn(), "", "");

        order.setTransactionId(tradeNo);
        order.setPayStatus(PaymentEnum.OK_PAID.getCode());
        order.setPayTime(System.currentTimeMillis() / 1000);
        rechargeOrderMapper.updateById(order);
    }
}
