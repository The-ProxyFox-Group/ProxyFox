import { parse as parseMessage } from "./commandProcessor.ts";
import { tree } from "./commandTree.ts";
import * as discord from "https://code.harmony.rocks/main";
import { webhook } from "./sendMessage.ts";
import { start } from "./webServer.ts";
export const client = new discord.Client({
    //@ts-ignore
    intents: [
        discord.GatewayIntents.GUILDS,
        discord.GatewayIntents.GUILD_MESSAGES,
        discord.GatewayIntents.GUILD_WEBHOOKS,
        discord.GatewayIntents.DIRECT_MESSAGES,
        discord.GatewayIntents.GUILD_MESSAGE_REACTIONS,
        discord.GatewayIntents.DIRECT_MESSAGE_REACTIONS,
        discord.GatewayIntents.GUILD_INTEGRATIONS
    ]
});
console.log("starting");

const keys = JSON.parse(Deno.readTextFileSync("./key.json"));

function handleMessage(msg: discord.Message): boolean {
    let parsedMsg = parseMessage(msg.content);
    if (parsedMsg.length > 0) {
        let currTree = tree;
        for (let i = 0; i <= parsedMsg.length; i++) {
            //@ts-ignore
            if (parsedMsg[i] == null || parsedMsg[i] == undefined || parsedMsg == "") currTree = currTree.default;
            //@ts-ignore
            else if (currTree[parsedMsg[i].toLowerCase()] == null) currTree = currTree.default;
            //@ts-ignore
            else currTree = currTree[parsedMsg[i].toLowerCase()];
            
            let breakCond: boolean = false;

            switch (typeof currTree) {
                case "string":
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
                        })
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
    if (msg.author.system || msg.author.bot || msg.author.system) return;
    try {
        if (!handleMessage(msg))
            webhook(msg);
    } catch (err) {
        sendError(msg,err);
    }
});

let count = 0;

function getTime(duration: number) {
    var seconds: any = Math.floor((duration / 1000) % 60),
        minutes: any = Math.floor((duration / (1000 * 60)) % 60),
        hours: any = Math.floor((duration / (1000 * 60 * 60)) % 24);
    
    hours = (hours < 10) ? "0" + hours : hours;
    minutes = (minutes < 10) ? "0" + minutes : minutes;
    seconds = (seconds < 10) ? "0" + seconds : seconds;
    
    return hours + ":" + minutes + ":" + seconds ;
}

async function setPres(text: string, since: number): Promise<void> {
    let activity: string;
    if (count == 0)
        activity = text + " In " + (await client.guilds.size()) + " servers";
    else {
        let now = Date.now();
        let time = now - since;
        activity = text + " Uptime: "+getTime(time);
    }

    client.setPresence({
        status: "online",
        activity: {name: activity, type: discord.ActivityTypes.PLAYING}
    });
}

export function sendError(msg: discord.Message, err: any) {
    let timestamp: Date = new Date();
    let timestampString = "\n`===TIMESTAMP="+timestamp.getTime().toString().trim() + "===`\n";
    console.log(timestampString + err);
    msg.channel.send("Unexpected error" + timestampString).catch(() => {});
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
start();
client.connect(keys.dev);
