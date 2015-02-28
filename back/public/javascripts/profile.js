frostbite.controller('ProfileCtrl', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {
    $scope.user = {};

	$scope.init = function(data) {
		console.log("INIT");
		$scope.user = data;
		console.log(data);
		$scope.getPicture();
	};

	$scope.getPicture = function() {
	    var emailLowerCase = $scope.user.email.toLowerCase();
		var hashEmail = CryptoJS.MD5( emailLowerCase );
		$scope.user.picture = "http://www.gravatar.com/avatar/" + hashEmail + "?s=250";
	}
 
}]);

