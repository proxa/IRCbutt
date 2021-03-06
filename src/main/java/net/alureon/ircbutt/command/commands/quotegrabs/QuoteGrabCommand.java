package net.alureon.ircbutt.command.commands.quotegrabs;

import net.alureon.ircbutt.IRCbutt;
import net.alureon.ircbutt.command.Command;
import net.alureon.ircbutt.response.BotIntention;
import net.alureon.ircbutt.response.BotResponse;
import net.alureon.ircbutt.util.IRCUtils;
import net.alureon.ircbutt.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.User;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The QuoteGrabCommand class provides a method for handling all QuoteGrab-related commands.
 */
public final class QuoteGrabCommand implements Command {

    /**
     * The logger for the class.
     */
    private static final Logger log = LogManager.getLogger(QuoteGrabCommand.class);

    @Override
    public BotResponse executeCommand(final IRCbutt butt, final GenericMessageEvent event, final String[] cmd) {
        log.trace("QuoteGrabCommand received the following: " + StringUtils.arrayToString(cmd));
        switch (cmd[0]) {
            case "grab":
                if (cmd.length == 2) {
                    if (cmd[1].equalsIgnoreCase(event.getUser().getNick())) {
                        return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(),
                                "You like grabbing yourself " + event.getUser().getNick() + "?");
                    } else if (cmd[1].equalsIgnoreCase(butt.getYamlConfigurationFile().getBotName())) {
                        return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(),
                                "get your hands off me, creep!");
                    } else {
                        String nickname = IRCUtils.getActualNickname(cmd[1], event);
                        if (butt.getChatStorage().hasQuoteFrom(nickname)) {
                            String quote = butt.getChatStorage().getLastQuoteFrom(nickname);
                            log.trace("Quote grabbed: " + quote);
                                if (!butt.getQuoteGrabTable().quoteAlreadyExists(nickname, quote)) {
                                    butt.getQuoteGrabTable().addQuote(nickname, quote, event.getUser().getNick());
                                    return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "Tada!");
                                } else {
                                    log.debug("User tried to add duplicate quote - not adding duplicate.");
                                    return new BotResponse(BotIntention.NO_REPLY, null, null);
                                }
                        } else {
                            return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(),
                                    "who's " + cmd[1] + "?");
                        }
                    }
                } else {
                    return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "!grab <nick>");
                }
            case "rq":
                if (cmd.length == 1) {
                    String quote = butt.getQuoteGrabTable().getRandomQuoteAndUser();
                    return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                    + " don't have any quotes yet!", event.getUser(),
                            "Database returned no quote - got null");
                } else {
                        String quote = butt.getQuoteGrabTable().getRandomQuoteAndUserFromUser(cmd[1]);
                        return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                        + " don't have any quotes for " + cmd[1],
                                event.getUser(), "Didn't find any quotes from user: " + cmd[1]);
                }
            case "rqn":
            case "rqnouser":
                if (cmd.length == 1) {
                    String quote = butt.getQuoteGrabTable().getRandomQuote();
                    return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                    + " don't have any quotes yet!", event.getUser(),
                            "Database returned no quote - got null");
                } else {
                        String quote = butt.getQuoteGrabTable().getRandomQuoteFromUser(cmd[1]);
                        return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                        + " don't have any quotes from " + cmd[1], event.getUser(),
                                "Database returned no quotes for user: " + cmd[1]);
                }
            case "q":
                if (cmd.length == 1) {
                    return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "!q <nick>");
                } else {
                        String quote = butt.getQuoteGrabTable().getLastQuoteFromUser(cmd[1]);
                        return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                        + " dont' have any quotes from " + cmd[1], event.getUser(),
                                "Database returned no quotes for user: " + cmd[1]);
                }
            case "qinfo":
            case "qi":
                if (cmd.length == 1) {
                    return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "!qinfo <id>");
                } else {
                        String[] quote = butt.getQuoteGrabTable().getQuoteInfo(Integer.parseInt(cmd[1]));
                        return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                        + " found no quote with id " + cmd[1], event.getUser(),
                                "Found no quote with id of " + cmd[1] + " in the database.");
                }
            case "qsay":
                if (cmd.length == 1) {
                    return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "!qsay <id>");
                } else {
                    String quote = butt.getQuoteGrabTable().getQuoteById(Integer.parseInt(cmd[1]));
                    return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                    + " don't find no quote with id " + cmd[1], event.getUser(),
                            "Found no quote in the database with id: " + cmd[1]);
                }
            case "qfind":
            case "qsearch":
            case "qf":
                if (cmd.length == 1) {
                    return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "!qfind <string>");
                } else {
                    String quote = butt.getQuoteGrabTable().findQuote(StringUtils.getArgs(cmd));
                    return getQuoteResponse(quote, butt.getYamlConfigurationFile().getBotNickName()
                                    + " didnt find nothin", event.getUser(),
                            "Found no quotes matching the search string: " + StringUtils.arrayToString(cmd));
                }
            case "qdel":
            case "qdelete":
            case "qrm":
            case "qremove":
                if (IRCUtils.isOpInBotChannel(butt, event.getUser())) {
                    try {
                        int id = Integer.parseInt(StringUtils.getArgs(cmd));
                        if (butt.getQuoteGrabTable().removeQuote(id)) {
                            return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "ok "
                                    + butt.getYamlConfigurationFile().getBotNickName() +  " wont member that no more");
                        } else {
                            return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(),
                                    butt.getYamlConfigurationFile().getBotNickName() + " didn't find no quote"
                                            + " with that id");
                        }
                    } catch (NumberFormatException ex) {
                        return new BotResponse(BotIntention.HIGHLIGHT, event.getUser(), "that ain't no number butt"
                                + " ever heard of");
                    }
                } else {
                    return new BotResponse(BotIntention.PRIVATE_MESSAGE, event.getUser(),
                            "You must be OP to remove quotes.");
                }
            default:
                break;
        }
        log.error("Fell through entire switch of QuoteGrabCommand without hitting a branch.");
        log.error("Received: " + StringUtils.arrayToString(cmd));
        return new BotResponse(BotIntention.NO_REPLY, null, null);
    }

    /**
     * Helper method that standardizes the way we respond to quote queries.
     * @param quote The quote (or null String) that we retrieved from the database.
     * @param noQuoteMessage The message we will give the user if we found no quotes.
     * @param user The user to highlight.
     * @param logMessage The message we will log to the console.
     * @return the bot's response based on the input and whether or not the quote was null.
     */
    private BotResponse getQuoteResponse(final String quote, final String noQuoteMessage, final User user,
                                         final String logMessage) {
        if (quote != null) {
            return new BotResponse(BotIntention.CHAT, null, quote);
        } else {
            log.debug(logMessage);
            return new BotResponse(BotIntention.HIGHLIGHT, user, noQuoteMessage);
        }
    }

    /**
     * Helper method that standardizes the way we respond to quote queries.
     * @param quote The array of data we retrieved from the database.
     * @param noQuoteMessage The message we will give the user if we found no quotes.
     * @param user The user to highlight.
     * @param logMessage The message we will log to console.
     * @return the bot's response based on the input and whether or not the quote was null.
     */
    private BotResponse getQuoteResponse(final String[] quote, final String noQuoteMessage, final User user,
                                         final String logMessage) {
         if (quote != null) {
            return new BotResponse(BotIntention.CHAT, null, quote[0], quote[1]);
        } else {
            log.debug(logMessage);
            return new BotResponse(BotIntention.HIGHLIGHT, user, noQuoteMessage);
        }
    }

    @Override
    public ArrayList<String> getCommandAliases() {
        return new ArrayList<>(Arrays.asList("qfind", "qsearch", "qf", "rq", "qsay", "qinfo", "qi", "q", "rqn",
                "rqnouser", "grab", "qdel", "qdelete", "qrm", "qremove"));
    }

    @Override
    public boolean allowsCommandSubstitution() {
        return true;
    }
}
