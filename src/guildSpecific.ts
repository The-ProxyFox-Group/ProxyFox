interface Specific {
    [id:string]:any;
}

export class GuildSpecific {
    data: Specific = {};
    
    put(id:string,value:any) {
        if (!this.data) this.data = {};
        this.data[id] = value;
    }
    get(id:string):any {
        if (!this.data) this.data = {};
        return this.data[id];
    }
    toString():string {
        return JSON.stringify(this.toJson());
    }
    toJson():object {
        return this.data;
    }
    static fromString(data:string):GuildSpecific {
        let json: object = JSON.parse(data);
        return this.fromJson(json);
    }
    static fromJson(json:object):GuildSpecific {
        let guildSpecific: GuildSpecific = new GuildSpecific();
        guildSpecific.data = <Specific>json;
        return guildSpecific;
    }
}