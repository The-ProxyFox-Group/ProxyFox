import { parse as parseMessage } from "./commandProcessor";
import { tree } from "./commandTree";
import * as discord from "discord.js";
import * as fs from "fs";
import { webhook } from "./sendMessage";
import { start } from "./webServer";
import process from 'process';
import { loadAll } from "./saveLoad";
export const client = new discord.Client({
    //@ts-ignore
    intents: [
        discord.Intents.FLAGS.GUILDS,
        discord.Intents.FLAGS.GUILD_MESSAGES,
        discord.Intents.FLAGS.GUILD_WEBHOOKS,
        discord.Intents.FLAGS.DIRECT_MESSAGES,
        discord.Intents.FLAGS.GUILD_MESSAGE_REACTIONS,
        discord.Intents.FLAGS.DIRECT_MESSAGE_REACTIONS,
        discord.Intents.FLAGS.GUILD_INTEGRATIONS
    ]
});
export const client2 = new discord.Client({
    //@ts-ignore
    intents: [
        discord.Intents.FLAGS.GUILDS,
        discord.Intents.FLAGS.GUILD_MESSAGES,
        discord.Intents.FLAGS.GUILD_WEBHOOKS,
        discord.Intents.FLAGS.DIRECT_MESSAGES,
        discord.Intents.FLAGS.GUILD_MESSAGE_REACTIONS,
        discord.Intents.FLAGS.DIRECT_MESSAGE_REACTIONS,
        discord.Intents.FLAGS.GUILD_INTEGRATIONS
    ]
});

console.log("starting");

process.on('unhandledRejection', (reason, promise) => {
    console.log('Unhandled Rejection at:', promise, 'reason:', reason);
});

process.on("uncaughtException", error => {
    console.log(error.name);
    console.log(error.message);
})

const keys = JSON.parse(fs.readFileSync("./key.json").toString());

function handleMessage(msg: discord.Message): boolean {
    let parsedMsg = parseMessage(msg.content);
    if (parsedMsg.length > 0) {
        let currTree = tree;
        for (let i = 0; i <= parsedMsg.length; i++) {
            //@ts-ignore
            if (parsedMsg[i] == null || parsedMsg[i] == undefined || parsedMsg == "") currTree = currTree.default;
            //@ts-ignore
            else if (currTree[parsedMsg[i].toLowerCase()] == null) currTree = currTree.default;
            else currTree = currTree[parsedMsg[i].toLowerCase()];
            
            let breakCond: boolean = false;

            switch (typeof currTree) {
                case "string":
                    if (!currTree) return true;
                    msg.channel.send(currTree).catch(err => {
                        sendError(msg,err);
                    });
                    return true;
                case "function":
                    //@ts-ignore
                    let output: string = currTree(msg,parsedMsg);
                    if (!!output)
                        msg.channel.send(output).catch(err => {
                            sendError(msg,err);
                        }).catch(err => {
                            sendError(msg,err);
                        });
                    return true;
                case "object":
                    break;
            }
        }
        return true;
    }
    return false;
}

client.on('messageCreate', async msg => {
    if (msg.author.system || msg.author.bot || msg.system || msg.webhookId) return;
    try {
        if (!handleMessage(msg))
            webhook(msg);
        else {
            let author;
            try {
                author = await msg.guild.members.fetch({user: "822926373064671252", force:true});
            } catch(e) {}
            if (!author)
                msg.channel.send(`
**Note:** This version of ProxyFox is depricated, meaning it will go away soon. This is due to restrictions of how many servers a bot can be in with it's author before verification.
To get the latest version of ProxyFox, contact Octal#9139 for the bot invite.
https://discord.gg/M2uBsJmRNT
`)
        }
    } catch (err) {
        sendError(msg,err);
    }
});
client2.on('messageCreate', msg => {
    if (msg.author.system || msg.author.bot || msg.system || msg.webhookId) return;
    try {
        if (!handleMessage(msg))
            webhook(msg);
        else if (!msg.content.contains("edit") && !msg.content.contains("delete"))
        msg.channel.send(`
**Note:** Because of verification, ProxyFox 2 cannot be in more than two servers that you own. If you own more than 2 servers with ProxyFox 2, please remove it to aid with verification. Thank you!
`)
    } catch (err) {
        sendError(msg,err);
    }
});


let count = 0;

function getTime(duration) {
    var seconds: any = Math.floor((duration / 1000) % 60),
        minutes: any = Math.floor((duration / (1000 * 60)) % 60),
        hours: any = Math.floor((duration / (1000 * 60 * 60)) % 24);
    
    hours = (hours < 10) ? "0" + hours : hours;
    minutes = (minutes < 10) ? "0" + minutes : minutes;
    seconds = (seconds < 10) ? "0" + seconds : seconds;
    
    return hours + ":" + minutes + ":" + seconds ;
}

function setPres(text: string, since?: number): void {
    let activity: string;
    if (count == 0)
        activity = text + " In " + client.guilds.cache.size + " servers";
    else {
        let now = Date.now();
        let time = now - since;
        activity = text + " Uptime: "+getTime(time);
    }

    client.user.setPresence({
        status: "online",
        activities: [{name: activity}]
    });
    if (count == 0)
        activity = text + " In " + client2.guilds.cache.size + " servers";
    else {
        let now = Date.now();
        let time = now - since;
        activity = text + " Uptime: "+getTime(time);
    }

    client2.user.setPresence({
        status: "online",
        activities: [{name: activity}]
    });
}

export function sendError(msg: discord.Message, err: any) {
    let timestamp: Date = new Date();
    let timestampString = "\n`===TIMESTAMP="+timestamp.getTime().toString().trim() + "===`\n";
    console.log(timestampString + err);
    msg.channel.send("Unexpected error" + timestampString + err).catch(() => {});
}

client.on("ready", () => {
    let since = Date.now();
    setPres("Run pf>help for help!",since);
    count++;
    count %= 2;
    setInterval(() => {
        setPres("Run pf>help for help!",since);
        count++;
        count %= 2;
    }, 30000);
    console.log("online");
});
loadAll();
start();
client2.login(keys.new);
client.login(keys.main);
