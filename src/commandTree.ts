import {accessMember, createMember, deleteMember} from "./memberCommands";
import {accessSystem, autoOff, autoOn, createSystem, deleteSystem, exportSystem, importSystem, listSystem} from "./systemCommands";
import * as discord from "discord.js";
import { time } from "console";

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
}

let autoTree = {
    "default": "Please specify weather you want autoproxy on or off.",
    "on": autoOn,
    "off": autoOff
}

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
    "time": getTime
};
