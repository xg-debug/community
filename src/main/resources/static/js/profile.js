$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			// 需要访问的地址
			CONTEXT_PATH + "/follow",
			// 需要发送到服务端的地址
			{"entityType":3, "entityId":$(btn).prev().val()},
			// func：请求成功后，服务器回调的函数，data为服务器传回的数据
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0) {
					// 关注成功,刷新页面
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
			// dataType：服务器返回数据的格式

		);
		//$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3, "entityId":$(btn).prev().val()},
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0) {
					// 取消关注成功,刷新页面
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
		);
		$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}