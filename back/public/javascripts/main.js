var frostbite = angular.module('frostbite', ['ngResource', 'ui.listview']);

/**********************

Factories

**********************/
frostbite.factory('FileMetricsAPI', ['$resource', function($resource){
		return $resource('api/metrics/:endpoint/:fileOrSince', {}, {
			getAllFileInfo: {method:'GET', params: {endpoint: "all"}}, //Need parameter "file" - a file name
			getPopularSince: {method: "GET", params: {endpoint: "popular"}, isArray: true} //Need parameter "since" - dateTime
		});
	}
]);

frostbite.factory('CurrentStatsAPI', ['$resource', function($resource){
		return $resource('api/currently/:endpoint/:file', {}, {
			getAllOpen: {method:'GET', params: { endpoint: "open" }, isArray: true},
			getUsersInFile: {method: 'GET', params: { endpoint :"viewing"}} //Need parameter "file"
		});
	}
]);

frostbite.factory('GitRepoAPI', ['$resource', function($resource){
		return $resource('repo/:file', {}, {
			getFile: {method:'GET'}
		});
	}
]);

/**********************

Controllers

**********************/

frostbite.controller('FileController', ['$scope', 'FileMetricsAPI', function($scope, FileMetricsAPI) {
	FileMetricsAPI.getInfo({file: "testfile.txt"}, function(data) {
		console.log(data);
		$scope.fileMetrics = data;
	});
}]);

frostbite.controller('IndexController', ['$scope', 'CurrentStatsAPI', 'FileMetricsAPI', function($scope, CurrentStatsAPI, FileMetricsAPI) {
	CurrentStatsAPI.getAllOpen(function(data){
		console.log(data);
		$scope.openFiles = data;
	});

	var oneWeek = 7 * 24 * 60 * 60;
	FileMetricsAPI.getPopularSince({fileOrSince: Math.round(Date.now()/1000 - oneWeek)}, function(data) {
		console.log(data);
		$scope.popularOneWeek = data;
	});

	var oneDay = 24 * 60 * 60;
	FileMetricsAPI.getPopularSince({fileOrSince: Math.round(Date.now()/1000 - oneDay)}, function(data) {
		console.log(data);
		$scope.popularOneDay = data;
	});
}]);


/**********************

Directives

**********************/

frostbite.directive('frostbiteHeader', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/header.partial.html"
	}
});



frostbite.directive('timeSpent', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/timeSpent.partial.html"
	}
});

frostbite.directive('breadcrumbs', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/breadcrumbs.partial.html"
	}
});

frostbite.directive('currentFiles', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/currentFiles.partial.html",
		scope : {
			openFiles: '='
		}
	}
});

frostbite.directive('popularFiles', function() {
	return {
		restrict: 'A',
		replace: true,
		templateUrl: "/assets/directives/popularFiles.partial.html",
		scope : {
			popularFiles: '=files',
			timeSpan: '='
		}
	}
});

/**********************

Filters

**********************/

frostbite.filter('breadcrumbsFilter', function() {
    return function(input, index) {
    	var pathString = "";
    	for (var i = 0; i <= index; i++) {
    	 	pathString = pathString + "/" + input[i];
    	};
        return pathString;  
    }
});
