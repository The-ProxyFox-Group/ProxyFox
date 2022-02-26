import * as discord from "discord.js";
import { sendError } from ".";
import { Member } from "./memberClass";
import { exists, load, save } from "./saveLoad";
import { serverRoles } from "./serverRole";
import { System } from "./systemClass";
import { webhookManager } from "./webhookManager";

let webhooks: webhookManager = new webhookManager();

export function sendMessageAsWebhook(msg: discord.Message, member: Member, system: System) {
    let name = member.getName(system.tag,msg.guildId);
    let url = member.getAvatar(msg.guildId) == null ? system.avatar : member.getAvatar(msg.guildId);
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
                        if (user != null && user != undefined && user.id == channel.client.user.id)
                            return sendAsHook(hookArr[i],msg,url,name,member);
                    }
                    channel.createWebhook("ProxyFox webhook").then(a => {
                        sendMessageAsWebhook(msg,member,system);
                    }).catch(err => {
                        sendError(msg,err);
                    });
                });
            else sendAsHook(webhooks.get(channel.id),msg,url,name,member);
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
                        if (user != null && user != undefined && user.id == channel.client.user.id)
                            return sendAsHook(hookArr[i],msg,url,name,member,null,channel.id);
                    }
                    baseChannel.createWebhook("ProxyFox webhook").then(a => {
                        sendMessageAsWebhook(msg,member,system);
                    }).catch(err => {
                        sendError(msg,err);
                    });
                });
            else sendAsHook(webhooks.get(channel.id),msg,url,name,member,null,channel.id);
        }
    }
}

export function webhook(msg: discord.Message) {
    if (!serverRoles.hasRole(msg.member,msg.guildId)) return;
    if (msg.channel instanceof discord.DMChannel) return;
    if (exists(msg.author.id.toString(),msg)) {
        let system = load(msg.author.id.toString());
        let serverProxy = system.serverProxy.get(msg.guildId)
        if (serverProxy === false) return;
        let member = system.memberFromMessage(msg.content);
        if (member != null) {
            if (member.serverProxy.get(msg.guildId) === false) return;
            system.auto = member.id;
            msg.content = member.getProxy(msg.content).trimMessage(msg.content);
            save(msg.author.id.toString(),system);
            sendMessageAsWebhook(msg,member,system);
            return;
        }
        member = system.memberFromAP();
        if (msg.content.startsWith("\\")) return;
        if (member == null) return;
        if (member.serverProxy.get(msg.guildId) === false) return;
        sendMessageAsWebhook(msg,member,system);
    }
}

function sendAsHook(hook: discord.Webhook, msg: discord.Message, url: string, name: string, member: Member, embed?:discord.MessageEmbed, thread?: string) {
    if (!webhooks.has(msg.channel.id))
        webhooks.put(msg.channel.id, hook);
    if (msg.stickers.map(a=>a).length > 0) return msg.channel.send("ProxyFox is unable to proxy this message, as it contains a sticker.");
    if (msg.content.length > 2000) return msg.channel.send("ProxyFox is unable to proxy this message, as it's content exceeds 2,000 characters.");
    if (msg.content.length == 0) msg.content = null;
    if (msg.reference != null) {
        msg.fetchReference().then(m => {
            let embed = new discord.MessageEmbed();
            embed.setAuthor(m.author.username + " ↩️",m.author.avatarURL());
            embed.setDescription("**[Reply to:](<"+m.url+">)** "+ (m.content.length > 100? m.content.substr(0,97)+"...": m.content) + (m.attachments.hasAny()? "📎": ""));
            msg.reference = null;
            sendAsHook(hook,msg,url,name,member,embed,thread);
        });
        return;
    }

    for (let i of  msg.attachments.map(a=>a))
        if (i.size >= 8388608) return msg.channel.send("ProxyFox is unable to proxy this message, as one or more of the files attached is greater than 8mb.");
    
    hook.send({
        avatarURL:url,
        username:name,
        //@ts-ignore
        content: msg.content,
        files: msg.attachments.map(a=>a.url),
        threadId: thread,
        embeds: embed? [embed]: undefined
    }).then(a => {
        let mess = <discord.Message> a;
        const filter = (reaction) => '❌❗❓'.indexOf(reaction.emoji.name) != -1;
        const messageFilter = (message) => /^pf[>;:!]/i.test(message.content) && message.reference && message.reference.messageId == mess.id;

        mess.channel.createMessageCollector({filter: messageFilter})
        .on("collect", message => {
            let authorId = message.author.id;
            let str = message.content.substr(3);
            let command = str.substring(0,str.indexOf(" "));
            let content = str.substring(str.indexOf(" "));
            if (!str.includes(" ")) command = str;
            switch (command) {
                case "edit": 
                    if (authorId == msg.author.id) {
                        message.delete().catch(e => {});
                        hook.editMessage(mess,content).catch(e => {});
                    }
                    return;
                case "delete":
                    if (authorId == msg.author.id) {
                        message.delete().catch(e => {});;
                        mess.delete().catch(e => {});
                    }
                    return;
                case "ping":
                    message.delete();
                    let embed = new discord.MessageEmbed();
                    embed.setDescription("**[Jump to message]("+mess.url+")**");
                    mess.channel.send({
                        content: "Psst! **" + member.getName("") + "**(<@"+msg.author.id+">)\nYou have been pinged by <@"+authorId+">!",
                        embeds: [embed]
                    });
                    return;
            }
        })

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
        setTimeout(() => {msg.delete();}, 100);
    }).catch(err => {
        /*if (err.toString().indexOf("Request entity too large"))
            return msg.channel.send("File too large to proxy.")*/
        sendError(msg,err);
    });
}