package net.alureon.ircbutt.command;

import net.alureon.ircbutt.response.BotResponse;
import net.alureon.ircbutt.IRCbutt;
import net.alureon.ircbutt.command.commands.*;
import net.alureon.ircbutt.command.commands.google.GoogleImageSearchCommand;
import net.alureon.ircbutt.command.commands.google.GoogleSearchCommand;
import net.alureon.ircbutt.command.commands.karma.KarmaCommand;
import net.alureon.ircbutt.math.Eval;
import net.alureon.ircbutt.math.MathLib;
import net.alureon.ircbutt.util.MathUtils;
import net.alureon.ircbutt.util.StringUtils;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.reflections.Reflections;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main CommandHandler for the program.  Any and all commands are routed here, then
 * further routed once the command's type is deduced.
 */
public class CommandHandler {


    /**
     * The instance of the IRCbutt object.
     */
    private IRCbutt butt;


    /**
     * Constructor sets the field of the IRCbutt instance.
     * @param butt The IRCbutt instance.
     */
    public CommandHandler(final IRCbutt butt) {
        this.butt = butt;
    }

    public static Set<Class> getCommandClasses() {
        Reflections reflections = new Reflections("net.alureon.ircbutt");
        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
        for (Class c : classes) {
            System.out.println(c.getName());
        }
        return null;
    }

    public void handleCommand(final GenericMessageEvent event, final String[] cmd) {
        /* For the sake of clearer code, let's just set these immediately */
        User user = event.getUser();
        String nick = user.getNick();

        /* The response of the bot */
        BotResponse response = null;

        /* if it's prefixed with a tilde it's a fact request */
        if (cmd[0].startsWith("~")) {
            response = butt.getFactCommand().handleKnowledge(cmd, user, nick);
            return;
        }

        /* remove the '!' from the command */
        cmd[0] = cmd[0].replaceFirst("!", "");

        if (!cmd[0].equals("learn")) {
            String commandSubstituted = parseCommandSubstitutionAndVariables(butt, response, StringUtils.arrayToString(cmd), nick);
            String[] commandSubstitutedArray = commandSubstituted.split(" ");

            /* switch of main bot commands */
            String result;
            switch (cmd[0]) {
                case "rq":
                case "grab":
                case "q":
                case "qinfo":
                case "qi":
                case "qsay":
                case "qsearch":
                case "qfind":
                case "qf":
                case "rqnouser":
                case "rqn":
                    butt.getQuoteGrabCommand().handleQuoteGrabs(response, commandSubstitutedArray, user, nick);
                    break;
                case "append":
                case "forget":
                case "fact":
                case "factinfo":
                case "factfind":
                case "factsearch":
                case "ffind":
                case "fsearch":
                case "finfo":
                case "factbyid":
                case "factid":
                case "fid":
                case "ff":
                case "fi":
                case "fs":
                    butt.getFactCommand().handleKnowledge(response, commandSubstitutedArray, user, nick);
                    break;
                case "echo":
                    response.chat(StringUtils.getArgs(commandSubstitutedArray));
                    break;
                case "g":
                    GoogleSearchCommand.handleGoogleSearch(butt, response, commandSubstitutedArray);
                    break;
                case "give":
                    result = GiveCommand.handleGive(response, event, user, commandSubstitutedArray);
                    response.chat(result);
                    break;
                case "rot13":
                case "rot":
                    result = Rot13Command.handleRot13(StringUtils.getArgs(commandSubstitutedArray));
                    response.chat(result);
                    break;
                case "yt":
                    YouTubeCommand.getYouTubeVideo(butt, response, commandSubstitutedArray);
                    break;
                case "ud":
                    UrbanDictionaryCommand.getDefinition(butt, response, commandSubstitutedArray);
                    break;
                case "version":
                    // handle version somewhere
                    break;
                case "dice":
                    response = new DiceCommand().executeCommand(butt, commandSubstitutedArray);
                    break;
                case "random":
                    response.chat(String.valueOf(MathUtils.getRandom()));
                    break;
                case "sqrt":
                case "pow":
                    MathLib.handleMath(response, commandSubstitutedArray);
                    break;
                case "check":
                    CheckCommand.handleCheck(response, StringUtils.getArgs(commandSubstitutedArray));
                    break;
                case "define":
                    DefineCommand.handleDefine(butt, response, cmd[1]);
                    break;
                case "invite":
                    InviteCommand.handleInvite(butt, StringUtils.getArgs(cmd).split(" "));
                    break;
                case "more":
                    butt.getMoreCommand().handleMore(response);
                    break;
                case "wr":
                    WakeRoomCommand.handleWakeRoom(response);
                    break;
                case "eval":
                    Eval.eval(response, StringUtils.getArgs(cmd));
                    break;
                case "karma":
                    KarmaCommand.getKarma(butt, response, user, StringUtils.getArgs(cmd));
                    break;
                case "coin":
                    CoinCommand.handleCoin(response);
                    break;
                case "gi":
                    GoogleImageSearchCommand.handleGoogleImageSearch(response, StringUtils.getArgs(cmd));
                    break;
                case "8":
                case "8ball":
                    MagicEightBallCommand.handleMagicEightBall(response);
                    break;
                case "butt":
                case "buttify":
                    String buttified = butt.getButtReplaceHandler().buttFormat(StringUtils.getArgs(commandSubstitutedArray));
                    response.chat(buttified);
                    break;
                default:
                    break;
            }
            if (response.getIntention() == null) {
                response.noResponse();
            }
        } else {
            butt.getFactCommand().handleKnowledge(response, cmd, user, nick);
        }
    }

    private static String parseCommandSubstitutionAndVariables(IRCbutt butt, BotResponse response, String input, String nick) {
        Pattern p = Pattern.compile("\\$\\([^)]*\\)");
        Matcher m = p.matcher(input);
        while (m.find()) {
            String command = m.group().substring(2, m.group().length() - 1);
            BotResponse botResponse = new BotResponse(response.getEvent());
            butt.getCommandHandler().handleCommand(response.getEvent(), command.split(" "), botResponse);
            input = input.replaceFirst(Pattern.quote(m.group()), botResponse.toString());
        }
        return input.replaceAll("\\$USER", nick);
    }

}
