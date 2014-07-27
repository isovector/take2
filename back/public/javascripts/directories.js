var ROOTURL = "http://0.0.0.0:9000/"

var directories = angular.module('directories', ['ui.listview'])
.filter('isDirectory', function() {
    return function(input) {
        if (input.isDir) {
	    return ROOTURL + "assets/images/directory.png"
	} else {
	    return ROOTURL + "assets/images/file.png"
	}	    
    }
})

.filter('breadcrumbsFilter', function() {
    return function(input, path) {
    	console.log(path);
    	var index = _.findIndex(input, function(a) { return a == path});
    	console.log(index);
    	var pathString = "";
    	for (var i = 0; i <= index; i++) {
    	 	pathString = pathString + "/" + input[i];
    	};
        return pathString;  
    }
})
.controller('DirecCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.filename = "";

    $scope.items = []
    

    $scope.setFilename = function(filename) {
	console.log("filename = " + filename);
	$scope.filename = filename;
        $scope.getDirectories();
    }

    $scope.getDirectories = function() {
	console.log("Getting data");
	console.log("repo/" + $scope.filename);
	$http.get("/repo/" + $scope.filename).success(function(data) {
	   console.log("RESPONSE");
	   console.log(data);
	   $scope.items = data;
	   //Split our path, then pop the last element so we get the right level
	   $scope.pathArray = data[0].path.split("/");
	   $scope.pathArray.pop();
	   console.log($scope.pathArray)
	});
	
    }
}])

.directive('breadcrumbs', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/breadcrumbs.partial.html"
	}
});
