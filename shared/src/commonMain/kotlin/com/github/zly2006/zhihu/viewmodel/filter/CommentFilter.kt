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
import com.github.zly2006.zhihu.data.ipLocation

/**
 * 过滤评论列表：命中屏蔽作者，或 IP 属地不在白名单内的评论会被移除，子评论同样处理。
 *
 * 没有 IP 属地信息的评论始终保留；[ipLocationFilter] 未对评论生效时只按屏蔽作者过滤。
 */
fun List<DataHolder.Comment>.filterBlockedComments(
    blockedUserIds: Set<String>,
    ipLocationFilter: IpLocationFilterSettings,
): List<DataHolder.Comment> {
    val filterByIpLocation = ipLocationFilter.isActiveForComments
    if (blockedUserIds.isEmpty() && !filterByIpLocation) return this

    fun keep(comment: DataHolder.Comment): Boolean = comment.author.id !in blockedUserIds &&
        (!filterByIpLocation || ipLocationFilter.allows(comment.ipLocation))

    return mapNotNull { comment ->
        if (!keep(comment)) {
            null
        } else {
            comment.copy(childComments = comment.childComments.filter(::keep))
        }
    }
}
