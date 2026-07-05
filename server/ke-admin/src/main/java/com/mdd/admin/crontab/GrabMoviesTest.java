package com.mdd.admin.crontab;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.admin.service.k.IMoviesInfoService;
import com.mdd.admin.service.k.ITmdbService;
import com.mdd.admin.validate.k.MoviesInfoCreateValidate;
import com.mdd.common.entity.k.MoviesInfo;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.k.MoviesInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class GrabMoviesTest {
    public static Map<String, String> movieUrls = new HashMap<String, String>();
    @Autowired
    ITmdbService tmdbService;
    @Autowired
    IMoviesInfoService iMoviesInfoService;

    @Autowired
    MoviesInfoMapper moviesInfoMapper;

    public void grab(){

    }



    static int index = 0;

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
        do {
            movieUrls.clear();
            for (String s : url) {
                try {
                    log.info("开始抓取：{}", s);
                    this.grabMovieUrl(s);
                    log.info("开始抓取电影信息");
                    for (Map.Entry<String, String> entry : movieUrls.entrySet()) {
                        log.info("开始抓取电影:{},地址：{}", entry.getKey(), entry.getValue());
                        try {
                            JSONObject mv = tmdbService.grabMovie(entry.getValue());
                            //保存影视信息
                            MoviesInfoCreateValidate createValidate = new MoviesInfoCreateValidate();
                            createValidate.setMovieName(mv.getString("movieName"));
                            createValidate.setContent(mv.getString("description"));
                            createValidate.setImage(mv.getString("image"));

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = sdf.parse(mv.getString("releaseDate"));
                            createValidate.setStartPlayTime(date);

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
                            Thread.sleep(5000);
                        } catch (HttpStatusException e) {
                            log.error("抓取电影失败，被阻止，退出等待下次任务！", e);
                            return;
                        } catch (OperateException | ExecutionException | ConnectException e){
                            log.error("抓取电影地址失败0", e);
                            Thread.sleep(5000);
                        }
                    }
                } catch (IOException | InterruptedException | ParseException |
                         OperateException e ) {
                    log.error("抓取电影地址失败1", e);
                    return;
                }
            }
        } while (!movieUrls.isEmpty());
        index += 25;
        log.info("电影抓取完成，index:{}", index);
    }

    public void grabMovieUrl(String u) throws IOException, InterruptedException {
        if (u == null || u.isEmpty() || index >= 400) return;
        do {
            for (int i = index; i < index + 25; i++) {
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
                                    if (!movieUrls.containsKey(movieUrl) && moviesInfoMapper.selectCount(new QueryWrapper<MoviesInfo>().eq("movie_name", movieName)) == 0) {
                                        if (!movieUrl.matches(".*?zh-CN.*?")) movieUrl = movieUrl + "?language=zh-CN";
                                        movieUrls.put(movieName, "https://www.themoviedb.org" + movieUrl);
                                    }
                                }
                            }
                        });
                    });
                } catch (HttpStatusException e) {
                    log.error("抓取电影失败，被阻止，休眠2小时！", e);
                    return;
                }
            }
        } while ( movieUrls.isEmpty() );
    }
}
