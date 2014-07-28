frostbite.controller('FileCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    $scope.lines_highlighted = []

    $scope.inity = function () {
        SyntaxHighlighter.all();
        $scope.create_test_data();
        $scope.highlight([2, 3, 4]); // call this elsewhere? or with corect parameters

    }

    $scope.setFileContents = function (filestuff) {
        console.log(filestuff);
        $scope.pathArray = filestuff.path.split("/");
        $('#file_brush').html(filestuff.contents);
    }

    $scope.highlight = function (line_nums) {
        console.log("into highlight");
        var preTags = document.getElementsByTagName('pre');
        for (var i = 0; i < preTags.length; i++) {
            var newlines = preTags[i].innerHTML.match(/\n/g);
            console.log(newlines);
            if (newlines && !/\bhighlight\b/.test(preTags[i].className)) {
                preTags[i].className += (preTags[i].className.charAt(preTags[i].className.length - 1) != ';' ? ';' : '')
                            + 'highlight:[' + function () {
                                var a = [];
                                console.log(line_nums);
                                a = line_nums;
                                return a
                            }() + ']';
            }
        }
    }

    $scope.add_importance = function () {
        console.log("intoimportance");
        var lines = $('.highlighted');
        console.log(lines);
        for (var i = 0; i < lines.length / 2; i++) {
            //Currently modifying color after page load, hence the modifying of style element
            //If we know importance on page load, revert to modifying css class name
            if ($scope.lines_highlighted[i] < 5) {
                lines[i].style.cssText = 'background-color:#ffc0cb !important';//lines[i].className = lines[i].className + " one";
                lines[i + lines.length / 2].style.cssText = 'background-color:#ffc0cb !important';//lines[i].className = lines[i].className + " one";
            } else if ($scope.lines_highlighted[i] < 10) {
                lines[i].style.cssText = 'background-color:#ff80cb !important';//lines[i].className = lines[i].className + " two";
                lines[i + lines.length / 2].style.cssText = 'background-color:#ff80cb !important';//lines[i].className = lines[i].className + " two";
            } else {
                lines[i].style.cssText = 'background-color:#ff00cb !important';//lines[i].className = lines[i].className + " three";
                lines[i + lines.length / 2].style.cssText = 'background-color:#ff00cb !important';//lines[i].className = lines[i].className + " three";
            }
        }
    }
    $scope.create_test_data = function () {

        $scope.lines_highlighted.push(2);
        $scope.lines_highlighted.push(6);
        $scope.lines_highlighted.push(1);
    }

    $scope.inity();
}]);