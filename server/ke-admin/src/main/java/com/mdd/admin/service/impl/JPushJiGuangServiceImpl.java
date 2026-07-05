package com.mdd.admin.service.impl;

import cn.jiguang.sdk.api.PushApi;
import cn.jiguang.sdk.bean.device.TagSetParam;
import cn.jiguang.sdk.bean.push.PushSendParam;
import cn.jiguang.sdk.bean.push.PushSendResult;
import cn.jiguang.sdk.bean.push.audience.Audience;
import cn.jiguang.sdk.bean.push.callback.Callback;
import cn.jiguang.sdk.bean.push.message.notification.NotificationMessage;
import cn.jiguang.sdk.bean.push.options.Options;
import cn.jiguang.sdk.enums.platform.Platform;
import cn.jiguang.sdk.exception.ApiErrorException;
import com.mdd.admin.service.IJPushJiGuangService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JPushJiGuangServiceImpl implements IJPushJiGuangService {
    @Autowired
    private PushApi pushApi;

    @Override
    public void sendNotice(String message, String title, List<String> registrationIds) {
        PushSendParam param = new PushSendParam();
        // 通知内容
        NotificationMessage.Android android = new NotificationMessage.Android();
        android.setAlert(message);
        android.setTitle(title);

        Map<String, Object> extrasMap = new HashMap<>();
        Map<String, Object> extrasParamMap = new HashMap<>();
        extrasParamMap.put("key1", "value1");
        extrasParamMap.put("key2", "value2");
        extrasMap.put("params", extrasParamMap);
        android.setExtras(extrasMap);


        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setAlert(message);
        notificationMessage.setAndroid(android);
//        notificationMessage.setIos(iOS);
        param.setNotification(notificationMessage);
        // 目标人群
        Audience audience = new Audience();
        audience.setRegistrationIdList( registrationIds );
        // 指定目标
        param.setAudience(audience);

//        // 或者发送所有人
//        param.setAudience(ApiConstants.Audience.ALL);
//
//        // 指定平台
        param.setPlatform(Arrays.asList(Platform.android));
        // 或者发送所有平台
        // param.setPlatform(ApiConstants.Platform.ALL);

        // 短信补充
        // param.setSmsMessage();

        // 回调
        // param.setCallback();

        Map<String, Object> callbackParams = new HashMap<>();
        callbackParams.put("callbackKey", "callbackValue");
        Callback callback = new Callback();
        callback.setParams(callbackParams);
        param.setCallback(callback);

        // 发送
        try {
            PushSendResult result = pushApi.send(param);
            log.info("send success:{}", result);
            log.info("send rateLimit:{}", result.getRateLimit());
        } catch (ApiErrorException e) {
            log.error("send error:{}", e.getApiError());
            log.error("send rateLimit:{}", e.getRateLimit());
        }
    }
}
