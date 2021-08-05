import { Member } from "./memberClass";

export class System {
    id: string;
    name: string;
    description: string;
    tag: string;
    avatar: string;
    timezone: string;
    members: Member[] = [];
    created: string;

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
        return `{"version":1,"id":"${this.id}","name":"${this.name}","description":"${this.description}","tag":"${this.tag}","avatar_url":"${this.avatar}","timezone":"${this.timezone}","members":${Member.getArrString(this.members)},"created":"${this.created}"}`.replace(/\"undefined\"/g,"null").replace(/\"\"/g,"null").replace(/\"null\"/g,"null");
    }

    static fromStr(str:string):System {
        let json = JSON.parse(str);
        let system = new System(json.name);
        system.description = json.description;
        system.tag = json.tag;
        system.avatar = json.avatar_url;
        system.timezone = json.timezone;
        system.members = Member.fromArr(json.members);
        system.created = json.created;
        return system;
    }

}