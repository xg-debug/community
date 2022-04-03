function like(btn, entityType, entityId, entityUserId, postId) {
    // 点赞发起异步请求
    $.post(
        // 访问的路径
        CONTEXT_PATH + "/like",
        // 携带的参数data
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                // 更新点赞的数量
                $(btn).children("i").text(data.likeCount);
                // 更新点赞的状态
                $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");
            } else {
                // 返回错误提示信息
                alert(data.msg);
            }
        }
    );
}