# android-list-long-click-action
Listener to add to a recycler view that shows an animation for an action when a user does a long click on an item in the list.

All the magic happens inside the single class ActionLongClickListener which reacts to user's touches in the recycler view and on long touch detected, starts the animations to highlight the selected item from the list.

To use it, simply add an instance of the ActionLongClickListener to the recyclerView.addOnItemTouchListener().

For example:

    list.addOnItemTouchListener(ActionLongClickListener(
            this,
            R.drawable.ic_share,
            ResourcesCompat.getColor(resources, R.color.colorAccent, null),
            object: ActionLongClickListener.SimpleActionLongClickCallback{
                override fun onAction(selectedPosition: Int) {
                    Toast.makeText(this@MainActivity, "Selected item at $selectedPosition", Toast.LENGTH_SHORT).show()
                }
            }))

For more complex scenarios where parts of the views from the list shouldn't be highlighted, you can implement the ```getViewBoundaryPadding```, ```getHighlightedViewBoundaryPadding``` and ```getViewBoundary``` methods. 

For instance, if we have a recycler view that displays different types of items (title item, simple item and simple item with header) we can decide which items not to consider for the selection (e.g. the title items) and which items we want to have part of it's rect highlighted (e.g. header item which also contains the stars rating).


list.addOnItemTouchListener(ActionLongClickListener(
            this,
            R.drawable.ic_share,
            ResourcesCompat.getColor(resources, R.color.colorAccent, null),
            object: ActionLongClickListener.ActionLongClickCallback{
                override fun getViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect? {
                    return null
                }

                override fun getHighlightedViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect?{
                    //return if(adapter.getItemViewType(selectedPosition) == AdapterExample.ITEM_WITH_HEADER_TYPE){
                    //    //we remove the stars-rating because we don't want them to be highlighted,
                    //    //but still be considered in the gesture (to show the loader under it).
                    //    //Moreover, we don't want the action event to be activated by clicking on it either.
                    //    val ratingHeight = selectedView?.findViewById<View?>(R.id.ratingBar)?.height?:0
                    //    Rect(
                    //        0,
                    //        0,
                    //        0,
                    //        ratingHeight)
                    //}else{
                    //    null //We want everything to be considered
                    //}
                    return null
                }

                override fun getViewBoundary(selectedPosition: Int, selectedView: View?): Rect? {
                    //return if(adapter.getItemViewType(selectedPosition) == AdapterExample.ITEM_WITH_HEADER_TYPE){
                    //    //we dont want the header region to be considered in the gesture at all
                    //    val headerHeight = selectedView?.findViewById<View?>(R.id.headerView)?.height?:0
                    //    Rect(
                    //        BOUNDARY_MATCH_ORIGINAL,
                    //        if(headerHeight == 0) BOUNDARY_MATCH_ORIGINAL else headerHeight,
                    //        BOUNDARY_MATCH_ORIGINAL,
                    //        BOUNDARY_MATCH_ORIGINAL)
                    //}else{
                    //    null //We want everything to be considered
                    //}
                    return null
                }

                override fun isItemActionable(selectedPosition: Int): Boolean {
                    //We don't want the title to be 
                    //return adapter.getItemViewType(selectedPosition) != AdapterExample.TITLE_TYPE
                    return true
                }

                override fun onAction(selectedPosition: Int) {
                    Toast.makeText(this@MainActivity, "Selected item at $selectedPosition", Toast.LENGTH_SHORT).show()
                }
            }))
            
![](long_tap_gesture.gif)
