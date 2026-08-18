$(function () {
    var QTY_FORMATTER = new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 4 });
    var KRW_FORMATTER = new Intl.NumberFormat("ko-KR");
    var DATETIME_FORMATTER = new Intl.DateTimeFormat("ko-KR", {
        year: "numeric", month: "2-digit", day: "2-digit",
        hour: "2-digit", minute: "2-digit"
    });

    var investments = [];
    var selectedAccountId = null;
    var PAGE_SIZE = 20;

    var STATUS_LABEL = {
        HOLDING: "보유중",
        PARTIALLY_SOLD: "일부매도",
        SOLD_OUT: "전량매도",
        TRADE_RESTRICTED: "거래제한",
        TRADE_SUSPENDED: "거래정지",
        TERMINATED: "보유종료"
    };
    var DANGER_STATUSES = ["TRADE_RESTRICTED", "TRADE_SUSPENDED"];
    var TYPE_LABEL = { STOCK: "국내주식", FUND: "펀드" };
    var TRADE_TYPE_LABEL = { BUY: "매수", SELL: "매도" };

    function formatDateTime(isoString) {
        if (!isoString) {
            return "-";
        }
        return DATETIME_FORMATTER.format(new Date(isoString)).replace(/\. /g, "-").replace(".", "");
    }
    function formatQty(qty) {
        return QTY_FORMATTER.format(qty || 0) + "주";
    }
    function formatAmount(amount) {
        return "₩" + KRW_FORMATTER.format(amount || 0);
    }
    function escapeHtml(value) {
        return $("<div>").text(value).html();
    }
    function statusBadge(status) {
        var label = STATUS_LABEL[status] || status;
        var cls = DANGER_STATUSES.indexOf(status) >= 0 ? "failed" : "completed";
        return '<span class="status-badge ' + cls + '">' + label + '</span>';
    }

    function renderList() {
        var $items = $("#diListItems").empty();
        $("#diListCount").text(investments.length + "건 수신");
        if (investments.length === 0) {
            $items.append('<div class="dash-empty">국내투자 계좌가 없습니다.</div>');
            return;
        }
        investments.forEach(function (item) {
            var isSelected = item.accountId === selectedAccountId;
            var $row = $(
                '<div class="di-list-item' + (isSelected ? " selected" : "") + '">' +
                '<div class="di-list-item-top">' +
                '<span class="di-list-item-name">' + escapeHtml(item.customerName) + '</span>' +
                (item.hasRestrictedHolding
                    ? '<span class="status-badge failed">거래제한/정지</span>'
                    : "") +
                '</div>' +
                '<div class="di-list-item-bottom">' +
                '<span>계좌 ' + escapeHtml(item.accountNo || "-") + '</span>' +
                '<span>예탁금 ' + formatAmount(item.cashAmount) + '</span>' +
                '<span>보유종목 ' + item.holdingCount + '건</span>' +
                '</div>' +
                '</div>'
            );
            $row.on("click", function () {
                selectedAccountId = item.accountId;
                renderList();
                loadDetail(item.accountId);
            });
            $items.append($row);
        });
    }

    function renderPagination(page) {
        var $pagination = $("#diPagination").empty();
        if (!page || page.totalPages <= 1) {
            return;
        }

        var current = page.page;
        var totalPages = page.totalPages;
        var BLOCK_SIZE = 10;
        var blockStart = Math.floor(current / BLOCK_SIZE) * BLOCK_SIZE;
        var blockEnd = Math.min(totalPages - 1, blockStart + BLOCK_SIZE - 1);

        function addButton(label, targetPage, isDisabled, isActive) {
            var classes = "page-btn" + (isActive ? " active" : "");
            var $btn = $('<button type="button" class="' + classes + '">' + label + "</button>");
            $btn.prop("disabled", isDisabled || isActive);
            if (!isDisabled && !isActive) {
                $btn.on("click", function () {
                    loadInvestments(targetPage);
                });
            }
            $pagination.append($btn);
        }

        addButton("이전", blockStart - 1, blockStart === 0, false);
        for (var i = blockStart; i <= blockEnd; i++) {
            addButton(String(i + 1), i, false, i === current);
        }
        addButton("다음", blockEnd + 1, blockEnd === totalPages - 1, false);
    }

    function renderDetail(data) {
        var $detail = $("#diDetail").empty();

        $detail.append(
            '<div class="section-header">' +
            '<span>' + escapeHtml(data.accountNo || "-") + ' · ' + escapeHtml(data.customerName) + '</span>' +
            '<span class="section-sub">예탁금 ' + formatAmount(data.cashAmount) + '</span>' +
            '</div>'
        );

        var holdings = data.holdings || [];
        var holdingRows = holdings.map(function (h) {
            return (
                '<tr>' +
                '<td>' + escapeHtml(h.name) + ' (' + escapeHtml(h.ticker) + ')</td>' +
                '<td>' + (TYPE_LABEL[h.type] || escapeHtml(h.type)) + '</td>' +
                '<td>' + formatQty(h.qty) + '</td>' +
                '<td>' + formatAmount(h.avgPurchasePrice) + '</td>' +
                '<td>' + formatDateTime(h.lastPurchaseDate) + '</td>' +
                '<td>' + statusBadge(h.status) + '</td>' +
                '<td>' + (h.currentlyPurchasable
                    ? '<span class="status-badge completed">매수가능</span>'
                    : '<span class="status-badge failed">매수불가</span>') + '</td>' +
                '</tr>'
            );
        }).join("");

        $detail.append(
            '<div class="section-header">' +
            '<span>보유종목</span>' +
            '<span class="section-sub">' + holdings.length + '건</span>' +
            '</div>' +
            '<table class="dash-table">' +
            '<thead><tr><th>종목</th><th>구분</th><th>수량</th><th>평균매수단가</th><th>최근매수일</th><th>상태</th><th>매수가능여부</th></tr></thead>' +
            '<tbody>' + (holdingRows || '<tr><td colspan="7" class="dash-empty">보유종목이 없습니다.</td></tr>') + '</tbody>' +
            '</table>'
        );

        var tradeHistory = data.tradeHistory || [];
        var tradeItems = tradeHistory.map(function (t) {
            return (
                '<li>' + (TRADE_TYPE_LABEL[t.tradeType] || escapeHtml(t.tradeType)) + ' ' +
                escapeHtml(t.stockCode) + ' ' + formatQty(t.qty) +
                ' @ ' + formatAmount(t.price) + ' · ' + formatDateTime(t.executedAt) +
                '</li>'
            );
        }).join("");

        $detail.append(
            '<div class="section-header">' +
            '<span>개별 매매내역</span>' +
            '<span class="section-sub">' + tradeHistory.length + '건</span>' +
            '</div>' +
            (tradeItems
                ? '<ul class="di-trade-history">' + tradeItems + '</ul>'
                : '<div class="dash-empty">매매내역이 없습니다.</div>')
        );
    }

    function loadDetail(accountId) {
        $("#diDetail").empty().append('<div class="dash-loading">불러오는 중...</div>');
        MARIA.auth.ajax({
            url: "/api/domestic-investments/" + accountId,
            method: "GET"
        })
            .done(function (res) {
                renderDetail(res.data);
            })
            .fail(function (xhr) {
                if (xhr.status === 401) {
                    return;
                }
                var message = "상세 정보를 불러오지 못했습니다.";
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                $("#diDetail").empty().append('<div class="dash-error">' + escapeHtml(message) + '</div>');
            });
    }

    function getFilterParams() {
        var params = {};
        var customerName = $("#diFilterCustomerName").val();
        if (customerName) {
            params.customerName = customerName;
        }
        var status = $("#diFilterStatus").val();
        if (status) {
            params.status = status;
        }
        return params;
    }

    function loadInvestments(page) {
        var targetPage = page || 0;
        var requestData = $.extend({ page: targetPage, size: PAGE_SIZE }, getFilterParams());
        MARIA.auth.ajax({
            url: "/api/domestic-investments",
            method: "GET",
            data: requestData
        })
            .done(function (res) {
                investments = res.data.content || [];
                selectedAccountId = null;
                renderList();
                renderPagination(res.data);
                if (investments.length > 0) {
                    selectedAccountId = investments[0].accountId;
                    renderList();
                    loadDetail(investments[0].accountId);
                } else {
                    $("#diDetail").empty().append('<div class="dash-empty">왼쪽에서 계좌를 선택하세요.</div>');
                }
                $("#diLoading").hide();
                $("#diBody").show();
            })
            .fail(function (xhr) {
                if (xhr.status === 401) {
                    return;
                }
                $("#diLoading").hide();
                var message = "목록을 불러오지 못했습니다.";
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                $("#diError").text(message).show();
            });
    }

    $("#diFilterSubmit").on("click", function () {
        loadInvestments(0);
    });
    $("#diFilterReset").on("click", function () {
        $("#diFilterCustomerName").val("");
        $("#diFilterStatus").val("");
        loadInvestments(0);
    });
    $("#diFilterCustomerName").on("keypress", function (e) {
        if (e.which === 13) {
            loadInvestments(0);
        }
    });

    loadInvestments(0);
});
