CBBarrels
=====
Simulates the functionality of constructs such as Mekanism's bins, Thermal Expansion's caches, Factorization barrels, etc. through a Bukkit plugin. Essentially, it's a dense storage unit that can carry up to 4096 of one type of item.

# Use
## Creation
To create a barrel, place a log of any kind and place a wall sign on one of its faces. The second line of the sign should read `[Barrel]` and the third line can contain an item filter in the format `id:meta`.
## Item Insertion
Once created, you can insert items (matching the filter) into the barrel by right-clicking with the item in hand. Right-clicking with an empty hand will place all items in your inventory into the barrel that match the item filter.
## Item Extraction
You can left-click the sign to extract one stack of its contents. Alternatively, you can break the sign to drop all of its contents at once, although this is not recommended.
## Locking
You can prevent an item filter from disappearing once the barrel is empty by sneaking and right clicking on the barrel. This is referred to as "locking", and is denoted on the sign by an exclamation point `!` on the item ID line.
