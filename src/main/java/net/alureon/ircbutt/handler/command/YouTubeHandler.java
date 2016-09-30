package net.alureon.ircbutt.handler.command;

import net.alureon.ircbutt.BotResponse;
import net.alureon.ircbutt.IRCbutt;
import net.alureon.ircbutt.util.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class YouTubeHandler {


    private final static Logger log = LoggerFactory.getLogger(YouTubeHandler.class);


    public static void getYouTubeVideo(IRCbutt butt, BotResponse response, String[] cmd) {
        butt.getMoreHandler().clearMore();
        try {
            String link = "http://www.youtube.com/results?search_query=" + URLEncoder.encode(StringUtils.getArgs(cmd), "utf-8");
            String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";
            try {
                Connection.Response cResponse = Jsoup.connect(link)
                        .ignoreContentType(true)
                        .userAgent(userAgent)
                        .referrer("http://www.google.com")
                        .timeout(12000)
                        .followRedirects(true)
                        .execute();
                Document doc = cResponse.parse();
                Element result = doc.getElementById("results");
                Elements videos = result.getElementsByClass("yt-lockup-title");
                int size = videos.size();
                for (int i = 0; i < size; i++) {
                    Element video = videos.get(i);
                    Attributes attributes = video.child(0).attributes();
                    String url = attributes.get("href");
                    if (i == 0) {
                        response.chat(URLDecoder.decode(video.text(), "utf8") + " http://youtube.com" + url);
                    } else {
                        butt.getMoreHandler().addMore(URLDecoder.decode(video.text(), "utf-8") + " http://youtube.com" + url);
                    }
                }
                butt.getMoreHandler().setNoMore("how about you refine your search terms instead?");
            } catch (IOException | NullPointerException ex) {
                log.error("Found no video", ex);
                response.privateMessage(response.getRecipient(), "Found no video");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
