import { sendError } from ".";
import { Member } from "./memberClass";
import { exists, load, save, saveExport } from "./saveLoad";
import { System } from "./systemClass";
import * as discord from "discord.js";
import * as fs from "fs";
import { Group } from "./groupClass";

export function createGroup(msg: discord.Message, parsedMessage: string[]): string {
    if (!exists(msg.author.id,msg)) return;
    let system: System = load(msg.author.id, msg);
    parsedMessage.shift();
    let name = parsedMessage.join(" ");
    system.groups.push(new Group(name));
    save(msg.author.id, system);
    return "Group `"+name+"` created!"
}
export function createSubsys(msg: discord.Message, parsedMessage: string[]): string {
    if (!exists(msg.author.id,msg)) return;
    let system: System = load(msg.author.id, msg);
    parsedMessage.shift();
    let name = parsedMessage.join(" ");
    system.subsystems.push(new Group(name));
    save(msg.author.id, system);
    return "Subsystem `"+name+"` created!"
}