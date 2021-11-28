import {accessMember, createMember, deleteMember} from "./memberCommands";
import {accessSystem, autoOff, autoOn, spOff, spOn, createSystem, deleteSystem, exportSystem, importSystem, listSystem, setAvatar, setTag, setName} from "./systemCommands";
import * as discord from "discord.js";
import { setRole } from "./serverRole";

function getTime(msg: discord.Message, parsedMessage: string[]):string {
    let time: number = new Date().valueOf()/1000;
    return `It is currently <t:${Math.floor(time)}:f>`
}

let memberTree = {
    "default": accessMember,
    "new": createMember,
    "add": createMember,
    "create": createMember,
    "delete": deleteMember,
    "remove": deleteMember
};

let systemTree = {
    "default": accessSystem,
    "new": createSystem,
    "add": createSystem,
    "create": createSystem,
    "delete": deleteSystem,
    "remove": deleteSystem,
    "list": listSystem,
    "tag": setTag,
    "avatar": setAvatar,
    "picture": setAvatar,
    "name": setName
}

let autoTree = {
    "default": "Please specify weather you want autoproxy on or off.",
    "on": autoOn,
    "off": autoOff
}

let spTree = {
    "default": "Please specify weather you want autoproxy on or off.",
    "on": spOn,
    "off": spOff
}

let help = `To get your system started:
- pf>system new <name>
    - Creates a system with <name> as the name.
- pf>member new <name>
    - Creates a member with <name> as the name
- pf>member <name> proxy <proxy>
    - Sets <name>'s proxy to <proxy>
- pf>member <name> avatar
    - Attach an image to set <name>'s avatar
**Or**
Use pf>import to import a system from PluralKit or any other proxy bot.
**Note: Currently, TupperBox is NOT supported for imports`;

export const tree = {
    "default": "Unknown command.",
    "member": memberTree,
    "m": memberTree,
    "system": systemTree,
    "s": systemTree,
    "list": listSystem,
    "import": importSystem,
    "export": exportSystem,
    "auto": autoTree,
    "ap": autoTree,
    "autoproxy": autoTree,
    "proxy": spTree,
    "serverproxy": spTree,
    "time": getTime,
    "help": help,
    "invite": "Use https://discord.com/oauth2/authorize?client_id=872276960951296051&scope=bot&permissions=258302340160 to invite ProxyFox to your server!\nTo get support, head on over to https://discord.gg/q3yF8ay9V7",
    "source": "Source code for ProxyFox is available at <https://github.com/Oliver-makes-code/ProxyFox>!",
    "explain": `ProxyFox is a bot to help those with DID/OSDD-1 communicate
It uses discord's webhooks to generate "pseudo-users" which different members of the system can use.
https://did-research.org/origin/structural_dissociation/ explains why and how DID/OSDD forms`,
    "role": setRole
};