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

import com.github.zly2006.zhihu.platform.MapSettingsStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IpLocationFilterTest {
    @Test
    fun readsIpLocationFilterSettingsFromSettingsStore() {
        val settings = MapSettingsStore(
            mutableMapOf(
                IP_LOCATION_FILTER_ENABLED_PREFERENCE_KEY to true,
                IP_LOCATION_FILTER_POSTS_PREFERENCE_KEY to false,
                IP_LOCATION_FILTER_COMMENTS_PREFERENCE_KEY to true,
                IP_LOCATION_WHITELIST_PREFERENCE_KEY to setOf("上海", "北京"),
            ),
        ).toIpLocationFilterSettings()

        assertTrue(settings.enabled)
        assertFalse(settings.filterPosts)
        assertTrue(settings.filterComments)
        assertEquals(setOf("上海", "北京"), settings.whitelist)
        assertFalse(settings.isActiveForPosts)
        assertTrue(settings.isActiveForComments)
        assertTrue(settings.hasWhitelistEntries)
    }

    @Test
    fun keepsContentWithoutIpLocationEvenWhenWhitelistDoesNotContainIt() {
        val settings = IpLocationFilterSettings(enabled = true, whitelist = setOf("上海"))

        assertTrue(settings.allows(null))
        assertTrue(settings.allows(""))
        assertTrue(settings.allows("   "))
        assertFalse(settings.allows("吉林"))
    }

    @Test
    fun matchesWhitelistEntriesByContainment() {
        val settings = IpLocationFilterSettings(enabled = true, whitelist = setOf("上海"))

        assertTrue(settings.allows("上海"))
        assertTrue(settings.allows("上海市浦东新区"))
        assertTrue(settings.allows(" 上海 "))
    }

    @Test
    fun emptyWhitelistFiltersNothing() {
        val settings = IpLocationFilterSettings(enabled = true, whitelist = emptySet())
        val blankOnly = IpLocationFilterSettings(enabled = true, whitelist = setOf("   "))

        assertFalse(settings.hasWhitelistEntries)
        assertTrue(settings.allows("吉林"))
        assertTrue(blankOnly.allows("吉林"))
    }

    @Test
    fun masterSwitchAndPerTypeSwitchesGateActivation() {
        val disabled = IpLocationFilterSettings(enabled = false, whitelist = setOf("上海"))
        assertFalse(disabled.isActiveForPosts)
        assertFalse(disabled.isActiveForComments)

        val postsOnly = IpLocationFilterSettings(
            enabled = true,
            filterPosts = true,
            filterComments = false,
            whitelist = setOf("上海"),
        )
        assertTrue(postsOnly.isActiveForPosts)
        assertFalse(postsOnly.isActiveForComments)
    }
}
