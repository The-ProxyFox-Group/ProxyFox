import { ProxyTag } from "./proxyClass";
import { GuildSpecific } from "./guildSpecific";
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
    serverAvatar: GuildSpecific = new GuildSpecific();
    serverNick: GuildSpecific = new GuildSpecific();
    serverProxy: GuildSpecific = new GuildSpecific();

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
    
    toJson():object {
        let json = {
            id: this.id,
            name: this.name,
            display_name: this.displayname,
            description: this.description,
            birthday: this.birthday,
            pronouns: this.pronouns,
            color: this.color,
            avatar_url: this.avatar,
            proxy_tags: ProxyTag.getArr(this.proxies),
            keep_proxy: false,
            message_count: this.messageCount,
            created: this.created,
            server_avatar: this.serverAvatar.toJson(),
            server_nick: this.serverNick.toJson(),
            server_proxy: this.serverProxy? this.serverProxy.toJson(): undefined
        };
        return json;
    }

    toJsonExport():object {
        let json = {
            id: this.id ?? undefined,
            name: this.name ?? undefined,
            display_name: this.displayname ?? undefined,
            description: this.description ?? undefined,
            birthday: this.birthday ?? undefined,
            pronouns: this.pronouns ?? undefined,
            color: this.color ?? undefined,
            avatar_url: this.avatar ?? undefined,
            proxy_tags: ProxyTag.getArr(this.proxies) ?? undefined,
            keep_proxy: false,
            message_count: this.messageCount ?? undefined,
            created: this.created ?? undefined,
        };
        return json;
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

    static getArr(members:Member[]):any {
        let out = []
        for (let i in members)
            out.push(members[i].toJson());
        return out;
    }
    
    static getArrExport(members:Member[]):any {
        let out = []
        for (let i in members)
            out.push(members[i].toJsonExport());
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
        out.serverAvatar = GuildSpecific.fromJson(obj.server_avatar);
        out.serverNick = GuildSpecific.fromJson(obj.server_nick);
        out.serverProxy = GuildSpecific.fromJson(obj.server_proxy);
        if (!out.serverProxy.data) out.serverProxy.data = {}
        return out;
    }
    getDisplayName(id?:string): string {
        if (this.serverNick.get(id)) return this.serverNick.get(id);
        return this.displayname;
    }

    getName(tag: string, id?: string) {
        return (this.getDisplayName(id) == null? this.name: this.getDisplayName(id)) + " " + (tag != undefined || tag != null? tag: "");
    }

    setName(newname:string) {
        this.name = newname;
    }

    setDisplayName(newname:string) {
        this.displayname = newname;
    }

    getAvatar(id:string) {
        if (this.serverAvatar.get(id))
            return this.serverAvatar.get(id);
        else return this.avatar;
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