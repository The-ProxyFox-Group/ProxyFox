import * as discord from "discord.js";
import { sendError } from ".";
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
    if (exists(user.id)) {
        let system = load(user.id.toString());
        if (system.memberFromName(memberName) != null) {
            if (isEmpty(parsedMessage[2])) {
                let member: Member = system.memberFromName(memberName);
                let attach: discord.MessageEmbed = new discord.MessageEmbed();
                if (!isEmpty(member.avatar)) attach.setThumbnail(member.avatar);
                else if (!isEmpty(system.avatar)) attach.setThumbnail(system.avatar);
                attach.setTitle(member.name + " ("+system.name+")" + " [`"+member.id+"`]");
                if (!isEmpty(member.displayname)) attach.addField("Display Name",member.displayname,true);
                attach.addField("Message Count",member.messageCount,true);
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
                if (!isEmpty(member.description)) attach.addField("Description", member.description, false);
                if (!isEmpty(member.birthday)) attach.addField("Birthday", member.birthday, true);
                if (!isEmpty(member.pronouns)) attach.addField("Pronouns",member.pronouns,true);
                
                if (!isEmpty(member.created)) {
                    let time:number = Date.parse(member.created);
                    attach.setFooter("Created on " + new Date(time).toUTCString());
                }
                msg.channel.send(attach);
                return;
            }
            if (["delete","remove"].indexOf(parsedMessage[2].toLowerCase()) != -1) {
                deleteMem(system,memberName,user,msg);
                return;
            }
            let member: Member = system.memberFromName(memberName);
            parsedMessage.shift();
            parsedMessage.shift();
            if (["displayname","nickname","nick","dn"].indexOf(parsedMessage[0].toLowerCase()) != -1) {
                parsedMessage.shift();
                let name: string = parsedMessage.join(" ");
                member.displayname = name;
                save(user.id.toString(),system);
                return "Member's name changed to `"+name+"`";
            }
            if (parsedMessage[0].toLowerCase() == "pronouns") {
                parsedMessage.shift();
                let pronouns: string = parsedMessage.join(" ");
                member.pronouns = pronouns;
                save(user.id.toString(),system);
                return "Member's pronouns changed to `"+pronouns+"`";
            }
            if (parsedMessage[0].toLowerCase() == "birthday") {
                parsedMessage.shift();
                let birthday: string = parsedMessage.join(" ");
                member.birthday = birthday;
                save(user.id.toString(),system);
                return "Member's birthday changed to `"+birthday+"`";
            }
            if (parsedMessage[0].toLowerCase() == "color") {
                parsedMessage.shift();
                let color: string = parsedMessage.join(" ");
                member.color = color;
                save(user.id.toString(),system);
                return "Member's color changed to `"+color+"`";
            }
            if (parsedMessage[0].toLowerCase() == "avatar") {
                parsedMessage.shift();
                let avatar:string;
                if (parsedMessage.length == 0 && (msg.attachments != null && msg.attachments != undefined))
                    avatar = msg.attachments.array()[0].url;
                else if (parsedMessage.length >= 1)
                    avatar = parsedMessage[0];
                else return "Invalid avatar given.";
                member.avatar = avatar;
                save(user.id.toString(),system);
                return "Member's avatar changed to `"+avatar+"`";
            }
            if (parsedMessage[0].toLowerCase() == "proxy") {
                parsedMessage.shift();
                if (parsedMessage[0].toLowerCase() == "add") {
                    parsedMessage.shift();
                    if (member.addProxy(parsedMessage.join(" "))) {
                        save(user.id.toString(),system);
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
                    save(user.id.toString(),system);
                    return "Proxy removed.";
                }
                if (member.addProxy(parsedMessage.join(" "))) {
                    save(user.id.toString(),system);
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

    if (exists(user.id.toString())) {
        let system = load(user.id.toString());
        let member = system.addMember(memberName);
        if (member == null)
            return "Member with the same name already exists. Please provide a different name.";
        save(user.id.toString(),system);
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

    if (exists(user.id.toString())) {
        let system = load(user.id.toString());
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
            let c = (<discord.TextChannel>(a.channel)).createMessageCollector(a => a.content == name && a.author.id == user.id,{time:30000}).on("collect", b => {
                c.stop();
                system.removeMember(name);
                save(user.id,system);
                msg.channel.send("Member deleted.");
            })
        }).catch((err) => {
            sendError(msg,err);
        });
    }
}