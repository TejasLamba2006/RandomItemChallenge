# RandomItemChallenge

Periodically gives random items to all players.

## About

Every X seconds, everyone online gets a random item. Could be a diamond. Could be dirt. Could be the new Mace from 1.21. The plugin comes with a curated list of 80+ items, or you can enable all Minecraft items.

## Features

- Configurable drop interval
- Curated item list or all items mode
- Blacklist for items you don't want
- Random enchantments possible
- Multiple delivery methods (inventory, hotbar, or drop)
- Quantity ranges
- World restrictions
- Action bar countdown to next item

## Commands

| Command | Description |
|---------|-------------|
| `/ric start` | Start the challenge |
| `/ric stop` | Stop it |
| `/ric toggle` | Toggle on/off |
| `/ric skip` | Force next item now |
| `/ric interval <seconds>` | Change the interval |
| `/ric status` | View current settings |
| `/ric reload` | Reload config |

## Configuration

**Item Selection**

- Use the built-in curated list (balanced selection of useful and interesting items)
- Or enable all items with a blacklist for things like command blocks

**Quantities**

- Fixed amount per drop
- Or random range (1-16 etc)

**Enchantments**

- Chance for items to come enchanted
- Max enchantment level cap
- Treasure enchantments toggle

**Delivery**

- INVENTORY - Goes in first empty slot
- HOTBAR - Prioritizes hotbar slots
- DROP - Drops at player's feet

## Permissions

| Permission | Description |
|------------|-------------|
| `randomitemchallenge.admin` | Admin commands |
| `randomitemchallenge.receive` | Receive items |
| `randomitemchallenge.bypass` | Don't receive items |

## Support

Discord: <https://discord.gg/7fQPG4Grwt>

## Requirements

- Minecraft 1.21+
- Paper or Spigot
- Java 21+

## License

MIT License
