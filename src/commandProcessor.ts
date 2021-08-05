const prefix: RegExp = /^pf[>;:!]/i;

export function parse(msg: string): string[] {
    let out: string[] = [];
    
    if (prefix.test(msg)) {
        let newMsg:string = msg.substr(3);
        let currStr:string = "";
        for (let i = 0; i < newMsg.length; i++) {
            let char:string = newMsg[i];
            switch (char) {
                case '"':
                case "'":
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

    out = out.filter(function(str) {
        return str != "";
    });

    return out;
}