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

import com.github.zly2006.zhihu.data.DataHolder
import kotlin.test.Test
import kotlin.test.assertEquals

class CommentFilterTest {
    @Test
    fun removesCommentsFromBlockedUsersIncludingChildren() {
        val comments = listOf(
            comment("kept", authorId = "ok"),
            comment("parent", authorId = "ok", children = listOf(comment("child-blocked", authorId = "blocked"))),
            comment("blocked", authorId = "blocked"),
        )

        val result = comments.filterBlockedComments(
            blockedUserIds = setOf("blocked"),
            ipLocationFilter = IpLocationFilterSettings(),
        )

        assertEquals(listOf("kept", "parent"), result.map { it.id })
        assertEquals(emptyList(), result.last().childComments)
    }

    @Test
    fun removesCommentsOutsideWhitelistAndKeepsMissingLocation() {
        val comments = listOf(
            comment("shanghai", authorId = "ok", ipLocation = "上海"),
            comment("jilin", authorId = "ok", ipLocation = "吉林"),
            comment("unknown", authorId = "ok"),
            comment(
                "parent",
                authorId = "ok",
                ipLocation = "上海",
                children = listOf(comment("child-jilin", authorId = "ok", ipLocation = "吉林")),
            ),
        )

        val result = comments.filterBlockedComments(
            blockedUserIds = emptySet(),
            ipLocationFilter = IpLocationFilterSettings(enabled = true, whitelist = setOf("上海")),
        )

        assertEquals(listOf("shanghai", "unknown", "parent"), result.map { it.id })
        assertEquals(emptyList(), result.last().childComments)
    }

    @Test
    fun keepsEverythingWhenNeitherRuleIsActive() {
        val comments = listOf(comment("jilin", authorId = "ok", ipLocation = "吉林"))

        val result = comments.filterBlockedComments(
            blockedUserIds = emptySet(),
            ipLocationFilter = IpLocationFilterSettings(
                enabled = true,
                filterComments = false,
                whitelist = setOf("上海"),
            ),
        )

        assertEquals(comments, result)
    }

    private fun comment(
        id: String,
        authorId: String,
        ipLocation: String? = null,
        children: List<DataHolder.Comment> = emptyList(),
    ): DataHolder.Comment = DataHolder.Comment(
        id = id,
        type = "comment",
        resourceType = "answer",
        url = "https://www.zhihu.com/api/v4/comments/$id",
        content = id,
        createdTime = 1L,
        isDelete = false,
        collapsed = false,
        reviewing = false,
        author = author(authorId),
        commentTag = ipLocation
            ?.let {
                listOf(
                    DataHolder.Comment.CommentTag(
                        type = "ip_info",
                        text = it,
                        color = "",
                        nightColor = "",
                        hasBorder = false,
                    ),
                )
            }.orEmpty(),
        childComments = children,
    )

    private fun author(id: String): DataHolder.Comment.Author = DataHolder.Comment.Author(
        id = id,
        urlToken = id,
        name = id,
        avatarUrl = "",
        avatarUrlTemplate = "",
        isOrg = false,
        type = "people",
        url = "",
        userType = "people",
        headline = "",
        gender = 0,
        isAdvertiser = false,
    )
}
