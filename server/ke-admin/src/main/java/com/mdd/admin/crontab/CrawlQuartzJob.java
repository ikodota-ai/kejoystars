package com.mdd.admin.crontab;

import cn.jiguang.sdk.api.PushApi;
import cn.jiguang.sdk.bean.push.batch.BatchPushParam;
import cn.jiguang.sdk.bean.push.batch.BatchPushSendParam;
import cn.jiguang.sdk.bean.push.batch.BatchPushSendResult;
import cn.jiguang.sdk.bean.push.message.notification.NotificationMessage;
import cn.jiguang.sdk.enums.platform.Platform;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.admin.service.IJPushJiGuangService;
import com.mdd.admin.service.impl.k.MoviesInfoServiceImpl;
import com.mdd.common.entity.k.Couple;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.entity.k.StarInstagram;
import com.mdd.common.entity.k.UserHistory;
import com.mdd.common.entity.user.User;
import com.mdd.common.enums.AlbumEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.k.CoupleMapper;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.mapper.k.StarInstagramMapper;
import com.mdd.common.mapper.k.UserHistoryMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.plugin.storage.BytesMultipartFile;
import com.mdd.common.plugin.storage.StorageDriver;
import com.mdd.common.plugin.storage.UploadFilesVo;
import com.mdd.common.util.ImageDownloaderUtil;
import com.mdd.common.util.RedisUtils;
import com.mdd.common.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import javax.annotation.Resource;

/**
 * 这是一个定时器，每天晚上12点执行，查找明星有instagram的，
 * 把instagram地址通过redis的消息队列，发送给抓取程序
 */
@Slf4j
@Component("crawlQuartzJob")
public class CrawlQuartzJob {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    StarInfoMapper starInfoMapper;
    Logger logger = LoggerFactory.getLogger(MoviesInfoServiceImpl.class);
    @Autowired
    PushApi pushApi;
    @Autowired
    UserHistoryMapper userHistoryMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    IJPushJiGuangService jPushJiGuangService;
    //@Scheduled(cron = "0 0 0 * * ?")
    //每10分钟执行一次
//    @Scheduled(cron = "0 0 * * * ?")
//    @Scheduled(cron = "0 0 0 * * ?")
    public void handle() {
        QueryWrapper<StarInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("`id`,`name`,`gender`,`x`,`twitter`");
        queryWrapper.apply("(NOT ISNULL(x)  OR NOT ISNULL(twitter))");
        List<StarInfo> list = starInfoMapper.selectList( queryWrapper );
        for (StarInfo starInfo : list) {
            if (starInfo.getX() != null && !starInfo.getX().isEmpty()) {
                publish("message_channel", new HashMap<String, String>(){{
                    put("id", starInfo.getId().toString());
                    put("web", "ins");
                    put("url", "https://www.instagram.com/" + starInfo.getX());
                }});
            }
            if (starInfo.getTwitter() != null && !starInfo.getTwitter().isEmpty()) {
                publish("message_channel", new HashMap<String, String>(){{
                    put("id", starInfo.getId().toString());
                    put("web", "x");
                    put("url", "https://x.com/" + starInfo.getTwitter());
                }});
            }
        }
    }
    private static final String IMAGE_URL_KEY = "IMAGE_URL_KEY";

