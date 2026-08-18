$(function () {
    var SELL_ORDER_STATUS_LABEL = {
        RECEIVED: "접수",
        EXECUTED: "체결",
        REJECTED: "거부"
    };

    var KRW_FORMATTER = new Intl.NumberFormat("ko-KR");
    var QTY_FORMATTER = new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 4 });
    var DATETIME_FORMATTER = new Intl.DateTimeFormat("ko-KR", {
        year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit"
    });

    var PAGE_SIZE = 20;
    var currentPage = 0;
    var totalPages = 1;

    function escapeHtml(value) {
        return $("<div>").text(value == null ? "" : value).html();
    }

    function formatAmount(amount) {
        return amount == null ? "-" : "₩" + KRW_FORMATTER.format(amount);
    }

    function formatQty(qty) {
        return qty == null ? "-" : QTY_FORMATTER.format(qty) + "주";
    }

    function formatDateTime(value) {
        return value ? DATETIME_FORMATTER.format(new Date(value)) : "-";
    }

    function sellOrderStatusLabel(status) {
        return SELL_ORDER_STATUS_LABEL[status] || status || "-";
    }

    function statusClassOf(status) {
        return (status || "").toLowerCase();
    }

    function showError(message) {
        $("#sellOrderError").text(message).show();
    }

    function hideError() {
        $("#sellOrderError").hide();
    }

    function buildFilterParams() {
        var params = { page: currentPage, size: PAGE_SIZE };
        var keyword = $("#sellOrderFilterKeyword").val().trim();
        var status = $("#sellOrderFilterStatus").val();
        var startDate = $("#sellOrderFilterStartDate").val();
        var endDate = $("#sellOrderFilterEndDate").val();
        if (keyword) {
            params.keyword = keyword;
        }
        if (status) {
            params.status = status;
        }
        if (startDate) {
            params.startDate = startDate;
        }
        if (endDate) {
            params.endDate = endDate;
        }
        return params;
    }

    function renderRows(orders) {
        var $body = $("#sellOrderListBody").empty();

        if (!orders.length) {
            $body.append('<tr><td colspan="8" class="sellorder-empty">매도 · 환전 내역이 없습니다.</td></tr>');
            return;
        }

        orders.forEach(function (order) {
            var productLabel = order.ticker
                ? escapeHtml(order.ticker) + (order.name ? " · " + escapeHtml(order.name) : "")
                : "-";
            var row =
                "<tr>" +
                '<td class="sellorder-ellipsis"><div class="sellorder-account-no">' + escapeHtml(order.accountNo || "-") + "</div>" +
                '<div class="sellorder-account-name">' + escapeHtml(order.customerName || "") + "</div></td>" +
                '<td class="sellorder-ellipsis">' + productLabel + "</td>" +
                '<td class="sellorder-amount">' + formatQty(order.sellQty) + "</td>" +
                '<td class="sellorder-amount">' + formatAmount(order.basePrice) + "</td>" +
                "<td>" + formatDateTime(order.processedAt) + "</td>" +
                '<td class="sellorder-amount">' + formatAmount(order.provisionalAmount) + "</td>" +
                '<td class="sellorder-amount">' + formatAmount(order.finalAmount) + "</td>" +
                "<td><span class=\"sellorder-status-badge " + statusClassOf(order.status) + '">' +
                escapeHtml(sellOrderStatusLabel(order.status)) + "</span></td>" +
                "</tr>";
            $body.append(row);
        });
    }

    function renderPagination() {
        var $pagination = $("#sellOrderPagination").empty();
        if (totalPages <= 1) {
            return;
        }

        var BLOCK_SIZE = 10;
        var blockStart = Math.floor(currentPage / BLOCK_SIZE) * BLOCK_SIZE;
        var blockEnd = Math.min(totalPages - 1, blockStart + BLOCK_SIZE - 1);

        function addButton(label, targetPage, isDisabled, isActive) {
            var classes = "page-btn" + (isActive ? " active" : "");
            var $btn = $('<button type="button" class="' + classes + '">' + label + "</button>");
            $btn.prop("disabled", isDisabled || isActive);
            if (!isDisabled && !isActive) {
                $btn.on("click", function () {
                    currentPage = targetPage;
                    loadSellOrders();
                });
            }
            $pagination.append($btn);
        }

        addButton("이전", blockStart - 1, blockStart === 0, false);
        for (var i = blockStart; i <= blockEnd; i++) {
            addButton(String(i + 1), i, false, i === currentPage);
        }
        addButton("다음", blockEnd + 1, blockEnd === totalPages - 1, false);
    }

    function loadSellOrders() {
        $("#sellOrderListBody").html('<tr><td colspan="8" class="sellorder-loading">불러오는 중...</td></tr>');
        hideError();

        MARIA.auth.ajax({
            url: "/api/sell-orders/history",
            method: "GET",
            data: buildFilterParams()
        })
            .done(function (res) {
                var page = res.data || { content: [], totalCount: 0 };
                var totalCount = page.totalCount || 0;
                totalPages = Math.max(1, Math.ceil(totalCount / PAGE_SIZE));
                $("#sellOrderCount").text(totalCount + "건");
                renderRows(page.content || []);
                renderPagination();
            })
            .fail(function (xhr) {
                if (xhr.status === 401) {
                    return;
                }
                $("#sellOrderListBody").empty();
                showError((xhr.responseJSON && xhr.responseJSON.message) || "매도 · 환전 내역을 불러오지 못했습니다.");
            });
    }

    $("#sellOrderFilterSearch").on("click", function () {
        currentPage = 0;
        loadSellOrders();
    });

    $("#sellOrderFilterKeyword").on("keydown", function (event) {
        if (event.key === "Enter") {
            currentPage = 0;
            loadSellOrders();
        }
    });

    $("#sellOrderFilterStatus").on("change", function () {
        currentPage = 0;
        loadSellOrders();
    });

    $("#sellOrderFilterReset").on("click", function () {
        $("#sellOrderFilterKeyword").val("");
        $("#sellOrderFilterStatus").val("");
        $("#sellOrderFilterStartDate").val("");
        $("#sellOrderFilterEndDate").val("");
        currentPage = 0;
        loadSellOrders();
    });

    loadSellOrders();
});
