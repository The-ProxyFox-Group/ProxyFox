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
        return `{"id":"${this.id}","name":"${this.name.replace(/"/g,"\\\"")}","display_name":"${this.displayname?this.displayname.replace(/"/g,"\\\""):null}","description":"${this.description? this.description.replace(/"/g,"\\\""):null}","birthday":"${this.birthday?this.birthday.replace(/"/g,"\\\""):null}","pronouns":"${this.pronouns?this.pronouns.replace(/"/g,"\\\""):null}","color":"${this.color?this.color.replace(/"/g,"\\\""):null}","avatar_url":"${this.avatar?this.avatar.replace(/"/g,"\\\""):null}","proxy_tags":${ProxyTag.getArrString(this.proxies)},"keep_proxy":false,"message_count":${this.messageCount},"created":"${this.created.replace(/"/g,"\\\"")}"}`
    }

    addProxy(str:string):boolean {
        if (str.indexOf("text") == -1) return false;
        let arr: string[] = str.split(/text/);
        this.proxies.push(new ProxyTag(arr[0],arr[1]));
        return true;
    }

    remProxy(str:string):number {
        if (str.indexOf("text") == -1) return 0;
        let arr: string[] = str.split(/text/);
        for (let i in this.proxies) {
            let proxy: ProxyTag = this.proxies[i];
            if (proxy.containsProxyTag(arr[0], arr[1])) {
                this.proxies[i] = null;
                this.proxies = this.proxies.filter(pro => pro != null);
                return 2;
            }
        }
        return 1;
    }

    static getArrString(members:Member[]):string {
        let out = "[";
        for (let i in members) {
            out += members[i].toString();
            out += ",";
        }
        out = out.substr(0,out.length-1) + "]";
        if (out.length == 1) out = "[]";
        return out;
    }

    static fromArr(obj:any):Member[] {
        let out: Member[] = [];
        for (let i = 0; i < obj.length; i++)
            out.push(Member.from(obj[i]));
        return out;
    }

    static from(obj:any):Member {
        let out = new Member(obj.name);
        out.id = obj.id;
        out.displayname = obj.display_name;
        out.description = obj.description;
        out.birthday = obj.birthday;
        out.pronouns = obj.pronouns;
        out.color = obj.color;
        out.proxies = ProxyTag.fromArr(obj.proxy_tags);
        out.messageCount = obj.message_count;
        out.created = obj.created;
        out.avatar = obj.avatar_url;
        return out;
    }

    getName(tag: string) {
        return (this.displayname == null? this.name: this.displayname) + " " + (tag != undefined || tag != null? tag: "");
    }

    setName(newname:string) {
        this.name = newname;
    }

    setDisplayName(newname:string) {
        this.displayname = newname;
    }

    containsProxy(message:string):boolean {
        for (let i in this.proxies)
            if (this.proxies[i].containsProxy(message)) return true;
        return false;
    }

    getProxy(message:string):ProxyTag {
        for (let i in this.proxies)
            if (this.proxies[i].containsProxy(message)) return this.proxies[i];
        return null;
    }
}