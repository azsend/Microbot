# AIO Slayer Plugin

A comprehensive slayer automation plugin for Microbot that handles slayer tasks from start to finish.

## Features

- **Full Task Automation**: Automatically gets tasks from slayer masters, travels to locations, and completes tasks
- **Inventory Setup Integration**: Uses the inventory setups plugin to load task-specific equipment and supplies
- **Combat Management**: Handles food consumption, prayer potions, combat potions, and special attacks
- **Cannon Support**: Automatically places and refills cannons for cannon-compatible tasks
- **Special Kill Mechanics**: Handles special requirements like gargoyle rock hammers and lizard ice coolers
- **Smart Looting**: Loots valuable items, performs high alchemy, and can bury bones
- **Banking Integration**: Automatically banks loot and restocks supplies
- **Comprehensive Task Support**: Supports all major slayer tasks with individual configurations

## Configuration

### General Settings
- **Slayer Master**: Choose your preferred slayer master (Nieve, Duradel, etc.)
- **Enable Banking**: Toggle banking for supplies and loot
- **Break Handler**: Enable break handler for anti-detection

### Combat Settings
- **Food Type**: Specify what food to eat (e.g., "shark", "monkfish")
- **Eat at HP**: Health threshold for eating food
- **Drink Prayer at**: Prayer level threshold for drinking prayer potions
- **Use Combat Potions**: Enable combat potion consumption
- **Special Attack**: Configure special attack usage and threshold

### Looting Settings
- **Minimum Item Value**: Minimum GP value for looting items
- **High Alchemy**: Enable high alchemy on valuable items
- **Alchemy Profit Threshold**: Minimum profit required for alching
- **Bury Bones**: Automatically bury bones for prayer XP
- **Additional Items**: Comma-separated list of additional items to always loot

### Task-Specific Configurations
Each supported task has three configuration options:
- **Inventory Setup**: Name of the inventory setup to use for this task
- **Action**: Kill, Skip, or Block the task
- **Cannon**: Whether to use a cannon for this task (if supported)

## Setup Instructions

1. **Create Inventory Setups**: 
   - Use the MInventory Setups plugin to create loadouts for each slayer task
   - Name them clearly (e.g., "Abyssal Demons", "Dust Devils")
   - Include all necessary gear, food, potions, and special items

2. **Configure Task Settings**:
   - Set the inventory setup name for each task you want to automate
   - Choose Kill/Skip/Block for each task
   - Enable cannon usage for applicable tasks

3. **Set Combat Preferences**:
   - Configure food type and eating threshold
   - Set prayer potion consumption level
   - Enable combat potions and special attacks as desired

4. **Configure Looting**:
   - Set minimum item value for looting
   - Configure high alchemy settings
   - Add any special items you always want to loot

## Supported Tasks

The plugin supports all major slayer tasks including:
- Aberrant Spectres
- Abyssal Demons
- Ankou
- Black Demons
- Bloodveld
- Dust Devils
- Gargoyles (with rock hammer support)
- Nechryael
- And many more...

## Task-Specific Features

### Cannon-Compatible Tasks
Tasks like Black Demons, Dust Devils, and Nechryael support cannon usage for faster kills and AFK training.

### Special Kill Requirements
- **Gargoyles**: Automatically uses rock hammer when gargoyles reach low HP
- **Lizards**: Uses ice cooler to finish lizards at low HP
- **Rockslugs**: Supports bag of salt finishing (planned)
- **Mutated Zygomites**: Supports fungicide spray (planned)

### High-Value Tasks
Tasks like Abyssal Demons and Gargoyles are optimized for profit with intelligent looting and high alchemy.

## State Machine

The plugin operates using a sophisticated state machine:
1. **Initializing**: Bot startup and preparation
2. **Checking Task**: Verify current slayer task
3. **Getting Task**: Travel to slayer master for new task
4. **Banking**: Load inventory setup and restock supplies
5. **Traveling**: Navigate to task location using webwalker
6. **Setting Up Cannon**: Place and set up cannon if configured
7. **Combat**: Fight monsters with full combat support
8. **Looting**: Collect valuable drops and perform alchemy
9. **Special Kill**: Handle special finishing requirements
10. **Task Complete**: Return to slayer master for new task

## Requirements

- Microbot client
- MInventory Setups plugin enabled
- Properly configured inventory setups for desired tasks
- Sufficient supplies and equipment for chosen tasks

## Tips

- Start with simple, profitable tasks like Gargoyles or Abyssal Demons
- Ensure your inventory setups include all necessary items for the task
- Test each setup manually before enabling automation
- Monitor the bot initially to ensure proper configuration
- Use the overlay to track progress and current state

## Safety Features

- Respects the break handler for anti-detection
- Includes antiban measures and randomization
- Stops safely on errors or unexpected conditions
- Comprehensive logging for debugging and monitoring

## Support

This plugin is part of the Microbot framework. For issues or feature requests, please refer to the main Microbot documentation and community resources.
