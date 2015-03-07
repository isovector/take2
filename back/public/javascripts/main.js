var frostbite = angular.module('frostbite', ['ngResource', 'ui.listview', 'ui.bootstrap']);

frostbite.filter('addSlash', function() {
	return function(input, index, pathArray) {
		if (pathArray.length - 1 == index) {
			return input;
		} else {
			return input + "/";
		}
	}
})

frostbite.filter('pathName', function() {
       return function(input) {
               return input.replace(/^.*[\\\/]/, '')
       }
})
frostbite.filter('timeago', function() {
       return function(input) {
               var substitute = function (stringOrFunction, number, strings) {
                       var string = $.isFunction(stringOrFunction) ? stringOrFunction(number, dateDifference) : stringOrFunction;
                       var value = (strings.numbers && strings.numbers[number]) || number;
                       return string.replace(/%d/i, value);
               },
       //nowTime = (new Date()).getTime(),
       //date = (new Date(input)).getTime(),
       //refreshMillis= 6e4, //A minute
       allowFuture = true,
       strings= {
               prefixAgo: null,
       prefixFromNow: null,
       suffixAgo: "",
       suffixFromNow: "",
       seconds: "less than a minute",
       minute: "about a minute",
       minutes: "%d minutes",
       hour: "about an hour",
       hours: "about %d hours",
       day: "a day",
       days: "%d days",
       month: "about a month",
       months: "%d months",
       year: "about a year",
       years: "%d years"
       },
       dateDifference = input*1000*5,
       words,
       seconds = Math.abs(dateDifference) / 1000,
       minutes = seconds / 60,
       hours = minutes / 60,
       days = hours / 24,
       years = days / 365,
       separator = strings.wordSeparator === undefined ?  " " : strings.wordSeparator,

       prefix = strings.prefixAgo,
       suffix = strings.suffixAgo;

       if (allowFuture) {
               if (dateDifference < 0) {
                       prefix = strings.prefixFromNow;
                       suffix = strings.suffixFromNow;
               }
       }

       words = seconds < 45 && substitute(strings.seconds, Math.round(seconds), strings) ||
       seconds < 90 && substitute(strings.minute, 1, strings) ||
       minutes < 45 && substitute(strings.minutes, Math.round(minutes), strings) ||
       minutes < 90 && substitute(strings.hour, 1, strings) ||
       hours < 24 && substitute(strings.hours, Math.round(hours), strings) ||
       hours < 42 && substitute(strings.day, 1, strings) ||
       days < 30 && substitute(strings.days, Math.round(days), strings) ||
       days < 45 && substitute(strings.month, 1, strings) ||
       days < 365 && substitute(strings.months, Math.round(days / 30), strings) ||
       years < 1.5 && substitute(strings.year, 1, strings) ||
       substitute(strings.years, Math.round(years), strings);

       return $.trim([prefix, words, suffix].join(separator));
	}
});
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

frostbite.factory('FileSearchAPI', ['$resource', function($resource){
		return $resource('/api/search/directory/:file', {}, {
			getFile: {method:'GET', isArray: true}
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

//Used in the typeahead for the header
frostbite.controller('SearchTypeaheadController', ['$scope', 'FileSearchAPI', function($scope, FileSearchAPI) {
	$scope.files = [];
	$scope.getFileSearch = function(val) {
		FileSearchAPI.getFile({file: val}, function(data){
			$scope.files = data;
		});
	};

	$scope.goToFile = function(result) {
		window.location = "/directory/" + result.path;
	};
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
