package com.mort.shelflauncher.steamshelflauncher.service.SteamStoreService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class SteamStoreServiceImpl implements SteamStoreService {

    private String fetchGameData(long gameId) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create("https://store.steampowered.com/api/appdetails?appids=" + gameId)).
                GET().build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return response.body();

    }


    // not used for now
    public Optional<String> fetchHeaderImageUrl(long gameId) throws IOException, InterruptedException {

        String response = fetchGameData(gameId);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response);
        JsonNode gameNode = root.get(String.valueOf(gameId));

        if(gameNode == null || gameNode.get("success").isNull() || !gameNode.get("success").asBoolean() ||
                gameNode.get("data").isNull() || gameNode.get("data").get("header_image").isNull()){
            return Optional.empty();
        }


        return Optional.of(gameNode.get("data").get("header_image").asText());


    }

    public String[] fetchGamePublisher(long gameId) throws IOException, InterruptedException {

        String response = fetchGameData(gameId);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response);
        JsonNode gameNode = root.get(String.valueOf(gameId));



        if(gameNode == null || gameNode.get("success").isNull() || !gameNode.get("success").asBoolean() ||
                gameNode.get("data").isNull() || gameNode.get("data").get("publishers").isNull()){
            return null;
        }

        return objectMapper.convertValue(gameNode.get("data").get("publishers"), String[].class);





    }

    @Override
    public Optional<String> fetchLibraryCoverUrl(long gameId) {

        String url = "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/" + gameId + "/library_600x900.jpg";

        return Optional.of(url);

    }

}
