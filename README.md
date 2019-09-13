# Android List long click action

This is a listener to add to a recycler view that shows an animation for an action when a user does a long click on an item in the list.

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
                    //val message = when(adapter.getItemViewType(selectedPosition)){
                    //    AdapterExample.TITLE_TYPE -> "Selected a title"
                    //    AdapterExample.SIMPLE_ITEM_TYPE -> "Selected a simple item"
                    //    AdapterExample.ITEM_WITH_HEADER_TYPE -> "Selected a simple item with header"
                    //    else -> "Selected nothing"
                    //}
                    //Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    Toast.makeText(this@MainActivity, "Selected item at $selectedPosition", Toast.LENGTH_SHORT).show()
                }
            }))
            
<img src="https://media.giphy.com/media/TJaK4DPJJfwuRGoEn7/giphy.gif" width="25%" height="25%">

## ActionLongClickListener
### onAction
When the long click finishes, the ```onAction``` method is called indicating the item position in the recycler view's adapter that was selected by the user.

### isItemActionable
Specify if the view at the ```selectedPosition``` can be used for the long click. If returned false, the item at that selected position is ignored.

### getViewBoundary
Defines the part of the view selected from the recycler view we want to use for the gesture. 
If null, the view boundary matches the selected view from the recycler view.

### getViewBoundaryPadding
Padding to the view that should be included for the gesture. Used to exclude/include
fragments of the view selected from the recycler view.
If null, the view to be part of the gesture matches the getViewBoundary()

### getHighlightedViewBoundaryPadding
Padding to the view being highlighted.
If values in Rect are positive, the highlighted section to be taken from the view will be smaller.
If values in Rect are negative, the highlighted section to be taken from the view will be bigger.
If null, the highlighted section will match the one defined by getViewBoundaryPadding()

## Button and bar position
Note that the button will be showing in the center of the highlightedView (considering padding if any specified),
and the loader will be shown at the bottom of the view boundary (or at the bottom of the padding if any).
Moreover the highlightedView (with the padding applied if any) is the only region accepted for the touching gesture. If user touches anywhere else, it won't activate the animation.
