export class ProxyTag {
    prefix: string;
    suffix: string;

    constructor(prefix:string, suffix:string) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    toJson(): object {
        let json = {
            prefix: this.prefix ?? undefined,
            suffix: this.suffix ?? undefined
        }
        return json;
    }

    toProxyString():string {
        let str:string = "";
        if (this.containsPrefix()) str += this.prefix;
        str += "text";
        if (this.containsSuffix()) str += this.suffix;
        return str;
    }

    containsSuffix(): boolean {
        return !(this.suffix == "" || this.suffix == null || this.suffix == undefined);
    }

    containsPrefix(): boolean {
        return !(this.prefix == "" || this.prefix == null || this.prefix == undefined);
    }

    containsProxy(message:string):boolean {
        if (!this.containsPrefix() && !this.containsSuffix()) return false;
        let hasPrefix = false;
        let hasSuffix = false;

        if (this.containsPrefix()) {
            if (message.startsWith(this.prefix)) hasPrefix = true;
        } else hasPrefix = true;

        if (this.containsSuffix()) {
            if (message.endsWith(this.suffix)) hasSuffix = true;
        } else hasSuffix = true;

        return hasPrefix && hasSuffix;
    }

    containsProxyTag(prefix:string, suffix:string):boolean {
        if (!this.containsPrefix() && !this.containsSuffix()) return false;
        let hasPrefix = false;
        let hasSuffix = false;

        if (this.containsPrefix()) {
            if (prefix == this.prefix) hasPrefix = true;
        } else hasPrefix = true;

        if (this.containsSuffix()) {
            if (suffix == this.suffix) hasSuffix = true;
        } else hasSuffix = true;

        return hasPrefix && hasSuffix;
    }

    trimMessage(message:string):string {
        if (this.containsPrefix())
            message = message.substr(this.prefix.length);
        if (this.containsSuffix())
            message = message.substr(this.suffix.length);
        return message;
    }

    static getArr(proxies:ProxyTag[]): any {
        let out = [];
        for (let i in proxies)
            out.push(proxies[i].toJson());
        return out;
    }

    static fromArr(obj:any):ProxyTag[] {
        let out: ProxyTag[] = [];
        for (let i = 0; i < obj.length; i++)
            out.push(ProxyTag.from(obj[i]));
        return out;
    }

    static from(obj:any):ProxyTag {
        return new ProxyTag(obj.prefix,obj.suffix);
    }
}