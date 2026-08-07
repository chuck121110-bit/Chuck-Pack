package net.aero.aeropack.modules.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ai Chat — Aero Pack
 * Automatically responds to server chat games using AI.
 * Providers: OpenAI, Anthropic, Google, Groq
 */
public class AiChat extends Module {

    public enum Provider { OpenAI, Anthropic, Google, Groq }

    // ── Setting groups ────────────────────────────────────────────────────────

    private final SettingGroup sgApi    = settings.createGroup("API");
    private final SettingGroup sgFilter = settings.createGroup("Chat Filter");
    private final SettingGroup sgPrompt = settings.createGroup("Prompt");
    private final SettingGroup sgTiming = settings.createGroup("Timing");
    private final SettingGroup sgLimits = settings.createGroup("Limits");
    private final SettingGroup sgDebug  = settings.createGroup("Debug");

    // ── API ───────────────────────────────────────────────────────────────────

    private final Setting<Provider> provider = sgApi.add(new EnumSetting.Builder<Provider>()
        .name("Provider")
        .description("Which AI provider to use.")
        .defaultValue(Provider.Groq)
        .build()
    );

    private final Setting<String> apiKey = sgApi.add(new StringSetting.Builder()
        .name("API Key")
        .description("Your API key. Stored in plaintext — keep your config folder private.")
        .defaultValue("")
        .build()
    );

    private final Setting<String> model = sgApi.add(new StringSetting.Builder()
        .name("Model")
        .description("OpenAI: gpt-4o-mini | Anthropic: claude-haiku-4-5 | Google: gemini-1.5-flash-8b | Groq: llama-3.1-8b-instant")
        .defaultValue("llama-3.1-8b-instant")
        .build()
    );

    // ── Chat filter ───────────────────────────────────────────────────────────

