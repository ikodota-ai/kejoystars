package com.mdd.front;// ... 其他 import


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.common.entity.user.UserSession;
import com.mdd.common.enums.ClientEnum;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.user.UserSessionMapper;
import com.mdd.common.util.StringUtils;
import com.mdd.common.util.YmlUtils;
import com.mdd.front.cache.TokenLoginCache;
import com.mdd.front.service.k.IStarInstagramService;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ImageAccessInterceptor implements HandlerInterceptor {

    // ... logger 定义
    private final Logger logger = LoggerFactory.getLogger(ImageAccessInterceptor.class);

    @Resource
    IStarInstagramService starInstagramService;

    @Resource
    UserSessionMapper userSessionMapper;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        // ... 判断是否为图片的逻辑
        String requestUri = request.getRequestURI();
        String s = request.getQueryString();
        boolean isImage = requestUri.endsWith(".jpg") || requestUri.endsWith(".png") || requestUri.endsWith(".gif");
        if (isImage) {
            String remoteAddr = request.getRemoteAddr();
            String method = request.getMethod();
            logger.info("图片访问记录 - IP: {}, 访问方式: {}, 资源路径: {}, TOKEN: {}", remoteAddr, method, requestUri, request.getHeader("token"));
            //判断是否带?down
            if (s != null && s.contains("down")) {
                //检查是否下载过，没有下载过则检查是否有下载次数，同步保存到数据库
                //取得图片名字
                // 读取请求令牌
                String token = request.getHeader(YmlUtils.get("sa-token.token-name"));
                LikeFrontThreadLocal.put("token", token);
                // 记录当前平台
                String terminal = request.getHeader("terminal");
                if (StringUtils.isEmpty(terminal)) {
                    //userSessionMapper.
                    if (StringUtils.isEmpty(token)) {
                        LikeFrontThreadLocal.put("terminal", ClientEnum.PC.getCode());
                    } else {
                        UserSession userSession = userSessionMapper.selectOne(new QueryWrapper<UserSession>().eq("token", token).gt("expire_time", System.currentTimeMillis() / 1000).orderByDesc("id").last("limit 1"));
                        LikeFrontThreadLocal.put("terminal", StringUtils.isNull(userSession) ? ClientEnum.H5.getCode() : userSession.getTerminal());
                    }
                } else {
                    LikeFrontThreadLocal.put("terminal", terminal);
                }

                String[] split = requestUri.split("/");
                String imageName = split[split.length - 1].replaceAll(".jpg", "").replaceAll(".png", "").replaceAll(".gif", "");
                if (imageName.matches(".*_\\d{4}$")) {
                    String md5ImageName = DigestUtils.md5Hex(split[split.length - 1]);
                    logger.info("imagename: {}, md5hex: {}, ", imageName, md5ImageName);
                    imageName = md5ImageName;
                }

//                Integer userid = LikeFrontThreadLocal.getUserId();
                Integer userid = TokenLoginCache.get();
                if (userid == 0) {
                    throw new OperateException("请先登录");
                }
                return starInstagramService.checkDownload(userid, imageName);
            }
        }
        return true;
    }
}
