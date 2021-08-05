import * as discord from "discord.js";

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
    let memberName: string = parsedMessage[2];

    return "Recieved member name `" + memberName +"`";
}

export function deleteMember(msg: discord.Message, parsedMessage: string[]):string {
    if (parsedMessage.length == 2)
        return "Please speciry a member name.";
    
    let user: discord.User = msg.author;
    let memberName: string = parsedMessage[2];

    return "Recieved member name `" + memberName +"`";
}