package fi.felixbade.TelegramBotClient;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import fi.felixbade.TelegramBotClient.APIModel.TelegramUpdate;

public class TelegramBotClient {

    public String token;
    private int lastUpdateId = 0;
    private Gson gson = new Gson();

    public TelegramBotClient(String token) {
        this.token = token;
    }

    public void sendMessage(int telegramChatId, String messageToTelegram) {
        new Thread(new Runnable() {
            public void run() {
                blockingSendMessage(telegramChatId, messageToTelegram);
            }
        }).start();
    }

    public void blockingSendMessage(int telegramChatId, String messageToTelegram) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", this.token);

        JsonObject blob = new JsonObject();
        blob.addProperty("text", messageToTelegram);
        blob.addProperty("chat_id", telegramChatId);
        blob.addProperty("parse_mode", "Markdown");

        try {
            HTTPJsonClient.post(url, blob);
        } catch (java.io.IOException e) {
        }
    }


    public TelegramUpdate[] getNextUpdates() {
        String url = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d",
        this.token, lastUpdateId+1);

        JsonObject updates = null;
        try {
            updates = HTTPJsonClient.get(url);
        } catch (IOException e) {
            return new TelegramUpdate[0];
        }

        if (updates == null) {
            return new TelegramUpdate[0];
        }

        if (updates.has("result")) {
            JsonArray updatesBlob = updates.getAsJsonArray("result");
            int updateCount = updatesBlob.size();
            TelegramUpdate[] parsedUpdates = new TelegramUpdate[updateCount];

            int i = 0;
            for (JsonElement blob : updatesBlob) {
                TelegramUpdate update = gson.fromJson(blob, TelegramUpdate.class);
                int updateId = update.update_id;
                if (updateId == this.lastUpdateId) {
                    continue;
                }
                this.lastUpdateId = updateId;
                parsedUpdates[i] = update;
                i++;
            }

            return parsedUpdates;
        }

        return new TelegramUpdate[0];
    }
}
