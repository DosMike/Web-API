package valandur.webapi.command.auth;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import valandur.webapi.security.AuthenticationProvider;

public class CmdAuthListDisable implements CommandExecutor {
    private boolean whitelist = false;

    public CmdAuthListDisable(boolean whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (whitelist) {
            AuthenticationProvider.toggleWhitelist(false);
            src.sendMessage(Text.of("Disabled whitelist"));
        } else {
            AuthenticationProvider.toggleBlacklist(false);
            src.sendMessage(Text.of("Disabled blacklist"));
        }

        return CommandResult.success();
    }
}
