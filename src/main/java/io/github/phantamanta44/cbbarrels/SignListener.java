package io.github.phantamanta44.cbbarrels;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class SignListener implements Listener { // TODO: craftbook integration

	private static final Pattern RGX_ID = Pattern.compile("(?:\\d+(?::\\d+)?!?)?");
	private static final Pattern RGX_COUNT = Pattern.compile("(?:[1-6]?\\d \u00D7 64 \\+ )?[1-6]?\\d");

	private CBBPlugin plugin;

	public SignListener(CBBPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onSignEdit(SignChangeEvent event) {
		if (blockCheck(event.getBlock()) && event.getLine(1).equalsIgnoreCase("[barrel]")) {
			try {
				IdInfo id = IdInfo.fromString(event.getLine(2));
				String name = "Empty";
				String idLine = "";
				if (id != null) {
					id.locked = true;
					name = id.toHumanReadable();
					idLine = id.toString();
				}
				event.setLine(0, name);
				event.setLine(1, "[Barrel]");
				event.setLine(2, idLine);
				event.setLine(3, "0");
				event.getPlayer().sendMessage(ChatColor.GOLD + "Barrel created!");
			} catch (NumberFormatException e) {
				event.getPlayer().sendMessage(ChatColor.RED + "Invalid item filter!");
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (blockCheck(event.getClickedBlock())) {
			Sign sign = (Sign)event.getClickedBlock().getState();
			if (signCheck(sign)) {
				IdInfo id = IdInfo.fromString(sign.getLine(2));
				BarrelCount count = BarrelCount.fromString(sign.getLine(3));
				if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					if (id != null && !event.getPlayer().isSneaking() && !count.isEmpty()) {
						Item ent = event.getPlayer().getWorld().dropItem(
								event.getClickedBlock().getLocation(),
								id.genStack(count.tryRemoveStack(Material.getMaterial(id.id).getMaxStackSize())));
						ent.setPickupDelay(0);
						sign.setLine(3, count.toString());
						if (count.isEmpty() && !id.locked) {
							sign.setLine(0, "Empty");
							sign.setLine(2, "");
						}
					}
				} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (!event.getPlayer().isSneaking()) {
						ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
						if (hand == null || hand.getType() == Material.AIR) {
							if (id != null) {
								for (int i = 0; i < event.getPlayer().getInventory().getSize(); i++) {
									ItemStack stack = event.getPlayer().getInventory().getItem(i);
									if (stack != null && id.matches(stack) && !stack.hasItemMeta() && stack.getEnchantments().size() == 0) {
										int left = count.addItems(stack.getAmount());
										if (left > 0) {
											stack.setAmount(left);
											break;
										}
										else
											event.getPlayer().getInventory().setItem(i, null);
									}
								}
							}
						} else if (id == null || id.matches(hand)) {
							if (hand.getEnchantments().size() > 0)
								event.getPlayer().sendMessage(ChatColor.RED + "You cannot store enchanted items in a barrel!");
							else if (hand.hasItemMeta())
								event.getPlayer().sendMessage(ChatColor.RED + "You cannot store items with unique metadata in a barrel!");
							else {
								int left = count.addItems(hand.getAmount());
								if (left > 0)
									hand.setAmount(left);
								else
									event.getPlayer().getInventory().setItemInMainHand(null);
								if (id == null) {
									id = new IdInfo(hand.getTypeId(), hand.getData().getData(), false);
									sign.setLine(0, id.toHumanReadable());
									sign.setLine(2, id.toString());
								}
							}
						}
						sign.setLine(3, count.toString());
					} else {
						if (id == null)
							event.getPlayer().sendMessage(ChatColor.RED + "You cannot lock an empty barrel!");
						else{
							id.locked = !id.locked;
							if (count.isEmpty() && !id.locked) {
								sign.setLine(0, "Empty");
								sign.setLine(2, "");
							} else {
								sign.setLine(0, id.toHumanReadable());
								sign.setLine(2, id.toString());
							}
							event.getPlayer().sendMessage(ChatColor.GOLD + "Barrel " + (id.locked ? "" : "un") + "locked.");
						}
					}
				}
			}
			sign.update(true, false);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (blockCheck(event.getBlock())) {
			Sign sign = (Sign)event.getBlock().getState();
			if (signCheck(sign))
				breakBlock(event.getBlock().getLocation(), sign);
		}
	}

	@EventHandler
	public void onThingsBlowingUp(EntityExplodeEvent event) {
		event.blockList().stream()
				.filter(SignListener::blockCheck)
				.map(block -> (Sign)block.getState())
				.filter(SignListener::signCheck)
				.forEach(sign -> breakBlock(sign.getLocation(), sign));
	}

	@EventHandler
	public void onPhysicsUpdate(BlockPhysicsEvent event) {
		if (event.getBlock().getType() == Material.WALL_SIGN) {
			Block back = getBlockOn(event.getBlock());
			if (back != null && back.getType() == Material.AIR) {
				Sign sign = (Sign)event.getBlock().getState();
				if (signCheck(sign))
					breakBlock(event.getBlock().getLocation(), sign);
			}
		}
	}

	private static void breakBlock(Location blockLoc, Sign sign) {
		IdInfo id = IdInfo.fromString(sign.getLine(2));
		if (id != null) {
			BarrelCount count = BarrelCount.fromString(sign.getLine(3));
			if (!count.isEmpty()) {
				World world = blockLoc.getWorld();
				while (count.stacks-- > 0)
					world.dropItemNaturally(blockLoc, id.genStack(64));
				world.dropItemNaturally(blockLoc, id.genStack(count.singles));
			}
		}
		sign.setLine(1, "");
		sign.update(true, false);
	}

	@SuppressWarnings("deprecation")
	private static Block getBlockOn(Block wallSign) {
		switch (wallSign.getData()) {
			case 2:
				return wallSign.getRelative(BlockFace.SOUTH);
			case 3:
				return wallSign.getRelative(BlockFace.NORTH);
			case 4:
				return wallSign.getRelative(BlockFace.EAST);
			case 5:
				return wallSign.getRelative(BlockFace.WEST);
			default:
				return null;
		}
	}

	private static boolean blockCheck(Block block) {
		if (block == null || block.getType() != Material.WALL_SIGN)
			return false;
		Block back = getBlockOn(block);
		return back != null && (back.getType() == Material.LOG || back.getType() == Material.LOG_2);
	}

	private static boolean signCheck(Sign sign) {
		return sign.getLine(1).equals("[Barrel]")
				&& RGX_ID.matcher(sign.getLine(2)).matches()
				&& RGX_COUNT.matcher(sign.getLine(3)).matches();
	}

}
