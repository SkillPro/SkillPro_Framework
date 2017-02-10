function toggle(e) {
	e = e.parentElement;
	e.className = e.className ? '' : 'collapsed';
}

function collapseAll(e) {
	collapseAllChildren(e.parentElement.parentElement);
}

function collapseAllChildren(e){
	var c = e.childNodes;
	for (var i = 0; i < c.length; i++) {
		if (c[i].tagName == 'DIV') {
			c[i].className = 'collapsed';
			collapseAllChildren(c[i]);
		}
	}
}

function expandAll(e) {
	expandAllChildren(e.parentElement.parentElement);
}

function expandAllChildren(e){
	var c = e.childNodes;
	for (var i = 0; i < c.length; i++) {
		if (c[i].tagName == 'DIV') {
			c[i].className = '';
			expandAllChildren(c[i]);
		}
	}
}