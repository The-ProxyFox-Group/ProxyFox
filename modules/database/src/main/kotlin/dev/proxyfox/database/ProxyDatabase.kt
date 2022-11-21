/*
 * Copyright (c) 2022, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.database

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.proxyfox.database.records.member.MemberProxyTagRecord
import dev.proxyfox.database.records.member.MemberRecord
import dev.proxyfox.database.records.member.MemberServerSettingsRecord
import dev.proxyfox.database.records.misc.*
import dev.proxyfox.database.records.system.SystemChannelSettingsRecord
import dev.proxyfox.database.records.system.SystemRecord
import dev.proxyfox.database.records.system.SystemServerSettingsRecord
import dev.proxyfox.database.records.system.SystemSwitchRecord
import kotlinx.datetime.Instant
import kotlin.time.Duration

// Created 2022-07-10T23:56:55

/**
 * @author Ampflower
 * @since ${version}
 **/
open class ProxyDatabase<T : Database>(protected val proxy: T) : Database() {
    override suspend fun setup(): Database {
        proxy.setup()
        return this
    }

    override suspend fun ping(): Duration {
        return proxy.ping()
    }

    override suspend fun getDatabaseName(): String {
        return proxy.getDatabaseName() + " (Proxied)"
    }

    override suspend fun fetchUser(userId: ULong): UserRecord? {
        return proxy.fetchUser(userId)
    }

    override suspend fun fetchSystemFromId(systemId: String): SystemRecord? {
        return proxy.fetchSystemFromId(systemId)
    }

    override suspend fun fetchMembersFromSystem(systemId: String): List<MemberRecord>? {
        return proxy.fetchMembersFromSystem(systemId)
    }

    override suspend fun fetchMemberFromSystem(systemId: String, memberId: String): MemberRecord? {
        return proxy.fetchMemberFromSystem(systemId, memberId)
    }

    override suspend fun fetchProxiesFromSystem(systemId: String): List<MemberProxyTagRecord>? {
        return proxy.fetchProxiesFromSystem(systemId)
    }

    override suspend fun fetchProxiesFromSystemAndMember(systemId: String, memberId: String): List<MemberProxyTagRecord>? {
        return proxy.fetchProxiesFromSystemAndMember(systemId, memberId)
    }

    override suspend fun fetchMemberServerSettingsFromSystemAndMember(serverId: ULong, systemId: String, memberId: String): MemberServerSettingsRecord? {
        return proxy.fetchMemberServerSettingsFromSystemAndMember(serverId, systemId, memberId)
    }

    override suspend fun getOrCreateServerSettingsFromSystem(serverId: ULong, systemId: String): SystemServerSettingsRecord {
        return proxy.getOrCreateServerSettingsFromSystem(serverId, systemId)
    }

    override suspend fun getOrCreateServerSettings(serverId: ULong): ServerSettingsRecord {
        return proxy.getOrCreateServerSettings(serverId)
    }

    override suspend fun updateServerSettings(serverSettings: ServerSettingsRecord) {
        proxy.updateServerSettings(serverSettings)
    }

    override suspend fun getOrCreateChannelSettingsFromSystem(channelId: ULong, systemId: String): SystemChannelSettingsRecord {
        return proxy.getOrCreateChannelSettingsFromSystem(channelId, systemId)
    }

    override suspend fun getOrCreateChannel(serverId: ULong, channelId: ULong): ChannelSettingsRecord {
        return proxy.getOrCreateChannel(serverId, channelId)
    }

    override suspend fun updateChannel(channel: ChannelSettingsRecord) {
        proxy.updateChannel(channel)
    }

    override suspend fun getOrCreateSystem(userId: ULong, id: String?): SystemRecord {
        return proxy.getOrCreateSystem(userId, id)
    }

    override suspend fun dropSystem(userId: ULong): Boolean {
        return proxy.dropSystem(userId)
    }

    override suspend fun getOrCreateMember(systemId: String, name: String, id: String?): MemberRecord? {
        return proxy.getOrCreateMember(systemId, name, id)
    }

    override suspend fun dropMember(systemId: String, memberId: String): Boolean {
        return proxy.dropMember(systemId, memberId)
    }

