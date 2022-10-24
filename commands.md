# ProxyFox Command List

### System Commands (`pf>system|s`)

- `new|n|add|create <name>`

  - Creates a system with an optional name
  
- `name|rename <name>`

  - Changes the system's name
  
- `list|l`

  - Lists the system
  
    - Optional `--verbose` parameter to list with more information
    
- `color|colour <color>`

  - Changes the default system color
  
- `pronouns|p <pronouns>`

  - Changes the system pronouns
  
- `description|desc|d <description>`

  - Changes the system description
  
- `avatar|pfp <link or attached>`

  - Changes the default system avatar
  
- `tag <tag>`

  - Changes the system's tag

### Member Commands (`pf>member|m`)

- `new|n|add|create <name>`

  - Creates a member with the given name (required)

- `delete|del|remove <member>` | `<member> delete|del|remove`
  - Deletes the given member

- `<member>`

  - Displays the member card

- `<member> name|rename`

  - Displays the member's name

- `<member> name|rename <name>`

  - Changes the member's name

- `<member> nickname|nick|displayname|dn`

  - Dispalys the members name
  
    - Optional `--clear` parameter to clear the name

- `<member> nickname|nick|displayname|dn <name>`

  - Changes the member's display name

- `<member> servername|servernick|sn`

  - Displays the member's server display name

    - Optional `--clear` parameter to clear the name

- `<member> servername|servernick|sn <name>`

  - Changes the member's server display name

- `<member> description|desc|d`

  - Displays the member's description
  
    - Optional `--raw` parameter to show the raw description text
    
    - Optional `--clear` parameter to clear the description

- `<member> description|desc|d <description>`

  - Changes the member's description

- `<member> avatar|pfp`

  - Displays the member's avatar
  
    - Optional `--clear` parameter to clear the avatar

- `<member> avatar|pfp <pfp>`

  - Changes the member's avatar

- `<member> serveravatar|serverpfp|sp|sa`

  - Displays the member's server avatar

    - Optional `--clear` parameter to clear the avatar

- `<member> serveravatar|serverpfp|sp|sa <pfp>`

  - Changes the member's server avatar

- `<member> pronouns`

  - Displays the member's pronouns

    - Optional `--raw` parameter to show the raw pronouns

    - Optional `--clear` parameter to clear the pronouns

- `<member> color`
  
  - Displays the member's color

- `<member> color <color>`

  - Changes the member's color

- `<member> birthday|bd`

  - Displays the member's birthday

    - Optional `--clear` parameter to clear the birthday

- `<member> birthday|bd <date>`

  - Changes the member's birthday

- `<member> autoproxy|ap [on|off]`

  - Displays or changes whether autoproxy is enabled for the member.

- `<member> proxy|p`

  - Displays the member's proxy tags

- `<member> proxy|p add <proxy>`

  - Adds a proxy tag

- `<member> proxy|p remove <proxy>`

  - Removes a proxy tag

- `<member> proxy|p <proxy>`

  - Adds a proxy tag

### Switch Commands (`pf>switch|sw`)

- `out|o`

  - Marks no members as fronting

- `move|mv|m <time>`

  - Moves the switch back by the provided time

- `delete|del|remove`

  - Deletes the latest switch

- `list|l`

  - List switches sorted by most recent

- `<..members>`

  - Marks the provided members as fronting

### Miscellaneous Commands (`pf>`)

- `import <url|file>`

  - Imports a system (TupperBox imports are currently disabled)

- `export`

  - Exports your system in a PluralKit compatible format

- `time`

  - Displays the current time, automatically adjusted to timezone

- `help`

  - Displays a help message

- `explain`

  - Explains this bot and it's purpose

- `invite`

  - Sends a link to invite this bot to your server

- `source`

  - Sends a link to this GitHub repository

- `proxy|p on|enable/off|disable`

  - Either enables or disables proxying for the current server

- `autoproxy|ap`

  - Displays the current autoproxy mode

- `autoproxy|ap front|f`

  - Makes you autoproxy as the first fronting member

- `autoproxy|ap latch|l`

  - Makes you autoproxy as the last proxied member

- `autoproxy|ap <member>`

  - Makes you autoproxy as the provided member

- `autoproxy off|disable`

  - Disables autoproxy

- `role`

  - Displays the current role required for proxying
  
    - Optional `--clear` parameter to remove the required role

- `role <role>`

  - Makes it so only users with the provided role can proxy (Moderator only)

- `delete|del <message>`

  - Deletes the provided message (Can be in reply)

- `reproxy|rp <member>`
  
  - Reproxy the latest message you've sent as member (Can also be in reply)

- `info|i <message>`

  - Fetches and DMs you info about the member that sent the message (Can also be in reply)

- `ping|p <message>`

  - Pings the message creator (Can be in reply)

- `edit|e <content>`

  - Replaces the message content

- `channel|c proxy on|enable/off|disable`

  - Toggles proxying for this channel (Moderator only)

- `debug`

  - Prints debug information.