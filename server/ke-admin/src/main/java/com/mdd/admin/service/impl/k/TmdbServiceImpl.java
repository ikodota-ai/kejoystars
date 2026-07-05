package com.mdd.admin.service.impl.k;

import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.admin.service.k.IStarInfoService;
import com.mdd.admin.service.k.ITmdbService;
import com.mdd.admin.validate.k.StarInfoCreateValidate;
import com.mdd.admin.vo.k.StarInfoDetailVo;
import com.mdd.common.entity.k.MoviesInfo;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.k.MoviesInfoMapper;
import com.mdd.common.util.ImageDownloaderUtil;
import com.mdd.common.util.UrlUtils;
import com.mdd.common.util.YmlUtils;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TmdbServiceImpl implements ITmdbService {
    private static final Logger log = LoggerFactory.getLogger(TmdbServiceImpl.class);
    @Resource
    IStarInfoService iStarInfoService;

    @Resource
    MoviesInfoMapper moviesInfoMapper;

    String domain;

    public JSONObject grabMovie(String url) throws InterruptedException, IOException, ExecutionException, ParseException,HttpStatusException {
        if (url.isEmpty()) return null;
        if (!url.matches(".*?zh-CN.*?")) url = url + "?language=zh-CN";
        // 加载本地HTML文件
        Connection connect = Jsoup.connect( url );
        Document doc;
        doc = connect.get();

        // 剧名
        String title = "";
        Element titleEl = doc.selectFirst("section.header div.title h2 a");
        if (titleEl != null) {
            title = titleEl.text();
            if(moviesInfoMapper.selectCount(new QueryWrapper< MoviesInfo >().eq("movie_name", title)) > 0) throw new OperateException("该电影已存在");
        } else {
            throw new OperateException("网页抓取不完整！");
        }
        // 简介（假设简介在某个p标签或div中，需根据实际页面调整选择器）
        String description = "";
        Element descElem = doc.selectFirst("div.overview p");
        if (descElem != null) {
            description = descElem.text();
        }
        //播出时间
        String releaseDate = "";
        Element edate = doc.selectFirst("div.header div.single_column section.header div.facts span.release");
        if (edate != null) {
            releaseDate = edate.text().replaceAll("^.*?(20\\d{2}\\-\\d{2}\\-\\d{2}).*?$", "$1");
        }
        // 默认语言
        String language = "";
        Element langElem = doc.selectFirst("p strong:contains(默认语言)");
        if (langElem != null && langElem.parent()!=null) {
            language = langElem.parent().text().replace("默认语言", "").trim();
        }

        // 个播出平台
        List<String> platforms = new ArrayList<>();
        Elements platformElems = doc.select("ul.networks li a img");
        for (Element e : platformElems) {
            String alt = e.attr("alt");
            platforms.add(alt);
        }

        // 状态
        String status = "";
                Element statusElem = doc.selectFirst("p strong:contains(状态)");
        if (statusElem != null && statusElem.parent()!=null) {
            status = statusElem.parent().text().replace("状态", "").trim();
        }
        String movieCover = "";
        Element coverElem = doc.selectFirst("main div.blurred img");
        if (coverElem != null) {
            movieCover = coverElem.attr("src");
            if (!movieCover.isEmpty()) {
                try{
                    //下载图片
                    movieCover = grabImage(movieCover,  "image/movies/");
                } catch(OperateException e){
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
        }
        // 构造JSON
        JSONObject result = new JSONObject();
        result.put("movieName", title);
        result.put("description", description);
        result.put("language", language);
        result.put("playPlatform", platforms);
        result.put("status", status);
        result.put("movieCover", UrlUtils.toAbsoluteUrl(movieCover));
        result.put("image",movieCover);
        result.put("releaseDate", releaseDate);
        int country = -1;
        switch (language) {
            case "英语":
                country = 2;
                break;
            case "汉语":
                country = 1;
                break;
            case "日语":
                country = 6;
                break;
            case "法语":
                country = 9;
                break;
            case "朝鲜语":
                country = 7;
                break;
            case "意大利语":
                country = 12;
                break;
            case "德语":
                country = 4;
                break;
            case "泰语":
                country = 5;
                break;
        }
        result.put("country", country);

        domain = url.replaceAll("^(https:|http:)//(.*?)/.*$", "$1//$2");
        List<JSONObject> femaleActors = new ArrayList<>();
        if (doc.selectFirst("section.top_billed li.filler") == null) {
            Elements li = doc.select("section.top_billed li.card");
            List<JSONObject> actors = new ArrayList<>();

            for (Element e : li) {
                JSONObject obj = new JSONObject();
                Element e1 = e.select("p").get(0).selectFirst("a");
                if (e1 != null) {
                    obj.put("name", e1.text());
                    obj.put("href", e1.attr("href"));
                } else continue;

                e1 = e.select("p").get(1);
                if (e1 != null) {
                    obj.put("cosplay", e1.text());
                }

                checkStarInfo(obj, actors);
            }
            femaleActors = actors;
        } else {
            String actorsUrl = Objects.requireNonNull(doc.selectFirst("section.top_billed li.filler a")).attr("href");
            femaleActors = grabActors(actorsUrl);
        }
        result.put("femaleActors", femaleActors);
        return result;
    }

    /**
     * 演员提取
     */
    public List<JSONObject> grabActors( String url) throws IOException, InterruptedException, ExecutionException, ParseException {
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

            Element e = li.select("div.info p").get(0).selectFirst("a");
            String name = "";
            if (e != null) {
                name = e.text();
                obj.put("name", name);
                obj.put("href", e.attr("href"));
            } else continue;

            e = li.select("div.info p").last();
            if (e != null) {
                obj.put("cosplay", e.text());
            }

            checkStarInfo(obj, actors);

            if (actors.size()>=9){
                break;
            }
        }

        return actors;
    }

    public void checkStarInfo(JSONObject obj, List<JSONObject> actors) throws ParseException, InterruptedException, ExecutionException {
        StarInfoDetailVo sioInfo = iStarInfoService.detailByName( obj.getString("name"));
        if (sioInfo ==null) {
            //这里加入多线程进行抓取
            Future<JSONObject> future = Executors.newCachedThreadPool().submit(() -> grabStarInfo(obj.getString("href")));
            JSONObject starInfo = future.get();
            if (starInfo != null && starInfo.get("gender").equals("女")) {
                obj.put("image", starInfo.get("avatar"));

                StarInfoCreateValidate star = new StarInfoCreateValidate();
                star.setName(obj.getString("name"));
                star.setAvatar(starInfo.getString("avatar"));
                String birthdayStr = starInfo.getString("birthday");
                Date birthday = null;
                if (birthdayStr != null && !birthdayStr.isEmpty() && birthdayStr.matches("^(\\d{4})年(\\d{1,2})月(\\d{1,2})日$")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
                    birthday = sdf.parse(birthdayStr);
                }
                star.setBirthday(birthday);
                star.setBirthplace(starInfo.getString("birthPlace"));
                countryConvert(star);
                star.setGender( starInfo.getString("gender") );
                star.setBiography(starInfo.get("biography") != null ? starInfo.getString("biography") : "");
                star.setTwitter(starInfo.getString("twitter"));
                star.setX(starInfo.getString("instagram"));
                star.setOriginal( starInfo.toJSONString() );

                int sid = iStarInfoService.add(star);
                obj.put("sid", sid);

                actors.add(obj);
            }
        } else if(sioInfo.getGender().equals("女")){
            obj.put("sid", sioInfo.getId());
            obj.put("image", sioInfo.getAvatar());
            actors.add(obj);
        }
    }

    public JSONObject grabStarInfo( String url) throws IOException {
        if (url.isEmpty()) return null;
        JSONObject result = new JSONObject();
        if (!url.matches(".*?zh-CN.*?")) url = url + "?language=zh-CN";
        // 加载本地HTML文件
        Connection connect = Jsoup.connect( domain + url );
        Document doc = connect.get();

        Element mainEl = doc.selectFirst("main div.person_v4");

        //封面图片
        assert mainEl != null;

        Element leftEl = mainEl.selectFirst("section.left_column");
        assert leftEl != null;
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
                if ("女".equals(gender)) {
                    Element imageEl = mainEl.selectFirst("section.images");
                    if (imageEl != null) {
                        Element imgEl = imageEl.selectFirst("img");
                        if (imgEl != null) {
                            String avatar = imgEl.attr("src");
                            if (!avatar.isEmpty()) {
                                try {
                                    avatar = grabImage(avatar, "image/star/avatar/");
                                } catch (OperateException e){
                                    String[] srcset = imgEl.attr("srcset").split(",");
                                    for (String s : srcset) {
                                        avatar = s.replaceAll("^\\s?(http[^\\s]+)\\s.*?$", "$1");
                                        try {
                                            avatar = grabImage(avatar, "image/star/avatar/");
                                            break;
                                        } catch (OperateException e1) {
                                            log.error(e1.getMessage());
                                            avatar = imgEl.attr("src");
                                        }
                                    }
                                }
                            }
                            result.put("avatar", avatar);

                        }
                    }
                }
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

    private void countryConvert(StarInfoCreateValidate starInfo){
        if (starInfo.getBirthplace() == null) return ;
        String birthplace = starInfo.getBirthplace();
        String[] split = birthplace.split(",");
        starInfo.setCountry(-1);
        if (split.length> 0) {
            switch (split[split.length - 1].trim().toLowerCase()) {
                case "hong kong":
                case "hongkong":
                case "british hong kong":
                    starInfo.setCountry(10);
                    break;
                case "taiwan":
                case "british crown colony [now china]":
                case "people's republic of china":
                case "zaozhuang，shandong province，china":
                case "taibei，taiwan，china":
                case "ji'nan，shandong province，china":
                case "baoji，shanxi province，china":
                case "中国，台湾，台北":
                case "china)":
                case "杭州":
                case "china":
                    starInfo.setCountry(1);
                    break;
                case "usa":
                case "united states":
                    starInfo.setCountry(2);
                    break;
                case "korea":
                case "义城郡":
                case "boeun - south korea":
                case "seoul - south korea":
                case "south korea":
                    starInfo.setCountry(7);
                    break;
                case "日本 东京":
                case "久喜市":
                case "日本神奈川县":
                case "giappone":
                case "japan":
                case "niigata prefecture - japan":
                case "yamagata prefecture - japan":
                    starInfo.setCountry(6);
                    break;
                case "thailand":
                case "thaïlande":
                    starInfo.setCountry(5);
                    break;
//                    case "india":
//                        starInfo.setCountry(6);
//                        break;
                case "british crown colony":
                case "uk":
                case "england":
                    starInfo.setCountry(3);
                    break;
                case "france":
                    starInfo.setCountry(9);
                    break;
                case "germany":
                case "sweden":
                case "netherlands":
                case "finlandia":
                    starInfo.setCountry(4);
                    break;
            }
        }
    }
}