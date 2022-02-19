import * as discord from "discord.js";
import { sendError } from ".";
import { parseIdx } from "./commandProcessor";
import { Member } from "./memberClass";
import { ProxyTag } from "./proxyClass";
import { exists, load, save } from "./saveLoad";
import { System } from "./systemClass";

function isEmpty(string:string):boolean {
    return string == "" || string == undefined || string == null;
}

function isArrEmpty(array: Array<any>):boolean {
    return array == null || array == undefined || array == [] || array.length == 0;
}

export function accessMember(msg: discord.Message, parsedMessage: string[]):string {
    if (parsedMessage.length == 1)
        return "Please specify a member command or a member name.";
    
    let user: discord.User = msg.author;
    let memberName: string = parsedMessage[1];
    if (exists(user.id,msg)) {
        let system = load(user.id);
        if (system.memberFromName(memberName) != null) {
            if (isEmpty(parsedMessage[2])) {
                let member: Member = system.memberFromName(memberName);
                let attach: discord.MessageEmbed = new discord.MessageEmbed();
                let color = member.color;
                //@ts-ignore
                if (color) attach.setColor(color);
                if (!isEmpty(member.avatar)) attach.setThumbnail(member.avatar);
                else if (!isEmpty(system.avatar)) attach.setThumbnail(system.avatar);
                attach.setTitle(member.name + " ("+system.name+")" + " [`"+member.id+"`]");
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
                if (!isEmpty(member.description)) {
                    if (member.description.length < 1000) attach.addField("Description", member.description, false);
                }
                if (!isEmpty(member.birthday)) attach.addField("Birthday", member.birthday, true);
                if (!isEmpty(member.pronouns)) attach.addField("Pronouns",member.pronouns,true);
                
                if (!isEmpty(member.created)) {
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
            let member: Member = system.memberFromName(memberName);
            parsedMessage.shift();
            parsedMessage.shift();
            let third = parseIdx(msg.content,3).trim();
            if (parsedMessage[0].toLowerCase() == "name") {
                if (third.length == 0) return "Make sure to provide a name"
                member.name = third;
                save(user.id,system);
                return "Member's name changed to `"+third+"`";
            }
            if (["displayname","nickname","nick","dn"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                member.displayname = third;
                save(user.id,system);
                return "Member's display name changed to `"+third+"`";
            }
            if (["serverdisplayname","servernickname","servernick","guilddisplayname","guildnickname","guildnick"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                if (["-reset","-clear","-remove"].indexOf(third) != -1) {
                    member.serverNick.put(msg.guildId,null);
                    save(user.id,system);
                    return "Member's server name reset";
                }
                member.serverNick.put(msg.guildId,third);
                save(user.id,system);
                return "Member's server name changed to `"+third+"`";
            }
            if (["serverproxy","sp"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                if (parsedMessage[0] == "off") {
                    member.serverProxy.put(msg.guildId,false);
                    save(user.id,system);
                    return "Member's proxy disabled for this server";
                }
                if (parsedMessage[0] == "on") {
                    member.serverProxy.put(msg.guildId,true);
                    save(user.id,system);
                    return "Member's proxy enabled for this server";
                }
                return "Please provide whether you want the server proxy on or off";
            }
            if (["serveravatar","guildavatar","serverpfp","guildpfp"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                let avatar:string;
                if (!third && !!msg.attachments)
                    try {
                        avatar = msg.attachments.map(a=>a)[0].url;
                    } catch(e) {
                        return member.getAvatar(msg.guildId);
                    }
                else if (third)
                    avatar = third
                else return member.getAvatar(msg.guildId);
                if (["reset","remove","delete"].indexOf(avatar.toLowerCase()) != -1) {
                    member.serverAvatar.put(msg.guildId,null);
                    save(user.id,system);
                    return "Member's server avatar reset!";
                }
                member.serverAvatar.put(msg.guildId,avatar);
                save(user.id,system);
                return "Member's server avatar changed to `"+avatar+"`";
            }
            if (parsedMessage[0].toLowerCase() == "pronouns") {
                let pronouns: string = third
                member.pronouns = pronouns;
                save(user.id,system);
                return "Member's pronouns changed to `"+pronouns+"`";
            }
            if (parsedMessage[0].toLowerCase() == "description") {
                let desc: string = third
                if (desc.length > 1000) return "Member description must be shorter than 1,000 characters.";
                member.description = desc;
                save(user.id,system);
                return "Member's description changed to `"+desc+"`";
            }
            if (parsedMessage[0].toLowerCase() == "birthday") {
                let birthday: string = third
                member.birthday = birthday;
                save(user.id,system);
                return "Member's birthday changed to `"+birthday+"`";
            }
            if (parsedMessage[0].toLowerCase() == "color") {
                let color: string = third
                member.color = color;
                save(user.id,system);
                return "Member's color changed to `"+color+"`";
            }
            if (["name","rename"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                let name: string = third
                member.name = name;
                save(user.id,system);
                return "Member's name changed to `"+name+"`";
            }
            if (parsedMessage[0].toLowerCase() == "avatar") {
                let avatar:string;
                if (!third && !!msg.attachments)
                    try {
                        avatar = msg.attachments.map(a=>a)[0].url;
                    } catch(e) {
                        return member.avatar;
                    }
                else if (third)
                    avatar = third
                else return member.avatar;
                member.avatar = avatar;
                save(user.id,system);
                return "Member's avatar changed to `"+avatar+"`";
            }
            if (parsedMessage[0].toLowerCase() == "proxy") {
                let fourth = parseIdx(msg.content,4);
                parsedMessage.shift();
                if (parsedMessage[0].toLowerCase() == "add") {
                    if (member.addProxy(fourth)) {
                        save(user.id,system);
                        return "Proxy `"+fourth+"` added!";
                    }
                    return "Invalid proxy, make sure to include `text` in it somewhere!";
                }
                if (["remove","delete"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                    let num: number = member.remProxy(third);
                    if (num == 0)
                        return "Invalid proxy, make sure to include `text` in it somewhere!";
                    if (num == 1)
                        return "Proxy doesn't exist";
                    save(user.id,system);
                    return "Proxy removed.";
                }
                if (member.addProxy(third)) {
                    save(user.id,system);
                    return "Proxy `"+third+"` added!";
                }
                return "Invalid proxy, make sure to include `text` in it somewhere!";
            }
            return "Please specify a member command, or put nothing to show the member's card";
        }
        return "Member doesn't exist in your system.";
    }

    return "System doesn't exist. Please create one with `pf>system new`";
}

export function createMember(msg: discord.Message, parsedMessage: string[]):string {
    if (parsedMessage.length == 2)
        return "Please specify a member name.";
    
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

export function deleteMember(msg: discord.Message, parsedMessage: string[]):string {
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
        return "Member doesn't exist in your system.";
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