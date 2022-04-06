import {accessMember, createMember, deleteMember} from "./memberCommands";
import {accessSystem, autoOff, autoOn, spOff, spOn, createSystem, deleteSystem, exportSystem, importSystem, listSystem, setAvatar, setTag, setName, setDesc, createSwitch} from "./systemCommands";
import * as discord from "discord.js";
import { setRole } from "./serverRole";
import { createGroup, createSubsys } from "./groupCommands";

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
    "name": setName,
    "rename": setName,
    "description": setDesc
}

let autoTree = {
    "default": "Please specify whether you want autoproxy on or off.",
    "on": autoOn,
    "off": autoOff
}

let spTree = {
    "default": "Please specify whether you want autoproxy on or off.",
    "on": spOn,
    "off": spOff
}

let subSysTree = {
    "new": createSubsys,
    "create": createSubsys
}

let groupTree = {
    "new": createGroup,
    "create": createGroup
}

let help = `To view commands for ProxyFox, visit <https://github.com/ProxyFox-developers/ProxyFox/blob/master/commands.md>
For quick setup:
- pf>system new name
- pf>member new John Doe
- pf>member "John Doe" proxy j:text`;

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
    "invite": "Proxyfox invites are temporarily disabled, as we're transitioning to a new bot. Contact Octal#9139 if you need an invite. https://discord.gg/M2uBsJmRNT", //"Use https://discord.com/oauth2/authorize?client_id=872276960951296051&scope=bot&permissions=258302340160 to invite ProxyFox to your server!\nTo get support, head on over to https://discord.gg/q3yF8ay9V7",
    "source": "Source code for ProxyFox is available at <https://github.com/ProxyFox-developers/ProxyFox>!",
    "explain": `ProxyFox is modern Discord bot designed to help systems communicate.
It uses discord's webhooks to generate "pseudo-users" which different members of the system can use. Someone will likely be willing to explain further if need be.`,
    "role": setRole,
    "sw": createSwitch,
    "switch": createSwitch,
    "edit": "",
    "delete": "",
    "ping": "",
    /*/
    "group": groupTree,
    "g": groupTree,
    "subsys": subSysTree,
    "sub": subSysTree
    //*/
};