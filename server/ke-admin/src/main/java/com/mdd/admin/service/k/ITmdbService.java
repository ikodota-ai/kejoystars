package com.mdd.admin.service.k;

import com.alibaba.fastjson2.JSONObject;
import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ITmdbService {

    public JSONObject grabMovie(String url)  throws InterruptedException, IOException, ExecutionException, ParseException, HttpStatusException;

    /**
     * 演员提取
     */
    public List<JSONObject> grabActors( String url)  throws IOException, InterruptedException, ExecutionException, ParseException ;

    public JSONObject grabStarInfo( String url) throws IOException;

    public String grabImage(String imageUrl, String dir) throws IOException;
}
