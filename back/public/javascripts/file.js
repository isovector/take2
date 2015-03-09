frostbite.directive('relatedFiles', function() {
	    return {
			restrict: 'A',
			replace: true,
			templateUrl: "/assets/directives/relatedFiles.partial.html",
			scope : {
				relatedFiles: '=files'
			}
	    }
});
frostbite.directive('expertUsers', function() {
	    return {
			restrict: 'A',
			replace: true,
			templateUrl: "/assets/directives/expertUsers.partial.html",
	        scope : {
	            expertUsers: '=experts'
	        }      

	    }
});
frostbite.directive('expertUsersGraph', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/expertUsersGraph.partial.html",
		scope : {
			expertUsers: '=experts'
		}
    }
});

frostbite.filter('percent', function() {
	return function(input) {
		return (input*100).toFixed(2);
	}
})

frostbite.controller('FileCtrl', ['$scope', '$filter', '$http', '$q', '$interval', function ($scope, $filter, $http, $q, $interval) {
    $scope.lines = []
    $scope.lineItems = []
    $scope.path = ""
    $scope.showFile = true;
    $scope.useFakeData = false;
	$scope.timeoutCount = 0;
	var MAXCOUNT = 5;
	$scope.userChartData = [];			

	$scope.getChart = function() {
		$scope.timeoutCount++;
		$scope.makeUserChart();
	};

	var stopInterval = $interval($scope.getChart, 300);

	var orderBy = $filter('orderBy');
	$scope.sortRelatedFiles = function() {
		$scope.relatedFiles = orderBy($scope.relatedFiles, ['-coefficient', 'filename'], false);
	}

	$scope.sortExperts = function() {
		$scope.expertUsers = orderBy($scope.expertUsers, ['-knowledge', 'name'], false);
	}

    $scope.getRelatedFiles = function (filename) {
		$http.get('/api/coefficients/' + filename).success(function(data) {
			$scope.relatedFiles = data;
			$scope.sortRelatedFiles();
		}).error(function() {
			$scope.relatedFiles = [];	
		});
	}

    $scope.getExpertUsers = function(filename) {
		$http.get('/api/experts/' + filename).success(function(data) {
			$scope.expertUsers = data;
			$scope.sortExperts();
		}).error(function() {
			$scope.expertUsers = [];	
		});
	}

    $scope.init = function () {
        SyntaxHighlighter.all();
        $scope.popupGetter();
    }

    $scope.makeUserChart = function(){
		var ctx = $("#userChart");
		if (ctx == null) {
			return;
		}
		ctx = ctx.get(0);
		if (ctx == null) {
			return;
		}
		if ($scope.userChartData.length != 0 || $scope.timeoutCount > MAXCOUNT) {
			$interval.cancel(stopInterval);
		} else {
			return;
		}

		ctx = ctx.getContext("2d");
        var options = {
            legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%> <%=segments[i].value*5%>s<%}%></li><%}%></ul>",
            segmentShowStroke: true,
            animateRotate: true,
            animationEasing: "easeOutQuart"
        }
        var myNewChart = new Chart(ctx).Pie($scope.userChartData, options);
        var legend = myNewChart.generateLegend();
		if (myNewChart.segments.length == 0) {
			$("#expertUsersGraphdiv").html("<div style=\"height:160px;display:inline-block;\"><span>Time spent in file by users:</span><div style=\"height:100%;\"><span style=\"position:absolute;bottom:20px;\">No user data found.</span></div></div>");
		} else {
			$("#user-legend").html(legend);
		}
    }

    // Checking for popup on page - modify it with appropriate data when it does
    $scope.popupGetter = function () {
        // I hate myself
        var checkExist = setInterval(function () {
            if ($('.popover').length) {    
                clearInterval(checkExist);           
                $scope.makePopup();
                var oldPop = $('.popover').attr("id");
                var stillExists = setInterval(function () {
                    // If we have more than one popup, destroy the earlier one
                    if ($('.popover').length > 1) {
                        $('.popover')[0].remove();
                    };
                    if (!($('.popover').length)) {
                        clearInterval(stillExists);
                        $scope.popupGetter();
                    } else if ($('.popover').attr("id") != oldPop) {
                        oldPop = $('.popover').attr("id");
                        $scope.makePopup();
                    }
                }, 10);
            }
        }, 500);
    }

    var brushMap = {
        "sh" : "bash",
        "cpp": "cpp",
        "cc": "cpp",
        "c": "cpp",
        "cs": "csharp",
        "css": "css",
        "java": "java",
        "js": "js",
        "pl": "perl",
        "txt": "text",
        "py": "python",
        "rb": "rails",
        "scala": "scala",
        "sql": "sql",
        "vb": "vb",
        "xml": "xml"
    };

    function getBrushType(filename){
        var brushType = brushMap[filename.split(".").pop()];
        return typeof(brushType) != "undefined" ? brushType : "plain";
    }

    var entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
      };

      function escapeHtml(string) {
        return String(string).replace(/[&<>"'\/]/g, function (s) {
          return entityMap[s];
        });
      }

    $scope.setFileContents = function (filestuff) {
        // Get our path for breadcrumbs
        // Splitting path based on deliminating char OS uses
        if(filestuff.path.indexOf("/") > -1) {
            $scope.pathArray = filestuff.path.split("/");
        } else if(filestuff.path.indexOf("\\") > -1) {
            $scope.pathArray = filestuff.path.split("\\");
        } else {
            $scope.pathArray == filestuff.path;
        }

        // Set highlight type based on filename ending
        $("#file_brush").attr('class', 'brush: ' + getBrushType(filestuff.name));
        
        //Insert the code content into the page
        $('#file_brush').html(escapeHtml(filestuff.contents));
        $scope.path = filestuff.path;
        $scope.create_data();
        $scope.getExpertUsers(filestuff.path);
        $scope.getRelatedFiles(filestuff.path);
	}

    // Adding chart, and styling the popup (title, arrow, margins)
    $scope.makePopup = function () {
        var popbox = document.getElementById($('.popover')[0].id);
        
        // Chart creation
        var ctx = $("#myChart").get(0).getContext("2d");
       
        // TODO - Change this to finding "number" for extensibility
        var lineSelected = $("[aria-describedby^='popover']");

        lineSelected.css("background-color", "#ffffff")

        var lineNum = parseInt(lineSelected.attr('class').split(/\s+/)[1].substring(6));

        var data = _.find($scope.lineItems, function (lineItem) {
            return lineItem.line == lineNum;
        }).chartData;
        var options = {
            legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%> <%=segments[i].value*5%>s<%}%></li><%}%></ul>",
            segmentShowStroke: true,
            animateRotate: true,
            animationEasing: "easeOutQuart"
        }
        var myNewChart = new Chart(ctx).Pie(data, options);
        var legend = myNewChart.generateLegend();
        $("#legend").html(legend);

        // Popup styling
        if ($(".popover").css("left").substring(0, 1) == "-") {
            $(".popover").css("left", "11px");
        }
        $(".popover-title").html("Users - Line " + lineNum);
        $(".arrow").css("display", "none");

    }

    // Add importance properties to lines (color, hover, popup)
    $scope.add_importance = function () {
        
        var lines = $('.line');
        var colors = [
            '#E9F2FF',
            '#D6E7FF',
            '#C2DBFF',
            '#ADCFFF',
            '#99C4FF',
            '#85B8FF',
            '#70ACFF',
            '#5CA0FF',
            '#4794FF',
            '#3388FF'
        ];
        var maxCount = 0;

        var maxLineCount = (_.max($scope.lineItems, function(lineItem){
            return lineItem.totalCount;
        })).totalCount;
        
        $scope.lineItems.forEach(function (lineItem) {

            if (lineItem.line < lines.length / 2 && lineItem.totalCount > 0) {
               
                var color = 'background-color:';

                color += colors[Math.floor(lineItem.totalCount/maxLineCount * colors.length) - 1];
                color += ' !important';
               
                lines[lineItem.line - 1].style.cssText = color;
                // Highlighting the line on hover
                $(lines[(lineItem.line - 1) + lines.length / 2]).hover(
                    function () {
                        lines[lineItem.line - 1].style.cssText = 'background-color: #FFFF99 !important';
                        lines[(lineItem.line - 1) + lines.length / 2].style.cssText = 'background-color: #FFFF99 !important';
                    }, function () {
                        lines[lineItem.line - 1].style.cssText = color;
                        lines[(lineItem.line - 1) + lines.length / 2].style.cssText = 'background-color: #FFFFFF !important';
                    });

                $(lines[lineItem.line - 1 + lines.length / 2]).popover({
                    trigger: 'click',
                    html: true,
                    title: 'Users',
                    content: '<canvas id="myChart" width="200px" height="200px"/>'+
                        '<br/>'+
                        '<div id="legend" width="200px" height="200px" style="list-style: none;">' +
                           'Loading...' +
                        '</div>' +
                        '<a href="/symbol/' + lineItem.symbolId + '" >' +
                            'See related to this section'+
                        '</a>',
                    container: 'body',
                    placement: 'left',
                    animation: false
                });

                $(lines[lineItem.line - 1 + lines.length / 2]).attr('symbolId', lineItem.symbolId);
            }
        });
    }

    $scope.add_user = function (userData, color) {
        var chartItem = {
            label: userData.user.name,
            value: userData.timeSpent,
            color: color,
            highlight: color
        }
        $scope.userChartData.push(chartItem);
    }

    // Parse data and attach user data to lines of code
    $scope.add_lines = function (userData, color) {
        var userColor = color;

        userData.timeSpentByLine.forEach(function (lineItem) {
            var symbolToFill = _.find($scope.symbolArray, function(symbolObject) {
                return symbolObject.line == lineItem.line;
            });

            var tempObj;
            var lineIndex;
            // Make a copy so we can modify the line when we iterate
            var lineItemForIteration = lineItem;
            symbolId = _.find($scope.symbolArray, function(symbol) {
                return symbol.line == lineItem.line; 
            }).id;
            while (lineItemForIteration.line != symbolToFill.endLine + 1) {
                if ((lineIndex = _.findIndex($scope.lines, { line: lineItemForIteration.line })) != -1) {
                    // Preparing line to be modified if line already exists
                    tempObj = $scope.lines[lineIndex];
                    $scope.lines[lineIndex] = $scope.lines[$scope.lines.length - 1];
                    $scope.lines[$scope.lines.length - 1] = tempObj;

                } else {
                    // Creating new line object (only do if line doesn't exist, otherwise get a handle on it...)
                    var tempObj = new Object();
                    tempObj.line = lineItemForIteration.line;
                    tempObj.users = [];
                    tempObj.symbolId = symbolId;
                    $scope.lines.push(tempObj);
                }
                // Creating new user object and pushing on to appropriate line object
                tempObj = new Object();
                tempObj.userId = userData.user.id;
                tempObj.userName = userData.user.name;
                tempObj.count = lineItemForIteration.count;
                tempObj.color = userColor;
                $scope.lines[$scope.lines.length - 1].users.push(tempObj);

                //Increment our line by 1 until we reach the end of the symbol
                lineItemForIteration.line += 1;
            }
            
        });

        // Add chart data and totalCount properties to the lines
        $scope.lineItems = _.map($scope.lines, function (lineItem) {
            var totalCount = 0;
            var chartData = [];
            _.forEach(lineItem.users, function (user) {
                totalCount += user.count;
                var chartItem = {
                    label: user.userName,
                    value: user.count,
                    color: user.color,
                    highlight: user.color
                }
                chartData.push(chartItem);
            })
            lineItem["totalCount"] = totalCount;
            lineItem["chartData"] = chartData;
            return lineItem;
        });

        console.log($scope.lineItems);
        // Call the importance maker
        $scope.add_importance();
        return userColor;
    }

    $scope.add_symbols = function (symbolArray) {
        $scope.symbolArray = _.sortBy(symbolArray, function(symbol) {
            return symbol.line;
        });
        var lines = $('.line');
        var lastLine = lines.length / 2
        // Iterate through the symbols to find the end of each symbol
        // start at 1 because we will look behind us, and the first symbol has nothing preceding it
        for (var i = 1; i <= symbolArray.length - 1; i++) {
            // Set the end to the start of the next one minus 1
            $scope.symbolArray[i-1].endLine = $scope.symbolArray[i].line - 1;
            // Last symbol ends at end of file, but we dont know how long so use -1
            if (i == symbolArray.length -1) {
                $scope.symbolArray[i].endLine = lastLine;
            };
        };
    }
    
    
    // Get data and send to parser
    $scope.create_data = function () {
        $http.get("/api/metrics/all/" + $scope.path).success(function (data) {
            // Make the user colors array
            var colors = [];
            if(data.userData.length == 1){
                colors.push(
                    Please.make_color({
                        colors_returned: data.userData.length,
                        scheme_type: 'analogous'
                    })
                );
            } else {
	            colors = Please.make_color({
		            colors_returned: data.userData.length,
		            scheme_type: 'analogous'
		        });
	        }
			// Add the symbols so we can mark sections as the same
            $scope.add_symbols(data.symbols);  

            for (var i = 0; i < data.userData.length; i++) {
                $scope.add_lines(data.userData[i], colors[i]);
                $scope.add_user(data.userData[i], colors[i]);
            }
        }).error(function (data) {
           
        });
    }
    $scope.init();
}]);
