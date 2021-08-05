export class ProxyTag {
    prefix: string;
    suffix: string;

    constructor(prefix:string, suffix:string) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    toString() {
        return `{"prefix":"${this.prefix}","suffix":"${this.suffix}"}`;
    }

    static getArrString(proxies:ProxyTag[]):string {
        let out = "[";
        for (let i in proxies) {
            out += proxies[i].toString();
            out += ",";
        }
        out += "]";
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