    private final Setting<Boolean> listenPlayerChat = sgFilter.add(new BoolSetting.Builder()
        .name("Listen to Player Messages")
        .description("Respond to messages sent by players in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> listenServerMessages = sgFilter.add(new BoolSetting.Builder()
        .name("Listen to Server Messages")
        .description("Respond to messages sent by the server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> useFilter = sgFilter.add(new BoolSetting.Builder()
        .name("Enable Filter")
        .description("Only respond to messages containing one of the keywords below.")
        .defaultValue(true)
        .build()
    );

    private final Setting<List<String>> filterKeywords = sgFilter.add(new StringListSetting.Builder()
        .name("Keywords")
        .description("Only respond if the message contains one of these (case insensitive).")
        .defaultValue(List.of(
            "trivia", "quiz", "question", "answer", "unscramble",
            "first to type", "first to say", "prize", "winner",
            "guess", "riddle", "solve", "correct answer",
            "chat game", "type the word", "what is", "who is",
            "how many", "true or false", "math", "scramble"
        ))
        .visible(useFilter::get)
        .build()
    );

    // ── Prompt ────────────────────────────────────────────────────────────────

    private final Setting<Boolean> useCustomPrompt = sgPrompt.add(new BoolSetting.Builder()
        .name("Custom Prompt")
        .description("Use a custom system prompt instead of the built-in trivia prompt.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> customPrompt = sgPrompt.add(new StringSetting.Builder()
        .name("System Prompt")
        .description("Your custom system prompt.")
        .defaultValue("Answer the following chat message as briefly as possible.")
        .visible(useCustomPrompt::get)
        .build()
    );

    // ── Timing ────────────────────────────────────────────────────────────────

    private final Setting<Integer> fixedDelay = sgTiming.add(new IntSetting.Builder()
        .name("Fixed Delay (ms)")
        .description("Always wait this long before responding.")
        .defaultValue(100).min(0).sliderRange(0, 10000)
        .build()
    );

    private final Setting<Integer> minDelay = sgTiming.add(new IntSetting.Builder()
        .name("Min Random Delay (ms)")
        .description("Minimum random extra time added on top of fixed delay.")
        .defaultValue(0).min(0).sliderRange(0, 10000)
        .build()
    );

    private final Setting<Integer> maxDelay = sgTiming.add(new IntSetting.Builder()
        .name("Max Random Delay (ms)")
        .description("Maximum random extra time added on top of fixed delay.")
        .defaultValue(0).min(0).sliderRange(0, 10000)
        .build()
    );

    // ── Limits ────────────────────────────────────────────────────────────────

    private final Setting<Boolean> enableSessionLimit = sgLimits.add(new BoolSetting.Builder()
        .name("Session Limit")
        .description("Limit API calls per session to save tokens.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> sessionLimit = sgLimits.add(new IntSetting.Builder()
        .name("Max Requests Per Session")
        .description("Maximum API calls per session.")
        .defaultValue(20).min(1).sliderRange(1, 200)
        .visible(enableSessionLimit::get)
        .build()
    );

    private final Setting<Integer> maxTokens = sgLimits.add(new IntSetting.Builder()
        .name("Max Tokens")
        .description("Maximum tokens in each AI response. Keep low to save budget.")
        .defaultValue(32).min(8).sliderRange(8, 256)
        .build()
    );

    private final Setting<Boolean> humanize = sgLimits.add(new BoolSetting.Builder()
        .name("Humanize")
        .description("Occasionally answers slightly wrong to seem less suspicious.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> mistakeChance = sgLimits.add(new IntSetting.Builder()
        .name("Mistake Chance (%)")
        .description("Percentage chance of intentionally answering slightly wrong.")
        .defaultValue(30).min(1).sliderRange(1, 100)
        .visible(humanize::get)
        .build()
    );

    // ── Debug ─────────────────────────────────────────────────────────────────

    private final Setting<Boolean> showErrors = sgDebug.add(new BoolSetting.Builder()
        .name("Show Errors")
        .description("Show API errors as Meteor messages (only you can see them).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showResponses = sgDebug.add(new BoolSetting.Builder()
        .name("Show Responses")
        .description("Show a Meteor message each time the AI responds.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showFiltered = sgDebug.add(new BoolSetting.Builder()
        .name("Show Filtered")
        .description("Show messages that were filtered out (useful for debugging keywords).")
        .defaultValue(false)
        .build()
    );

    // ── State ─────────────────────────────────────────────────────────────────

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AeroAiChat");
        t.setDaemon(true);
        return t;
    });

    private final Random random = new Random();
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicLong lastResponseTime = new AtomicLong(0);

    private static final String DEFAULT_PROMPT =
        "You are a chat game answering bot. Your ONLY job is to output the answer. Nothing else. " +
        "RULES: " +
        "1. Output ONLY the answer. No explanations, no punctuation, no labels, no 'the answer is', no attempts, no reasoning. " +
        "2. If you output anything other than the raw answer you will be disqualified. " +
        "3. True/false questions: respond with only 'true' or 'false' in lowercase. " +
        "4. Math questions: respond with only the number. " +
        "5. Unscramble tasks: the letters are scrambled. Find the ONE real English or Minecraft word those letters make. Output only that word. Do not guess multiple times. Think carefully then output only the final answer. " +
        "6. First to type tasks: output only the exact word shown. " +
        "7. All other questions: output only the shortest possible answer. " +
        "The server name is {SERVER_NAME}. " +
        "Hints: [ Amethyst geodes spawn at Y level and below in 1.18 -> 30, " +
        "Minecraft moon phases match real life -> true ]";

    // ── Constructor ───────────────────────────────────────────────────────────

    public AiChat() {
        super(
            Categories.Misc,
            "Ai Chat Games",
            "Automatically responds to server chat games using AI. Set your API key in the API settings group."
        );
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void onActivate() {
        requestCount.set(0);
        lastResponseTime.set(0);
        if (apiKey.get().isBlank()) {
            error("No API key set. Open Ai Chat Games settings and enter your API key.");
            toggle();
        }
    }

    // ── Chat listener ─────────────────────────────────────────────────────────

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (mc.player == null || mc.getConnection() == null) return;

        String message = null;

        if (event.packet instanceof ClientboundDisguisedChatPacket sysPacket) {
            if (!listenServerMessages.get()) return;
            message = sysPacket.message().getString();
        } else if (event.packet instanceof ClientboundPlayerChatPacket chatPacket) {
            if (!listenPlayerChat.get()) return;
            message = chatPacket.unsignedContent() != null
                ? chatPacket.unsignedContent().getString()
                : chatPacket.body().content();
        }

        if (message == null || message.isBlank()) return;

        String playerName = mc.player.getName().getString();
        if (message.contains("<" + playerName + ">") || message.startsWith(playerName + ":")) return;

        if (useFilter.get()) {
            String lower = message.toLowerCase();
            boolean matched = filterKeywords.get().stream()
                .anyMatch(kw -> lower.contains(kw.toLowerCase()));
            if (!matched) {
                if (showFiltered.get()) info("Filtered: " + message);
                return;
            }
        }

        if (enableSessionLimit.get() && requestCount.get() >= sessionLimit.get()) {
            if (requestCount.get() == sessionLimit.get()) {
                log("Session limit reached (" + sessionLimit.get() + "). Toggle off/on to reset.", true);
                requestCount.incrementAndGet();
            }
            return;
        }

        final String finalMessage = message;
        executor.submit(() -> handleMessage(finalMessage));
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    private void handleMessage(String chatMessage) {
        try {
            String response = callApi(buildSystemPrompt(), chatMessage);
            if (response == null || response.isBlank()) return;

            response = response.strip().replaceAll("[\"']", "").replaceAll("\n", " ").trim();
            if (response.isBlank()) return;

            response = stripPreamble(response);
            if (response.isBlank()) return;

            if (humanize.get() && random.nextInt(100) < mistakeChance.get()) {
                response = humanizeResponse(response);
            }

            int totalDelay = fixedDelay.get();
            if (maxDelay.get() > minDelay.get()) {
                totalDelay += minDelay.get() + random.nextInt(maxDelay.get() - minDelay.get());
            } else {
                totalDelay += minDelay.get();
            }
            if (totalDelay > 0) Thread.sleep(totalDelay);

            final String toSend = response;
            if (mc.player != null && mc.getConnection() != null) {
                mc.execute(() -> {
                    mc.player.connection.sendChat(toSend);
                    lastResponseTime.set(System.currentTimeMillis());
                    requestCount.incrementAndGet();
                    if (showResponses.get()) info("Responded: " + toSend);
                });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log("Error: " + e.getMessage(), true);
        }
    }

    // ── API calls ─────────────────────────────────────────────────────────────

    private String callApi(String system, String user) throws Exception {
        return switch (provider.get()) {
            case OpenAI    -> callOpenAi(system, user);
            case Anthropic -> callAnthropic(system, user);
            case Google    -> callGoogle(system, user);
            case Groq      -> callGroq(system, user);
        };
    }

    private String callOpenAi(String system, String user) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", model.get());
        body.addProperty("max_tokens", maxTokens.get());
        JsonArray messages = new JsonArray();
        JsonObject sys = new JsonObject(); sys.addProperty("role", "system"); sys.addProperty("content", system);
        JsonObject usr = new JsonObject(); usr.addProperty("role", "user");   usr.addProperty("content", user);
        messages.add(sys); messages.add(usr);
        body.add("messages", messages);

        HttpResponse<String> res = http.send(HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey.get())
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .timeout(Duration.ofSeconds(15)).build(),
            HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        if (json.has("error")) { log("OpenAI: " + json.getAsJsonObject("error").get("message").getAsString(), true); return null; }
        return json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
    }

    private String callAnthropic(String system, String user) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", model.get());
        body.addProperty("max_tokens", maxTokens.get());
        body.addProperty("system", system);
        JsonArray messages = new JsonArray();
        JsonObject usr = new JsonObject(); usr.addProperty("role", "user"); usr.addProperty("content", user);
        messages.add(usr);
        body.add("messages", messages);

        HttpResponse<String> res = http.send(HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey.get())
            .header("anthropic-version", "2023-06-01")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .timeout(Duration.ofSeconds(15)).build(),
            HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        if (json.has("error")) { log("Anthropic: " + json.getAsJsonObject("error").get("message").getAsString(), true); return null; }
        return json.getAsJsonArray("content").get(0).getAsJsonObject().get("Component").getAsString();
    }

    private String callGoogle(String system, String user) throws Exception {
        String combined = system + "\n\nChat message to respond to: " + user;
        JsonObject part = new JsonObject(); part.addProperty("Component", combined);
        JsonArray parts = new JsonArray(); parts.add(part);
        JsonObject contentObj = new JsonObject(); contentObj.add("parts", parts);
        JsonArray contents = new JsonArray(); contents.add(contentObj);
        JsonObject genConfig = new JsonObject(); genConfig.addProperty("maxOutputTokens", maxTokens.get());
        JsonObject body = new JsonObject(); body.add("contents", contents); body.add("generationConfig", genConfig);

        HttpResponse<String> res = http.send(HttpRequest.newBuilder()
            .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model.get() + ":generateContent?key=" + apiKey.get()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .timeout(Duration.ofSeconds(30)).build(),
            HttpResponse.BodyHandlers.ofString());

        JsonObject json;
        try { json = JsonParser.parseString(res.body()).getAsJsonObject(); }
        catch (Exception e) { log("Google: invalid response", true); return null; }

        if (json.has("error")) { log("Google: " + json.getAsJsonObject("error").get("message").getAsString(), true); return null; }

        JsonElement candidatesEl = json.get("candidates");
        if (candidatesEl == null || !candidatesEl.isJsonArray() || candidatesEl.getAsJsonArray().isEmpty()) {
            if (json.has("promptFeedback")) log("Google blocked: " + json.get("promptFeedback").toString(), true);
            else log("Google: no candidates", true);
            return null;
        }

        JsonObject candidate = candidatesEl.getAsJsonArray().get(0).getAsJsonObject();
        if (candidate.has("finishReason")) {
            String reason = candidate.get("finishReason").getAsString();
            if (!reason.equals("STOP") && !reason.equals("MAX_TOKENS")) { log("Google stopped: " + reason, true); return null; }
        }

        JsonElement contentEl = candidate.get("content");
        if (contentEl == null || !contentEl.isJsonObject()) { log("Google: no content", true); return null; }
        JsonElement partsEl = contentEl.getAsJsonObject().get("parts");
        if (partsEl == null || !partsEl.isJsonArray() || partsEl.getAsJsonArray().isEmpty()) { log("Google: no parts", true); return null; }

        String fallback = null;
        for (JsonElement p : partsEl.getAsJsonArray()) {
            if (!p.isJsonObject()) continue;
            JsonObject pObj = p.getAsJsonObject();
            JsonElement textEl = pObj.get("Component");
            if (textEl == null || textEl.getAsString().isBlank()) continue;
            boolean isThought = pObj.has("thought") && pObj.get("thought").getAsBoolean();
            if (!isThought) return textEl.getAsString();
            if (fallback == null) fallback = textEl.getAsString();
        }
        if (fallback != null) return fallback;
        log("Google: all parts empty", true);
        return null;
    }

    private String callGroq(String system, String user) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", model.get());
        body.addProperty("max_tokens", maxTokens.get());
        JsonArray messages = new JsonArray();
        JsonObject sys = new JsonObject(); sys.addProperty("role", "system"); sys.addProperty("content", system);
        JsonObject usr = new JsonObject(); usr.addProperty("role", "user");   usr.addProperty("content", user);
        messages.add(sys); messages.add(usr);
        body.add("messages", messages);

        HttpResponse<String> res = http.send(HttpRequest.newBuilder()
            .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey.get())
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .timeout(Duration.ofSeconds(15)).build(),
            HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        if (json.has("error")) { log("Groq: " + json.getAsJsonObject("error").get("message").getAsString(), true); return null; }
        return json.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String stripPreamble(String response) {
        if (response.contains("\n")) response = response.substring(0, response.indexOf("\n")).trim();
        if (response.contains("(")) response = response.substring(0, response.indexOf("(")).trim();

        // Strip <think>...</think> blocks
        response = response.replaceAll("(?s)<think>.*?</think>", "").trim();

        String[] prefixes = {
            "the answer is ", "the answer is: ", "the correct answer is ", "the correct answer is: ",
            "answer: ", "answer is ", "answer is: ", "correct answer: ", "correct: ",
            "it is ", "it's ", "that is ", "that's ", "i think ", "i believe ",
            "the word is ", "the unscrambled word is ", "result: ", "result is ",
            "solution: ", "response: ", "my answer is ", "my answer: ",
            "final answer: ", "final answer is "
        };

        boolean changed = true;
        while (changed) {
            changed = false;
            String lower = response.toLowerCase();
            for (String prefix : prefixes) {
                if (lower.startsWith(prefix)) {
                    response = response.substring(prefix.length()).trim();
                    changed = true;
                    break;
                }
            }
        }

        response = response.replaceAll("[.!?,;:]+$", "").trim();

        String[] words = response.split("\\s+");
        if (words.length > 1) {
            String firstWord = words[0].replaceAll("[,;:.!?()'\"]+$", "");
            if (firstWord.length() <= 15) response = firstWord;
        }

        return response.trim();
    }

    private String humanizeResponse(String response) {
        try {
            double num = Double.parseDouble(response.trim());
            int nudge = (random.nextInt(5) + 1) * (random.nextBoolean() ? 1 : -1);
            if (num == Math.floor(num)) return String.valueOf((long) num + nudge);
            return String.valueOf(Math.round((num + nudge * 0.1) * 100.0) / 100.0);
        } catch (NumberFormatException ignored) {}

        if (response.trim().length() < 3) return response;
        char[] chars = response.trim().toCharArray();
        int idx = random.nextInt(chars.length - 1);
        char tmp = chars[idx]; chars[idx] = chars[idx + 1]; chars[idx + 1] = tmp;
        return new String(chars);
    }

    private void log(String msg, boolean isError) {
        if (!showErrors.get() && isError) return;
        mc.execute(() -> error(msg));
    }

    private String buildSystemPrompt() {
        if (useCustomPrompt.get()) return customPrompt.get();
        String serverName = "Unknown";
        if (mc.getCurrentServer() != null) {
            serverName = mc.getCurrentServer().name.isBlank()
                ? mc.getCurrentServer().ip
                : mc.getCurrentServer().name;
        } else if (mc.isSingleplayer()) {
            serverName = "Singleplayer";
        }
        return DEFAULT_PROMPT.replace("{SERVER_NAME}", serverName);
    }
}
