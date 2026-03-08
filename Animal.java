import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.Random;

public class Animal {

    static String API_KEY = "your api key of groq";

    static StringBuilder conversation = new StringBuilder();
    static String secretAnimal = "";
    static String systemPrompt = ""; // game shuru hone ke baad set hoga

    // ─────────────────────────────────────────
    // FIX 1: Multi-word animals bhi aayenge
    //         Random seed se variety aayegi
    // ─────────────────────────────────────────
    public static String getSecretAnimal() throws Exception {
        String[] hints = {
            "common", "rare", "aquatic", "jungle", "desert",
            "Arctic", "African", "Asian", "domestic", "wild"
        };
        String hint = hints[new Random().nextInt(hints.length)];

        String messages = "["
            + "{\"role\":\"system\",\"content\":\"You are picking a secret animal for a guessing game. "
            + "Pick ONE real animal - it can be 1 or 2 words (e.g. Blue Whale, Snow Leopard, Kangaroo). "
            + "Respond with ONLY the animal name. Nothing else.\"},"
            + "{\"role\":\"user\",\"content\":\"Pick a " + hint + " animal. Reply with only its name.\"}"
            + "]";
        return callGroq(messages, 10).trim().toLowerCase();
    }

    // ─────────────────────────────────────────
    // FIX 2: verifyGuess - strict matching
    // ─────────────────────────────────────────
    public static boolean verifyGuess(String userGuess, String animal) throws Exception {
        String messages = "["
            + "{\"role\":\"system\",\"content\":\"You are a strict judge. Check if two animal names mean the SAME animal. "
            + "Common name, scientific name, or alternate spelling all count as same. "
            + "Reply ONLY with YES or NO.\"},"
            + "{\"role\":\"user\",\"content\":\"Is '" + userGuess.trim().toLowerCase()
            + "' the same animal as '" + animal.trim().toLowerCase() + "'? Reply YES or NO only.\"}"
            + "]";
        String result = callGroq(messages, 5).trim().toUpperCase();
        return result.startsWith("YES");
    }

    // ─────────────────────────────────────────
    // YES/NO question - AI jawab dega
    // ─────────────────────────────────────────
    public static String askAI(String userMessage) throws Exception {
        // FIX 3: Conversation mein properly add karo
        conversation.append("\nHuman: ").append(userMessage);

        StringBuilder sb = new StringBuilder("[");
        sb.append("{\"role\":\"system\",\"content\":").append(toJson(systemPrompt)).append("}");
        for (String line : conversation.toString().split("\n")) {
            if (line.startsWith("Human: "))
                sb.append(",{\"role\":\"user\",\"content\":").append(toJson(line.substring(7))).append("}");
            else if (line.startsWith("Bot: "))
                sb.append(",{\"role\":\"assistant\",\"content\":").append(toJson(line.substring(5))).append("}");
        }
        sb.append("]");

        String result = callGroq(sb.toString(), 5);

        // Sirf YES ya NO accept karo
        String clean = result.toUpperCase().trim();
        if (clean.contains("YES")) clean = "YES";
        else if (clean.contains("NO")) clean = "NO";
        else clean = "NO"; // default

        conversation.append("\nBot: ").append(clean);
        return clean;
    }

