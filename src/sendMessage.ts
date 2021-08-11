import * as discord from "discord.js";
import { send } from "process";
import { client } from ".";
import { Member } from "./memberClass";
import { exists, load, save } from "./saveLoad";
import { System } from "./systemClass";

export function sendMessageAsWebhook(msg: discord.Message, member: Member, system: System) {
    let name = member.getName(system.tag);
    let url = member.avatar == null ? system.avatar : member.avatar;
    if (msg.channel.isText) {
        let channel = <discord.TextChannel>msg.channel;
        channel.fetchWebhooks().then(hooks => {
            let hookArr = hooks.array();
            for (let i in hookArr) {
                let user: Object | discord.User = hookArr[i].owner;

                //@ts-ignore
                if (user != null && user != undefined && user.id == client.user.id) {
                    hookArr[i].edit({
                        name,
                        avatar: url
                    }).then(hook => {
                        let newMsg;
                        if (msg.content != null || msg.content != "" || msg.content != undefined) newMsg = hook.send(msg.content);
                        let attach = msg.attachments.array();
                        for (let i = 0; i < attach.length; i++) {
                            newMsg = hook.send(attach[i]);
                        }
                        newMsg.then(a => {
                            msg.delete();
                        });
                    });
                    return;
                }
            }
            channel.createWebhook("ProxyFox webhook").then(a => {
                sendMessageAsWebhook(msg,member,system);
            });
        })
    }
}

export function webhook(msg: discord.Message) {
    if (exists(msg.author.id.toString())) {
        let system = load(msg.author.id.toString());
        let member = system.memberFromMessage(msg.content);
        if (member != null) {
            system.auto = member.id;
            msg.content = member.getProxy(msg.content).trimMessage(msg.content);
            save(msg.author.id.toString(),system);
            sendMessageAsWebhook(msg,member,system);
            return;
        }
        member = system.memberFromAP();
        if (member != null)
            sendMessageAsWebhook(msg,member,system);
    }
}