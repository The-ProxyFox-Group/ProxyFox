import * as discord from "discord.js";
import { exists, load, save } from "./saveLoad";
import { System } from "./systemClass";

export function accessMember(msg: discord.Message, parsedMessage: string[]):string {
    if (parsedMessage.length == 1)
        return "Please specify a member command or a member name.";
    
    let user: discord.User = msg.author;
    let memberName: string = parsedMessage[1];

    return "Recieved member name `" + memberName +"`";
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
        return "member doesn't exist in your systen.";
    }
    return "System doesn't exist. Please create one with `pf>system new`";
}

function deleteMem(system: System, name:string, user: discord.User, msg:discord.Message) {
    if (system.memberFromName(name) != null) {
        msg.channel.send("Are you sure you want to delete is member? Reply with the member name again to delete.").then(a => {
            //@ts-ignore
            let c = (<discord.TextChannel>(a.channel)).createMessageCollector(a => a.content == memberName && a.author.id == user.id,{time:30000}).on("collect", b => {
                c.stop();
                system.removeMember(name);
                save(user.id,system);
                msg.channel.send("Member deleted.");
            });

        })
        return;
    }
}