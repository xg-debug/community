// 相当于window.onload
$(function () {
    // 分别给置顶、加精、删除三个按钮绑定单击事件
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});


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
                // 重新加载页面
                // $(".badge badge-danger").text(data.allUnreadCount != 0 ? data.allUnreadCount : '');
                // alert(allUnreadCount);
            } else {
                // 返回错误提示信息
                alert(data.msg);
            }
        }
    );
}

// 置顶
function setTop () {
    // 发起异步请求
    $.post(
        // 访问的路径
        CONTEXT_PATH + "/discuss/top",
        // 携带的参数data
        {"postId" : $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                // 点过置顶后，将置顶按钮设置为不可用
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful () {
    // 发起异步请求
    $.post(
        // 访问的路径
        CONTEXT_PATH + "/discuss/wonderful",
        // 携带的参数data
        {"postId" : $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                // 点过加精后，将加精按钮设置为不可用
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete () {
    // 发起异步请求
    $.post(
        // 访问的路径
        CONTEXT_PATH + "/discuss/delete",
        // 携带的参数data
        {"postId" : $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                // 点过删除后，就跳转到首页
                location.href = CONTEXT_PATH + "/index"
            } else {
                alert(data.msg);
            }
        }
    );
}