import {accessMember, createMember, deleteMember} from "./memberCommands";
import {accessSystem, createSystem, deleteSystem, exportSystem, importSystem, listSystem} from "./systemCommands";

let memberTree = {
    "default": accessMember,
    "new": createMember,
    "add": createMember,
    "create": createMember,
    "delete": deleteMember,
    "remove": deleteMember
};

let systemTree = {
    "default": accessSystem,
    "new": createSystem,
    "add": createSystem,
    "create": createSystem,
    "delete": deleteSystem,
    "remove": deleteSystem,
    "list": listSystem,
}

export const tree = {
    "default": "Unknown command.",
    "member": memberTree,
    "m": memberTree,
    "system": systemTree,
    "s": systemTree,
    "list": listSystem,
    "import": importSystem,
    "export": exportSystem,
};
