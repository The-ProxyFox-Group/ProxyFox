import * as discord from "https://code.harmony.rocks/main";
import { client, sendError } from "./index.ts";
import { Member } from "./memberClass.ts";
import { exists, load, save } from "./saveLoad.ts";
import { serverRoles } from "./serverRole.ts";
import { System } from "./systemClass.ts";
import { webhookManager } from "./webhookManager.ts";

const Constants = {
    DISCORD_API_URL: 'https://discord.com/api',
    DISCORD_GATEWAY_URL: 'wss://gateway.discord.gg',
    DISCORD_CDN_URL: 'https://cdn.discordapp.com',
    DISCORD_API_VERSION: 9,
    DISCORD_VOICE_VERSION: 4
};

async function fetchWebhooks(channel: discord.Channel): Promise<discord.Webhook[]> {
    const raw = (await client.rest.get(
        `${Constants.DISCORD_API_URL}/v${Constants.DISCORD_API_VERSION}/channels/${channel.id}/webhooks`
    )) as discord.WebhookPayload[];
    return raw.map((e) => {
        let hook = new discord.Webhook(e,client);
        //@ts-ignore
        hook.fromPayload(e);
        return hook;
    });
}
async function createWebhook(channel: discord.Channel,name: string): Promise<discord.Webhook> {
    const raw = (await client.rest.post(
        `${Constants.DISCORD_API_URL}/v${Constants.DISCORD_API_VERSION}/channels/${channel.id}/webhooks`,
        {name}
    )) as discord.WebhookPayload;
    return new discord.Webhook(raw,client);
}

let webhooks: webhookManager = new webhookManager();

export function sendMessageAsWebhook(msg: discord.Message, member: Member, system: System) {
    //@ts-ignore
    let name = member.getName(system.tag,msg.guildID);
    //@ts-ignore
    let url = member.getAvatar(msg.guildID) == null ? system.avatar : member.getAvatar(msg.guildId);
    if (msg.channel.isText()) {
        let channel = <discord.TextChannel>msg.channel;
        let time = new Date().getTime();
        if (!channel.isThread())
            if (!webhooks.has(channel.id)) {
                fetchWebhooks(channel).then(hooks => {
                    let time2 = new Date().getTime();
                    if (time2 > time + 30000)
                        return;
                    console.log("No webhook storage made for " + channel.id + ". Generating one.");
                    for (let i in hooks) {
                        //@ts-ignore
                        let user: discord.User = hooks[i].user;

                        //@ts-ignore
                        if (user != null && user != undefined && user.id == client.user.id)
                            return sendAsHook(hooks[i],msg,url,name,member);
                    }
                    createWebhook(channel,"ProxyFox webhook").then(a => {
                        sendMessageAsWebhook(msg,member,system);
                    }).catch(err => {
                        sendError(msg,err);
                    });
                });
            }
            else sendAsHook(webhooks.get(channel.id),msg,url,name,member);
        else {
            let channel = <discord.ThreadChannel> msg.channel;
            //@ts-ignore
            channel.guild.channels.resolve(channel.parentID).then(parent => {
                if (!parent) return;
                if (!webhooks.has(channel.id)) {
                    fetchWebhooks(parent).then(hooks => {
                        let time2 = new Date().getTime();
                        if (time2 > time + 30000)
                            return;
                        console.log("No webhook storage made for " + channel.id + ". Generating one.");
                        for (let i in hooks) {
                            //@ts-ignore
                            let user: Object | discord.User = hooks[i].user;
                            
                            //@ts-ignore
                            if (user != null && user != undefined && user.id == client.user.id)
                                //@ts-ignore
                                return sendAsHook(hooks[i],msg,url,name,member,null,channel.id);
                        }
                        createWebhook(channel,"ProxyFox webhook").then(a => {
                            sendMessageAsWebhook(msg,member,system);
                        }).catch(err => {
                            sendError(msg,err);
                        });
                    });
                }
                //@ts-ignore
                else sendAsHook(webhooks.get(channel.id),msg,url,name,member,null,channel.id);
            });
        }
    }
}

