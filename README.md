# RandomItemChallenge

A fun Minecraft plugin that periodically gives random items to all players! Perfect for challenge runs, survival games, or just adding some chaos to your server.

## Features

- **Periodic Random Items** - Players receive random items at configurable intervals
- **Custom Item List** - Choose from 80+ curated items or use ALL Minecraft items
- **Blacklist System** - Exclude any items you don't want appearing
- **Enchantment Support** - Random items may come enchanted
- **Multiple Delivery Methods** - Inventory, hotbar, or drop at player location
- **Visual Effects** - Particles, sounds, and action bar notifications
- **World Filtering** - Restrict to specific worlds if needed
- **Game Mode Filtering** - Only survival/adventure players receive items
- **1.21+ Support** - Includes new items like Mace, Wind Charge, and Breeze Rod!

## Requirements

- Minecraft Server: Paper/Spigot 1.21+
- Java: 21 or higher

## Installation

1. Download the latest `RandomItemChallenge.jar`
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure in `plugins/RandomItemChallenge/config.yml`
5. Use `/ric start` to begin!

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ric start` | Start the challenge | `randomitemchallenge.admin` |
| `/ric stop` | Stop the challenge | `randomitemchallenge.admin` |
| `/ric toggle` | Toggle on/off | `randomitemchallenge.admin` |
| `/ric skip` | Skip to next item drop | `randomitemchallenge.admin` |
| `/ric interval <seconds>` | Set the drop interval | `randomitemchallenge.admin` |
| `/ric status` | View current status | `randomitemchallenge.admin` |
| `/ric reload` | Reload configuration | `randomitemchallenge.admin` |

**Aliases:** `/randomitemchallenge`, `/ric`, `/randomitem`, `/itemchallenge`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `randomitemchallenge.admin` | Access to all admin commands | OP |
| `randomitemchallenge.receive` | Receive random items | All Players |
| `randomitemchallenge.bypass` | Don't receive random items | None |

## Configuration

```yaml
# Enable/disable plugin
enabled: false

# Seconds between each item drop
interval: 30

# Number of items per drop
items-per-drop: 1

# Item Settings
items:
  # Use custom list (true) or all Minecraft items (false)
  use-custom-list: true
  
  # Items that will NEVER be given
  blacklist:
    - COMMAND_BLOCK
    - BEDROCK
    - BARRIER
    # ... more items

# Quantity Settings
quantity:
  random-quantity: true
  min: 1
  max: 16

# Enchantment Settings
enchantments:
  chance: 0.1        # 10% chance
  max-enchants: 2
  max-level: 3
  allow-treasure: false

# Delivery Method: INVENTORY, DROP, or HOTBAR
delivery:
  method: INVENTORY
  show-message: true
  
  # Sound effects
  sound:
    enabled: true
    type: ENTITY_ITEM_PICKUP
    
  # Particle effects
  particles:
    enabled: true
    type: HAPPY_VILLAGER
```

## Use Cases

### Challenge Runs
Start a survival world with random items - forces players to adapt their gameplay based on what they receive!

### Mini-Games
Use for custom mini-games where players must survive with random equipment.

### Events
Run special events where players get surprise items throughout gameplay.

### Chaos Mode
Set a short interval and enable all items for maximum chaos!

## Tips

- **Balance the interval**: 30-60 seconds works well for most servers
- **Use the blacklist**: Remove overpowered or game-breaking items
- **Customize quantities**: Lower quantities for harder challenges
- **Enable enchantments**: Adds variety and excitement
- **World filtering**: Keep it in one world for organized events

## Support

- **Discord**: [https://discord.gg/7fQPG4Grwt](https://discord.gg/7fQPG4Grwt)
- **Issues**: Report bugs on our GitHub

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Made with ❤️ by TejasLamba2006
