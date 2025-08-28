package pon.main.modules.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import meteordevelopment.orbit.EventHandler;
import pon.main.Main;
import pon.main.events.impl.EventTick;
import pon.main.modules.Parent;
import pon.main.modules.settings.*;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.message.MessageType.Parameters;
import net.minecraft.text.Text;

import com.mojang.authlib.GameProfile;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import pon.main.utils.MentionChecker;

public class AutoResponser extends Parent {
    public Setting<Float> delay = new Setting<>("delay (S)", 1f, 0.1f, 5f);
    public Setting<Boolean> autoOutputMentions = new Setting<>("auto mentions output", true);
    public Setting<Integer> sendDelay = new Setting<>("send delay", 4, 1, 6);
    public Setting<Integer> maxTokens = new Setting<>("max tokens", 3000, 100, 10000);

    public Header header = new Header("this module works on https://openrouter.ai", () -> {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://openrouter.ai"));
            } catch (Exception ignored) {}
        } else {
            notify(new Notify.NotifyData(
                "java desktop is not supported on this system",
                Notify.NotifyType.System,
                getNotifyLiveTime()
            ));
        }
    });
    public Setting<String> modelId = new Setting<>("model id", "openai/gpt-oss-20b:free");
    public Setting<String> token = new Setting<>("token", "sk-...");
    public Setting<String> mentions = new Setting<>("mentions", "");
    public HGroup hgroup = new HGroup("constants", modelId, modelId, token, mentions);
    public Group constants = new Group(
        "constants",
        header,
        hgroup
    );
    public KeyButton cancelButton = new KeyButton(
        "cancel button", -1, () -> {
            cancelTask = true;
        }
    );

    private static final Map<String, Deque<JsonObject>> chatHistories = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledTask = null;
    private boolean cancelTask = false;

    public AutoResponser() {
        super("auto responser", Main.Categories.ui);
        // обычные сообщения
        ClientReceiveMessageEvents.CHAT.register(
            (Text message, SignedMessage signedMsg, GameProfile sender, Parameters params, Instant timestamp) -> {
                if (!Parent.fullNullCheck() && enable) {
                    if (sender.getName().equals(mc.getSession().getUsername())) return;

                    processMessage(message.getString(), sender.getName());
                }
            }
        );
        // для серверов с плагинами на чат
        ClientReceiveMessageEvents.GAME.register(
            (Text message, boolean overlay) -> {
                if (!Parent.fullNullCheck() && enable) {
                    String text = message.getString();
                    String senderName = extractSenderName(text);

                    if (senderName == null || senderName.equals(mc.getSession().getUsername())) return;

                    processMessage(message.getString(), senderName);
                }
            }
        );
    }

    @EventHandler
    private void onTick(EventTick e) {
        if (mc.player == null || scheduledTask == null || scheduledTask.isDone()) return;
        if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen || !enable || cancelTask) {
            cancelTask = true;
            scheduledTask.cancel(false);
            resetFlags();

            Notify.NotifyData n = new Notify.NotifyData(
                getName() + " | cancel",
                Notify.NotifyType.Important,
                getNotifyLiveTime()
            );
            notify(n);
        }
    }

    private void resetFlags() {
        cancelTask = false;
    }

    private void scheduleMessageProcessing(String senderName, String text, String prefix) {
        if (scheduledTask != null && !scheduledTask.isDone()) {
            scheduledTask.cancel(false);
        }
        resetFlags();
        scheduledTask = scheduler.schedule(() -> {
            if (!cancelTask) {
                mc.execute(() -> response(senderName, text, prefix));
            }
            resetFlags();
        }, delay.getValue().longValue(), TimeUnit.SECONDS);
    }

    public void response(String senderName, String text, String prefix) {
        new Thread(() -> {
            try {
                Deque<JsonObject> history = chatHistories.computeIfAbsent(senderName, u -> new ArrayDeque<>());
                JsonObject userEntry = new JsonObject();
                userEntry.addProperty("role", "user");
                userEntry.addProperty("content", text);
                history.addLast(userEntry);

                StringBuilder fullResponse = new StringBuilder();
                StringBuilder accumulator = new StringBuilder();
                int maxLength = 256;
                int prefixLength = prefix.length();
                int chunkLength = maxLength - prefixLength;

                getResponse(senderName, (String delta) -> {
                    if (!enable) return;

                    fullResponse.append(delta);
                    String sanitizedDelta = sanitizeMinecraftChat(delta);
                    accumulator.append(sanitizedDelta);
                    while (accumulator.length() >= chunkLength) {
                        String chunk = accumulator.substring(0, chunkLength);
                        accumulator.delete(0, chunkLength);
                        String message = prefix + chunk;
                        if (prefix.startsWith("/")) {
                            String command = message.substring(1);
                            mc.execute(() -> mc.getNetworkHandler().sendChatCommand(command));
                        } else {
                            mc.execute(() -> mc.getNetworkHandler().sendChatMessage(message));
                        }
                        try {
                            int randomDelay = ThreadLocalRandom.current().nextInt(
                                Math.max(1, sendDelay.getValue() - 2),
                                sendDelay.getValue() + 2
                            );
                            Thread.sleep(randomDelay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
                if (!accumulator.isEmpty()) {
                    String remaining = accumulator.toString();
                    String message = prefix + remaining;
                    if (prefix.startsWith("/")) {
                        String command = message.substring(1);
                        mc.execute(() -> Objects.requireNonNull(mc.getNetworkHandler()).sendChatCommand(command));
                    } else {
                        mc.execute(() -> mc.getNetworkHandler().sendChatMessage(message));
                    }
                }

                JsonObject assistantEntry = new JsonObject();
                assistantEntry.addProperty("role", "assistant");
                assistantEntry.addProperty("content", fullResponse.toString());
                history.addLast(assistantEntry);

                while (history.size() > 10) {
                    history.removeFirst();
                }
            } catch (Exception e) {
                Notify.NotifyData n = new Notify.NotifyData(
                    getName() + " | " + e,
                    Notify.NotifyType.System,
                    getNotifyLiveTime()
                );
                notify(n);
            }
        }, "ChatRequest-Thread").start();
    }

    private String extractSenderName(String message) {
        if (message == null) return null;
        int open = message.indexOf('<');
        int close = message.indexOf('>', open);
        if (open == -1 || close == -1 || close <= open + 1) {
            return null;
        }
        String inside = message.substring(open + 1, close);
        return inside.replaceAll("§.", "");
    }

    public void getResponse(String username, Consumer<String> contentConsumer) {
        try {
            Deque<JsonObject> history = chatHistories.get(username);

            JsonObject payload = new JsonObject();
            payload.addProperty("model", modelId.getValue());
            payload.addProperty("include_reasoning", false);
            payload.addProperty("max_tokens", maxTokens.getValue());
            payload.addProperty("stream", true);
            JsonArray messagesArray = new JsonArray();
            for (JsonObject msgObj : history) {
                messagesArray.add(msgObj);
            }
            payload.add("messages", messagesArray);
            String requestBody = new Gson().toJson(payload);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .header("Authorization", "Bearer " + token.getValue())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<Stream<String>> resp = client.send(request, HttpResponse.BodyHandlers.ofLines());
            resp.body().forEach(line -> {
                if (line.startsWith("data: ")) {
                    String data = line.substring(6);
                    if (!data.equals("[DONE]")) {
                        JsonObject chunk = JsonParser.parseString(data).getAsJsonObject();
                        JsonObject choice = chunk.getAsJsonArray("choices").get(0).getAsJsonObject();
                        if (choice.has("delta") && choice.get("delta").getAsJsonObject().has("content")) {
                            String contentDelta = choice.get("delta").getAsJsonObject().get("content").getAsString();
                            contentConsumer.accept(contentDelta);
                        }
                    }
                }
            });
        } catch (Exception ignored) {}
    }

    public static String sanitizeMinecraftChat(String input) {
        if (input == null) return null;
        String noControl = input.replaceAll("\\p{Cc}", "");
        return noControl.replace('\n', ' ').replace('\r', ' ');
    }

    private void processMessage(String text, String senderName) {
        resetFlags();

        String playerName = mc.getSession().getUsername();
        String cleanedText = null;
        String prefix = "";

        if (text.contains("[private]") || text.contains("whispers to you")) {
            int colonIndex = text.indexOf(": ");
            if (colonIndex != -1) {
                cleanedText = text.substring(colonIndex + 2).trim();
            } else {
                String senderPrefix = "<" + senderName + "> ";
                int senderIndex = text.indexOf(senderPrefix);
                if (senderIndex != -1) {
                    cleanedText = text.substring(senderIndex + senderPrefix.length()).trim();
                } else {
                    int privateIndex = text.indexOf("[private]");
                    if (privateIndex != -1) {
                        cleanedText = text.substring(privateIndex + "[private]".length()).trim();
                        cleanedText = cleanedText.replaceAll("^<[^>]+>\\s*", "").trim();
                    } else {
                        cleanedText = text.trim();
                    }
                }
            }
            prefix = "/tell " + senderName + " ";
        } else {
            String messagePart;
            int colonIndex = text.indexOf(": ");
            if (colonIndex != -1) {
                messagePart = text.substring(colonIndex + 2).trim();
            } else {
                String senderPrefix = "<" + senderName + "> ";
                if (text.startsWith(senderPrefix)) {
                    messagePart = text.substring(senderPrefix.length()).trim();
                } else {
                    messagePart = text.trim();
                }
            }

            String mention = MentionChecker.checkMention(messagePart, playerName, autoOutputMentions.getValue(), mentions.getValue());

            if (mention != null) {
                cleanedText = messagePart.replaceAll("(?i)" + Pattern.quote(mention), "").trim();
                String prefixPart = colonIndex != -1 ? text.substring(0, colonIndex) : text;
                if (prefixPart.contains("[clan]")) {
                    prefix = "#";
                } else if (prefixPart.contains("[global]")) {
                    prefix = "!";
                } else {
                    prefix = "";
                }
            }
        }

        if (cleanedText != null) {
            Notify.NotifyData n = new Notify.NotifyData(
                "mentioned! gen will start in " + delay.getValue().toString() + "s, disable module or open the chat to cancel",
                Notify.NotifyType.Important, (int) (delay.getValue() * 20)
            );
            notify(n);
            scheduleMessageProcessing(senderName, cleanedText, prefix);
        }
    }
}
