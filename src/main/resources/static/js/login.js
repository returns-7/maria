$(function () {
    // 이미 로그인돼 있으면 바로 통계보드로.
    if (MARIA.auth.getAccessToken()) {
        window.location.href = "/admin/statistics";
        return;
    }

    $("#loginForm").on("submit", function (event) {
        event.preventDefault();

        var loginId = $("#loginId").val();
        var password = $("#password").val();
        $("#loginError").hide();

        $.ajax({
            url: "/api/auth/admin/login",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({loginId: loginId, password: password})
        })
            .done(function (res) {
                MARIA.auth.saveTokens(res.data.accessToken, res.data.refreshToken);
                window.location.href = "/admin/statistics";
            })
            .fail(function (xhr) {
                var message = "로그인에 실패했습니다.";
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                $("#loginError").text(message).show();
            });
    });
});
