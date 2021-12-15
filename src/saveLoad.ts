import * as fs from "https://deno.land/std/fs/mod.ts";
import { System } from "./systemClass.ts";
import * as discord from "https://code.harmony.rocks/main";
import { sendError } from "./index.ts";

export function load(id:string):System {
    return System.fromStr(Deno.readTextFileSync("./systems/"+id+".json"));
}

export function save(id:string, system:System) {
    if (!fs.existsSync("./systems/"+id+".json")) Deno.createSync("./systems/"+id+".json")
    Deno.writeTextFileSync("./systems/"+id+".json",system.toString().replace(/\n/g,"\\n"));
}

export function saveExport(id:string, system:System) {
    Deno.writeTextFileSync("./systems/"+id+"_export.json",system.toExportString().replace(/\n/g,"\\n"));
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
                Deno.removeSync("./systems/"+msg.author.id+".json");
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