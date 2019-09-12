package uk.co.diegobarle.longclicklistanimation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterExample(private val items: List<ListItem>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object{
        const val TITLE_TYPE = 0
        const val SIMPLE_ITEM_TYPE = 1
        const val ITEM_WITH_HEADER_TYPE = 2
    }

    override fun getItemViewType(position: Int): Int {
        if(position < 0) return -1
        return when(items[position]){
            is TitleListItem -> TITLE_TYPE
            is SimpleListItem -> SIMPLE_ITEM_TYPE
            is SimpleListItemWithHeader -> ITEM_WITH_HEADER_TYPE
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType){
            TITLE_TYPE -> TitleViewHolder(layoutInflater.inflate(R.layout.list_view_title, parent, false))
            SIMPLE_ITEM_TYPE -> SimpleItemViewHolder(layoutInflater.inflate(R.layout.list_view_simple_item, parent, false))
            ITEM_WITH_HEADER_TYPE -> SimpleItemWithHeaderViewHolder(layoutInflater.inflate(R.layout.list_view_simple_item_header, parent, false))
            else -> TitleViewHolder(layoutInflater.inflate(R.layout.list_view_title, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)){
            TITLE_TYPE -> (holder as TitleViewHolder).onBind(items[position] as TitleListItem)
            SIMPLE_ITEM_TYPE -> (holder as SimpleItemViewHolder).onBind(items[position] as SimpleListItem)
            ITEM_WITH_HEADER_TYPE -> (holder as SimpleItemWithHeaderViewHolder).onBind(items[position] as SimpleListItemWithHeader)
            else -> (holder as TitleViewHolder).onBind(TitleListItem(""))
        }
    }


    inner class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView = view.findViewById<TextView?>(R.id.titleView)

        fun onBind(item: TitleListItem) {
            titleView?.text = item.title
        }
    }

    inner class SimpleItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val descriptionView = view.findViewById<TextView?>(R.id.descriptionView)

        fun onBind(item: SimpleListItem) {
            descriptionView?.text = item.description
        }
    }

    inner class SimpleItemWithHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val descriptionView = view.findViewById<TextView?>(R.id.descriptionView)
        private val headerView = view.findViewById<TextView?>(R.id.headerView)

        fun onBind(item: SimpleListItemWithHeader) {
            headerView?.text = item.headerTitle
            descriptionView?.text = item.description
        }
    }
}