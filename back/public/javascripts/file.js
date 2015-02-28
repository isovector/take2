frostbite.controller('FileCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    $scope.lines = []
    $scope.lineItems = []
    $scope.path = ""
    $scope.showFile = true;
    $scope.useFakeData = false;
    $scope.userChartData = []


    $scope.init = function () {
        SyntaxHighlighter.all();
        $scope.popupGetter();

        $("[name='my-checkbox']").bootstrapSwitch();
        $('input[name="my-checkbox"]').bootstrapSwitch('onSwitchChange',(function () {
            console.log('kkkkk');
            if ($(".file-viewer").css("display") == "none") {
                $(".file-viewer").css("display", "block");
                $(".user-viewer").css("display", "none");
            }else{
                $(".file-viewer").css("display", "none");
                $(".user-viewer").css("display", "block");
                $scope.makeUserChart();
            }
        }));

        $("[name='fake-data']").bootstrapSwitch();
        $('input[name="fake-data"]').bootstrapSwitch('onSwitchChange', (function () {
            
            $scope.useFakeData = !$scope.useFakeData;
            console.log($scope.useFakeData)
            $scope.create_data();
        }));
    }

    $scope.makeUserChart = function(){
        var ctx = $("#userChart").get(0).getContext("2d");

        // TODO - Change this to finding "number" for extensibility
        //var lineSelected = $("[aria-describedby^='popover']");

        //lineSelected.css("background-color", "#ffffff")

        //var lineNum = parseInt(lineSelected.attr('class').split(/\s+/)[1].substring(6));

        /*var data = _.find($scope.lineItems, function (lineItem) {
            return lineItem.line == lineNum;
        }).chartData;
        console.log(data)*/

        var options = {
            legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%> <%=segments[i].value*5%>s<%}%></li><%}%></ul>",
            segmentShowStroke: true,
            animateRotate: true,
            animationEasing: "easeOutQuart"
        }
        console.log($scope.userChartData.length);
        var myNewChart = new Chart(ctx).Pie($scope.userChartData, options);
        var legend = myNewChart.generateLegend();
        $("#user-legend").html(legend);

        // Popup styling
        /*console.log($(".popover").css("left"))
        if ($(".popover").css("left").substring(0, 1) == "-") {
            console.log("farout"); $(".popover").css("left", "11px");
        }
        $(".popover-title").html("Users - Line " + lineNum);
        $(".arrow").css("display", "none");*/
    }

    // Checking for popup on page - modify it with appropriate data when it does
    $scope.popupGetter = function () {
        // I hate myself
        var checkExist = setInterval(function () {
            if ($('.popover').length) {
                $scope.makePopup();
                clearInterval(checkExist);
                var oldPop = $('.popover').attr("id");
                var stillExists = setInterval(function () {
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
            legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<segments.length; i++){%><li><span style=\"background-color:<%=segments[i].fillColor%>\"></span><%if(segments[i].label){%><%=segments[i].label%> <%=segments[i].value*5%>s<%}%></li><%}%></ul>",
            segmentShowStroke: true,
            animateRotate: true,
            animationEasing: "easeOutQuart"
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
            /*'#0000ff',
            '#00ff00',
            '#ff0000'*/
        ];
        var maxCount = 0;

        var maxLineCount = (_.max($scope.lineItems, function(lineItem){
            return lineItem.totalCount;
        })).totalCount;
        
        $scope.lineItems.forEach(function (lineItem) {
            
            if (lineItem.line < lines.length / 2 && lineItem.totalCount > 0) {
               
                console.log(lineItem.line);
                console.log(lineItem.totalCount)
                var color = 'background-color:';
                //Currently modifying color after page load, hence the modifying of style element
                //If we know importance on page load, revert to modifying css class name
                /*if (lineItem.totalCount > 2) {
                    color += colors[2];
                }
                else if (lineItem.totalCount > 1) {
                    color += colors[1];
                }
                else if (lineItem.totalCount > 0) {
                    color += colors[0];
                }*/
                color += colors[Math.floor(lineItem.totalCount/maxLineCount * 10)];
                color += ' !important';
               
                lines[lineItem.line - 1].style.cssText = color;
                //lines[(lineItem.line - 1) + lines.length / 2].style.cssText = color;

                // Highlighting the line on hover
                $(lines[(lineItem.line - 1) + lines.length / 2]).hover(
                    function () {
                        lines[lineItem.line - 1].style.cssText = 'background-color: #FFFF99 !important';
                        lines[(lineItem.line - 1) + lines.length / 2].style.cssText = 'background-color: #FFFF99 !important';
                    }, function () {
                        lines[lineItem.line - 1].style.cssText = color;
                        lines[(lineItem.line - 1) + lines.length / 2].style.cssText = 'background-color: #FFFFFF !important';
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

    $scope.add_user = function (userData, color) {
        /*console.log(color)
        var r = parseInt(color.substring(1, 2), 16);
        var g = parseInt(color.substring(2, 4), 16);
        var b = parseInt(color.substring(4, 6), 16);
       
        var rt = (r + (0.25 * (255 - r))).toString(16);
        var gt = (g + (0.25 * (255 - g))).toString(16);
        var bt = (b + (0.25 * (255 - b))).toString(16);
        console.log(bt);
        var highlightColor = '#' + rt.toString(10) + gt.toString(10) + bt.toString(10);*/

        var chartItem = {
            label: userData.user.name,
            value: userData.timeSpent,
            color: color,
            highlight: color
        }
        $scope.userChartData.push(chartItem);
        
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
                    highlight: user.color
                }
                chartData.push(chartItem);
            })
            lineItem["totalCount"] = totalCount;
            lineItem["chartData"] = chartData;
            return lineItem;
        });

        // Call the importance maker
        $scope.add_importance();
        return userColor;
    }
    
    
    // Get data and send to parser
    $scope.create_data = function () {
        $http.get("/api/metrics/all/" + $scope.path).success(function (data) {
            // Fake it til you make it
            if ($scope.useFakeData) {
                data = { "file": "back/app/controllers/SnapshotController.scala", "commit": "unimplemented", "userData": [{ "user": { "id": 1, "name": "Adam Sils", "email": "silsadam@gmail.com", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 28, "timeSpentByLine": [{ "line": 40, "count": 1 }, { "line": 41, "count": 1 }, { "line": 42, "count": 1 }, { "line": 43, "count": 1 }, { "line": 44, "count": 1 }, { "line": 45, "count": 2 }, { "line": 46, "count": 1 }, { "line": 47, "count": 1 }, { "line": 48, "count": 2 }, { "line": 49, "count": 2 }, { "line": 50, "count": 2 }, { "line": 51, "count": 2 }, { "line": 52, "count": 2 }, { "line": 53, "count": 1 }, { "line": 54, "count": 1 }, { "line": 55, "count": 1 }, { "line": 56, "count": 1 }, { "line": 57, "count": 1 }, { "line": 58, "count": 1 }, { "line": 59, "count": 1 }, { "line": 60, "count": 1 }] }, { "user": { "id": 3, "name": "Sandy Maguire", "email": "sandy@sandymaguire.me", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 45, "timeSpentByLine": [{ "line": 30, "count": 1 }, { "line": 31, "count": 1 }, { "line": 32, "count": 1 }, { "line": 33, "count": 1 }, { "line": 34, "count": 1 }, { "line": 35, "count": 2 }, { "line": 36, "count": 1 }, { "line": 37, "count": 1 }, { "line": 38, "count": 2 }, { "line": 39, "count": 2 }, { "line": 40, "count": 2 }, { "line": 41, "count": 2 }, { "line": 42, "count": 2 }, { "line": 43, "count": 1 }, { "line": 44, "count": 1 }, { "line": 45, "count": 1 }, { "line": 6, "count": 1 }, { "line": 7, "count": 1 }, { "line": 8, "count": 1 }, { "line": 9, "count": 1 }, { "line": 10, "count": 1 }, { "line": 11, "count": 1 }, { "line": 12, "count": 1 }, { "line": 13, "count": 1 }, { "line": 14, "count": 1 }, { "line": 15, "count": 1 }, { "line": 16, "count": 1 }, { "line": 17, "count": 1 }, { "line": 18, "count": 1 }] }, { "user": { "id": 2, "name": "Yolo Swagins", "email": "yolo@swag.me", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 8, "timeSpentByLine": [{ "line": 20, "count": 2 }, { "line": 21, "count": 2 }, { "line": 22, "count": 2 }, { "line": 23, "count": 2 }, { "line": 43, "count": 1 }, { "line": 44, "count": 1 }, { "line": 45, "count": 1 }, { "line": 54, "count": 2 }, { "line": 55, "count": 2 }] }] }
            }
            debugger;
            for (var i = 0; i < data.userData.length; i++) {
                var curColor = $scope.add_lines(data.userData[i], i, data.userData.length);
                $scope.add_user(data.userData[i], curColor);
            }
        }).error(function (data) {
            if ($scope.useFakeData) {
                data = { "file": "back/app/controllers/SnapshotController.scala", "commit": "unimplemented", "userData": [{ "user": { "id": 1, "name": "Adam Sils", "email": "silsadam@gmail.com", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 28, "timeSpentByLine": [{ "line": 40, "count": 1 }, { "line": 41, "count": 1 }, { "line": 42, "count": 1 }, { "line": 43, "count": 1 }, { "line": 44, "count": 1 }, { "line": 45, "count": 2 }, { "line": 46, "count": 1 }, { "line": 47, "count": 1 }, { "line": 48, "count": 2 }, { "line": 49, "count": 2 }, { "line": 50, "count": 2 }, { "line": 51, "count": 2 }, { "line": 52, "count": 2 }, { "line": 53, "count": 1 }, { "line": 54, "count": 1 }, { "line": 55, "count": 1 }, { "line": 56, "count": 1 }, { "line": 57, "count": 1 }, { "line": 58, "count": 1 }, { "line": 59, "count": 1 }, { "line": 60, "count": 1 }] }, { "user": { "id": 3, "name": "Sandy Maguire", "email": "sandy@sandymaguire.me", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 45, "timeSpentByLine": [{ "line": 30, "count": 1 }, { "line": 31, "count": 1 }, { "line": 32, "count": 1 }, { "line": 33, "count": 1 }, { "line": 34, "count": 1 }, { "line": 35, "count": 2 }, { "line": 36, "count": 1 }, { "line": 37, "count": 1 }, { "line": 38, "count": 2 }, { "line": 39, "count": 2 }, { "line": 40, "count": 2 }, { "line": 41, "count": 2 }, { "line": 42, "count": 2 }, { "line": 43, "count": 1 }, { "line": 44, "count": 1 }, { "line": 45, "count": 1 }, { "line": 6, "count": 1 }, { "line": 7, "count": 1 }, { "line": 8, "count": 1 }, { "line": 9, "count": 1 }, { "line": 10, "count": 1 }, { "line": 11, "count": 1 }, { "line": 12, "count": 1 }, { "line": 13, "count": 1 }, { "line": 14, "count": 1 }, { "line": 15, "count": 1 }, { "line": 16, "count": 1 }, { "line": 17, "count": 1 }, { "line": 18, "count": 1 }] }, { "user": { "id": 2, "name": "Yolo Swagins", "email": "yolo@swag.me", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 8, "timeSpentByLine": [{ "line": 20, "count": 2 }, { "line": 21, "count": 2 }, { "line": 22, "count": 2 }, { "line": 23, "count": 2 }, { "line": 43, "count": 1 }, { "line": 44, "count": 1 }, { "line": 45, "count": 1 }, { "line": 54, "count": 2 }, { "line": 55, "count": 2 }] }] }
                for (var i = 0; i < data.userData.length; i++) {
                    var curColor = $scope.add_lines(data.userData[i], i, data.userData.length)
                    $scope.add_user(data.userData[i], curColor);
                }
            }
        });

        
    }

    $scope.init();
}]);