(function() {
	Envjs.scriptTypes['text/javascript'] = true;
	
	var s;
	while ((s = req.readLine()) != null) {
		document.writeln(s);
	}
	document.close();
	req = document.documentElement.outerHTML;
})();