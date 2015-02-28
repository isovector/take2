frostbite.filter('percent', function() {
	return function(input) {
		return (input*100).toFixed(2);
	}
})
frostbite.controller('ProfileCtrl', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {
    $scope.user = {};

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
   }	   
}]);
