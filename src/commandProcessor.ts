const prefix: RegExp = /^pf[>;:!]/i;

export function parse(msg: string): string[] {
    let out: string[] = [];
    
    if (prefix.test(msg)) {
        let newMsg:string = msg.substr(3);
        let currStr:string = "";
        for (let i = 0; i < newMsg.length; i++) {
            let char:string = newMsg[i];
            switch (char) {
                case "\\":
                    currStr += newMsg[++i];
                    break;
                case '"':
                    out.push(currStr);
                    currStr = "";
                    for (let j = ++i; j < newMsg.length; j++) {
                        if (newMsg[j] == '"') {
                            i = j;
                            break;
                        }
                        currStr += newMsg[j];
                    }
                    break;
                case "'":
                    out.push(currStr);
                    currStr = "";
                    for (let j = ++i; j < newMsg.length; j++) {
                        if (newMsg[j] == "'") {
                            i = j;
                            break;
                        }
                        currStr += newMsg[j];
                    }
                    break;
                case ' ':
                    out.push(currStr);
                    currStr = "";
                    break;
                default:
                    currStr += char;
            }
        }
        out.push(currStr);
    }

    out = out.filter(str => str != "");

    return out;
}

export function parseIdx(msg: string, idx: number): string {
    let out: string[] = [];
    
    if (prefix.test(msg)) {
        let newMsg:string = msg.substr(3);
        let currStr:string = "";
        for (let i = 0; i < newMsg.length; i++) {
            let char:string = newMsg[i];
            switch (char) {
                case "\\":
                    currStr += newMsg[++i];
                    break;
                case '"':
                    out.push(currStr);
                    out = out.filter(str => str != "");
                    if (out.length == idx)
                        return newMsg.substring(i).trim();
                    currStr = "";
                    for (let j = ++i; j < newMsg.length; j++) {
                        if (newMsg[j] == '"') {
                            i = j;
                            break;
                        }
                        currStr += newMsg[j];
                    }
                    break;
                case "'":
                    out.push(currStr);
                    out = out.filter(str => str != "");
                    if (out.length == idx)
                        return newMsg.substring(i).trim();
                    currStr = "";
                    for (let j = ++i; j < newMsg.length; j++) {
                        if (newMsg[j] == "'") {
                            i = j;
                            break;
                        }
                        currStr += newMsg[j];
                    }
                    break;
                case ' ':
                    out.push(currStr);
                    out = out.filter(str => str != "");
                    if (out.length == idx)
                        return newMsg.substring(i).trim();
                    currStr = "";
                    break;
                default:
                    currStr += char;
            }
        }
    }

    return "";
}