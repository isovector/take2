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
	$scope.items = jQuery.parseJSON(filename);
	for (var i = 0; i < $scope.items.length; i++) {
            $scope.items[i].editedlast  = {"id":7, "time":new Date()};
        }
        $scope.getUsers();
	//$scope.getDirectories();
    }

    $scope.getDirectories = function() {
	console.log("Getting data");
	console.log("repo/" + $scope.filename);
	$http.get("/repo/" + $scope.filename).success(function(data) {
	     console.log("RESPONSE");
	     console.log(data);
	     $scope.items = data;
	     //TODO: switch to real date.. or just remove
	     for (var i = 0; i < $scope.items.length; i++) {
                 $scope.items[i].editedlast = {"id":7, "time":new Date()};
             }
             $scope.getUsers();
	});	
    }

    $scope.getUsers = function() {
	console.log("Getting users");
	for (var i = 0; i < $scope.items.length; i++) {
            // TODO: switch to actual number of users
            var numUsers = Math.floor((Math.random() * 5));
	    $scope.items[i].users = [{name:"Jeff", email:"ja6lee@uwaterloo.ca"}, {name:"Steve", email:"cool@cool.com"}];
            if (numUsers >= 3) {
		$scope.items[i].currentUsers = $scope.items[i].users[0].name + " and " + (numUsers - 1) + " Others";
	    } else if (numUsers == 2){
		$scope.items[i].currentUsers = $scope.items[i].users[0].name + " and " + $scope.items[i].users[1].name;
	    } else if (numUsers == 1) {
		$scope.items[i].currentUsers = $scope.items[i].users[0].name;
            } else {
		$scope.items[i].currentUsers = "";
	    }
	}
    }
}])

.directive('breadcrumbs', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/breadcrumbs.partial.html"
	}
});
