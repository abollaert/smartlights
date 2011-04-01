function showMoods() {
	$.mobile.pageLoading();
	
	$.ajax({
		url: 'http://localhost:8080/api/GetMoods',
		type: 'POST',
		dataType : 'json',
		data: ({ format : 'json' }),
		success: function(moods) {
			console.log(moods);
			
			moodList = $("#moodlist");
			moodList.empty();
			
			for (var i = 0; i < moods.moods.length; i++) {
				var mood = moods.moods[i];
				
				console.log(mood.name);
				moodList.append("<li><a href=\"showMood(" + mood.moodId + ")>" + mood.name + "</a></li>");
			}
		},
		error: function(xhr, status, error) {
			console.log(status);
			
		},
		complete: function(xhr, status) {
			$.mobile.pageLoading(true);
		}
	});
}
