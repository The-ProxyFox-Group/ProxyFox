import * as fs from "fs";
import { System } from "./systemClass";
import * as discord from "discord.js";
import { sendError } from ".";

export function load(id:string):System {
    return System.fromStr(fs.readFileSync("./systems/"+id+".json").toString());
}

export function save(id:string, system:System) {
    fs.writeFileSync("./systems/"+id+".json",system.toString().replace(/\n/g,"\\n"));
}

export function saveExport(id:string, system:System) {
    fs.writeFileSync("./systems/"+id+"_export.json",system.toExportString().replace(/\n/g,"\\n"));
}

export function exists(id:string,msg:discord.Message):boolean {
    if (!fs.existsSync("./systems/"+id+".json")) return false;
    try {
        load(id);
    } catch(e) {
        msg.channel.send("An unexpected error ocurred while loading your system. Exporting and deleting.\nReminder: Systems imported from Tupperbox **will not** work.");
        msg.author.createDM().then(channel => {
            //@ts-ignore
            channel.send({
                files: [new discord.MessageAttachment("./systems/"+id+".json", "system.json")]
            }).then(message => {
                channel.send(message.attachments.map(a=>a)[0].url);
                fs.unlinkSync("./systems/"+msg.author.id+".json");
            }).catch(err => {
                sendError(msg,err);
            });
        }).catch(err => {
            sendError(msg,err);
        });
        return false;
    }
    return true;
}