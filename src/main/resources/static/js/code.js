$(function () {
    $("#submitBtn").submit(reset);
});


function reset() {
    $.post(
        CONTEXT_PATH+"/user/reset",
        {"email":$("input[name='email']").val(), "code":$("input[name='code']").val(),
                "newPwd":$("input[name='newPwd']").val()},
        function (data) {
            // 将data(json字符串)转换为JSON对象
            data = data.parseJSON(data);
            if (data.code == 0) {
                // 更新密码成功,重定向到登录页面
                location.href="/login";
            } else {
                alert(data.msg);
            }
        }

    );
}