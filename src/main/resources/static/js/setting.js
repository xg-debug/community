$(function () {
    $("#uploadForm").submit(upload);
})

function upload() {
    $.ajax({
        url:"http://upload-z1.qiniup.com",
        method: "post",
        processData: false,
        // 不让jQuery去设置上传的类型
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success : function (data) {
            if (data && data.code == 0) {
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName":$("input[name='key']").val()},
                    function(data) {
                        data = $.parseJSON(data);
                        if (data.code == 0) {
                            // 上传成功则刷新页面
                            window.location.reload();
                        }else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败!");
            }
        }

    });

    // 不在继续向下执行默认的事件，上面的逻辑已经处理好了
    return false;
}