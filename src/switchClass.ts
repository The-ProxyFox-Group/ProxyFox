import { Member } from "./memberClass";
import { System } from "./systemClass";
import * as discord from "discord.js";

export class Switch {
    memberIds: string[];
    timeStamp: string;
    id: string;

    constructor(members: Member[]) {
        this.memberIds = [];
        for (let i = 0; i < members.length; i++) {
            let member = members[i];
            this.memberIds.push(member.id);
        }
        this.timeStamp = new Date().toISOString();
        this.id = this.createId();
    }

    public equals(other): boolean {
        if (!(other instanceof Switch)) return false;
        if (other.memberIds.length != this.memberIds.length) return false;
        other.memberIds = other.memberIds.sort((a,b) => a > b? 1: a < b? -1: 0);
        this.memberIds = this.memberIds.sort((a,b) => a > b? 1: a < b? -1: 0);
        for (let i = 0; i < this.memberIds.length; i++) {
            let memA = this.memberIds[i];
            let memB = other.memberIds[i];
            if (memA == memB) return true;
        }
        return false;
    } 

    public addEmbedField(system:System, name:string, embed: discord.MessageEmbed) {
        let memStr = "";
        for (let i = 0; i < this.memberIds.length; i++) {
            let str = this.memberIds[i];
            let member: Member = system.memberFromName(str);
            memStr += member.name + " [`" + str + "`]\n";
        }
        embed.addField(name,memStr);
    }

    private createId():string {
        let out: string = "";
        var characters: string = 'abcdefghijklmnopqrstuvwxyz';
        for ( var i = 0; i < 5; i++ )
            out += characters.charAt(Math.floor(Math.random() * 26));
        return out;
    }

    public toJson():object {
        let json = {
            id: this.id,
            timestamp: this.timeStamp,
            members: this.memberIds
        }
        return json;
    }

    public static fromJson(json):Switch {
        let sw: Switch = new Switch([]);
        sw.timeStamp = json.timestamp;
        sw.memberIds = json.members;
        sw.id = json.id;
        return sw;
    }

    public static bulkToJson(switches: Switch[]): object[] {
        let obj = [];
        for (let i = 0; i < switches.length; i++) {
            let sw: Switch = switches[i];
            obj.push(sw.toJson());
        }
        return obj;
    }
    
    public static bulkFromJson(json: object[]): Switch[] {
        let switches: Switch[] = [];

        for (let i = 0; i < json.length; i++) {
            let sw = json[i];
            switches.push(this.fromJson(sw));
        }

        return switches.sort((a,b) => {
            return a.timeStamp > b.timeStamp? 1: a.timeStamp < b.timeStamp? -1: 0;
        });
    }
}