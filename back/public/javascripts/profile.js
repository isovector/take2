frostbite.filter('percent', function() {
	return function(input) {
		return (input*100).toFixed(2);
	}
})
frostbite.controller('ProfileCtrl', ['$scope','$filter', '$http', '$timeout', function($scope, $filter, $http, $timeout) {
    $scope.user = {};

	var orderBy = $filter('orderBy');
	$scope.orderByPercentile = function() {
		$scope.user.expertise = orderBy($scope.user.expertise, ['-knowledge', 'filename'], false);
	}

	$scope.init = function(data) {
		$scope.user = data;
		console.log(data);
		$scope.getPicture();
		$scope.getExpertise();
	};

	$scope.getPicture = function() {
	    var emailLowerCase = $scope.user.email.toLowerCase();
		var hashEmail = CryptoJS.MD5( emailLowerCase );
		$scope.user.picture = "http://www.gravatar.com/avatar/" + hashEmail + "?s=250";
	}

   $scope.getExpertise = function() {
		var array = $.map($scope.user.expertise, function(value, index) {
			return {filename: index, knowledge: value};
		});
		$scope.user.expertise = array;
		$scope.orderByPercentile();
   }	   
}]);
