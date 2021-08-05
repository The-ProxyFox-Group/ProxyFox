import { ProxyTag } from "./proxyClass";

export class Member {
    id: string;
    name: string;
    displayname: string;
    description: string;
    birthday: string;
    pronouns: string;
    color: string;
    avatar: string;
    proxies: ProxyTag[] = [];
    messageCount: number = 0;
    created: string;

    constructor(name:string) {
        this.name = name;
        this.id = this.createId();
        this.created = new Date().toISOString();
    }

    private createId():string {
        let out: string = "";
        var characters: string = 'abcdefghijklmnopqrstuvwxyz';
        for ( var i = 0; i < 5; i++ )
            out += characters.charAt(Math.floor(Math.random() * 26));
        return out;
    }
    
    toString():string {
        return `{"id":"${this.id}","name":"${this.name}","display_name":"${this.displayname}","description":"${this.description}","birthday":"${this.birthday}","pronouns":"${this.pronouns}","color":"${this.color}","avatar_url":"${this.avatar}","proxy_tags":${ProxyTag.getArrString(this.proxies)},"keep_proxy":false,"message_count":${this.messageCount},"created":"${this.created}"}`
    }

    static getArrString(members:Member[]):string {
        let out = "[";
        for (let i in members) {
            out += members[i].toString();
            out += ",";
        }
        out += "]";
        return out;
    }

    static fromArr(obj:any):Member[] {
        let out: Member[] = [];
        for (let i = 0; i < obj.length; i++)
            out.push(Member.from(obj[i]));
        return out;
    }

    static from(obj:any):Member {
        let out = new Member("");
        out.name = obj.name;
        out.displayname = obj.display_name;
        out.description = obj.description;
        out.birthday = obj.birthday;
        out.pronouns = obj.pronouns;
        out.color = obj.color;
        out.proxies = ProxyTag.fromArr(obj.proxy_tags);
        out.messageCount = obj.message_count;
        out.created = obj.created;
        return out;
    }

    getName(tag: string) {
        return (this.displayname == null? this.name: this.displayname) + tag;
    }

    setName(newname:string) {
        this.name = newname;
    }

    setDisplayName(newname:string) {
        this.displayname = newname;
    }
}