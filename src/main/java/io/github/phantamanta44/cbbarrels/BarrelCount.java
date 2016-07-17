package io.github.phantamanta44.cbbarrels;

class BarrelCount {

	private static final String TIMES_STACK_PLUS = " \u00D7 64 + ";
	private static final int TSP_LEN = TIMES_STACK_PLUS.length();

	int stacks, singles;

	BarrelCount(int stacks, int singles) {
		this.stacks = stacks;
		this.singles = singles;
	}

	int addItems(int count) {
		singles += count;
		while (singles >= 64 && stacks < 64) {
			singles -= 64;
			stacks++;
		}
		if (stacks >= 64) {
			int left = singles;
			singles = 0;
			return left;
		}
		return 0;
	}

	int removeItems(int count) {
		int total = stacks * 64 + singles;
		if (total <= count) {
			stacks = singles = 0;
			return total;
		}
		total -= count;
		singles = total % 64;
		stacks = (total - singles) / 64;
		return count;
	}

	int tryRemoveStack(int size) {
		return removeItems(size);
	}

	boolean isEmpty() {
		return stacks == 0 && singles == 0;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (stacks > 0)
			b.append(stacks).append(TIMES_STACK_PLUS);
		return b.append(singles).toString();
	}

	static BarrelCount fromString(String s) {
		int split = s.indexOf(TIMES_STACK_PLUS);
		if (split == -1)
			return new BarrelCount(0, Integer.parseInt(s));
		return new BarrelCount(Integer.parseInt(s.substring(0, split)), Integer.parseInt(s.substring(split + TSP_LEN, s.length())));
	}

}
