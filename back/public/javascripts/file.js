frostbite.controller('FileCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    $scope.lines = []
    $scope.path = ""
    $scope.showFile = true;

    $scope.inity = function () {
        SyntaxHighlighter.all();
    }

    $scope.init_data = function () {
        $scope.create_test_data();
        //$scope.highlight([2, 3, 4]); // call this elsewhere? or with corect parameters
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
        $scope.init_data();
    }

    $scope.highlight = function (line_nums) {
        console.log(line_nums);
        var preTags = document.getElementsByTagName('pre');
        for (var i = 0; i < preTags.length; i++) {
            var newlines = preTags[i].innerHTML.match(/\n/g);
            console.log(newlines);
            if (newlines && !/\bhighlight\b/.test(preTags[i].className)) {
                preTags[i].className += (preTags[i].className.charAt(preTags[i].className.length - 1) != ';' ? ';' : '')
                            + 'highlight:[' + function () {
                                var a = [2,3,4];
                                console.log(line_nums);
                                a = line_nums;
                                return a
                            }() + ']';
            }
        }
    }

    $scope.add_importance = function (line_nums) {
        console.log("intoimportance");
        var lines = $('.line');
        console.log(lines);
        line_nums.forEach(function (num) {
            if (num < 148) {
                console.log(num);
                //Currently modifying color after page load, hence the modifying of style element
                //If we know importance on page load, revert to modifying css class name
                //if ($scope.lines_highlighted[i] < 5) {

                lines[num].style.cssText = 'background-color:#ffc0cb !important';//lines[i].className = lines[i].className + " one";
                //lines[num*2].style.cssText = 'background-color:#ffc0cb !important';//lines[i].className = lines[i].className + " one";
            }
            //lines[i + lines.length / 2].style.cssText = 'background-color:#ffc0cb !important';//lines[i].className = lines[i].className + " one";
            /*} else if ($scope.lines_highlighted[i] < 10) {
                lines[i].style.cssText = 'background-color:#ff80cb !important';//lines[i].className = lines[i].className + " two";
                lines[i + lines.length / 2].style.cssText = 'background-color:#ff80cb !important';//lines[i].className = lines[i].className + " two";
            } else {
                lines[i].style.cssText = 'background-color:#ff00cb !important';//lines[i].className = lines[i].className + " three";
                lines[i + lines.length / 2].style.cssText = 'background-color:#ff00cb !important';//lines[i].className = lines[i].className + " three";
            }*/
        });
    }

    $scope.add_lines = function (userData) {
        console.log(userData);
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
            $scope.lines[$scope.lines.length - 1].users.push(tempObj);
        });
        console.log($scope.lines);
        $scope.add_importance(_.map($scope.lines, function (lineItem) {
            return lineItem.line;
        }));
    }
    

    $scope.create_test_data = function () {
        $http.get("/api/metrics/all/" + $scope.path).success(function (data) {
            //fake it til you make it
            data = { "file": "back/app/controllers/SnapshotController.scala", "commit": "unimplemented", "userData": [{ "user": { "id": 1, "name": "Sandy Maguire", "email": "sandy@sandymaguire.me", "picture": "unimplemented", "lastActivity": 1406511540074 }, "timeSpent": 2, "timeSpentByLine": [{ "line": 138, "count": 1 }, { "line": 88, "count": 2 }, { "line": 170, "count": 1 }, { "line": 120, "count": 2 }, { "line": 10, "count": 1 }, { "line": 56, "count": 2 }, { "line": 142, "count": 1 }, { "line": 174, "count": 1 }, { "line": 42, "count": 1 }, { "line": 24, "count": 1 }, { "line": 52, "count": 1 }, { "line": 14, "count": 1 }, { "line": 110, "count": 2 }, { "line": 20, "count": 1 }, { "line": 46, "count": 1 }, { "line": 152, "count": 1 }, { "line": 78, "count": 2 }, { "line": 164, "count": 1 }, { "line": 106, "count": 2 }, { "line": 84, "count": 2 }, { "line": 132, "count": 1 }, { "line": 116, "count": 2 }, { "line": 74, "count": 2 }, { "line": 6, "count": 1 }, { "line": 60, "count": 2 }, { "line": 102, "count": 2 }, { "line": 28, "count": 1 }, { "line": 38, "count": 1 }, { "line": 160, "count": 1 }, { "line": 70, "count": 2 }, { "line": 92, "count": 2 }, { "line": 156, "count": 1 }, { "line": 124, "count": 2 }, { "line": 96, "count": 2 }, { "line": 134, "count": 1 }, { "line": 128, "count": 1 }, { "line": 2, "count": 1 }, { "line": 166, "count": 1 }, { "line": 32, "count": 1 }, { "line": 34, "count": 1 }, { "line": 148, "count": 1 }, { "line": 64, "count": 2 }, { "line": 22, "count": 1 }, { "line": 44, "count": 1 }, { "line": 118, "count": 2 }, { "line": 12, "count": 1 }, { "line": 54, "count": 2 }, { "line": 144, "count": 1 }, { "line": 86, "count": 2 }, { "line": 172, "count": 1 }, { "line": 76, "count": 2 }, { "line": 98, "count": 2 }, { "line": 140, "count": 1 }, { "line": 66, "count": 2 }, { "line": 108, "count": 2 }, { "line": 130, "count": 1 }, { "line": 80, "count": 2 }, { "line": 162, "count": 1 }, { "line": 112, "count": 2 }, { "line": 48, "count": 1 }, { "line": 18, "count": 1 }, { "line": 150, "count": 1 }, { "line": 50, "count": 1 }, { "line": 16, "count": 1 }, { "line": 154, "count": 1 }, { "line": 72, "count": 2 }, { "line": 175, "count": 1 }, { "line": 104, "count": 2 }, { "line": 40, "count": 1 }, { "line": 26, "count": 1 }, { "line": 158, "count": 1 }, { "line": 114, "count": 2 }, { "line": 8, "count": 1 }, { "line": 58, "count": 2 }, { "line": 82, "count": 2 }, { "line": 36, "count": 1 }, { "line": 168, "count": 1 }, { "line": 146, "count": 1 }, { "line": 30, "count": 1 }, { "line": 4, "count": 1 }, { "line": 126, "count": 1 }, { "line": 136, "count": 1 }, { "line": 94, "count": 2 }, { "line": 68, "count": 2 }, { "line": 62, "count": 2 }, { "line": 90, "count": 2 }, { "line": 122, "count": 2 }, { "line": 100, "count": 2 }] }] }
            console.log(data);
            for (var i = 0; i < data.userData.length; i++) {
                $scope.add_lines(data.userData[i])
            }
           
        })//TODO highlight lines based on data
        $scope.highlight(7);
        //$scope.lines_highlighted.push(6);
        //$scope.lines_highlighted.push(1);
    }

    $scope.inity();
}]);