    override suspend fun updateMember(member: MemberRecord) {
        proxy.updateMember(member)
    }

    override suspend fun updateMemberServerSettings(serverSettings: MemberServerSettingsRecord) {
        proxy.updateMemberServerSettings(serverSettings)
    }

    override suspend fun updateSystem(system: SystemRecord) {
        proxy.updateSystem(system)
    }

    override suspend fun updateSystemServerSettings(serverSettings: SystemServerSettingsRecord) {
        proxy.updateSystemServerSettings(serverSettings)
    }

    override suspend fun updateSystemChannelSettings(channelSettings: SystemChannelSettingsRecord) {
        proxy.updateSystemChannelSettings(channelSettings)
    }

    override suspend fun updateUser(user: UserRecord) {
        proxy.updateUser(user)
    }

    override suspend fun createMessage(userId: Snowflake, oldMessageId: Snowflake, newMessageId: Snowflake, channelBehavior: ChannelBehavior, memberId: String, systemId: String, memberName: String) {
        proxy.createMessage(userId, oldMessageId, newMessageId, channelBehavior, memberId, systemId, memberName)
    }

    override suspend fun updateMessage(message: ProxiedMessageRecord) {
        proxy.updateMessage(message)
    }

    override suspend fun fetchMessage(messageId: Snowflake): ProxiedMessageRecord? {
        return proxy.fetchMessage(messageId)
    }

    override suspend fun fetchLatestMessage(systemId: String, channelId: Snowflake): ProxiedMessageRecord? {
        return proxy.fetchLatestMessage(systemId, channelId)
    }

    override suspend fun getOrCreateTokenFromSystem(systemId: String): TokenRecord {
        return proxy.getOrCreateTokenFromSystem(systemId)
    }

    override suspend fun updateToken(token: TokenRecord) {
        return proxy.updateToken(token)
    }

    override suspend fun createProxyTag(record: MemberProxyTagRecord): Boolean {
        return proxy.createProxyTag(record)
    }

    override suspend fun createSwitch(systemId: String, memberId: List<String>, timestamp: Instant?): SystemSwitchRecord? {
        return proxy.createSwitch(systemId, memberId, timestamp)
    }

    override suspend fun dropSwitch(switch: SystemSwitchRecord) {
        proxy.dropSwitch(switch)
    }

    override suspend fun updateSwitch(switch: SystemSwitchRecord) {
        proxy.updateSwitch(switch)
    }

    override suspend fun fetchSwitchesFromSystem(systemId: String): List<SystemSwitchRecord>? {
        return proxy.fetchSwitchesFromSystem(systemId)
    }

    override suspend fun dropProxyTag(proxyTag: MemberProxyTagRecord) {
        proxy.dropProxyTag(proxyTag)
    }

    override suspend fun updateTrustLevel(systemId: String, trustee: ULong, level: TrustLevel): Boolean {
        return proxy.updateTrustLevel(systemId, trustee, level)
    }

    override suspend fun fetchTrustLevel(systemId: String, trustee: ULong): TrustLevel {
        return proxy.fetchTrustLevel(systemId, trustee)
    }

    override suspend fun fetchTotalSystems(): Int? {
        return proxy.fetchTotalSystems()
    }

    override suspend fun fetchTotalMembersFromSystem(systemId: String): Int? {
        return proxy.fetchTotalMembersFromSystem(systemId)
    }

    override suspend fun fetchMemberFromSystemAndName(systemId: String, memberName: String, caseSensitive: Boolean): MemberRecord? {
        return proxy.fetchMemberFromSystemAndName(systemId, memberName)
    }

    override suspend fun export(other: Database) {
        proxy.export(other)
    }

    @Suppress("DEPRECATION_ERROR", "DeprecatedCallableAddReplaceWith")
    @Deprecated(level = DeprecationLevel.ERROR, message = "Not for regular use.")
    override suspend fun drop() {
        proxy.drop()
    }

    override suspend fun firstFreeSystemId(id: String?): String {
        return proxy.firstFreeSystemId(id)
    }

    override suspend fun firstFreeMemberId(systemId: String, id: String?): String {
        return proxy.firstFreeMemberId(systemId, id)
    }

    override fun close() {
        proxy.close()
    }
}