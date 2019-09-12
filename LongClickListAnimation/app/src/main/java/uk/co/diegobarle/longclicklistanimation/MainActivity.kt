package uk.co.diegobarle.longclicklistanimation

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.diegobarle.longclicklistanimation.ActionLongClickListener.Companion.BOUNDARY_MATCH_ORIGINAL

class MainActivity: Activity(){

    private val listItems = listOf(
        TitleListItem("Title 1"),
        SimpleListItem("Simple item 1"),
        SimpleListItem("Simple item 2"),
        TitleListItem("Title 2"),
        SimpleListItemWithHeader("Simple item with header 1", "Header 1"),
        SimpleListItemWithHeader("Simple item with header 2", "Header 2")
    )

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupTimelineEventsRecyclerView()
    }

    private fun setupTimelineEventsRecyclerView() {
        list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val adapter = AdapterExample(listItems)
        list.adapter = adapter

        list.addOnItemTouchListener(ActionLongClickListener(
            this,
            R.drawable.ic_share,
            ResourcesCompat.getColor(resources, R.color.colorAccent, null),
            object: ActionLongClickListener.ActionLongClickCallback{
                override fun getViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect? {
                    return null
                }

                override fun getHighlightedViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect?{
                    return if(adapter.getItemViewType(selectedPosition) == AdapterExample.ITEM_WITH_HEADER_TYPE){
                        //we remove the stars-rating because we don't want them to be highlighted,
                        //but still be considered in the gesture (to show the loader under it).
                        //Moreover, we don't want the action event to be activated by clicking on it either.
                        val ratingHeight = selectedView?.findViewById<View?>(R.id.ratingBar)?.height?:0
                        Rect(
                            0,
                            0,
                            0,
                            ratingHeight)
                    }else{
                        null //We want everything to be considered
                    }
                }

                override fun getViewBoundary(selectedPosition: Int, selectedView: View?): Rect? {
                    return if(adapter.getItemViewType(selectedPosition) == AdapterExample.ITEM_WITH_HEADER_TYPE){
                        //we dont want the header region to be considered in the gesture at all
                        val headerHeight = selectedView?.findViewById<View?>(R.id.headerView)?.height?:0
                        Rect(
                            BOUNDARY_MATCH_ORIGINAL,
                            if(headerHeight == 0) BOUNDARY_MATCH_ORIGINAL else headerHeight,
                            BOUNDARY_MATCH_ORIGINAL,
                            BOUNDARY_MATCH_ORIGINAL)
                    }else{
                        null //We want everything to be considered
                    }
                }

                override fun isItemActionable(selectedPosition: Int): Boolean {
                    return adapter.getItemViewType(selectedPosition) != AdapterExample.TITLE_TYPE
                }

                override fun onAction(selectedPosition: Int) {
                    val message = when(adapter.getItemViewType(selectedPosition)){
                        AdapterExample.TITLE_TYPE -> "Selected a title"
                        AdapterExample.SIMPLE_ITEM_TYPE -> "Selected a simple item"
                        AdapterExample.ITEM_WITH_HEADER_TYPE -> "Selected a simple item with header"
                        else -> "Selected nothing"
                    }
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                }
            }))
    }
}