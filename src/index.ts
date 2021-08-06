import { parse as parseMessage } from "./commandProcessor";
import { tree } from "./commandTree";
import * as discord from "discord.js";
import * as fs from "fs";
import { Member } from "./memberClass";
import { System } from "./systemClass";
import { getData } from "./systemCommands";
import { webhook } from "./sendMessage";
export const client = new discord.Client();

const keys = JSON.parse(fs.readFileSync("./key.json").toString());

function handleMessage(msg: discord.Message): boolean {
    let parsedMsg = parseMessage(msg.content);
    if (parsedMsg.length > 0) {
        let currTree = tree;
        for (let i = 0; i <= parsedMsg.length; i++) {
            //@ts-ignore
            if (currTree[parsedMsg[i]] == null) currTree = currTree.default;
            else currTree = currTree[parsedMsg[i]];
            
            let breakCond: boolean = false;

            switch (typeof currTree) {
                case "string":
                    msg.channel.send(currTree);
                    breakCond = true;
                    break;
                case "function":
                    //@ts-ignore
                    let output: string = currTree(msg,parsedMsg);
                    if (output != undefined && output != null && output != "")
                        msg.channel.send(output);
                    breakCond = true;
                    break;
                case "object":
                    break;
            }

            if (breakCond) break;
        }
        return true;
    }
    return false;
}

client.on('message', msg => {
    if (msg.author.system || msg.author.bot) return;
    try {
        if (!handleMessage(msg)) {
            webhook(msg);
        }
    } catch (err) {
        let timestamp: Date = new Date();
        let timestampString = "\n`===TIMESTAMP="+timestamp.getTime().toString().trim() + "===`\n";
        console.log(timestampString + err);
        msg.channel.send("Unexpected error" + timestampString);
    }
});

client.login(keys.dev);