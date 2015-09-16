/**
 * Proposes a job name.
 */
function proposeJobName(clazz) {
    if (clazz == '' && clazz.indexOf('.') == -1) {
        $('#nameProposal').text("");

        return;
    }

    var name = clazz.substring(clazz.lastIndexOf('.') + 1, clazz.length);

    $('#nameProposal').text("maybe " + name + " ?");
}

/**
 * Displays description and job arguments for given class
 */
onJobSelected = function () {
    var clazz = $("#clazz").val();

    proposeJobName(clazz);

    if (clazz == null) {
        $("#arguments").empty();

        return;
    }

    $.getJSON(SERVICE_URL, {"className": clazz}, function (job) {
        $("#description").text(job.description);
        $("#disallowConcurrentExecution").text(job.disallowConcurrentExecution);
        $("#persistJobDataAfterExecution").text(job.persistJobDataAfterExecution);

        var htmlBuilder = "";
        $(job.arguments).each(function (index, argument) {
            htmlBuilder += "<tr>";
            if (argument.required) {
                htmlBuilder += '<td style="text-align: center;"><span style="font-weight:bold;" >' + argument.name + '*</span></td>';
            }
            else {
                htmlBuilder += '<td style="text-align: center;">' + argument.name + "</td>";
            }
            htmlBuilder += "<td>" + argument.description + "</td>";
            htmlBuilder += "<td>";
            $(argument.sampleValues).each(function (i, sample) {
                if (i > 0) {
                    htmlBuilder += "<br>";
                }
                htmlBuilder += sample;
            });
            htmlBuilder += "</td>"
            htmlBuilder += "</tr>";
        });

        $("#arguments").html(htmlBuilder);
    });
}

showLogs = function (executionId) {
    $.getJSON(LOGS_SERVICE_URL, {"executionId": executionId}, function (page) {
        $("#logs-" + executionId + "-link").hide();
        $("#logs-" + executionId).show();

        var htmlBuilder = "";

        $(page.items).each(function (index, log) {
            htmlBuilder += log.formattedDate;
            htmlBuilder += " ";
            htmlBuilder += "<span class=\"" + log.level + "\">" + log.level + "</span>";
            htmlBuilder += " ";
            htmlBuilder += log.message;

            if (log.stackTrace != null) {
                htmlBuilder += " ";
                htmlBuilder += "<a href=\"#\" onclick=\"viewStackTrace(" + executionId + "," + index + ");return false;\">view stacktrace</a>";
                htmlBuilder += "<span id=\"logs-" + executionId + "-" + index + "-stacktrace\" style=\"display:none;\">" + log.formattedStackTrace + "</span>";
            }

            htmlBuilder += "<br>";
        });

        if (page.items.length < page.totalCount) {
            htmlBuilder += "...<br><a href=\"#\" onclick=\"viewLogs(" + executionId + ", 1);return false;\">view all</a>";
        }

        $("#logs-" + executionId).html(htmlBuilder);
    });
}

viewStackTrace = function (executionId, index) {
    var stackTrace = $("#logs-" + executionId + "-" + index + "-stacktrace").html();

    var popup = window.open('', 'stacktrace-' + executionId + "-" + index, 'width=1200,height=800');

    popup.document.documentElement.innerHTML = stackTrace;
}

viewLogs = function (executionId, pageIndex) {
    window.open('/glass/traces/' + executionId, 'logs-' + executionId, 'width=1200,height=800');
}

