package com.adjustclient.utilities.auth;

import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.ResourceLines;
import com.adjustclient.utilities.name.WordNameGenerator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

public final class RandomNames {
    public static final String[] adjectives;
    public static final String[] nouns;
    public static final String[] names;
    static {
        try {
            adjectives = ResourceLines.read(new ResourceLocation("adjust:random/adjectives.txt"));
            nouns = ResourceLines.read(new ResourceLocation("adjust:random/nouns.txt"));
            names = ResourceLines.read(new ResourceLocation("adjust:random/names.txt"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static String randomUsername() {
        return new WordNameGenerator(RandomUtil.RANDOM).generate();
    }
    public static RandomSkin randomSkin() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URI("https://api.novaskin.me/v2/random").toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0");
            connection.setConnectTimeout(5000);
            String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
            JsonArray posts = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("posts");
            List<JsonElement> players = new ArrayList<>();
            for (JsonElement post : posts) {
                if (post.isJsonObject() && post.getAsJsonObject().get("model").getAsString().equals("player")) players.add(post);
            }
            if (players.isEmpty()) return null;
            String id = players.get(RandomUtil.nextInt(players.size())).getAsJsonObject().get("id").getAsString();
            HttpsURLConnection details = (HttpsURLConnection) new URI("https://api.novaskin.me/v2/post/" + id).toURL().openConnection();
            details.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0");
            details.setConnectTimeout(5000);
            JsonObject post = JsonParser.parseString(IOUtils.toString(details.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
            String texture = post.get("texture").getAsString();
            return new RandomSkin("https://t.novaskin.me/" + texture, "classic");
        } catch (IOException | URISyntaxException e) {
            return null;
        }
    }
}
