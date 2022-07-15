import { ReactionUserManager, Webhook } from "discord.js";

export interface webhookStorage {
    [key: string]: Webhook;
}

export class webhookManager {
    webhooks: webhookStorage = {};
    public constructor() {

    }
    public async put(key: string, value: Webhook) {
        this.webhooks[key] = value;
        let i = 0
        for (let webhook in this.webhooks) {
            i++;
            if (i > 50) 
        }
    }
    public async has(key: string): Promise<boolean> {
        if (!this.webhooks[key]) return false;
        try {
            await this.webhooks[key].edit({});
        } catch (e) {
            this.remove(key)
            return false;
        }
        return true;
    }
    public async get(key: string): Promise<Webhook> {
        if (!await this.has(key)) return null;
        return this.webhooks[key];
    }
    public async remove(key: string) {
        if (!await this.has(key)) return;
        this.webhooks[key] = undefined;
    }
}