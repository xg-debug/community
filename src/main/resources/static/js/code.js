$(function () {
    $("invalid-feedback").click(send_code)
});

function send_code() {
    $.post(
        CONTEXT_PATH+"/user/getCode",
        function (data) {

        }

    )
}