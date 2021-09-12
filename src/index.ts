import { parse as parseMessage } from "./commandProcessor";
import { tree } from "./commandTree";
import * as discord from "discord.js";
import * as fs from "fs";
import { webhook } from "./sendMessage";
import { start } from "./webServer";
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
                    msg.channel.send(currTree).catch(err => {
                        sendError(msg,err);
                    });;
                    return true;
                case "function":
                    //@ts-ignore
                    let output: string = currTree(msg,parsedMsg);
                    if (output != undefined && output != null && output != "")
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

client.on('messageCreate', msg => {
    if (msg.author.system || msg.author.bot || msg.system || msg.webhookId) return;
    try {
        if (!handleMessage(msg))
            webhook(msg);
    } catch (err) {
        sendError(msg,err);
    }
});

function setPres(text: string) {
    client.user.setPresence({
        //@ts-ignore
        activities: [{name: text}],
        status: "online"
    });
}

export function sendError(msg: discord.Message, err: any) {
    let timestamp: Date = new Date();
    let timestampString = "\n`===TIMESTAMP="+timestamp.getTime().toString().trim() + "===`\n";
    console.log(timestampString + err);
    msg.channel.send("Unexpected error" + timestampString).catch(() => {});
}

function toTime(time: number):string {
    let minutes = Math.floor(time/60);
    let seconds = time-(minutes*60);
    let hours = Math.floor(minutes/60);
    minutes %= 60;
    let hStr = hours.toString();
    let mStr = minutes.toString().length == 1? "0"+minutes.toString(): minutes.toString();
    let sStr = seconds.toString().length == 1? "0"+seconds.toString(): seconds.toString();

    return hStr + ":" + mStr + ":" + sStr;
}

client.on("ready", () => {
    let uptime = 0;
    setPres("Run pf>help for help! Online for: 0:00:00");
    setInterval(() => {
        setPres("Run pf>help for help! Online for: " +toTime(++uptime*30));
    }, 30000);
    console.log("online");
});

console.log("starting");
start();
client.login(keys.main);