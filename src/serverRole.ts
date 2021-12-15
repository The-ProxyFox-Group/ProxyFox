import * as fs from "https://deno.land/std/fs/mod.ts";
import * as discord from "https://code.harmony.rocks/main";
interface RoleStorage {
    [key:string]:string;
}
export class ServerRoles {
    roleStorage: RoleStorage = {};

    constructor() {
        if (fs.existsSync("./roles.json"))
            this.roleStorage = <RoleStorage>JSON.parse(Deno.readTextFileSync("./roles.json").toString());
    }
    save() {
        Deno.writeTextFileSync("./roles.json",JSON.stringify(this.roleStorage));
    }

    setRole(msg: discord.Message, parsedMessage: string[]) {
        //@ts-ignore
        if (!msg.member.permissions.has(discord.PermissionFlags.MANAGE_ROLES,true)) return "Insufficient permissions to run command.";
        let role = parsedMessage[1];
        if (["reset","remove","delete"].indexOf(role.toLocaleLowerCase()) != -1) {
            //@ts-ignore
            this.roleStorage[msg.guildID] = undefined;
            this.save();
            return "Proxy role reset.";
        }
        let id: string;
        if (role.startsWith("<@&") && role.endsWith(">"))
            id = role.substring(3,role.length-1);
        if (parseInt(role))
            id = role;
        //@ts-ignore
        if (!id) return "Invalid role given.";
        //@ts-ignore
        this.roleStorage[msg.guildID] = id;
        this.save();
        return "Proxy role updated <@&"+id+">!";
    }

    async hasRole(user:discord.Member,server:string): Promise<boolean> {
        return new Promise((res,rej) => {
            if (!this.roleStorage) {
                this.roleStorage = {};
                if (fs.existsSync("./roles.json"))
                    this.roleStorage = <RoleStorage>JSON.parse(Deno.readTextFileSync("./roles.json").toString());
                return res(true);
            }
            if (!this.roleStorage[server]) return res(true);
            let m = user.roles.get(this.roleStorage[server]);
            m.then(r => {
                res(!!r);
            })
        });
    }
}
export const serverRoles = new ServerRoles();

export function setRole(msg: discord.Message, parsedMessage: string[]) {
    return serverRoles.setRole(msg,parsedMessage);
}