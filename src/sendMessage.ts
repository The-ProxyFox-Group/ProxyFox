import * as discord from "discord.js";
import { send } from "process";
import { client, sendError } from ".";
import { Member } from "./memberClass";
import { exists, load, save } from "./saveLoad";
import { System } from "./systemClass";

export function sendMessageAsWebhook(msg: discord.Message, member: Member, system: System) {
    let name = member.getName(system.tag);
    let url = member.avatar == null ? system.avatar : member.avatar;
    if (msg.channel.isText) {
        let channel = <discord.TextChannel>msg.channel;
        if (!channel.isThread())
            channel.fetchWebhooks().then(hooks => {
                let hookArr = hooks.map(a=>a);
                for (let i in hookArr) {
                    let user: Object | discord.User = hookArr[i].owner;

                    //@ts-ignore
                    if (user != null && user != undefined && user.id == client.user.id) {
                        hookArr[i].edit({
                            name: "ProxyFox proxy",
                            avatar: ""
                        }).then(hook => {
                            let attach = msg.attachments.map(a=>a);
                            let newMsg = hook.send({
                                avatarURL:url,
                                username:name,
                                //@ts-ignore
                                content: msg.content,
                                //@ts-ignore
                                files: attach
                            });
                            newMsg.then(a => {
                                msg.delete();
                            }).catch(err => {
                                sendError(msg,err);
                            });
                        }).catch(err => {
                            sendError(msg,err);
                        });;
                        return;
                    }
                }
                channel.createWebhook("ProxyFox webhook").then(a => {
                    sendMessageAsWebhook(msg,member,system);
                }).catch(err => {
                    sendError(msg,err);
                });
            });
        else {
            let channel = <discord.ThreadChannel> msg.channel;
            let baseChannel = <discord.TextChannel> channel.parent;
            baseChannel.fetchWebhooks().then(hooks => {
                let hookArr = hooks.map(a=>a);
                for (let i in hookArr) {
                    let user: Object | discord.User = hookArr[i].owner;

                    //@ts-ignore
                    if (user != null && user != undefined && user.id == client.user.id) {
                        hookArr[i].edit({
                            name: "ProxyFox proxy",
                            avatar: ""
                        }).then(hook => {
                            let attach = msg.attachments.map(a=>a);
                            let newMsg = hook.send({
                                avatarURL:url,
                                username:name,
                                //@ts-ignore
                                content: msg.content,
                                //@ts-ignore
                                files: attach,
                                threadId: channel.id
                            });
                            newMsg.then(a => {
                                msg.delete();
                            }).catch(err => {
                                sendError(msg,err);
                            });
                        }).catch(err => {
                            sendError(msg,err);
                        });
                        return;
                    }
                }
                baseChannel.createWebhook("ProxyFox webhook").then(a => {
                    sendMessageAsWebhook(msg,member,system);
                }).catch(err => {
                    sendError(msg,err);
                });;
            });
        }
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
        if (msg.content.startsWith("\\")) return;
        if (member != null)
            sendMessageAsWebhook(msg,member,system);
    }
}