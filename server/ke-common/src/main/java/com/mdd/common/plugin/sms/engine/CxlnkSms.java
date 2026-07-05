package com.mdd.common.plugin.sms.engine;

import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * 阿里云短信
 */
public class CxlnkSms {

    private static final Logger log = LoggerFactory.getLogger(CxlnkSms.class);
    private Integer sendResult;     // 发送结果
    private String mobile;          // 手机号码
    private String templateId;      // 短信模板
    private String templateParams;  // 短信参数
    private final Map<String, String> config; // 短信配置
    private final String accessKey = "Njg4MzMyNDQ=", accessSecret = "MzMzNzIyMzIzOTQ1MDAxMDAyNDQ=", signCode = "237e54da", msgType = "1";

    /**
     * 构造方法
     *
     * @author fzr
     * @param config 短信配置
     */
    public CxlnkSms(Map<String, String> config) {
        this.config = config;
    }

    /**
     * 设置手机号
     *
     * @author fzr
     * @param mobile 手机号码
     * @return AliSms
     */
    public CxlnkSms setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    /**
     * 设置模板id
     *
     * @author fzr
     * @param templateId 模板id
     * @return AliSms
     */
    public CxlnkSms setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    /**
     * 设置模板参数
     *
     * @author fzr
     * @param templateParams 模板参数
     * @return AliSms
     */
    public CxlnkSms setTemplateParams(String templateParams) {
        this.templateParams = templateParams;
        return this;
    }

    /**
     * 获取发送结果
     *
     * @author fzr
     * @return Integer [1=成功, 2=失败]
     */
    public Integer getSendResult() {
        return this.sendResult;
    }

    /**
     * 发送短信
     *
     * @author fzr
     * @return String
     */
    public String send() {
        String url = "https://api.cxlnk.com/api/msgService/sendSms";
        JSONObject payload = new JSONObject();
        payload.put("accessKey",accessKey);
        payload.put("accessSecret", accessSecret);
        payload.put("signCode", signCode);
        payload.put("templateCode", this.templateId);
        payload.put("mobile", this.mobile);
        payload.put("params", new ArrayList<String>(){{
            add(templateParams);
        }});
        payload.put("msgType", msgType);
//        if (config.containsKey("taskId")) {
//            payload.put("taskId", config.get("taskId"));
//        }
        log.info("CxlnkSms request payload: {}", payload.toJSONString());
        try {
            java.net.URL apiUrl = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toJSONString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            log.info("CxlnkSms response: {}", response.toString());

            JSONObject respJson = JSONObject.parseObject(response.toString());
            int code = respJson.getIntValue("code");
            String msg = respJson.getString("msg");
            boolean success = respJson.getBooleanValue("success");

            if (code == 200 && success) {
                this.sendResult = 1;
                return "发送成功：" + msg;
            } else if (code == 60021) {
                this.sendResult = 2;
                return "余额不足，请充值后重试！";
            } else if (code == 502) {
                this.sendResult = 2;
                return "系统繁忙";
            } else if (code == 500 || code == 501 || code == 503) {
                this.sendResult = 2;
                return "系统错误";
            } else if (code == 5001) {
                this.sendResult = 2;
                return "请求参数校验异常";
            } else {
                this.sendResult = 2;
                return "发送失败：" + msg;
            }
        } catch (Exception e) {
            this.sendResult = 2;
            return "接口调用异常：" + e.getMessage();
        }
        /**
         * 1.接口说明（单条短信）
         * 名称 	说明
         * 协议 	HTTPS POST
         * 地址 	https://api.cxlnk.com/api/msgService/sendSms
         * Accept 	application/json
         * Content-Type 	application/json;charset=utf-8
         *
         * 2.请求参数说明
         *
         * 响应 body 数据为 JSON 格式
         * 列表 	内容 	备注
         * accessKey 	API调用账号（必填，String类型，从灿星云平台获取）
         * accessSecret 	API调用密钥（必填，String类型，从灿星云平台获取）
         * signCode 	短信签名code（必填）
         * templateCode 	短信模版code（必填）
         * mobile 	目标手机号（必填，String类型）
         * params 	参数（选填，List类型），若有参数则必填且参数需与所选模版中参数名称以及数量一致
         * msgType 	短信类型（必填, Integer类型）
         * 1：验证码；2：短信通知；3：会员营销；4：推广短信
         * taskId 	自定义任务 id （非必填）
         *
         * 4.响应参数列表
         * 列表 	内容 	备注
         * code 	状态码200则为成功
         * msg 	状态说明
         * success 	处理是否成功:取值范围 true，false
         * data 	业务数据 如果失败则为空
         * mobile 	手机号码
         * msgId 	短信id
         *
         * 6.结果码说明
         * 结果码 	描述
         * 200 	成功
         * 502 	系统繁忙
         * 60021 	余额不足，请充值后重试！
         * 500，501，503 	系统错误
         * 5001 	请求参数校验异常
         */
    }

}
