package com.sampson.leecode.temp

/**
N 个灯泡排成一行，编号从 1 到 N 。最初，所有灯泡都关闭。每天只打开一个灯泡，直到 N 天后所有灯泡都打开。

给你一个长度为 N 的灯泡数组 blubs ，其中 bulls[i] = x 意味着在第 (i+1) 天，我们会把在位置 x 的灯泡打开，其中 i 从 0 开始，x 从 1 开始。

给你一个整数 K ，请你输出在第几天恰好有两个打开的灯泡，使得它们中间 正好 有 K 个灯泡且这些灯泡 全部是关闭的 。

如果不存在这种情况，返回 -1 。如果有多天都出现这种情况，请返回 最小的天数 。
 */
class Test {
    fun kEmptySlots(bulbs: IntArray, k: Int): Int {
        //目的是找数组任意两元素只差为k，且两元素之间不存在其他元素。
        //由于只返回最小天数，为提高算法效率，通过空间换时间，需要将数组排序，并保存天与位置的对应关系

        // whichDay 中 下标表示第几个灯泡从0开始，元素灯泡第几天被点亮。
        val whichDay = IntArray(bulbs.size)
        // maxBulb 中 存放这一天里已点亮的最大的灯的下标
        val maxBulb = IntArray(bulbs.size)
        var maxBulbNum = -1
        for (i in bulbs) {
            whichDay[bulbs[i] - 1] = i + 1
            if (maxBulbNum < bulbs[i]) {
                maxBulbNum = bulbs[i]
            }
            maxBulb[i] = maxBulbNum
        }
        check(bulbs, whichDay, maxBulb, k, 0)
        return -1
    }

    private fun check(bulbs: IntArray, whichDay: IntArray, maxBulb: IntArray, k: Int, startDay: Int) {
        if (startDay >= bulbs.size) {
            // 遍历完了，没有找到，返回-1
        }
        val startBulb = bulbs[startDay + 1]
        if (startBulb + k >= whichDay.size) {
            // 超范围，跳过
        }
        val endDay = whichDay[startBulb + k + 1]

    }
}