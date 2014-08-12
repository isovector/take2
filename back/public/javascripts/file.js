frostbite.controller('FileCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    $scope.lines = []
    $scope.lineItems = []
    $scope.path = ""
    $scope.showFile = true;

    $scope.init = function () {
        SyntaxHighlighter.all();
        $scope.popupGetter();
    }

    // Checking for popup on page - modify it with appropriate data when it does
    $scope.popupGetter = function () {
        // I hate myself
        var checkExist = setInterval(function () {
            if ($('.popover').length) {
                $scope.makePopup();
                clearInterval(checkExist);
                var stillExists = setInterval(function () {
                    if (!($('.popover').length)) {
                        clearInterval(stillExists);
                        $scope.popupGetter();
                    }
                }, 10);

            }
        }, 1000);
    }

    $scope.setFileContents = function (filestuff) {
        console.log(filestuff);
        //Get our path for breadcrumbs
        $scope.pathArray = filestuff.path.split("/");

        //Insert the code content into the page
        $('#file_brush').html(filestuff.contents);
        $scope.path = filestuff.path;
        console.log(filestuff.path);
        console.log($scope.path);
        $scope.create_data();
    }

    // Adding chart, and styling the popup (title, arrow, margins)
    $scope.makePopup = function (el) {

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
        console.log(data)
        var options = {
            legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%> <%=segments[i].value%><%}%></li><%}%></ul>",
            segmentShowStroke: true
        }
        var myNewChart = new Chart(ctx).Pie(data, options);
        var legend = myNewChart.generateLegend();
        $("#legend").html(legend);

        // Popup styling
        console.log($(".popover").css("left"))
        if ($(".popover").css("left").substring(0, 1) == "-") {
            console.log("farout"); $(".popover").css("left", "11px");
        }
        $(".popover-title").html("Users - Line " + lineNum);
        $(".arrow").css("display", "none");

    }

    // Add importance properties to lines (color, hover, popup)
    $scope.add_importance = function () {
        
        var lines = $('.line');
        $scope.lineItems.forEach(function (lineItem) {
            
            if (lineItem.line < lines.length / 2 && lineItem.totalCount > 0) {
               
                console.log(lineItem.line);
                console.log(lineItem.totalCount)
                var color = 'background-color:';
                //Currently modifying color after page load, hence the modifying of style element
                //If we know importance on page load, revert to modifying css class name

                if (lineItem.totalCount > 2) {
                    color += '#F37E91';
                }
                else if (lineItem.totalCount > 1) {
                    color += '#F9A2B0';
                }
                else if (lineItem.totalCount > 0) {
                    color += '#FDDAE0';
                }

                color += ' !important';
               
                lines[lineItem.line - 1].style.cssText = color;
                lines[(lineItem.line - 1) + lines.length / 2].style.cssText = color;

                // Highlighting the line on hover
                $(lines[(lineItem.line - 1) + lines.length / 2]).hover(
                    function () {
                        lines[lineItem.line - 1].style.cssText = 'background-color: #FFFF99 !important';
                        lines[(lineItem.line - 1) + lines.length / 2].style.cssText = 'background-color: #FFFF99 !important';
                    }, function () {
                        lines[lineItem.line - 1].style.cssText = color;
                        lines[(lineItem.line - 1) + lines.length / 2].style.cssText = color;
                    });

                // Creating popup to be displayed on hover
                $(lines[lineItem.line - 1 + lines.length / 2]).popover({
                    trigger: 'hover',
                    html: true,
                    title: 'Users',
                    content: '<canvas id="myChart" width="200px" height="200px"/><br/><div id="legend" width="200px" height="200px" style="list-style: none;">Loading...</div>',

                    container: 'body',
                    placement: 'left',
                    animation: false

                });

            }
        });
    }

    $scope.rainbow = function(numOfSteps, step) {
        // This function generates vibrant, "evenly spaced" colours (i.e. no clustering). This is ideal for creating easily distinguishable vibrant markers in Google Maps and other apps.
        // Adam Cole, 2011-Sept-14
        // HSV to RBG adapted from: http://mjijackson.com/2008/02/rgb-to-hsl-and-rgb-to-hsv-color-model-conversion-algorithms-in-javascript
        var r, g, b;
        var h = step / numOfSteps;
        var i = ~~(h * 6);
        var f = h * 6 - i;
        var q = 1 - f;
        switch (i % 6) {
            case 0: r = 1, g = f, b = 0; break;
            case 1: r = q, g = 1, b = 0; break;
            case 2: r = 0, g = 1, b = f; break;
            case 3: r = 0, g = q, b = 1; break;
            case 4: r = f, g = 0, b = 1; break;
            case 5: r = 1, g = 0, b = q; break;
        }
        var c = "#" + ("00" + (~ ~(r * 255)).toString(16)).slice(-2) + ("00" + (~ ~(g * 255)).toString(16)).slice(-2) + ("00" + (~ ~(b * 255)).toString(16)).slice(-2);
        return (c);
    }

    // Parse data and attach user data to lines of code
    $scope.add_lines = function (userData, index, length) {
        console.log("into add lines")
        console.log(userData);

        var userColor = $scope.rainbow(length, index);

        userData.timeSpentByLine.forEach(function (lineItem) {
            console.log(lineItem);
            var tempObj;
            var lineIndex;

            if ((lineIndex = _.findIndex($scope.lines, { line: lineItem.line })) != -1) {
                // Preparing line to be modified if line already exists
                tempObj = $scope.lines[lineIndex];
                $scope.lines[lineIndex] = $scope.lines[$scope.lines.length - 1];
                $scope.lines[$scope.lines.length - 1] = tempObj;

            } else {
                // Creating new line object (only do if line doesn't exist, otherwise get a handle on it...)
                var tempObj = new Object();
                tempObj.line = lineItem.line;
                tempObj.users = [];
                $scope.lines.push(tempObj);
            }
            // Creating new user object and pushing on to appropriate line object
            tempObj = new Object();
            tempObj.userId = userData.user.id;
            tempObj.userName = userData.user.name;
            tempObj.count = lineItem.count;
            tempObj.color = userColor;
            $scope.lines[$scope.lines.length - 1].users.push(tempObj);
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
                    highlight: "#5AD3D1"
                }
                chartData.push(chartItem);
            })
            lineItem["totalCount"] = totalCount;
            lineItem["chartData"] = chartData;
            return lineItem;
        });

        // Call the importance maker
        $scope.add_importance();
    }
    
    
    // Get data and send to parser
    $scope.create_data = function () {
        $http.get("/api/metrics/all/" + $scope.path).success(function (data) {
            // Fake it til you make it
            if (data.userData.length == 0) {
                data = { "file": "back/app/controllers/SnapshotController.scala", "commit": "unimplemented", "userData": [{ "user": { "id": 1, "name": "Sandy Maguire", "email": "sandy@sandymaguire.me", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 2, "timeSpentByLine": [{ "line": 138, "count": 1 }, { "line": 88, "count": 2 }, { "line": 170, "count": 1 }, { "line": 120, "count": 2 }, { "line": 10, "count": 1 }, { "line": 56, "count": 2 }, { "line": 142, "count": 1 }, { "line": 174, "count": 1 }, { "line": 42, "count": 1 }, { "line": 24, "count": 1 }, { "line": 52, "count": 1 }, { "line": 14, "count": 1 }, { "line": 110, "count": 2 }, { "line": 20, "count": 1 }, { "line": 46, "count": 1 }, { "line": 152, "count": 1 }, { "line": 78, "count": 2 }, { "line": 164, "count": 1 }, { "line": 106, "count": 2 }, { "line": 84, "count": 2 }, { "line": 132, "count": 1 }, { "line": 116, "count": 2 }, { "line": 74, "count": 2 }, { "line": 6, "count": 1 }, { "line": 60, "count": 2 }, { "line": 102, "count": 2 }, { "line": 28, "count": 1 }, { "line": 38, "count": 1 }, { "line": 160, "count": 1 }, { "line": 70, "count": 2 }, { "line": 92, "count": 2 }, { "line": 156, "count": 1 }, { "line": 124, "count": 2 }, { "line": 96, "count": 2 }, { "line": 134, "count": 1 }, { "line": 128, "count": 1 }, { "line": 2, "count": 1 }, { "line": 166, "count": 1 }, { "line": 32, "count": 1 }, { "line": 34, "count": 1 }, { "line": 148, "count": 1 }, { "line": 64, "count": 2 }, { "line": 22, "count": 1 }, { "line": 44, "count": 1 }, { "line": 118, "count": 2 }, { "line": 12, "count": 1 }, { "line": 54, "count": 2 }, { "line": 144, "count": 1 }, { "line": 86, "count": 2 }, { "line": 172, "count": 1 }, { "line": 76, "count": 2 }, { "line": 98, "count": 2 }, { "line": 140, "count": 1 }, { "line": 66, "count": 2 }, { "line": 108, "count": 2 }, { "line": 130, "count": 1 }, { "line": 80, "count": 2 }, { "line": 162, "count": 1 }, { "line": 112, "count": 2 }, { "line": 48, "count": 1 }, { "line": 18, "count": 1 }, { "line": 150, "count": 1 }, { "line": 50, "count": 1 }, { "line": 16, "count": 1 }, { "line": 154, "count": 1 }, { "line": 72, "count": 2 }, { "line": 175, "count": 1 }, { "line": 104, "count": 2 }, { "line": 40, "count": 1 }, { "line": 26, "count": 1 }, { "line": 158, "count": 1 }, { "line": 114, "count": 2 }, { "line": 8, "count": 1 }, { "line": 58, "count": 2 }, { "line": 82, "count": 2 }, { "line": 36, "count": 1 }, { "line": 168, "count": 1 }, { "line": 146, "count": 1 }, { "line": 30, "count": 1 }, { "line": 4, "count": 1 }, { "line": 126, "count": 1 }, { "line": 136, "count": 1 }, { "line": 94, "count": 2 }, { "line": 68, "count": 2 }, { "line": 62, "count": 2 }, { "line": 90, "count": 2 }, { "line": 122, "count": 2 }, { "line": 100, "count": 2 }] }, { "user": { "id": 1, "name": "Yolo Swagins", "email": "yolo@swag.me", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 2, "timeSpentByLine": [{ "line": 138, "count": 1 }, { "line": 64, "count": 1 }] }] };
                console.log(data);
            }
            for (var i = 0; i < data.userData.length; i++) {
                $scope.add_lines(data.userData[i], i, data.userData.length)
            }
        })
    }

    $scope.init();
}]);