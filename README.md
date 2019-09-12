# android-list-long-click-action
Listener to add to a recycler view that shows an animation for an action when a user does a long click on an item in the list.

All the magic happens inside the single class ActionLongClickListener which reacts to user's touches in the recycler view and on long touch detected, starts the animations to highlight the selected item from the list.

To use it, simply add an instance of the ActionLongClickListener to the recyclerView.addOnItemTouchListener().

For example:

    list.addOnItemTouchListener(ActionLongClickListener(
            this,
            R.drawable.ic_share,
            ResourcesCompat.getColor(resources, R.color.colorAccent, null),
            object: ActionLongClickListener.ActionLongClickCallback{
                override fun getViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect? {
                    return null
                }

                override fun getHighlightedViewBoundaryPadding(selectedPosition: Int, selectedView: View?): Rect?{
                    return null
                }

                override fun getViewBoundary(selectedPosition: Int, selectedView: View?): Rect? {
                    return null
                }

                override fun isItemActionable(selectedPosition: Int): Boolean {
                    return true
                }

                override fun onAction(selectedPosition: Int) {
                    Toast.makeText(this@MainActivity, "Item $selectedPosition selected", Toast.LENGTH_SHORT).show()
                }
            }))
