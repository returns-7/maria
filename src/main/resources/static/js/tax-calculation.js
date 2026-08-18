$(function () {
    var BENEFIT_LABEL = {
        possible: "가능",
        reduced: "일부감액",
        impossible: "배제"
    };

    var KRW_FORMATTER = new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 0 });
    var DATETIME_FORMATTER = new Intl.DateTimeFormat("ko-KR", {
        year: "numeric", month: "2-digit", day: "2-digit",
        hour: "2-digit", minute: "2-digit"
    });

    var accountMap = {};
    var snapshotByAccountId = {};
    var allSnapshots = [];
    var currentPage = 1;
    var PAGE_SIZE = 20;
    var searchKeyword = "";

    function escapeHtml(value) {
        return $("<div>").text(value == null ? "" : value).html();
    }

    function formatAmount(amount) {
        return "₩" + KRW_FORMATTER.format(amount || 0);
    }

    function formatDateTime(isoString) {
        return isoString ? DATETIME_FORMATTER.format(new Date(isoString)) : "-";
    }

    function formatRatio(ratio) {
        return ratio == null ? "-" : (Number(ratio) * 100).toFixed(1) + "%";
    }

    function showError(message) {
        $("#taxLoading").hide();
        $("#taxError").text(message).show();
    }

    function canTriggerBatch() {
        var admin = MARIA.auth.currentAdmin();
        return !!admin && (admin.role === "ADMIN" || admin.role === "SETTLEMENT");
    }

    function totalPagesOf(list) {
        return Math.max(1, Math.ceil(list.length / PAGE_SIZE));
    }

    function matchesSearch(snap) {
        if (!searchKeyword) {
            return true;
        }
        var account = accountMap[snap.accountId] || {};
        var haystack = ((account.accountNo || "") + " " + (account.customerName || "")).toLowerCase();
        return haystack.indexOf(searchKeyword) !== -1;
    }

    function filteredSnapshots() {
        return allSnapshots.filter(matchesSearch);
    }

    function renderKpi() {
        var taxable = allSnapshots.filter(function (s) { return (s.finalTax || 0) > 0; });
        var reduced = allSnapshots.filter(function (s) {
            var benefit = ((accountMap[s.accountId] || {}).benefit || "").toLowerCase();
            return benefit === "reduced" || benefit === "impossible";
        });
        var taxSum = taxable.reduce(function (sum, s) { return sum + Number(s.finalTax || 0); }, 0);

        $("#kpiTotal").text(allSnapshots.length + "건");
        $("#kpiTaxable").text(taxable.length + "건");
        $("#kpiReduced").text(reduced.length + "건");
        $("#kpiTaxSum").text(formatAmount(taxSum));
    }

    function renderSnapshots(snapshots) {
        // 최종세액 내림차순: 확정산 안 된 계좌(값 0)가 1페이지를 뒤덮어 "다 이상하다"로
        // 보이는 걸 막는다. 실제 계산값이 있는 계좌가 먼저 보인다.
        allSnapshots = (snapshots || []).slice().sort(function (a, b) {
            return (b.finalTax || 0) - (a.finalTax || 0);
        });
        snapshotByAccountId = {};
        allSnapshots.forEach(function (s) { snapshotByAccountId[s.accountId] = s; });
        renderKpi();
        currentPage = Math.min(currentPage, totalPagesOf(filteredSnapshots()));
        renderPage();
    }

    function renderPage() {
        var $body = $("#taxSnapshotBody").empty();
        var visible = filteredSnapshots();
        $("#taxSnapshotCount").text(visible.length + "건" + (searchKeyword ? " (전체 " + allSnapshots.length + "건 중)" : ""));

        if (visible.length === 0) {
            $body.append('<tr><td colspan="8" class="tax-empty">' + (searchKeyword ? "검색 결과가 없습니다." : "세액 스냅샷이 없습니다.") + '</td></tr>');
            $("#taxPagination").hide();
            return;
        }

        var totalPages = totalPagesOf(visible);
        var startIndex = (currentPage - 1) * PAGE_SIZE;
        var pageSnapshots = visible.slice(startIndex, startIndex + PAGE_SIZE);

        pageSnapshots.forEach(function (snap) {
            var account = accountMap[snap.accountId] || {};
            var benefitKey = (account.benefit || "").toLowerCase();
            var row =
                "<tr data-account-id=\"" + snap.accountId + "\">" +
                "<td><div class=\"account-no\">" + escapeHtml(account.accountNo || "-") + "</div>" +
                "<div class=\"account-name\">" + escapeHtml(account.customerName || "") + "</div></td>" +
                "<td><span class=\"status-badge " + escapeHtml(benefitKey) + "\">" +
                (BENEFIT_LABEL[benefitKey] || "-") + "</span></td>" +
                "<td>" + formatAmount(snap.weightedSell) + "</td>" +
                "<td>" + formatAmount(snap.weightedGain) + "</td>" +
                "<td>" + formatRatio(snap.adjustRatio) + "</td>" +
                "<td>" + formatAmount(snap.finalDeduction) + "</td>" +
                "<td>" + formatAmount(snap.finalTax) + "</td>" +
                "<td>" + formatDateTime(snap.calculatedAt) + "</td>" +
                "</tr>";
            $body.append(row);
        });

        $("#taxPageInfo").text(currentPage + " / " + totalPages);
        $("#previousTaxPage").prop("disabled", currentPage === 1);
        $("#nextTaxPage").prop("disabled", currentPage === totalPages);
        $("#taxPagination").css("display", "flex");
    }

    function openDetail(accountId) {
        var account = accountMap[accountId] || {};
        $("#detailAccountNo").text("불러오는 중...");

        MARIA.auth.ajax({
            url: "/api/tax/preview/" + accountId,
            method: "GET"
        }).done(function (res) {
            var result = res.data.taxCalculationResultDTO;
            $("#detailAccountNo").text((account.accountNo || "-") + " · " + (account.customerName || ""));
            $("#detailDescription").text("계산 흐름 (§3 공식 5단계) — 방금 다시 계산됨");
            $("#detailWeightedSell").text(formatAmount(result.weightedSell));
            $("#detailWeightedGain").text(formatAmount(result.weightedGain));
            $("#detailExternalAmount").text(formatAmount(result.weightedExternalAmount));
            $("#detailAdjustRatio").text(formatRatio(result.adjustRatio));
            $("#detailFinalDeduction").text(formatAmount(result.finalDeduction));
            $("#detailOriginalGain").text(formatAmount(result.originalGainAmount));
            $("#detailFinalTax").text(formatAmount(result.finalTax));
            $("#detailAsOf").text($("#clockValue").text() || "-");

            var snapshot = snapshotByAccountId[accountId];
            var isStale = snapshot && Number(snapshot.finalTax || 0) !== Number(result.finalTax || 0);
            $("#detailStaleBadge").toggle(!!isStale);
        }).fail(function (xhr) {
            if (xhr.status === 401) return;
            var message = (xhr.responseJSON && xhr.responseJSON.message) || "세액 미리보기를 불러오지 못했습니다.";
            MARIA.ui.showError(message);
        });
    }

    function loadTax() {
        MARIA.auth.ajax({ url: "/api/account/list", method: "GET" })
            .done(function (accountRes) {
                var accounts = accountRes.data || [];
                var accountIds = [];
                accounts.forEach(function (account) {
                    accountMap[account.accountId] = account;
                    accountIds.push(account.accountId);
                });

                if (accountIds.length === 0) {
                    renderSnapshots([]);
                    $("#taxLoading").hide();
                    $("#taxBody").show();
                    return;
                }

                // ponytail: URL 쿼리스트링 길이 한도(414/400) 때문에 청크 분할. accountIds가
                // 계속 커지면 백엔드에 POST 방식 조회 API를 새로 만드는 쪽으로 바꿀 것.
                var CHUNK_SIZE = 200;
                var chunks = [];
                for (var i = 0; i < accountIds.length; i += CHUNK_SIZE) {
                    chunks.push(accountIds.slice(i, i + CHUNK_SIZE));
                }

                $.when.apply($, chunks.map(function (chunk) {
                    return MARIA.auth.ajax({
                        url: "/api/tax/snapshots",
                        method: "GET",
                        data: { accountIds: chunk },
                        traditional: true
                    });
                })).done(function () {
                    var results = chunks.length === 1 ? [arguments] : arguments;
                    var snapshots = [];
                    $.each(results, function (_, result) {
                        snapshots = snapshots.concat(result[0].data || []);
                    });
                    renderSnapshots(snapshots);
                    $("#taxLoading").hide();
                    $("#taxBody").show();
                }).fail(function (xhr) {
                    if (xhr && xhr.status === 401) return;
                    showError((xhr && xhr.responseJSON && xhr.responseJSON.message) || "세액 스냅샷을 불러오지 못했습니다.");
                });
            })
            .fail(function (xhr) {
                if (xhr.status === 401) return;
                showError((xhr.responseJSON && xhr.responseJSON.message) || "계좌 목록을 불러오지 못했습니다.");
            });
    }

    $("#taxSnapshotBody").on("click", "tr[data-account-id]", function () {
        openDetail($(this).data("account-id"));
    });

    $("#taxSearchInput").on("input", function () {
        searchKeyword = $(this).val().trim().toLowerCase();
        currentPage = 1;
        renderPage();
    });

    $("#previousTaxPage").on("click", function () {
        if (currentPage > 1) {
            currentPage -= 1;
            renderPage();
        }
    });

    $("#nextTaxPage").on("click", function () {
        if (currentPage < totalPagesOf(filteredSnapshots())) {
            currentPage += 1;
            renderPage();
        }
    });

    $("#triggerBatch").toggle(canTriggerBatch()).on("click", function () {
        if (!canTriggerBatch()) return;
        MARIA.auth.ajax({ url: "/api/tax/snapshots/jobs", method: "POST" })
            .done(function (res) {
                $("#batchTriggerResult").text("요청됨 · runId " + res.data.runId + " · " + res.data.status);
            })
            .fail(function (xhr) {
                if (xhr.status === 401) return;
                MARIA.ui.showError((xhr.responseJSON && xhr.responseJSON.message) || "배치 실행 요청에 실패했습니다.");
            });
    });

    loadTax();
});
