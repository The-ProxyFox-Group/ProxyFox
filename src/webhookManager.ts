import { Webhook } from "discord.js";

export interface webhookStorage {
    [key: string]: Webhook;
}