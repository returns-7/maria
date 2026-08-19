/**
 * 공용 레이아웃(사이드바+헤더) 동작.
 * layout/main.html을 쓰는 모든 페이지에서 공통으로 로드된다.
 */
MARIA.ui = MARIA.ui || {};

MARIA.ui.showError = function (message) {
    var $container = $("#toastContainer");
    var maxToastCount = 3;
    while ($container.children(".toast").length >= maxToastCount) {
        $container.children(".toast").first().remove();
    }
    var $toast = $("<div>", { "class": "toast", text: message || "요청 처리 중 오류가 발생했습니다." });
    $container.append($toast);
    requestAnimationFrame(function () { $toast.addClass("is-visible"); });
    setTimeout(function () {
        $toast.removeClass("is-visible");
        setTimeout(function () { $toast.remove(); }, 200);
    }, 3500);
};

$(function () {
    if (!MARIA.auth.requireAuth()) {
        return;
    }

    var currentBusinessTime = null;
    var admin = MARIA.auth.currentAdmin();
    if (admin) {
        $("#adminBadge").text(admin.name + " · " + admin.role);
    }

    var isAdmin = admin && admin.role === "ADMIN";
    $("#clockButton")
        .prop("disabled", !isAdmin)
        .toggleClass("is-editable", isAdmin)
        .attr("title", isAdmin ? "업무시각 설정" : "업무시각 조회");

    applyTheme(localStorage.getItem("maria.theme") || "light");
    loadReferenceTime();

    $("#themeToggle").on("click", function () {
        var next = document.documentElement.getAttribute("data-theme") === "dark" ? "light" : "dark";
        localStorage.setItem("maria.theme", next);
        applyTheme(next);
    });

    $("#logoutLink").on("click", function () {
        MARIA.auth.logout();
    });

    loadAccountReviewCount();

    setInterval(function () {
        if (!currentBusinessTime) {
            return;
        }

        currentBusinessTime = new Date(currentBusinessTime.getTime() + 1000);
        renderBusinessTime();
    }, 1000);

    $("#clockButton").on("click", function () {
        if (!isAdmin || !currentBusinessTime) {
            return;
        }

        $("#clockDatetime").val(toDateTimeLocalValue(currentBusinessTime));
        $("#clockReasonCode").val("");
        $("#clockModal").prop("hidden", false);
        $("#clockDatetime").trigger("focus");
    });

    $("#clockModalClose, #clockModalCancel").on("click", closeClockModal);

    $("#clockModal").on("click", function (event) {
        if (event.target === this) {
            closeClockModal();
        }
    });

    $(document).on("keydown", function (event) {
        if (event.key === "Escape" && !$("#clockModal").prop("hidden")) {
            closeClockModal();
        }
    });

    $("#clockApplyButton").on("click", function () {
        var newDatetime = $("#clockDatetime").val();
        var reasonCode = $("#clockReasonCode").val().trim();

        if (!newDatetime) {
            MARIA.ui.showError("변경할 업무시각을 입력해 주세요.");
            return;
        }
        if (!reasonCode) {
            MARIA.ui.showError("업무시각 변경 사유를 입력해 주세요.");
            return;
        }

        var $applyButton = $(this).prop("disabled", true);
        MARIA.auth.ajax({
            url: "/api/admin/system-clock",
            method: "PATCH",
            contentType: "application/json",
            data: JSON.stringify({
                newDatetime: newDatetime.length === 16 ? newDatetime + ":00" : newDatetime,
                reasonCode: reasonCode
            })
        })
            .done(function (res) {
                if (res.data) {
                    currentBusinessTime = new Date(res.data);
                    renderBusinessTime();
                }
                $(document).trigger("maria:system-clock-changed");
                closeClockModal();
            })
            .fail(function (xhr) {
                var message = xhr.responseJSON && xhr.responseJSON.message;
                MARIA.ui.showError(message || "업무시각 변경에 실패했습니다.");
            })
            .always(function () {
                $applyButton.prop("disabled", false);
            });
    });
    function applyTheme(theme) {
        document.documentElement.setAttribute("data-theme", theme);
        $("#themeToggle").text(theme === "dark" ? "🌙" : "☀");
        $(document).trigger("maria:themeChange", [theme]);
    }

    function loadAccountReviewCount() {
        MARIA.auth.ajax({
            url: "/api/account/requiring-action-count",
            method: "GET"
        }).done(function (res) {
            var count = res.data || 0;
            $("#accountReviewBadge").text(count).toggle(count > 0);
        });
    }

    function loadReferenceTime() {
        MARIA.auth.ajax({
            url: "/api/admin/system-clock",
            method: "GET"
        })
            .done(function (res) {
                if (res.data) {
                    currentBusinessTime = new Date(res.data);
                    renderBusinessTime();
                }
            })
            .fail(function (xhr) {
                if (xhr.status !== 401) {
                    MARIA.ui.showError("시스템 업무시각을 불러오지 못했습니다.");
                }
            });
    }

    function renderBusinessTime() {
        $("#clockValue").text(formatDateTime(currentBusinessTime));
    }

    function closeClockModal() {
        $("#clockModal").prop("hidden", true);
    }

    function toDateTimeLocalValue(value) {
        function pad(number) {
            return String(number).padStart(2, "0");
        }

        return value.getFullYear()
            + "-" + pad(value.getMonth() + 1)
            + "-" + pad(value.getDate())
            + "T" + pad(value.getHours())
            + ":" + pad(value.getMinutes());
    }

    function formatDateTime(value) {
        return new Intl.DateTimeFormat("ko-KR", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
            hour12: false
        }).format(new Date(value));
    }
});