    @Autowired
    StarInstagramMapper starInstagramMapper;
    /**
     * 每个小时启动一次
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void CrawlImage() {
        while (true) {
            //查看redis list长度
            Long size = RedisUtils.handler().opsForList().size(IMAGE_URL_KEY);
            if (size > 0) {
                Map<String, String> text = (HashMap<String, String>) RedisUtils.handler().opsForList().rightPop(IMAGE_URL_KEY);
                System.out.println( "Received message: " + JSONObject.toJSONString(text) ); // 处理接收到的消息。
//                JSONObject jsonObject = JSONObject.parseObject(text);
                //生成本地文件名,path + md5(jsonObject.getString("url")) + ".jpg"检查图片是否存在,
                //将远程图片保存到本地
                String url = text.get("url");
                String regex = "^http(s?)://[^/]+/([^?]+).*?$";
                String pathPart = url.replaceAll(regex, "$2");
                if (!url.matches(regex) || pathPart.isEmpty()) continue;

                String md5 = DigestUtils.md5Hex(pathPart);
                int count = Math.toIntExact(starInstagramMapper.selectCount(new QueryWrapper<StarInstagram>().eq("check_code", md5)));
                if (count == 0) {
                    ImageDownloaderUtil.FetchedImage fetched;
                    try {
                        fetched = ImageDownloaderUtil.downloadImageAsBytes(url, new HashMap<>());
                    } catch (Exception e) {
                        System.err.println("下载图片失败: " + e.getMessage());
                        continue;
                    }
                    if (fetched == null) {
                        System.err.println("图片过小或无效: " + url);
                        continue;
                    }

                    // 通过 StorageDriver 分发到当前生效的存储引擎
                    String ext = fetched.getExtension();
                    BytesMultipartFile file = new BytesMultipartFile(
                            "file", md5 + "." + ext,
                            "image/" + ("jpg".equals(ext) ? "jpeg" : ext),
                            fetched.getBytes());
                    UploadFilesVo vo;
                    try {
                        vo = new StorageDriver().upload(file, "image/" + text.get("id"), AlbumEnum.IMAGE.getCode());
                    } catch (OperateException e) {
                        System.err.println("上传图片失败: " + e.getMessage());
                        continue;
                    }
                    String fileName = vo.getUrl();
                    System.out.println("图片下载成功: " + fileName);

                    //保存到starInstagram中
                    String batch = "";
                    if (text.containsKey("batch")) batch = text.get("batch");

                    StarInstagram starInstagram = new StarInstagram();
                    starInstagram.setStarId(Integer.valueOf(text.get("id")));
                    starInstagram.setCheckCode(md5);
                    starInstagram.setImage(fileName);
                    starInstagram.setBatch(batch);
                    starInstagram.setStatus("Y");
                    starInstagram.setSource( text.get("web"));
                    starInstagram.setCreateTime(System.currentTimeMillis()/1000);

                    starInstagramMapper.insert(starInstagram);
                } else {
                    System.out.println("图片已存在");
                }
            } else {
                break;
            }
        }
    }
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendNotice() {
        //检查明星当日是否有新的照片更新，如有更新则给关注过当前明星的用户发送通知
        List<StarInstagram> starInstagrams = starInstagramMapper.selectList(
                new QueryWrapper<StarInstagram>().eq("status", "Y")
                        .eq("create_time", TimeUtils.dateToTimestamp(DateUtils.formatDate(new Date(),"yyyy-MM-dd")+" 00:00:00")).groupBy("star_id"));
        for (StarInstagram starInstagram : starInstagrams) {
            StarInfo info = starInfoMapper.selectById(starInstagram.getStarId());
            List<UserHistory> list = userHistoryMapper.selectList(new QueryWrapper<UserHistory>().eq("type", "S").in("s_m_id", info.getId()));

            if (list != null && !list.isEmpty()) {
                String msg = String.format("您关注的明星[%s]有新的照片更新！", info.getName());
                List<String> registrationIds = new ArrayList<>();

                for (UserHistory userHistory : list) {
                    User user = userMapper.selectById(userHistory.getUserid());

                    registrationIds.add(user.getRegistrationId());
                    if (registrationIds.size() >= 1000){
                        //发送通知
                        jPushJiGuangService.sendNotice(msg, msg, registrationIds);
                        registrationIds.clear();
                    }
                }
                if (!registrationIds.isEmpty()) {
                    //发送通知
                    jPushJiGuangService.sendNotice(msg, msg, registrationIds);
                }
            }
        }
    }

    @Resource
    CoupleMapper coupleMapper;

    public void grabStarInfo() {
        try {
            List<Couple> list = coupleMapper.selectList(new QueryWrapper<Couple>());
            for (Couple cp : list) {
                if (cp.getStar()!=null && !cp.getStar().isEmpty()) {
                    JSONArray stars = JSONArray.parse(cp.getStar());
                    for (Object star : stars) {
                        StarInfo starInfo = starInfoMapper.selectOne(new QueryWrapper<StarInfo>().eq("id", star));
                        if (starInfo.getX() != null && !starInfo.getX().isEmpty()) {
                            publish("message_channel", new HashMap<String, String>() {{
                                put("id", starInfo.getId().toString());
                                put("web", "ins");
                                put("url", "https://www.instagram.com/" + starInfo.getX());
                                put("first", "1");
                            }});
                        }
                        if (starInfo.getTwitter() != null && !starInfo.getTwitter().isEmpty()) {
                            publish("message_channel", new HashMap<String, String>() {{
                                put("id", starInfo.getId().toString());
                                put("web", "x");
                                put("url", "https://x.com/" + starInfo.getTwitter());
                                put("first", "1");
                            }});
                        }
                    }
                }
            }
         } catch (Exception e) {
             log.error("定时任务抓取电影失败", e);
        }
    }

    public void publish(String channel, Object message) {
        RedisUtils.handler().convertAndSend(channel, message);
        System.out.println("发布消息 - 频道: " + channel + ", 内容: " + JSONObject.toJSONString(message));
    }
}
