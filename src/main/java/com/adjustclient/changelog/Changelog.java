package com.adjustclient.changelog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.ChatColor;

public final class Changelog {
    public static final List<ChangelogLink> versions = new ArrayList<>();
    public static final List<ChangelogLink> unreadVersions = new ArrayList<>();
    public static ChangelogLink latest;
    public static ChangelogLink lastRead;
    public static Integer initialUnreadCount;
    public static int changelogInitState;
    public static int contentHash;
    public static int versionCode;
    public static int unreadCount;

    public static ChangelogLink findVersion(int id) {
        return versions.stream().filter(version -> version.id == id)
                .findFirst().orElse(null);
    }

    public static void updateUnread(ChangelogLink readThrough) {
        unreadVersions.clear();
        for (int i = versions.indexOf(readThrough) + 1; i < versions.size(); i++) {
            unreadVersions.add(versions.get(i));
        }
        unreadCount = unreadVersions.stream().mapToInt(version -> version.version.changes.size())
                .sum();
        if (initialUnreadCount == null) initialUnreadCount = unreadCount;
        lastRead = readThrough;
    }

    static {
        changelogInitState = 0;
        ChangelogVersionBuilder builder;

        builder = new ChangelogVersionBuilder();
        builder.add("Public Release");
        versions.add(new ChangelogLink("July 1st", 0, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Kill Aura:Disable On} options to {Kill Aura}");
        builder.add("Added {HUD:Scale} option to {HUD}");
        builder.add("Added {HUD:Tab GUI} options to {HUD}");
        builder.improve("Fixed stuttering on native executable");
        builder.improve("Fixed pressing enter to open party chat");
        builder.improve("Fixed cookie accounts crashing on Windows");
        builder.improve("Synchronized toggle states between modules with matching binds");
        versions.add(new ChangelogLink("July 2nd", 1, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Third Person:Free Look} and merged {Third Person:View Clip} into new {Third Person} module");
        builder.add("Added {Item Animations:Old transform} option to Item Animations module");
        builder.add("Added 'reset' option to {*Configuration} command");
        builder.add("Added separate {HUD:Click GUI->Scale} option to Click GUI");
        builder.add("Added {Safe Walk}");
        builder.add("Added {Fast Place}");
        builder.improve("Improved {Scaffold:Mode:Hypixel} {Scaffold:Addons->Tower} when strafing");
        builder.improve("Improved {No Slowdown:Mode:Hypixel} {No Slowdown} with usable items");
        builder.improve("Improved tab competition in commands");
        builder.improve("Improved party chat bind press detection");
        builder.improve("Fixed party chat invisible messages");
        builder.improve("Made drag and drop possible in add account");
        versions.add(new ChangelogLink("July 3rd", 2, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Auto Clicker}");
        builder.add("Added validation options to {Safe Walk}");
        builder.improve("{HUD:Global Chat->Mode:Global chat / IRC} is now available in main chat");
        builder.improve("Interactive mouse cursor states");
        builder.improve("Show currently logged-on account");
        builder.improve("Fixed full-screen bug");
        versions.add(new ChangelogLink("July 4th", 3, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added configuration binding via .bind config <config>");
        builder.add("Added {Kill Aura:Hold attack} option to {Kill Aura}");
        builder.improve("Fixed Dynamic FOV issues with {Sprint} {Sprint:Server state} option");
        builder.improve("Automatically close chests in {Inventory} module");
        builder.improve("Improved Hypixel {Scaffold:Addons->No Ascend} place detection");
        builder.improve("Fixed {Auto Clicker} clicking in inventories");
        versions.add(new ChangelogLink("July 5th", 4, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Added {Crosshair:Mode} option to {Crosshair}");
        builder.improve("Made {HUD:Scale} options be global");
        builder.improve("Several improvements to {Inventory} module");
        versions.add(new ChangelogLink("July 6th", 5, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added expandable options for different types of velocity for {Velocity:Mode:Normal} {Velocity}");
        builder.add("Added {Long Jump:Fireball->Height} option to Hypixel Fireball {Long Jump}");
        builder.add("Added {Safe Walk:Limit speed} option to {Safe Walk}");
        builder.add("Added {Anti Fireball} module");
        builder.add("Added description to vague options");
        builder.improve("Fixed random freezes when joining servers");
        builder.improve("Made changes to {HUD:Click GUI} design");
        builder.improve("Made clientside only {Inventory Move} correct keys after exiting inventories");
        builder.improve("Added {Crosshair:Outline} option to {Crosshair}");
        builder.improve("Fixed {ESP} icon flickering");
        versions.add(new ChangelogLink("July 9th", 6, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Binding in {HUD:Click GUI}");
        builder.add("Dream Defender team detection");
        builder.add("Turned {Long Jump:Fireball->Temporary Flight} in {Long Jump} into a keybind");
        builder.improve("Turned {HUD:Scale} options into a slider");
        builder.improve("Fixed bed locations for new map rotation");
        versions.add(new ChangelogLink("July 11th", 7, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Chams:Enemy} option to {Chams}");
        builder.add("Added clicking & scrolling to changelog");
        builder.improve("Fixed {Inventory:Hotbar->Auto armor} in {Inventory}");
        builder.improve("{Inventory} module now prefers block stacks with more items");
        builder.improve("Improved {Speed:Strafe->Fall} strafe in Hypixel {Speed}");
        versions.add(new ChangelogLink("July 12th", 8, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Third Person:Distance} option to {Third Person}");
        builder.add("Added list and clear options to {*Target} and {*Whitelist} commands");
        builder.add("Added {HUD:Player List->Columns->Bounty} column to {HUD:Player List}");
        builder.add("Added {JoinClaim} module");
        builder.improve("Client performance");
        builder.improve("Fixed Cerberus crashes");
        versions.add(new ChangelogLink("July 13th", 9, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {HUD:Chat Prefixes} option for controlling prefixes of commands and IRC");
        builder.add("Added {HUD:Player List->Columns->Enchants} column to {HUD:Player List}");
        builder.add("Added {Chams:Local->Mode:Normal Mode} to {Chams}");
        builder.add("Added Download Cookie button to view account GUI");
        builder.add("Added Browser account type to Account Manager");
        builder.add("Added {GhostHand}");
        builder.improve("Fixed {KillAura:CPS (min):CPS} options being corrupted after loading different configurations");
        builder.improve("Fixed last accessed showing bogus time in account repositories");
        builder.improve("Expanded {Inventory:Automatic Close} options in {Inventory}");
        builder.improve("Fixed {FastUse:Automation->Auto tool} not switching items");
        builder.improve("Improved {Breaker} block targeting");
        versions.add(new ChangelogLink("July 14th", 10, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {HUD:Toggled->Suffix} option for {HUD:Toggled}");
        builder.improve("Fixed scaling bugs with {HUD:Toggled} and {HUD:Click GUI}");
        builder.improve("Fixed party chat not being movable");
        builder.improve("Player detection in {AntiBot}");
        builder.improve("Memory usage");
        versions.add(new ChangelogLink("July 15th", 11, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Radiance");
        builder.add("Added {Player Detect:Hypixel->Nick Detect->Tab Overlay} option in {Player Detect:Hypixel->Nick Detect}");
        builder.add("Added {HUD:Player List->Hold Bind} option in {HUD:Player List}");
        builder.add("Added {Item Animations:Sword->Enabled} option for blocking animations in {Item Animations}");
        builder.improve("Added latest.log file to allow external overlays");
        builder.improve("Better property formatting for numbers");
        versions.add(new ChangelogLink("July 17th", 12, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {AutoClicker:Disable on->Blocking sword:Disable on sword blocking} option to {AutoClicker}");
        builder.add("Added {HUD:Sense} options to {HUD}");
        builder.add("Added {AntiAim:Disable on->Rotations:Replace Rotations} and {AntiAim:Spin Speed} options to {AntiAim}");
        builder.add("Added {Scaffold:Addons->No Ascend->Bind:No Ascend Bind} Bind to {Scaffold}");
        builder.add("Added {SafeWalk:Eagle} options to {Safe Walk}");
        builder.add("Added {KillAura:Render->Range:Range render} to {KillAura}");
        builder.add("Added {Breaker:Render} options to {Breaker}");
        builder.add("Added targeted configuration resets via .config reset [module]");
        builder.improve("Fixed latest.log file not containing player names");
        builder.improve("Fixed configuration resets updating scale options");
        builder.improve("Made {Anti Fireball} disable when using {Scaffold} as a temporary fix");
        builder.improve("Fixed crashes during world join that occurred for some users");
        builder.remove("Removed {Speed:Fast fall} from {Speed}");
        versions.add(new ChangelogLink("July 19th", 13, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed {Velocity} mitigating");
        builder.improve("Fixed {Auto Clicker:Disable on->Blocking sword} option in {Auto Clicker}");
        builder.improve("Fixed sneaking with {Inventory Move} module");
        builder.improve("Fixed bug with latest.log not containing chat prefix");
        versions.add(new ChangelogLink("July 20th", 14, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Speed:Fast fall} to {Speed:Mode:Hypixel Speed} with {Disabler} module");
        builder.improve("Fixed lag spikes when switching configurations");
        builder.improve("Fixed {HUD:Party} chat typing into Minecraft textboxes");
        builder.improve("Fixed flags with {Velocity}");
        builder.improve("Launcher downloads updates automatically");
        builder.improve("Improved design in {HUD:Click GUI}");
        versions.add(new ChangelogLink("July 21st", 15, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed being unable to drag some frames in {HUD:Click GUI}");
        builder.improve("Fixed {Chat Filter} not working with {Spammer} or {Kill Event}");
        builder.improve("Improved {Scaffold:Addons->No ascend:Buffer No Ascend} buffering in {Scaffold}");
        versions.add(new ChangelogLink("July 22nd", 16, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Snapping options to {KillAura}");
        builder.add("Added {Auto Hypixel:Ban Detect} to {Auto Hypixel}");
        builder.add("Added {Fast Use:Automation->Auto Tool Bind:Hold Bind} to {Fast Use:Automation->Auto Tool}");
        builder.improve("Fixed {Inventory:Hotbar->Auto armor:Hotbar Auto armor} in {Inventory} interacting with blocks");
        builder.improve("Fixed {Scaffold} placing blocks above height limit in Hypixel Bed Wars");
        builder.improve("Second hotfix for {Kill Aura} bug with Snapping");
        versions.add(new ChangelogLink("July 23rd", 17, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed being unable to delete alt repositories");
        builder.improve("Fixed bed locations for Raze map");
        versions.add(new ChangelogLink("July 24th", 18, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Copy Credentials button to view account GUI for Microsoft accounts");
        builder.improve("Made {Speed:Fast fall} work with Jump Boost potions in {Speed}");
        builder.improve("Made parties keep messages after reconnecting");
        builder.improve("Fixed {Scaffold} not placing on some blocks like snow or tall grass");
        builder.improve("Fixed skins going black in alt manager and sometimes player capes");
        builder.improve("Fixed Browser account type not working on some systems");
        builder.improve("Fixed {NoRotate} always being activated");
        versions.add(new ChangelogLink("July 26th", 19, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Various bug fixes for ban checker in account manager");
        builder.improve("Made drag and drop for cookie accounts work in account manager GUI");
        builder.improve("Fixed {KillAura:Range->Swing:Hide Swings} in {KillAura} not working after target was lost");
        builder.improve("Fixed module incompatibilities with rejoining Mega Walls");
        versions.add(new ChangelogLink("July 28th", 20, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Player Detect:Hacker Detect->Ignore Friendly} to {Player Detect:Hacker Detect}");
        builder.improve("Improved visibility in {HUD:Click GUI}");
        builder.improve("Fixed {Auto Heal} using items in incompatible gamemodes");
        builder.improve("Fixed certain modules not working in The Pit");
        versions.add(new ChangelogLink("July 31st", 21, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed {Step:Mode:Hypixel} {Step} clipping into blocks");
        builder.improve("Attempt to fix client freezes on some systems");
        versions.add(new ChangelogLink("August 1st", 22, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {ESP:Players->Icons->Potions} equipment icons to {ESP}");
        builder.add("Added {Trails} module");
        builder.improve("Fixed cookie accounts always being invalid (bug from last update)");
        builder.improve("Fixed middle clicking configurations in {HUD:Click GUI} not opening bind menu");
        versions.add(new ChangelogLink("August 2nd", 23, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Inventory:Layout} option to {Inventory} for hotbar layout customization");
        builder.add("Added {Skeleton:Enemy} option to {Skeleton}");
        builder.add("Added {Indicators:Enemy} option to {Indicators}");
        builder.add("Added vertical animations to {HUD:Tab GUI}");
        builder.add("Added full bind listing via .bind list");
        builder.improve("Fixed {Freecam} interacting on mouse release");
        builder.improve("Renamed Validate to {FastPlace:Legit} in {FastPlace} and fixed bugs with it");
        builder.improve("Fixed {Freecam} not following player position after respawning or switching servers");
        versions.add(new ChangelogLink("August 4th", 24, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {KillAura:Switch delay} to {KillAura}");
        builder.improve("Fixed {KillAura:Mode:Single} not sticking to entities in {KillAura}");
        builder.improve("Fixed {Speed} flagging after a few hops and on certain blocks");
        builder.remove("Removed {KillAura:Mode:Tick} mode from {KillAura}");
        versions.add(new ChangelogLink("August 5th", 25, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {HUD:Sense->Enabled} option to {HUD:Sense} to hide if you're on Adjust");
        builder.add("Added {KillAura:Rotations->Look For} option to {KillAura}");
        builder.improve("Made {Streamer} replace player names in account manager and notifications");
        builder.improve("Colors of other users are now visible in {HUD:Global Chat->Mode:Global chat / IRC}");
        builder.improve("Fixed {HUD:Party} chat not scrolling to bottom after a restart");
        builder.improve("Improved rotations and switching in {KillAura}");
        builder.improve("Fixed bug with {AutoHeal} sometimes not working");
        builder.remove("Removed Snapping from {KillAura}");
        versions.add(new ChangelogLink("August 7th", 26, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {*Discord} command for linking your Adjust account with Discord");
        builder.improve("Fixed a bug with {KillAura} {KillAura:Switch Delay} option");
        versions.add(new ChangelogLink("August 8th", 27, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {InventoryMove:Mode:Hypixel} mode to {Inventory Move}");
        builder.add("Added {Blink:Mode} option and configurable {Blink:Resend->Delay Type:Delay Types} to {Blink}");
        builder.add("Added {AutoBlock:Projectiles->Fireballs}, {AutoBlock:Projectiles->Arrows} and {AutoBlock:Projectiles->Ender Pearls} target options to {AutoBlock}");
        builder.add("Added {*Nicks} command to view all nicked players in your lobby");
        builder.add("Added loadvisuals sub-command to {*Configuration} command for loading visual options (.c loadvisuals <cfg>)");
        builder.improve("Heavily improved {Step} on {Step:Mode:Hypixel} mode");
        builder.improve("Fixed {AutoPotion} throwing potions in inventory and stopping your movement");
        builder.improve("Users with expired subscriptions are now able to access single-player and game settings");
        builder.improve("Fixed {NoSlowdown} clipping into blocks on {NoSlowdown:Mode:Hypixel} mode");
        builder.improve("Fixed crashing when trying to access language GUI");
        builder.remove("Removed Mouse from {InventoryMove}");
        versions.add(new ChangelogLink("August 11th", 28, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed bugs with {Step} on {Step:Mode:Hypixel} mode");
        builder.improve("Fixed being unable to move screen objects on some GUI scales");
        versions.add(new ChangelogLink("August 12th", 29, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed {NoSlowdown} unblocking on {NoSlowdown:Mode:Hypixel} mode in SkyWars");
        builder.improve("Fixed bans with {NoFall} and {Step} on {NoFall:Mode:Hypixel} mode");
        builder.improve("Fixed {KillAura:Rotations->Look For:Zero Ticks} being a tick late in {KillAura:Rotations->Look For:Look For} in {KillAura}");
        builder.improve("Fixed being unable to use your inventory if you didn't have {InventoryMove:Mode:Hypixel} {InventoryMove} enabled");
        versions.add(new ChangelogLink("August 14th", 30, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed random keys being unpressed and sneaking with {InventoryMove:Mode:Hypixel} {InventoryMove}");
        builder.improve("Fixed {Third Person:Free Look} interacting with {InventoryMove:Mode:Hypixel} {InventoryMove}");
        builder.improve("Added account type text below player head in account manager");
        builder.remove("Temporarily removed {InventoryMove:Mode:Hypixel} {InventoryMove}");
        builder.remove("Temporarily removed consumables from {NoSlowdown} {NoSlowdown:Mode:Hypixel} mode");
        versions.add(new ChangelogLink("August 15th", 31, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {MoreKnockback} module");
        builder.add("Added {Velocity:Allow Self Fireballs} to {Velocity} module");
        builder.improve("Made rotations set back faster when breaking or placing blocks");
        builder.improve("Added party message for when invites expire or get declined");
        versions.add(new ChangelogLink("August 17th", 32, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Breaker:Render:HUD} option to {Breaker}");
        builder.improve("Fixed {LagBreak} constantly jumping when enabled");
        builder.improve("Fixed bug with {NoFall}");
        versions.add(new ChangelogLink("August 20th", 33, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed {ESP:Item Pool} option in {ESP} being choppy with moving items");
        builder.improve("Improved {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode");
        builder.improve("Unpatched {NoFall} on {NoFall:Mode:Hypixel} mode");
        builder.improve("Fixed issues with HUD scale option");
        versions.add(new ChangelogLink("August 24th", 34, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {NoSlowdown:Consumables:Auto jump} and {NoSlowdown:Consumables:Prevent stepping} options to {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown}");
        builder.add("Added {Sprint:Keep sprint->Slowdown} option to {Sprint:Keep sprint} in {Sprint}");
        builder.add("Added {KillAura:Rotations->Enabled:Rotations Enabled} option to {KillAura}");
        builder.add("Added {Velocity:Addons} to {Velocity:Mode:Normal} mode in {Velocity}");
        builder.add("Added {Scaffold:Rotations->Snap} rotation option to {Scaffold}");
        builder.add("Added {Flight:Mode:Bedwars Practice} mode to {Flight}");
        builder.add("Added {NoSlowdown:Mode:Swap} mode to {NoSlowdown}");
        builder.add("Added {Tweaks:Minimal bobbing:Minimal Bobbing} module");
        builder.add("Added {MovementFix} module");
        builder.improve("Fixed {FastUse:Automation->Auto tool} taking priority over item swapping");
        builder.improve("Merged {Flight:Mode:Air Jump} mode in {Flight} with {Flight:Mode:Air Walk}");
        builder.improve("Unpatched {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown} with consumables");
        builder.remove("Removed {NoFall:Mode:Hypixel} mode in {NoFall} due to patch (temporarily)");
        versions.add(new ChangelogLink("August 27th", 35, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {NoFall:Mode:Hypixel} mode to {NoFall} (unpatched)");
        builder.add("Added {AimAssist} module");
        builder.improve("Fixed {Speed:Fast fall} in {Speed} not working when using {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown} with consumables");
        builder.improve("Improved {Speed:Fast fall} in {Speed} when stepping up blocks");
        builder.improve("Added mouse bind functionality to container GUIs");
        versions.add(new ChangelogLink("August 28th", 36, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Disabler:HUD} option to {Disabler}");
        builder.improve("Updated {PlayerDetect:Hypixel->Name Reveal} in {PlayerDetect} for recent update (reveals nicked players in Bed Wars queues)");
        builder.improve("Fixed {KillAura:Range->Field of view} in {KillAura} being broken");
        builder.improve("Fixed {Velocity:Addons->Delay} in {Velocity} always being enabled");
        builder.improve("Massively improved speed of {Disabler}");
        builder.improve("Fixed Custom Items in OptiFine");
        builder.improve("Potentially fixed bugs with {HUD:Sense} (hotfix)");
        versions.add(new ChangelogLink("August 29th", 37, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed {HUD:Player List->Hypixel API} in {HUD:Player List} breaking after recent updates");
        builder.improve("Fixed setting random skin in the profile editing menu");
        builder.improve("Updated {PlayerDetect:Hypixel->Name Reveal} in {PlayerDetect} for Duels");
        builder.improve("Fixed {Breaker} resetting block hit delay");
        versions.add(new ChangelogLink("August 30th", 38, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Speed:Damage->Boost:Damage Boost} mode to {Speed:Mode:Hypixel} mode in {Speed}");
        builder.add("Added Bow mode to {LongJump:Mode:Hypixel} {LongJump}");
        builder.improve("Fixed {Breaker:Render->Outline} render in {Breaker} not working with {Breaker:Render->Full} disabled");
        builder.improve("Fixed {Breaker:Render->HUD} in {Breaker} not using the spoofed item for percentage");
        builder.improve("Fixed bed locations for new map rotation");
        builder.improve("Fixed {LongJump} issues with {Sprint:Strafe}");
        builder.improve("Fixed issues with {Breaker} rotations");
        versions.add(new ChangelogLink("August 31st", 39, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {BlockOverlay:Chest->Hypixel:Hide opened} option for {BlockOverlay:Chest:Chests} in {BlockOverlay}");
        builder.add("Added Type option to certain color options");
        builder.add("Added {Scaffold:Addons->Take velocity} option to {Scaffold}");
        builder.improve("Fixed {AutoBlock} module right clicking while looking at interactable blocks");
        builder.improve("Made {HUD:Toggled} in {HUD} cover full color range with radiance");
        versions.add(new ChangelogLink("September 3rd", 40, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {*Friends} command for managing your Adjust friends and {HUD:Friend Notifications} options in {HUD} module");
        builder.add("Added {*Block} command for blocking Adjust users from interacting with you");
        builder.add("Added {ItemAnimations:Sword:Accurate} option to {ItemAnimations} for {ItemAnimations:Sword->Animation:Vanilla} animation");
        builder.improve("Merged party invite notifications with other social notifications");
        builder.improve("Fixed {HUD:Sense} sometimes not showing other Adjust users");
        builder.improve("Fixed various issues with rainbow color type");
        builder.remove("Temporarily removed {Scaffold:Addons->No ascend} mode from {Scaffold} due to patches");
        versions.add(new ChangelogLink("September 11th", 41, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Consumables enabled option to {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown}");
        builder.improve("Fixed {NoSlowdown} with {NoSlowdown:Consumables} on {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown}");
        builder.improve("Unpatched jumping with {Scaffold} on {Scaffold:Mode:Hypixel} mode");
        builder.improve("Fixed inexplicable flags with {Scaffold} after towering");
        builder.improve("Fixed social invites having inaccurate headings");
        builder.improve("Fixed bans when jumping with {Scaffold} (hotfix)");
        versions.add(new ChangelogLink("September 12th", 42, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Updated {InventoryMove:Mode:Hypixel} mode in {InventoryMove}");
        versions.add(new ChangelogLink("September 14th", 43, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Boost speed and {Scaffold:Addons->Allow sprint->Strafe} options to {Scaffold:Addons->Allow sprint} on {Scaffold:Mode:Hypixel} mode in {Scaffold}");
        builder.add("Added {InventoryMove:Full speed} option to {InventoryMove:Mode:Hypixel} mode in {InventoryMove}");
        builder.improve("Made {*Whitelist} and {*Target} list save");
        builder.improve("Massively improved {Scaffold:Addons->Allow sprint} on {Scaffold:Mode:Hypixel} mode in {Scaffold}");
        builder.improve("Fixed account manager login times being off by 40 seconds");
        versions.add(new ChangelogLink("September 16th", 44, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {KillAura:Priority} options to {KillAura}");
        builder.improve("Fixed flags with {Scaffold:Addons->Tower} and jumping while using {Scaffold:Addons->Allow sprint} on {Scaffold:Mode:Hypixel} mode in {Scaffold}");
        builder.improve("Fixed {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode with {NoSlowdown:Consumables} not being full while landing on a block");
        builder.improve("Improved {Scaffold:Addons->Tower} in {Scaffold} while standing next to blocks");
        builder.improve("Fixed stuttering with Dynamic FOV while using {Scaffold:Addons->Tower} in {Scaffold}");
        builder.improve("Made {KillAura} prioritize targeted players and players over mobs");
        builder.remove("Removed {InventoryMove:Full speed} option from {InventoryMove:Mode:Hypixel} mode in {InventoryMove}");
        versions.add(new ChangelogLink("September 17th", 45, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {AutoHypixel:Auto Play->Play on death} option to {AutoHypixel:Auto Play} in {AutoHypixel}");
        builder.add("Added {BedPlates} module");
        builder.improve("Fixed rare unblocks while attacking with {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode in SkyWars");
        builder.improve("Fixed {Step} not working after using {Scaffold} on {Scaffold:Mode:Hypixel} mode and staying on ground");
        builder.improve("Fixed {Velocity} pushing you off blocks while standing still on {Velocity:Mode:Hypixel} mode");
        versions.add(new ChangelogLink("September 18th", 46, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Merged Better Scoreboard and Minimal Bobbing into {Tweaks} module");
        builder.add("Added {Tweaks:Smooth rotation yaw} to {Tweaks}");
        builder.add("Added {BlockOverlay:Obsidian} to {BlockOverlay}");
        builder.improve("Fixed {BedPlates} sometimes crashing the game");
        versions.add(new ChangelogLink("September 20th", 47, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {*Nickname} command for changing your visual in-game username");
        builder.add("Added {Scaffold:Addons->No ascend->Speed only} option to {Scaffold:Addons->No ascend} in {Scaffold}");
        builder.add("Added {Scaffold:Addons->No ascend} on {Scaffold:Mode:Hypixel} mode in {Scaffold}");
        builder.add("Added {Streamer:Name Type} option to {Streamer} module");
        builder.add("Added {Breaker:Hide swings} option to {Breaker}");
        builder.improve("Fixed {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode with {NoSlowdown:Consumables} when consuming multiple items in a row");
        builder.improve("Unpatched jumping and using {Speed} module while using {Scaffold} on {Scaffold:Mode:Hypixel} mode");
        builder.improve("Unpatched {Scaffold:Addons->Tower} on {Scaffold:Mode:Hypixel} mode in {Scaffold}");
        builder.remove("Removed Boost speed option in {Scaffold:Addons->Allow sprint} on {Scaffold:Mode:Hypixel} mode in {Scaffold}");
        versions.add(new ChangelogLink("September 27th", 48, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {ESP:Players->Icons->Hacking} option to {ESP:Players->Icons} for player targets in {ESP}");
        builder.add("Added {Player Detect:Hacker Detect->Tab Overlay} option in {Player Detect:Hacker Detect}");
        builder.add("Added {ItemPhysics} module");
        builder.improve("Merged both modes of {LongJump:Mode:Hypixel} mode in {LongJump} with automatic decision");
        builder.improve("Made {*Nickname} command have more freedom in allowed nicknames");
        builder.improve("Fixed bed locations for Aquarium map");
        versions.add(new ChangelogLink("September 29th", 49, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {ESP:Players->Icons->Hacking->Location} option to {ESP:Players->Icons->Hacking} in {ESP:Players->Icons} for player targets in {ESP}");
        builder.improve("Fixed {Player Detect:Hacker Detect->Tab Overlay} option in {Player Detect:Hacker Detect} being misplaced with scores displayed");
        builder.improve("Fixed warning icon missing textures");
        versions.add(new ChangelogLink("September 30th", 50, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Unpatched {Scaffold:Addons->Tower} on {Scaffold:Mode:Hypixel} mode in {Scaffold}");
        builder.improve("Fixed {ESP:Players->Icons->Hacking} in {ESP:Players->Icons} being misplaced on {ESP:Players->Icons->Hacking->Location:Nametag} location");
        builder.improve("Fixed flagging with {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode during heavy network lag");
        builder.improve("Fixed {Disabler} taking longer than usual");
        versions.add(new ChangelogLink("October 3rd", 51, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {InventoryMove:Full speed} option to {InventoryMove:Mode:Hypixel} mode in {InventoryMove}");
        builder.add("Added strafe to {LongJump} on {LongJump:Mode:Hypixel} mode with Fireballs");
        builder.add("Added spooky decorations to main menu for upcoming Halloween");
        builder.improve("Fixed {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode with {NoSlowdown:Consumables} preventing block interactions");
        builder.improve("Fixed {Scaffold:Addons->Block count HUD} not displaying in third person view");
        builder.improve("Fixed ghosting issues with {KillAura} originating in previous update");
        builder.improve("Fixed issues with {AntiBot} detecting players as bots");
        versions.add(new ChangelogLink("October 4th", 52, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added weekly polls in the main menu");
        builder.add("Added Halloween Simulator module for the seasonal mini-game on Hypixel");
        builder.add("Added {LongJump:Fireball->Regain buffer} option to {LongJump:Mode:Hypixel} mode in {LongJump}");
        builder.add("Added {ItemAnimations:Sword->Animation:None} mode to {ItemAnimations:Sword->Animation:Sword Animation} in {ItemAnimations}");
        builder.add("Added {BlockPlaces:Color} option to {BlockPlaces}");
        builder.improve("Made {LongJump:Mode:Hypixel} mode with Fireballs in {LongJump} allow activating {LongJump:Fireball->Temporary Flight} midair");
        builder.improve("Clicks are now properly delayed by {InventoryMove} on {InventoryMove:Mode:Hypixel} mode");
        builder.improve("Improved consistency of {LongJump:Mode:Hypixel} mode with Fireballs in {LongJump}");
        builder.improve("Fixed a bug with {HUD:Sense} requiring it to be re-enabled");
        builder.improve("Added limit to {Step} if it were to interfere with {Breaker}");
        builder.improve("Fixed parties not updating members after leaving");
        builder.improve("Fixed {Disabler} rarely failing without recovery");
        versions.add(new ChangelogLink("October 5th", 53, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {ItemAnimations:Sword->Animation:Slide} and {ItemAnimations:Sword->Animation:Attract} modes to {ItemAnimations:Sword->Animation} in {ItemAnimations}");
        builder.add("Added {ESP:Players->Information->Background} option to {ESP:Players->Information} in {ESP}");
        builder.improve("Fixed {Scaffold} snapping rotations with {Scaffold:Addons->Allow sprint} on {Scaffold:Mode:Hypixel} mode");
        builder.improve("Improved vertical control for {LongJump:Mode:Hypixel} mode with Fireballs in {LongJump} (downwards)");
        builder.improve("Changed {ItemAnimations:Sword->Equip animation} into a slider for intensity in {ItemAnimations}");
        builder.improve("Improved compatibility between {ESP:Theme:Themes} in {ESP}");
        versions.add(new ChangelogLink("October 6th", 54, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Speed:Fast fall->Ticks} and {Speed:Fast fall->Scaffold} options to {Speed:Fast fall} in {Speed:Mode:Hypixel} {Speed}");
        builder.improve("Improved {Speed:Fast fall} in {Speed} on {Speed:Mode:Hypixel} mode (7 ticks)");
        builder.improve("Fixed legit rotations snapping back instantly with {Scaffold}");
        builder.improve("Made {Streamer} replace player names in scoreboard");
        versions.add(new ChangelogLink("October 8th", 55, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {ESP:Players->Information->Nametags->Scale} option to {ESP:Players->Information->Nametags} and {ESP:Players->Information->Item name} in {ESP}");
        builder.add("Added {Blink:Mode:Both} mode to {Blink}");
        builder.improve("Fixed multiple issues with {Scaffold:Addons->Tower} in {Scaffold} on {Scaffold:Mode:Hypixel} mode");
        builder.improve("Fixed random {Scaffold:Addons->Allow sprint} flags in {Scaffold} on {Scaffold:Mode:Hypixel} mode");
        builder.improve("Fixed {Breaker} repetitively failing to break the same bed");
        builder.improve("Fixed {AntiBot} not detecting some shopkeeper NPCs in Hypixel Bed Wars");
        versions.add(new ChangelogLink("October 10th", 56, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Breaker:Block:Dragon Egg} block type to {Breaker}");
        builder.improve("Fixed flags with {NoSlowdown} and {Speed}");
        builder.improve("Fixed some issues with game detection");
        versions.add(new ChangelogLink("October 12th", 57, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Speed:Mode:Miniblox} mode to {Speed}");
        builder.add("Added {Flight:Mode:Miniblox} mode to {Flight}");
        builder.add("Added {AntiVoid:Disable Bind} to {AntiVoid}");
        builder.improve("Various bug fixes and improvements");
        versions.add(new ChangelogLink("October 15th", 58, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {KillEvent:Send message->Only on hackers} option to {KillEvent:Send message} in {KillEvent}");
        builder.add("Added {Flight:Vertical Speed} option to {Flight} on {Flight:Mode:Miniblox} mode");
        builder.add("Added {Disabler:Mode:Miniblox} mode to {Disabler}");
        builder.improve("Fixed lag backs with {Speed} on {Speed:Mode:Hypixel} mode with low jump ticks");
        versions.add(new ChangelogLink("October 18th", 59, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Velocity:Addons->Sprint reset} to {Velocity:Addons} in {Velocity} on {Velocity:Mode:Normal} mode");
        builder.add("Added {Tweaks:Scoreboard->Transparent} option to {Tweaks:Scoreboard} in {Tweaks}");
        builder.add("Added {LagBreak:Break in range} option to {LagBreak}");
        builder.add("Added proper settings to {LagBreak}");
        builder.add("Added {BackTrack} module");
        builder.improve("Added support for {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode with the predictive anti-cheat");
        builder.improve("Moved Transparent in Chat Options to {Tweaks:Chat->Transparent} in {Tweaks}");
        builder.improve("Moved Chat Stacker module to {Tweaks:Chat->Message stacking} in {Tweaks}");
        builder.improve("Fixed {Disabler} not activating configuration save timer");
        builder.improve("Improved prediction and timing in {LagBreak}");
        versions.add(new ChangelogLink("October 22nd", 60, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Speed:Strafe->Range} option to {Speed:Strafe} in {Speed} on {Speed:Mode:Hypixel} mode (new strafe method!)");
        builder.improve("Fixed target settings not affecting {BackTrack}");
        versions.add(new ChangelogLink("October 26th", 61, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added back Boost speed and Auto jump to {Scaffold:Addons->Allow sprint} in {Scaffold:Mode:Hypixel} {Scaffold}");
        builder.add("Added {LagBreak:HUD} option to {LagBreak} and {BackTrack}");
        builder.improve("Made {Scaffold:Addons->No ascend} in {Scaffold:Mode:Hypixel} {Scaffold} mode not use extra blocks");
        builder.improve("Added multiple account selection to account manager (hold Ctrl)");
        builder.improve("Added move functionality for accounts between repositories");
        builder.improve("Unpatched {Freecam} housing nuker");
        builder.improve("Added random tips to main menu");
        builder.improve("Fixed {TargetStrafe} getting stuck on walls");
        builder.improve("Fixed {BackTrack} attacking dead entities");
        versions.add(new ChangelogLink("October 30th", 62, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Speed:Damage->Low hop} option to {Speed:Damage} in {Speed}");
        builder.improve("Unpatched {Speed:Fast fall} in {Speed} on seven ticks");
        builder.improve("Made {Breaker} break beds regardless of perceived color being local");
        versions.add(new ChangelogLink("November 2nd", 63, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {AutoHypixel:Auto Play->Map dodging} option to {AutoHypixel:Auto play} in {AutoHypixel}");
        builder.add("Added {GhostHand:Blocks} option to {GhostHand}");
        builder.improve("Fixed Boost speed in {Scaffold:Addons->Allow sprint} in {Scaffold:Mode:Hypixel} {Scaffold} flagging when disabling falling off blocks");
        builder.improve("Fixed {Scaffold} sometimes being unable to place when jumping below blocks on {Scaffold:Mode:Hypixel} mode");
        versions.add(new ChangelogLink("November 3rd", 64, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Strafe check to {Player Detect:Hacker Detect}");
        builder.add("Added {HUD:Toggled:Enabled} option to {HUD:Toggled} {HUD}");
        builder.improve("Renamed Radiance to {HUD:Enable Secondary:Secondary} and made it a color picker");
        builder.improve("Unpatched {Scaffold:Addons->Allow sprint} in {Scaffold} on {Scaffold:Mode:Hypixel} mode");
        versions.add(new ChangelogLink("November 6th", 65, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {*Spectate} command");
        builder.add("Added {ItemAnimations:Sword->Animation:Detract} to {ItemAnimations:Sword->Animation:Sword Animations}");
        builder.add("Added {Scaffold:Addons->Clientside->Swap:Clientside Swap} option to {Scaffold}");
        builder.improve("Fixed {Breaker} failing to break blocks on Bed Wars (possibly)");
        versions.add(new ChangelogLink("November 8th", 66, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {*APIKey} command");
        builder.add("Added {Step:Disable Bind} to {Step}");
        builder.add("Added {Item Animations:Position->Sword only} to {ItemAnimations:Position} in {Item Animations}");
        builder.improve("Made all reconnect notifications stack");
        builder.improve("Fixed {Scaffold} flagging when enabling it on the edge");
        versions.add(new ChangelogLink("November 10th", 67, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {NoFall:Mode:Water} mode to {NoFall}");
        builder.add("Added {ItemAnimations:Sword->Animation:Swank} to {ItemAnimations:Sword->Animation:Sword Animations}");
        builder.improve("Added hotkey for switching tabs in chat (Ctrl + Tab)");
        versions.add(new ChangelogLink("November 12th", 68, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {ItemAnimations:Sword->Animation:Punch} to {ItemAnimations:Sword->Animation:Sword Animations}");
        builder.improve("Fixed {NoSlowdown} flagging with {Sprint} on predictive anti-cheat");
        builder.improve("Made {Sprint:Omnidirectional} {Sprint} bypass predictive anti-cheat");
        versions.add(new ChangelogLink("November 13th", 69, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {HUD:Player List:Auto-Target} to {HUD:Player List}");
        builder.add("Added {LagBreak:Maximum targets} setting to {LagBreak}");
        builder.improve("Fixed bans with {MovementFix} on normal anti-cheat by disabling certain movement modules");
        versions.add(new ChangelogLink("November 16th", 70, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Inventory:Addons->Bed Wars Auto Chest} to {Inventory} (via scrolling up/down)");
        builder.add("Added {HUD:Player List->Other->Level Color Highlight} to {HUD:Player List} Hypixel game levels");
        builder.improve("Made {AutoHypixel:Bed Wars->Bed Wars Tracker} notifications stack");
        builder.improve("Made {Scaffold:Addons->Tower} compatible with {Speed} and {Scaffold:Mode:Hypixel} {Scaffold}");
        builder.remove("Temporarily removed {Scaffold:Addons->Allow Sprint} from {Scaffold:Mode:Hypixel} {Scaffold} (use {Scaffold:Addons->No ascend} instead)");
        versions.add(new ChangelogLink("November 20th", 71, builder));
        versions.add(new ChangelogLink("Happy New Year " + ChatColor.BOLD + "2025" + ChatColor.RESET + "!",
                "Happy New Year from the Adjust team!",
                "Sorry for the lack of updates lately. There was a lot of internal drama which completely demotivated me (final) and I required a break.",
                "Have a great 2025 :)"));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {SafeWalk:Eagle->Mode} to {SafeWalk:Eagle} for automatically handling sneak delay");
        builder.add("Added {HUD:Widgets->Session Information}, {HUD:Widgets->Click Speed} and {HUD:Widgets->Memory Usage} widgets to {HUD}");
        builder.add("Added list of all previously used accounts (accessible via top-left button in account manager)");
        builder.add("Added anti-cheat detection which automatically disables banning features");
        builder.add("Added bulk importing to account manager for credentials & cookie accounts");
        builder.add("Added multiple {Speed:Mode:NCP} modes to {Speed} and {Step}");
        builder.add("Added blink-less {Phase:Mode:Hypixel} mode to {Phase}");
        builder.add("Added {NoSlowdown:Mode:Custom} mode to {NoSlowdown}");
        builder.add("Added selectable {AutoPotion:Throw Types} to {Auto Potion}");
        builder.add("Added {Tweaks:Disconnect confirm} to {Tweaks}");
        builder.add("Added {Velocity:Mode:Zero} mode to {Velocity}");
        builder.improve("Made {Movement Fix} no longer be required on games with predictive anti-cheat");
        builder.improve("Fixed {AutoPotion} requiring movement in order to function");
        builder.improve("Fixed {NoSlowdown:Mode:Hypixel} {NoSlowdown} mitigating with swords");
        builder.improve("Improved speed with {Step:Mode:Hypixel} {Step} when stepping up large towers");
        builder.improve("Fixed {Inventory} and {Breaker} not accounting for tool enchants");
        builder.improve("Added functionality for drag and dropping ZIP files in account manager");
        builder.improve("Added spectator highlight and out-of-range alpha in {HUD:Player List}");
        builder.improve("Fixed {AutoClicker} and {FastUse} breaking blocks after clicking done buttons");
        builder.improve("Fixed {Bed Plates} showing a bunch of random blocks for uncovered beds");
        builder.improve("Added option for creating empty configurations");
        builder.remove("Removed Movement Recorder module");
        builder.remove("Removed Halloween Simulator module");
        versions.add(new ChangelogLink("January 31st", 72, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Potion mode to {AutoHeal}");
        builder.improve("Fixed {Scaffold:Mode:Hypixel} {Scaffold} bans and flagging when swapping stacks");
        builder.improve("Fixed {NoSlowdown:Consumables:Auto jump} in {NoSlowdown:Mode:Hypixel} {NoSlowdown} not consuming item");
        builder.improve("Fixed {NoSlowdown:Mode:Hypixel} {NoSlowdown} mitigating for some users when autoblocking (again)");
        builder.improve("Fixed {Scaffold:Addons->Clientside->Swap:Clientside Swing} option in {Scaffold} not working");
        builder.improve("Fixed flags with {Step} and {Speed} on {Speed:Mode:Hypixel} modes");
        builder.improve("Fixed block placing when holding down right click");
        builder.improve("Fixed {AutoHeal} not working in The Pit");
        builder.remove("Removed {Player Detect:Hacker Detect:Auto Report} from {Player Detect:Hacker Detect}");
        versions.add(new ChangelogLink("February 4th", 73, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Velocity:Addons->Jump reset->Rotate} option to {Velocity:Jump reset} in {Velocity} on {Velocity:Mode:Normal Mode}");
        builder.add("Added {Sprint:Omnidirectional->Mode} option to {Sprint:Omnidirectional} in {Sprint}");
        builder.add("Added {Sprint:Keep sprint->Mode} option to {Sprint:Keep sprint} in {Sprint}");
        builder.add("Added Adjust-wide {!Global Configurations} to {HUD:Click GUI}");
        builder.add("Added multiple {Breaker:Hypixel->Priority} options to {Breaker}");
        builder.add("Added Main Color mode to all color settings");
        builder.add("Added {Auto Bed Defense} module");
        builder.improve("Made shift clicking the configuration delete button in {HUD:Click GUI} bypass confirmation screen");
        builder.improve("Allowed more special characters (_-=+,./\"';:!@#$%^&*~) in configuration names");
        builder.improve("Fixed {Pearl Assist} attempting to throw without having an ender pearl");
        builder.improve("Made dialog boxes automatically select the first text input box");
        builder.improve("Fixed {Indicators} not updating with {Third Person:Free Look} and {Freecam}");
        builder.improve("Improved {Scaffold} and {KillAura}'s compatibility with {MovementFix}");
        builder.improve("Fixed Hypixel blocking Adjust protocol version");
        builder.improve("Fixed {Breaker} getting stuck in clientside GUIs");
        builder.improve("Added leave message to Adjust parties");
        builder.improve("Unpatched Hypixel bypasses");
        builder.remove("Removed Prevent sprinting while using from {MovementFix}");
        builder.remove("Reverted {AutoHeal} to old version");
        builder.remove("Removed patched Hypixel bypasses");
        versions.add(new ChangelogLink("August 7th", 74, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {WallClimb} module for Hypixel");
        builder.improve("Fixed {NoSlowdown} with {NoSlowdown:Consumables} on {NoSlowdown:Mode:Hypixel} mode start consuming the same item");
        builder.improve("Fixed {Scaffold} occasionally failing with {Scaffold:Addons->No ascend} on {Scaffold:Mode:Hypixel} mode");
        builder.improve("Fixed {HUD:Widgets->Session Information} ignoring {Streamer} module");
        builder.improve("Greatly improved {Velocity:Mode:Hypixel} mode in {Velocity} when in air");
        builder.improve("Fixed {AntiAim} not being allowed on Hypixel");
        builder.improve("Fixed drag issues with {!Global Configurations} screen");
        builder.improve("Improved lag issues with {Auto Bed Defense}");
        versions.add(new ChangelogLink("August 8th", 75, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {NoSlowdown:^hypixelConsumables->HUD} option to {NoSlowdown:^hypixelConsumables} in {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode");
        builder.add("Added {LongJump} module back with a {LongJump:Mode:Hypixel} mode for Fireballs");
        builder.add("Added description editing to {!Global Configurations} screen");
        builder.add("Added {MurderMystery} module");
        builder.improve("Fixed {Sprint:Omnidirectional} {Sprint} blinking forever when disabling the setting");
        builder.improve("Fixed {Sprint:Omnidirectional->Mode:Rotate} mode in {Sprint:Omnidirectional} {Sprint} interfering with Fireball throws");
        builder.improve("Fixed loadvisuals subcommand in {*Configuration} command clearing current binds");
        builder.improve("Improved {InventoryMove} on {InventoryMove:Mode:Hypixel} mode in containers");
        builder.improve("Fixed {AutoHypixel:Auto Play} in {AutoHypixel} not working on Arcade games");
        builder.improve("Fixed bed locations for new map rotation");
        builder.improve("Fixed {AutoBlock} interacting with blocks");
        versions.add(new ChangelogLink("August 9th", 76, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added Detective to {Murder Mystery} roles");
        builder.add("Added {ItemAnimations:Swing Speed->Ignore effects} to {ItemAnimations:Swing Speed} in {ItemAnimations}");
        builder.add("Made Account Manager accessible from the in-game pause menu");
        builder.add("Added position chat format in parties (use via \\{pos} in party chat)");
        builder.add("Added {Tweaks:Tab Overlay->Remove NPCs} option to {Tweaks:Tab Overlay} in {Tweaks}");
        builder.add("Added refresh button to {!Global Configurations} screen");
        builder.add("Added tab complete in party chat");
        builder.improve("Fixed {ItemAnimations:Swing Speed} in {ItemAnimations} not working with effects");
        builder.improve("Made {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown} full with swords");
        builder.improve("Fixed spectator detect for other players on Murder Mystery gamemode");
        builder.improve("Made {Murder Mystery} roles show on player list and indicators");
        builder.improve("Fixed {ESP:Items} in {ESP} not identifying items in Murder Mystery");
        builder.improve("Disabled Watchdog anti-cheat model on Housing");
        builder.improve("Unpatched {Auto Hypixel:Auto Report Accept} in {Auto Hypixel}");
        builder.improve("Improved {Scaffold} rotations with {MovementFix}");
        builder.improve("Fixed {AutoPotion} not working with healing potions");
        builder.improve("Fixed lightning bolts not being rendered");
        versions.add(new ChangelogLink("August 12th", 77, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Tweaks:Chat->No lobby messages} to {Tweaks:Chat} in {Tweaks}");
        builder.add("Added {Murder Mystery:Auto Strike} to {Murder Mystery} as a replacement for {Kill Aura}");
        builder.improve("Fixed lag with {Scaffold} and {Auto Bed Defense}");
        builder.improve("Reworked {Anti AFK} module and heavily improved it");
        builder.improve("Upped the message limit on history of {HUD:Global Chat->Mode:Global chat / IRC}");
        builder.improve("Fixed a major issue causing consistent flags on predictive anti-cheats");
        builder.improve("Fixed {Inventory} module running in games it shouldn't (such as Murder Mystery)");
        builder.improve("Fixed bugs with {*Bind} command and improved the command usage overall");
        builder.improve("Fixed fade-in animations in {HUD:Click GUI} affecting the entire screen");
        builder.improve("Fixed {Phase:Mode:Blink} mode in {Phase} blinking for excess time");
        builder.improve("Fixed {Breaker} not functioning correctly in Bed Wars duels");
        builder.improve("Fixed {BackTrack} not working and improved timing");
        builder.improve("Added time warning to {Join Claim}");
        builder.improve("Unpatched Hypixel bypasses");
        versions.add(new ChangelogLink("August 14th", 78, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {KillAura:Render->Target->Lag only} option to {KillAura:Render->Target:Target Render} in {KillAura}");
        builder.add("Added {LongJump:Fireball->More Fireballs} bind to {LongJump} on {LongJump:Mode:Hypixel} mode");
        builder.add("Added {BackTrack:Range->Timeout:Timeout range} option to {BackTrack}");
        builder.add("Added {HUD:Tab GUI->Gradient} option to {HUD:Tab GUI} in {HUD}");
        builder.add("Added {TeamsDetection} module");
        builder.improve("Fixed loadvisuals subcommand in {*Configuration} command not loading color options");
        builder.improve("Prioritized swapping blocks over throwing items in {Inventory} module");
        builder.improve("Made {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown} full again with swords");
        builder.improve("Fixed {BackTrack} activating with multiple players in range");
        builder.improve("Fixed {Freecam} moving faster diagonally with {Freecam:No Clip}");
        builder.improve("Improved {Scaffold} on {Scaffold:Mode:Hypixel} mode");
        builder.remove("Removed blink in {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode with swords");
        versions.add(new ChangelogLink("August 15th", 79, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added height limit detection to {Scaffold} for Bed Rush duels");
        builder.add("Added {LongJump:Fireball->HUD} option to {LongJump} on {LongJump:Mode:Hypixel} mode");
        builder.add("Added {AntiSpleef} module for Spleef games");
        builder.improve("Fixed {KillAura} attacking less with {Sprint:Keep sprint} on {Sprint:Keep sprint->Mode:Hypixel} mode");
        builder.improve("Fixed {NoSlowdown} on {NoSlowdown:Mode:Hypixel} mode with swords flagging on velocity");
        builder.improve("Unpatched {LongJump} with Fireballs on {LongJump:Mode:Hypixel} mode");
        builder.improve("Added support for going downwards with {WallClimb}");
        builder.improve("Fixed {Breaker} breaking while {Scaffold} is in use");
        versions.add(new ChangelogLink("August 16th", 80, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added support for logging in with just session token and importing from clipboard using Session login");
        builder.add("Added height limit detection to {Scaffold} for Bridge duels, SkyWars, and Bed Wars");
        builder.add("Added {FastUse:Block breaking->Added ticks} option to {FastUse:Block breaking} in {FastUse} with {FastUse:Block breaking->Override damage}");
        builder.add("Added Water Bucket option to {Inventory:Layout} in {Inventory} module");
        builder.add("Added loadbinds subcommand to {*Configuration} command");
        builder.add("Added {BlockOverlay:Chest->Max. range} settings to blocks in {BlockOverlay}");
        builder.add("Added {HUD:Toggled->Padding} option to {HUD:Toggled} in {HUD}");
        builder.improve("Fixed {*Configuration} subcommands not marking your configuration as modified");
        builder.improve("Fixed {Tweaks:Chat->Message stacking} in {Tweaks} ignoring chat tabs");
        builder.improve("Made messages from {*Message} show up on Adjust chat tab");
        builder.improve("Made Ctrl+V create multiple lines at once in string properties in Click GUI");
        builder.improve("Fixed {Breaker} rotating for excess time with block hit delay");
        builder.improve("Improved sprint resetting with {Velocity} module");
        builder.improve("Fixed Microsoft login failing on some accounts");
        versions.add(new ChangelogLink("August 17th", 81, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added MacOS support");
        builder.add("Added {!Detection Status} to main menu");
        builder.add("Added LiquidProxy support (currently only works with Hypixel)");
        builder.improve("Fixed {Streamer} name replacement with holographic text (armor stands)");
        builder.improve("Fixed {Skeleton} module being off when players are moving");
        builder.improve("Fixed dialog boxes text padding with higher scales");
        versions.add(new ChangelogLink("August 18th", 82, builder));

        builder = new ChangelogVersionBuilder();
        builder.improve("Fixed scrolling to swap item not working with {InventoryMove} on {InventoryMove:Mode:Hypixel} mode");
        builder.improve("Fixed {InventoryMove} on {InventoryMove:Mode:Hypixel} mode causing {Inventory} module to increase delay");
        versions.add(new ChangelogLink("August 19th", 83, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Blink:Auto-Disable} option to {Blink}");
        builder.improve("Improved {Scaffold} on {Scaffold:Mode:Hypixel} mode");
        builder.improve("Fixed {HUD:Event Timers} being draggable when not showing");
        builder.improve("Added extra controls when holding down Ctrl for your own configurations in {!Global Configurations}");
        builder.improve("Fixed configurations uploaded by developers ignoring sorting in {!Global Configurations}");
        builder.improve("Fixed fail reasons in notifications when inviting somebody to a repository being inaccurate");
        builder.improve("Fixed color property types not always showing");
        builder.improve("Upped limit of {ESP} nametag scale settings");
        versions.add(new ChangelogLink("August 21st", 84, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added update notice inside of the client");
        builder.add("Added {AntiVoid:Mode:Legit} mode to {AntiVoid} (beta test and requires {AntiVoid:Disable Bind}, {/b/will change})");
        builder.improve("Fixed flags with {Scaffold} on {Scaffold:Mode:Hypixel} mode and fixed placing on chests & other actionable blocks");
        versions.add(new ChangelogLink("August 23rd", 85, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {!Media} rank for users");
        builder.add("Added {!Player Control} to {Freecam}");
        builder.improve("Fixed IP ban warning showing with LiquidProxy");
        versions.add(new ChangelogLink("August 24th", 86, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Trails:Blink only} option to {Trails}");
        builder.add("Added {AutoClicker:Inventory} options to {AutoClicker}");
        builder.add("Added {HUD:Other->Watermark} and {HUD:Other->Information} options to {HUD}");
        versions.add(new ChangelogLink("August 27th", 87, builder));
        versions.add(new ChangelogLink("Happy New Year " + ChatColor.BOLD + "2026" + ChatColor.RESET + "!",
                "Happy New Year from final and Calculus!",
                "Wow, it feels like it was just yesterday when I wrote the last New Years message. It's actually unbelievable.",
                "May 2026 be super awesome for y'all :)"));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Terrain:Mode:Legit} mode to {Terrain}");
        builder.add("Added {Aim Assist:Linear} mode to {Aim Assist}");
        builder.add("Added {NoSlowdown:Mode:Legit} mode to {NoSlowdown}");
        builder.add("Added {Crosshair:Inactivity Fade} option to {Crosshair}");
        builder.add("Added reset settings to default in {HUD:Click GUI} with Ctrl+Left Click");
        builder.add("Added toggle for the anti-cheat detection with {AntiCheat} module");
        builder.add("Added {Builder} module with {Builder:Bed Defense} feature");
        builder.add("Added {PlayerDetect:Hacker Detect->Announce} option to {PlayerDetect:Hacker Detect} in {PlayerDetect}");
        builder.add("Added {Safe Walk:Eagle->No sneak animation} option to {Safe Walk:Eagle} in {Safe Walk}");
        builder.add("Added {KillAura:Range->Through entities} option to {KillAura:Range} in {KillAura}");
        builder.add("Added {NoJumpDelay:Disable with fly} option to {NoJumpDelay}");
        builder.add("Added {MovementFix:Optimize scaffold} option to {MovementFix}");
        builder.add("Added {AntiVoid:Void Dist. Override Key:Void Distance Override Key} to {AntiVoid}");
        builder.add("Added {AntiAFK:Require Inactivity} option to {AntiAFK}");
        builder.add("Added {HUD:Click GUI->Separators} option to {HUD:Click GUI}");
        builder.add("Added {HUD:Widgets->Radar} to {HUD:Widgets} in {HUD}");
        builder.add("Added search bar to {HUD:Click GUI}");
        builder.add("Added {ESP:Players->Outline} option to {ESP}");
        builder.add("Added {Trajectories} module");
        builder.add("Added {ClientSpoofer} module");
        builder.add("Added {PixelParty} module");
        builder.add("Added {Zombies} module");
        builder.add("Added {Criticals} module (not for Hypixel)");
        builder.add("Added {!Friends} screen");
        builder.improve("Fixed particles and names rotating incorrectly with {ThirdPerson:Free Look} in {ThirdPerson}");
        builder.improve("Fixed {Sprint:Omnidirectional->Mode:Rotate} mode in {Sprint:Omnidirectional} {Sprint} interfering with item drops");
        builder.improve("Made {PearlAssist:Safety timer->Safewalk} in {PearlAssist} compatible with predictive anti-cheats");
        builder.improve("Moved {HUD:Event Timers} and {HUD:Player List} options into {HUD:Widgets}");
        builder.improve("Fixed {AutoHypixel:Auto Play->Map dodging} option in {AutoHypixel:Auto Play} in {AutoHypixel}");
        builder.improve("Fixed {ESP:Players->Preview} in {ESP} not working when not in a world");
        builder.improve("Added proper timestamps for messages over a week ago in IRC");
        builder.improve("Fixed {Fast Place} making it hard to use the return to lobby bed item");
        builder.improve("Changed minimum limit of {Breaker:Hypixel->Break priority time} option in {Breaker}");
        builder.improve("Fixed rainbow color type not being available for {HUD:Color} in {HUD}");
        builder.improve("Added scrolling handling in {HUD:Click GUI} for number sliders");
        builder.improve("Fixed pixel bleeding with scale options (lossless scaling)");
        builder.improve("Fixed {ChatFilter} patching decimal numbers as URLs");
        builder.improve("Fixed color type setting sometimes not being saved");
        builder.improve("Unpatched {ChatFilter} with insertion-bypassed words");
        builder.improve("Completely reworked internal rotation handling");
        builder.improve("Added snapping to HUD element dragging");
        builder.improve("Improved {AntiVoid:Mode:Legit} mode in {AntiVoid}");
        builder.improve("Fixed Credentials login");
        builder.improve("Unpatched {NoSlowdown:Mode:Hypixel} mode in {NoSlowdown}");
        builder.improve("Fixed {FastUse:Automation->Auto Tool} swapping on chests and ender chests in Bed Wars");
        builder.remove("Removed {Auto Bed Defense} (replaced by {Builder:Bed Defense} in {Builder} module)");
        builder.remove("Removed patched features");
        versions.add(new ChangelogLink("January 1st", 88, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {PluginScanner} module");
        builder.add("Added {Builder:Block In} to {Builder}");
        builder.add("Added {VoiceChat} module");
        builder.add("Added {NoSlowdown:Mode:NCP} mode to {NoSlowdown}");
        builder.add("Added more modes to {MoreKnockback}");
        builder.add("Added {Criticals:Delay} to {Criticals}");
        builder.improve("Fixed {AutoBlock} opening chests and interacting with other blocks");
        builder.improve("Fixed {AntiVoid:Mode:Legit} {AntiVoid} not working in-game");
        builder.improve("Improved LiquidProxy handling");
        builder.remove("Temporarily disabled ban checking in account manager (patched)");
        versions.add(new ChangelogLink("January 6th", 89, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Reach} module");
        builder.add("Added {Zombies:Auto Shoot->Rotate Snap} to {Zombies}");
        builder.add("Added {AntiVoid:Block Clutch} to {AntiVoid:Mode:Legit} {AntiVoid}");
        builder.add("Added {Scaffold:Addons->No ascend} to {Scaffold:Mode:Hypixel} {Scaffold}");
        builder.improve("Made the client color of other Adjust users visible everywhere ({HUD:Player List}, {ESP}, etc.)");
        builder.improve("Number and color settings can now be set by middle clicking on them in {HUD:Click GUI}");
        builder.remove("Removed blink from {Sprint:Omnidirectional->Mode:Rotate} mode in {Sprint:Omnidirectional} {Sprint}");
        versions.add(new ChangelogLink("January 9th", 90, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Zombies:HUD} to {Zombies}");
        builder.add("Added {VoiceChat:HUD} to {VoiceChat}");
        builder.add("Added {Tweaks:No hotbar scroll} to {Tweaks}");
        builder.add("Added {VoiceChat:Soundboard} to {VoiceChat}");
        builder.add("Added {BlockOverlay:Mob Spawner} to {BlockOverlay}");
        builder.add("Added client capes accessible from {HUD:Sense->Cape}");
        builder.improve("Fixed {KillAura:Rotations->Look For} in {KillAura} {KillAura:Rotations} being ignored");
        builder.improve("Fixed {Inventory} interfering with crafting in player inventory");
        versions.add(new ChangelogLink("January 14th", 91, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Regen} module");
        builder.add("Added {Hitboxes} module");
        builder.add("Added {Tweaks:No Title} to {Tweaks}");
        builder.add("Added {ESP:Theme:Pixel} {ESP:Theme} to {ESP}");
        builder.add("Added {Speed:Jump Ticks} to {Speed:Mode:NCP} {Speed}");
        builder.add("Added {AutoClicker:Trigger Bot} to {AutoClicker}");
        builder.add("Added {AutoHypixel:Auto Policies Accept} to {AutoHypixel}");
        builder.add("Added known plugin highlighting to {PluginScanner}");
        builder.improve("Fixed {*Statistics} command GUI not allowing mouse movement");
        versions.add(new ChangelogLink("January 31st", 92, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {DoubleJump} module with {DoubleJump:Mode:MMC} {DoubleJump:Mode}");
        builder.improve("Fixed flags with {NoSlowdown:Mode:Hypixel} {NoSlowdown} when taking velocity");
        versions.add(new ChangelogLink("February 1st", 93, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {HitSelect} module");
        builder.add("Added {Displacement} module");
        builder.add("Added {KnockbackDelay} module");
        builder.add("Added {BlockSwap} module");
        builder.add("Added {GuessTheBuild} module");
        builder.add("Added {Stasis} module");
        builder.add("Added {Jesus} module");
        builder.add("Added {TrueSight} module");
        builder.add("Added {SafeWalk:Eagle:Avoid double sneak} to {SafeWalk}");
        builder.add("Added {KillAura:Rotations:Visibility} option to {KillAura} {KillAura:Rotations}");
        builder.add("Added {AutoBlock:Mode:Predictive} mode to {AutoBlock}");
        builder.add("Added {Builder:Head Hitter} to {Builder}");
        builder.add("Added {Inventory:Addons:Quick Buy} to {Inventory}");
        builder.add("Added {GameSpeed:Mode:Hypixel} mode to {GameSpeed}");
        builder.add("Added {FastPlace:Activation delay} to {FastPlace}");
        builder.add("Added {GhostHand:Opacity} to {GhostHand}");
        builder.add("Added {AutoHypixel:Chat Stats} to {AutoHypixel}");
        builder.add("Added {InventoryMove:Mode:Legit} mode to {InventoryMove}");
        builder.add("Added {Scaffold:Mode:NCP} mode to {Scaffold}");
        builder.add("Added {Scaffold:Addons->Auto stasis} to {Scaffold}");
        builder.add("Added {Scaffold:Addons->No ascend->Mode:Buffer} mode to {Scaffold:Addons:No ascend} in {Scaffold}");
        builder.add("Added {ESP:Players->Border->Team color} to {ESP:Players->Border} in {ESP}");
        builder.add("Added {PlayerDetect:Hypixel:Vanish Detect} to {PlayerDetect}");
        builder.add("Added {ClientSpoofer:Mode:Lunar Client} mode to {ClientSpoofer}");
        builder.add("Added Refresh account type to Account Manager");
        builder.add("Added {!Fernan's Club Integration}");
        builder.add("Added support for {!Urchin API}");
        builder.add("Added modern command auto completion");
        builder.improve("Unpatched {NoSlowdown:Mode:Hypixel} {NoSlowdown}");
        builder.improve("Unpatched {Scaffold:Mode:Hypixel} {Scaffold}");
        builder.improve("Recoded {MoreKnockback}");
        builder.improve("Recoded {Backtrack}");
        builder.improve("Recoded {Builder:Block In} in {Builder}");
        builder.improve("Recoded {Builder:Bed Defense} in {Builder}");
        builder.improve("Made {FastUse:Automation} in {FastUse} instantly swap back when attacking or placing blocks");
        builder.improve("Improved {Zombies:Auto Shoot:Prediction} in {Zombies}");
        builder.improve("Made {Trajectories} show the path of live projectiles");
        builder.improve("{HUD:Widgets:Player List} now uses Cubelify's color scheme in Bed Wars");
        builder.improve("Fixed OptiFine capes not rendering");
        builder.improve("Fixed other Adjust users HUD colors not being correctly updated");
        builder.improve("Updated hardware ID algorithm");
        builder.remove("Removed Delay from {Velocity:Normal} {Velocity} in favor of {KnockbackDelay}");
        builder.remove("Removed Teams column from the {HUD:Widgets:Player List} in favor of recoloring the players name");
        builder.remove("Removed patched features");
        versions.add(new ChangelogLink("October 13th", 94, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {Atmosphere:Fog} to {Atmosphere} module");
        builder.add("Added {Motion Blur} module");
        builder.add("Added Disable On {BackTrack:Disable On:Knockback} to {BackTrack} module");
        builder.add("Added {Tweaks:Chat:Player heads} in {Tweaks:Chat} to {Tweaks} module");
        builder.add("Added a button to copy chat messages");
        builder.add("Added Wait to place sliders in {Builder:Bed Defense} and {Builder:Block In} in {Builder} module");
        builder.add("Added {Tweaks:Borderless Fullscreen} in {Tweaks} module");
        builder.improve("Fixed {FastPlace} interfering with {Scaffold}");
        builder.improve("Fixed setbacks in {BackTrack} and {Knockback Delay} module");
        builder.improve("Fixed perspective not working properly when bound to a mouse button");
        builder.improve("Improved {Trajectories} module");
        versions.add(new ChangelogLink("October 14th", 95, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added emojis to the global chat");
        builder.add("Added {Third Person:Free Look:Hold} to {Third Person:Free Look} in {Third Person} module");
        builder.add("Added loaddefenses sub-command to {*Configuration} command for loading bed defense presets (.c loaddefenses <cfg>)");
        builder.add("Added Setting to disable held projectile lines to {Trajectories} module");
        builder.add("Added {Tweaks:Zoom:No cinematic} and {Tweaks:Zoom:Scroll zoom} in {Tweaks:Zoom} to {Tweaks} module");
        builder.add("Added {More Particles} module");
        builder.add("Added {AutoHypixel:Bed Wars:Anti Obsidian Misplace} to {AutoHypixel:Bedwars} in {AutoHypixel} module");
        builder.add("Added {AutoHypixel:Bed Wars->Bed Wars Tracker->Notifications->Mode} to {AutoHypixel:Bed Wars->Bed Wars Tracker->Notifications} in {AutoHypixel:Bed Wars->Bed Wars Tracker}");
        builder.add("Added {AutoHypixel:Bed Wars->Bed Wars Tracker->Notifications->Ping} to {AutoHypixel:Bed Wars->Bed Wars Tracker->Notifications} in {AutoHypixel:Bed Wars->Bed Wars Tracker}");
        builder.add("Added more items to {AutoHypixel:Bed Wars->Bed Wars Tracker}");
        builder.improve("Fixed players being falsely detected in Bed Wars pre lobbies");
        builder.improve("Made {Tweaks:Chat} {Tweaks:Chat:Modern suggestions} toggleable in {Tweaks} module");
        builder.improve("Added a button to delete chat messages");
        builder.improve("Fixed inventory modules closing your inventory when trying to craft");
        builder.improve("Made toggled modules in the {Hud:Click GUI} brighter");
        builder.improve("Taking a screenshot now automatically copies it to clipboard");
        builder.improve("Improved {ESP:Items} in {ESP} module");
        builder.improve("Combat modules no longer target team silverfish and golems");
        versions.add(new ChangelogLink("October 16th", 96, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {!Localts Integration}");
        builder.add("Added {AutoRespawn} module");
        builder.add("Added {AutoBlock:Ping} to {AutoBlock:Mode:Predictive} {AutoBlock}");
        builder.add("Added {Sprint:Ignore} {Sprint:Ignore:Using Item} to {Sprint} module");
        builder.add("Added {Tweaks:Bow} to {Tweaks} module");
        builder.improve("Fixed {AntiAim} crashing when {AntiAim:Disable on} {AntiAim:Disable on->Rotations} is enabled");
        builder.improve("Fixed {!Urchin API} not working for some users");
        builder.improve("Recoded {Sprint:Keep sprint:Hypixel} {Sprint:Keep sprint} in {Sprint} module");
        builder.improve("Fixed {NoSlowdown:Mode:Hypixel} {NoSlowdown} making you use random items");
        builder.improve("Improved {AutoBlock:Ping:Automatic} in {AutoBlock:Mode:Predictive} {AutoBlock}");
        builder.improve("Improved {Scaffold:Mode:Hypixel} mode in {Scaffold} module");
        builder.improve("Made {AutoClicker:Inventory:CPS override} in {AutoClicker} faster");
        builder.improve("Fixed {Team Detection} not working properly when nicked on Hypixel");
        builder.remove("Temporarily removed {Zombies} module because it is being recoded");
        versions.add(new ChangelogLink("October 31st", 97, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {AutoRewound} module");
        builder.add("Added {Criticals:Mode:Rewound} mode to {Criticals} module");
        builder.add("Added {NoFall:Mode:Rewound} mode to {NoFall} module");
        builder.add("Added {InventoryMove:Allow interactions} option to {InventoryMove:Mode:Vanilla} mode in {InventoryMove}");
        builder.add("Added {NoSlowdown:Mode:Rewound} mode to {NoSlowdown} module");
        builder.add("Added {Scaffold:Mode:Rewound} mode to {Scaffold} module");
        builder.add("Added {Flight:Mode:Rewound} mode to {Flight} module");
        builder.add("Added {Speed:Mode:Rewound} mode to {Speed} module");
        builder.add("Added {Jesus:Mode:Rewound} mode to {Jesus} module");
        builder.add("Added {AntiVoid:Mode:Packet} mode to {AntiVoid} module");
        builder.add("Added {AntiVoid:Mode:Bounce} mode to {AntiVoid} module");
        builder.add("Added {Phase:Mode:Vertical} mode to {Phase} module");
        builder.add("Added {Phase:Mode:Packetless} mode to {Phase} module");
        builder.add("Added {Step:Height} to {Step} module");
        versions.add(new ChangelogLink("November 6th", 98, builder));

        builder = new ChangelogVersionBuilder();
        builder.add("Added {HighJump} module");
        builder.add("Added {Speed:Disabler} option to {Speed:Mode:Rewound} {Speed}");
        builder.add("Added {AutoRewound:Hide bots} option to {AutoRewound}");
        builder.add("Added support for new game modes on Rewound");
        builder.improve("Improved {Criticals:Mode:Rewound} mode in {Criticals}");
        builder.improve("Improved {Flight:Mode:Rewound} mode in {Flight}");
        builder.improve("Improved {Speed:Mode:Rewound} mode in {Speed}");
        builder.improve("Improved {AutoRewound:Pit->Spade Swap} and {AutoRewound:Pit->Sword Swap} in {AutoRewound}");
        builder.improve("Fixed Pit spawn not being detected on Rewound");
        builder.remove("Removed {AntiVoid:Mode:Bounce} mode in {AntiVoid} in favor of {HighJump}");
        versions.add(new ChangelogLink("November 8th", 99, builder));

        for (ChangelogLink version : versions) {
            if (version.id != -1 && version != findVersion(version.id)) {
                throw new RuntimeException("dup id: " + version.id);
            }
        }
        latest = versions.get(versions.size() - 1);
        contentHash = 0;
        updateUnread(versions.get(versions.size() - 2));
        contentHash = Objects.hash(latest.id, contentHash);
        versionCode = ((latest.id & 0xFFFF) << 16) | (contentHash & 0xFF);
    }
}
