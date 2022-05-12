package im.vector.app.eachchat.search.chatRecord

import im.vector.app.eachchat.search.contactsearch.data.SearchData

/**
 * Created by zhouguanjie on 2021/1/5.
 */
sealed class SearchAction {

    data class SearchResult(val result: List<SearchData>?) : SearchAction()

    data class SearchMoreResult(val result: List<SearchData>?) : SearchAction()

}