export function webhook(msg: discord.Message) {
    //@ts-ignore
    serverRoles.hasRole(msg.member,msg.guild).then((b) => {
        if (!b) return;
        if (msg.channel instanceof discord.DMChannel) return;
        if (exists(msg.author.id.toString(),msg)) {
            let system = load(msg.author.id.toString());
            //@ts-ignore
            let serverProxy = system.serverProxy.get(msg.guildID)
            if (serverProxy === false) return;
            let member = system.memberFromMessage(msg.content);
            if (member != null) {
                system.auto = member.id;
                //@ts-ignore
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
    })
}

function sendAsHook(hook: discord.Webhook, msg: discord.Message, url: string, name: string, member: Member, embed?:discord.Embed, thread?: string) {
    if (!webhooks.has(msg.channel.id))
        webhooks.put(msg.channel.id, hook);
    let attach = msg.attachments.map(a=>a);
    if (msg.messageReference) {
        let ref = msg.messageReference;
        //@ts-ignore
        msg.channel.messages.resolve(ref.message_id)
            .then((m) => {
                if (!m) {
                    //@ts-ignore
                    msg.reference = undefined;
                    //@ts-ignore
                    sendAsHook(hook,msg,url,name,member,null,thread);
                    return;
                }
                let embed = new discord.Embed();
                embed.setAuthor(m.author.username + " ↩️",m.author.avatar);
                embed.setDescription("**[Reply to:](<"+m.author.id+">)** "+ (m.content.length > 100? m.content.substr(0,97)+"...": m.content) + (m.attachments.length > 0? "📎": ""));
                //@ts-ignore
                msg.reference = undefined;
                sendAsHook(hook,msg,url,name,member,embed,thread);
            });
        return;
    }
    let embeds = undefined;
    if (embed != null)
        embeds = [embed];
    send(hook,msg.content,{
        avatar:url,
        name:name,
        //@ts-ignore
        file: attach,
        thread: thread,
        embeds
    }).then(mess => {
        member.messageCount++;
        //const filter = (reaction) => '❌❗❓'.indexOf(reaction.emoji.name) != -1;
        /*mess.createReactionCollector({
            filter
        }).on("collect", (react, user) => {
            switch (react.emoji.name) {
                case "❌":
                    if (user.id == msg.author.id)
                        mess.delete();
                    return;
                case "❗":
                    let embed = new discord.Embed();
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
        })*/
        setTimeout(() => {msg.delete();}, 100);
    }).catch(err => {
        sendError(msg,err);
    });
}

async function send(hook: discord.Webhook,text?: string, option?: any): Promise<discord.Message> {
    if (typeof text === 'object') {
        option = text
        text = undefined
    }

    if (text === undefined && option === undefined) {
        throw new Error('Either text or option is necessary.')
    }

    if (option instanceof discord.Embed)
        option = {
            embeds: [option]
        }

    const payload = {
        content: text,
        embeds:
        option?.embed !== undefined
            ? [option.embed]
            : option?.embeds !== undefined
            ? option.embeds
            : undefined,
        file: option?.file,
        tts: option?.tts,
        allowed_mentions: option?.allowedMentions,
        username: undefined as undefined | string,
        avatar_url: undefined as undefined | string
    }

    if (option?.name !== undefined) {
        payload.username = option?.name
    }

    if (option?.avatar !== undefined) {
        payload.avatar_url = option?.avatar
    }

    if (
        payload.embeds !== undefined &&
        payload.embeds instanceof Array &&
        payload.embeds.length > 10
    )
    throw new Error(`Cannot send more than 10 embeds through Webhook`)
    let threadString = option?.thread? "&thread_id="+option?.thread:"";
    const resp = await client.rest.post(hook.url + '?wait=true'+threadString, payload)

    const res = new discord.Message(
        client,
        resp,
        hook as unknown as discord.TextChannel,
        hook as unknown as discord.User
    )
    await res.mentions.fromPayload(resp)
    return res
}