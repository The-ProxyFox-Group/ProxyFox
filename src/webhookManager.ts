import { ReactionUserManager, Webhook } from "discord.js";

export interface webhookStorage {
    [key: string]: Webhook;
}

export class webhookManager {
    webhooks: webhookStorage = {};
    public constructor() {

    }
    public put(key: string, value: Webhook) {
        this.webhooks[key] = value;
    }
    public has(key: string): boolean {
        return !!this.webhooks[key];
    }
    public get(key: string): Webhook {
        if (!this.has(key)) return null;
        return this.webhooks[key];
    }
    public remove(key: string) {
        if (!this.has(key)) return;
        this.webhooks[key] = undefined;
    }
}