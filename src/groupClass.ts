import { group } from "console";

export class Group {
    id: string = "";
    name: string = "";
    members: string[] = [];
    description: string = "";
    created: string = "";

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

    public toJson():Object {
        let out = {
            id: this.id,
            name: this.name,
            members: this.members,
            description: this.description,
            created: this.created
        }
        return out;
    }

    public static fromJson(obj: any): Group {
        let out = new Group(obj.name);
        out.id = obj.id,
        out.members = obj.members;
        out.description = obj.description;
        out.created = obj.created;
        return out;
    }

    public static bulkToJson(groups: Group[]): Object[] {
        let out = [];
        for (let i = 0; i < groups.length; i++) {
            out.push(groups[i].toJson());
        }
        return out;
    }

    public static bulkFromJson(groups: any[]): Group[] {
        let out = [];
        for (let i = 0; i < groups.length; i++) {
            out.push(Group.fromJson(groups[i]));
        }
        return out;
    }
}