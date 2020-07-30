package josegamerpt.realregions;

import josegamerpt.realregions.config.Config;
import josegamerpt.realregions.gui.WorldViewer;
import josegamerpt.realregions.utils.Text;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Command implements CommandExecutor {
    String nop = "&cSorry but you don't have permission to use this command.";

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if ((sender instanceof Player)) {
            Player p = (Player) sender;
            if ((cmd.getName().equalsIgnoreCase("regions")) && (p.hasPermission("RealRegions.Admin"))) {
                switch (args.length) {
                    case 0:
                        printHelp(p);
                        break;
                    case 1:
                        switch (args[0].toLowerCase()) {
                            case "create":
                                Text.send(p, "&cWrong usage. &f/regions create <name>");
                                break;
                            case "reload":
                            case "rl":
                                Config.reload();
                                RealRegions.prefix = Text.color(Config.file().getString("RealRegions.Prefix"));
                                Text.send(p, "&aReloaded.");
                                break;
                            case "menu":
                                WorldViewer wv = new WorldViewer(p);
                                wv.openInventory(p);
                                break;
                            default:
                                Text.send(p, "&fNo command has been found with that syntax.");
                                break;
                        }
                        break;
                    case 2:
                        switch (args[0].toLowerCase()) {
                            case "create":
                                String name = args[1];
                                break;
                            default:
                                Text.send(p, "&fNo command has been found with that syntax.");
                                break;
                        }
                        break;
                    default:
                        Text.send(p, "&fNo command has been found with that syntax.");
                        break;
                }
            } else {
                Text.send(p, nop);
            }
        } else {
            System.out.print("Only players can execute this command.");
        }
        return false;
    }

    private void printHelp(Player p) {
        Text.sendList(p,
                Arrays.asList("", "         &fReal&eRegions", "&7Release &a" + RealRegions.getPL().getDescription().getVersion(), "",
                        "/region menu (opens the RealRegions Menu)", "/region pos1", "/region pos2", "/region expV (expands the selection vertically)"));
    }
}
