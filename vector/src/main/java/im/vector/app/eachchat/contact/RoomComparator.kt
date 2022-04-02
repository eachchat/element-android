package im.vector.app.eachchat.contact

import org.matrix.android.sdk.api.session.room.model.RoomSummary

class RoomComparator : Comparator<RoomSummary> {
    override fun compare(o1: RoomSummary, o2: RoomSummary): Int {
        //邀请排最前面
        //收藏排第二栏
        //低优先级放最后
        //其它往后面排
//        if (o2.membership == o1.membership) {
//            if (o1.isFavorite && o2.isFavorite) {
//                return orderByTime(o1, o2)
//            } else if (o1.isFavorite) {
//                return -1
//            } else if (o2.isFavorite) {
//                return 1
//            }

//            if (o1.isLowPriority && o2.isLowPriority) {
//                return orderByTime(o1, o2)
//            } else if (o1.isLowPriority) {
//                return 1
//            } else if (o2.isLowPriority) {
//                return -1
//            }
//            return orderByTime(o1, o2)
//        } else {
//            return if (o1.membership == Membership.INVITE) {
//                -1
//            } else {
//                1
//            }
//        }
        return orderByTime(o1, o2)
    }

    private fun orderByTime(o1: RoomSummary, o2: RoomSummary): Int {
        if (o1.latestPreviewableEvent == null && o2.latestPreviewableEvent == null) {
            return 0
        }
        if (o1.latestPreviewableEvent == null) {
            return 1
        } else if (o2.latestPreviewableEvent == null) {
            return -1
        }
        return if (o1.latestPreviewableEvent?.root?.originServerTs!! > o2.latestPreviewableEvent?.root?.originServerTs!!) {
            -1
        } else {
            1
        }
    }
}
