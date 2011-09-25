/** UI interaction code for the menu. */
function menuMouseOver(itemId) {
	var item = $("#" + itemId);
	item.removeClass("ui-state-default");
	item.addClass("ui-state-hover");
}

function menuMouseOut(itemId) {
	var item = $("#" + itemId);
	item.removeClass("ui-state-hover");
	item.addClass("ui-state-default")
}