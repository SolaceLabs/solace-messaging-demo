
function updateStatus() {
    var d = new Date();
    var t = d.toLocaleTimeString();
    document.getElementById("time").innerHTML = t;

	getJobResults();
	getSummary();
}

function createResultsRow(job) {
	var htmlOutput = "<tr>";
	htmlOutput += "<td>" + job.jobStatus.jobId + "</td>";
	htmlOutput += "<td>" + job.jobStatus.workCount + "</td>";
	htmlOutput += "<td>" + job.jobStatus.rate + "</td>";
	htmlOutput += "<td>" + job.jobStatus.delayMin + "</td>";
	htmlOutput += "<td>" + job.jobStatus.delayMax + "</td>";
	
	htmlOutput += "<td>&nbsp;</td>";
	
	if (job.jobStatus.isComplete) {
		htmlOutput += "<td>Complete</td>";
	} else {
		htmlOutput += "<td>In Progress</td>";
	}
	htmlOutput += "<td>" + job.requests.numMsgsSent + "</td>";
	htmlOutput += "<td>" + job.requests.numPubAcksOk + "</td>";
	htmlOutput += "<td>" + job.requests.numPubAckErrors + "</td>";
	htmlOutput += "<td>" + job.requests.numPubAckOutstanding + "</td>";
	htmlOutput += "<td>" + job.responses.numResponseOk + "</td>";
	htmlOutput += "<td>" + job.responses.numResponseErrors + "</td>";
	htmlOutput += "<td>" + job.responses.numResponsesOutstanding + "</td>";
	htmlOutput += "<td>" + parseFloat(Math.round(job.responses.respPerSec * 100) / 100).toFixed(2); +  "</td>";
	
	// Display in seconds with 3 decimals. Receive in usec
	htmlOutput += "<td>" + Number(job.latency.min / 1000000).toFixed(3) + "</td>";
	htmlOutput += "<td>" + Number(job.latency.avg / 1000000).toFixed(3) + "</td>";
	htmlOutput += "<td>" + Number(job.latency.perc95th / 1000000).toFixed(3) + "</td>";
	htmlOutput += "<td>" + Number(job.latency.perc99th / 1000000).toFixed(3) + "</td>";
	htmlOutput += "<td>" + Number(job.latency.max / 1000000).toFixed(3) + "</td>";
	htmlOutput += "<td>" + Number(job.latency.stdDev / 1000000).toFixed(3)  + "</td>";
	
	htmlOutput += "</tr>";
	return htmlOutput;
}

function getJobResults() {
	$.ajax({
        url: "/v1/workerOffloadDemo/stats/jobs",
    	success: function(data) {
    		var jobsJson = data.jobs;
            var resultsBody = "";
            for (var i = 0; i < jobsJson.length; i++) { 
            	resultsBody += createResultsRow(jobsJson[i]);
            }
            
        	document.getElementById("results_body").innerHTML = resultsBody
        },
        error: function(xhr, status, error) {
        	console.log(xhr.responseText);
        }
    });
}

function getSummary() {
	$.ajax({
        url: "/v1/workerOffloadDemo/stats/global",
        success: function(data) {
        	document.getElementById("total_jobs").innerHTML = data.totalJobs;
            document.getElementById("finished_jobs").innerHTML = data.finishedJobs;
            document.getElementById("total_requests_sent").innerHTML = data.workRequestsSent;
            document.getElementById("total_responses_received").innerHTML = data.workResponsesRecv;
        },
        error: function(xhr, status, error) {
        	console.log(xhr.responseText);
        }
    });
}

function resetSummary() {
	$.ajax({
        url: "/v1/workerOffloadDemo/stats/global",
        type: 'DELETE'
    }).then(function(data, status, jqxhr) {
    	console.log(jqxhr);
    });
}

function clearComplete() {
	$.ajax({
        url: "/v1/workerOffloadDemo/jobs",
        type: 'DELETE'
    }).then(function(data, status, jqxhr) {
    	console.log(jqxhr);
    });
}

function addJob() {
    var jobInfo = {
    	id : $("#job-id").val(),
    	jobCount : $("#work-count").val(),
    	rateInMsgPerSec : $("#send-rate").val(),
    	minWorkDelayInMSec : $("#delay-min").val(),
    	maxWorkDelayInMSec : $("#delay-max").val()
    };
    
    $.ajax({
        url: "/v1/workerOffloadDemo/jobs",
        contentType: "application/json; charset=utf-8",
        type: 'post',
        dataType: "json",
        data: JSON.stringify(jobInfo),
        success: function () {
        	console.log("Successfully submitted job: " + jobInfo)
        },
        error: function(xhr, status, error) {
        	console.log(xhr.responseText);
	    	console.log("Job Info of failure: " + JSON.stringify(jobInfo, null, 2));
	    }
        	
    });
}






