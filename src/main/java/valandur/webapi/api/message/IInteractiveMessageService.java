package valandur.webapi.api.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The message service allows sending interactive messages to online players.
 * These messages will show up in the chat and feature clickable answers, which will trigger a web hook-like response.
 */
public interface IInteractiveMessageService {

    /**
     * Gets all the messages that were sent since server start.
     * @return The messages sent since server start.
     */
    List<IInteractiveMessage> getMessages();

    /**
     * Sends a new interactive message to a player.
     * @param msg The message to send.
     */
    void sendMessage(IInteractiveMessage msg);

    /**
     * Gets a message by its uuid.
     * @param uuid The uuid of the message.
     * @return An optional containing the message if found, otherwise an empty optional.
     */
    Optional<IInteractiveMessage> getMessage(UUID uuid);
}
