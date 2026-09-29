/*
 * Zhihu++ - Free & Ad-Free Zhihu client for all platforms.
 * Copyright (C) 2024-2026, zly2006 <i@zly2006.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation (version 3 only).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.zly2006.zhihu.viewmodel.filter

import com.github.zly2006.zhihu.platform.SettingsStore

const val IP_LOCATION_FILTER_ENABLED_PREFERENCE_KEY = "enableIpLocationFilter"
const val IP_LOCATION_FILTER_POSTS_PREFERENCE_KEY = "ipLocationFilterPosts"
const val IP_LOCATION_FILTER_COMMENTS_PREFERENCE_KEY = "ipLocationFilterComments"
const val IP_LOCATION_WHITELIST_PREFERENCE_KEY = "ipLocationWhitelist"

/**
 * IP 属地白名单过滤配置。
 *
 * 白名单采用“包含匹配”：内容的 IP 属地只要包含任意一条白名单条目就保留。
 * 没有 IP 属地信息的内容始终保留，避免接口缺字段时误伤；白名单为空时该规则不产生过滤。
 */
data class IpLocationFilterSettings(
    val enabled: Boolean = false,
    val filterPosts: Boolean = true,
    val filterComments: Boolean = true,
    val whitelist: Set<String> = emptySet(),
) {
    val isActiveForPosts: Boolean
        get() = enabled && filterPosts

    val isActiveForComments: Boolean
        get() = enabled && filterComments

    /** 白名单中是否至少有一条可用条目，供设置页提示用户。 */
    val hasWhitelistEntries: Boolean
        get() = whitelist.any { it.isNotBlank() }

    fun allows(ipLocation: String?): Boolean {
        val location = ipLocation?.trim().orEmpty()
        if (location.isEmpty()) return true
        val entries = whitelist.filter { it.isNotBlank() }
        if (entries.isEmpty()) return true
        return entries.any { location.contains(it.trim()) }
    }
}

fun SettingsStore.toIpLocationFilterSettings(): IpLocationFilterSettings = IpLocationFilterSettings(
    enabled = getBoolean(IP_LOCATION_FILTER_ENABLED_PREFERENCE_KEY, false),
    filterPosts = getBoolean(IP_LOCATION_FILTER_POSTS_PREFERENCE_KEY, true),
    filterComments = getBoolean(IP_LOCATION_FILTER_COMMENTS_PREFERENCE_KEY, true),
    whitelist = getStringSet(IP_LOCATION_WHITELIST_PREFERENCE_KEY, emptySet()),
)
