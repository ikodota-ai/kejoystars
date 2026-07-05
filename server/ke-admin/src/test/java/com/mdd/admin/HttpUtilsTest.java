package com.mdd.admin;

import cn.jiguang.sdk.api.PushApi;
import cn.jiguang.sdk.bean.push.PushSendParam;
import cn.jiguang.sdk.bean.push.PushSendResult;
import cn.jiguang.sdk.bean.push.callback.Callback;
import cn.jiguang.sdk.bean.push.message.notification.NotificationMessage;
import cn.jiguang.sdk.bean.push.options.Options;
import cn.jiguang.sdk.constants.ApiConstants;
import cn.jiguang.sdk.enums.platform.Platform;
import cn.jiguang.sdk.exception.ApiErrorException;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.admin.crontab.CrawlQuartzJob;
import com.mdd.admin.service.IJPushJiGuangService;
import com.mdd.common.entity.smsLog.SmsLog;
import com.mdd.common.enums.NoticeEnum;
import com.mdd.common.enums.SmsEnum;
import com.mdd.common.enums.YesNoEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.smsLog.SmsLogMapper;
import com.mdd.common.plugin.notice.NoticeDriver;
import com.mdd.common.plugin.notice.vo.NoticeSmsVo;
import com.mdd.common.util.ImageDownloaderUtil;
import com.mdd.common.util.StringUtils;
import com.mdd.common.util.ToolUtils;
import com.mdd.common.util.YmlUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import cn.jiguang.sdk.bean.push.audience.Audience;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LikeAdminApplication.class})
public class HttpUtilsTest {
    public static String domain;
    @Resource
    CrawlQuartzJob crawlQuartzJob;

    @Autowired
    IJPushJiGuangService jPushJiGuangService;

    @Autowired
    private PushApi pushApi;
    @Test
    public void _main(){
        //取得关注用户列表,actorList结构是： sid是明星id
        String ss = "<img class=\"profile w-full\" src=\"https://media.themoviedb.org/t/p/w300_and_h450_face/bRC1B2VwV0wK3ElciFAK6QZf2wD.jpg\" srcset=\"https://media.themoviedb.org/t/p/w300_and_h450_face/bRC1B2VwV0wK3ElciFAK6QZf2wD.jpg 1x, https://media.themoviedb.org/t/p/w600_and_h900_face/bRC1B2VwV0wK3ElciFAK6QZf2wD.jpg 2x\" alt=\"凯拉·奈特莉\">";
        Document doc = Jsoup.parse(ss);

        Element coverElem = doc.selectFirst("img");
        String movieCover = coverElem.attr("src");
        if (!movieCover.isEmpty()) {
            try{
                //下载图片
                movieCover = grabImage(movieCover,  "image/movies/");
            } catch(OperateException | IOException e){
                String[] srcset = coverElem.attr("srcset").split(",");
                for (String s : srcset) {
                    movieCover = s.replaceAll("^\\s?(http[^\\s]+)\\s.*?$", "$1");
                    try {
                        movieCover = grabImage(movieCover, "image/movies/");
                        break;
                    } catch (IOException e1) {
                        log.error(e1.getMessage());
                        movieCover = coverElem.attr("src");
                    }
                }
            }
        }

        System.out.println(movieCover);
    }

    public String grabImage(String imageUrl, String dir) throws IOException {
        if (imageUrl.isEmpty()) return null;
        // 映射目录
        String directory = YmlUtils.get("like.upload-directory");
        if (directory == null || directory.equals("")) {
            throw new OperateException("请配置上传目录like.upload-directory");
        }

        // 文件信息
        String savePath = (directory +  dir);

        // 创建目录
        File fileExist = new File(savePath);
        if (!fileExist.exists()) {
            if (!fileExist.mkdirs()) {
                throw new OperateException("创建上传目录失败");
            }
        }
        String imageFilename = imageUrl.replaceAll("^.*?([^/]{0,})$", "$1");
        try {
            ImageDownloaderUtil.downloadImage(imageUrl, new HashMap<String, String>() {{
                put("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5");
                put("Accept-Encoding", "gzip, deflate, br, zstd");
                put("Accept-Language", "en-US,en;q=0.5");
                put("Connection", "keep-alive");
                put("Cookie", "_ga_4257LNMWD9=GS2.1.s1757375481$o12$g1$t1757375522$j19$l0$h0; _ga=GA1.1.1590383222.1757146055");
                put("Host", "media.themoviedb.org");
                put("Priority", "u=5, i");
                put("Referer", "https://www.themoviedb.org/");
                put("Sec-Fetch-Dest", "image");
                put("Sec-Fetch-Mode", "no-cors");
                put("Sec-Fetch-Site", "same-site");
                put("TE", "trailers");
                put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:142.0) Gecko/20100101 Firefox/142.0");
            }}, savePath + imageFilename);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("图片下载失败" + imageUrl);
            throw new OperateException("图片下载失败" + imageUrl);
        }

