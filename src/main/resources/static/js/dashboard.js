$(function () {
    var STATUS_LABEL = {
        APPLIED: "심사대기",
        OPENED: "개설",
        CLOSURE_REQUESTED: "해지신청",
        CLOSED: "해지",
        REJECTED: "반려"
    };

    var BATCH_STATUS_LABEL = {
        RUNNING: "진행중",
        COMPLETED: "완료",
        FAILED: "실패"
    };

    var KRW_FORMATTER = new Intl.NumberFormat("ko-KR");
    var DATETIME_FORMATTER = new Intl.DateTimeFormat("ko-KR", {
        year: "numeric", month: "2-digit", day: "2-digit",
        hour: "2-digit", minute: "2-digit"
    });

    function formatDateTime(isoString) {
        if (!isoString) {
            return "-";
        }
        return DATETIME_FORMATTER.format(new Date(isoString)).replace(/\. /g, "-").replace(".", "");
    }

    function formatAmount(amount) {
        return "₩" + KRW_FORMATTER.format(amount || 0);
    }

    function usageRatioPercent(account) {
        if (!account.limitAmount || Number(account.limitAmount) <= 0) {
            return null;
        }
        return Math.round((Number(account.usedAmount) / Number(account.limitAmount)) * 100);
    }

    function renderPriorityAccounts(accounts) {
        var $body = $("#priorityAccountsBody").empty();

        if (!accounts || accounts.length === 0) {
            $body.append('<tr><td colspan="3" class="dash-empty">처리할 계좌가 없습니다.</td></tr>');
            return;
        }

        accounts.forEach(function (account) {
            var ratio = usageRatioPercent(account);
            var ratioCellHtml = "-";
            if (ratio !== null) {
                var barClass = ratio >= 80 ? "usage-bar-fill danger" : "usage-bar-fill";
                ratioCellHtml =
                    '<div class="usage-bar-wrap">' +
                    '<div class="usage-bar-track"><div class="' + barClass + '" style="width:' + Math.min(ratio, 100) + '%"></div></div>' +
                    "<span>" + ratio + "%</span>" +
                    "</div>";
            }

            var row =
                "<tr>" +
                "<td><div class=\"account-no\">" + escapeHtml(account.accountNo || "-") + "</div>" +
                "<div class=\"account-name\">" + escapeHtml(account.customerName || "") + "</div></td>" +
                "<td>" + (STATUS_LABEL[account.status] || escapeHtml(account.status)) + "</td>" +
                "<td>" + ratioCellHtml + "</td>" +
                "</tr>";
            $body.append(row);
        });
    }

    function renderAuditLogs(logs) {
        var $list = $("#recentAuditLogs").empty();

        if (!logs || logs.length === 0) {
            $list.text("최근 감사로그가 없습니다.");
            return;
        }

        logs.forEach(function (log) {
            var line =
                '<div><span class="audit-log-time">' + formatDateTime(log.processedAt) + "</span> · " +
                '<span class="audit-log-actor">' + escapeHtml(log.targetTable || "-") + "</span> — " +
                escapeHtml(log.reasonCode || "") + "</div>";
            $list.append(line);
        });
    }

    function renderBatch(batch) {
        if (!batch) {
            $("#batchExecutedAt").text("실행 이력 없음");
            $("#batchStatus").text("-");
            return;
        }
        $("#batchExecutedAt").text(formatDateTime(batch.executedAt));
        var statusKey = (batch.status || "").toLowerCase();
        $("#batchStatus")
            .attr("class", "status-badge " + statusKey)
            .text(BATCH_STATUS_LABEL[batch.status] || batch.status);
    }

    function escapeHtml(value) {
        return $("<div>").text(value).html();
    }

    function loadDashboard() {
        MARIA.auth.ajax({
            url: "/api/admin/dashboard",
            method: "GET"
        })
            .done(function (res) {
                var data = res.data;

                $("#kpiPending").text(data.pendingAccountCount + " 건");
                $("#kpiProvisional").text(data.provisionalExchangeCount + " 건");
                $("#kpiNearLimit").text(data.nearLimitAccountCount + " 건");
                $("#kpiTodaySell").text(formatAmount(data.todaySellAmount));
                $("#kpiApproved").text(data.todayApprovedCount);
                $("#kpiRejected").text(data.todayRejectedCount);

                renderBatch(data.latestSettlementBatch);
                renderPriorityAccounts(data.priorityAccounts);
                renderAuditLogs(data.recentAuditLogs);

                $("#dashboardLoading").hide();
                $("#dashboardBody").show();
            })
            .fail(function (xhr) {
                if (xhr.status === 401) {
                    return; // MARIA.auth.ajax가 이미 로그인 페이지로 보냄
                }
                $("#dashboardLoading").hide();
                var message = "대시보드를 불러오지 못했습니다.";
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                $("#dashboardError").text(message).show();
            });
    }

    loadDashboard();
});
