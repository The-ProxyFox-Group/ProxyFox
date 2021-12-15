import { Group } from "./groupClass";
import { GuildSpecific } from "./guildSpecific";
import { Member } from "./memberClass";
import { Switch } from "./switchClass";

export class System {
    id: string = null;
    name: string = null;
    description: string = null;
    tag: string = null;
    avatar: string = null;
    timezone: string = null;
    members: Member[] = [];
    created: string = null;
    auto: string = null;
    autobool: boolean = false;
    serverProxy: GuildSpecific = new GuildSpecific();
    switches: Switch[] = [];
    groups: Group[] = [];
    subsystems: Group[] = [];

    constructor(name:string) {
        this.name = name;
        this.id = this.createId();
    }

    private createId():string {
        let out: string = "";
        var characters: string = 'abcdefghijklmnopqrstuvwxyz';
        for ( var i = 0; i < 5; i++ )
            out += characters.charAt(Math.floor(Math.random() * 26));
        return out;
    }

    toString():string {
        let json = {
            version: 1,
            id: this.id,
            name: this.name,
            description: this.description,
            tag: this.tag,
            avatar_url: this.avatar,
            timezone: this.timezone,
            members: Member.getArr(this.members),
            created: this.created,
            auto: this.auto,
            auto_bool: this.autobool,
            server_proxy: this.serverProxy.toJson(),
            switches: Switch.bulkToJson(this.switches),
            groups: Group.bulkToJson(this.groups),
            subsystems: Group.bulkToJson(this.subsystems),
        };
        return JSON.stringify(json);
    }

    toExportString():string {
        let json = {
            version: 1,
            id: this.id,
            name: this.name,
            description: this.description,
            tag: this.tag,
            avatar_url: this.avatar,
            timezone: this.timezone,
            members: Member.getArrExport(this.members),
            switches: [],
            created: this.created,
        };
        return JSON.stringify(json);
    }

    memberFromMessage(message: string): Member {
        for (let i in this.members)
            if (this.members[i].containsProxy(message)) return this.members[i];
        return null;
    }

    memberFromAP():Member {
        if (!this.autobool) return null;
        for (let i in this.members)
            if (this.members[i].id == this.auto) return this.members[i];
        return null;
    }

    memberFromName(message: string): Member {
        for (let i in this.members)
            if (this.members[i].name.toLowerCase() == message.toLowerCase() || this.members[i].id == message.toLowerCase()) return this.members[i];
        return null;
    }
    
    addMember(name:string):Member {
        if (this.memberFromName(name) != null) return null;
        let member = new Member(name);
        this.members.push(member);
        return member;
    }

    removeMember(name:string) {
        for (let i in this.members)
            if (this.members[i].name.toLowerCase() == name.toLowerCase()) {
                this.members[i] = null;
                this.members = this.members.filter(mem => mem != null);
                return;
            }
    }

    static fromStr(str:string):System {
        let json = JSON.parse(str);
        let system = new System(json.name);
        system.id = json.id;
        system.description = json.description;
        system.tag = json.tag;
        system.avatar = json.avatar_url;
        system.timezone = json.timezone;
        system.members = Member.fromArr(json.members);
        system.created = json.created;
        system.auto = json.auto;
        system.autobool = json.auto_bool;
        system.serverProxy = GuildSpecific.fromJson(json.server_proxy);
        system.switches = Switch.bulkFromJson(json.switches);
        system.groups = Group.bulkFromJson(json.groups);
        system.subsystems = Group.bulkFromJson(json.subsystems);
        return system;
    }
}