    // Core Groq API call
    public static String callGroq(String messagesJson, int maxTokens) throws Exception {
        String body = "{"
            + "\"model\":\"llama-3.3-70b-versatile\","
            + "\"messages\":" + messagesJson + ","
            + "\"max_tokens\":" + maxTokens + ","
            + "\"temperature\":0.5"
            + "}";

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + API_KEY)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
            .send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200) {
            System.out.println("[ERROR] Status: " + resp.statusCode());
            System.out.println("[ERROR] " + resp.body().substring(0, Math.min(200, resp.body().length())));
            return "Error";
        }
        return extractContent(resp.body());
    }

    // ─────────────────────────────────────────
    // FIX 4: Guess detection - sirf clear guesses
    //         Multi-word animals bhi handle honge
    // ─────────────────────────────────────────
    public static String extractGuess(String input) {
        String lower = input.toLowerCase().trim().replace("?", "");

        // Format: "is it a/an [animal]" ya "it is a/an [animal]"
        String[] guessPrefixes = {"is it a ", "is it an ", "it is a ", "it is an "};
        for (String prefix : guessPrefixes) {
            if (lower.startsWith(prefix)) {
                return lower.substring(prefix.length()).trim();
            }
        }

        // Single word guess - but question words nahi hone chahiye
        String[] questionStarters = {
            "does", "is", "can", "do", "has", "are", "was",
            "will", "would", "could", "did", "which", "what",
            "where", "when", "how", "why"
        };
        if (!lower.contains(" ")) {
            for (String q : questionStarters) {
                if (lower.equals(q) || lower.startsWith(q)) return null;
            }
            if (lower.length() > 2) return lower;
        }

        // Two-word guess (e.g. "blue whale", "snow leopard")
        String[] words = lower.split(" ");
        if (words.length == 2) {
            boolean startsWithQuestion = false;
            for (String q : questionStarters) {
                if (words[0].equals(q)) { startsWithQuestion = true; break; }
            }
            if (!startsWithQuestion) return lower;
        }

        return null; // normal question
    }

    static String toJson(String text) {
        return "\"" + text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\"";
    }

    static String extractContent(String json) {
        try {
            int searchFrom = 0;
            while (true) {
                int idx = json.indexOf("\"content\":", searchFrom);
                if (idx == -1) break;
                int pos = idx + 10;
                while (pos < json.length() && json.charAt(pos) == ' ') pos++;
                if (json.startsWith("null", pos)) { searchFrom = pos + 4; continue; }
                if (json.charAt(pos) != '"') { searchFrom = pos + 1; continue; }
                int start = pos + 1, end = start;
                while (end < json.length()) {
                    char c = json.charAt(end);
                    if (c == '\\') end += 2;
                    else if (c == '"') break;
                    else end++;
                }
                String r = json.substring(start, end)
                    .replace("\\n", " ").replace("\\r", "")
                    .replace("\\\"", "\"").replace("\\\\", "\\").trim();
                if (!r.isEmpty()) return r;
                searchFrom = end + 1;
            }
            return "Error";
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("     ? 20 QUESTIONS ANIMAL GAME ?      ");
        System.out.println("========================================\n");

        // Animal choose karo
        System.out.print("AI ek animal choose kar raha hai... ");
        secretAnimal = getSecretAnimal();

        if (secretAnimal.isEmpty() || secretAnimal.equals("error")) {
            System.out.println("API kaam nahi kar rahi. Key check karo.");
            sc.close();
            return;
        }
        System.out.println("Done!\n");

        // FIX 5: System prompt mein animal inject karo
        systemPrompt = "You are playing a 20 questions game. Your secret animal is: \"" + secretAnimal + "\". "
            + "RULES: "
            + "1. NEVER change your animal or reveal its name. "
            + "2. Answer ONLY with YES or NO - single word, nothing else. "
            + "3. Answer honestly based on real facts about " + secretAnimal + ". "
            + "4. If asked about properties, answer based on " + secretAnimal + " only.";

        System.out.println("20 sawaal mein animal guess karo!");
        System.out.println("Guess ke liye: 'is it a [animal]' ya seedha naam likho");
        System.out.println("----------------------------------------");

        // FIX 6: Ek hi counter use karo - sawaalNumber
        int sawaalNumber = 0;
        boolean won = false;
        final int MAX = 20;

        while (sawaalNumber < MAX && !won) {
            System.out.printf("%n[Sawaal %d/20 | %d bache] > ",
                sawaalNumber + 1, MAX - sawaalNumber);

            String input = sc.nextLine().trim();

            // Blank input - count mat karo
            if (input.isEmpty()) continue;

            // FIX 7: "game over" handle karo
            if (input.equalsIgnoreCase("game over") || input.equalsIgnoreCase("give up")) {
                System.out.println("\nTumne give up kar diya!");
                System.out.printf("Secret animal tha: %s%n", secretAnimal.toUpperCase());
                sc.close();
                return;
            }

            sawaalNumber++; // ab count karo

            String guess = extractGuess(input);

            if (guess != null) {
                // ── GUESS ──
                System.out.print("Check kar raha hai...");
                boolean correct = verifyGuess(guess, secretAnimal);

                if (correct) {
                    System.out.println(" CORRECT! ✓");
                    won = true;
                    System.out.println("\n========================================");
                    System.out.printf("  Sahi! Animal tha: %s%n", secretAnimal.toUpperCase());
                    System.out.printf("  Tumne %d sawaalon mein guess kiya!%n", sawaalNumber);
                    System.out.println("========================================");
                } else {
                    System.out.println(" GALAT!");
                    // FIX 3: Conversation mein sahi format mein add karo
                    conversation.append("\nHuman: Is it a ").append(guess).append("?");
                    conversation.append("\nBot: NO");
                }

            } else {
                // ── YES/NO QUESTION ──
                System.out.print("AI soch raha hai...");
                String answer = askAI(input);
                System.out.println(" " + answer);
            }

            // Warning - 5 bache
            if (!won && (MAX - sawaalNumber) == 5) {
                System.out.println("\n  ⚠ Sirf 5 sawaal bache hain! Guess karo!");
            }
        }

        // Game over - haara
        if (!won) {
            System.out.println("\n========================================");
            System.out.println("   20 sawaal khatam! AI jeet gaya!     ");
            System.out.printf ("   Secret animal tha: %s%n", secretAnimal.toUpperCase());
            System.out.println("========================================");
        }

        sc.close();
    }
}