        return dir + imageFilename;
    }

    @Test
    public void test() throws Exception {
//        crawlQuartzJob.publish("message_channel", new HashMap<String, String>(){{
//            put("id", "13");
//            put("web", "qq");
//            put("url", "https://news.qq.com/");
////            put("first", "1");
//        }});
        jPushJiGuangService.sendNotice("this is android alert", "this is android alert", Arrays.asList("1104a897938d8adddb4"));
    }


    @Test
    public void send() {
        PushSendParam param = new PushSendParam();
        // 通知内容
        NotificationMessage.Android android = new NotificationMessage.Android();
        android.setAlert("this is android alert");
        android.setTitle("this is android title");

        NotificationMessage.IOS iOS = new NotificationMessage.IOS();
        Map<String, String> iOSAlert = new HashMap<>();
        iOSAlert.put("title", "this is iOS title");
        iOSAlert.put("subtitle", "this is iOS subtitle");
        iOS.setAlert(iOSAlert);

        Map<String, Object> extrasMap = new HashMap<>();
        Map<String, Object> extrasParamMap = new HashMap<>();
        extrasParamMap.put("key1", "value1");
        extrasParamMap.put("key2", "value2");
        extrasMap.put("params", extrasParamMap);
        android.setExtras(extrasMap);
        iOS.setExtras(extrasMap);

        NotificationMessage notificationMessage = new NotificationMessage();
        notificationMessage.setAlert("this is alert");
        notificationMessage.setAndroid(android);
//        notificationMessage.setIos(iOS);
        param.setNotification(notificationMessage);

        // 目标人群
        Audience audience = new Audience();
        audience.setRegistrationIdList(Arrays.asList("1104a897938d8adddb4", "170976fa8baa038fb68"));
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

        // options
        Options options = new Options();
        Map<String, Object> thirdPartyMap = new HashMap<>();
        Map<String, Object> huaweiMap = new HashMap<>();
//        huaweiMap.put("distribution", "first_ospush");
        huaweiMap.put("importance", "NORMAL");
        huaweiMap.put("category", "MARKETING");
        thirdPartyMap.put("huawei", huaweiMap);
        options.setThirdPartyChannel(thirdPartyMap);
        param.setOptions(options);

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

    @Test
    public void test1() throws Exception {
        crawlQuartzJob.CrawlImage();
    }

    public static JSONObject grabMovie( String url) throws Exception {
        if (url.isEmpty()) return null;
        // 加载本地HTML文件
        Connection connect = Jsoup.connect( url );
        Document doc = connect.get();

        // 剧名
        String title = doc.selectFirst("section.header div.title h2 a").text();
        // 简介（假设简介在某个p标签或div中，需根据实际页面调整选择器）
        String description = "";
        Element descElem = doc.selectFirst("div.overview p");
        if (descElem != null) {
            description = descElem.text();
        }

        // 默认语言
        String language = doc.selectFirst("p strong:contains(默认语言)").parent().text().replace("默认语言", "").trim();

        // 个播出平台
        List<String> platforms = new ArrayList<>();
        Elements platformElems = doc.select("ul.networks li a img");
        for (Element e : platformElems) {
            String alt = e.attr("alt");
            platforms.add(alt);
        }

        // 状态
        String status = doc.selectFirst("p strong:contains(状态)").parent().text().replace("状态", "").trim();

        // 构造JSON
        JSONObject result = new JSONObject();
        result.put("title", title);
        result.put("description", description);
        result.put("language", language);
        result.put("platforms", platforms);
        result.put("status", status);

        domain = url.replaceAll("^(https:|http:)//(.*?)/.*$", "$1//$2");
        String actorsUrl = doc.selectFirst("section.top_billed li.filler a").attr("href");
        List<JSONObject> femaleActors = grabActors(actorsUrl);
        result.put("femaleActors", femaleActors);
        return result;
    }

    /**
     * 演员提取
     * @param url
     * @return
     * @throws Exception
     */
    public static List<JSONObject> grabActors( String url) throws Exception {
        if (url.isEmpty()) return null;
        // 加载本地HTML文件
        Connection connect = Jsoup.connect( domain + url );
        Document doc = connect.get();
        //剧集演员
        // 提取演员列表，如“泰勒·席林 饰演 Piper Chapman”，并组成json数组
        Elements actorCards = doc.select("main section div.content_wrapper");
        List<JSONObject> actors = new ArrayList<>();

        // 假设演员列表在 <li> 标签内
        Elements actorLis = doc.select("section.pad ol li");
        for (Element li : actorLis) {
            JSONObject obj = new JSONObject();

            Element el = li.selectFirst("div.glyphicons_v2 img");
            if (el != null) {
                obj.put("image_66", el.attr("src"));
            } else {
                obj.put("image_66", "");
            }

            Element e = li.select("div.info p").get(0).selectFirst("a");
            if (e != null) {
                String name = e.text();
                obj.put("name", name);
                obj.put("href", e.attr("href"));
                actors.add(obj);
            }

            e = li.select("div.info p").get(1).selectFirst("a");
            if (e != null) {
                obj.put("cosplay", e.text());
            }
            actors.add(obj);
            break;
        }

        JSONObject starInfo = grabStarInfo( actors.get(0).getString("href") );
        if (starInfo != null && starInfo.get("gender").equals("女")) {
            actors.get(0).put("info", starInfo);
        }

        return actors;
    }

    public static JSONObject grabStarInfo( String url) throws IOException {
        if (url.isEmpty()) return null;
        JSONObject result = new JSONObject();
        // 加载本地HTML文件
        Connection connect = Jsoup.connect( domain + url );
        Document doc = connect.get();

        Element mainEl = doc.selectFirst("main div.person_v4");

        //封面图片
        Element imageEl = mainEl.selectFirst("section.images");
        if (imageEl != null) {
            Element imgEl = imageEl.selectFirst("img");
            if (imgEl != null) {
                result.put("avatar", imgEl.attr("src"));
            }
        }

        Element leftEl = mainEl.selectFirst("section.left_column");
        Elements socialEls = leftEl.select("div.social_links div");
        for (Element socialEl : socialEls) {
            Element aEl = socialEl.selectFirst("a");
            if (aEl != null && aEl.attr("href").contains("twitter.com")) {
                result.put("twitter", aEl.attr("href").replaceAll("^http.*?com/(.*?)/?$", "$1"));
            } else if (aEl != null && aEl.attr("href").contains("instagram.com")) {
                result.put("instagram", aEl.attr("href").replaceAll("^http.*?com/(.*?)/?$", "$1"));
            }
        }

        // 生平简介（meta[name=description]或og:description）
        String biography = "";
        Element bioEl = doc.selectFirst("meta[property=og:description]");
        if (bioEl != null) {
            biography = bioEl.attr("content").trim();
        } else {
            Element descEl = doc.selectFirst("meta[name=description]");
            if (descEl != null) {
                biography = descEl.attr("content").trim();
            }
        }
        result.put("biography", biography);

        // 参与作品数（从生平简介中提取“出演了xx多部电影”）
        String worksCount = "";
        // 性别（根据描述或页面结构，示例中为“女演员”）
        String gender = "";
        // 艺名（title标签或og:title）
        String stageName = "";
        // 生日（从生平简介中提取“xxxx年xx月xx日出生”）
        String birthday = "";
        String birthPlace = "";//出生地
        Elements baseInfoEl = leftEl.select("section.facts p");
        for (Element p : baseInfoEl) {
            String text = p.text();
            if (text.contains("生日")) {
                int start = text.indexOf("生日");
                birthday = text.substring(start).replace("生日", "").trim();
            } else if (text.contains("出生地")) {
                int start = text.indexOf("出生地");
                birthPlace = text.substring(start).replace("出生地", "").trim();
            } else if (text.contains("性别")) {
                int start = text.indexOf("性别");
                gender = text.substring(start).replace("性别", "").trim();
            } else if (text.contains("参与作品数")) {
                int start = text.indexOf("参与作品数");
                worksCount = text.replace("参与作品数", "").trim();
            }else if (text.contains("艺名")) {
                int start = text.indexOf("艺名");
                stageName = text.substring(start).replace("艺名", "").trim();
            }
        }

        result.put("stageName", stageName);
        result.put("worksCount", worksCount);
        result.put("gender", gender);
        result.put("birthday", birthday.replaceAll("\\s","").replaceAll("^(.*?)日.*?$", "$1日"));
        result.put("birthPlace", birthPlace);
        //System.out.println(result.toJSONString());
        return result;
    }

    @Test
    public void testDownload() throws Exception {
        String imageUrl = "https://media.themoviedb.org/t/p/w300_and_h450_bestv2/tu1CjyjoUoV7rGPyvwvH9b0MlbW.jpg";
        // 映射目录
        String directory = YmlUtils.get("like.upload-directory");
        if (directory == null || directory.equals("")) {
            throw new OperateException("请配置上传目录like.upload-directory");
        }

        String dir = "image/cover/";
        // 文件信息
        String savePath = (directory +  dir);

        // 创建目录
        File fileExist = new File(savePath);
        if (!fileExist.exists()) {
            if (!fileExist.mkdirs()) {
                throw new OperateException("创建上传目录失败");
            }
        }
        String imageFilename = imageUrl.replaceAll("^.*?([^/]{0,})$", "$1");

        ImageDownloader.downloadImage(imageUrl, new HashMap<String, String>(){{
                put("Accept", "image/avif,image/webp,image/png,image/svg+xml,image/*;q=0.8,*/*;q=0.5");
                put("Accept-Encoding", "gzip, deflate, br, zstd");
                put("Accept-Language", "en-US,en;q=0.5");
                put("Connection", "keep-alive");
                put("Cookie", "_ga_4257LNMWD9=GS2.1.s1757375481$o12$g1$t1757375522$j19$l0$h0; _ga=GA1.1.1590383222.1757146055");
                put("Host", "media.themoviedb.org");
                put("Priority", "u=5, i");
                put("Referer", "https://www.themoviedb.org/");
                put("Sec-Fetch-Dest", "image");
                put("Sec-Fetch-Mode", "no-cors");
                put("Sec-Fetch-Site", "same-site");
                put("TE", "trailers");
                put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:142.0) Gecko/20100101 Firefox/142.0");
        }}, savePath + imageFilename);
    }
    @Test
    public void testGrabTwitter() throws Exception {
        // 1. 发起HTTP请求
        URL url = new URL("https://www.baidu.com");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        // 2. 获取HTML内容
        StringBuilder html = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
        }

        // 3. 解析HTML
        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
            public void handleText(char[] data, int pos) {
                System.out.println("Text: " + new String(data));
            }
        };
        new ParserDelegator().parse(
                new StringReader(html.toString()), callback, true);

        // 4. 执行JS代码（示例）
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        engine.eval("function test(){ return '模拟JS执行'; }");
        System.out.println(engine.eval("test()"));
    }

    @Test
    public void test2(){
        String s = "src=\"http://apiadmin.kejoystars.com:8881/adminapi/NativeWebCrawlerWithProxy/p?url=https://twitter.com/AngelssBecky&source=\"";

        Pattern pattern = Pattern.compile("(src|href)=\"(.*?)\"");
        Matcher matcher = pattern.matcher(s.toString());
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            try {
                String originalUrl = matcher.group(2);
                String encodedUrl = java.net.URLEncoder.encode(originalUrl, "UTF-8");
                String replacement = matcher.group(1) + "=\"localhost/?source=" + encodedUrl + "\"";
                matcher.appendReplacement(sb, replacement);
            } catch (Exception e) {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }
        matcher.appendTail(sb);
        s = sb.toString();

        System.out.println(s);
    }
    @Resource
    SmsLogMapper smsLogMapper;
    @Test
    public void smsSend(){
        String mobile = "18602821107";
        String scene = "BDSJHM";
        QueryWrapper smsLogQueryWrapper = new QueryWrapper<SmsLog>();
        smsLogQueryWrapper.eq("mobile", mobile);
        smsLogQueryWrapper.eq("send_status", SmsEnum.SEND_SUCCESS.getCode());
        smsLogQueryWrapper.in("scene_id", NoticeEnum.getSmsScene());
        smsLogQueryWrapper.eq("is_verify", YesNoEnum.NO.getCode());
        smsLogQueryWrapper.eq("scene_id", NoticeEnum.getSceneByTag(scene));

        smsLogQueryWrapper.orderByDesc("send_time");
        smsLogQueryWrapper.last("limit 1");
        SmsLog smsLog = smsLogMapper.selectOne(smsLogQueryWrapper);
        if (StringUtils.isNotNull(smsLog)) {
            if (smsLog.getSendTime() + 5 * 60 > System.currentTimeMillis() / 1000 ) {
                throw new OperateException("已有短信记录，请勿重复发送");
            }
        }

        String code = ToolUtils.randomInt(4);
        NoticeSmsVo params = new NoticeSmsVo()
                .setScene(NoticeEnum.getSceneByTag(scene))
                .setMobile(mobile)
                .setExpire(900)
                .setParams(new String[] {
                        "code:" + code
                })
                .setCode(code);

        NoticeDriver.handle(params);
    }
}

class ImageDownloader {
    public static void downloadImage(String imageUrl, Map<String, String> headers, String savePath) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.setRequestMethod("GET");
        conn.connect();

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        conn.disconnect();
    }
}
