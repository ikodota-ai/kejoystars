package com.mdd.admin.controller;

import com.mdd.admin.validate.k.NativeWebCrawlerWithProxyValidated;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.core.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlPage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping(value = "/adminapi/NativeWebCrawlerWithProxy")
@Api(tags = "抓取代理")
public class NativeWebCrawlerWithProxyController {
    private final String proxy = "http://apiadmin.kejoystars.com:8881/adminapi/NativeWebCrawlerWithProxy/p";

    @NotLogin
    @GetMapping("/p")
    @ApiOperation(value="代理入口")
    public String p(@Validated
                        NativeWebCrawlerWithProxyValidated proxyValidate)
            throws Exception{
        // 1. 发起HTTP请求
        String target = proxyValidate.getUrl();
        if (proxyValidate.getSource() != null && !proxyValidate.getSource().isEmpty()) {
            target = proxyValidate.getSource();
        }
        URL url = new URL( target );
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (proxyValidate.getMethod()!=null){
            conn.setRequestMethod(proxyValidate.getMethod());
        } else {
            conn.setRequestMethod("GET");
        }
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        if (proxyValidate.getHeaders() != null) {
            for (String key : proxyValidate.getHeaders().keySet()) {
                conn.setRequestProperty(key, proxyValidate.getHeaders().getString(key));
            }
        }
        // 2. 获取HTML内容
        StringBuilder html = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
        }
        //将html里的url换成 source 并将原url加入参数source
        Pattern pattern = Pattern.compile("(src|href)=\"(.*?)\"");
        Matcher matcher = pattern.matcher(html.toString());
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                String originalUrl = matcher.group(2);
                String encodedUrl = java.net.URLEncoder.encode(originalUrl, "UTF-8");
                String replacement = matcher.group(1) + "=\"" + proxy + "?url=" + encodedUrl + "\"";
                matcher.appendReplacement(sb, replacement);
            } catch (Exception e) {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        html = new StringBuilder(sb.toString());

        return html.toString();
    }

    @NotLogin
    @GetMapping("/trace")
    public AjaxResult<Map<String, Object>> traceWebRequests(@Validated
                                            NativeWebCrawlerWithProxyValidated proxyValidate) throws Exception {
        Set<String> requestUrls = new HashSet<>();
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
//            webClient.getOptions().setThrowExceptionOnScriptError(false);
            // 监听所有请求
            webClient.setWebConnection(new org.htmlunit.util.WebConnectionWrapper(webClient) {
                @Override
                public org.htmlunit.WebResponse getResponse(WebRequest webRequest) throws java.io.IOException {
                    requestUrls.add(webRequest.getUrl().toString());
                    return super.getResponse(webRequest);
                }
            });

            HtmlPage page = webClient.getPage(new WebRequest(new URL(proxyValidate.getUrl())));
            webClient.waitForBackgroundJavaScript(500); // 等待JS执行

            // 返回所有请求过的网址
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("urls", requestUrls);
            return AjaxResult.success("", result);
        }
    }

    @NotLogin
    @GetMapping("/instagram")
    public AjaxResult<String> instagramWebRequests(@Validated
                                                            NativeWebCrawlerWithProxyValidated proxyValidate) throws Exception {
        // 使用HtmlUnit抓取Instagram用户图片
        String url = proxyValidate.getUrl();
        try (WebClient webClient = new WebClient()) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);

            HtmlPage page = webClient.getPage(url);
            webClient.waitForBackgroundJavaScript(1000);

            // 正则匹配图片URL（img标签的src属性）
            Pattern pattern = Pattern.compile("<img[^>]+src=[\"']([^\"'>]+)[\"'][^>]*>");
            Matcher matcher = pattern.matcher(page.asXml());
            Set<String> imgUrls = new HashSet<>();
            while (matcher.find()) {
                imgUrls.add(matcher.group(1));
            }
            return AjaxResult.success("", String.join(",", imgUrls));
        }
    }
}
