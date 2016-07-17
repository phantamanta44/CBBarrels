package io.github.phantamanta44.cbbarrels;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
class IdInfo {

	int id, meta;
	boolean locked;

	IdInfo(int id, int meta, boolean locked) {
		this.id = id;
		this.meta = meta;
		this.locked = locked;
	}

	ItemStack genStack(int amount) {
		return new ItemStack(id, amount, (short)meta);
	}

	boolean matches(ItemStack stack) {
		return stack.getTypeId() == id && stack.getData().getData() == meta;
	}

	public String toHumanReadable() {
		return Material.getMaterial(id).toString() + (meta == 0 ? "" : ("#" + meta));
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder().append(id);
		if (meta != 0)
			b.append(":").append(meta);
		if (locked)
			b.append("!");
		return b.toString();
	}

	static IdInfo fromString(String s) {
		if (s.isEmpty())
			return null;
		boolean locked = s.endsWith("!");
		int id, meta = 0;
		if (s.contains(":")) {
			id = Integer.parseInt(s.substring(0, s.indexOf(":")));
			meta = Integer.parseInt(s.substring(s.indexOf(":") + 1, s.length() - (locked ? 1 : 0)));
		}
		else
			id = Integer.parseInt(locked ? s.substring(0, s.length() - 1) : s);
		return new IdInfo(id, meta, locked);
	}

}
