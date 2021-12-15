import * as discord from "https://code.harmony.rocks/main";
import { sendError } from "./index.ts";
import { Member } from "./memberClass.ts";
import { ProxyTag } from "./proxyClass.ts";
import { exists, load, save } from "./saveLoad.ts";
import { System } from "./systemClass.ts";

function isEmpty(string:string):boolean {
    return string == "" || string == undefined || string == null;
}

function isArrEmpty(array: Array<any>):boolean {
    return array == null || array == undefined || array == [] || array.length == 0;
}

export function accessMember(msg: discord.Message, parsedMessage: string[]):string|void {
    if (parsedMessage.length == 1)
        return "Please specify a member command or a member name.";
    
    let user: discord.User = msg.author;
    let memberName: string = parsedMessage[1];
    if (exists(user.id,msg)) {
        let system: System = load(user.id);
        if (system.memberFromName(memberName) != null) {
            if (isEmpty(parsedMessage[2])) {
                //@ts-ignore
                let member: Member = system.memberFromName(memberName);
                let attach: discord.Embed = new discord.Embed();
                //@ts-ignore
                if (!isEmpty(member.avatar)) attach.setThumbnail(member.avatar);
                //@ts-ignore
                else if (!isEmpty(system.avatar)) attach.setThumbnail(system.avatar);
                attach.setTitle(member.name + " ("+system.name+")" + " [`"+member.id+"`]");
                //@ts-ignore
                if (!isEmpty(member.displayname)) attach.addField("Display Name",member.displayname,true);
                if (!member.messageCount) member.messageCount = 0;
                attach.addField("Message Count",member.messageCount.toString(),true);
                if (!isArrEmpty(member.proxies)) {
                    let str: string = "";
                    for (let i in member.proxies) {
                        let proxy: ProxyTag = member.proxies[i];
                        str += "`";
                        if (proxy.containsPrefix()) str += proxy.prefix;
                        str += "text";
                        if (proxy.containsSuffix()) str += proxy.suffix;
                        str += "`\n";
                    }
                    attach.addField("Proxy Tags", str, true);
                }
                //@ts-ignore
                if (!isEmpty(member.description)) attach.addField("Description", member.description, false);
                //@ts-ignore
                if (!isEmpty(member.birthday)) attach.addField("Birthday", member.birthday, true);
                //@ts-ignore
                if (!isEmpty(member.pronouns)) attach.addField("Pronouns",member.pronouns,true);
                
                //@ts-ignore
                if (!isEmpty(member.created)) {
                    //@ts-ignore
                    let time:number = Date.parse(member.created);
                    attach.setFooter("Created on " + new Date(time).toUTCString());
                }
                msg.channel.send({
                    //@ts-ignore
                    embeds: [attach]
                }).catch(err => {
                    sendError(msg,err);
                })
                return;
            }
            if (["delete","remove"].indexOf(parsedMessage[2].toLowerCase()) != -1) {
                deleteMem(system,memberName,user,msg);
                return;
            }
            //@ts-ignore
            let member: Member = system.memberFromName(memberName);
            parsedMessage.shift();
            parsedMessage.shift();
            if (["displayname","nickname","nick","dn"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                let name: string = parsedMessage.join(" ");
                member.displayname = name;
                save(user.id,system);
                return "Member's name changed to `"+name+"`";
            }
            if (["serverdisplayname","servernickname","servernick","guilddisplayname","guildnickname","guildnick"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                let name: string = parsedMessage.join(" ");
                if (["-reset","-clear","-remove"].indexOf(name) != -1) {
                    //@ts-ignore
                    member.serverNick.put(msg.guildID,null);
                    save(user.id,system);
                    return "Member's server name reset";
                }
                //@ts-ignore
                member.serverNick.put(msg.guildID,name);
                save(user.id,system);
                return "Member's server name changed to `"+name+"`";
            }
            if (["serveravatar","guildavatar","serverpfp","guildpfp"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                let avatar:string;
                if (parsedMessage.length == 0 && !!msg.attachments)
                    try {
                        avatar = msg.attachments.map(a=>a)[0].url;
                    } catch(e) {
                        //@ts-ignore
                        return member.getAvatar(msg.guildId);
                    }
                else if (parsedMessage.length >= 1)
                    avatar = parsedMessage[0];
                //@ts-ignore
                else return member.getAvatar(msg.guildId);
                if (["reset","remove","delete"].indexOf(avatar.toLowerCase()) != -1) {
                    //@ts-ignore
                    member.serverAvatar.put(msg.guildId,null);
                    save(user.id,system);
                    return "Member's server avatar reset!";
                }
                //@ts-ignore
                member.serverAvatar.put(msg.guildId,avatar);
                save(user.id,system);
                return "Member's server avatar changed to `"+avatar+"`";
            }
            if (parsedMessage[0].toLowerCase() == "pronouns") {
                parsedMessage.shift();
                let pronouns: string = parsedMessage.join(" ");
                member.pronouns = pronouns;
                save(user.id,system);
                return "Member's pronouns changed to `"+pronouns+"`";
            }
            if (parsedMessage[0].toLowerCase() == "description") {
                parsedMessage.shift();
                let desc: string = parsedMessage.join(" ");
                member.description = desc;
                save(user.id,system);
                return "Member's description changed to `"+desc+"`";
            }
            if (parsedMessage[0].toLowerCase() == "birthday") {
                parsedMessage.shift();
                let birthday: string = parsedMessage.join(" ");
                member.birthday = birthday;
                save(user.id,system);
                return "Member's birthday changed to `"+birthday+"`";
            }
            if (parsedMessage[0].toLowerCase() == "color") {
                parsedMessage.shift();
                let color: string = parsedMessage.join(" ");
                member.color = color;
                save(user.id,system);
                return "Member's color changed to `"+color+"`";
            }
            if (["name","rename"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                let name: string = parsedMessage.join(" ");
                member.name = name;
                save(user.id,system);
                return "Member's name changed to `"+name+"`";
            }
            if (parsedMessage[0].toLowerCase() == "avatar") {
                parsedMessage.shift();
                let avatar:string;
                if (parsedMessage.length == 0 && !!msg.attachments)
                    try {
                        avatar = msg.attachments.map(a=>a)[0].url;
                    } catch(e) {
                        //@ts-ignore
                        return member.avatar;
                    }
                else if (parsedMessage.length >= 1)
                    avatar = parsedMessage[0];
                //@ts-ignore
                else return member.avatar;
                member.avatar = avatar;
                save(user.id,system);
                return "Member's avatar changed to `"+avatar+"`";
            }
            if (parsedMessage[0].toLowerCase() == "proxy") {
                parsedMessage.shift();
                if (parsedMessage[0].toLowerCase() == "add") {
                    parsedMessage.shift();
                    if (member.addProxy(parsedMessage.join(" "))) {
                        save(user.id,system);
                        return "Proxy `"+parsedMessage.join(" ")+"` added!";
                    }
                    return "Invalid proxy, make sure to include `text` in it somewhere!";
                }
                if (["remove","delete"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                    parsedMessage.shift();
                    let num: number = member.remProxy(parsedMessage.join(" "));
                    if (num == 0)
                        return "Invalid proxy, make sure to include `text` in it somewhere!";
                    if (num == 1)
                        return "Proxy doesn't exist";
                    save(user.id,system);
                    return "Proxy removed.";
                }
                if (member.addProxy(parsedMessage.join(" "))) {
                    save(user.id,system);
                    return "Proxy `"+parsedMessage.join(" ")+"` added!";
                }
                return "Invalid proxy, make sure to include `text` in it somewhere!";
            }
            return "Please specify a member command, or put nothing to show the member's card";
        }
        return "Member doesn't exist in your systen.";
    }

    return "System doesn't exist. Please create one with `pf>system new`";
}

export function createMember(msg: discord.Message, parsedMessage: string[]):string {
    if (parsedMessage.length == 2)
        return "Please speciry a member name.";
    
    let user: discord.User = msg.author;
    parsedMessage.shift();
    parsedMessage.shift();
    let memberName: string = parsedMessage.join(" ");

    if (exists(user.id,msg)) {
        let system = load(user.id);
        let member = system.addMember(memberName);
        if (member == null)
            return "Member with the same name already exists. Please provide a different name.";
        save(user.id,system);
        return "Member '"+memberName+"' created!";
    }
    return "System doesn't exist. Please create one with `pf>system new`";
}

export function deleteMember(msg: discord.Message, parsedMessage: string[]):string|void {
    if (parsedMessage.length == 2)
        return "Please speciry a member name.";
    
    let user: discord.User = msg.author;
    parsedMessage.shift();
    parsedMessage.shift();
    let memberName: string = parsedMessage.join(" ");

    if (exists(user.id,msg)) {
        let system = load(user.id);
        if (system.memberFromName(memberName) != null) {
            deleteMem(system,memberName,user,msg);
            return;
        }
        return "Member doesn't exist in your systen.";
    }
    return "System doesn't exist. Please create one with `pf>system new`";
}

function deleteMem(system: System, name:string, user: discord.User, msg:discord.Message) {
    if (system.memberFromName(name) != null) {
        msg.channel.send("Are you sure you want to delete is member? Reply with the member name again to delete.").then(a => {
            //@ts-ignore
            let c = (<discord.TextChannel>(a.channel)).createMessageCollector(a => a.author.id == user.id,{time:30000}).on("collect", b => {
                if (b.content != name) return;
                c.stop();
                system.removeMember(name);
                save(user.id,system);
                msg.channel.send("Member deleted.").catch(err => {
                    sendError(msg,err);
                });;
            })
        }).catch((err) => {
            sendError(msg,err);
        });
    }
}