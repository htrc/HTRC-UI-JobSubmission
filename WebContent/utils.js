function addEntry(tableId) {

	var table = document.getElementById(tableId);

	var rowCount = table.rows.length;
	var row = table.insertRow(rowCount);

	var cell1 = row.insertCell(0);
	var element1 = document.createElement("input");
	element1.type = "checkbox";
	element1.name = "depCheckbox";
	element1.setAttribute("onclick", "onUnCheck('" + tableId +"')");

	cell1.appendChild(element1);

	var cell2 = row.insertCell(1);
	var element2 = document.createElement("input");
	element2.type = "file";
	element2.name = "dependancy";
	element2
			.setAttribute("onclick", "markCheckbox(this.parentNode.parentNode)");
	cell2.appendChild(element2);
}

function markCheckbox(selectedRow) {
	try {
		var chkbox = selectedRow.cells[0].childNodes[0];
		if (chkbox != null)
			chkbox.checked = true;
	} catch (e) {
		alert(e);
	}

}

function onUnCheck(tableId) {
	try {
		var table = document.getElementById(tableId);
		var rowCount = table.rows.length;

		for ( var i = 0; i < rowCount; i++) {
			var row = table.rows[i];
			var chkbox = row.cells[0].childNodes[0];
			if (null != chkbox && false == chkbox.checked) {
				table.deleteRow(i);
				rowCount--;
				i--;
			}
		}
	} catch (e) {
		alert(e);
	}

}