package net.alureon.ircbutt.handler;

import net.alureon.ircbutt.BotResponse;

/**
 * Created by alureon on 5/10/15.
 */
public class Rot13Handler {

    public void handleRot13(BotResponse response, String in) {
        StringBuilder sb = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                char b = (char) (c + (byte) 13);
                if (b > 'Z') {
                    b -= 26;
                }
                sb.append((char) b);
            } else if (c >= 'a' && c <= 'z') {
                char b = (char) (c + (byte) 13);
                if (b > 'z') {
                    b -= 26;
                }
                sb.append((char) b);
            } else {
                sb.append((char) c);
            }
        }
        response.chat(sb.toString());
    }
}
