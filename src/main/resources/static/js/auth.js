/**
 * 인증 공용 유틸 (jQuery 기반) — HttpOnly 쿠키 전환 버전.
 * 토큰은 서버가 HttpOnly 쿠키로 관리. 브라우저가 자동 전송.
 * localStorage 토큰 관련 코드 전부 제거.
 */
var MARIA = window.MARIA || {};

MARIA.auth = (function ($) {

    // GET /api/admin/me 호출로 로그인 여부 확인.
    // 401이면 /login으로 리다이렉트. Promise를 반환하므로 .done()에서 후속 처리.
    function requireAuth() {
        var deferred = $.Deferred();
        $.ajax({ url: "/api/admin/me", method: "GET" })
            .done(function (res) { deferred.resolve(res.data); })
            .fail(function (xhr) {
                if (xhr.status === 401) {
                    window.location.href = "/login";
                }
                deferred.reject(xhr);
            });
        return deferred.promise();
    }

    function logout() {
        $.ajax({ url: "/api/auth/admin/logout", method: "POST" })
            .always(function () { window.location.href = "/login"; });
    }

    // Authorization 헤더 없음 — 브라우저가 쿠키 자동 전송.
    // 401이면 refresh 시도(refresh_token 쿠키 자동 전송) 후 원 요청 재시도.
    function ajax(options) {
        var deferred = $.Deferred();

        $.ajax(options)
            .done(function (data, textStatus, jqXHR) {
                deferred.resolve(data, textStatus, jqXHR);
            })
            .fail(function (xhr) {
                if (xhr.status !== 401) {
                    deferred.reject(xhr);
                    return;
                }
                refreshAccessToken()
                    .done(function () {
                        $.ajax(options)
                            .done(function (data, textStatus, jqXHR) {
                                deferred.resolve(data, textStatus, jqXHR);
                            })
                            .fail(function (retryXhr) {
                                if (retryXhr.status === 401) { logout(); }
                                deferred.reject(retryXhr);
                            });
                    })
                    .fail(function () {
                        logout();
                        deferred.reject(xhr);
                    });
            });

        return deferred.promise();
    }

    var refreshInFlight = null;
    function refreshAccessToken() {
        if (refreshInFlight) return refreshInFlight;
        refreshInFlight = $.ajax({
            url: "/api/auth/admin/refresh",
            method: "POST"
            // body 없음 — refresh_token 쿠키 자동 전송
        }).always(function () { refreshInFlight = null; });
        return refreshInFlight;
    }

    return { requireAuth: requireAuth, logout: logout, ajax: ajax };
})(jQuery);
