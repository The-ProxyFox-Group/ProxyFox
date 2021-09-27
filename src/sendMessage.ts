import * as discord from "discord.js";
import { client, sendError } from ".";
import { Member } from "./memberClass";
import { exists, load, save } from "./saveLoad";
import { System } from "./systemClass";
import { webhookManager } from "./webhookManager";

let webhooks: webhookManager = new webhookManager();

export function sendMessageAsWebhook(msg: discord.Message, member: Member, system: System) {
    let name = member.getName(system.tag);
    let url = member.avatar == null ? system.avatar : member.avatar;
    if (msg.channel.isText) {
        let channel = <discord.TextChannel>msg.channel;
        let time = new Date().getTime();
        if (!channel.isThread())
            if (!webhooks.has(channel.id))
                channel.fetchWebhooks().then(hooks => {
                    let time2 = new Date().getTime();
                    if (time2 > time + 30000)
                        return;
                    console.log("No webhook storage made for " + channel.id + ". Generating one.");
                    let hookArr = hooks.map(a=>a);
                    for (let i in hookArr) {
                        let user: Object | discord.User = hookArr[i].owner;

                        //@ts-ignore
                        if (user != null && user != undefined && user.id == client.user.id)
                            return sendAsHook(hookArr[i],msg,url,name,member);
                    }
                    channel.createWebhook("ProxyFox webhook").then(a => {
                        sendMessageAsWebhook(msg,member,system);
                    }).catch(err => {
                        sendError(msg,err);
                    });
                });
            else sendAsHook(webhooks[channel.id],msg,url,name,member);
        else {
            let channel = <discord.ThreadChannel> msg.channel;
            let baseChannel = <discord.TextChannel> channel.parent;
            if (!webhooks.has(channel.id))
                baseChannel.fetchWebhooks().then(hooks => {
                    let time2 = new Date().getTime();
                    if (time2 > time + 30000)
                        return;
                    console.log("No webhook storage made for " + channel.id + ". Generating one.");
                    let hookArr = hooks.map(a=>a);
                    for (let i in hookArr) {
                        let user: Object | discord.User = hookArr[i].owner;
                        //@ts-ignore
                        if (user != null && user != undefined && user.id == client.user.id)
                            return sendAsHook(hookArr[i],msg,url,name,member,null,channel.id);
                    }
                    baseChannel.createWebhook("ProxyFox webhook").then(a => {
                        sendMessageAsWebhook(msg,member,system);
                    }).catch(err => {
                        sendError(msg,err);
                    });
                });
            else sendAsHook(webhooks[channel.id],msg,url,name,member,null,channel.id);
        }
    }
}

export function webhook(msg: discord.Message) {
    if (msg.channel instanceof discord.DMChannel) return;
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

function sendAsHook(hook: discord.Webhook, msg: discord.Message, url: string, name: string, member: Member, embed?:discord.MessageEmbed, thread?: string) {
    webhooks.put(msg.channel.id, hook);
    if (msg.content.length == 0) msg.content = null;
    let attach = msg.attachments.map(a=>a);
    if (msg.reference != null) {
        msg.fetchReference().then(m => {
            let embed = new discord.MessageEmbed();
            embed.setAuthor(m.author.username + " ↩️",m.author.avatarURL());
            embed.setDescription("[Reply to:](<"+m.url+">) "+ (m.content.length > 100? m.content.substr(0,97)+"...": m.content));
            msg.reference = null;
            sendAsHook(hook,msg,url,name,member,embed,thread);
        });
        return;
    }
    let embeds = null;
    if (embed != null)
        embeds = [embed];
    hook.send({
        avatarURL:url,
        username:name,
        //@ts-ignore
        content: msg.content,
        //@ts-ignore
        files: attach,
        threadId: thread,
        embeds
    }).then(a => {
        member.messageCount++;
        const filter = (reaction) => '❌❗❓'.indexOf(reaction.emoji.name) != -1;
        let mess = <discord.Message> a;
        mess.createReactionCollector({
            filter
        }).on("collect", (react, user) => {
            switch (react.emoji.name) {
                case "❌":
                    if (user.id == msg.author.id)
                        mess.delete();
                    return;
                case "❗":
                    let embed = new discord.MessageEmbed();
                    embed.setDescription("**[Jump to message]("+mess.url+")**");
                    mess.channel.send({
                        content: "Psst! **" + member.getName("") + "**(<@"+msg.author.id+">)\nYou have been pinged by <@"+user.id+">!",
                        embeds: [embed]
                    });
                    react.remove();
                    break;
                case "❓":
                    user.createDM().then(a => {
                        user.send("Proxy owner: `"+msg.author.username+"#"+msg.author.discriminator+"`, `"+msg.author.id+"`");
                    });
                    react.remove();
                    break;
            }
        })
        msg.delete();
    }).catch(err => {
        if (err.toString().indexOf("Request entity too large"))
            return msg.channel.send("File too large to proxy.")
        sendError(msg,err);
    });
}