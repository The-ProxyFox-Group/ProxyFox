import * as fs from "fs";
import { System } from "./systemClass";

export function load(id:string):System {
    return System.fromStr(fs.readFileSync("./systems/"+id+".json").toString());
}

export function save(id:string, system:System) {
    fs.writeFileSync("./systems/"+id+".json",system.toString());
}