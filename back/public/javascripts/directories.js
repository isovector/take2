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
	});
	
    }
}]);
