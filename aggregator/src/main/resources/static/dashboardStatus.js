/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


function updateStatus() {
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
        url: "/status",
    	success: function(data) {
    		var jobsJson = data.jobs;
            var resultsBody = "";
            for (var i = 0; i < jobsJson.length; i++) { 
            	resultsBody += createResultsRow(jobsJson[i]);
            }
            
        	document.getElementById("results_body").innerHTML = resultsBody
        	
        	if (data.connected) {
        		document.getElementById("solace_connection").innerHTML = "Yes";
        	} else {
        		document.getElementById("solace_connection").innerHTML = "No";
        	}
        },
        error: function(xhr, status, error) {
        	console.log(xhr.responseText);
        }
    });
}

function getSummary() {
	$.ajax({
        url: "/stats/global",
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
        url: "/stats/global",
        type: 'DELETE'
    }).then(function(data, status, jqxhr) {
    	console.log(jqxhr);
    });
}

function clearComplete() {
	$.ajax({
        url: "/jobs",
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
        url: "/jobs",
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






