/** Code for the status page. */
function StatusPage() {
	var digitalModules = [];
	var dimmerModules = [];
	
	console.log("Status page : Loading modules...");
	
	apiLoadModules(function(modules, status, jqXHR) {
		console.log("Done, modules are loaded...");
		
		if (jQuery.browser.mozilla) {
			// Need to parse it for FF, Chrome instantly returns an Object. And fails on the parse().
			modules = JSON.parse(modules);
		}
		
		$.each(modules, function(index, module) {
			module = module[0];
			
			if (module.type == "DIMMER") {
				dimmerModules.push(module.dimmerModule);
			} else if (module.type == "DIGITAL") {
				digitalModules.push(module.digitalModule);
			}
		});
		
		console.log("Loaded [" + digitalModules.length + "] digital modules and [" + dimmerModules.length + "] dimmer modules");
		
		setupUI();
	}, function(jqXHR, status, error) {
		console.log("Error loading modules : [" + error + "]")
	});
	
	function setupUI() {
		$.each(dimmerModules, function(index, module) {
			createTab(module, "Dimmer");
		});
		
		$("#moduleTabs").tabs();
	}
	
	function createTab(module, type) {
		var tabName = "moduleTab" + module.moduleId;
		
		// First add the tab header.
		var tabHeaders = $("#moduleTabHeaders");
		tabHeaders.append("<li><a href='#" + tabName + "'>" + type + " module " + module.moduleId + "</a></li>")
		
		var moduleTabs = $("#moduleTabs");
		moduleTabs.append("<div id='" + tabName + "'>Module </div");
	}
}

StatusPage.prototype.digitalModules = function() {
	return digitalModules;
}

StatusPage.prototype.dimmerModules = function() {
	return dimmerModules;
}