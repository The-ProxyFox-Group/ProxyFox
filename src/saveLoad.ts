import * as fs from "fs";
import { System } from "./systemClass";
import * as discord from "discord.js";
import { sendError } from ".";

class Systems {
    values: {[id:string]:System} = {}

    constructor() {}

    update(id:string, system: System) {
        this.values[id] = system
    }
    exists(id:string): boolean {
        return !!this.values[id]
    }

    toJson(): Object {
        let out = {}
        
        for (let i in this.values)
            out[i] = this.values[i].toJson();
        return out;
    }

    static fromJson(json): Systems {
        let out = new Systems()
        for (let i in json)
            out.update(i,System.fromJson(json[i]));
        return out;
    }
}

var systems: Systems = new Systems();

export function delete_(id:string) {
    systems.update(id,undefined)
}

export function load(id:string):System {
    if (!systems.exists(id)) {
        systems.update(id,System.fromStr(fs.readFileSync("./systems/"+id+".json").toString()));
        fs.unlinkSync("./systems/"+id+".json");
    }
    return systems.values[id];
}

export function loadAll() {
    let json = fs.readFileSync("./systems.json").toJSON();
    systems = Systems.fromJson(json);
}

export function save(id:string, system:System) {
    systems.update(id,system);
    fs.writeFileSync("./systems.json",JSON.stringify(systems.toJson()));
}

export function saveExport(id:string, system:System) {
    fs.writeFileSync("./systems/"+id+"_export.json",system.toExportString().replace(/\n/g,"\\n"));
}

export function exists(id:string,msg:discord.Message):boolean {
    if (systems.exists(id)) return true;
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