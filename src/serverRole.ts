import * as fs from "fs";
import * as discord from "discord.js";
interface RoleStorage {
    [key:string]:string;
}
export class ServerRoles {
    roleStorage: RoleStorage = {};

    constructor() {
        if (fs.existsSync("./roles.json"))
            this.roleStorage = <RoleStorage>JSON.parse(fs.readFileSync("./roles.json").toString());
    }
    save() {
        fs.writeFileSync("./roles.json",JSON.stringify(this.roleStorage));
    }

    setRole(msg: discord.Message, parsedMessage: string[]) {
        if (!msg.member.permissions.has(discord.Permissions.FLAGS.MANAGE_ROLES,true)) return "Insufficient permissions to run command.";
        let role = parsedMessage[1];
        if (["reset","remove","delete"].indexOf(role.toLocaleLowerCase()) != -1) {
            this.roleStorage[msg.guildId] = undefined;
            this.save();
            return "Proxy role reset.";
        }
        let id: string;
        if (role.startsWith("<@&") && role.endsWith(">"))
            id = role.substring(3,role.length-1);
        if (parseInt(role))
            id = role;
        
        if (!id) return "Invalid role given.";
        this.roleStorage[msg.guildId] = id;
        this.save();
        return "Proxy role updated <@&"+id+">!";
    }

    hasRole(user:discord.GuildMember,server:string) {
        if (!this.roleStorage[server]) return true;
        return !!user.roles.cache.find(r=>r.id == this.roleStorage[server]);
    }
}
export const serverRoles = new ServerRoles();

export function setRole(msg: discord.Message, parsedMessage: string[]) {
    return serverRoles.setRole(msg,parsedMessage);
}