package com.mdd.admin;

import com.alibaba.excel.util.FileUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.admin.service.k.IMoviesInfoService;
import com.mdd.admin.service.k.ITmdbService;
import com.mdd.admin.validate.k.MoviesInfoCreateValidate;
import com.mdd.common.entity.k.MoviesInfo;
import com.mdd.common.entity.k.StarInfo;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.k.MoviesInfoMapper;
import com.mdd.common.mapper.k.StarInfoMapper;
import com.mdd.common.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LikeAdminApplication.class})
public class GrabMoviesTest {
    public static Map<String, String> movieUrls = new LinkedHashMap<String, String>(){{
        put("某种运算法则","https://www.themoviedb.org/movie/493405");
    }};
    @Autowired
    ITmdbService tmdbService;
    @Autowired
    IMoviesInfoService iMoviesInfoService;

    @Autowired
    MoviesInfoMapper moviesInfoMapper;
    @Test
    public void test1() {

    }
    @Test
    public void grab() {
        String url = "https://mydramalist.com/list/1R88rJN3";
        List<Map<String, String>> list = new ArrayList<>();
        try {
            for (int i = 1; i <= 7; i++) {
                Connection connect = Jsoup.connect(url + "?page=" + i);
                //加入请求头
                connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0");
                connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                connect.header("Accept-Language", "en-US,en;q=0.9");
//                connect.header("Accept-Encoding", "gzip, deflate, br, zstd");
                connect.header("Connection", "keep-alive");
                connect.header("Cookie", "_ga_9MWM3T9VD7=GS2.1.s1772362700$o9$g1$t1772364017$j60$l0$h0; _ga=GA1.1.1583697086.1770160687; cw-test-00000000_htlbid-tude_0_100=tude; cw-test-20250625_prebid-v2-test_1_99=control; cw-test-20251104_5x5-uid2-status_50_50=disabled; cw-test-00000000_5x5-hem-disabled_50_50=control; cw-test-20250807_uid-2-test_100_0_0=enabled@sha256_absent; cto_bundle=shMAcF8wUXc2OGpPcVZRRU1MWFZoOUs1RGJFR3cxSUVIUEVVNHowWlF0VUNzY09QcGRHYThoalRmUG9UaTR3amZnOXo0RjZxaiUyRmZkd2NvY2VpbUVKbVhITGFscmpONGxMamxZejhFQTBsSXNRMlM2SGlWdkVmZ1dxT0NFUHprJTJGUGVCenY1Mk9NRCUyRm45UEtkaUZ2ZUglMkJLdkpsVW01VmZYWUVjTElRc3BOTUxNWDdrU2k4JTJGbDNod1NPbSUyQkdjcFc0JTJCdktTZQ; cto_bidid=BEtkUl9KZFZKU2FaRUElMkZNREIlMkZYUmJvc3BKbHU2RUU0VzVydDRkVXBZY1pqdTYlMkZvVEhLWCUyQlBEd1MyVmtUUzZJTDdIbmhLTWtveVAlMkJDNm0lMkJOMTNnZFNSRlBRSWZiMndMZkxmTVAwcURQdks4UHFESzJIWEdqSUxxczdvQyUyQlc5S0VkeVN6S0ViSDlQMnJuVW8wNjBNRnJvWGIzZyUzRCUzRA; __qca=P1-0a506ac2-e6fd-4f32-afb6-73e504799940; cto_dna_bundle=7jSsOF9OSGhaRXNPaHBuakdLbnBETiUyRkttUW42MTI0djV5NmxOTjJIMWlraWVTSW02cG9ZdGslMkZiUkhSdFElMkZqNzllaG54eGhZTmIwa2RHOEl5cjNaSE04aWZIQSUzRCUzRA; hb_insticator_uid=54e89929-8243-4a1e-83f0-a19c1fb556d1; __gads=ID=110235488f99f54b:T=1770160997:RT=1772362704:S=ALNI_MbP9LFCbeE8sEZBbBd3SOIg2WkAdg; __gpi=UID=00001335a38bff2c:T=1770160997:RT=1772362704:S=ALNI_MZVN71WLvhJBwgSZc-_KwoB3DS8vg; __eoi=ID=1d2757ccc4d9cd1a:T=1770160997:RT=1772362704:S=AA-Afja9_uLo6sxstqjMiqydeMuK");
                connect.header("Upgrade-Insecure-Requests", "1");
                connect.header("Sec-Fetch-Dest", "document");
                connect.header("Sec-Fetch-Mode", "navigate");
                connect.header("Sec-Fetch-Site", "none");
                connect.header("Sec-Fetch-User", "?1");

                Document doc = connect.get();
                //读取回来的是乱码

                System.out.println(doc.title());
                Elements elements = doc.select("ul.list-group li");
                log.info("当前而共{}条电影！", elements.size());
                for (int j = 0; j < elements.size(); j++) {
                    /**
                     * <li id="mdl-11183" class="list-group-item"> <div class="row"> <div class="col-xs-2">
                     *     <a class="film-cover cover" href="/11183-the-handmaiden">
                     *         <img class="img-responsive cover lazy entered loaded" data-src="https://i.mydramalist.com/2Nlv2t.jpg" alt="The Handmaiden" data-ll-status="loaded" src="https://i.mydramalist.com/2Nlv2t.jpg"></a>
                     *         </div>
                     *         <div class="col-xs-10 content"> <div class="row no-gutter">
                     *             <div class="col-xs-6"> <h2 class="text-primary title">1. <a href="/11183-the-handmaiden" title="The Handmaiden">The Handmaiden</a>
                     *             <a class="btn simple btn-manage-list" rel="nofollow" data-id="11183" data-stats="mylist:11183"><span>
                     *                 <i class="far fa-plus"></i>
                     *             </span></a> </h2> <p class="text-muted">Korean Movie - 2016</p> </div> <div class="col-xs-4 text-center group-item-rating">
                     *                 <i class="far fa-star"></i>   </div> <div class="col-xs-2 text-center group-item-rating">  <i class="far fa-star"></i>  </div> </div>  </div> </div> </li>
                     */
                    //提取url, 图片，电影名称，p.text-muted的内容组成csv
                    String href = elements.get(j).select("a.film-cover").attr("href");
                    String img = elements.get(j).select("img.img-responsive").attr("data-src");
                    String title = elements.get(j).select("h2.text-primary a").text();
                    String text = elements.get(j).select("p.text-muted").text();
                    Map<String, String> map = new HashMap<>();
                    map.put("href", href);
                    map.put("img", img);
                    map.put("title", title);
                    map.put("text", text);
                    list.add(map);
                }
            }
            //保存csv文件中
            File file = new File("E:\\movie.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            log.info("抓取{}条数据！", list.size());
            //写入csv文件
            StringBuilder csv = new StringBuilder();
            for (Map<String, String> map : list) {
                csv.append(map.get("href")).append(",").append(map.get("img")).append(",").append(map.get("title")).append(",").append(map.get("text")).append("\n");
            }
            try (FileOutputStream fis = new FileOutputStream(file)) {
                fis.write(csv.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void grabSearchTmdb() throws IOException {
        String url = "https://www.themoviedb.org/search/movie?query=";
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        int count = 0;
        //读取moive.csv文件
        File file = new File("E:\\movie.csv");
        try (FileInputStream fis = new FileInputStream(file)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                String movieName = split[2].trim();

                Map<String, String> map = new HashMap<>();
                map.put("source", "https://mydramalist.com" + split[0]);
                map.put("sourceTitle", movieName);
                list.add(map);
            }

            while (count < list.size()) {
                try{
                    for (Map<String, String> map : list) {
                        if (map.containsKey("ok")) continue;
                        String movieUrl = url + map.get("sourceTitle");
                        Connection connect = Jsoup.connect(movieUrl);
                        //加入请求头
                        connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:147.0) Gecko/20100101 Firefox/147.0");
                        connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                        connect.header("Accept-Language", "en-US,en;q=0.9");
                        connect.header("Connection", "keep-alive");

                        Document doc = connect.get();
                        Elements elements = doc.select("div.white_column div.movie div.results div.card");
                        map.put("href", "");
                        map.put("title", "");
                        map.put("tmdb", "");
                        if (!elements.isEmpty()) {
                            if (elements.size() > 1) {
                                map.put("href", movieUrl);
                                map.put("ok", "2");
                            } else {
                                Element el = elements.get(0).selectFirst("div.details div.title a");
                                String title = Objects.requireNonNull(el.selectFirst("h2")).text().replace(",", "，");
                                map.put("href", "https://www.themoviedb.org" + el.attr("href"));
                                map.put("title", title);
                                if (title.equals(map.get("sourceTitle"))) {
                                    map.put("ok", "1");
                                } else {
                                    map.put("ok", "-2");
                                }
                            }
                        } else {
                            map.put("ok", "0");
                        }
                        count++;
                        Thread.sleep(10000);
                    }
                } catch (Exception ignored){}
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        //保存csv文件中
        File f = new File("E:\\movies.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        log.info("抓取{}条数据！", list.size());
        //写入csv文件
        StringBuilder csv = new StringBuilder();
        for (Map<String, String> map : list) {
            csv.append(map.get("source")).append(",").append(map.get("sourceTitle")).append(",").append(map.get("href")).append(",").append(map.get("title")).append(",").append(map.get("ok")).append("\n");
        }
        try (FileOutputStream fs = new FileOutputStream(f)) {
            fs.write(csv.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static int index = 50;
    @Autowired
    StarInfoMapper starInfoMapper;
    @Test
    public void T(){
        List<StarInfo> starInfos = starInfoMapper.selectList(new QueryWrapper<StarInfo>().isNotNull("birthplace"));
        for (StarInfo starInfo : starInfos) {
            if (starInfo.getBirthplace() == null) continue;
            String birthplace = starInfo.getBirthplace();
            String[] split = birthplace.split(",");
            if (split.length> 0) {
                switch (split[split.length-1].trim().toLowerCase()){
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
                starInfoMapper.updateById(starInfo);
            }
        }
    }

    @Test
    public void test(){
        String[] url = {
                "https://www.themoviedb.org/movie",
//                "https://www.themoviedb.org/tv",
//                "https://www.themoviedb.org/movie/now-playing",
//                "https://www.themoviedb.org/movie/upcoming",
//                "https://www.themoviedb.org/movie/top-rated",
//                "https://www.themoviedb.org/tv/top-rated",
//                "https://www.themoviedb.org/tv/airing-today",
//                "https://www.themoviedb.org/tv/on-the-air"
        };
//        do {
//            movieUrls.clear();
            for (String s : url) {
                try {
                    log.info("开始抓取：{}", s);
//                    this.grabMovieUrl();
                    log.info("开始抓取电影信息");
                    for (Map.Entry<String, String> entry : movieUrls.entrySet()) {
                        if ("".equals(entry.getValue())) continue;
                        log.info("开始抓取电影:{},地址：{}", entry.getKey(), entry.getValue());
                        try {
                            JSONObject mv = tmdbService.grabMovie(entry.getValue());
                            if (mv == null) {
                                entry.setValue("");
                                Thread.sleep(10000);
                                continue;
                            }
                            //保存影视信息
                            MoviesInfoCreateValidate createValidate = new MoviesInfoCreateValidate();
                            createValidate.setMovieName(mv.getString("movieName"));
                            createValidate.setContent(mv.getString("description"));
                            createValidate.setImage(mv.getString("image"));

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            if (mv.getString("releaseDate") != null && !"".equals(mv.getString("releaseDate"))) {
                                Date date = sdf.parse(mv.getString("releaseDate"));
                                createValidate.setStartPlayTime(date);
                            }

                            String status = "W";
                            switch (mv.getString("status")) {
                                case "已上映":
                                    status = "P";
                                    break;
                                case "拍摄中 ":
                                case "后期制作":
                                    status = "W";
                                    break;
                                case "已完结":
                                    status = "D";
                                    break;
                                case "回归剧":
                                    status = "D";
                                    break;

                            }
                            createValidate.setStatus(status);
                            int country = 14;
                            switch (mv.getString("language")) {
                                case "英语":
                                    country = 15;
                                    break;
                                case "汉语":
                                    country = 14;
                                    break;
                                case "日语":
                                    country = 18;
                                    break;
                                case "法语":
                                    country = 22;
                                    break;
                                case "朝鲜语":
                                    country = 20;
                                    break;
                                case "意大利语":
                                case "德语":
                                    country = 17;
                                    break;
                                case "泰语":
                                    country = 18;
                                    break;
                            }
                            createValidate.setCountry(country);
                            for (Object item : mv.getJSONArray("femaleActors")) {
                                JSONObject items = (JSONObject) item;
                                items.put("star", items.getString("name"));
                                items.remove("name");
                                items.remove("href");
                            }
                            createValidate.setActorList(mv.getJSONArray("femaleActors").toJSONString());
                            iMoviesInfoService.add(createValidate);
                            log.info("影视【{}】抓取完成......", createValidate.getMovieName());
                            entry.setValue("");
                            Thread.sleep(10000);
                        } catch (HttpStatusException e) {
                            log.error("抓取电影失败，被阻止，休眠2小时！", e);
                            Thread.sleep(3600000 * 3);
                        } catch (OperateException | ExecutionException | ConnectException | SSLException e) {
                            log.error("抓取电影地址失败0", e);
                            Thread.sleep(10000);
                        }
                    }
                } catch (IOException | InterruptedException | ParseException |
                         OperateException e ) {
                    log.error("抓取电影地址失败1", e);
                    return;
                }
            }
//            try {
//                log.info("进入休眠一小时！");
//                Thread.sleep(3600000);
//            } catch (Exception ignored) {}
//        } while (!movieUrls.isEmpty());
//        log.info(JSONArray.toJSONString( movieUrls ));
        index += 20;
        log.info("电影抓取完成，共{}个", 0);
    }

    public void grabMovieUrl() {
        File file = new File("E:\\movies.csv");
        try (FileInputStream fis = new FileInputStream(file)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                if (!"0".equals(split[4])) {
                    movieUrls.put(split[1], split[2]);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void grabMovieUrl(String u) throws IOException, InterruptedException {
        if (u == null || u.isEmpty()) return;
        for (int i = 50; i < 75; i++) {
            String url = u + "?page=" + i;
            if (!url.matches(".*?zh-CN.*?")) url = url + "&language=zh-CN";
            try {
                Connection connect = Jsoup.connect(url);
                Document doc = connect.get();
                doc.select("section.results div.page_wrapper").forEach((e) -> {
                    //取得影视地址
                    Elements movies = e.select("div.style_1 a");
                    movies.forEach((movie) -> {
                        if (movie != null) {
                            String movieUrl = movie.attr("href");
                            String movieName = movie.text();
                            if (!movieUrl.isEmpty() && !movieName.isEmpty()) {
    //                    movieUrl = "https://www.themoviedb.org" + movieUrl;
                                //检查movieUrls是否已经存在
                                if (!movieUrls. containsKey(movieUrl) && moviesInfoMapper.selectCount(new QueryWrapper<MoviesInfo>().eq("movie_name", movieName)) == 0) {
                                    if (!movieUrl.matches(".*?zh-CN.*?")) movieUrl = movieUrl + "?language=zh-CN";
                                    movieUrls.put(movieName, "https://www.themoviedb.org" + movieUrl);
                                }
                            }
                        }
                    });
                });
            } catch (HttpStatusException e){
                log.error("抓取电影失败，被阻止，休眠2小时！", e);
                Thread.sleep(3600000 * 2);
            }
            //取得下一页地址
//            Element next = doc.selectFirst("div.page_wrapper div.pagination p.load_more a.no_click");
//            if (next != null) {
//                url = "https://www.themoviedb.org" + next.attr("href");
//            } else {
//                url = "";
//            }
        }
    }
}
