package com.mdd.front.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.common.config.AlipayConfig;
import com.mdd.common.entity.RechargeOrder;
import com.mdd.common.entity.setting.DevPayConfig;
import com.mdd.common.entity.setting.DevPayWay;
import com.mdd.common.entity.user.User;
import com.mdd.common.entity.user.UserAuth;
import com.mdd.common.enums.*;
import com.mdd.common.exception.OperateException;
import com.mdd.common.exception.PaymentException;
import com.mdd.common.mapper.log.UserAccountLogMapper;
import com.mdd.common.mapper.RechargeOrderMapper;
import com.mdd.common.mapper.setting.DevPayConfigMapper;
import com.mdd.common.mapper.setting.DevPayWayMapper;
import com.mdd.common.mapper.user.UserAuthMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.plugin.wechat.WxMnpDriver;
import com.mdd.common.plugin.wechat.WxPayDriver;
import com.mdd.common.plugin.wechat.request.PaymentRequestV3;
import com.mdd.common.util.*;
import com.mdd.front.service.IPayService;
import com.mdd.front.validate.PaymentValidate;
import com.mdd.front.vo.pay.PayStatusVo;
import com.mdd.front.vo.pay.PayWayInfoVo;
import com.mdd.front.vo.pay.PayWayListVo;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class PayServiceImpl implements IPayService {

    @Resource
    UserMapper userMapper;

    @Resource
    UserAuthMapper userAuthMapper;

    @Resource
    DevPayWayMapper devPayWayMapper;

    @Resource
    DevPayConfigMapper devPayConfigMapper;

    @Resource
    RechargeOrderMapper rechargeOrderMapper;

    @Resource
    UserAccountLogMapper logMoneyMapper;
    @Value("${ali.pay.order-notify-url}")
    private String aliPayNotifyUrl;

    /**
     * 支付方式
     *
     * @author fzr
     * @param from 场景
     * @param orderId 订单ID
     * @param terminal 终端
     * @return List<PayWayListedVo>
     */
    @Override
    public PayWayListVo payWay(String from, Integer orderId, Integer terminal) {
        List<DevPayWay> devPayWays = devPayWayMapper.selectList(
                new QueryWrapper<DevPayWay>()
                    .eq("scene", terminal)
                    .eq("status", YesNoEnum.YES.getCode()).orderByDesc("id"));

        PayWayListVo vo = new PayWayListVo();
        if (from.equals("recharge")) {
            RechargeOrder rechargeOrder = rechargeOrderMapper.selectById(orderId);
            vo.setOrderAmount(rechargeOrder.getOrderAmount());
        }

        Integer walletType = PaymentEnum.WALLET_PAY.getCode();
        List<PayWayInfoVo> list = new LinkedList<>();
        for (DevPayWay way : devPayWays) {
            if (from.equals("recharge") && way.getPayConfigId().equals(walletType)) {
                continue;
            }

            DevPayConfig devPayConfig = devPayConfigMapper.selectById(way.getPayConfigId());
            PayWayInfoVo infoVo = new PayWayInfoVo();
            infoVo.setId(devPayConfig.getId());
            infoVo.setName(devPayConfig.getName());
            infoVo.setIcon(UrlUtils.toAbsoluteUrl(devPayConfig.getIcon()));
            infoVo.setIsDefault(way.getIsDefault());
            infoVo.setSort(devPayConfig.getSort());
            infoVo.setRemark(devPayConfig.getRemark());
            infoVo.setPayWay(devPayConfig.getPayWay());
            if (devPayConfig.getPayWay().equals(PaymentEnum.WX_PAY.getCode())) {
                infoVo.setExtra("微信快捷支付");
            }
            if (devPayConfig.getPayWay().equals(PaymentEnum.ALI_PAY.getCode())) {
                infoVo.setExtra("支付宝快捷支付");
            }
            list.add(infoVo);
        }

        Collections.sort(list, Comparator.comparing(PayWayInfoVo::getSort).reversed()
                .thenComparing(Comparator.comparingInt(PayWayInfoVo::getId).reversed()));
        vo.setLists(list);
        return vo;
    }

    /**
     * 订单状态
     *
     * @author fzr
     * @param from 场景
     * @param orderId 订单ID
     * @return PayStatusVo
     */
    @Override
    public PayStatusVo payStatus(String from, Integer orderId) {
        PayStatusVo vo = new PayStatusVo();
        boolean orderExist = false;

        switch (from) {
            case "recharge":
                RechargeOrder rechargeOrder = rechargeOrderMapper.selectById(orderId);
                if (StringUtils.isNotNull(rechargeOrder)) {
                    orderExist = true;
                    vo.setPayStatus(rechargeOrder.getPayStatus());
                    vo.setOrderId(rechargeOrder.getId());
                    JSONObject order = new JSONObject();
                    order.put("order_amount", rechargeOrder.getOrderAmount());
                    order.put("order_sn", rechargeOrder.getSn());
                    order.put("pay_time", StringUtils.isNotNull(rechargeOrder.getPayTime()) ? TimeUtils.timestampToDate(rechargeOrder.getPayTime()) : "");
                    order.put("pay_way", PaymentEnum.getPayWayMsg(rechargeOrder.getPayWay()));
                    vo.setOrder(order);
                }
                break;
            case "order":
                break;
        }

        if (!orderExist) {
            throw new OperateException("订单不存在!");
        }

        return vo;
    }

    /**
     * 发起支付
     *
     * @param params 参数
     * @param terminal 终端
     * @return Object
     */
    public Object prepay(PaymentValidate params, Integer terminal, String code) {
        try {
            params.setTerminal(terminal);
            String openId = null;
            UserAuth userAuth = userAuthMapper.selectOne(new QueryWrapper<UserAuth>()
                    .eq("user_id", params.getUserId())
                    //.eq("terminal", terminal)
                    .last("limit 1"));

            if (StringUtils.isNotNull(userAuth)) {
                openId = userAuth.getOpenid();
            } else {
                if(terminal.intValue() != ClientEnum.PC.getCode()) {
                    if (StringUtils.isNotEmpty(code)) {
                        if (ClientEnum.OA.getCode() == terminal.intValue()) {
                            WxMpService wxMpService = WxMnpDriver.oa();
                            WxOAuth2AccessToken wxOAuth2AccessToken = wxMpService.getOAuth2Service().getAccessToken(code);
                            openId  = wxOAuth2AccessToken.getOpenId();
                        } else if (ClientEnum.MNP.getCode() == terminal.intValue()) {
                            WxMaService wxMaService = WxMnpDriver.mnp();
                            WxMaJscode2SessionResult sessionResult = wxMaService.getUserService().getSessionInfo(code);
                            openId = sessionResult.getOpenid();
                        }
                    }
                }
            }

            switch (params.getPayWay()) {
                case 1: // 余额支付
                    String attach = params.getAttach();
                    String orderSn = params.getOutTradeNo();
                    this.handlePaidNotify(attach, orderSn, null);
                    return Collections.emptyList();
                case 2: // 微信支付
                    PaymentRequestV3 requestV3 = new PaymentRequestV3();
                    requestV3.setTerminal(terminal);
                    requestV3.setOpenId(openId);
                    requestV3.setAttach(params.getAttach());
                    requestV3.setOutTradeNo(params.getOutTradeNo());
                    requestV3.setOrderAmount(params.getOrderAmount());
                    requestV3.setDescription(params.getDescription());
                    Object result = WxPayDriver.unifiedOrder(requestV3);
                    if (terminal == ClientEnum.H5.getCode()) {
                        Assert.notNull(params.getRedirect(), "redirectUrl参数缺失");
                        JSONObject ret = new JSONObject();
                        String h5Url = result.toString();
                        ret.put("config", WxPayDriver.unifiedOrder(requestV3));
                        ret.put("pay_way", 2);
                        return ret;
                    }
                    JSONObject ret = new JSONObject();
                    JSONObject config = JSONObject.parseObject(JSONObject.toJSONString(WxPayDriver.unifiedOrder(requestV3)));
                    config.put("package", config.getString("packageValue"));
                    config.remove("packageValue");
                    ret.put("config", config);
                    ret.put("pay_way", 2);
                    return ret;
                    //return WxPayDriver.unifiedOrder(requestV3);
                case 3: //支付宝
                    return this.createAliH5Order(params);
            }
        } catch (Exception e) {
            throw new OperateException(e.toString());
        }

        throw new PaymentException("支付发起异常");
    }

    /**
     * 支付回调处理
     *
     * @author fzr
     * @param attach 场景码
     * @param outTradeNo 订单编号
     * @param transactionId 流水号
     */
    @Override
    @Transactional
    public void handlePaidNotify(String attach, String outTradeNo, String transactionId) {
        switch (attach) {
            case "order":
                break;
            case "recharge":
                this.rechargeCallback(outTradeNo, transactionId);
                break;
        }
    }

    @Override
    public JSONObject createAliH5Order(PaymentValidate params) throws AlipayApiException {
        JSONObject ret = new JSONObject();
        // 订单编号
        String orderSn = "";
        // 订单状态
        Integer orderStatus = 0;
        // 订单金额
        BigDecimal orderAmount = BigDecimal.ZERO;
        Long orderId = 0L;
        String payReturnUrl = "";
        switch (params.getScene()){
            case "member":
                break;
            case "recharge":
                RechargeOrder rechargeOrder = rechargeOrderMapper.selectOne(new QueryWrapper<RechargeOrder>()
                        .eq("id", params.getOrderId())
                        .eq("pay_status", PaymentEnum.UN_PAID.getCode())
                        .eq("user_id", params.getUserId())
                        .last("limit 1"));
                orderSn = rechargeOrder.getSn();
                orderStatus = rechargeOrder.getPayStatus();
                orderAmount = rechargeOrder.getOrderAmount();
                orderId = Long.valueOf(rechargeOrder.getId());
                payReturnUrl = UrlUtils.localDomain(params.getRedirect());
                Assert.notNull(rechargeOrder, "订单不存在");
                break;
            default:
                throw new OperateException("不支持当前订单类型");
        }

        Assert.isTrue(orderStatus != null && orderStatus == PaymentEnum.UN_PAID.getCode(), "订单非待支付状态,不能创建支付单");

        String gateWay = StringUtils.isNotNull(YmlUtils.get("like.alidebug")) && YmlUtils.get("like.alidebug").equals("true") ? AlipayConfig.GATEWAY_URL_DEBUG : AlipayConfig.GATEWAY_URL;
        AlipayClient alipayClient = new DefaultAlipayClient(gateWay, ConfigUtils.getAliDevPay("app_id"), ConfigUtils.getAliDevPay("private_key"), "json", AlipayConfig.CHARSET, ConfigUtils.getAliDevPay("ali_public_key"), AlipayConfig.SIGN_TYPE);
        //2、封装 Request
        String form = "";
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderSn);
        bizContent.put("total_amount", orderAmount);
        bizContent.put("subject", params.getDescription());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("app_pay", "Y");
        bizContent.put("passback_params", JSONObject.toJSONString(new JSONObject(){{
            put("from", params.getScene());
        }}));
        if (params.getTerminal().equals(ClientEnum.H5.getCode()) || params.getTerminal().equals(ClientEnum.OA.getCode())) {
            AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
            //alipayRequest.setNotifyUrl("http://y4f8ud.natappfree.cc" + aliPayNotifyUrl);
            alipayRequest.setNotifyUrl(UrlUtils.getRequestUrl() + aliPayNotifyUrl);
            alipayRequest.setReturnUrl(payReturnUrl);
            alipayRequest.setBizContent(bizContent.toString());

            try {
                form = alipayClient.pageExecute(alipayRequest).getBody();
                ret.put("config", form);
                ret.put("pay_way", params.getPayWay());
                return ret;
            } catch (Exception e) {
                throw new PaymentException(e.getMessage());
            }
        } else {
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            //alipayRequest.setNotifyUrl("http://y4f8ud.natappfree.cc" + aliPayNotifyUrl);
            alipayRequest.setNotifyUrl(UrlUtils.getRequestUrl() + aliPayNotifyUrl);
            alipayRequest.setReturnUrl(payReturnUrl);
            alipayRequest.setBizContent(bizContent.toString());

            try {
                form = alipayClient.pageExecute(alipayRequest).getBody();
                ret.put("config", form);
                ret.put("pay_way", params.getPayWay());
                return ret;
            } catch (Exception e) {
                throw new OperateException(e.getMessage());
            }
        }
    }

    /**
     * 余额充值回调
     *
     * @author fzr
     * @param outTradeNo 订单号
     * @param transactionId 流水号
     */
    private void rechargeCallback(String outTradeNo, String transactionId) {
        for (int i=0; i<=0; i++) {
            RechargeOrder rechargeOrder = rechargeOrderMapper.selectOne(
                    new QueryWrapper<RechargeOrder>()
                            .eq("sn", outTradeNo).isNull("delete_time")
                            .last("limit 1"));

            if (StringUtils.isNull(rechargeOrder)) {
                log.error("充值订单不存在: {} : {}", outTradeNo, transactionId);
                break;
            }

            if (rechargeOrder.getPayStatus().equals(PaymentEnum.OK_PAID.getCode())) {
                log.error("充值订单已支付: {} : {}", outTradeNo, transactionId);
                break;
            }

            rechargeOrder.setPayStatus(1);
            rechargeOrder.setTransactionId(transactionId);
            rechargeOrder.setPayTime(System.currentTimeMillis() / 1000);
            rechargeOrder.setUpdateTime(System.currentTimeMillis() / 1000);
            rechargeOrderMapper.updateById(rechargeOrder);


            logMoneyMapper.add(rechargeOrder.getUserId(),
                    AccountLogEnum.UM_INC_RECHARGE.getCode(),
                    rechargeOrder.getOrderAmount(),
                    rechargeOrder.getId(),
                    rechargeOrder.getSn(),
                    "用户充值余额", null);

            User user = userMapper.selectById(rechargeOrder.getUserId());
            user.setUserMoney(user.getUserMoney().add(rechargeOrder.getOrderAmount()));
            user.setUpdateTime(System.currentTimeMillis() / 1000);
            userMapper.updateById(user);
        }
    }
}
