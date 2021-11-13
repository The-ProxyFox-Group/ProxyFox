interface Specific {
    [id:string]:any;
}

export class GuildSpecific {
    data: Specific = {};
    
    put(id:string,value:any) {
        this.data[id] = value;
    }
    get(id:string):any {
        return this.data[id]
    }
    toString():string {
        return JSON.stringify(this.toJson());
    }
    toJson():object {
        let json: object = {};
        for (let k in this.data)
            json[k] = this.data[k];
        return json;
    }
    static fromString(data:string):GuildSpecific {
        let json: object = JSON.parse(data);
        return this.fromJson(json);
    }
    static fromJson(json:object):GuildSpecific {
        let guildSpecific: GuildSpecific = new GuildSpecific();
        for (let k in json) 
            guildSpecific.put(k,json[k]);
        return guildSpecific;
    }
}