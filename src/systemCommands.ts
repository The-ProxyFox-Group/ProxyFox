import * as discord from "discord.js";
import * as fs from "fs";
import * as https from "https";

export function accessSystem(msg: discord.Message, parsedMessage: string[]) {
    
}

export function createSystem(msg: discord.Message, parsedMessage: string[]):string {
    return "test";
}

export function deleteSystem(msg: discord.Message, parsedMessage: string[]):string {
    return "test";
}

export function listSystem(msg: discord.Message, parsedMessage: string[]):string {
    return "test";
}

export function exportSystem(msg:discord.Message, parsedMessage: string[]) {
    if (fs.existsSync("./systems/"+msg.author.id+".json")) {
        msg.channel.send("Check your DMs :>");
        msg.author.createDM().then(channel => {
            channel.send(getSysExportMessage(msg.author.id.toString())).then(message => {
                channel.send(message.attachments.array()[0].url);
            })
        });
        return;
    }
    msg.channel.send("No system registered.");
}

export function importSystem(msg:discord.Message, parsedMessage: string[]):string {
    if (parsedMessage.length > 1) {
        getData(parsedMessage[parsedMessage.length-1],"./systems/"+msg.author.id+".json");
        return "System imported.";
    }
    if (msg.attachments.array().length > 0) {
        let url = msg.attachments.array()[0].url;
        getData(url,"./systems/"+msg.author.id+".json");
        return "System imported.";
    }
    return "No system to import.";
}

function getSysExportMessage(id:string):discord.MessageAttachment {
    return new discord.MessageAttachment("./systems/"+id+".json");
}

function getSys(id:string):string {
    return fs.readFileSync("./systems/"+id+".json").toString();
}

export function getData(url:string,path:string) {
    let output = ""
    https.get(url,res => {
        res.on("data", chunk => {
            output += chunk;
        })
    }).on("close", () => {
        fs.writeFileSync(path,output);
    });
}