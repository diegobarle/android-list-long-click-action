package uk.co.diegobarle.longclicklistanimation

abstract class ListItem

data class TitleListItem(val title: String): ListItem()

data class SimpleListItem(val description: String): ListItem()

data class SimpleListItemWithHeader(val description: String, val headerTitle: String): ListItem()